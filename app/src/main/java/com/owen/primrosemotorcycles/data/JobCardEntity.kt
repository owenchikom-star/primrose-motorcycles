package com.owen.primrosemotorcycles.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

enum class JobStatus { OPEN, IN_PROGRESS, WAITING_PARTS, DONE, COLLECTED }

@Entity(tableName = "job_cards")
data class JobCard(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),

    @get:PropertyName("jobNumber") @set:PropertyName("jobNumber")
    var jobNumber: String = "JC-${System.currentTimeMillis() % 100000}",

    @get:PropertyName("branchId") @set:PropertyName("branchId")
    var branchId: String = "germiston_main",

    // Header - from your paper image
    @get:PropertyName("orderNo") @set:PropertyName("orderNo") var orderNo: String = "",
    @get:PropertyName("paymentType") @set:PropertyName("paymentType") var paymentType: String = "CASH",
    @get:PropertyName("customerName") @set:PropertyName("customerName") var customerName: String = "",
    @get:PropertyName("customerAddress") @set:PropertyName("customerAddress") var customerAddress: String = "",
    @get:PropertyName("customerPhone") @set:PropertyName("customerPhone") var customerPhone: String = "",
    @get:PropertyName("bikeModel") @set:PropertyName("bikeModel") var bikeModel: String = "",
    @get:PropertyName("bikeColor") @set:PropertyName("bikeColor") var bikeColor: String = "",
    @get:PropertyName("bikeReg") @set:PropertyName("bikeReg") var bikeReg: String = "",
    @get:PropertyName("mileage") @set:PropertyName("odometer") var mileage: String = "",
    @get:PropertyName("vinNo") @set:PropertyName("vinNo") var vinNo: String = "",
    @get:PropertyName("engNo") @set:PropertyName("engNo") var engNo: String = "",

    // Instructions
    @get:PropertyName("customerInstructions") @set:PropertyName("customerInstructions") var customerInstructions: String = "",
    @get:PropertyName("workshopNotes") @set:PropertyName("workshopNotes") var workshopNotes: String = "",
    @get:PropertyName("workshopRepairs") @set:PropertyName("workshopRepairs") var workshopRepairs: String = "",
    @get:PropertyName("sparesList") @set:PropertyName("sparesList") var sparesList: String = "",
    @get:PropertyName("finalTotal") @set:PropertyName("finalTotal") var finalTotal: String = "",

    // CHECKLIST - Left column is qty (litres/ml), Right columns are R and C
    @get:PropertyName("engineOilQty") @set:PropertyName("engineOilQty") var engineOilQty: String = "",
    @get:PropertyName("engineOilR") @set:PropertyName("engineOilR") var engineOilR: String = "",
    @get:PropertyName("engineOilC") @set:PropertyName("engineOilC") var engineOilC: String = "",

    @get:PropertyName("gearBoxQty") @set:PropertyName("gearBoxQty") var gearBoxQty: String = "",
    @get:PropertyName("gearBoxR") @set:PropertyName("gearBoxR") var gearBoxR: String = "",
    @get:PropertyName("gearBoxC") @set:PropertyName("gearBoxC") var gearBoxC: String = "",

    @get:PropertyName("brakeFluidQty") @set:PropertyName("brakeFluidQty") var brakeFluidQty: String = "",
    @get:PropertyName("brakeFluidR") @set:PropertyName("brakeFluidR") var brakeFluidR: String = "",
    @get:PropertyName("brakeFluidC") @set:PropertyName("brakeFluidC") var brakeFluidC: String = "",

    @get:PropertyName("airFilterR") @set:PropertyName("airFilterR") var airFilterR: String = "",
    @get:PropertyName("airFilterC") @set:PropertyName("airFilterC") var airFilterC: String = "",

    @get:PropertyName("oilFilterR") @set:PropertyName("oilFilterR") var oilFilterR: String = "",
    @get:PropertyName("oilFilterC") @set:PropertyName("oilFilterC") var oilFilterC: String = "",

    @get:PropertyName("engineCleanedR") @set:PropertyName("engineCleanedR") var engineCleanedR: String = "",
    @get:PropertyName("engineCleanedC") @set:PropertyName("engineCleanedC") var engineCleanedC: String = "",

    @get:PropertyName("washedR") @set:PropertyName("washedR") var washedR: String = "",
    @get:PropertyName("washedC") @set:PropertyName("washedC") var washedC: String = "",

    @get:PropertyName("serviceFeeR") @set:PropertyName("serviceFeeR") var serviceFeeR: String = "",
    @get:PropertyName("serviceFeeC") @set:PropertyName("serviceFeeC") var serviceFeeC: String = "",

    @get:PropertyName("driver") @set:PropertyName("driver") var driver: String = "",
    @get:PropertyName("driverPhone") @set:PropertyName("driverPhone") var driverPhone: String = "",

    @get:PropertyName("sparesR") @set:PropertyName("sparesR") var sparesR: String = "",
    @get:PropertyName("sparesC") @set:PropertyName("sparesC") var sparesC: String = "",

    @get:PropertyName("finalTotalR") @set:PropertyName("finalTotalR") var finalTotalR: String = "",
    @get:PropertyName("finalTotalC") @set:PropertyName("finalTotalC") var finalTotalC: String = "",

    @get:PropertyName("nextServiceKm") @set:PropertyName("nextServiceKm") var nextServiceKms: String = "",

    // Status & Flags
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "OPEN",
    @get:PropertyName("reportedIssue") @set:PropertyName("reportedIssue") var reportedIssue: String = "",
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("checklistDone") @set:PropertyName("checklistDone") var checklistDone: Boolean = false,
    @get:PropertyName("quotationDone") @set:PropertyName("quotationDone") var quotationDone: Boolean = false,
    @get:PropertyName("customerSignature") @set:PropertyName("customerSignature") var customerSignature: String = "",
    @get:PropertyName("technician") @set:PropertyName("technician") var technician: String = "",

    @Exclude @get:Exclude @set:Exclude var isSynced: Boolean = false
) {
    constructor() : this(id = "")
    fun getStatusEnum(): JobStatus = try { JobStatus.valueOf(status) } catch (e: Exception) { JobStatus.OPEN }
}

@Entity
data class BikeChecklist(
    @PrimaryKey val jobCardId: String,
    val branchId: String,
    val engineOil: CheckState,
    val brakes: CheckState,
    val chain: CheckState,
    val cables: CheckState,
    val lights: CheckState,
    val suspension: CheckState,
    val tires: CheckState,
    val horn: CheckState,
    val brakesFluid: CheckState,
    val coolant: CheckState,
    val sideStand: CheckState,
    val mainStand: CheckState,
    val mirrors: CheckState,
    val levers: CheckState,
    val footLest: CheckState,
    val brackets: CheckState,
    val panels: CheckState,
    val handlebars: CheckState,
    val seat: CheckState,
    val box: CheckState,
    val notes: String,
    val checkedBy: String
)
enum class CheckState { OK, NEEDS_ATTENTION, REPLACE, NA }

@Entity(tableName = "quotations")
data class Quotation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val number: String = "QUO${(10000..99999).random()}", // QUO0013961
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val page: String = "1/1",
    val reference: String = "", // 460500 S
    val salesRep: String = "",
    val discountPercent: Double = 0.0,
    val vatRate: Double = 0.15, // 15%

    // From Job Card
    val jobCardId: String,
    val stockNo: String = "",
    val vehicle: String = "", // SUZUKI GIXXER 150 BLUE
    val regNo: String = "",
    val km: String = "",
    val driver: String = "",
    val customerNotes: String = "",
    val customerName: String = "",
    val engineNo: String = "",
    val vinNo: String = "",
    val customerVatNo: String = "",

    // Totals - calculated
    val notes: String = "Banking details:\nFirst National Bank (FNB)\nAcc No: 51112342088\nBranch No: 252942 - Primrose\nAcc Name: Primrose Motorcycle Centre\nAcc Type: Cheque Account\nNO CASH DEPOSIT"
)

@Entity(tableName = "quotation_items")
data class QuotationItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val quotationId: String,
    val description: String, // e.g. "3374650244068 - MOTUL MOTO 20W50 MINERAL 1LT" or "SI. - REAR SHOCK"
    val quantity: Double = 1.0,
    val inclPrice: Double = 0.0, // with VAT
    val sortOrder: Int = 0
) {
    val exclPrice: Double get() = inclPrice / 1.15
    val totalExcl: Double get() = exclPrice * quantity
}

// Shop Config - one document, reusable
data class ShopConfig(
    val branchId: String = "germiston_main",
    val shopName: String = "Germiston Moto Dealers",
    val address: String = "",
    val phone: String = "",
    val logoUrl: String = ""
)

@Entity(tableName = "spare_parts")
data class SparePart(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val jobCardId: String, // FK to JobCard
    val name: String,
    val partNumber: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val total: Double get() = quantity * unitPrice
}

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val number: String = "INV${(10000..99999).random()}",
    val date: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val page: String = "1/1",
    val reference: String = "",
    val salesRep: String = "",
    val discountPercent: Double = 0.0,
    val vatRate: Double = 0.15,

    val jobCardId: String,
    val stockNo: String = "",
    val vehicle: String = "",
    val regNo: String = "",
    val km: String = "",
    val driver: String = "",
    val customerNotes: String = "",
    val customerName: String = "",
    val engineNo: String = "",
    val vinNo: String = "",
    val customerVatNo: String = "",
    val notes: String = "Banking details:\nFirst National Bank (FNB)\nAcc No: 51112342088\nBranch No: 252942 - Primrose\nAcc Name: Primrose Motorcycle Centre\nAcc Type: Cheque Account\nNO CASH DEPOSIT"
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val description: String,
    val quantity: Double = 1.0,
    val inclPrice: Double = 0.0,
    val sortOrder: Int = 0
) {
    val exclPrice: Double get() = inclPrice / 1.15
    val totalExcl: Double get() = exclPrice * quantity
}