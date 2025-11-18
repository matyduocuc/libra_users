package com.empresa.libra_users.screen.admin.books

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.domain.validation.*
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBooksScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.booksUiState.collectAsStateWithLifecycle()
    var showAddBookDialog by remember { mutableStateOf(false) }
    var selectedBook by remember { mutableStateOf<BookEntity?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var soloDisponibles by remember { mutableStateOf(false) }

    // Obtener categorías únicas
    val categorias = remember(uiState.books) {
        uiState.books.map { it.categoria }.distinct().sorted()
    }

    LaunchedEffect(searchQuery) {
        viewModel.updateBookSearchQuery(searchQuery)
    }

    LaunchedEffect(selectedCategory) {
        viewModel.updateBookCategoryFilter(selectedCategory)
    }

    LaunchedEffect(soloDisponibles) {
        viewModel.updateBookDisponiblesFilter(soloDisponibles)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Gestión de Libros",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBookDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "Añadir libro",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra de búsqueda y filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Búsqueda mejorada
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar por título, autor o ISBN") },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Filtros mejorados
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Filtro por categoría
                        FilterChip(
                            selected = selectedCategory != null,
                            onClick = { selectedCategory = if (selectedCategory != null) null else categorias.firstOrNull() },
                            label = { 
                                Text(
                                    if (selectedCategory != null) "Categoría: $selectedCategory" else "Categoría",
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        // Filtro solo disponibles
                        FilterChip(
                            selected = soloDisponibles,
                            onClick = { soloDisponibles = !soloDisponibles },
                            label = { 
                                Text(
                                    "Solo disponibles",
                                    style = MaterialTheme.typography.labelMedium
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }

                    // Selector de categoría si está activo
                    if (selectedCategory != null) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                            TextField(
                                value = selectedCategory ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Todas") },
                                    onClick = {
                                        selectedCategory = null
                                        expanded = false
                                    }
                                )
                                categorias.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria) },
                                        onClick = {
                                            selectedCategory = categoria
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Lista de libros
            Box(
                modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(
                    text = uiState.error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error
                )
                    uiState.filteredBooks.isEmpty() -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Text(
                                    "No hay libros que coincidan con tu búsqueda",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Intenta ajustar los filtros o crear un nuevo libro",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                else -> {
                    LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                            items(uiState.filteredBooks, key = { it.id }) { book ->
                            BookGridItem(
                                book = book,
                                    onClick = { selectedBook = book },
                                    onEdit = { selectedBook = book; showEditDialog = true }
                            )
                            }
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
            onAddBook = { result ->
                if (result.isSuccess) {
                showAddBookDialog = false
            }
            },
            viewModel = viewModel
        )
    }

    if (showEditDialog && selectedBook != null) {
        EditBookDialog(
            book = selectedBook!!,
            onDismiss = { showEditDialog = false; selectedBook = null },
            onUpdate = { result ->
                if (result.isSuccess) {
                    showEditDialog = false
                    selectedBook = null
                }
            },
            viewModel = viewModel
        )
    }

    selectedBook?.let { book ->
        BookDetailsDialog(
            book = book,
            onDismiss = { selectedBook = null },
            onEdit = { selectedBook = book; showEditDialog = true },
            onDelete = {
                showDeleteConfirmDialog = true
            }
        )
    }

    if (showDeleteConfirmDialog && selectedBook != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Eliminar libro?") },
            text = {
                Text("Esta acción no se puede deshacer. ¿Estás seguro de que deseas eliminar \"${selectedBook?.title}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBook(selectedBook!!)
                        showDeleteConfirmDialog = false
                selectedBook = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun BookGridItem(
    book: BookEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.coverUrl)
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_report_image)
                        .build(),
                    contentDescription = "Portada de ${book.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.7f),
                    contentScale = ContentScale.Crop
                )
                // Badge de disponibilidad mejorado
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = when {
                        book.disponibles > 0 -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = "${book.disponibles}/${book.stock}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (book.disponibles > 0) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    book.author,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        book.categoria,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BookDetailsDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                book.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Portada mejorada
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Portada de ${book.title}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Información en cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow("Autor", book.author)
                        InfoRow("Categoría", book.categoria)
                        InfoRow("ISBN", book.isbn)
                        InfoRow("Editorial", book.publisher)
                        InfoRow("Año", book.anio.toString())
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoColumn("Stock", book.stock.toString())
                        InfoColumn("Disponibles", book.disponibles.toString())
                        InfoColumn("Estado", book.status)
                    }
                }
                
                if (book.descripcion.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Descripción",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                book.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Eliminar", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBookDialog(
    onDismiss: () -> Unit,
    onAddBook: (Result<String>) -> Unit,
    viewModel: AdminDashboardViewModel
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var anio by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
    var stock by remember { mutableStateOf("1") }
    var descripcion by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }

    // Errores de validación
    var titleError by remember { mutableStateOf<String?>(null) }
    var authorError by remember { mutableStateOf<String?>(null) }
    var categoriaError by remember { mutableStateOf<String?>(null) }
    var isbnError by remember { mutableStateOf<String?>(null) }
    var publisherError by remember { mutableStateOf<String?>(null) }
    var anioError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var descripcionError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    val categoriasDisponibles = listOf("Ciencia", "Literatura", "Ciencia Ficción", "Fantasía", "Historia", "Juvenil", "Misterio", "Suspenso", "Terror", "Romance")
    var categoriaExpanded by remember { mutableStateOf(false) }

    val categoryMapping = mapOf(
        "Ciencia" to 1,
        "Literatura" to 1,
        "Ciencia Ficción" to 2,
        "Fantasía" to 2,
        "Historia" to 3,
        "Juvenil" to 3,
        "Misterio" to 4,
        "Suspenso" to 4,
        "Terror" to 4,
        "Romance" to 3
    )

    val homeSections = listOf("Ninguno", "Trending", "Free")
    var homeSectionExpanded by remember { mutableStateOf(false) }
    var selectedHomeSection by remember { mutableStateOf(homeSections.first()) }

    val anioActual = Calendar.getInstance().get(Calendar.YEAR)

    // Validar en tiempo real
    LaunchedEffect(title) {
        titleError = validateBookTitle(title)
    }
    LaunchedEffect(author) {
        authorError = validateBookAuthor(author)
    }
    LaunchedEffect(categoria) {
        categoriaError = validateBookCategory(categoria)
    }
    LaunchedEffect(isbn) {
        isbnError = validateISBN(isbn)
    }
    LaunchedEffect(publisher) {
        publisherError = validateBookPublisher(publisher)
    }
    LaunchedEffect(anio) {
        anioError = anio.toIntOrNull()?.let { validateBookYear(it, anioActual) }
    }
    LaunchedEffect(stock) {
        stockError = stock.toIntOrNull()?.let { validateStock(it) }
    }
    LaunchedEffect(descripcion) {
        descripcionError = validateBookDescription(descripcion)
    }

    val canSubmit = titleError == null && authorError == null && categoriaError == null &&
            isbnError == null && publisherError == null && anioError == null &&
            stockError == null && descripcionError == null &&
            title.isNotBlank() && author.isNotBlank() && categoria.isNotBlank() &&
            isbn.isNotBlank() && publisher.isNotBlank() && anio.isNotBlank() && stock.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir nuevo libro") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                generalError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título*") },
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor*") },
                    isError = authorError != null,
                    supportingText = { authorError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = { categoriaExpanded = !categoriaExpanded }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoría*") },
                        readOnly = false,
                        isError = categoriaError != null,
                        supportingText = { categoriaError?.let { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        categoriasDisponibles.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN*") },
                    isError = isbnError != null,
                    supportingText = { isbnError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Editorial*") },
                    isError = publisherError != null,
                    supportingText = { publisherError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = anio,
                    onValueChange = { anio = it },
                    label = { Text("Año*") },
                    isError = anioError != null,
                    supportingText = { anioError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock*") },
                    isError = stockError != null,
                    supportingText = { stockError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    isError = descripcionError != null,
                    supportingText = { descripcionError?.let { Text(it) } },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("URL de Portada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = homeSectionExpanded,
                    onExpandedChange = { homeSectionExpanded = !homeSectionExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedHomeSection,
                        onValueChange = {},
                        label = { Text("Sección en Home") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeSectionExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = homeSectionExpanded,
                        onDismissRequest = { homeSectionExpanded = false }
                    ) {
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
                    val anioInt = anio.toIntOrNull() ?: anioActual
                    val stockInt = stock.toIntOrNull() ?: 1
                    val categoryId = categoryMapping[categoria] ?: 1
                    val sectionToSave = if (selectedHomeSection == "Ninguno") "None" else selectedHomeSection

                    val result = viewModel.addBook(
                        title = title,
                        author = author,
                        categoria = categoria,
                        isbn = isbn,
                        publisher = publisher,
                        anio = anioInt,
                        stock = stockInt,
                        descripcion = descripcion,
                        coverUrl = coverUrl,
                        categoryId = categoryId,
                        homeSection = sectionToSave
                    )
                    if (result.isSuccess) {
                        onAddBook(result)
                    } else {
                        generalError = result.exceptionOrNull()?.message ?: "Error al guardar el libro"
                    }
                },
                enabled = canSubmit
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBookDialog(
    book: BookEntity,
    onDismiss: () -> Unit,
    onUpdate: (Result<String>) -> Unit,
    viewModel: AdminDashboardViewModel
) {
    var title by remember { mutableStateOf(book.title) }
    var author by remember { mutableStateOf(book.author) }
    var categoria by remember { mutableStateOf(book.categoria) }
    var isbn by remember { mutableStateOf(book.isbn) }
    var publisher by remember { mutableStateOf(book.publisher) }
    var anio by remember { mutableStateOf(book.anio.toString()) }
    var stock by remember { mutableStateOf(book.stock.toString()) }
    var descripcion by remember { mutableStateOf(book.descripcion) }
    var coverUrl by remember { mutableStateOf(book.coverUrl) }

    // Errores de validación
    var titleError by remember { mutableStateOf<String?>(null) }
    var authorError by remember { mutableStateOf<String?>(null) }
    var categoriaError by remember { mutableStateOf<String?>(null) }
    var isbnError by remember { mutableStateOf<String?>(null) }
    var publisherError by remember { mutableStateOf<String?>(null) }
    var anioError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    val categoriasDisponibles = listOf("Ciencia", "Literatura", "Ciencia Ficción", "Fantasía", "Historia", "Juvenil", "Misterio", "Suspenso", "Terror", "Romance")
    var categoriaExpanded by remember { mutableStateOf(false) }

    val categoryMapping = mapOf(
        "Ciencia" to 1,
        "Literatura" to 1,
        "Ciencia Ficción" to 2,
        "Fantasía" to 2,
        "Historia" to 3,
        "Juvenil" to 3,
        "Misterio" to 4,
        "Suspenso" to 4,
        "Terror" to 4,
        "Romance" to 3
    )

    val homeSections = listOf("Ninguno", "Trending", "Free")
    var homeSectionExpanded by remember { mutableStateOf(false) }
    var selectedHomeSection by remember { mutableStateOf(if (book.homeSection == "None") "Ninguno" else book.homeSection) }

    val anioActual = Calendar.getInstance().get(Calendar.YEAR)

    // Coroutine scope para operaciones asíncronas
    val scope = rememberCoroutineScope()

    // Validar en tiempo real
    LaunchedEffect(title) {
        titleError = validateBookTitle(title)
    }
    LaunchedEffect(author) {
        authorError = validateBookAuthor(author)
    }
    LaunchedEffect(categoria) {
        categoriaError = validateBookCategory(categoria)
    }
    LaunchedEffect(isbn) {
        isbnError = validateISBN(isbn)
    }
    LaunchedEffect(publisher) {
        publisherError = validateBookPublisher(publisher)
    }
    LaunchedEffect(anio) {
        anioError = anio.toIntOrNull()?.let { validateBookYear(it, anioActual) }
    }
    LaunchedEffect(stock) {
        stockError = stock.toIntOrNull()?.let { validateStock(it) }
    }

    val canSubmit = titleError == null && authorError == null && categoriaError == null &&
            isbnError == null && publisherError == null && anioError == null &&
            stockError == null &&
            title.isNotBlank() && author.isNotBlank() && categoria.isNotBlank() &&
            isbn.isNotBlank() && publisher.isNotBlank() && anio.isNotBlank() && stock.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar libro") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                generalError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título*") },
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Autor*") },
                    isError = authorError != null,
                    supportingText = { authorError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = { categoriaExpanded = !categoriaExpanded }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoría*") },
                        readOnly = false,
                        isError = categoriaError != null,
                        supportingText = { categoriaError?.let { Text(it) } },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        categoriasDisponibles.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = isbn,
                    onValueChange = { isbn = it },
                    label = { Text("ISBN*") },
                    isError = isbnError != null,
                    supportingText = { isbnError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Editorial*") },
                    isError = publisherError != null,
                    supportingText = { publisherError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = anio,
                    onValueChange = { anio = it },
                    label = { Text("Año*") },
                    isError = anioError != null,
                    supportingText = { anioError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock*") },
                    isError = stockError != null,
                    supportingText = { stockError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Disponibles: ${book.disponibles} (no editable directamente)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = coverUrl,
                    onValueChange = { coverUrl = it },
                    label = { Text("URL de Portada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = homeSectionExpanded,
                    onExpandedChange = { homeSectionExpanded = !homeSectionExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedHomeSection,
                        onValueChange = {},
                        label = { Text("Sección en Home") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = homeSectionExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = homeSectionExpanded,
                        onDismissRequest = { homeSectionExpanded = false }
                    ) {
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
                    val anioInt = anio.toIntOrNull() ?: book.anio
                    val stockInt = stock.toIntOrNull() ?: book.stock
                    val categoryId = categoryMapping[categoria] ?: book.categoryId.toInt()
                    val sectionToSave = if (selectedHomeSection == "Ninguno") "None" else selectedHomeSection

                    val updatedBook = book.copy(
                        title = title,
                        author = author,
                        categoria = categoria,
                        isbn = isbn,
                        publisher = publisher,
                        anio = anioInt,
                        stock = stockInt,
                        descripcion = descripcion,
                        coverUrl = coverUrl,
                        categoryId = categoryId.toLong(),
                        homeSection = sectionToSave
                    )

                    scope.launch {
                        val result = viewModel.updateBook(updatedBook)
                        if (result.isSuccess) {
                            onUpdate(result)
                        } else {
                            generalError = result.exceptionOrNull()?.message ?: "Error al actualizar el libro"
                        }
                    }
                },
                enabled = canSubmit
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
