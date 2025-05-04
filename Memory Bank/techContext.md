# Technical Context: Electrician App

## Technologies Used
- **Kotlin:** Primary programming language for Android development.
- **Jetpack Compose:** Modern Android UI toolkit.
- **Android SDK:** Core Android development framework.
- **Room Persistence Library:** SQLite object mapping library for local database storage.
- **Hilt:** Dependency injection library for Android.
- **Gradle:** Build automation tool.
- **JSON:** Data format used for storing NEC data in assets.

## Development Setup
- **IDE:** Android Studio (recommended) or VS Code with Kotlin/Android extensions.
- **Minimum SDK:** API Level 21 (Android 5.0 Lollipop).
- **Target SDK:** API Level 36.
- **Build System:** Gradle.

## Technical Constraints
- **Mobile Environment:** Development is constrained by mobile device resources (CPU, memory, battery) and screen size variations.
- **Offline Functionality:** Core features, especially calculators and NEC references, should function offline.
- **Data Storage:** Local data storage using Room is the primary method for persistence.
- **NEC Data Format:** NEC data is currently stored and loaded from JSON files, requiring careful parsing and mapping to database entities. The correct parsing and database population of these JSON files is critical for the functionality of the calculators, and the data loading and basic calculation issues in the Conduit Fill and Box Fill calculators have been addressed.

## Dependencies
Key dependencies are managed in the `app/build.gradle` file and include:
- Compose UI, Material Design (managed with Compose BOM)
- Room Runtime, KTX, Compiler
- Hilt Android, Compiler, Navigation Compose
- Kotlin Coroutines
- Lifecycle ViewModel, Runtime KTX
- Navigation Compose
- Gson (for JSON parsing)
- `androidx.compose.material:material` (added to resolve `ExperimentalMaterialApi` warning)

## Tool Usage Patterns
- **read_file:** Used to inspect existing code, configuration files, and Memory Bank documents.
- **write_to_file:** Used to create new files or completely replace the content of existing files, particularly for initial Memory Bank setup, large code changes, or adding new dependencies/BOMs to build files.
- **replace_in_file:** Used for making targeted modifications to existing code and configuration files, including removing deprecated properties from Gradle files.
- **list_files:** Used to explore the project structure and identify relevant files.
- **search_files:** Used to find specific code patterns or content across the codebase.
- **execute_command:** Used for running build commands, tests, or other necessary CLI operations (e.g., Gradle tasks).
- **browser_action:** Can be used for testing web-based aspects if any are introduced or for referencing online documentation.
- **ask_followup_question:** Used to clarify requirements or gather missing information from the user.
- **attempt_completion:** Used to signal the completion of a task and present the results.
- **plan_mode_respond:** Used in PLAN MODE to discuss plans, gather information, and respond to user queries about the approach.
