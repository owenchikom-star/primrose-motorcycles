package com.owen.primrosemotorcycles.userInterface.invoice

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.owen.primrosemotorcycles.R
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobCardListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    jobId: String,
    viewModel: JobCardListViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val formState by viewModel.createUiState.collectAsState()
    val invoiceState by viewModel.invoiceUiState.collectAsState()
    val items by viewModel.invoiceItems.collectAsState()

    LaunchedEffect(jobId) {
        viewModel.loadJobForEdit(jobId)
        viewModel.initInvoiceStateForJob()
        viewModel.loadSparesIntoInvoice(jobId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Tax Invoice") },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
            )
        },
        bottomBar = {
            Column(Modifier.padding(16.dp).navigationBarsPadding()) {
                Button(
                    onClick = {
                        viewModel.generateInvoicePdf(context, jobId) { file ->
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Generate Tax Invoice PDF & Share")
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Autofilled from Job Card: ${formState.jobNumber}", style = MaterialTheme.typography.titleSmall)
                    Text("Vehicle: ${formState.bikeModel}", fontSize = 11.sp)
                    Text("Reg No: ${formState.bikeReg}", fontSize = 11.sp)
                    Text("Mileage: ${formState.mileage}", fontSize = 11.sp)
                    Text("Customer: ${formState.customerName}", fontSize = 11.sp)
                }
            }

            OutlinedTextField(
                value = invoiceState.reference,
                onValueChange = { viewModel.updateInvoiceReference(it) },
                label = { Text(stringResource(R.string.label_reference)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = invoiceState.salesRep,
                onValueChange = { viewModel.updateInvoiceSalesRep(it) },
                label = { Text(stringResource(R.string.label_sales_rep)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = invoiceState.stockNo,
                onValueChange = { viewModel.updateInvoiceStockNo(it) },
                label = { Text(stringResource(R.string.label_stock_no)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = invoiceState.customerNotes,
                onValueChange = { viewModel.updateInvoiceCustomerNotes(it) },
                label = { Text("Customer Notes / Estimate Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Invoice Line Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.setInvoiceAddItemDialogVisible(true) }) {
                    Text("+ Add Item")
                }
            }

            items.forEach { item ->
                OutlinedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp)) {
                        Text(item.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Qty: ${item.quantity}", fontSize = 11.sp)
                            Text("Incl: R${String.format(java.util.Locale.US, "%.2f", item.inclPrice)}", fontSize = 11.sp)
                            Text("Total: R${String.format(java.util.Locale.US, "%.2f", item.totalExcl)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (invoiceState.showAddItemDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setInvoiceAddItemDialogVisible(false) },
                title = { Text("Add Invoice Item") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = invoiceState.itemDescription,
                            onValueChange = { viewModel.updateInvoiceItemDescription(it) },
                            label = { Text("Description / Part") }
                        )
                        OutlinedTextField(
                            value = invoiceState.itemQuantity,
                            onValueChange = { viewModel.updateInvoiceItemQuantity(it) },
                            label = { Text("Quantity") }
                        )
                        OutlinedTextField(
                            value = invoiceState.itemPrice,
                            onValueChange = { viewModel.updateInvoiceItemPrice(it) },
                            label = { Text("Incl. Price (R)") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.confirmAddInvoiceItem(jobId) }) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setInvoiceAddItemDialogVisible(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
