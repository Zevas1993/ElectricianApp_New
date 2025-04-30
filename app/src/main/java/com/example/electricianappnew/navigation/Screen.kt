package com.example.electricianappnew.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

// Define constants for argument keys
object NavArg {
    const val JOB_ID = "jobId"
    const val TASK_ID = "taskId"
    // Add other argument keys as needed (e.g., CLIENT_ID, MATERIAL_ID)
}

// Sealed interface/class for defining screens
sealed class Screen(val route: String) {

    // Dashboard
    object Dashboard : Screen("dashboard")

    // Jobs Feature
    object JobList : Screen("jobList")
    object AddEditJob : Screen("addEditJob?${NavArg.JOB_ID}={${NavArg.JOB_ID}}") {
        val arguments = listOf(
            navArgument(NavArg.JOB_ID) {
                type = NavType.StringType
                nullable = true // Allow null for adding new job
                // defaultValue = null // Default is null for nullable StringType
            }
        )
        // Function to create route for editing an existing job
        fun createRoute(jobId: String) = "addEditJob?${NavArg.JOB_ID}=$jobId"
        // Route for adding a new job (no argument needed, uses default null)
        val addRoute = "addEditJob" // No need for default value query param
    }
    object JobDetail : Screen("jobDetail/{${NavArg.JOB_ID}}") {
        val arguments = listOf(
            navArgument(NavArg.JOB_ID) { type = NavType.StringType } // ID is String
        )
        fun createRoute(jobId: String) = "jobDetail/$jobId" // ID is String
    }
    object AddEditTask : Screen("addEditTask/{${NavArg.JOB_ID}}?${NavArg.TASK_ID}={${NavArg.TASK_ID}}") {
         val arguments = listOf(
            navArgument(NavArg.JOB_ID) { type = NavType.StringType }, // ID is String
            navArgument(NavArg.TASK_ID) {
                type = NavType.StringType
                nullable = true // Allow null for adding new task
                // defaultValue = null // Default is null for nullable StringType
            }
        )
        // Function to create route for adding a new task
        fun createRoute(jobId: String) = "addEditTask/$jobId" // No need for default task ID query param
        // Function to create route for editing an existing task
        fun createRoute(jobId: String, taskId: String) = "addEditTask/$jobId?${NavArg.TASK_ID}=$taskId" // IDs are Strings
    }

    // --- Add other screens used in MainActivity here ---
    // Example: Calculators
    object CalculatorList : Screen("calculatorList")
    object ConduitFill : Screen("conduitFill")
    object WireAmpacity : Screen("wireAmpacity")
    object VoltageDrop : Screen("voltageDrop")
    // ... add all other calculator screens

    // Example: Clients
    object ClientList : Screen("clientList")
    object AddEditClient : Screen("addEditClient?clientId={clientId}") // Keep existing string args for now if needed
    // ...

     // Example: Inventory
    object InventoryList : Screen("inventoryList")
    object AddEditMaterial : Screen("addEditMaterial?materialId={materialId}") // Keep existing string args for now if needed
    object InventoryDetail : Screen("inventoryDetail/{inventoryItemId}") // Keep existing string args for now if needed
    // ...

    // Example: Photo Doc
    object PhotoDocList : Screen("photoDocList")
    object AddEditPhotoDoc : Screen("addEditPhotoDoc/{jobId}?taskId={taskId}") // Keep existing string args for now if needed
    // ...

    // Example: NEC Code
    object NecCode : Screen("necCode")
    object CircuitColorReference : Screen("circuitColors")
    object ElectricalFormulas : Screen("electricalFormulas")
    object ElectricalSymbols : Screen("electricalSymbols")
    // ...

    // Helper function to append arguments to a base route (optional, but can be useful)
    // fun withArgs(vararg args: String): String {
    //     return buildString {
    //         append(route)
    //         args.forEach { arg ->
    //             append("/$arg")
    //         }
    //     }
    // }
}
