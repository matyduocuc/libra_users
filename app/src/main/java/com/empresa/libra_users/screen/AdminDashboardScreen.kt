package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AdminDashboardScreen(modifier: Modifier = Modifier) { // Parámetro añadido
    Box(
        modifier = modifier.fillMaxSize(), // Modificador aplicado
        contentAlignment = Alignment.Center
    ) {
        Text("Panel de Administración")
    }
}
