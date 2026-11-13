package com.owen.primrosemotorcycles.userInterface.checkup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.owen.primrosemotorcycles.data.CheckupDao
import com.owen.primrosemotorcycles.data.FleetBike
import kotlinx.coroutines.launch
import java.util.Date

@Composable
fun CheckupListScreen(
    onStartCheckup: (String) -> Unit,
    onOpenCheckup: (Int) -> Unit,
    dao: CheckupDao
) {
    val checkups by dao.getAllCheckups().collectAsState(initial = emptyList())
    val fleetBikes by dao.getAllFleetBikes().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var newBikeReg by remember { mutableStateOf("") }
    var newBikeModel by remember { mutableStateOf("") }

    // Seed default fleet bikes if table is empty
    LaunchedEffect(fleetBikes) {
        if (fleetBikes.isEmpty()) {
            listOf(
                FleetBike("AKM-001 - Honda ACE", "Honda ACE"),
                FleetBike("AKM-002 - Honda ACE", "Honda ACE"),
                FleetBike("AKM-003 - Yamaha", "Yamaha"),
                FleetBike("AKM-004 - Honda ACE", "Honda ACE")
            ).forEach { dao.insertFleetBike(it) }
        }
    }

    LazyColumn(Modifier.padding(16.dp)) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Ackermans Pharmacy - Weekly Checkups",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Fleet: ${fleetBikes.size} Bikes",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Button(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bike")
                    Spacer(Modifier.width(4.dp))
                    Text("Add Bike")
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        items(fleetBikes, key = { it.registration }) { bike ->
            Card(
                onClick = { onStartCheckup(bike.registration) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(bike.registration, style = MaterialTheme.typography.titleMedium)
                        val last = checkups.find { it.bikeReg == bike.registration }
                        Text(
                            if (last != null) "Last: ${Date(last.date)} - ${last.overallStatus}" else "No checkup yet",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(onClick = { onStartCheckup(bike.registration) }) {
                        Text("Start Check")
                    }

                    IconButton(onClick = {
                        scope.launch {
                            dao.deleteFleetBike(bike)
                        }
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Bike",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("Recent Checkups", style = MaterialTheme.typography.titleLarge)
        }

        items(checkups.take(10), key = { it.id }) { c ->
            ListItem(
                headlineContent = { Text("${c.bikeReg} - ${c.overallStatus}") },
                supportingContent = { Text(c.notes) },
                modifier = Modifier.clickable { onOpenCheckup(c.id) }
            )
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
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
                            scope.launch {
                                dao.insertFleetBike(FleetBike(registration = fullReg, model = model))
                            }
                            newBikeReg = ""
                            newBikeModel = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
