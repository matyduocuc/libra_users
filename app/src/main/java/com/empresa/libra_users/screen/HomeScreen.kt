package com.empresa.libra_users.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow // <-- ¡IMPORTACIÓN CORREGIDA AQUÍ!
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.viewmodel.MainViewModel
import com.empresa.libra_users.viewmodel.HomeUiState
import com.empresa.libra_users.data.local.user.BookEntity
// import com.empresa.libra_users.R.drawable.book_placeholder // Requerido si usas recursos locales

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit
) {
    val homeState by vm.home.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // Espacio para la barra inferior
    ) {
        // 1. Mostrar estado de carga y errores
        item {
            if (homeState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            homeState.errorMsg?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        }

        // 2. Iterar sobre las categorías (similar a la imagen del catálogo)
        homeState.categorizedBooks.forEach { (categoryTitle, books) ->
            item {
                BookCategorySection(
                    title = categoryTitle,
                    books = books,
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        // 3. Sección de Autenticación (se mantiene al final)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(onClick = onGoLogin) { Text("Ir a Login") }
                OutlinedButton(onClick = onGoRegister) { Text("Ir a Registro") }
            }
        }
    }
}

// ----------------------------------------------------
// COMPONENTES DE SOPORTE DEL CATÁLOGO
// ----------------------------------------------------

@Composable
fun BookCategorySection(
    title: String,
    books: List<BookEntity>,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Botón "Ver más"
            IconButton(onClick = { /* Navegar a la vista de "Ver todo" de esta categoría */ }) {
                Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Ver más")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Carrusel Horizontal de Libros (LazyRow)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books) { book ->
                BookCoverItem(book = book)
            }
        }
    }
}

@Composable
fun BookCoverItem(book: BookEntity) {
    // Diseño del ítem de la portada (simulando la imagen del catálogo)
    Column(
        modifier = Modifier.width(110.dp), // Ancho fijo para las portadas
        horizontalAlignment = Alignment.Start
    ) {
        // Área de la Portada
        Box(
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.5f)), // Color de fondo gris como placeholder
            contentAlignment = Alignment.BottomEnd
        ) {
            // Aquí iría tu componente de carga de imagen (Coil, Glide, etc.) usando book.coverUrl
            /*
            AsyncImage(
                model = book.coverUrl,
                contentDescription = book.title,
                modifier = Modifier.fillMaxSize()
            )
            */

            // Icono de estantería (placeholder)
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "Detalles",
                tint = Color.White,
                modifier = Modifier
                    .padding(4.dp)
                    .size(20.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        // Título del libro
        Text(
            book.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis // <-- Referencia simplificada, usa la importación
        )
    }
}