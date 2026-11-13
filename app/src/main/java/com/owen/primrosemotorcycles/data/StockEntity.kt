package com.owen.primrosemotorcycles.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val partNumber: String = "", // e.g. S1651033G10000 - FILTER OIL
    val name: String, // e.g. FILTER OIL
    val description: String = "",
    val category: String = "GENERAL", // ENGINE, BRAKES, OIL, ELECTRICAL
    val brand: String = "", // MOTUL, YAMAHA, SUZUKI
    val quantityOnHand: Int = 0,
    val minQuantity: Int = 2, // low stock alert
    val location: String = "A1", // bin shelf
    val costPrice: Double = 0.0, // what you pay
    val sellingPrice: Double = 0.0, // incl VAT - what customer pays
    val supplier: String = "",
    val barcode: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = quantityOnHand <= minQuantity
    val isOutOfStock: Boolean get() = quantityOnHand <= 0
}

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val stockItemId: String,
    val type: String, // IN, OUT, ADJUST, SALE (from job card)
    val quantityChange: Int, // +10 or -1
    val quantityAfter: Int,
    val reason: String, // "Purchase", "Used on Job Card JC-123", "Stock Count"
    val jobCardId: String? = null,
    val quotationId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val user: String = "MARK"
)

@Entity(tableName = "stock_purchase_orders")
data class PurchaseOrder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val orderNumber: String = "PO${System.currentTimeMillis().toString().takeLast(5)}",
    val supplier: String,
    val status: String = "PENDING", // PENDING, RECEIVED
    val totalCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)