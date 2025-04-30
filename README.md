# ElectricianApp_New

This is a comprehensive mobile application for electricians, built using Kotlin and Jetpack Compose. It provides various tools and resources to assist with daily tasks, including calculators, inventory management, job tracking, photo documentation, and NEC code lookups.

## Features

*   **Calculators:** A suite of electrical calculators for common tasks (e.g., Wire Ampacity, Voltage Drop, Box Fill, Conduit Fill, Motor Calculations, Ohms Law, Pipe Bending, Fault Current, Transformer Sizing, Dwelling Load, Series/Parallel Resistance, Luminaire Layout).
*   **Inventory Management:** Track materials and inventory items.
*   **Job Tracking:** Manage jobs, tasks, and associated details.
*   **Photo Documentation:** Capture and organize photos related to jobs or inventory.
*   **NEC Code Reference:** Look up NEC code sections and tables.

## Development Status

*   **Luminaire Layout Calculator:** The Luminaire Layout calculator has undergone a significant workover and is now complete. It implements the Lumen Method, calculates RCR and CU, and provides a recommended grid layout with offsets and spacing, including a visual diagram.
*   **NEC Data Expansion:** The application is in the process of expanding its integrated NEC data. Initially planned for a single JSON file, the approach has been revised to use separate JSON files for the specific NEC data required by each calculator. This data is loaded into a Room database for efficient access.

## NEC Data Structure

NEC data is stored in the `app/src/main/assets/` directory in separate JSON files, categorized by the calculator or function that primarily uses the data. This approach improves organization and allows for more targeted data loading.

Current NEC data files include:

*   `wire_ampacity_data.json`: Contains data for Wire Ampacity calculations (primarily NEC Table 310.16, 310.15(B), 310.15(C)).
*   `voltage_drop_data.json`: Contains data for Voltage Drop calculations (primarily NEC Table 8, Table 9).
*   `box_fill_data.json`: Contains data for Box Fill calculations (primarily NEC Table 314.16(B)).
*   `conduit_fill_data.json`: Contains data for Conduit Fill calculations (primarily NEC Table 4, Table 5).
*   `raceway_sizing_data.json`: Contains data for Raceway Sizing calculations (primarily NEC Table 4, Table 5).
*   `motor_calculator_data.json`: Contains data for Motor Calculations (primarily NEC Tables 430.248, 430.250, Table 430.52).

This data is loaded into the Room database on application startup (or database migration) via `DatabaseCallback.kt` and accessed through `NecDataDao.kt` and `NecDataRepository.kt`.

## Technologies Used

*   Kotlin
*   Jetpack Compose
*   Android Architecture Components (ViewModel, Room Database)
*   Dependency Injection (Hilt)
*   Gson (for JSON parsing)

## Setup and Installation

1.  Clone the repository:
    ```bash
    git clone https://github.com/ChrisBoyd/ElectricianApp_New.git
    ```
2.  Open the project in Android Studio.
3.  Ensure you have the necessary Android SDKs and build tools installed.
4.  Sync the project with Gradle files.
5.  Populate the JSON files in `app/src/main/assets/` with the required NEC data.
6.  Build and run the application on an emulator or physical device.

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

[Specify your license here, e.g., MIT, Apache 2.0]

## Contact

[Your Contact Information]
