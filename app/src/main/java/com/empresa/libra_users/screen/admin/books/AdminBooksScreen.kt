package com.empresa.libra_users.screen.admin.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBooksScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.booksUiState.collectAsStateWithLifecycle()
    var showAddBookDialog by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBookDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir libro")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(
                    text = uiState.error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error
                )
                uiState.books.isEmpty() -> Text(
                    "No hay libros disponibles.",
                    style = MaterialTheme.typography.headlineMedium
                )
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.books, key = { it.id }) { book ->
                            BookGridItem(
                                book = book,
                                onClick = { selectedBook = book }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Diálogos ---
    if (showAddBookDialog) {
        AddBookDialog(
            onDismiss = { showAddBookDialog = false },
            onAddBook = { title, author, coverUrl, isbn, publisher, categoryId, homeSection ->
                viewModel.addBook(
                    title = title,
                    author = author,
                    coverUrl = coverUrl,
                    isbn = isbn,
                    publisher = publisher,
                    categoryId = categoryId,
                    homeSection = homeSection
                )
                showAddBookDialog = false
            }
        )
    }

    selectedBook?.let { book ->
        BookDetailsDialog(
            book = book,
            onDismiss = { selectedBook = null },
            onDelete = {
                viewModel.deleteBook(book)
                selectedBook = null
            }
        )
    }
}

@Composable
private fun BookGridItem(
    book: BookEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(book.coverUrl)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_report_image) // Imagen de error
                    .build(),
                contentDescription = "Portada de ${book.title}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
            Column(Modifier.padding(8.dp)) {
                Text(book.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }
    }
}

@Composable
private fun BookDetailsDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(book.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = book.coverUrl,
                    contentDescription = "Portada de ${book.title}",
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
                Text("Autor: ${book.author}", style = MaterialTheme.typography.bodyLarge)
                Text("ISBN: ${book.isbn}", style = MaterialTheme.typography.bodyMedium)
                Text("Editorial: ${book.publisher}", style = MaterialTheme.typography.bodyMedium)
                Text("Publicado: ${book.publishDate}", style = MaterialTheme.typography.bodyMedium)
                Text("Estado: ${book.status}", style = MaterialTheme.typography.bodyMedium)
                Text("Inventario: ${book.inventoryCode}", style = MaterialTheme.typography.bodyMedium)
                Text("Sección Home: ${book.homeSection}", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        dismissButton = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookDialog(
    onDismiss: () -> Unit,
    onAddBook: (title: String, author: String, coverUrl: String, isbn: String, publisher: String, categoryId: Int, homeSection: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }

    val categories = mapOf(
        "Clásicos universales" to 1,
        "Ciencia ficción y fantasía" to 2,
        "Romance y drama" to 3,
        "Misterio y suspenso" to 4
    )
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategoryName by remember { mutableStateOf(categories.keys.first()) }

    val homeSections = listOf("Ninguno", "Trending", "Free")
    var homeSectionExpanded by remember { mutableStateOf(false) }
    var selectedHomeSection by remember { mutableStateOf(homeSections.first()) }

    val canSubmit = title.isNotBlank() && author.isNotBlank() && coverUrl.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir nuevo libro") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título*") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Autor*") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = coverUrl, onValueChange = { coverUrl = it }, label = { Text("URL de Portada*") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = isbn, onValueChange = { isbn = it }, label = { Text("ISBN") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = publisher, onValueChange = { publisher = it }, label = { Text("Editorial") })
                Spacer(Modifier.height(16.dp))

                // --- Selector de Categoría ---
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    TextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        label = { Text("Categoría") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.keys.forEach { categoryName ->
                            DropdownMenuItem(
                                text = { Text(categoryName) },
                                onClick = {
                                    selectedCategoryName = categoryName
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // --- Selector de Sección Home ---
                ExposedDropdownMenuBox(expanded = homeSectionExpanded, onExpandedChange = { homeSectionExpanded = !homeSectionExpanded }) {
                    TextField(
                        value = selectedHomeSection,
                        onValueChange = {},
                        label = { Text("Sección en Home") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeSectionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = homeSectionExpanded, onDismissRequest = { homeSectionExpanded = false }) {
                        homeSections.forEach { section ->
                            DropdownMenuItem(
                                text = { Text(section) },
                                onClick = {
                                    selectedHomeSection = section
                                    homeSectionExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val selectedCategoryId = categories[selectedCategoryName] ?: 1
                    val sectionToSave = if (selectedHomeSection == "Ninguno") "None" else selectedHomeSection
                    onAddBook(title, author, coverUrl, isbn, publisher, selectedCategoryId, sectionToSave)
                },
                enabled = canSubmit
            ) {
                Text("Añadir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
