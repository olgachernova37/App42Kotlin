package com.olhadem.app42.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * modifier тут явно задає розмір 48dp - той самий "квадрат", в який
 * обгорнута іконка лупи в GreenTopBar. Так обидві іконки завжди
 * однакового розміру, незалежно від того, чи стандартний розмір
 * IconButton колись зміниться в новій версії Material3.
 */
@Composable
fun MoreOptionsButton(onClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Text(
            text = "⋮",
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
