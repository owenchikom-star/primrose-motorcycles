package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.PdfLogoHelper
import com.owen.primrosemotorcycles.data.SparePart
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class RequisitionPdfGenerator(private val context: Context) {

    companion object {
        fun calculateTotal(parts: List<SparePart>): Double {
            return parts.sumOf { it.total }
        }
    }

    fun generate(job: JobCard, parts: List<SparePart>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val bold = TextPaint().apply { textSize = 11f; typeface = Typeface.DEFAULT_BOLD; color = Color.BLACK }
        val normal = TextPaint().apply { textSize = 10f; color = Color.BLACK }
        val linePaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 1f }

        PdfLogoHelper.drawLogo(canvas, context, left = 400f, top = 10f, width = 150f, height = 40f)

        var y = 30f
        canvas.drawText("WORKSHOP REQUISITION LIST", 180f, y, TextPaint().apply { textSize = 14f; typeface = Typeface.DEFAULT_BOLD; color = Color.BLACK })
        y += 20f
        canvas.drawText("Job: ${job.jobNumber} | Reg: ${job.bikeReg} | Customer: ${job.customerName}", 20f, y, normal)
        y += 20f
        canvas.drawLine(20f, y, 575f, y, linePaint)
        y += 10f

        // Table header
        canvas.drawText("NO", 20f, y, bold)
        canvas.drawText("SPARE NAME", 50f, y, bold)
        canvas.drawText("PART NUMBER", 250f, y, bold)
        canvas.drawText("QTY", 380f, y, bold)
        canvas.drawText("PRICE", 420f, y, bold)
        canvas.drawText("TOTAL", 500f, y, bold)
        y += 15f
        canvas.drawLine(20f, y, 575f, y, linePaint)
        y += 10f

        val grandTotal = calculateTotal(parts)
        parts.forEachIndexed { index, part ->
            canvas.drawText("${index + 1}", 20f, y, normal)
            canvas.drawText(part.name.take(25), 50f, y, normal)
            canvas.drawText(part.partNumber, 250f, y, normal)
            canvas.drawText("${part.quantity}", 380f, y, normal)
            canvas.drawText("R${part.unitPrice}", 420f, y, normal)
            canvas.drawText("R${part.total}", 500f, y, normal)
            y += 16f
        }
        y += 10f
        canvas.drawLine(20f, y, 575f, y, Paint().apply { color = Color.BLACK; strokeWidth = 1.5f })
        y += 20f
        val formattedGrandTotal = String.format(Locale.US, "%.2f", grandTotal)
        canvas.drawText("GRAND TOTAL: R $formattedGrandTotal", 400f, y, bold)

        document.finishPage(page)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Requisition_${job.jobNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}
