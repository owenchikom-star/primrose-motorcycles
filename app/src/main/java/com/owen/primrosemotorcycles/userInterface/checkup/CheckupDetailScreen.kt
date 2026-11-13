package com.owen.primrosemotorcycles.userInterface.checkup

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.owen.primrosemotorcycles.data.CheckupDao
import com.owen.primrosemotorcycles.data.CheckupItem
import com.owen.primrosemotorcycles.data.WeeklyCheckup
import com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails.SignatureDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckupDetailScreen(
    checkupId: Int,
    dao: CheckupDao,
    onBack: () -> Unit,
    onFixNow: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var checkup by remember { mutableStateOf<WeeklyCheckup?>(null) }
    var items by remember { mutableStateOf<List<CheckupItem>>(emptyList()) }

    LaunchedEffect(checkupId) {
        checkup = dao.getCheckupById(checkupId)
        items = dao.getItemsSync(checkupId)
    }

    val currentCheckup = checkup

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentCheckup?.let { "Checkup: ${it.bikeReg}" } ?: "Checkup Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentCheckup != null) {
                        IconButton(onClick = {
                            val generator = CheckupPdfGenerator(context)
                            val file = generator.generate(currentCheckup, items)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Checkup PDF"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share PDF")
                        }
                    }
                }
            )
        }
    ) { pad ->
        if (currentCheckup == null) {
            Column(Modifier.padding(pad).padding(16.dp)) {
                Text("Loading checkup detail...")
            }
        } else {
            Column(
                Modifier
                    .padding(pad)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            currentCheckup.bikeReg,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Date: ${SimpleDateFormat("dd MMM yyyy, HH:mm", LocalLocale.current.platformLocale).format(Date(currentCheckup.date))}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Odometer: ${currentCheckup.odometer.ifBlank { "N/A" }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Inspector: ${currentCheckup.checkedBy}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        val statusColor = when (currentCheckup.overallStatus) {
                            "PASS" -> Color(0xFF2E7D32)
                            "NEEDS_SERVICE" -> Color(0xFFE65100)
                            else -> Color.Red
                        }
                        Text(
                            "Status: ${currentCheckup.overallStatus}",
                            style = MaterialTheme.typography.titleMedium,
                            color = statusColor,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Checklist Items", style = MaterialTheme.typography.titleLarge)

                items.forEach { item ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                if (item.note.isNotBlank()) {
                                    Text(item.note, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                }
                            }
                            val itemColor = when (item.status) {
                                "OK" -> Color(0xFF2E7D32)
                                "NOTE" -> Color(0xFFE65100)
                                else -> Color.Red
                            }
                            Text(item.status, style = MaterialTheme.typography.titleMedium, color = itemColor)
                        }
                    }
                }

                if (currentCheckup.driverSignature.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Driver Signature", style = MaterialTheme.typography.titleLarge)
                    SignatureDisplay(
                        signatureBase64 = currentCheckup.driverSignature,
                        onClear = {},
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val generator = CheckupPdfGenerator(context)
                        val file = generator.generate(currentCheckup, items)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Checkup PDF"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export & View PDF Report")
                }
            }
        }
    }
}
