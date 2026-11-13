package com.owen.primrosemotorcycles.userInterface.jobcardList

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.owen.primrosemotorcycles.R
import kotlinx.coroutines.flow.flowOf

@Composable
fun SparesSection(
    jobCardId: String?,
    viewModel: JobCardListViewModel
) {
    val context = LocalContext.current

    val partsFlow = remember(jobCardId) {
        if (jobCardId.isNullOrBlank()) flowOf(emptyList()) else viewModel.sparePartDao.getForJob(jobCardId)
    }
    val totalFlow = remember(jobCardId) {
        if (jobCardId.isNullOrBlank()) flowOf(0.0) else viewModel.sparePartDao.getTotalForJob(jobCardId)
    }

    val parts by partsFlow.collectAsState(initial = emptyList())
    val totalRaw by totalFlow.collectAsState(initial = 0.0)
    val total = totalRaw ?: 0.0

    val sparesState by viewModel.sparesUiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray).padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.section_spares_materials), fontWeight = FontWeight.Bold)
            Button(
                onClick = { viewModel.openAddSpareDialog() },
                enabled = !jobCardId.isNullOrBlank()
            ) {
                Text(stringResource(R.string.button_add_spare))
            }
        }

        if (parts.isEmpty()) {
            Text(stringResource(R.string.no_spares_added), fontSize = 12.sp, color = Color.Gray)
        } else {
            parts.forEach { part ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openEditSpareDialog(part) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${part.name} (${part.partNumber}) x${part.quantity} - R${part.total}",
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp
                    )
                    IconButton(onClick = { viewModel.deleteSpare(part) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                }
            }
            HorizontalDivider()
            Text("TOTAL: R $total", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))

            Button(
                onClick = {
                    if (!jobCardId.isNullOrBlank()) {
                        viewModel.generateRequisitionPdf(context, jobCardId) { file ->
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Open Requisition PDF"))
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.generate_requisition_pdf))
            }
        }
    }

    if (sparesState.showDialog && !jobCardId.isNullOrBlank()) {
        AddSpareDialog(
            state = sparesState,
            onDismiss = { viewModel.closeSpareDialog() },
            onNameChange = { viewModel.updateSpareName(it) },
            onPartNoChange = { viewModel.updateSparePartNumber(it) },
            onQtyChange = { viewModel.updateSpareQuantity(it) },
            onPriceChange = { viewModel.updateSpareUnitPrice(it) },
            onSave = { viewModel.confirmSaveSpare(jobCardId) }
        )
    }
}

@Composable
fun AddSpareDialog(
    state: SparesUiState,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onPartNoChange: (String) -> Unit,
    onQtyChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (state.editingPart != null) stringResource(R.string.edit_spare_title) else stringResource(R.string.add_spare_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.label_spare_name)) }
                )
                OutlinedTextField(
                    value = state.partNumber,
                    onValueChange = onPartNoChange,
                    label = { Text(stringResource(R.string.label_part_number)) }
                )
                OutlinedTextField(
                    value = state.quantity,
                    onValueChange = onQtyChange,
                    label = { Text(stringResource(R.string.label_quantity)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.unitPrice,
                    onValueChange = onPriceChange,
                    label = { Text(stringResource(R.string.label_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = state.name.isNotBlank()
            ) {
                Text(if (state.editingPart != null) stringResource(R.string.update) else stringResource(R.string.add))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
