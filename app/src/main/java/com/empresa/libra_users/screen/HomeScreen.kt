package com.empresa.libra_users.screen

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    vm: MainViewModel,
    onLogout: () -> Unit // <-- CAMBIO: Recibe onLogout
) {
    val homeState by vm.home.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Indicador de carga y errores
        item {
            if (homeState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            homeState.errorMsg?.let {
                Text("Error: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        }

        // 2. Carruseles de libros por categoría
        homeState.categorizedBooks.forEach { (categoryTitle, books) ->
            item {
                BookCategorySection(
                    title = categoryTitle,
                    books = books,
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        // 3. SECCIÓN DE BOTONES DE LOGIN/REGISTRO ELIMINADA
    }
}

// --- EL RESTO DEL ARCHIVO SE QUEDA IGUAL ---

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
            IconButton(onClick = { /* Navegar a "Ver todo" */ }) {
                Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Ver más")
            }
        }
        Spacer(Modifier.height(12.dp))
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
    Column(
        modifier = Modifier.width(110.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomEnd
        ) {
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
        Text(
            book.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
