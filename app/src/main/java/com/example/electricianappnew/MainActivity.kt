package com.example.electricianappnew

import android.os.Bundle
import android.util.Log // Add Log import
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController // Add missing import
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation // Import for nested graphs
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.electricianappnew.navigation.Screen // Add this import
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import com.example.electricianappnew.ui.dashboard.DashboardScreen
import com.example.electricianappnew.ui.calculators.*
import com.example.electricianappnew.ui.clients.AddEditClientScreen
import com.example.electricianappnew.ui.clients.ClientListScreen
import com.example.electricianappnew.ui.inventory.AddEditMaterialScreen
import com.example.electricianappnew.ui.inventory.InventoryDetailScreen
import com.example.electricianappnew.ui.inventory.InventoryListScreen
import com.example.electricianappnew.ui.jobs.AddEditJobScreen
import com.example.electricianappnew.ui.jobs.AddEditTaskScreen
import com.example.electricianappnew.ui.jobs.JobDetailScreen
import com.example.electricianappnew.ui.jobs.JobListScreen
import com.example.electricianappnew.ui.neccode.CircuitColorReferenceScreen // Import new screen
import com.example.electricianappnew.ui.neccode.ElectricalFormulasScreen // Import new screen
import com.example.electricianappnew.ui.neccode.ElectricalSymbolsScreen // Import new screen
import com.example.electricianappnew.ui.neccode.NecCodeScreen // Import new screen
import com.example.electricianappnew.ui.photodoc.AddEditPhotoDocScreen
import com.example.electricianappnew.ui.photodoc.PhotoDocListScreen
// Import other screens as needed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Enable Hilt
class MainActivity : ComponentActivity() {
    private val TAG = "AppStartup" // Define log tag

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "MainActivity onCreate: Start")
        super.onCreate(savedInstanceState)
        setContent {
            Log.d(TAG, "MainActivity setContent: Start")
            ElectricianAppNewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation() // Restore navigation
                }
            }
        }
    }
}

// Keep AppNavigation function defined, just not called from setContent for now
@Composable
fun AppNavigation() {
    Log.d("AppStartup", "AppNavigation Composable: Start") // Use literal tag here
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }

        // Define nested graphs using extension functions
        calculatorsGraph(navController)
        clientsGraph(navController)
        inventoryGraph(navController)
        jobsGraph(navController)
        photoDocGraph(navController)
        necCodeGraph(navController)

        // --- Other Placeholders ---
        // composable("estimating") { EstimatingScreen() }
        // composable("invoicing") { InvoicingScreen() }
        // composable("reporting") { ReportingScreen() }
    }
}

// --- Nested Graph Definitions ---

fun NavGraphBuilder.calculatorsGraph(navController: NavController) {
    navigation(startDestination = "calculatorList", route = "calculators") { // Set start destination
        composable("calculatorList") { CalculatorListScreen(navController = navController) } // Add the list screen composable
        // Actual calculator screens become part of this nested graph
        composable("conduitFill") { ConduitFillScreen(navController = navController) }
        composable("wireAmpacity") { WireAmpacityScreen(navController = navController) }
        composable("voltageDrop") { VoltageDropScreen(navController = navController) }
        composable("boxFill") { BoxFillScreen(navController = navController) }
        composable("dwellingLoad") { DwellingLoadScreen(navController = navController) }
        composable("racewaySizing") { RacewaySizingScreen(navController = navController) }
        composable("motorCalculator") { MotorCalculatorScreen(navController = navController) }
        composable("transformerSizing") { TransformerSizingScreen(navController = navController) }
        composable("ohmsLaw") { OhmsLawScreen(navController = navController) }
        composable("seriesParallelResistance") { SeriesParallelResistanceScreen(navController = navController) }
        composable("pipeBending") { PipeBendingScreen(navController = navController) }
        composable("luminaireLayout") { LuminaireLayoutScreen(navController = navController) }
        composable("faultCurrent") { FaultCurrentScreen(navController = navController) }
        // Add a "calculatorList" composable if needed, or adjust startDestination
    }
}

fun NavGraphBuilder.clientsGraph(navController: NavController) {
    navigation(startDestination = "clientList", route = "clients") {
        composable("clientList") {
            ClientListScreen(
                // Removed incorrect navController parameter
                onAddClientClick = { navController.navigate("addEditClient") }, // Route remains the same globally
                onClientClick = { clientId -> navController.navigate("addEditClient?clientId=$clientId") }
            )
        }
        composable(
            route = "addEditClient?clientId={clientId}", // Route remains the same globally
            arguments = listOf(navArgument("clientId") { nullable = true; type = NavType.StringType })
        ) {
            AddEditClientScreen(
                onSaveComplete = { navController.popBackStack() },
                onCancelClick = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.inventoryGraph(navController: NavController) {
    navigation(startDestination = "inventoryList", route = "inventory") {
        composable("inventoryList") {
            InventoryListScreen(
                // Removed incorrect navController parameter
                onAddItemClick = { navController.navigate("addEditMaterial") }, // Route remains the same globally
                onItemClick = { itemId -> navController.navigate("inventoryDetail/$itemId") } // Route remains the same globally
            )
        }
        composable(
            route = "addEditMaterial?materialId={materialId}", // Route remains the same globally
            arguments = listOf(navArgument("materialId") { nullable = true; type = NavType.StringType })
        ) {
            AddEditMaterialScreen(
                onSaveComplete = { navController.popBackStack() },
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "inventoryDetail/{inventoryItemId}", // Route remains the same globally
            arguments = listOf(navArgument("inventoryItemId") { type = NavType.StringType })
        ) {
            InventoryDetailScreen(
                onEditItemClick = { /* TODO: Navigate to edit */ }
                // onAdjustStockClick is handled internally
            )
        }
    }
}

fun NavGraphBuilder.jobsGraph(navController: NavController) {
    navigation(startDestination = "jobList", route = "jobs") {
        composable("jobList") {
            JobListScreen(
                // Pass hoisted lambdas
                onAddJobClick = { navController.navigate(Screen.AddEditJob.addRoute) },
                onJobClick = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) }
            )
        }
        composable(
            route = Screen.AddEditJob.route, // Use Screen object route definition
            arguments = Screen.AddEditJob.arguments // Use Screen object arguments
        ) {
            AddEditJobScreen(
                // Removed navController based on build error
                onSaveComplete = { navController.popBackStack() }, // Keep popBackStack for completion
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.JobDetail.route, // Use Screen object route definition
            arguments = Screen.JobDetail.arguments // Use Screen object arguments
        ) {
            // No need to extract args here, ViewModel handles it
            JobDetailScreen(
                // Pass hoisted lambdas
                onNavigateBack = { navController.navigateUp() },
                onEditJobClick = { jobId -> navController.navigate(Screen.AddEditJob.createRoute(jobId)) },
                onAddTaskClick = { jobId -> navController.navigate(Screen.AddEditTask.createRoute(jobId)) },
                onEditTaskClick = { jobId, taskId -> navController.navigate(Screen.AddEditTask.createRoute(jobId, taskId)) }
            )
        }
        composable(
            route = Screen.AddEditTask.route, // Use Screen object route definition
            arguments = Screen.AddEditTask.arguments // Use Screen object arguments
        ) {
            // No need to extract args here, ViewModel handles it
            AddEditTaskScreen(
                // Removed navController = navController, as it's not needed by the hoisted screen
                onSaveComplete = { navController.popBackStack() },
                onCancelClick = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.photoDocGraph(navController: NavController) {
    navigation(startDestination = "photoDocList", route = "photoDocs") {
        composable("photoDocList") { // Potentially add jobId/taskId args later for filtering
            PhotoDocListScreen(
                // Explicitly name parameters
                modifier = Modifier, // Add default modifier explicitly
                // viewModel = hiltViewModel(), // Let Hilt handle this implicitly
                onAddPhotoClick = { /* Need context (jobId/taskId) */ /* TODO: Decide how to launch add from list */ },
                onPhotoClick = { photoId -> /* TODO: Navigate to photo detail */ }
            )
        }
        composable(
            route = "addEditPhotoDoc/{jobId}?taskId={taskId}", // Route remains the same globally
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("taskId") { nullable = true; type = NavType.StringType; defaultValue = null } // Optional taskId
            )
        ) {
            AddEditPhotoDocScreen(
                 // Explicitly name parameter
                onSaveSuccess = { navController.popBackStack() } // Keep this lambda for now
            )
        }
    }
}

fun NavGraphBuilder.necCodeGraph(navController: NavController) {
    navigation(startDestination = "necCode", route = "nec") { // Example start destination
        composable("necCode") { NecCodeScreen(navController = navController) } // Pass NavController
        composable("circuitColors") { CircuitColorReferenceScreen(navController = navController) } // Pass NavController
        composable("electricalFormulas") { ElectricalFormulasScreen(navController = navController) } // Pass NavController
        composable("electricalSymbols") { ElectricalSymbolsScreen(navController = navController) } // Pass NavController
    }
}
