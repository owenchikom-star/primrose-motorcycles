package com.owen.primrosemotorcycles.userInterface.checkup

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Base64
import com.owen.primrosemotorcycles.data.CheckupItem
import com.owen.primrosemotorcycles.data.PdfLogoHelper
import com.owen.primrosemotorcycles.data.WeeklyCheckup
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckupPdfGenerator(private val context: Context) {

    fun generate(checkup: WeeklyCheckup, items: List<CheckupItem>): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Paints
        val paintTitle = Paint().apply { color = Color.BLACK; textSize = 16f; typeface = Typeface.DEFAULT_BOLD }
        val paintHeaderBold = Paint().apply { color = Color.BLACK; textSize = 11f; typeface = Typeface.DEFAULT_BOLD }
        val paintText = Paint().apply { color = Color.BLACK; textSize = 10f; typeface = Typeface.DEFAULT }
        val paintSmall = Paint().apply { color = Color.DKGRAY; textSize = 9f; typeface = Typeface.DEFAULT }
        val borderPaint = Paint().apply { color = Color.LTGRAY; style = Paint.Style.STROKE; strokeWidth = 1f }
        val headerBgPaint = Paint().apply { color = Color.parseColor("#F2F2F2"); style = Paint.Style.FILL }

        val left = 30f
        val right = 565f
        val top = 30f
        var y = top

        // Logo
        PdfLogoHelper.drawLogo(canvas, context, left = left, top = y, width = 130f, height = 40f)

        // Title Right Aligned
        canvas.drawText("WEEKLY CHECKUP REPORT", right - 210f, y + 25f, paintTitle)
        y += 50f

        // Info Box
        canvas.drawRect(left, y, right, y + 65f, borderPaint)
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date(checkup.date))

        canvas.drawText("Bike Reg: ${checkup.bikeReg}", left + 10f, y + 20f, paintHeaderBold)
        canvas.drawText("Checkup ID: #${checkup.id}", left + 260f, y + 20f, paintText)
        canvas.drawText("Date: $dateStr", left + 380f, y + 20f, paintText)

        canvas.drawText("Odometer: ${checkup.odometer.ifBlank { "N/A" }}", left + 10f, y + 45f, paintText)
        canvas.drawText("Inspector: ${checkup.checkedBy}", left + 260f, y + 45f, paintText)

        val statusColor = when (checkup.overallStatus) {
            "PASS" -> Color.parseColor("#2E7D32")
            "NEEDS_SERVICE" -> Color.parseColor("#E65100")
            else -> Color.RED
        }
        val statusPaint = Paint().apply { color = statusColor; textSize = 11f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Overall: ${checkup.overallStatus}", left + 380f, y + 45f, statusPaint)

        y += 80f

        // Checklist Table Header
        canvas.drawRect(left, y, right, y + 24f, headerBgPaint)
        canvas.drawRect(left, y, right, y + 24f, borderPaint)
        canvas.drawText("Checklist Item", left + 10f, y + 16f, paintHeaderBold)
        canvas.drawText("Status", left + 300f, y + 16f, paintHeaderBold)
        canvas.drawText("Notes / Findings", left + 380f, y + 16f, paintHeaderBold)
        y += 24f

        // Table Rows
        items.forEach { item ->
            canvas.drawRect(left, y, right, y + 22f, borderPaint)

            val itemStatusColor = when (item.status) {
                "OK" -> Color.parseColor("#2E7D32")
                "NOTE" -> Color.parseColor("#E65100")
                else -> Color.RED
            }
            val itemStatusPaint = Paint().apply { color = itemStatusColor; textSize = 9.5f; typeface = Typeface.DEFAULT_BOLD }

            canvas.drawText(item.name.take(40), left + 10f, y + 15f, paintText)
            canvas.drawText(item.status, left + 300f, y + 15f, itemStatusPaint)
            canvas.drawText(item.note.take(25), left + 380f, y + 15f, paintSmall)
            y += 22f
        }

        y += 30f

        // Notes summary
        if (checkup.notes.isNotBlank()) {
            canvas.drawText("General Notes / Findings:", left, y, paintHeaderBold)
            y += 15f
            checkup.notes.lines().forEach { line ->
                canvas.drawText(line, left + 10f, y, paintText)
                y += 14f
            }
            y += 20f
        }

        // Driver Signature Section
        canvas.drawText("Driver / Inspector Signature:", left, y, paintHeaderBold)
        canvas.drawLine(left, y + 55f, left + 200f, y + 55f, borderPaint)

        if (checkup.driverSignature.isNotBlank()) {
            try {
                val bytes = Base64.decode(checkup.driverSignature, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    val destRect = android.graphics.RectF(left, y + 10f, left + 180f, y + 50f)
                    canvas.drawBitmap(bitmap, null, destRect, null)
                }
            } catch (_: Exception) {}
        }
        canvas.drawText("Signed By Driver", left, y + 68f, paintSmall)

        document.finishPage(page)

        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.cacheDir
        val file = File(outputDir, "Checkup_Report_${checkup.bikeReg.replace(" ", "_")}_${checkup.id}.pdf")
        val outputStream = FileOutputStream(file)
        document.writeTo(outputStream)
        document.close()
        outputStream.close()

        return file
    }
}
