package com.owen.primrosemotorcycles.userInterface.stock


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owen.primrosemotorcycles.data.StockItem

@Composable
fun StockItemCard(
    item: StockItem,
    onClick: () -> Unit = {},
    onStockIn: () -> Unit,
    onStockOut: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUseInJob: () -> Unit
) {
    val isLow = item.isLowStock
    val isOut = item.isOutOfStock
    val profit = item.sellingPrice - item.costPrice
    val margin = if (item.costPrice > 0) (profit / item.costPrice * 100) else 0.0

    val borderColor = when {
        isOut -> Color.Red
        isLow -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFE0E0E0)
    }

    val qtyColor = when {
        isOut -> Color.White
        isLow -> Color.White
        else -> Color.Black
    }

    val qtyBg = when {
        isOut -> Color.Red
        isLow -> Color(0xFFFF9800)
        else -> Color(0xFFF5F5F5)
    }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isLow || isOut) 1.5.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(containerColor = if (isOut) Color(0xFFFFEBEE) else Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Top Row: Part No + Qty Badge
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Category chip
                        Box(
                            Modifier.clip(RoundedCornerShape(4.dp)).background(
                                when (item.category) {
                                    "OIL" -> Color(0xFF4CAF50)
                                    "FILTER" -> Color(0xFF2196F3)
                                    "BRAKES" -> Color(0xFFF44336)
                                    else -> Color.Gray
                                }
                            ).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(item.category, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            item.partNumber.ifBlank { "NO PN" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isLow) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        item.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.Black
                    )

                    if (item.brand.isNotBlank() || item.location.isNotBlank()) {
                        Text(
                            "${item.brand} • Bin: ${item.location} ${if (item.supplier.isNotBlank()) "• ${item.supplier}" else ""}",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Qty Circle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(qtyBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${item.quantityOnHand}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = qtyColor
                        )
                    }
                    if (isLow) {
                        Text(
                            if (isOut) "OUT!" else "LOW!",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text("Min: ${item.minQuantity}", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Pricing Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column {
                    Text("Cost: R${String.format("%.2f", item.costPrice)}", fontSize = 11.sp, color = Color.Gray)
                    Text("Sell: R${String.format("%.2f", item.sellingPrice)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Profit: R${String.format("%.2f", profit)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (profit > 0) Color(0xFF2E7D32) else Color.Red)
                    Text("${String.format("%.0f", margin)}% margin", fontSize = 10.sp, color = Color.Gray)
                }
            }

            if (isLow) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (item.quantityOnHand.toFloat() / (item.minQuantity * 2).coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = if (isOut) Color.Red else Color(0xFFFF9800),
                    trackColor = Color(0xFFEEEEEE)
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))

            // Action Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // -1 button
                OutlinedButton(
                    onClick = onStockOut,
                    enabled = !isOut,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Icon(Icons.Default.Remove, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("-1", color = Color.Red, fontSize = 12.sp)
                }

                // +1 button
                OutlinedButton(
                    onClick = onStockIn,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Add, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("+1", color = Color(0xFF2E7D32), fontSize = 12.sp)
                }

                // Use in Job
                Button(
                    onClick = onUseInJob,
                    enabled = !isOut,
                    modifier = Modifier.weight(1.2f).height(36.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Build, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Use", fontSize = 12.sp)
                }

                // More menu
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// Compact version for picking stock when creating Quotation / Job Card
@Composable
fun StockPickerCard(
    item: StockItem,
    onSelect: () -> Unit
) {
    val isOut = item.isOutOfStock
    ListItem(
        headlineContent = { Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${item.partNumber} • ${item.quantityOnHand} left • R${item.sellingPrice}", fontSize = 11.sp) },
        leadingContent = {
            Box(Modifier.size(36.dp).clip(CircleShape).background(if (isOut) Color.Red else Color(0xFFE8F5E9)), contentAlignment = Alignment.Center) {
                Text("${item.quantityOnHand}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isOut) Color.White else Color.Black)
            }
        },
        trailingContent = {
            Button(onClick = onSelect, enabled = !isOut, modifier = Modifier.height(32.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                Text(if (isOut) "Out" else "Add", fontSize = 11.sp)
            }
        },
        modifier = Modifier.clickable(enabled = !isOut) { onSelect() }
    )
    HorizontalDivider(color = Color(0xFFEEEEEE))
}