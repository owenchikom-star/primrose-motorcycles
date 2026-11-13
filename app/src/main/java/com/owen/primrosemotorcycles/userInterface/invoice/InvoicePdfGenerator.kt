package com.owen.primrosemotorcycles.userInterface.invoice

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import androidx.core.graphics.toColorInt
import com.owen.primrosemotorcycles.data.Invoice
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.PdfLogoHelper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvoiceItemUiState(
    val invoiceId: String = "",
    val description: String = "",
    val quantity: Double = 1.0,
    val inclPrice: Double = 0.0,
    val sortOrder: Int = 0
) {
    val exclPrice: Double get() = inclPrice / 1.15
    val totalExcl: Double get() = exclPrice * quantity
}

class InvoicePdfGenerator(private val context: Context) {

    fun generate(invoice: Invoice, jobCard: JobCard, items: List<InvoiceItemUiState>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val black = Color.BLACK
        val grey = "#8A8A8A".toColorInt()
        val lightGrey = "#D9D9D9".toColorInt()

        val tpSmall = TextPaint().apply { color = black; textSize = 8f }
        val tpBoldSmall = TextPaint().apply { color = black; textSize = 8f; typeface = Typeface.DEFAULT_BOLD }
        val tpNormal = TextPaint().apply { color = black; textSize = 9f }
        val tpBold = TextPaint().apply { color = black; textSize = 9f; typeface = Typeface.DEFAULT_BOLD }
        val tpWhite = TextPaint().apply { color = Color.WHITE; textSize = 9f; typeface = Typeface.DEFAULT_BOLD }
        val tpTitle = TextPaint().apply { color = black; textSize = 16f; typeface = Typeface.DEFAULT_BOLD }

        var y = 30f
        // Title
        canvas.drawText("Tax Invoice", 20f, y, tpTitle)
        y += 5f
        // Logo - right
        PdfLogoHelper.drawLogo(canvas, context, left = 380f, top = 25f, width = 170f, height = 45f)
        y += 25f

        // Company Info Left
        val companyLines = listOf(
            "Primrose Motorcycle Centre cc" to true,
            "152 Rietfontein Road" to false,
            "Primrose" to false,
            "Germiston" to false,
            "1401 CK96/62919/23" to false,
            "Tel 011 828 9091" to false,
            "VAT No:  4540104223" to true
        )
        companyLines.forEach { (text, bold) ->
            canvas.drawText(text, 20f, y, if (bold) tpBoldSmall else tpSmall)
            y += 10f
        }
        y += 10f

        // Meta Right
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val meta = listOf(
            "Invoice No:" to invoice.number,
            "Date:" to df.format(Date(invoice.date)),
            "Page:" to invoice.page,
            "Reference:" to invoice.reference,
            "Sales Rep:" to invoice.salesRep,
            "Due Date:" to df.format(Date(invoice.dueDate)),
            "Discount:" to "${invoice.discountPercent}%"
        )
        var my = 95f
        meta.forEach { (k, v) ->
            canvas.drawText(k, 350f, my, tpBoldSmall)
            canvas.drawText(v, 520f - tpSmall.measureText(v), my, tpSmall)
            my += 12f
        }
        // Total Due grey bar
        my += 5f
        val totalDue = items.sumOf { it.inclPrice * it.quantity }
        canvas.drawRect(350f, my, 575f, my + 18f, Paint().apply { color = grey })
        canvas.drawText("Total Due:", 360f, my + 12f, tpWhite)
        canvas.drawText("R${String.format(Locale.US, "%.2f", totalDue)}", 510f, my + 12f, tpWhite)
        my += 25f

        y = maxOf(y, my)

        // W/SHOP block - Customer & Bike info from Job Card
        val workshopLines = mutableListOf<String>()
        workshopLines.add("W/SHOP : JOB CARD")
        val custName = jobCard.customerName.ifBlank { invoice.customerName }
        if (custName.isNotBlank()) workshopLines.add("Name: $custName")
        val custPhone = jobCard.customerPhone
        if (jobCard.customerPhone.isNotBlank()) workshopLines.add("Tel: $custPhone")
        val address = jobCard.customerAddress
        if (jobCard.customerAddress.isNotBlank()) workshopLines.add("Address: $address")
        val eng = jobCard.engNo.ifBlank { invoice.engineNo }
        if (eng.isNotBlank()) workshopLines.add("ENGINE NO: $eng")
        val vin = jobCard.vinNo.ifBlank { invoice.vinNo }
        if (vin.isNotBlank()) workshopLines.add("VIN NO: $vin")
        if (invoice.customerVatNo.isNotBlank()) workshopLines.add(" Customer VAT No: ${invoice.customerVatNo}")
        val vehicleStr = jobCard.bikeModel.ifBlank { " ${invoice.vehicle}" }
        if (vehicleStr.isNotBlank()) workshopLines.add("Vehicle: $vehicleStr")
        val regStr = jobCard.bikeReg.ifBlank { "Reg No: ${invoice.regNo}" }
        if (regStr.isNotBlank()) workshopLines.add("Reg No: $regStr")
        val kmStr = jobCard.mileage.ifBlank { invoice.km }
        if (kmStr.isNotBlank()) workshopLines.add("Mileage: $kmStr")
        val driverStr = jobCard.driver.ifBlank { invoice.driver }
        if (driverStr.isNotBlank()) workshopLines.add("Driver: $driverStr")
        workshopLines.forEach { line ->
            canvas.drawText(line, 20f, y, if (line.startsWith("W/SHOP")) tpBoldSmall else tpSmall)
            y += 11f
        }
        y += 10f

        // Table Header grey
        canvas.drawRect(20f, y, 575f, y + 16f, Paint().apply { color = grey })
        canvas.drawText("Description", 22f, y + 11f, tpWhite)
        canvas.drawText("Quantity", 300f, y + 11f, tpWhite)
        canvas.drawText("Incl. Price", 390f, y + 11f, tpWhite)
        canvas.drawText("Total", 510f, y + 11f, tpWhite)
        y += 30f

        fun drawRow(desc: String, qty: String, incl: String, total: String) {
            if (y > 680) return
            canvas.drawText(desc.take(75), 22f, y, tpSmall)
            canvas.drawText(qty, 320f, y, tpSmall)
            canvas.drawText(incl, 400f, y, tpSmall)
            canvas.drawText(total, 510f, y, tpSmall)
            y += 11f
        }

        if (jobCard.sparesList.isNotBlank()) {
            jobCard.sparesList.split("\n").forEach { spareLine ->
                if (spareLine.isNotBlank()) {
                    drawRow(spareLine, "", "", "")
                }
            }
        }

        val notesText = invoice.customerNotes
        if (notesText.isNotBlank()) {
            notesText.split("\n").forEach { noteLine ->
                if (noteLine.isNotBlank()) {
                    canvas.drawText(noteLine, 22f, y, TextPaint().apply { textSize = 7.5f; color = black })
                    y += 10f
                }
            }
            y += 4f
        }

        items.forEach { item ->
            drawRow(
                desc = item.description,
                qty = String.format(Locale.US, "%.2f", item.quantity),
                incl = "R${String.format(Locale.US, "%.2f", item.inclPrice)}",
                total = "R${String.format(Locale.US, "%.2f", item.totalExcl)}"
            )
        }

        y += 8f
        canvas.drawLine(20f, y, 575f, y, Paint().apply { color = lightGrey; strokeWidth = 1f })
        y += 12f

        val notesX = 20f
        canvas.drawText("Notes:", notesX, y, tpBoldSmall)
        y += 11f
        invoice.notes.split("\n").forEach { line ->
            canvas.drawText(line, notesX, y, tpSmall)
            y += 10f
        }

        var ty = y - 50f
        val exclTotal = items.sumOf { item -> item.totalExcl }
        val vat = exclTotal * 0.15
        val grand = exclTotal + vat

        fun drawTotal(label: String, value: String) {
            canvas.drawText(label, 350f, ty, tpBoldSmall)
            canvas.drawText(value, 510f, ty, tpSmall)
            ty += 12f
        }
        drawTotal("Total Discount:", "R0.00")
        drawTotal("Total Exclusive:", "R${String.format(Locale.US, "%.2f", exclTotal)}")
        drawTotal("Total VAT:", "R${String.format(Locale.US, "%.2f", vat)}")
        ty += 4f
        canvas.drawLine(350f, ty, 575f, ty, Paint().apply { strokeWidth = 0.5f })
        ty += 12f
        drawTotal("Sub Total:", "R${String.format(Locale.US, "%.2f", grand)}")
        drawTotal("Total Rounding:", "R0.00")
        ty += 15f
        canvas.drawRect(350f, ty, 575f, ty + 18f, Paint().apply { color = grey })
        canvas.drawText("Total Due:", 360f, ty + 12f, tpWhite)
        canvas.drawText("R${String.format(Locale.US, "%.2f", grand)}", 510f, ty + 12f, tpWhite)

        document.finishPage(page)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "${invoice.number}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
