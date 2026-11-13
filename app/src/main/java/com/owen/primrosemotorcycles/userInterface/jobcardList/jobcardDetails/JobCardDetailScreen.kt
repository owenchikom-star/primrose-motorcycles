package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.owen.primrosemotorcycles.data.JobCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardDetailScreen(
    jobCard: JobCard?,
    viewModel: JobCardDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(jobCard?.jobNumber ?: "Job Card") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp).navigationBarsPadding()) {
                Button(
                    onClick = {
                        jobCard?.let { j ->
                            viewModel.generateJobCardPdf(context, j.id) { file ->
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share PDF"))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = jobCard != null
                ) {
                    Text("Generate PDF & Share")
                }
            }
        }
    ) { pad ->
        if (jobCard == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier
                .padding(pad)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header info - editable
            Text("Primrose motorcycles - JOB CARD", style = MaterialTheme.typography.titleMedium)
            EditableField("NUMBER", jobCard.jobNumber) { valText -> viewModel.updateField { it.copy(jobNumber = valText) } }
            EditableField("NAME", jobCard.customerName) { valText -> viewModel.updateField { it.copy(customerName = valText) } }
            EditableField("TEL", jobCard.customerPhone) { valText -> viewModel.updateField { it.copy(customerPhone = valText) } }
            EditableField("MAKE + MODEL", jobCard.bikeModel) { valText -> viewModel.updateField { it.copy(bikeModel = valText) } }
            EditableField("COLOR", jobCard.bikeColor) { valText -> viewModel.updateField { it.copy(bikeColor = valText) } }
            EditableField("REG NO.", jobCard.bikeReg) { valText -> viewModel.updateField { it.copy(bikeReg = valText) } }
            EditableField("ODOMETER", jobCard.mileage) { valText -> viewModel.updateField { it.copy(mileage = valText) } }
            EditableField("VIN NO.", jobCard.vinNo) { valText -> viewModel.updateField { it.copy(vinNo = valText) } }
            EditableField("ENG NO.", jobCard.engNo) { valText -> viewModel.updateField { it.copy(engNo = valText) } }

            HorizontalDivider()
            Text("CUSTOMER'S INSTRUCTIONS", style = MaterialTheme.typography.titleSmall)
            EditableField("Instructions", jobCard.customerInstructions, maxLines = 4) { valText -> viewModel.updateField { it.copy(customerInstructions = valText) } }

            HorizontalDivider()
            Text("SERVICE CHECKLIST - Fill Rands / Cents", style = MaterialTheme.typography.titleSmall)
            // R/C Table like paper
            ChecklistRow(
                "ENGINE OIL DRAINED / TOPPED UP", jobCard.engineOilQty, "litres", jobCard.engineOilR, jobCard.engineOilC,
                onQty = { valText -> viewModel.updateField { it.copy(engineOilQty = valText) } },
                onR = { valText -> viewModel.updateField { it.copy(engineOilR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(engineOilC = valText) } }
            )
            ChecklistRow(
                "GEAR BOX DRAINED / TOPPED UP", jobCard.gearBoxQty, "litres", jobCard.gearBoxR, jobCard.gearBoxC,
                onQty = { valText -> viewModel.updateField { it.copy(gearBoxQty = valText) } },
                onR = { valText -> viewModel.updateField { it.copy(gearBoxR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(gearBoxC = valText) } }
            )
            ChecklistRow(
                "BRAKE FLUID CHANGED / TOPPED UP", jobCard.brakeFluidQty, "ml", jobCard.brakeFluidR, jobCard.brakeFluidC,
                onQty = { valText -> viewModel.updateField { it.copy(brakeFluidQty = valText) } },
                onR = { valText -> viewModel.updateField { it.copy(brakeFluidR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(brakeFluidC = valText) } }
            )
            SimplePriceRow(
                "AIR FILTER CLEANED / REPLACED", jobCard.airFilterR, jobCard.airFilterC,
                onR = { valText -> viewModel.updateField { it.copy(airFilterR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(airFilterC = valText) } }
            )
            SimplePriceRow(
                "OIL FILTER CHANGED", jobCard.oilFilterR, jobCard.oilFilterC,
                onR = { valText -> viewModel.updateField { it.copy(oilFilterR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(oilFilterC = valText) } }
            )
            SimplePriceRow(
                "ENGINE CLEANED / FLUSHED", jobCard.engineCleanedR, jobCard.engineCleanedC,
                onR = { valText -> viewModel.updateField { it.copy(engineCleanedR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(engineCleanedC = valText) } }
            )
            SimplePriceRow(
                "WASHED / POLISHED", jobCard.washedR, jobCard.washedC,
                onR = { valText -> viewModel.updateField { it.copy(washedR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(washedC = valText) } }
            )
            SimplePriceRow(
                "SERVICE FEE", jobCard.serviceFeeR, jobCard.serviceFeeC,
                onR = { valText -> viewModel.updateField { it.copy(serviceFeeR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(serviceFeeC = valText) } }
            )

            HorizontalDivider()
            Text("WORKSHOP NOTES", style = MaterialTheme.typography.titleSmall)
            EditableField("Notes", jobCard.workshopNotes, maxLines = 4) { valText -> viewModel.updateField { it.copy(workshopNotes = valText) } }

            Text("WORKSHOP REPAIRS", style = MaterialTheme.typography.titleSmall)
            EditableField("Repairs", jobCard.workshopRepairs, maxLines = 6) { valText -> viewModel.updateField { it.copy(workshopRepairs = valText) } }

            HorizontalDivider()
            Text("SPARES & MATERIALS", style = MaterialTheme.typography.titleSmall)
            EditableField("Spares list", jobCard.sparesList, maxLines = 4) { valText -> viewModel.updateField { it.copy(sparesList = valText) } }
            SimplePriceRow(
                "SPARES TOTAL", jobCard.sparesR, jobCard.sparesC,
                onR = { valText -> viewModel.updateField { it.copy(sparesR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(sparesC = valText) } }
            )
            SimplePriceRow(
                "FINAL TOTAL", jobCard.finalTotalR, jobCard.finalTotalC,
                onR = { valText -> viewModel.updateField { it.copy(finalTotalR = valText) } },
                onC = { valText -> viewModel.updateField { it.copy(finalTotalC = valText) } }
            )
            EditableField("Next service at (kms)", jobCard.nextServiceKms) { valText -> viewModel.updateField { it.copy(nextServiceKms = valText) } }

            HorizontalDivider()
            Text("CUSTOMER SIGNATURE", style = MaterialTheme.typography.titleSmall)
            if (jobCard.customerSignature.isNotBlank()) {
                SignatureDisplay(
                    signatureBase64 = jobCard.customerSignature,
                    onClear = { viewModel.updateField { it.copy(customerSignature = "") } }
                )
            } else {
                SignaturePad(
                    onSignatureCaptured = { base64 ->
                        viewModel.updateField { it.copy(customerSignature = base64) }
                    }
                )
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardDetailScreen(
    viewModel: JobCardDetailViewModel = hiltViewModel(),
    jobId: String,
    onBack: () -> Unit
) {
    LaunchedEffect(jobId) { viewModel.load(jobId) }
    val job by viewModel.jobCard.collectAsState()
    JobCardDetailScreen(
        jobCard = job,
        viewModel = viewModel,
        onBack = onBack
    )
}

@Composable
fun EditableField(label: String, value: String, maxLines: Int = 1, onChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        maxLines = maxLines
    )
}

@Composable
fun ChecklistRow(
    title: String,
    qty: String,
    qtyLabel: String,
    r: String,
    c: String,
    onQty: (String) -> Unit,
    onR: (String) -> Unit,
    onC: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.labelSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = qty, onValueChange = onQty, label = { Text(qtyLabel) }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = r, onValueChange = onR, label = { Text("R") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = c, onValueChange = onC, label = { Text("C") }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SimplePriceRow(
    title: String,
    r: String,
    c: String,
    onR: (String) -> Unit,
    onC: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, Modifier.weight(2f).padding(top = 16.dp), style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(value = r, onValueChange = onR, label = { Text("R") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = c, onValueChange = onC, label = { Text("C") }, modifier = Modifier.weight(1f))
    }
}