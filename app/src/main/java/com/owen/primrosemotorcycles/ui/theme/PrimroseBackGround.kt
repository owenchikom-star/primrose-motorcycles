package com.owen.primrosemotorcycles.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.owen.primrosemotorcycles.R

@Composable
fun PrimroseBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Background color first
        Box(
            Modifier.fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        // LOGO WATERMARK - centered, faded, behind everything
        Image(
            painter = painterResource(id = R.drawable.primrose_logo),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .size(420.dp)
                .alpha(0.08f) // 8% - very subtle, won't hurt readability
        )

        // Second smaller logo at bottom for tablet
        Image(
            painter = painterResource(id = R.drawable.primrose_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(160.dp)
                .alpha(0.05f)
                .padding(16.dp)
        )

        // Your actual app content on top
        Box(Modifier.fillMaxSize()) {
            content()
        }
    }
}