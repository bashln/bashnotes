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
app/build/outputs/apk/release/OfflineNotes-v0.4.1+5-release.apk
```

## 4) Create and publish GitHub Release

```bash
git tag -a v0.4.1 -m "OfflineNotes v0.4.1"
git push origin v0.4.1

gh release create v0.4.1 \
  app/build/outputs/apk/release/OfflineNotes-v0.4.1+5-release.apk \
  --title "OfflineNotes v0.4.1" \
  --notes "## Highlights
- Grouping modes: Tag, Folder, Type
- File-type filter: All, Org, Markdown
- Show relative path in note cards
- Persist grouping/filter preferences
- Move filters to drawer menu (cleaner main screen)
- Add tag backup to file for recovery after updates
- Auto-restore tags from backup if DataStore empty
"
```

## 5) Verify install from release asset

```bash
adb install -r OfflineNotes-v0.4.1+5-release.apk
```
