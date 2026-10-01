---
name: huda-design
description: Project design system and rules for Huda Qur'an's UI (Compose Multiplatform — Android, iOS, web canvas). Use before creating or restyling ANY screen, component, theme, animation or web page in this repo. Adapts the generic frontend-design and mobile-design skills to this codebase.
---

# Huda Qur'an design system

This app is **Kotlin + Compose Multiplatform** (not Flutter, not React Native). One `commonMain`
UI tree renders on Android, iOS and the web (Compose on a `<canvas>` — Kotlin/JS + Wasm). The
generic skills `frontend-design` and `mobile-design` still apply; this file translates them.

Read first: `frontend-design/SKILL.md` (aesthetic direction), `mobile-design/SKILL.md`,
`mobile-design/touch-psychology.md`, `mobile-design/mobile-performance.md`.

## Subject, audience, job

- Subject: reading and listening to the Qur'an, prayer times, duas, Esma-ül Hüsna.
- Audience: Turkish-first Muslim readers, many elderly; also 7 other languages incl. Arabic (RTL).
- Primary job: open the app → continue reading within one tap. Calm, reverent, uncluttered.
- Visual vernacular to draw from: illuminated mushaf pages, girih/eight-point-star geometry,
  gilt ornament, paper and ink, dusk light. Never cartoonish, never "SaaS dashboard".

## Palette (default theme = Sakura)

The user chose the Sakura dusk palette as the **main** theme. Tokens live in
`ui/theme/Color.kt`; screens must read `MaterialTheme.colorScheme`, never hard-coded hex.

| Token | Hex | Role |
|---|---|---|
| SakuraInk | #1B0C1A | background |
| SakuraSurface | #2D222F | cards / surface |
| SakuraWine | #4B2138 | surfaceVariant, secondary containers |
| SakuraPlum | #6D3C52 | primaryContainer, outlines |
| SakuraMauve | #765D67 | secondary text, dividers |
| SakuraBlossom | #FADCD5 | primary accent + body text |

Light/Dark (gilt + emerald) remain as options. Mushaf paper/ink/gilt are fixed and theme-independent.

## Compose translations of the generic rules

| Generic rule (RN/Flutter) | Do this here |
|---|---|
| FlatList / ListView.builder | `LazyColumn` / `LazyVerticalGrid` with a stable `key =` (never index) |
| React.memo / const widgets | stable params, `remember`, `derivedStateOf`; hoist state, pass lambdas |
| useNativeDriver / GPU props | animate `graphicsLayer { alpha / scaleX / translationY / rotationZ }`, not size/padding |
| 44pt / 48dp targets | `Modifier.minimumInteractiveComponentSize()` or ≥ 48.dp; ≥ 8.dp between targets |
| Loading / error states | every remote load shows a progress indicator and an error with a retry button |
| Deep links day one | every new `Screen` gets `DeepLink.parse` + `toPath` + a round-trip test |
| Platform divergence | `currentPlatform` (`platform/Platform.kt`) — WEB gets top nav + wide layouts |
| Reduced motion | keep motion short (≤ 400 ms), one orchestrated entrance per screen (`StaggeredEntrance`) |

## Layout & responsiveness

- Breakpoints: `ui/components/WindowSizeClass.kt` (COMPACT < 600dp ≤ MEDIUM < 1200dp ≤ EXPANDED).
- Lists/grids: `GridCells.Adaptive(minSize = …)` so phones get 1–2 columns and web/tablet fill the width.
- Long-form reading screens stay capped (`isReadingScreen` in `App.kt`); list/dashboard screens use full width.
- Width caps: write `Modifier.widthIn(max = X).fillMaxWidth()`, never `fillMaxWidth().widthIn(max = X)` —
  the second form is a silent no-op (the fixed incoming width wins), which is what stretched the web
  pages edge to edge. Centre the capped column with the parent's `contentAlignment = TopCenter`.
- Web is a real website: top navigation, wide hero, multi-column sections; not a stretched phone.
- Horizontal padding 16.dp (phone) / 24–32.dp (web); section rhythm 24–28.dp.

## Motifs & signature element

- `IslamicMotifBackground` — faint geometric watermark (alpha ≤ 0.04) behind screens.
- `SectionHeader` — decorative header with ornament; use instead of ad-hoc title rows.
- `PageHeader` — every non-home screen's title (display face + subtitle + `OrnamentRule`); pass `onBack` for pushed screens.
- `OrnamentRule` / `IlluminatedFrame` — gilt hairline dividers and the hero frame (`Ornament.kt`).
- `HudaSearchField` / `FilterPill` — the pill search input and filter chips; no stock OutlinedTextField for search.
- `StarNumberBadge` — surah/ayah numbers; `ShamsaRosette` — the signature medallion (home hero, splash, player).
- `GlassSurface` — the card. Vary shape by hierarchy (hero 28.dp, card 22.dp, chip 12.dp).
- Spend boldness in ONE place per screen (e.g. Home hero with the next prayer countdown,
  Mushaf's gilt page frame). Everything else stays quiet.

## Avoid (tells of generated UI)

Emoji as the only iconography on primary actions; identical cards for everything; gradient washes
as decoration; ALL-CAPS eyebrows; accenting one word in a headline; fade-up on every section;
hard-coded Turkish strings (all copy goes through `ui/localization/Strings.kt`, 8 languages).

## Ads

In-feed native ad every `LIST_AD_INTERVAL` (10) rows via `ListAdCard` / `showListAdAfter`; gate
with `adsSupported && !preferences.isAdFree()`. Never place ads beside playback controls.

## Checkpoint before writing UI code

State: screen, platforms affected, the one signature element, which tokens/components you reuse,
loading/error/empty states, and how it behaves at COMPACT and EXPANDED widths.
