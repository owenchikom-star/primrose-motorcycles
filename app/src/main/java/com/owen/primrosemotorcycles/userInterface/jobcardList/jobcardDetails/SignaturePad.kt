package com.owen.primrosemotorcycles.userInterface.jobcardList.jobcardDetails

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (String) -> Unit,
    onClear: () -> Unit = {}
) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val p = Path().apply { moveTo(offset.x, offset.y) }
                                currentPath = p
                                paths.add(p)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentPath?.let { p ->
                                    p.lineTo(change.position.x, change.position.y)
                                    if (paths.isNotEmpty()) {
                                        paths[paths.lastIndex] = Path().apply { addPath(p) }
                                    }
                                }
                            }
                        )
                    }
            ) {
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color.Black,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = {
                paths.clear()
                currentPath = null
                onClear()
            }) {
                Text("Clear Signature")
            }

            Button(
                onClick = {
                    if (paths.isNotEmpty()) {
                        val bitmap = Bitmap.createBitmap(500, 200, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            style = android.graphics.Paint.Style.STROKE
                            strokeWidth = 5f
                            isAntiAlias = true
                            strokeCap = android.graphics.Paint.Cap.ROUND
                            strokeJoin = android.graphics.Paint.Join.ROUND
                        }
                        paths.forEach { composePath ->
                            canvas.drawPath(composePath.asAndroidPath(), paint)
                        }
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                        val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                        onSignatureCaptured(base64)
                    }
                },
                enabled = paths.isNotEmpty()
            ) {
                Text("Save Signature")
            }
        }
    }
}

@Composable
fun SignatureDisplay(
    signatureBase64: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(signatureBase64) {
        try {
            if (signatureBase64.isNotBlank()) {
                val bytes = Base64.decode(signatureBase64, Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (bitmap != null) {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Customer Signature",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            TextButton(onClick = onClear) {
                Text("Clear & Re-sign")
            }
        }
    }
}