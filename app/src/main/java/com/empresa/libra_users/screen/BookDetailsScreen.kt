package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    vm: MainViewModel,
    bookId: Long,
    onBack: () -> Unit
) {
    val bookState = remember { mutableStateOf<BookEntity?>(null) }
    val showLoanDialog = remember { mutableStateOf(false) }
    val loanDays = remember { mutableStateOf(1) }

    LaunchedEffect(bookId) {
        bookState.value = vm.getBookById(bookId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bookState.value?.title ?: "Detalles del Libro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) {
        val book = bookState.value
        if (book != null) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(book.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Portada de ${book.title}",
                        modifier = Modifier.width(120.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(book.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("por ${book.author}", style = MaterialTheme.typography.titleMedium)
                        Text("Editorial: ${book.publisher}", style = MaterialTheme.typography.bodyMedium)
                        Text("Publicado: ${book.publishDate}", style = MaterialTheme.typography.bodyMedium)
                        Text("ISBN: ${book.isbn}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Aquí va una descripción del libro. Por ahora, es un texto de ejemplo. En una implementación real, este campo debería provenir de tu BookEntity.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { showLoanDialog.value = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = book.status == "Available"
                ) {
                    Text(if (book.status == "Available") "Solicitar Préstamo" else "No disponible")
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (showLoanDialog.value) {
            LoanConfirmationDialog(
                loanDays = loanDays.value,
                onLoanDaysChange = { loanDays.value = it },
                onConfirm = {
                    // Aquí llamas a la función del ViewModel para registrar el préstamo
                    // vm.registerLoan(userId = 1, bookId = bookId, loanDays = loanDays.value)
                    showLoanDialog.value = false
                },
                onDismiss = { showLoanDialog.value = false }
            )
        }
    }
}

@Composable
private fun LoanConfirmationDialog(
    loanDays: Int,
    onLoanDaysChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Préstamo") },
        text = {
            Column {
                Text("Selecciona el número de días para el préstamo:")
                Slider(
                    value = loanDays.toFloat(),
                    onValueChange = { onLoanDaysChange(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 28
                )
                Text(text = "Días: $loanDays", modifier = Modifier.align(Alignment.End))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
