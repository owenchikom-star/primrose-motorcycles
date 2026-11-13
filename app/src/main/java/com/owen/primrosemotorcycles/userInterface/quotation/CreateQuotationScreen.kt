package com.owen.primrosemotorcycles.userInterface.quotation

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.userInterface.jobcardList.JobCardListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuotationScreen(
    jobId: String,
    viewModel: JobCardListViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val formState by viewModel.createUiState.collectAsState()
    val quotationState by viewModel.quotationUiState.collectAsState()
    val items by viewModel.quotationItems.collectAsState()

    LaunchedEffect(jobId) {
        viewModel.loadJobForEdit(jobId)
        viewModel.initQuotationStateForJob()
        viewModel.loadSparesIntoQuotation(jobId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_quotation)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.autofilled_from_job_card, formState.jobNumber), style = MaterialTheme.typography.titleSmall)
                    Text("001 - VEHICLE -${formState.bikeModel}", fontSize = 11.sp)
                    Text("002 - REG -${formState.bikeReg}", fontSize = 11.sp)
                    Text("003 - KM -${formState.mileage}", fontSize = 11.sp)
                    Text("004 - DRIVER -${formState.driverName}", fontSize = 11.sp)
                }
            }

            OutlinedTextField(
                value = quotationState.reference,
                onValueChange = { viewModel.updateQuotationReference(it) },
                label = { Text(stringResource(R.string.label_reference)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = quotationState.salesRep,
                onValueChange = { viewModel.updateQuotationSalesRep(it) },
                label = { Text(stringResource(R.string.label_sales_rep)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = quotationState.stockNo,
                onValueChange = { viewModel.updateQuotationStockNo(it) },
                label = { Text(stringResource(R.string.label_stock_no)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = quotationState.customerNotes,
                onValueChange = { viewModel.updateQuotationCustomerNotes(it) },
                label = { Text(stringResource(R.string.label_customer_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            HorizontalDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.section_parts_labour, items.size), fontWeight = FontWeight.Bold)
                Button(onClick = { viewModel.setQuotationAddItemDialogVisible(true) }) { Text(stringResource(R.string.button_add)) }
            }

            items.forEach { item ->
                Row(Modifier.fillMaxWidth()) {
                    Text(item.description, Modifier.weight(1f), fontSize = 12.sp)
                    Text("R${item.inclPrice} x${item.quantity.toInt()}", fontSize = 12.sp)
                }
            }

            val excl = items.sumOf { it.inclPrice * it.quantity / 1.15 }
            val vat = excl * 0.15
            val total = excl + vat

            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.label_total_exclusive, String.format("%.2f", excl)))
                    Text(stringResource(R.string.label_total_vat, String.format("%.2f", vat)))
                    Text(stringResource(R.string.label_total_due, String.format("%.2f", total)), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }

            Button(
                onClick = {
                    val quote = viewModel.prepareQuotationFromCurrentJob(jobId).copy(
                        reference = quotationState.reference,
                        salesRep = quotationState.salesRep,
                        stockNo = quotationState.stockNo,
                        customerNotes = quotationState.customerNotes
                    )
                    val currentJobCard = JobCard(
                        id = formState.jobId ?: formState.currentJobId,
                        jobNumber = formState.jobNumber,
                        customerName = formState.customerName,
                        customerPhone = formState.customerPhone,
                        customerAddress = formState.customerAddress,
                        driver = formState.driverName,
                        driverPhone = formState.driverPhone,
                        bikeReg = formState.bikeReg,
                        bikeModel = formState.bikeModel,
                        bikeColor = formState.bikeColor,
                        mileage = formState.mileage,
                        vinNo = formState.vinNo,
                        engNo = formState.engNo,
                        sparesList = formState.sparesMaterials,
                        customerInstructions = formState.customerInstructions,
                        workshopNotes = formState.workshopNotes,
                        workshopRepairs = formState.workshopRepairs
                    )
                    val generator = QuotationPdfGenerator(context)
                    val file = generator.generate(
                        quote = quote.copy(id = quote.id),
                        jobCard = currentJobCard,
                        items = items.map { it.copy(quotationId = quote.id) }
                    )
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "View Quotation"))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(R.string.generate_quotation_pdf))
            }
        }
    }

    if (quotationState.showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setQuotationAddItemDialogVisible(false) },
            title = { Text(stringResource(R.string.add_item_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = quotationState.itemDescription,
                        onValueChange = { viewModel.updateQuotationItemDescription(it) },
                        label = { Text(stringResource(R.string.label_item_description)) }
                    )
                    OutlinedTextField(
                        value = quotationState.itemQuantity,
                        onValueChange = { viewModel.updateQuotationItemQuantity(it) },
                        label = { Text(stringResource(R.string.label_quantity)) }
                    )
                    OutlinedTextField(
                        value = quotationState.itemPrice,
                        onValueChange = { viewModel.updateQuotationItemPrice(it) },
                        label = { Text(stringResource(R.string.label_incl_price)) }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.confirmAddQuotationItem(targetJobId = jobId)
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setQuotationAddItemDialogVisible(false) }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}
