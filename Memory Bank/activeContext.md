# Active Context: Electrician App

## Current Work Focus
The immediate focus is on verifying the successful integration of the restructured NEC data by testing the application.

## Recent Changes
- Restructured NEC data from a single `nec_data.json` into multiple, calculator-specific JSON files (`wire_ampacity_data.json`, `voltage_drop_data.json`, `box_fill_data.json`, `conduit_fill_data.json`, `raceway_sizing_data.json`, `motor_calculator_data.json`).
- Updated `NecDataRepository.kt` and `DatabaseCallback.kt` to handle the new file structure.
- Incremented the database version in `AppDatabase.kt` to 49 to trigger data migration.
- Reviewed relevant ViewModels (`WireAmpacityViewModel.kt`, `VoltageDropViewModel.kt`, etc.) for compatibility.
- Updated `README.md` to reflect the new NEC data file structure.
- Initialized and read all core Memory Bank files.
- Removed deprecated `android.databinding.enableV2`, `android.enableJetifier`, `android.nonTransitiveRClass`, `kotlin.compiler.execution.strategy=in-process`, and `kapt.use.worker.api=false` properties from `gradle.properties`.
- Added `androidx.compose.material:material` dependency to `app/build.gradle` to resolve `ExperimentalMaterialApi` warning.
- Fixed "Suspicious indentation" Lint error in `VoltageDropViewModel.kt` at line 206.
- Successfully built the project after resolving the Lint error.

## Next Steps
1.  **Test Integration:** Build, run, and test the application to verify the NEC data restructuring was successful. Check Logcat for errors and test calculator functionality (Wire Ampacity, Voltage Drop, Box Fill, Conduit Fill, Raceway Sizing, Motor Calculator).
2.  **Update Memory Bank (Post-Testing):** Based on testing results, update Memory Bank files (especially `progress.md`) to reflect the verified state.
3.  **Address Luminaire Calculator:** Proceed with the planned "total workover" of the Luminaire Layout calculator.
4.  **Address Wire Ampacity Issues:** Re-evaluate the Wire Ampacity calculator issues after confirming the new data structure works.

## Active Decisions and Considerations
- Testing the data restructuring is the highest priority before proceeding with new features or fixes.
- The build is now successful, with some deprecation warnings originating from the Android Gradle Plugin and its dependencies. These warnings need to be addressed for compatibility with future Gradle versions (9.0+).
- The Luminaire calculator workover remains a key objective after the current integration is verified.
- The approach to fixing the Wire Ampacity calculator may change based on the testing results of the new data structure.

## Gradle Deprecation Warnings (for Gradle 9.0+)
The following deprecation warnings were identified during the build with `--warning-mode all`. These appear to originate from the Android Gradle Plugin and its dependencies and will need to be addressed by updating those dependencies in the future:
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
- Resolving deprecated Gradle properties and managing dependencies are important for project health and compatibility.
- Using Compose BOM simplifies dependency management for Compose libraries.
