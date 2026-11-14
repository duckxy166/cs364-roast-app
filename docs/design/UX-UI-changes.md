# UX/UI revision · 14 November 2026

Scope: design prototype only, as clarified by the user. No Android Java/XML, Room, permission handling or Foreground Service implementation was changed.

## Critique issues addressed in the design

| Issue | Design change |
|---|---|
| Primary usage information below a large hero | Home now leads with monitoring status and tracked-app remaining time. The cobalt focus entry is compact and follows the list. |
| Edit/remove icons do not look actionable | Both actions have visible text, backgrounds/borders and minimum 48px prototype touch targets. |
| Home missing from navigation | Four labeled destinations with exactly one current destination. Brand mark is no longer an undocumented navigation control. |
| No monitoring recovery states | Active, missing-permission and stopped examples with specific recovery actions. The design toolbar explicitly labels these as sample states. |
| Tablet is a stretched phone | Expanded preview uses a navigation rail and bounded content; narrow containers collapse back to bottom navigation. |

## Other refinements

### Follow-up after the latest critique

- Focus setup now contains duration, presets and Start instead of a large idle countdown. Active sessions keep the large countdown, and pause-on-leave guidance is visible before Start.
- Exact-limit and exceeded states are separate; exceeded cards show the number of minutes over and a warning-colored progress bar.
- Home's View all action includes the full tracked-app count even when only two summaries are shown.
- Settings is reorganized: language row and choice sheet, paired light/dark previews with selected checks, and a separate reminder group with collapsible thresholds. Enabling demo expands the seconds-based schedule.

- Removed decorative English eyebrows/slogans from the visible product flow.
- Removed the duplicated Settings shortcut in the top bar.
- Replaced the tilted timer frame with a stable circular countdown.
- Back actions have visible text; confirmations keep their safe cancel/continue option.
- Dialog cancel returns keyboard focus to the trigger.
- Valid custom focus minutes survive a theme switch.
- Coherent outline icons replace Unicode stand-ins in app cards and the picker.
- Feedback from a previous state is cleared when rendering a different state.
- Existing permissions, app selection, validation, empty state, delete confirmation, focus interruption and bilingual/theme preview flows remain simulated and interactive.

## Verification

- Headless Chrome rendering and prototype interaction checks at 320, 360 and 736px; both Thai and English, Day and Night.
- Expanded preview inspection includes Home, App limits, Focus and Settings in both themes.
- Additional checks cover visible action labels, one current navigation destination, missing-permission gating, restart recovery, dialog focus return and theme/input preservation.
- No script runtime errors or horizontal element overflow in the checked cases.
- Measured text pairs: coral action 6.11:1; focus foreground 5.98:1; focus supporting text 5.18:1; day secondary text 5.75:1; night secondary text 9.31:1; selected-state text 7.01:1 / 7.57:1; warning text 5.99:1 / 7.21:1. These checks cover the listed token pairs, not every browser/platform state.
- 49 refreshed PNG references in `screens/`. They are browser design references, not Android-device evidence.

## Audit findings outside this design pass

The audit's placeholder Activities, main-thread Room work, actual Android theme resources, native scrolling, expanded Android layouts and release test button remain implementation work. No claim is made that these source-code issues were fixed. `DESIGN.md` documents the handoff intent for the eventual native implementation.

Actual TalkBack, Android font scale, system bars, IME, Service behavior, notification/audio behavior and device frame timing require Android implementation and device verification later.
