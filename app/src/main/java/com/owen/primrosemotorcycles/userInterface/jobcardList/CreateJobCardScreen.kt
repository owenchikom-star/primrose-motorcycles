package com.owen.primrosemotorcycles.userInterface.jobcardList

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.owen.primrosemotorcycles.R
import com.owen.primrosemotorcycles.data.JobCardDao
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.SignatureDisplay
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.SignaturePad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobCardScreen(
    dao: JobCardDao,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    jobId: String? = null,
    onCreateQuotation: ((String) -> Unit)? = null,
    onCreateInvoice: ((String) -> Unit)? = null,
    onOpenStock: (() -> Unit)? = null,
    navController: NavController,
    viewModel: JobCardListViewModel,
    firebase: FirebaseFirestore? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val formState by viewModel.createUiState.collectAsState()

    // Populate data if editing an existing JobCard, or generate job number if creating new
    LaunchedEffect(jobId) {
        if (!jobId.isNullOrBlank()) {
            viewModel.loadJobForEdit(jobId)
        } else {
            viewModel.initNewIfNeeded()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (!jobId.isNullOrBlank()) stringResource(R.string.edit_job_card) else stringResource(R.string.new_job_card)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(formState.jobNumber, style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.branch_primrose), style = MaterialTheme.typography.bodySmall)
                }
            }

            Text(stringResource(R.string.section_customer), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = formState.customerName,
                onValueChange = { viewModel.updateCustomerName(it) },
                label = { Text(stringResource(R.string.label_customer_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.customerPhone,
                onValueChange = { viewModel.updateCustomerPhone(it) },
                label = { Text(stringResource(R.string.label_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.driverName,
                onValueChange = { viewModel.updateDriverName(it) },
                label = { Text(stringResource(R.string.label_driver_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.driverPhone,
                onValueChange = { viewModel.updateDriverPhone(it) },
                label = { Text(stringResource(R.string.label_driver_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.customerAddress,
                onValueChange = { viewModel.updateCustomerAddress(it) },
                label = { Text(stringResource(R.string.label_address)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.section_bike), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = formState.bikeReg,
                onValueChange = { viewModel.updateBikeReg(it) },
                label = { Text(stringResource(R.string.label_reg_number)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.bikeModel,
                onValueChange = { viewModel.updateBikeModel(it) },
                label = { Text(stringResource(R.string.label_model)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.bikeColor,
                onValueChange = { viewModel.updateBikeColor(it) },
                label = { Text(stringResource(R.string.label_color)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.mileage,
                onValueChange = { viewModel.updateMileage(it) },
                label = { Text(stringResource(R.string.label_mileage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.vinNo,
                onValueChange = { viewModel.updateVinNo(it) },
                label = { Text(stringResource(R.string.label_vin)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.engNo,
                onValueChange = { viewModel.updateEngNo(it) },
                label = { Text(stringResource(R.string.label_eng)) },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.section_job_details), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = formState.workshopNotes,
                onValueChange = { viewModel.updateWorkshopNotes(it) },
                label = { Text(stringResource(R.string.label_workshop_notes)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formState.workshopRepairs,
                onValueChange = { viewModel.updateWorkshopRepairs(it) },
                label = { Text(stringResource(R.string.label_workshop_repairs)) },
                modifier = Modifier.fillMaxWidth()
            )
            SparesSection(
                jobCardId = formState.jobId ?: formState.currentJobId,
                viewModel = viewModel
            )
            OutlinedTextField(
                value = formState.customerInstructions,
                onValueChange = { viewModel.updateCustomerInstructions(it) },
                label = { Text(stringResource(R.string.label_customer_instructions)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = formState.issue,
                onValueChange = { viewModel.updateIssue(it) },
                label = { Text(stringResource(R.string.label_reported_issue)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                minLines = 3
            )
            OutlinedTextField(
                value = formState.technician,
                onValueChange = { viewModel.updateTechnician(it) },
                label = { Text(stringResource(R.string.label_technician)) },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()
            Text(stringResource(R.string.section_customer_signature), style = MaterialTheme.typography.titleMedium)
            if (formState.customerSignature.isNotBlank()) {
                SignatureDisplay(
                    signatureBase64 = formState.customerSignature,
                    onClear = { viewModel.updateCustomerSignature("") }
                )
            } else {
                SignaturePad(
                    onSignatureCaptured = { base64 -> viewModel.updateCustomerSignature(base64) }
                )
            }

            val enabled = formState.customerName.isNotBlank() && formState.issue.isNotBlank()

            Button(
                onClick = {
                    viewModel.saveCurrentJobCard(onSaved)
                },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (!jobId.isNullOrBlank()) stringResource(R.string.update_job_card) else stringResource(R.string.save_job_card))
            }

            if (!jobId.isNullOrBlank()) {
                Button(
                    onClick = {
                        viewModel.generateCurrentJobCardPdf(context) { file ->
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share PDF"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = true
                ) {
                    Text(stringResource(R.string.generate_pdf_share))
                }
                Button(
                    onClick = {
                        viewModel.loadSparesIntoQuotation()
                        navController.navigate("create_quotation/${formState.jobId}")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.create_quotation_from_job_card))
                }
                Button(
                    onClick = {
                        viewModel.loadSparesIntoInvoice()
                        navController.navigate("create_invoice/${formState.jobId}")
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Create Tax Invoice from Job Card")
                }
            }

            // NEW BUTTONS - Only show if job already exists (not creating new)
            if (!jobId.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // QUOTATION
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (onCreateQuotation != null) {
                                onCreateQuotation(jobId) // TABLET: switch right pane
                            } else {
                                navController.navigate("create_quotation/$jobId") // PHONE: old way
                            }
                        }
                    ) { Text("Quotation") }

                    // INVOICE
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (onCreateInvoice != null) {
                                onCreateInvoice(jobId)
                            } else {
                                navController.navigate("create_invoice/$jobId")
                            }
                        }
                    ) { Text("Invoice") }

                    // STOCK (optional)
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (onOpenStock != null) onOpenStock()
                            else navController.navigate("stock")
                        }
                    ) { Text("Stock") }
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = onSaved, modifier = Modifier.fillMaxWidth()) {
                Text("Save Job Card")
            }
        }
    }
}
