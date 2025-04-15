package com.example.electricianappnew.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material.icons.filled.Search // Import Search icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electricianappnew.data.model.InventoryItem
import com.example.electricianappnew.data.model.InventoryItemWithMaterial // Import the combined model
import com.example.electricianappnew.data.model.Material
import com.example.electricianappnew.ui.inventory.viewmodel.InventoryListViewModel // Import ViewModel
import com.example.electricianappnew.ui.theme.ElectricianAppNewTheme


@Composable
fun InventoryListScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryListViewModel = hiltViewModel(), // Inject ViewModel
    onItemClick: (String) -> Unit,
    onAddItemClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState() // Observe state

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItemClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Material/Item")
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
            Text("Inventory", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(16.dp))

            // Search Input Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                label = { Text("Search Inventory...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))


            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                 // Check displayedInventoryItems for empty state after filtering
                uiState.displayedInventoryItems.isEmpty() && !uiState.isLoading -> {
                     Text(
                         if (uiState.searchQuery.isBlank()) "No inventory items found." else "No items match your search.",
                         modifier = Modifier.align(Alignment.CenterHorizontally)
                     )
                }
                else -> {
                    LazyColumn {
                         // Iterate over displayedInventoryItems
                        items(uiState.displayedInventoryItems, key = { it.inventoryItem.id }) { itemWithMaterial ->
                            InventoryListItem(
                                item = itemWithMaterial.inventoryItem,
                                material = itemWithMaterial.material,
                                onClick = { onItemClick(itemWithMaterial.inventoryItem.id) },
                                onDeleteClick = { viewModel.deleteMaterial(itemWithMaterial) } // Pass delete handler
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryListItem(
    item: InventoryItem,
    material: Material?, // Associated material data
    onClick: () -> Unit,
    onDeleteClick: () -> Unit // Add onDeleteClick parameter
) {
    val isLowStock = item.lowStockThreshold != null && item.quantityOnHand <= item.lowStockThreshold!!
    val quantityColor = if (isLowStock) MaterialTheme.colorScheme.error else LocalContentColor.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(material?.name ?: "Unknown Material", style = MaterialTheme.typography.titleMedium)
            Text(
                "PN: ${material?.partNumber ?: "N/A"} | Loc: ${item.location}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "${item.quantityOnHand.formatQuantity()} ${item.unitOfMeasure}",
            style = MaterialTheme.typography.bodyLarge,
            color = quantityColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) { // Add Delete IconButton
            Icon(Icons.Default.Delete, contentDescription = "Delete Material")
        }
    }
}

// Helper to format quantity nicely
private fun Double.formatQuantity(): String {
    // Show decimal only if needed
    return if (this == this.toInt().toDouble()) {
        this.toInt().toString()
    } else {
        String.format("%.1f", this) // Show one decimal if not whole
    }
}


@Preview(showBackground = true)
@Composable
fun InventoryListScreenPreview() {
    ElectricianAppNewTheme {
        InventoryListScreen(onItemClick = {}, onAddItemClick = {})
    }
}
