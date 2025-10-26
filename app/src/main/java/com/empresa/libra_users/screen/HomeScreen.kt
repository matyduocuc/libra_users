package com.empresa.libra_users.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onLogout: () -> Unit // Manteniéndolo por si se usa en el drawer
) {
    val homeState by vm.home.collectAsStateWithLifecycle()
    val allBooks = homeState.categorizedBooks.values.flatten().distinctBy { it.id }
    val purpleColor = Color(0xFF6650a4) // Color morado principal

    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }

    // -- Lógica para mostrar el diálogo de detalles --
    selectedBook?.let { book ->
        BookDetailsDialog(
            book = book,
            onDismiss = { selectedBook = null },
            onAddToCart = { bookEntity ->
                vm.addToCart(bookEntity)
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) { // 1. Fondo del tema por defecto
        LazyColumn(
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { FreeBooksSection(books = homeState.categorizedBooks["Ciencia ficción y fantasía"] ?: emptyList(), onBookClick = { book -> selectedBook = book }, purpleColor = purpleColor) }
            item { TrendingBooksSection(books = allBooks.filterNot { it.categoryId == 2L }, onBookClick = { book -> selectedBook = book }, purpleColor = purpleColor) }
        }
    }
}


@Composable
private fun FreeBooksSection(books: List<BookEntity>, onBookClick: (BookEntity) -> Unit, purpleColor: Color) {
    Column {
        Text(
            text = "Nuestra selección de libros destacables por su popularidad",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = purpleColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                FreeBookCarouselItem(book = book, onBookClick = { onBookClick(book) })
            }
        }
    }
}

@Composable
private fun FreeBookCarouselItem(book: BookEntity, onBookClick: () -> Unit) {
    Card(
        modifier = Modifier.width(280.dp).clickable { onBookClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Color sutil del tema
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(book.coverUrl).crossfade(true).build(),
            contentDescription = book.title,
            modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun TrendingBooksSection(books: List<BookEntity>, onBookClick: (BookEntity) -> Unit, purpleColor: Color) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = "Novedades y títulos de tendencia",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = purpleColor,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Novedades y títulos de actualidad",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))
        LazyHorizontalGrid(
            rows = GridCells.Fixed(2),
            modifier = Modifier.height(420.dp), // Aumentamos la altura para más visibilidad
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(books) { book ->
                TrendingBookGridItem(book = book, onBookClick = { onBookClick(book) }, purpleColor = purpleColor)
            }
        }
    }
}
