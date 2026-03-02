# AGENTS.md
Repository guide for agentic coding tools working on OfflineNotes.

## 0) Quick Start
- Stack: Kotlin + Jetpack Compose + Material 3 + Navigation Compose + DataStore.
- App model: fully offline notes app; `.md` and `.org` files are source of truth.
- Storage: SAF only (`ACTION_OPEN_DOCUMENT_TREE` + persistable URI permission).
- Networking: do not add network/sync APIs inside the app.
- Bottom bar has exactly 2 tabs: `Notas` and `Sync`.
- Git safety: never run `git push` unless the user explicitly asks for push in this chat.
- Before finalizing: run lint/test/build commands listed below.

## 1) Product Constraints (non-negotiable)
- OfflineNotes is not an IDE and not a second-brain platform.
- Keep UX minimal: list -> open -> write -> save.
- Do not implement: backlinks, graph, plugins, advanced markdown preview, attachments,
  database as source-of-truth, AI features, login, WebDAV, built-in sync.
- Sync screen is informational only; mention external Nextcloud usage.

## 2) Rule Files (Cursor/Copilot)
Checked paths:
- `.cursorrules`
- `.cursor/rules/`
- `.github/copilot-instructions.md`

Current status:
- No Cursor rules found.
- No Copilot instructions found.

If these files appear later, merge their rules into this file and follow precedence:
1. User instruction
2. Repo-local policy file (`.cursor/...`, Copilot instructions)
3. `AGENTS.md`
4. Default tool behavior

## 3) Build, Lint, and Test Commands
This repo uses a single Android app module: `:app`.

Preferred command style:
- Use `./gradlew <task>` when Gradle wrapper exists.
- If wrapper is missing locally, run equivalent Gradle task via Android Studio.

### 3.1 Core commands
- Build debug APK: `./gradlew :app:assembleDebug`
- Build release APK: `./gradlew :app:assembleRelease`
- Lint: `./gradlew :app:lint`
- Unit tests (all): `./gradlew :app:testDebugUnitTest`
- Instrumentation tests: `./gradlew :app:connectedDebugAndroidTest`

### 3.2 Run a single test (important)
- Single unit test class:
  `./gradlew :app:testDebugUnitTest --tests "com.offlinenotes.ExampleUnitTest"`
- Single unit test method:
  `./gradlew :app:testDebugUnitTest --tests "com.offlinenotes.ExampleUnitTest.testName"`
- Single instrumentation class:
  `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.offlinenotes.ExampleInstrumentedTest`
- Single instrumentation method:
  `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.offlinenotes.ExampleInstrumentedTest#testName`

### 3.3 Validation order for agents
- First: run narrowest relevant test(s).
- Then: run `:app:testDebugUnitTest`.
- Then: run `:app:lint`.
- Before large changes or release prep: run full build (`assembleDebug`).

## 4) Architecture and File Ownership
Use this project structure:
- `data/`
  - `NotesRepository` (SAF file operations)
  - `SettingsRepository` (DataStore preferences)
- `domain/`
  - `NoteMeta` and small domain types
- `ui/`
  - `notes/NotesListScreen`
  - `editor/EditorScreen`
  - `sync/SyncScreen`
  - theme package for visual system
- `viewmodel/`
  - `NotesListViewModel`
  - `EditorViewModel`

Guideline:
- Keep repositories focused on IO.
- Keep ViewModels stateful and UI-friendly.
- Keep composables mostly side-effect free except UI orchestration.

## 5) Code Style Guidelines

### 5.1 Kotlin and imports
- Follow Kotlin official style and keep imports explicit.
- No wildcard imports.
- Remove unused imports in the same edit.
- Prefer immutable `val`; use `var` only when state must change.

### 5.2 Formatting
- Use IDE/ktfmt defaults consistently; avoid manual style churn.
- Keep lines readable (~100 chars target).
- Use 4-space indentation, no tabs.
- Keep composables compact; extract only when readability improves.

### 5.3 Types and state
- Add explicit types for public APIs and shared state models.
- Use `data class` for UI/domain state.
- Prefer `StateFlow`/`SharedFlow` for ViewModel outputs.
- Avoid reflection-heavy or meta-programmed abstractions.

### 5.4 Naming conventions
- Types/classes/interfaces: `PascalCase`.
- Functions/properties: `camelCase`.
- Constants: `UPPER_SNAKE_CASE` for true constants only.
- File names should match primary top-level type/composable.

### 5.5 Error handling
- Wrap SAF and file IO with `runCatching`/`Result` at repository boundaries.
- Show user-facing failures via Snackbar/messages.
- Never silently swallow exceptions.
- Keep error text actionable but short.
- Never log secrets, tokens, or personal data.

## 6) UI / Design System Rules
- Material 3 with custom dark theme based on Tokyo Night Storm.
- Required palette anchors:
  - background `#24283b`, surface `#292e42`, surfaceVariant `#1f2335`
  - primary `#9ece6a`, onPrimary `#1f2335`
  - onBackground/onSurface `#c0caf5`
  - secondaryText `#a9b1d6`, muted `#565f89`, error `#f7768e`
- Use spacing in multiples of 8dp.
- Use rounded corners consistently (16dp-20dp).
- Keep elevation subtle.
- Use green accent (`primary`) only for active affordances (FAB, completed tasks,
  active icons).
- Typography should stay minimal; do not introduce many size variants.

## 7) Behavior Rules
- First app use must prompt folder selection.
- Persist URI with `takePersistableUriPermission` + DataStore.
- List only `.md` and `.org` files.
- Sort by `lastModified` when available, fallback to name.
- Editor must support simple checklist toggling for lines beginning with
  `- [ ]`, `- [x]`, or `- [X]` by editing raw text.
- Auto-save when leaving editor screen.

## 8) Definition of Done
A change is complete when:
- Feature behavior matches product constraints.
- Relevant tests pass (or inability to run is documented).
- Lint/build are clean (or clearly documented if not executable in environment).
- No unrelated refactors are mixed into feature fixes.
- Documentation and this file are updated when workflows change.
