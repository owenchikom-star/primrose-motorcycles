package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.owen.primrosemotorcycles.data.JobStatus
import com.owen.primrosemotorcycles.userInterface.checkup.CheckupDetailScreen
import com.owen.primrosemotorcycles.userInterface.checkup.CheckupFormScreen
import com.owen.primrosemotorcycles.userInterface.checkup.CheckupViewModel
import com.owen.primrosemotorcycles.userInterface.invoice.CreateInvoiceScreen
import com.owen.primrosemotorcycles.userInterface.jobcardList.CreateJobCardScreen
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobListEvent
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobCardListScreen
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobCardListViewModel
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobListUiState
import com.owen.primrosemotorcycles.userInterface.quotation.CreateQuotationScreen
import com.owen.primrosemotorcycles.userInterface.stock.StockManagementScreen
import kotlinx.coroutines.launch

@Composable
fun TabletJobCardLayout(
    uiState: JobListUiState,
    listVm: JobCardListViewModel,
    checkupVm: CheckupViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var section by remember { mutableStateOf("jobs") } // jobs | checkups | stock
    var selectedJobId by remember { mutableStateOf<String?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var detailDestination by remember { mutableStateOf<String?>(null) }
    var selectedCheckupBike by remember { mutableStateOf<String?>(null) }
    var selectedCheckupId by remember { mutableStateOf<Int?>(null) }

    var showAddBikeDialog by remember { mutableStateOf(false) }
    var newBikeReg by remember { mutableStateOf("") }
    var newBikeModel by remember { mutableStateOf("") }

    val checkupState by checkupVm.uiState.collectAsState()
    val allCheckups by checkupVm.allCheckups.collectAsState()
    val fleetBikes by checkupVm.fleetBikes.collectAsState()
    val scope = rememberCoroutineScope()
    val dummyNav = rememberNavController()

    Row(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // ===== LEFT PANE - Navigation + Filters =====
        Column(Modifier.width(230.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)).padding(16.dp)) {
            Text("PRIMROSE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
            Text("MOTORCYCLES", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(Modifier.height(20.dp))

            // TOP TABS
            NavigationRailItem(selected = section == "jobs", onClick = { section = "jobs" }, icon = { Icon(Icons.Default.Build, null) }, label = { Text("Job Cards") })
            NavigationRailItem(selected = section == "checkups", onClick = { section = "checkups" }, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Ackermans Checkups") })
            NavigationRailItem(selected = section == "stock", onClick = { section = "stock" }, icon = { Icon(Icons.Default.Inventory, null) }, label = { Text("Stock") })

            Spacer(Modifier.height(20.dp))

            if (section == "jobs") {
                Text("Filter", style = MaterialTheme.typography.labelLarge)
                FilterChip(selected = uiState.selectedStatus == null, onClick = { listVm.onStatusFilterChange(null) }, label = { Text("All") })
                FilterChip(selected = uiState.selectedStatus == JobStatus.OPEN, onClick = { listVm.onStatusFilterChange(JobStatus.OPEN) }, label = { Text("Open") })
                FilterChip(selected = uiState.selectedStatus == JobStatus.IN_PROGRESS, onClick = { listVm.onStatusFilterChange(JobStatus.IN_PROGRESS) }, label = { Text("In Progress") })
            } else if (section == "checkups") {
                Text("Ackermans Fleet", style = MaterialTheme.typography.labelLarge)
                Text("${fleetBikes.size} Bikes", color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showAddBikeDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Bike")
                }
            }

            Spacer(Modifier.weight(1f))
            Button(onClick = {
                if (section == "jobs") {
                    listVm.resetForNewJobCard()
                    isCreating = true
                    selectedJobId = null
                    detailDestination = null
                } else if (section == "checkups") {
                    selectedCheckupBike = null
                    selectedCheckupId = null
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (section == "jobs") "Add Job Card" else "New Checkup")
            }
        }

        // ===== MIDDLE PANE =====
        Box(Modifier.width(340.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))) {
            if (section == "jobs") {
                JobCardListScreen(
                    uiState = uiState,
                    windowWidthSizeClass = WindowWidthSizeClass.Expanded,
                    onEvent = { event ->
                        when (event) {
                            JobListEvent.AddClicked -> {
                                listVm.resetForNewJobCard()
                                isCreating = true
                                selectedJobId = null
                                detailDestination = null
                            }
                            is JobListEvent.CardClicked -> {
                                selectedJobId = event.id
                                isCreating = false
                                detailDestination = null
                            }
                            JobListEvent.StockClicked -> { section = "stock" }
                            is JobListEvent.SearchChanged -> { listVm.onSearchChange(event.query) }
                            is JobListEvent.StatusSelected -> { listVm.onStatusFilterChange(event.status) }
                            JobListEvent.ClearFilters -> { listVm.clearFilters() }
                        }
                    }
                )
            } else if (section == "checkups") {
                // CHECKUPS MIDDLE - Fleet List + Recent
                LazyColumn(Modifier.padding(12.dp)) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fleet Bikes", style = MaterialTheme.typography.titleMedium)
                            Button(onClick = { showAddBikeDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Add Bike")
                            }
                        }
                    }
                    items(fleetBikes, key = { it.registration }) { bike ->
                        val bikeReg = bike.registration
                        val last = allCheckups.find { it.bikeReg == bikeReg }
                        Card(
                            onClick = {
                                selectedCheckupBike = bikeReg
                                checkupVm.startCheckup(bikeReg)
                                selectedCheckupId = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = if (selectedCheckupBike == bikeReg) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(bikeReg, style = MaterialTheme.typography.titleMedium)
                                    Text(last?.let { "Last: ${it.overallStatus} - ${it.notes.take(40)}" } ?: "No checkup yet", style = MaterialTheme.typography.bodySmall)
                                    if (last?.overallStatus == "NEEDS_REPAIR") {
                                        Text("NEEDS REPAIR", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                IconButton(onClick = { checkupVm.deleteFleetBike(bikeReg) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Bike", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)); Text("Recent Checkups", style = MaterialTheme.typography.titleMedium) }
                    items(allCheckups.take(20)) { c ->
                        ListItem(
                            headlineContent = { Text("${c.bikeReg} - ${c.overallStatus}") },
                            supportingContent = { Text(c.notes) },
                            modifier = Modifier.clickable {
                                selectedCheckupId = c.id
                                selectedCheckupBike = c.bikeReg
                                checkupVm.loadCheckup(c.id)
                            }
                        )
                    }
                }
            } else {
                // Stock middle
                StockManagementScreen(
                    onScanBarcode = {},
                    onBack = null
                )
            }
        }

        // ===== RIGHT PANE =====
        Box(Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)).padding(16.dp)) {
            if (section == "stock") {
                Text("Select action from stock list", style = MaterialTheme.typography.titleMedium)
            } else if (section == "jobs") {
                when {
                    detailDestination == "quotation" && selectedJobId != null -> {
                        CreateQuotationScreen(
                            jobId = selectedJobId!!,
                            navController = dummyNav,
                            onBack = { detailDestination = null }
                        )
                    }
                    detailDestination == "invoice" && selectedJobId != null -> {
                        CreateInvoiceScreen(
                            jobId = selectedJobId!!,
                            navController = dummyNav,
                            onBack = { detailDestination = null }
                        )
                    }
                    isCreating || selectedJobId != null -> {
                        key(isCreating, selectedJobId) {
                            CreateJobCardScreen(
                                dao = listVm.dao,
                                firebase = listVm.firestore,
                                viewModel = listVm,
                                navController = dummyNav,
                                jobId = if (isCreating) null else selectedJobId,
                                onBack = { isCreating = false; selectedJobId = null },
                                onSaved = { isCreating = false; selectedJobId = null },
                                onCreateQuotation = { id -> selectedJobId = id; detailDestination = "quotation" },
                                onCreateInvoice = { id -> selectedJobId = id; detailDestination = "invoice" },
                                onOpenStock = { section = "stock" }
                            )
                        }
                    }
                    else -> { Text("Select a Job Card or Add New", style = MaterialTheme.typography.titleMedium) }
                }
            } else {
                // CHECKUPS RIGHT - Form or Detail
                if (selectedCheckupId != null) {
                    CheckupDetailScreen(
                        checkupId = selectedCheckupId!!,
                        dao = checkupVm.checkupDao,
                        onBack = { selectedCheckupId = null; selectedCheckupBike = null },
                        onFixNow = { reg, issue ->
                            scope.launch {
                                val jobId = checkupVm.createJobCardFromIssue(reg, issue)
                                section = "jobs"
                                selectedJobId = jobId
                                isCreating = false
                                detailDestination = null
                            }
                        }
                    )
                } else {
                    CheckupFormScreen(
                        state = checkupState,
                        onOdometer = { checkupVm.setOdometer(it) },
                        onSignature = { checkupVm.setDriverSignature(it) },
                        onStatus = { item, status -> checkupVm.setItemStatus(item, status) },
                        onNote = { item, note -> checkupVm.setItemNote(item, note) },
                        onFixNow = { item, note ->
                            scope.launch {
                                val jobId = checkupVm.createJobCardFromIssue(checkupState.bikeReg, "$item: $note")
                                section = "jobs"
                                selectedJobId = jobId
                                isCreating = false
                                detailDestination = null
                            }
                        },
                        onSave = {
                            checkupVm.saveCheckup {
                                selectedCheckupBike = null
                                selectedCheckupId = null
                                checkupVm.clearForm()
                            }
                        },
                        onBack = { selectedCheckupBike = null; selectedCheckupId = null }
                    )
                }
            }
        }
    }

    if (showAddBikeDialog) {
        AlertDialog(
            onDismissRequest = { showAddBikeDialog = false },
            title = { Text("Add Bike to Fleet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBikeReg,
                        onValueChange = { newBikeReg = it },
                        label = { Text("Bike Registration / Name (e.g. AKM-005)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newBikeModel,
                        onValueChange = { newBikeModel = it },
                        label = { Text("Model (e.g. Honda ACE 125)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBikeReg.isNotBlank()) {
                            val reg = newBikeReg.trim()
                            val model = newBikeModel.trim()
                            val fullReg = if (model.isNotBlank() && !reg.contains("-")) "$reg - $model" else reg
                            checkupVm.addFleetBike(fullReg, model)
                            newBikeReg = ""
                            newBikeModel = ""
                            showAddBikeDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBikeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
