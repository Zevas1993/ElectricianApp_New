package com.example.electricianappnew.ui.inventory // Ensure only one package declaration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions // Keep this import
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType // Keep this import
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.data.model.InventoryItem
import com.example.electricianappnew.data.model.InventoryTransaction
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.ui.inventory.viewmodel.InventoryDetailViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryDetailViewModel = hiltViewModel(),
    onEditItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    var adjustmentQuantity by remember { mutableStateOf("") }
    var adjustmentError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage ?: "Error loading details",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            uiState.inventoryItem != null && uiState.material != null -> {
                val item = uiState.inventoryItem!!
                val material = uiState.material!!
                val transactions = uiState.transactions

                val isLowStock = item.lowStockThreshold != null && item.quantityOnHand <= item.lowStockThreshold!!
                val quantityColor = if (isLowStock) MaterialTheme.colorScheme.error else LocalContentColor.current

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    item { // Item Details Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(material.name, style = MaterialTheme.typography.headlineMedium)
                            // IconButton(onClick = { onEditItemClick(item.id) }) { // TODO: Add edit navigation
                            //     Icon(Icons.Default.Edit, contentDescription = "Edit Item")
                            // }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Part Number: ${material.partNumber ?: "N/A"}")
                        Text("Category: ${material.category}")
                        Text("Location: ${item.location}")
                        Text("Unit: ${item.unitOfMeasure}")
                        Text("Quantity on Hand: ${item.quantityOnHand.formatQuantity()}", color = quantityColor, style = MaterialTheme.typography.titleMedium)
                        item.lowStockThreshold?.let {
                             Text("Low Stock Threshold: ${it.formatQuantity()}", style = MaterialTheme.typography.bodySmall)
                         }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    item { // Stock Adjustment Section
                         Text("Adjust Stock", style = MaterialTheme.typography.titleLarge)
                         Spacer(modifier = Modifier.height(8.dp))
                         OutlinedTextField(
                             value = adjustmentQuantity,
                             onValueChange = {
                                 adjustmentQuantity = it
                                 adjustmentError = null
                             },
                             label = { Text("Quantity (+/-)") },
                             keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // Ensure Decimal is used
                             modifier = Modifier.fillMaxWidth(),
                             isError = adjustmentError != null,
                             singleLine = true
                         )
                         if (adjustmentError != null) {
                             Text(adjustmentError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                         }
                         Spacer(modifier = Modifier.height(8.dp))
                         Row(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.SpaceEvenly
                         ) {
                             Button(onClick = {
                                 val quantity = adjustmentQuantity.toDoubleOrNull()
                                 if (quantity == null || quantity <= 0) {
                                     adjustmentError = "Enter a valid positive quantity"
                                 } else {
                                     viewModel.adjustStock(quantity)
                                     adjustmentQuantity = ""
                                 }
                             }) { Text("Add Stock") }
                             Button(onClick = {
                                 val quantity = adjustmentQuantity.toDoubleOrNull()
                                 if (quantity == null || quantity <= 0) {
                                      adjustmentError = "Enter a valid positive quantity"
                                 } else {
                                     viewModel.adjustStock(-quantity)
                                     adjustmentQuantity = ""
                                 }
                             },
                                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                             ) { Text("Remove Stock") }
                         }
                         HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    item { // Transaction History Header
                        Text("Transaction History", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Transaction History List
                    if (transactions.isEmpty()) {
                        item { Text("No transaction history.") }
                    } else {
                        items(transactions, key = { it.id }) { transaction ->
                            TransactionListItem(transaction = transaction, dateFormatter = dateFormatter)
                            HorizontalDivider()
                        }
                    }
                }
            }
            else -> {
                 Text("Item details not found.", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: InventoryTransaction, dateFormatter: SimpleDateFormat) {
     Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.SpaceBetween
    ) {
         Column(modifier = Modifier.weight(1f)) {
             Text(transaction.transactionType, style = MaterialTheme.typography.bodyMedium)
             Text(dateFormatter.format(transaction.timestamp), style = MaterialTheme.typography.labelSmall)
             if(transaction.relatedJobId != null) {
                  Text("Job: ${transaction.relatedJobId}", style = MaterialTheme.typography.labelSmall) // TODO: Show Job Name
             }
              if(transaction.notes.isNotEmpty()) {
                  Text("Notes: ${transaction.notes}", style = MaterialTheme.typography.labelSmall)
             }
         }
         Text(
             text = "${if(transaction.quantityChange > 0) "+" else ""}${transaction.quantityChange.formatQuantity()}",
             style = MaterialTheme.typography.bodyLarge,
             color = if(transaction.quantityChange < 0) MaterialTheme.colorScheme.error else Color.Green // Simple color coding
         )
    }
}

// Re-use or move this helper
private fun Double.formatQuantity(): String {
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        String.format("%.1f", this)
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryDetailScreenPreview() {
    ElectricianAppNewTheme {
        InventoryDetailScreen(onEditItemClick = {})
    }
}
