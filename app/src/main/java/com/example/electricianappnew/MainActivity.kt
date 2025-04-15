package com.example.electricianappnew

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
// Removed import for non-existent DashboardScreen
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ElectricianAppNewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Set startDestination to "jobList" for now
    NavHost(navController = navController, startDestination = "jobList") {
        // composable("dashboard") {
        //     DashboardScreen(navController = navController) // Keep commented out
        // }

        // --- Calculators ---
        composable("conduitFill") { ConduitFillScreen(navController = navController) } // Pass NavController
        composable("wireAmpacity") { WireAmpacityScreen(navController = navController) } // Pass NavController
        composable("voltageDrop") { VoltageDropScreen(navController = navController) } // Pass NavController
        composable("boxFill") { BoxFillScreen(navController = navController) } // Pass NavController
        composable("dwellingLoad") { DwellingLoadScreen() }
        composable("racewaySizing") { RacewaySizingScreen() }
        composable("motorCalculator") { MotorCalculatorScreen() }
        composable("transformerSizing") { TransformerSizingScreen() }
        composable("ohmsLaw") { OhmsLawScreen(navController = navController) } // Pass NavController
        composable("seriesParallelResistance") { SeriesParallelResistanceScreen() }
        composable("pipeBending") { PipeBendingScreen() }
        composable("luminaireLayout") { LuminaireLayoutScreen() }
        composable("faultCurrent") { FaultCurrentScreen() }

        // --- Clients ---
        composable("clientList") {
            ClientListScreen(
                onAddClientClick = { navController.navigate("addEditClient") },
                onClientClick = { clientId -> navController.navigate("addEditClient?clientId=$clientId") }
            )
        }
        composable(
            route = "addEditClient?clientId={clientId}",
            arguments = listOf(navArgument("clientId") { nullable = true; type = NavType.StringType })
        ) {
            AddEditClientScreen(
                onSaveComplete = { navController.popBackStack() },
                onCancelClick = { navController.popBackStack() }
            )
        }

        // --- Inventory ---
        composable("inventoryList") {
            InventoryListScreen(
                onAddItemClick = { navController.navigate("addEditMaterial") },
                onItemClick = { itemId -> navController.navigate("inventoryDetail/$itemId") }
            )
        }
         composable(
            route = "addEditMaterial?materialId={materialId}",
            arguments = listOf(navArgument("materialId") { nullable = true; type = NavType.StringType })
        ) {
             AddEditMaterialScreen(
                 onSaveComplete = { navController.popBackStack() },
                 onCancelClick = { navController.popBackStack() }
             )
         }
        composable(
            route = "inventoryDetail/{inventoryItemId}",
            arguments = listOf(navArgument("inventoryItemId") { type = NavType.StringType })
        ) {
            InventoryDetailScreen(
                 onEditItemClick = { /* TODO: Navigate to edit */ }
                 // onAdjustStockClick is handled internally
            )
        }


        // --- Jobs & Tasks ---
        composable("jobList") {
            JobListScreen(
                onAddJobClick = { navController.navigate("addEditJob") },
                onJobClick = { jobId -> navController.navigate("jobDetail/$jobId") }
            )
        }
         composable(
            route = "addEditJob?jobId={jobId}",
            arguments = listOf(navArgument("jobId") { nullable = true; type = NavType.StringType })
        ) {
             AddEditJobScreen(
                 onSaveComplete = { navController.popBackStack() },
                 onCancelClick = { navController.popBackStack() }
             )
         }
        composable(
            route = "jobDetail/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
             val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
             JobDetailScreen(
                 onEditJobClick = { navController.navigate("addEditJob?jobId=$jobId") },
                 onAddTaskClick = { navController.navigate("addEditTask/$jobId") },
                 onTaskClick = { jId, tId -> navController.navigate("addEditTask/$jId?taskId=$tId") },
                 onViewClientClick = { clientId -> navController.navigate("addEditClient?clientId=$clientId") },
                 onAddPhotoForJobClick = { jId -> navController.navigate("addEditPhotoDoc/$jId") }, // Pass only JobId
                 onAddPhotoForTaskClick = { jId, tId -> navController.navigate("addEditPhotoDoc/$jId?taskId=$tId") } // Pass JobId and TaskId
             )
         }
         composable(
            route = "addEditTask/{jobId}?taskId={taskId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("taskId") { nullable = true; type = NavType.StringType }
            )
        ) {
             AddEditTaskScreen(
                 onSaveComplete = { navController.popBackStack() },
                 onCancelClick = { navController.popBackStack() }
             )
         }

        // --- Photo Documentation ---
         composable("photoDocList") { // Potentially add jobId/taskId args later for filtering
             PhotoDocListScreen(
                 onAddPhotoClick = { /* Need context (jobId/taskId) */ /* TODO: Decide how to launch add from list */ },
                 onPhotoClick = { photoId -> /* TODO: Navigate to photo detail */ }
             )
         }
         composable(
             route = "addEditPhotoDoc/{jobId}?taskId={taskId}", // Route accepts jobId and optional taskId
             arguments = listOf(
                 navArgument("jobId") { type = NavType.StringType },
                 navArgument("taskId") { nullable = true; type = NavType.StringType; defaultValue = null } // Optional taskId
             )
         ) {
             AddEditPhotoDocScreen(
                 onSaveSuccess = { navController.popBackStack() }
             )
         }

         // --- NEC Code & References ---
         composable("necCode") { NecCodeScreen() }
         composable("circuitColors") { CircuitColorReferenceScreen() }
         composable("electricalFormulas") { ElectricalFormulasScreen() }
         composable("electricalSymbols") { ElectricalSymbolsScreen() }


        // --- Other Placeholders ---
        // composable("estimating") { EstimatingScreen() }
        // composable("invoicing") { InvoicingScreen() }
        // composable("reporting") { ReportingScreen() }
    }
}
