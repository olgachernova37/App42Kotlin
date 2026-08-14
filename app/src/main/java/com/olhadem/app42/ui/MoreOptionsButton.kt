package com.olhadem.app42.ui

import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MoreOptionsButton(onClick: () -> Unit = {}) {
    IconButton(onClick = onClick) {
        Text(
            text = "⋮",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
