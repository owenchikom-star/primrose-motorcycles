package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.owen.primrosemotorcycles.data.JobCard
import com.owen.primrosemotorcycles.data.PdfLogoHelper
import com.owen.primrosemotorcycles.data.SparePart
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class JobCardPdfGenerator(private val context: Context) {

    fun generate(job: JobCard, sparesTotal: Double? = null, parts: List<SparePart> = emptyList()): File {
        val document = PdfDocument()
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        PdfLogoHelper.drawLogo(canvas, context, left = 200f, top = 10f, width = 150f, height = 40f)

        val black = Color.BLACK
        val border = Paint().apply { color = black; style = Paint.Style.STROKE; strokeWidth = 1.2f }
        val line = Paint().apply { color = black; style = Paint.Style.STROKE; strokeWidth = 0.8f }

        val tpTitle = TextPaint().apply { color = black; textSize = 15f; typeface = Typeface.DEFAULT_BOLD }
        val tpBold10 = TextPaint().apply { color = black; textSize = 10f; typeface = Typeface.DEFAULT_BOLD }
        val tpNormal11 = TextPaint().apply { color = black; textSize = 10f; typeface = Typeface.DEFAULT }
        val tpSmall9 = TextPaint().apply { color = black; textSize = 8.5f; typeface = Typeface.DEFAULT }
        val tpTiny7 = TextPaint().apply { color = black; textSize = 6.5f; typeface = Typeface.DEFAULT }
        val tpTiny7Bold = TextPaint().apply { color = black; textSize = 7f; typeface = Typeface.DEFAULT_BOLD }

        val left = 20f
        val right = 575f
        val contentW = right - left
        val pageBottom = 800f

        var y = 60f

        fun newPage() {
            document.finishPage(page)
            pageNum++
            pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNum).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = 20f
        }

        fun drawParagraph(text: String, x: Float, yStart: Float, width: Float, paint: TextPaint): Float {
            if (text.isBlank()) return yStart
            val layout =
                StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.15f)
                    .setIncludePad(false)
                    .build()
            canvas.save()
            canvas.translate(x, yStart)
            layout.draw(canvas)
            canvas.restore()
            return yStart + layout.height
        }

        fun drawBlockWithPagination(title: String, text: String, paint: TextPaint, minLines: Int = 3): Float {
            if (y > pageBottom - 30) newPage()
            canvas.drawText(title, left, y, tpBold10)
            y += 12f

            val rowHeight = 16f

            if (text.isBlank()) {
                for (i in 0 until minLines) {
                    if (y + rowHeight > pageBottom - 30) newPage()
                    canvas.drawLine(left, y + 13f, right, y + 13f, line)
                    y += rowHeight
                }
                y += 6f
                return y
            }

            val layout =
                StaticLayout.Builder.obtain(text, 0, text.length, paint, contentW.toInt())
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1.15f)
                    .setIncludePad(false)
                    .build()

            var printedLines = 0
            for (lineIdx in 0 until layout.lineCount) {
                val lineTop = layout.getLineTop(lineIdx).toFloat()
                val lineBottom = layout.getLineBottom(lineIdx).toFloat()
                val lineHeight = lineBottom - lineTop

                if (y + maxOf(lineHeight, rowHeight) > pageBottom - 30) {
                    newPage()
                    canvas.drawText("$title (continued)", left, y, tpBold10)
                    y += 14f
                }

                val start = layout.getLineStart(lineIdx)
                val end = layout.getLineEnd(lineIdx)
                val lineText = text.substring(start, end).trimEnd('\n')

                canvas.drawText(lineText, left + 2, y + 10f, paint)
                canvas.drawLine(left, y + 13f, right, y + 13f, line)
                y += maxOf(lineHeight, rowHeight)
                printedLines++
            }

            val extraLinesNeeded = maxOf(1, minLines - printedLines)
            for (i in 0 until extraLinesNeeded) {
                if (y + rowHeight > pageBottom - 30) break
                canvas.drawLine(left, y + 13f, right, y + 13f, line)
                y += rowHeight
            }

            y += 6f
            return y
        }

        y += 40f
        // HEADER
        canvas.drawText("NUMBER", left, y, tpBold10)
        canvas.drawLine(left + 48f, y + 2, left + 140f, y + 2, line)
        canvas.drawText(job.jobNumber, left + 50f, y, tpNormal11)

        canvas.drawText("JOB CARD", 240f, y, tpTitle)

        // Date including time
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(job.createdAt))
        canvas.drawText("DATE", 370f, y, tpBold10)
        canvas.drawLine(405f, y + 2, right, y + 2, line)
        canvas.drawText(dateStr, 408f, y, tpSmall9)
        y += 60f

        // PRIMROSE MOTORCYCLES LOGO HEADER
        val logoPaint = TextPaint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC)
            color = black
        }

        // RIGHT BOXES
        var rY = y - 14f
        val boxHeight = 15f
        fun box(label: String, value: String) {
            canvas.drawRect(340f, rY, right, rY + boxHeight, border)
            canvas.drawText(label, 342f, rY + 11f, tpTiny7Bold)
            canvas.drawText(value.take(30), 415f, rY + 11f, tpSmall9)
            rY += boxHeight
        }
        box("TIME WANTED", "")
        box("MAKE + MODEL", job.bikeModel)
        box("COLOR", job.bikeColor)
        box("REG NO.", job.bikeReg)
        box("ODOMETER", if (job.mileage.isNotBlank()) "${job.mileage} kms" else "")
        box("VIN NO.", job.vinNo)
        box("ENG NO.", job.engNo)

        // LEFT CUSTOMER DETAILS
        canvas.drawText("NAME", left, y, tpBold10)
        canvas.drawLine(left + 34f, y + 2, 320f, y + 2, line)
        canvas.drawText(job.customerName, left + 36f, y, tpNormal11)
        y += 16f

        canvas.drawText("ADDRESS", left, y, tpBold10)
        canvas.drawLine(left + 52f, y + 2, 320f, y + 2, line)
        canvas.drawText(job.customerAddress, left + 54f, y, tpSmall9)
        y += 16f

        canvas.drawLine(left + 52f, y + 2, 320f, y + 2, line)
        y += 16f

        canvas.drawText("TEL", left, y, tpBold10)
        canvas.drawLine(left + 28f, y + 2, 320f, y + 2, line)
        canvas.drawText(job.customerPhone, left + 30f, y, tpNormal11)
        y += 18f

        // TECHNICIAN UNDERLINED
        canvas.drawText("TECHNICIAN", left, y, tpBold10)
        canvas.drawLine(left + 70f, y + 2, 320f, y + 2, line)
        canvas.drawText(job.technician, left + 72f, y, tpNormal11)
        y += 18f

        y = maxOf(y, rY + 6f)

        // DRIVER NAME ON TOP OF CUSTOMER INSTRUCTIONS
        if (job.driver.isNotBlank() || job.driverPhone.isNotBlank()) {
            canvas.drawText("DRIVER", left, y, tpBold10)
            canvas.drawLine(left + 44f, y + 2, 320f, y + 2, line)
            val driverInfo = if (job.driverPhone.isNotBlank()) "${job.driver} (${job.driverPhone})" else job.driver
            canvas.drawText(driverInfo, left + 46f, y, tpNormal11)
            y += 18f
        }

        // CUSTOMER INSTRUCTIONS
        canvas.drawText("CUSTOMER'S INSTRUCTIONS", left, y, tpBold10)
        y += 6f
        val instrText = if (job.customerInstructions.isNotBlank()) job.customerInstructions else job.reportedIssue
        for (i in 0..2) {
            canvas.drawLine(left, y + i * 16f, 320f, y + i * 16f, line)
        }
        val instrEnd = drawParagraph(instrText, left, y, 300f, tpNormal11)
        y = maxOf(instrEnd + 10f, y + 50f)

        // WORKSHOP NOTES
        y = drawBlockWithPagination("WORKSHOP NOTES", job.workshopNotes, tpNormal11, minLines = 4)

        // WORKSHOP REPAIRS
        if (y > pageBottom - 50) newPage()
        y += 6f
        y = drawBlockWithPagination("WORKSHOP REPAIRS", job.workshopRepairs, tpNormal11, minLines = 12)

        // SPARES & MATERIALS
        y = drawBlockWithPagination("SPARES & MATERIALS", job.sparesList, tpNormal11, minLines = 5)

        // FOOTER
        if (y > pageBottom - 60) newPage()
        canvas.drawText("FINAL TOTAL", 340f, y + 8f, tpBold10)

        val calculatedTotal = sparesTotal ?: if (parts.isNotEmpty()) {
            RequisitionPdfGenerator.calculateTotal(parts)
        } else {
            val r = job.finalTotalR.toDoubleOrNull() ?: job.sparesR.toDoubleOrNull() ?: 0.0
            val c = job.finalTotalC.toDoubleOrNull() ?: job.sparesC.toDoubleOrNull() ?: 0.0
            r + (c / 100.0)
        }

        val finalTotalText = if (calculatedTotal > 0.0) {
            String.format(Locale.US, "R %.2f", calculatedTotal)
        } else if (job.finalTotal.isNotBlank()) {
            job.finalTotal
        } else {
            "R 0.00"
        }

        canvas.drawText(finalTotalText, 440f, y + 8f, tpBold10)
        canvas.drawLine(340f, y + 12f, right, y + 12f, line)

        y += 22f
        if (y > pageBottom - 60) newPage()
        val footerStartY = y

        // LEFT SIDE: CONDITIONS OF CONTRACT
        canvas.drawText("CONDITIONS OF CONTRACT", left, footerStartY, tpBold10)
        val contractText1 = "All vehicles and contents left in our custody are parked, driven and stored entirely at owners risk. We in no way accept liability or responsibility for any loss or damage of whatsoever nature and howsoever caused, including negligence or recklessness of our servants or agents."
        val contractText2 = "I accept the above conditions and authorise you to do the work listed above at my expense and to replace and supply parts and materials including oils and petrol which may be necessary to complete the work."

        val contractWidth = 300f
        var leftFooterY = drawParagraph(contractText1, left, footerStartY + 10f, contractWidth, tpTiny7)
        leftFooterY = drawParagraph(contractText2, left, leftFooterY + 4f, contractWidth, tpTiny7)

        // RIGHT SIDE: NEXT SERVICE & CUSTOMER SIGNATURE
        var rightFooterY = footerStartY + 8f
        canvas.drawText("Your next lubrication service at:", 340f, rightFooterY, tpSmall9)
        canvas.drawText("${job.nextServiceKms.ifBlank { "_____" }} kms", 340f, rightFooterY + 12f, tpBold10)

        rightFooterY += 38f
        if (job.customerSignature.isNotBlank()) {
            try {
                val bytes = android.util.Base64.decode(job.customerSignature, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, null, RectF(340f, rightFooterY - 26f, 520f, rightFooterY + 8f), null)
                }
            } catch (_: Exception) {}
        }

        canvas.drawLine(340f, rightFooterY + 10f, right, rightFooterY + 10f, line)
        canvas.drawText("Customer Signature", 340f, rightFooterY + 20f, tpSmall9)

        document.finishPage(page)
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "JobCard_${job.jobNumber}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}