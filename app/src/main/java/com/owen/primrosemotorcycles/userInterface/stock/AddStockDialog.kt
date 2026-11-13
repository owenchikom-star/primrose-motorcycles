package com.owen.primrosemotorcycles.userInterface.stock


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockDialog(
    onDismiss: () -> Unit,
    onSave: (partNumber: String, name: String, qty: Int, minQty: Int, cost: Double, sell: Double, category: String, location: String, supplier: String, brand: String, barcode: String) -> Unit
) {
    var partNumber by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("OIL") }
    var brand by rememberSaveable { mutableStateOf("MOTUL") }
    var qty by rememberSaveable { mutableStateOf("1") }
    var minQty by rememberSaveable { mutableStateOf("2") }
    var location by rememberSaveable { mutableStateOf("A1") }
    var costPrice by rememberSaveable { mutableStateOf("") }
    var sellingPrice by rememberSaveable { mutableStateOf("") }
    var supplier by rememberSaveable { mutableStateOf("") }
    var barcode by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf("OIL", "FILTER", "BRAKES", "ENGINE", "ELECTRICAL", "BODY", "TYRE", "CHAIN", "GENERAL", "CONSUMABLES")
    val brands = listOf("MOTUL", "YAMAHA", "SUZUKI", "HONDA", "NGK", "DID", "Brembo", "Other")

    val profit = remember(costPrice, sellingPrice) {
        val c = costPrice.toDoubleOrNull() ?: 0.0
        val s = sellingPrice.toDoubleOrNull() ?: 0.0
        if (c > 0) s - c else 0.0
    }

    val isValid = name.isNotBlank() && qty.toIntOrNull() != null && sellingPrice.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Spare Part", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Part Number with auto-generate
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = partNumber,
                        onValueChange = { partNumber = it.uppercase() },
                        label = { Text("Part No e.g. 3374650244068") },
                        placeholder = { Text("S1651033G10000") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    TextButton(onClick = {
                        // Auto-generate like your system: S + random
                        partNumber = "S${(1000000000..9999999999).random()}"
                    }) { Text("GEN") }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.uppercase() },
                    label = { Text("Name * e.g. MOTUL MOTO 20W50 1LT") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isBlank()
                )

                // Category Dropdown
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expanded = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it.uppercase() },
                        label = { Text("Bin A1") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it.filter { c -> c.isDigit() } },
                        label = { Text("Qty On Hand *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minQty,
                        onValueChange = { minQty = it.filter { c -> c.isDigit() } },
                        label = { Text("Min Qty (Low Alert)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costPrice,
                        onValueChange = { costPrice = it },
                        label = { Text("Cost R") },
                        placeholder = { Text("215.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellingPrice,
                        onValueChange = { sellingPrice = it },
                        label = { Text("Selling R *") },
                        placeholder = { Text("350.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                if (profit != 0.0) {
                    Text(
                        "Profit: R${String.format("%.2f", profit)} (${if ((costPrice.toDoubleOrNull() ?: 0.0) > 0) String.format("%.0f", profit / (costPrice.toDoubleOrNull() ?: 1.0) * 100) else 0}%)",
                        color = if (profit > 0) Color(0xFF2E7D32) else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier") },
                    placeholder = { Text("Yamaha SA / Motul Distributor") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode (optional)") },
                    placeholder = { Text("Scan or type") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = { Text("📷", modifier = Modifier) }
                )

                Text(
                    "This will create opening transaction IN ${qty.ifBlank { "0" }} units",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        partNumber.ifBlank { "GEN-${System.currentTimeMillis().toString().takeLast(6)}" },
                        name,
                        qty.toIntOrNull() ?: 0,
                        minQty.toIntOrNull() ?: 2,
                        costPrice.toDoubleOrNull() ?: 0.0,
                        sellingPrice.toDoubleOrNull() ?: 0.0,
                        category,
                        location,
                        supplier,
                        brand,
                        barcode
                    )
                },
                enabled = isValid
            ) { Text("Save Stock") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// Quick Add version for when you are on Job Card screen - faster
@Composable
fun QuickAddStockDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (name: String, sellPrice: Double) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var price by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add Part") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Part Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Selling Price R") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, price.toDoubleOrNull() ?: 0.0) }, enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}