# UI/UX Name Explorer (v3)

Offline Android app (Kotlin, Jetpack Compose, Material 3) with a curated library of 127 UI/UX design names across 24 categories, live Compose previews, a component explorer, favorites/history (Room), and an optional, user-configured AI model connection.

## What's new in v3
- **Smooth finger-only zoom & pan (`InteractivePhoneViewport`):**
  - Zoom and pan now run on the GPU via `Modifier.graphicsLayer` instead of resizing layout dimensions on every frame, eliminating layout/text re-measurement jank during pinch gestures.
  - **Unified multi-touch gesture engine:** Putting 2 fingers anywhere on the phone (even directly over elements) smoothly pinch-zooms (`85%–450%`) and pans the phone canvas without child elements hijacking fingers or dragging apart.
  - **Fixed horizontal pan lock:** Unlocked full 2D centroid panning at all zoom levels (`> 100%`), with smooth spring snap-back when pinched below `100%` and smooth animated double-tap zoom (`100%` ↔ `225%` at tap point).
  - **Finger-only controls:** Removed the bottom-right zoom button bar. Zooming and panning are 100% finger-driven, with 4 corner resize handles and a top rotation handle on the selected element for smooth 1-finger resizing and rotation.
- **Dedicated New Page for Preview / Edit Layout (`PreviewEditScreen`):**
  - Open the full-screen **Preview / Edit Layout** page from any style's Detail screen (tap the preview card or the **Preview Layout** / **Edit Layout** buttons), from **My Screens** (**Preview** / **Edit** buttons), or from the Screen Designer (**New Page** / fullscreen icon).
  - Smooth animated **Preview** ↔ **Edit Layout** mode switcher pill in the top floating glass bar.
  - **Zero layout jump on selection:** Floating glass inspector and component dock overlay the bottom edge cleanly so selecting or deselecting elements never resizes the phone canvas mid-gesture.
  - In **Preview** mode, Switches, Chips, Buttons, and Bottom Navigation items respond to live finger taps while supporting smooth 2-finger pinch-zoom and 1-finger pan.

## What was added in v2
- **Phone frame:** the designer screen sits inside a phone body with a status bar and a punch-hole or notch cut-out. Switch between Punch-hole, Notch and No frame from the chip row (the choice is remembered).
- **Icon element:** Add > Icon opens a searchable picker with 50 icons. Change the icon later, and pick its color (accent, text, muted, white, black). Icons appear in the generated prompt.
- Older saved screens open unchanged.

## Screen Designer (v1–v3 features)
Home > Screen Designer (or any style's detail page > **Preview Layout** / **Edit Layout**).
- Pick any of the 127 library styles (18 have a hand-tuned look; the rest are tinted with their own palette).
- Add elements (top bar, headline, paragraph, button, card, image, input, chip, switch, FAB, bottom nav, icon).
- Two fingers pinch to zoom and pan the whole phone smoothly; one finger moves an element, drags any of the 4 corner handles to resize, or drags the top handle to rotate (snapping to 0/45/90 degree angles and screen center with pink guide lines). Sliders give fine control, and Undo covers up to 60 steps.
- Save screens (Room), reopen and edit them, clone, delete, and copy a prompt from the list.
- The prompt is generated locally from the chosen style plus your layout (positions and sizes in percent), works offline, and can be copied or shared. Optionally refine it with your connected AI model; that reply is labelled as a live response.
- The database moves from version 1 to 2 with a real migration, so favorites and history from earlier builds are kept.

## Build status - read this first
This project was generated in an environment with **no Android SDK, Gradle or network**, so it has **not been compiled or run**.
Static checks were done (balanced brackets, valid XML/JSON, catalog validation). The `.github/workflows/build.yml` workflow builds it on GitHub Actions.

## Requirements
- Android Studio Koala+ (or Code On The Go / AndroidIDE), JDK 17, Android SDK 34
- Gradle 8.7+ (Android Studio downloads it from `gradle/wrapper/gradle-wrapper.properties`; no `gradlew`/jar is included - open the project in Android Studio, or run `gradle wrapper` once)
- minSdk 26, target/compile SDK 34

## Build
1. Open the `UIUXNameExplorer` folder in Android Studio and let Gradle sync.
2. Run the `app` configuration, or `gradle :app:assembleDebug`.
3. Unit tests: `gradle :app:testDebugUnitTest`.
4. Release builds are unsigned and unminified by default; add your own signing config.

## AI model setup (optional)
Settings > AI model settings. Enter a full **https** endpoint, model name and (if needed) API key.
- *OpenAI-compatible*: endpoints ending in `/chat/completions` (OpenAI, OpenRouter, Groq, Gemini's compatibility endpoint, or your own proxy).
- *Anthropic Messages*: `https://api.anthropic.com/v1/messages`.
The key is encrypted with an Android Keystore AES-GCM key and never hardcoded or logged. Direct calls from a phone can still expose a key; for production, point the endpoint at your own backend proxy.
Live AI answers are always labelled separately from the curated library, and nothing is simulated: with no network or no config you get a clear error.

## Architecture
`domain/` models + SearchEngine · `data/` catalog (assets JSON), Room, DataStore, Keystore, Retrofit/OkHttp, repositories · `ui/` Compose screens, ViewModels (Hilt) · `di/` Hilt module.

## Catalog
`app/src/main/assets/design_catalog.json` is generated by `python3 tools/generate_catalog.py` (validates unique names and related links).
