# Active Context: Electrician App

## Current Work Focus
The immediate focus is on reviewing the updated Memory Bank to determine the next task, following the successful resolution of the Conduit Fill calculator data loading issue.

## Recent Changes
- Restructured NEC data from a single `nec_data.json` into multiple, calculator-specific JSON files (`wire_ampacity_data.json`, `voltage_drop_data.json`, `box_fill_data.json`, `conduit_fill_data.json`, `raceway_sizing_data.json`, `motor_calculator_data.json`).
- Successfully diagnosed and fixed the runtime issue in the Conduit Fill calculator where wire types and the initial wire entry were not loading.
- Successfully diagnosed and fixed the runtime issue in the Box Fill calculator where device fill calculation was blocked if conductors were not entered before devices.
- Updated `NecDataRepository.kt` and `DatabaseCallback.kt` to handle the new file structure.
- Incremented the database version in `AppDatabase.kt` to 49 to trigger data migration.
- Reviewed relevant ViewModels (`WireAmpacityViewModel.kt`, `VoltageDropViewModel.kt`, etc.) for compatibility.
- Updated `README.md` to reflect the new NEC data file structure.
- Initialized and read all core Memory Bank files.
- Removed deprecated `android.databinding.enableV2`, `android.enableJetifier`, `android.nonTransitiveRClass`, `kotlin.compiler.execution.strategy=in-process`, and `kapt.use.worker.api=false` properties from `gradle.properties`.
- Added `androidx.compose.material:material` dependency to `app/build.gradle` to resolve `ExperimentalMaterialApi` warning.
- Fixed "Suspicious indentation" Lint error in `VoltageDropViewModel.kt` at line 206.
- Resolved compilation errors in `ConduitFillViewModel.kt` by removing incorrect `takeUntil` imports and ensuring suspend functions are called within `viewModelScope.launch`.
- Successfully built and ran the application, confirming the previous build failure is resolved, including the `Unresolved reference 'toDp'` error after manual intervention (Invalidate Caches/Restart, Gradle Sync) in Android Studio.
- Added logging to `ConduitFillScreen.kt` and `ConduitFillViewModel.kt` to diagnose runtime data loading issues.

## Next Steps
1.  **Test Calculators:** Proceed with testing the calculators that rely on the new NEC data structure (Wire Ampacity, Voltage Drop, Box Fill, Raceway Sizing, Motor Calculator) to verify their successful integration.
2.  **Address Luminaire Calculator:** Proceed with the planned "total workover" of the Luminaire Layout calculator after the data integration is verified across all relevant calculators.
3.  **Address Wire Ampacity Issues:** Re-evaluate the Wire Ampacity calculator issues after confirming the new data structure works and the Conduit Fill and Box Fill issues are resolved.

## Active Decisions and Considerations
- The previous build failure, including the `Unresolved reference 'toDp'` error, is resolved following manual steps in Android Studio.
- The runtime data loading issue in the Conduit Fill calculator has been successfully diagnosed and fixed.
- The Conduit Fill calculator now correctly loads wire types and initializes the first wire entry.
- The runtime data loading/calculation issue in the Box Fill calculator has been successfully diagnosed and fixed.
- Our operational environment is strictly VSCode; manual steps in Android Studio are user interventions.
- The immediate next step is to test the calculators to verify data integration.
- The Luminaire calculator workover and other calculator testing can now proceed.

## Gradle Deprecation Warnings (for Gradle 9.0+)
The following deprecation warnings were identified during previous successful builds with `--warning-mode all`. These appear to originate from the Android Gradle Plugin and its dependencies and will need to be addressed by updating those dependencies in the future:
- `Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getCrunchPngs' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType$AgpDecorated.isCrunchPngs' (and the setter) to 'boolean'.`
- `Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getUseProguard' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.gradle.internal.dsl.BuildType.isUseProguard' (and the setter) to 'boolean'.`
- `Declaring an 'is-' property with a Boolean type has been deprecated. Starting with Gradle 9.0, this property will be ignored by Gradle. The combination of method name and return type is not consistent with Java Bean property rules and will become unsupported in future versions of Groovy. Add a method named 'getWearAppUnbundled' with the same behavior and mark the old one with @Deprecated, or change the type of 'com.android.build.api.variant.impl.ApplicationVariantImpl.isWearAppUnbundled' (and the setter) to 'boolean'.`

## Important Patterns and Preferences
- Adherence to MVVM and Repository patterns.
- Use of Jetpack Compose for UI.
- Data-driven approach using multiple specific JSON files for NEC data and Room database.
- Managing dependencies using Compose BOM for version consistency.

## Learnings and Project Insights
- Restructuring NEC data into specific files improves organization and potentially simplifies data loading for individual calculators.
- Database migrations triggered by version increments are crucial for loading new data structures from assets.
- Thorough testing after significant data structure changes is essential.
- Maintaining an accurate Memory Bank is vital, especially when context shifts between sessions or tasks.
- Using Compose BOM simplifies dependency management for Compose libraries.
- Upgrading AGP to 8.9.2 did not resolve the "is-" property deprecation warnings, indicating they are likely tied to the AGP version itself or its dependencies and will need to be addressed in a future update.
- Successfully resolved compilation errors and built the project, allowing for testing of the data integration.
