package com.owen.primrosemotorcycles.data

import android.content.Context
import android.graphics.*
import com.owen.primrosemotorcycles.R

object PdfLogoHelper {
    fun drawLogo(canvas: Canvas, context: Context, left: Float = 400f, top: Float = 10f, width: Float = 150f, height: Float = 50f) {
        try {
            // Load from drawable
            val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.primrose_logo)
            val src = Rect(0, 0, bitmap.width, bitmap.height)
            val dst = RectF(left, top, left + width, top + height)
            canvas.drawBitmap(bitmap, src, dst, Paint().apply { isFilterBitmap = true })
        } catch (e: Exception) {
            // If logo not found, just write text so PDF doesn't crash
            canvas.drawText("Primrose motorcycles", left, top + 20f, Paint().apply { color = Color.BLACK; textSize = 14f; typeface = Typeface.DEFAULT_BOLD })
        }
    }
}