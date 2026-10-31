---
name: check-app-on-device
description: Install, launch and walk through the Qode debug app on a connected Android device to see what works, using screenshots, the UI tree and logs. Use when asked to test the app on a phone, check a flow, or confirm a change works on a real device.
---

# Check the app on a device

1. Confirm a device with `adb devices`.
2. Install with `sh ./gradlew :androidApp:installDebug`. If it fails on signatures, see `docs/ai/release.md` and ask before uninstalling.
3. Launch `MainActivity` directly. `monkey` can open LeakCanary because the debug app has two launcher entries:
   ```
   adb shell am force-stop com.qodein.qode.debug
   adb logcat -c
   adb shell am start -n com.qodein.qode.debug/com.qodein.qode.MainActivity
   ```
4. Capture a screen with `${CLAUDE_SKILL_DIR}/scripts/snap.sh <output-dir> <name>`. It saves a PNG and prints each label and tappable area with its bounds. Read the PNG when the layout matters.
5. Tap the center of an element's bounds with `adb shell input tap <x> <y>`. Don't reuse coordinates from an earlier run, since the UI can shift.
6. After each flow, read the logs:
   ```
   adb logcat -d --pid=$(adb shell pidof com.qodein.qode.debug) '*:W'
   adb logcat -d -b crash
   ```

## Ground rules

- Debug builds use `qode-dev`, whose data is shared. Don't submit, vote, report, block, or delete anything unless the user asks.
- Don't sign in or change settings on the user's behalf.
- Dismiss the Android 16 KB compatibility dialog if it appears, and report it.
- Check empty states against the data before calling them bugs.
- Report tested, failing and untested flows separately. Save durable findings as described in `CLAUDE.md`.
