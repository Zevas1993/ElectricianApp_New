# Electrician App New (Android Native - Kotlin/Compose)

This project is a complete rebuild of the Electrician App, developed using native Android with Kotlin and Jetpack Compose.

## Current Status (as of 2025-04-13 ~12:40 PM ET)

The foundational structure of the application has been established, including:

1.  **Project Setup:**
    *   Gradle configured with specified versions (AGP 8.2.2, Gradle 8.13.0, Kotlin 2.0.0).
    *   Basic Android Manifest, resources (strings, colors, themes), and `.gitignore`.
    *   `gradle.properties` created with `android.useAndroidX=true` and increased JVM heap size (`org.gradle.jvmargs=-Xmx4g`).
    *   Placeholder launcher icons created (`drawable/ic_launcher.xml`, `drawable/ic_launcher_round.xml`) and referenced in `AndroidManifest.xml`.
    *   XML theme updated to inherit from `Theme.Material3.DayNight` and remove deprecated attributes.
2.  **Core Architecture:**
    *   **Dependency Injection:** Hilt is set up (`ElectricianApp`, `DatabaseModule`, build file configurations).
    *   **Data Persistence:** Room is set up.
        *   Entities defined for: `Job`, `Task`, `PhotoDoc`, `Material`, `InventoryItem`, `InventoryTransaction`, `Client`, and various `NecData` tables.
        *   DAOs created for all entities.
        *   `AppDatabase` class defined, linking all entities and DAOs.
        *   `DateConverter` created for Room.
        *   Mechanism for pre-populating NEC data from `assets/nec_data.json` using a `RoomDatabase.Callback` is implemented.
    *   **Repository Pattern:** Interfaces and Implementations created for `JobTaskRepository`, `PhotoDocRepository`, `InventoryRepository`, `ClientRepository`, and `NecDataRepository`. Hilt module updated to provide these.
3.  **UI & Navigation:**
    *   Jetpack Navigation Compose is set up in `MainActivity.kt`.
    *   A dashboard structure (`DashboardScreen`) is implemented as the main entry point.
    *   Placeholder screens created for all planned features up to Phase 7.
    *   Navigation routes defined and implemented to access all these screens from the dashboard or relevant parent screens.
    *   Shared UI components (`ExposedDropdownMenuBoxInput`, `NumberInputField`) created in `ui/common`.
4.  **ViewModel Integration:**
    *   ViewModels created for all 13 calculators.
    *   All calculator screens refactored to use their respective ViewModels via `hiltViewModel()`.
    *   ViewModels updated to inject and use `NecDataRepository` where applicable (e.g., `WireAmpacityViewModel`, `VoltageDropViewModel`, `BoxFillViewModel`, `ConduitFillViewModel`, `RacewaySizingViewModel`). *Note: Some calculation logic still uses placeholders pending repository updates.*
5.  **Build Configuration:**
    *   Troubleshooting ongoing Gradle build issues related to KSP plugin resolution. Persistently unable to resolve KSP `1.0.30` or `2.1.0-1.0.9` despite adding the JetBrains KSP repository and clearing caches.
    *   Currently configured to use manually installed Gradle 8.13.
    *   Android Studio Gradle JDK setting aligned with `JAVA_HOME`.
    *   KSP version (`2.0.0-1.0.21`) defined in `settings.gradle` (`pluginManagement`).
    *   JetBrains KSP repository added to `settings.gradle` (`pluginManagement.repositories`).
    *   KSP plugin applied by ID only in `app/build.gradle`.
    *   Root `build.gradle` cleaned up (no buildscript block, no plugin versions).
6.  **Build Troubleshooting & Fixes (2025-04-13):**
    *   Regenerated missing Gradle wrapper scripts (`gradlew.bat`, `gradlew`).
    *   Updated `app/build.gradle`:
        *   Set Compose BOM to `2024.06.00`.
        *   Set Kotlin `jvmTarget` to `17`.
        *   Added Coil dependency (`io.coil-kt:coil-compose:2.6.0`).
    *   Fixed various Kotlin compilation errors:
        *   Corrected `Icons.Default.AddAPhoto` to `Icons.Filled.AddAPhoto` in `PhotoDocListScreen.kt`.
        *   Added missing `KeyboardType.Companion.NumberDecimal` imports in multiple calculator screens and `AddEditMaterialScreen.kt`.
        *   Resolved `LocalContentColor` import ambiguity in `ConduitFillScreen.kt`.
        *   Added missing imports (`LocalContentColor`, `hiltViewModel`, `Preview`, `ElectricianAppNewTheme`) and `@Composable` annotation in `ConduitFillScreen.kt`.
    *   Resolved persistent Hilt/Javapoet build error (`NoSuchMethodError: 'java.lang.String com.squareup.javapoet.ClassName.canonicalName()'`) by downgrading Hilt plugin and dependencies to `2.44.2`.
    *   Resolved Coil 3 build errors by ensuring correct dependencies (`coil`, `coil-compose`, `coil-network-okhttp`, `okhttp`) and imports (`coil3...`) were used, and temporarily removing the `.crossfade(true)` call from `ImageRequest.Builder` for diagnostic purposes.

## Current Working Configuration (2025-04-14 ~10:57 PM ET)

This configuration successfully builds (`.\gradlew assembleDebug`).

*   **Plugins (`settings.gradle`):**
    *   AGP (Application & Library): `8.9.1`
    *   Kotlin Android: `2.1.10`
    *   KSP: `2.1.10-1.0.31`
    *   Hilt Android: `2.44.2` (Downgraded from 2.56.1 to resolve build error)
    *   Kotlin Compose Plugin: `2.1.10`
*   **Libraries & Settings (`app/build.gradle`):**
    *   `compileSdk`: 36
    *   `targetSdk`: 36
    *   `minSdk`: 26
    *   `jvmTarget`: '17'
    *   `core-ktx`: 1.16.0
    *   `lifecycle-runtime-ktx`: 2.8.7
    *   `lifecycle-runtime-compose`: 2.8.7
    *   `activity-compose`: 1.10.1
    *   `material`: 1.12.0
    *   `compose-bom`: 2025.04.00
    *   `navigation-compose`: 2.8.9
    *   `room_version`: 2.7.0 (using KSP)
    *   `hilt-android`: 2.44.2 (using KSP)
    *   `hilt-compiler`: 2.44.2 (using KSP)
    *   `hilt-navigation-compose`: 1.2.0
    *   `gson`: 2.13.0
    *   `coil`: 3.1.0
    *   `coil-compose`: 3.1.0
    *   `coil-network-okhttp`: 3.1.0
    *   `okhttp`: 4.12.0

## Implemented Features (Phase 1)

*   **Calculators:**
    *   Conduit Fill: Logic implemented using NEC Chapter 9 Tables 1, 4, and 5 data fetched from the database. Dropdowns populated dynamically.
    *   Wire Ampacity: Logic implemented using NEC Table 310.16, temperature correction factors, conductor adjustment factors, and termination limits (110.14(C)) fetched/calculated from the database. Dropdowns populated dynamically.
    *   Voltage Drop: Logic implemented using NEC Chapter 9 Table 8 resistance values fetched from the database. Dropdowns populated dynamically.
    *   Box Fill: Logic implemented using NEC 314.16(B) volume allowances fetched from the database. UI updated for detailed input.
    *   Dwelling Load (Standard Method): Logic implemented using NEC Article 220 rules and demand factors. UI updated for detailed input. (Note: Range demand uses Table 220.55 Col C & Note 1; multi-range/other notes not yet implemented).
    *   Raceway Sizing: Logic implemented using NEC Chapter 9 Tables 1, 4, and 5 data fetched from the database to determine minimum required raceway size. UI updated for multiple wire entries.
    *   Motor Calculator: Logic implemented using NEC Article 430 (Tables 430.248/250 for FLC, 430.52 for protection, 430.32 for overloads, 430.22 for conductors). UI updated for detailed input.
    *   Transformer Sizing: Logic implemented using NEC Article 450 rules for kVA, FLA, and OCPD sizing. UI updated for detailed input.
    *   Ohm's Law: Logic implemented for V=IR and P=VI calculations. UI allows selecting variable to solve for.
    *   Series/Parallel Resistance: Logic implemented for calculating total resistance. UI allows adding/removing multiple resistor values.
    *   Pipe Bending: Logic implemented for Offset, 3-Point Saddle, 90-Degree Stub, and 4-Point Saddle. Uses hardcoded bending constants (gain values approximated). UI updated for 4-point inputs.
    *   Luminaire Layout (Lumen Method): Logic implemented using Zonal Cavity method. Requires user input for CU. UI updated for detailed input.
    *   Fault Current (Transformer Impedance Method): Logic implemented to calculate FLA and AFC based on kVA, voltage, and %Z. UI updated.
    *   **UI Refactoring (All Calculators):** Successfully refactored all 13 calculator screens to use Jetpack Compose `Scaffold` and `TopAppBar`. Consolidated common UI components (`CalculationResultRow`, `InputSectionHeader`, etc.) and helper functions (`formatCalculationResult`) into `ui/common/SharedComponents.kt`. Centralized `WireEntry` data class in `data/model/CalculatorModels.kt`. Resolved associated compilation errors (conflicting overloads, unresolved references, incorrect state access).
*   **Inventory Management:**
    *   Data models (`Material`, `InventoryItem`, `InventoryTransaction`, `InventoryItemWithMaterial`) created.
    *   `InventoryDao` and `InventoryRepository` created with necessary CRUD methods.
    *   ViewModels (`InventoryListViewModel`, `InventoryDetailViewModel`, `AddEditMaterialViewModel`) created and integrated with UI.
    *   ViewModels (`InventoryListViewModel`, `InventoryDetailViewModel`, `AddEditMaterialViewModel`) created and integrated with UI.
    *   UI Screens (`InventoryListScreen`, `InventoryDetailScreen`, `AddEditMaterialScreen`) updated to use ViewModels and display data.
    *   Delete functionality added to `InventoryListScreen`.
    *   Stock adjustment logic implemented in `InventoryDetailViewModel` and UI added in `InventoryDetailScreen`.
*   **Client Management:**
    *   ViewModels (`ClientListViewModel`, `AddEditClientViewModel`) created and integrated with UI.
    *   UI Screens (`ClientListScreen`, `AddEditClientScreen`) updated to use ViewModels and display/save data.
    *   Delete functionality added to `ClientListScreen`.
*   **Job & Task Management:**
    *   Data model updated to include `clientId` in `Job`.
    *   ViewModels (`JobListViewModel`, `JobDetailViewModel`, `AddEditJobViewModel`, `AddEditTaskViewModel`) created and integrated with UI.
    *   UI Screens (`JobListScreen`, `JobDetailScreen`, `AddEditJobScreen`, `AddEditTaskScreen`) updated to use ViewModels and display/save data.
    *   Delete functionality added to `JobListScreen` (for Jobs) and `JobDetailScreen` (for Tasks).
    *   Task status update logic implemented in `JobDetailViewModel` and UI added in `JobDetailScreen`.
    *   Inventory usage integration added to `AddEditTaskViewModel` and `AddEditTaskScreen`.
*   **Photo Documentation (Basic):**
    *   Data model (`PhotoDoc`), DAO (`PhotoDocDao`), and Repository (`PhotoDocRepository`) created.
    *   ViewModels (`PhotoDocListViewModel`, `AddEditPhotoDocViewModel`) created.
    *   UI Screens (`PhotoDocListScreen`, `AddEditPhotoDocScreen`) created for listing and adding photos (linked to Jobs).

## Immediate Next Steps (Revised Plan - 2025-04-13)

1.  **Refine Core Features:**
    *   **Photo Documentation:** Enhance linking (allow linking to Tasks), add a detail view, improve image handling.
    *   **Inventory Usage:** Refine insufficient stock logic and potentially allow editing used quantities on tasks.
2.  **Implement Search/Filtering:** Add search capabilities to Job, Client, and Inventory list screens.
3.  **Implement NEC Code Lookup:** Build out the `NecCodeScreen.kt` to effectively search and display the data from `assets/nec_data.json`.
4.  **Add Reference Tools:**
    *   Create a simple screen for standard Circuit Color references.
    *   Add a section or screen for common Electrical Formulas.
    *   Add a section or screen for common Electrical Symbols.
5.  **UI/UX Polish & Testing:** Address any remaining UI inconsistencies, improve error messages/loading states, and perform thorough testing.

## Future Features / Roadmap

### AR Wiring Visualization (Deferred)

**Goal:** Visualize planned electrical wiring/conduit paths overlaid on the real-world camera view, anchored to detected surfaces.

**Detailed Plan:**

1.  **Technology Stack:** Android ARCore, Kotlin, Jetpack Compose, SceneView library (recommended for Compose integration).
2.  **Setup & Configuration:** Add dependencies (ARCore, SceneView), configure Manifest (permissions, AR Optional), implement compatibility checks.
3.  **UI/UX Flow:** Dedicated AR screen (`ARWiringScreen`) launched from Job/Task details. Detect planes, allow user taps to place anchors, render path in real-time. UI for save/clear/exit.
4.  **Data Modeling:** New `WiringPath` entity (Room) linked to Job/Task, storing anchor data (e.g., JSON of poses). Add DAO/Repository methods.
5.  **AR Screen Implementation:** Use `SceneView` composable, manage ARCore `Session` lifecycle, handle plane detection and user taps (`HitResult`) to create `Anchor` objects.
6.  **ViewModel (`ARWiringViewModel`):** Manage AR state, handle taps, create/retrieve anchors, manage `WiringPath` data (saving/loading), expose data to UI.
7.  **Rendering Logic:** Use `SceneView` API. Load simple 3D model (e.g., cylinder). For each path segment, calculate position/scale/rotation between anchors and transform the model instance (`ModelNode`).
8.  **Challenges:** Compose/AR integration, 3D math, UX for point placement, performance, tracking accuracy, data persistence (Cloud Anchors?).
9.  **Phased Implementation:**
    *   Phase 1: Core Setup (Deps, Perms, Basic Screen, Plane Detect, Anchor Place).
    *   Phase 2: Line Rendering (Load model, render single segment).
    *   Phase 3: Path Data (Model/DAO/Repo, Multi-point paths, Save/Load, Render full path).
    *   Phase 4: Refinement (UX, Edit/Delete, Optimize, Cloud Anchors?).

*(Other previously considered features like Estimating, Invoicing, Time Tracking, Reporting, User Management are deferred further.)*
