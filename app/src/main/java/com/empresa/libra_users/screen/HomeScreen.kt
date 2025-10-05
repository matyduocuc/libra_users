package com.empresa.libra_users.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.empresa.libra_users.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPrimaryAction: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca — Home") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del logo (asegúrate de tener un archivo en res/drawable, p. ej. logo.png)
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo Biblioteca",
                modifier = Modifier
                    .size(120.dp)
                    .padding(8.dp)
            )

            Text(
                text = "Bienvenido/a",
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = "Pantalla base hecha con Jetpack Compose.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Acción principal")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}
