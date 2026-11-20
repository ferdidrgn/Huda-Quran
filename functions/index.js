const { onRequest } = require("firebase-functions/v2/https");
const { defineSecret, defineString } = require("firebase-functions/params");

const clientId = defineSecret("QF_CLIENT_ID");
const clientSecret = defineSecret("QF_CLIENT_SECRET");
const environment = defineString("QF_ENVIRONMENT", { default: "prelive" });
const scopeMode = defineString("QF_CONTENT_SCOPE_MODE", { default: "granular" });
const tokenCache = new Map();
const rateBuckets = new Map();
const PREFIX = "/api/qf/";
const CONTENT_HOSTS = {
  prelive: { api: "https://apis-prelive.quran.foundation", auth: "https://prelive-oauth2.quran.foundation" },
  production: { api: "https://apis.quran.foundation", auth: "https://oauth2.quran.foundation" },
};
const CONTENT_QUERY = new Set([
  "language", "page", "per_page", "audio", "translations", "tafsirs", "segments",
]);
const SEARCH_QUERY = new Set([
  "mode", "query", "page", "size", "translation_ids", "filter_translations",
  "exact_matches_only", "get_text", "highlight", "navigationalResultsNumber",
  "versesResultsNumber", "indexes",
]);

function allowedRoute(path) {
  const chapter = "(?:[1-9]|[1-9]\\d|10\\d|11[0-4])";
  return new RegExp(`^(?:content/api/v4/(?:chapters|resources/(?:recitations|chapter_reciters|translations|tafsirs)|verses/by_chapter/${chapter}|chapter_recitations/[1-9]\\d*(?:/${chapter})?)|search/api/v1/search)$`).test(path);
}

function validateQuery(url, allowlist, isSearch) {
  const result = new URLSearchParams();
  for (const [key, value] of url.searchParams.entries()) {
    if (!allowlist.has(key) || value.length > (key === "query" ? 200 : 500)) {
      throw Object.assign(new Error("Invalid query parameter"), { status: 400 });
    }
    result.append(key, value);
  }
  if (isSearch && (!result.get("query") || !["quick", "advanced"].includes(result.get("mode")))) {
    throw Object.assign(new Error("Search requires mode and query"), { status: 400 });
  }
  if (result.has("page") && (!/^\d+$/.test(result.get("page")) || Number(result.get("page")) < 1)) {
    throw Object.assign(new Error("Invalid page"), { status: 400 });
  }
  if (result.has("per_page") && (!/^\d+$/.test(result.get("per_page")) || Number(result.get("per_page")) > 50)) {
    throw Object.assign(new Error("Invalid page size"), { status: 400 });
  }
  return result;
}

function checkRateLimit(req, now = Date.now()) {
  const key = req.ip || "unknown";
  const bucket = rateBuckets.get(key);
  if (!bucket || now - bucket.start >= 60_000) {
    if (rateBuckets.size >= 5_000) rateBuckets.delete(rateBuckets.keys().next().value);
    rateBuckets.set(key, { start: now, count: 1 });
    return true;
  }
  bucket.count += 1;
  return bucket.count <= 60;
}

async function accessToken(scope, cfg, fetchImpl = fetch, now = Date.now()) {
  const cacheKey = `${cfg.id}:${scope}`;
  const cached = tokenCache.get(cacheKey);
  if (cached && cached.expiresAt > now + 30_000) return cached.value;

  const response = await fetchImpl(`${cfg.auth}/oauth2/token`, {
    method: "POST",
    headers: {
      authorization: `Basic ${Buffer.from(`${cfg.id}:${cfg.secret}`).toString("base64")}`,
      "content-type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "client_credentials",
      scope,
    }),
    signal: AbortSignal.timeout(10_000),
  });
  if (!response.ok) throw Object.assign(new Error("OAuth token request failed"), { status: 502 });
  const data = await response.json();
  if (typeof data.access_token !== "string" || !Number.isFinite(Number(data.expires_in))) {
    throw Object.assign(new Error("OAuth token response invalid"), { status: 502 });
  }
  tokenCache.set(cacheKey, { value: data.access_token, expiresAt: now + Number(data.expires_in) * 1000 });
  return data.access_token;
}

async function proxyRequest(req, res, deps = {}) {
  const fetchImpl = deps.fetch || fetch;
  try {
    if (req.method !== "GET") return res.status(405).set("Allow", "GET").json({ error: "method_not_allowed" });
    if (!checkRateLimit(req)) return res.status(429).json({ error: "rate_limited" });
    const url = new URL(req.originalUrl || req.url, "https://hudaquran.web.app");
    if (!url.pathname.startsWith(PREFIX)) return res.status(404).json({ error: "not_found" });
    const route = decodeURIComponent(url.pathname.slice(PREFIX.length));
    if (route.includes("..") || route.includes("\\") || !allowedRoute(route)) {
      return res.status(404).json({ error: "not_found" });
    }
    const isSearch = route.startsWith("search/");
    const query = validateQuery(url, isSearch ? SEARCH_QUERY : CONTENT_QUERY, isSearch);
    const env = deps.environment || environment.value();
    const hosts = CONTENT_HOSTS[env];
    if (!hosts) return res.status(500).json({ error: "proxy_not_configured" });
    const cfg = { id: deps.clientId || clientId.value(), secret: deps.clientSecret || clientSecret.value(), auth: hosts.auth };
    const contentScopeMode = deps.scopeMode || scopeMode.value();
    let scope = "search";
    if (!isSearch) {
      if (contentScopeMode === "legacy") {
        scope = "content";
      } else {
        const scopes = new Set(["content.quran.read"]);
        if (route.includes("resources/recitations") || route.includes("resources/chapter_reciters") || route.includes("chapter_recitations/")) {
          scopes.add("content.audio.read");
        }
        if (route.includes("resources/translations") || query.has("translations")) {
          scopes.add("content.translations.read");
        }
        if (route.includes("resources/tafsirs") || query.has("tafsirs")) {
          scopes.add("content.tafsirs.read");
        }
        scope = [...scopes].join(" ");
      }
    }
    const targetBase = isSearch ? `${hosts.api}/search` : `${hosts.api}`;
    const upstreamPath = isSearch ? "/api/v1/search" : `/${route}`;
    let token = await accessToken(scope, cfg, fetchImpl);
    let upstream = await fetchImpl(`${targetBase}${upstreamPath}?${query}`, {
      headers: { accept: "application/json", "x-auth-token": token, "x-client-id": cfg.id },
      signal: AbortSignal.timeout(20_000),
    });
    if (upstream.status === 401) {
      tokenCache.delete(`${cfg.id}:${scope}`);
      token = await accessToken(scope, cfg, fetchImpl);
      upstream = await fetchImpl(`${targetBase}${upstreamPath}?${query}`, {
        headers: { accept: "application/json", "x-auth-token": token, "x-client-id": cfg.id },
        signal: AbortSignal.timeout(20_000),
      });
    }
    const text = await upstream.text();
    if (text.length > 1_000_000) return res.status(502).json({ error: "upstream_response_too_large" });
    if (!upstream.ok) return res.status(upstream.status === 403 ? 403 : 502).json({ error: "upstream_request_failed" });
    res.set("Cache-Control", "public, max-age=0, s-maxage=300");
    res.type("application/json").status(200).send(text);
  } catch (error) {
    const status = error && error.status === 400 ? 400 : 502;
    return res.status(status).json({ error: status === 400 ? "invalid_request" : "upstream_unavailable" });
  }
}

exports.quranFoundationProxy = onRequest(
  { region: "us-central1", secrets: [clientId, clientSecret], timeoutSeconds: 30, memory: "256MiB" },
  (req, res) => proxyRequest(req, res),
);
