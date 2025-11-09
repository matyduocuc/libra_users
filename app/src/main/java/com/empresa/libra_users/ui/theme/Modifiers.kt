package com.empresa.libra_users.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.empresa.libra_users.R

@Composable
fun Modifier.appBackground(darkTheme: Boolean): Modifier = this.paint(
    painter = painterResource(id = R.drawable.app_background),
    contentScale = ContentScale.Crop,
    colorFilter = if (darkTheme) {
        ColorFilter.tint(Color.Black.copy(alpha = 0.5f), blendMode = BlendMode.Darken)
    } else {
        null
    }
)
