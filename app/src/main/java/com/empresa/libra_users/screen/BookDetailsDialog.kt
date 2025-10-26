package com.empresa.libra_users.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity

@Composable
fun BookDetailsDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onAddToCart: (BookEntity) -> Unit // Cambiado para reflejar la nueva acción
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF212121) // Fondo oscuro del diálogo
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Barra superior con botón de cerrar ---
                Box(
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Portada del libro ---
                    Card(
                        modifier = Modifier.height(250.dp).aspectRatio(0.7f),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(book.coverUrl).crossfade(true).build(),
                            contentDescription = book.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(24.dp))

                    // --- Título y autor ---
                    Text(book.title, style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
                    Text(book.author, style = MaterialTheme.typography.titleMedium, color = Color.LightGray, modifier = Modifier.clickable { /*TODO*/ })

                    Spacer(Modifier.height(24.dp))

                    // --- Botón principal ---
                    Button(
                        onClick = { 
                            onAddToCart(book) // Llama a la nueva función
                            onDismiss() // Cierra el diálogo después de añadir al carrito
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("OBTENER", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(32.dp))

                    // --- Descripción ---
                    Text("Descripción del editor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = book.isbn, // Usando ISBN como descripción por ahora
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.LightGray
                    )
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
