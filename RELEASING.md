# Releasing OfflineNotes

This project distributes APKs through GitHub Releases.

## 1) Create a release keystore (one-time)

```bash
mkdir -p ~/.keystores
keytool -genkeypair \
  -v \
  -keystore ~/.keystores/offlinenotes-release.jks \
  -alias offlinenotes \
  -keyalg RSA \
  -keysize 4096 \
  -validity 36500
```

Important:
- Keep this keystore file safe and backed up.
- Losing it prevents future updates signed with the same identity.

## 2) Export signing variables

Add these to your shell profile (`~/.bashrc`, `~/.zshrc`, etc):

```bash
export OFFLINENOTES_KEYSTORE_PATH="$HOME/.keystores/offlinenotes-release.jks"
export OFFLINENOTES_KEYSTORE_PASSWORD="<keystore-password>"
export OFFLINENOTES_KEY_ALIAS="offlinenotes"
export OFFLINENOTES_KEY_PASSWORD="<key-password>"
```

Reload shell:

```bash
source ~/.bashrc
# or
source ~/.zshrc
```

## 3) Build release APK

```bash
./gradlew :app:assembleRelease
```

Output filename pattern:

```text
app/build/outputs/apk/release/OfflineNotes-v<versionName>+<versionCode>-release.apk
```

Example:

```text
app/build/outputs/apk/release/OfflineNotes-v0.1.0+1-release.apk
```

## 4) Create and publish GitHub Release

```bash
git tag -a v0.1.0 -m "OfflineNotes v0.1.0"
git push origin v0.1.0

gh release create v0.1.0 \
  app/build/outputs/apk/release/OfflineNotes-v0.1.0+1-release.apk \
  --title "OfflineNotes v0.1.0" \
  --notes "## Highlights
- ...
"
```

## 5) Verify install from release asset

```bash
adb install -r OfflineNotes-v0.1.0+1-release.apk
```
