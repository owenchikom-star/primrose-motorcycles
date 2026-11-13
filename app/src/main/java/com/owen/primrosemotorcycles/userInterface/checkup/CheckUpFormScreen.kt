package com.owen.primrosemotorcycles.userInterface.checkup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.owen.primrosemotorcycles.data.AckermansChecklist
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.SignatureDisplay
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.SignaturePad

@Composable
fun CheckupFormScreen(
    bikeReg: String,
    viewModel: CheckupViewModel = hiltViewModel(),
    onFixNow: (String, String) -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(bikeReg) {
        viewModel.startCheckup(bikeReg)
    }
    CheckupFormScreen(
        state = state,
        onOdometer = viewModel::setOdometer,
        onSignature = viewModel::setDriverSignature,
        onStatus = viewModel::setItemStatus,
        onNote = viewModel::setItemNote,
        onFixNow = { item, note -> onFixNow(state.bikeReg, "$item: $note") },
        onSave = {
            viewModel.saveCheckup {
                onSaved()
            }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckupFormScreen(
    state: CheckupUiState,
    onOdometer: (String) -> Unit,
    onSignature: (String) -> Unit,
    onStatus: (String, String) -> Unit,
    onNote: (String, String) -> Unit,
    onFixNow: (String, String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkup: ${state.bikeReg}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.odometer,
                onValueChange = onOdometer,
                label = { Text("Odometer") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            AckermansChecklist.forEach { item ->
                val status = state.items[item] ?: "OK"
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(item, style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = status == "OK", onClick = { onStatus(item, "OK") }, label = { Text("OK") })
                            FilterChip(selected = status == "NOTE", onClick = { onStatus(item, "NOTE") }, label = { Text("Note for Service") })
                            FilterChip(selected = status == "FIX_NOW", onClick = { onStatus(item, "FIX_NOW") }, label = { Text("Fix Now") })
                        }
                        if (status != "OK") {
                            OutlinedTextField(
                                value = state.itemNotes[item] ?: "",
                                onValueChange = { onNote(item, it) },
                                label = { Text("What is wrong?") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (status == "FIX_NOW") {
                                Button(onClick = { onFixNow(item, state.itemNotes[item] ?: "") }) {
                                    Text("Fix Now - Open Job Card")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Driver Signature", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (state.driverSignature.isNotBlank()) {
                SignatureDisplay(
                    signatureBase64 = state.driverSignature,
                    onClear = { onSignature("") },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                SignaturePad(
                    onSignatureCaptured = { onSignature(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Save Weekly Checkup")
            }
        }
    }
}
