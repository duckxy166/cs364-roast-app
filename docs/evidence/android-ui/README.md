# Native Android UI references

Captured from the real Java/XML app on Android Emulator `Medium_Phone_API_36.0`, API 36. These are not browser renders. Explicit debug instrumentation binds the same sample app values used in the approved prototype: Instagram 18/30, YouTube 12/45, TikTok 0/30 minutes. Fixture data is not loaded by the normal app and is absent from release.

| Prefix | Display pixels / density | Approximate dp | Theme / language | Font scale |
|---|---|---|---|---|
| phone390-day | 1080×2280 / 443 | 390×823 | Day / Thai | 1.0 |
| phone390-night | 1080×2280 / 443 | 390×823 | Night / Thai | 1.0 |
| tablet800-day | 1600×2560 / 320 | 800×1280 | Day / Thai | 1.0 |
| tablet800-night | 1600×2560 / 320 | 800×1280 | Night / English | 1.0 |
| compact360-large-text | 900×1600 / 400 | 360×640 | Night / English | 1.3 |

Each configuration includes Home, permission-warning and empty variants, managed apps, picker, daily-limit sheet, removal confirmation, onboarding, Focus idle/custom/active/exit, Settings, language sheet, schedule/demo and reminder-preview states. `capture-results.json` records completed runs. Final captures replace earlier exploratory images; only this folder is part of the delivered native evidence.

The harness asserts that manual duration 60 leaves all four preset buttons unchecked. Other captures support visual review; they are not pixel-diff assertions, TalkBack tests or proof of live tracking/permission integration. On the compact display, scrollable pages intentionally show only the visible viewport. Android system bars and platform dialogs follow the OS rather than the browser's simulated chrome.

Display size, density and font scale are restored after capture. The fixture harness is a debug-only tool; see `../../design/ANDROID-UI-HANDOFF.md` to reproduce it and connect real data.

## Publication integration check

The five capture configurations were rerun after rebasing onto main `58b919e` (#9 included). `launcher-selected-bang.png` verifies the selected exclamation-mark icon on the real launcher. `main-integration-roast-persona.png` verifies navigation from the preserved Home entry to the existing persona feature. There are 90 matrix captures plus these 2 supplemental captures. The persona feature code and database implementation belong to #9 and are unchanged by this UI delivery.
