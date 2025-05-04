# System Patterns: Electrician App

## Architecture
The application follows a Model-View-ViewModel (MVVM) architecture pattern, particularly within the UI modules using Jetpack Compose. Data is managed through a Repository pattern, abstracting the data sources (Room Database, assets).

## Key Technical Decisions
- **Language:** Kotlin is used for Android development.
- **UI Toolkit:** Jetpack Compose is used for building the user interface.
- **Database:** Room Persistence Library is used for local data storage.
- **Dependency Injection:** Hilt is used for dependency injection.
- **NEC Data:** NEC data is stored in multiple, calculator-specific JSON files (e.g., `wire_ampacity_data.json`, `voltage_drop_data.json`, etc.) within the assets and loaded into the Room database on application startup or database version migration.

## Design Patterns
- **MVVM:** Separates UI logic from business logic and data handling.
- **Repository Pattern:** Provides a clean API for data access, abstracting the underlying data source.
- **Dependency Injection:** Manages dependencies between components, improving testability and maintainability.

## Component Relationships
- **UI (Composables):** Observe StateFlow/LiveData from ViewModels.
- **ViewModels:** Hold UI state, expose data streams, and interact with Repositories.
- **Repositories:** Provide data to ViewModels, abstracting data sources (DAO, assets).
- **DAO (Data Access Objects):** Define methods for interacting with the Room database.
- **Database:** Stores structured application data, including NEC data.
- **Assets (Specific JSON files):** Initial source for NEC data population (e.g., `wire_ampacity_data.json`, `voltage_drop_data.json`).

## Critical Implementation Paths
- **NEC Data Loading:** The process of reading the specific JSON files, parsing them, and populating the Room database via `DatabaseCallback` is critical for calculator functionality. The data loading and basic calculation issues in the Conduit Fill and Box Fill calculators have been addressed, improving the reliability of this path.
- **Calculator Logic:** The calculation logic within classes like `LuminaireCalculator.kt` and `WireAmpacityViewModel.kt` relies heavily on accurate data retrieval from the Repository.
- **UI State Management:** The flow of data from the Repository through the ViewModel to the UI and user interaction handling are key to the application's responsiveness and correctness.
