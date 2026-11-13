package com.owen.primrosemotorcycles.userInterface.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.owen.primrosemotorcycles.data.StockItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockManagementScreen(
    viewModel: StockViewModel = hiltViewModel(),
    onScanBarcode: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val allStock by viewModel.allStock.collectAsState()
    val lowStock by viewModel.lowStock.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val stockUiState by viewModel.stockUiState.collectAsState()

    val query = stockUiState.searchQuery
    val showAddDialog = stockUiState.showAddDialog
    val selectedTab = stockUiState.selectedTab

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spare Parts Stock") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (lowStock.isNotEmpty()) {
                        BadgedBox(badge = { Badge { Text("${lowStock.size}") } }) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Low Stock",
                                tint = Color.Red
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { viewModel.setShowAddDialog(true) }) {
                Icon(Icons.Default.Add, null); Text("Add Stock")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search Part No / Name / Barcode") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    IconButton(onClick = onScanBarcode) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("All (${allStock.size})") })
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Low Stock (${lowStock.size})", color = Color.Red) })
            }

            val list =
                if (query.isNotBlank()) searchResults else if (selectedTab == 1) lowStock else allStock

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { item ->
                    StockItemCard(
                        item,
                        onStockIn = { viewModel.addStockIn(item.id, 1, "Manual In") },
                        onStockOut = { viewModel.addStockIn(item.id, -1, "Manual Out") },
                        onUseInJob = { viewModel.deductForJobCard(item.id, 1, "TEST-JOB") },
                        onEdit = { /*TODO*/ },
                        onDelete = { /*TODO*/ }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddStockDialog(
            onDismiss = { viewModel.setShowAddDialog(false) },
            onSave = { partNumber, name, qty, minQty, cost, sell, category, location, supplier, brand, barcode ->
                viewModel.createItem(
                    partNumber = partNumber,
                    name = name,
                    qty = qty,
                    minQty = minQty,
                    cost = cost,
                    sell = sell
                )
                viewModel.setShowAddDialog(false)
            }
        )
    }
}

@Composable
fun StockItemCard(
    item: StockItem,
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onUseInJob: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, if (item.isLowStock) Color.Red else Color.LightGray)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(item.partNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(item.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Bin: ${item.location} | Cost: R${item.costPrice} | Sell: R${item.sellingPrice}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${item.quantityOnHand}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = if (item.isLowStock) Color.Red else Color.Black
                    )
                    if (item.isLowStock) Text(
                        "LOW!",
                        color = Color.Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onStockIn) { Text("+IN") }
                OutlinedButton(onClick = onStockOut) { Text("-OUT") }
                Button(onClick = onUseInJob, enabled = !item.isOutOfStock) { Text("Use 1") }
            }
        }
    }
}
