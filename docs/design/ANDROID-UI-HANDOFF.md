# Android UI handoff · วางเดี๋ยวนี้!

This delivery implements the approved Roast Club screen structure in Java/XML. The selected launcher and in-app identity is option 2: coral `!` on cobalt. Day and Night share semantic resources and bundled Noto Sans Thai. `roast-club.html` remains the interactive design source; `../evidence/android-ui/` contains actual Android emulator captures.

## Scope and ownership

This package owns presentation: layouts, styling, navigation, selection state, sheets and confirmation UI. Per the user's explicit instruction, the tracking owner connects installed-app discovery, permissions, usage data, persistence, limits, monitoring and alerts. No sample usage data is loaded when the app starts normally. The existing `TrackedAppEntity`, `UsageSource`, Room configuration and services are unchanged.

The screenshot fixtures live only in `app/src/debug/`; the release DEX was checked to exclude `UiReferenceCapture`. They are activated explicitly through instrumentation, not through an in-app demo menu. The existing Settings demo switch still controls the project's real `AlertThresholds` behavior.

## Binding the tracking screens

All view updates must run on Android's main thread. Load and save data using the tracking feature's existing background mechanism, then submit a new list to the UI.

```java
List<AppLimitsUi.App> tracked = /* feature-owned state */;
List<AppLimitsUi.App> available = /* installed apps from feature */;
bindUi(tracked, available, new AppLimitsUi.Actions() {
    @Override public void saveLimit(AppLimitsUi.App app, long minutes) {
        // Persist in the tracking feature, then call bindUi with refreshed state.
    }
    @Override public void remove(AppLimitsUi.App app) {
        // The UI already confirmed this action. Persist removal, then refresh.
    }
});
```

`AppLimitsUi.App(packageName, name, icon, usedMinutes, limitMinutes)` is a presentation model, not a new database schema. Package identity must be stable. Supply a new list after a change: the UI does not retain or persist the tracked selection. `setAvailableApps` copies its input. Icons may be real installed-app Drawables; null uses the semantic placeholder.

The app picker opens from `pid_apps_add`, filters the supplied available list, and opens the daily-limit sheet. The limit sheet validates a minimum of one minute. Remove calls the shared `ConfirmDialog` before invoking the callback. Without a feature binding, the screen shows its empty state and no available apps; it does not pretend a save succeeded.

## Binding Home

```java
home.renderAppUsage(tracked); // Displays at most two summaries and the full count.
home.renderPermissionWarning(missingRequiredPermission);
```

A complete permission state hides the warning. The tracking/permission owner must call these methods after loading state and again after returning from Android settings. UI defaults to an empty list and a hidden warning; this default is not a permission check. Existing test-roast wiring remains present with its view hidden from the product layout.

## Binding onboarding

```java
bindPermissions(usageAllowed, notificationsAllowed,
    requestUsageAccess, requestNotifications, continueToHome);
```

Pass actual permission results and actual request actions. Continue is enabled only when both supplied flags are true. Rebind after returning from settings. This UI code adds no permission request or service logic.

## Focus and settings

The existing Focus timer, completion persistence, pause/resume and shared confirmation are retained. Presentation changes hide the countdown before starting, show the circular active timer, synchronize all preset checked states with manual input, and confirm before navigating away from an active session.

Settings retains `SettingsManager`, the original radio IDs and the Material switch. Language now opens a bottom sheet, appearance uses previews, and the reminder schedule collapses normally and expands in demo mode. The reminder preview is visual only: it sends no notification and plays no sound or vibration.

## Navigation and sizes

`PutItDownNavigation` binds a Material navigation bar on compact screens and a rail from `layout-sw600dp` on expanded screens. Destination IDs are preserved in the shared menu. Existing layout IDs remain in their feature layouts; Home's old button destination IDs are menu IDs now, so code must use the navigation listener rather than casting those IDs to Button. Feature owners should call the shared binder after setting their content view.

## Reproducing the screenshot references

Build and install debug, then run explicitly:

```text
adb shell am instrument -w -e prefix phone-day -e theme day -e language th com.cs364.roastapp/com.cs364.roastapp.ui.UiReferenceCapture
```

Captures are written to `/sdcard/Android/data/com.cs364.roastapp/files/ui-reference/`. The harness supplies fixture app models to the real native UI; it never writes fixture apps or focus completions to Room. It checks that custom focus minutes clear every preset selection. The fixture run temporarily changes app language/theme/demo settings and restores language/theme; run it only in a dedicated emulator, with demo initially off. It leaves demo off.

The delivered screen references show UI states, not proof that tracking and Android permission requests have been connected. Owner acknowledgement, live feature integration, physical-device performance and TalkBack remain outside this UI handoff.
