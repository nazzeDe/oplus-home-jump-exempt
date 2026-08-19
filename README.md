# 应用跳转白名单（Oplus Jump Allowlist）

LSPosed module for OnePlus / OPPO / realme (OxygenOS / ColorOS).

Package: `com.nazze.oplusjumpallowlist`

## What it does

You pick **caller** apps in a settings allowlist. When an allowlisted package starts another app, ColorOS / OxygenOS skips the inter-app jump confirmation dialog (for example `允许 30 天`).

- Allowlisted caller → jump is allowed at the intercept decision point; no confirm UI.
- Everyone else → stock jump-intercept / dialog behavior.
- Empty allowlist (default) → no callers are exempted.
- Matching is by **caller package name**, not target app and not caller→callee pairs.
- Changing the allowlist applies on later jumps without a reboot. If the config cannot be read, that jump is not exempted (fail-closed).
- Module on/off is controlled in LSPosed; the app has no master switch.

This module does **not** auto-follow the default Home / launcher role. Add launchers (for example Niagara / `bitpit.launcher`) yourself if you want them exempted.

## Requirements

- Rooted device with LSPosed
- OxygenOS / ColorOS jump interception present on the build

## Build

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew --no-daemon :app:assembleDebug
```

APK output:

`app/build/outputs/apk/debug/app-debug.apk`

## Install and enable

1. Install the APK.
2. Open LSPosed → Modules → enable **应用跳转白名单**.
3. Set scope to **系统框架 (`system`)** only.
4. Reboot if LSPosed asks for a reload after install or update.
5. Open the module app → search and check callers you want exempted (third-party list by default; enable **显示系统应用** to include system packages).

Do **not** scope `com.oplus.securitypermission` for this allowlist module. The decision hook runs in `system_server`.

## Verify

1. Leave the allowlist empty, or leave a normal third-party app unchecked. Trigger a jump that ColorOS normally intercepts → the system confirm dialog should still appear.
2. Check a caller such as `bitpit.launcher`. From that app, open several previously blocked targets → no jump confirm dialog.
3. Uncheck that caller → the same jump path can show the stock dialog again (no reboot required for allowlist edits).
4. Disable the module in LSPosed → behavior returns to stock; saved allowlist data may remain but no longer applies.

## Troubleshooting

| Symptom | Check |
| --- | --- |
| No effect after install | Module enabled; scope includes `system`; reboot/reload after APK update |
| Every jump still prompts | Caller package is checked in the module settings (launcher package, not the target) |
| Unexpected exemptions | Allowlist should only contain packages you intend; empty list exempts nobody |
| Hook not loading | Confirm LSPosed logs mention this module under the `system` / framework process |

## Out of scope

- Automatic Home / `ROLE_HOME` exemption
- Caller→callee pair lists
- Restoring a “forever allow” dialog UI
- Unrelated ColorOS fixes (animations, recents, gestures)
- Magisk / platform-signed system replacements
