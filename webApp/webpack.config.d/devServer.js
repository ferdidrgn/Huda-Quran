// Local dev server (`./gradlew :webApp:jsBrowserDevelopmentRun` /
// `:webApp:wasmJsBrowserDevelopmentRun`) serves plain static files with no knowledge of the
// app's client-side routes. Navigating (or refreshing) directly to e.g. /surah/67 hits
// webpack-dev-server's own 404 ("Cannot GET /surah/67") before index.html — and therefore the
// Kotlin app that would parse that path — ever loads.
//
// Firebase Hosting already rewrites every path to index.html in production (see
// firebase.json's "rewrites"), which is exactly the same SPA-fallback behavior
// `historyApiFallback` gives the dev server: serve index.html for any GET that doesn't match a
// real static file, and let client-side routing take it from there.
config.devServer = {
    ...(config.devServer || {}),
    historyApiFallback: true,
};
