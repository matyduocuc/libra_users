package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    vm: MainViewModel,
    bookId: Long,
    onBack: () -> Unit
) {
    val bookState = remember { mutableStateOf<BookEntity?>(null) }
    val user by vm.user.collectAsStateWithLifecycle()

    // Estados para los diálogos
    val showLoanDialog = remember { mutableStateOf(false) }
    val showPaymentDialog = remember { mutableStateOf(false) }

    // Estados para los datos del préstamo y pago
    val loanDays = remember { mutableStateOf(1) }
    val cardNumber = remember { mutableStateOf("") }
    val expiryDate = remember { mutableStateOf("") }
    val cvv = remember { mutableStateOf("") }

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
                // --- Contenido de la pantalla de detalles ---
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(book.coverUrl).crossfade(true).build(),
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxWidth().height(220.dp), contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
                Text(book.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("por ${book.author}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                RatingBar(rating = 4.5) // Calificación con estrellas
                Spacer(Modifier.height(16.dp))
                Text("Editorial: ${book.publisher}", style = MaterialTheme.typography.bodyLarge)
                Text("Publicado: ${book.publishDate}", style = MaterialTheme.typography.bodyLarge)
                Text("ISBN: ${book.isbn}", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Aquí va la descripción del libro...", style = MaterialTheme.typography.bodyLarge)
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

        // --- Diálogos ---
        if (showLoanDialog.value) {
            LoanDaysDialog(
                loanDays = loanDays.value,
                onLoanDaysChange = { days -> loanDays.value = if (days in 1..30) days else loanDays.value },
                onConfirm = {
                    showLoanDialog.value = false
                    showPaymentDialog.value = true // Abrir diálogo de pago
                },
                onDismiss = { showLoanDialog.value = false }
            )
        }

        if (showPaymentDialog.value) {
            PaymentDialog(
                cardNumber = cardNumber.value, onCardNumberChange = { num -> cardNumber.value = num },
                expiryDate = expiryDate.value, onExpiryDateChange = { date -> expiryDate.value = date },
                cvv = cvv.value, onCvvChange = { c -> cvv.value = c },
                onConfirm = {
                    user?.let { currentUser ->
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        val loanDate = dateFormat.format(calendar.time)
                        calendar.add(Calendar.DAY_OF_YEAR, loanDays.value)
                        val dueDate = dateFormat.format(calendar.time)

                        vm.registerLoan(currentUser.id, bookId, loanDate, dueDate)
                    }
                    showPaymentDialog.value = false
                },
                onDismiss = { showPaymentDialog.value = false }
            )
        }
    }
}

@Composable
private fun RatingBar(rating: Double, starCount: Int = 5, starColor: Color = Color(0xFFFFC107)) {
    Row {
        val fullStars = rating.toInt()
        val halfStar = if (rating - fullStars >= 0.5) 1 else 0
        val emptyStars = starCount - fullStars - halfStar
        repeat(fullStars) { Icon(Icons.Filled.Star, contentDescription = null, tint = starColor) }
        if (halfStar == 1) { Icon(Icons.Filled.StarHalf, contentDescription = null, tint = starColor) }
        repeat(emptyStars) { Icon(Icons.Filled.StarOutline, contentDescription = null, tint = starColor) }
    }
}

@Composable
private fun LoanDaysDialog(
    loanDays: Int, onLoanDaysChange: (Int) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Préstamo de Libro") },
        text = {
            Column {
                Text("Selecciona el número de días para el préstamo (máx. 30 días):")
                Slider(value = loanDays.toFloat(), onValueChange = { onLoanDaysChange(it.toInt()) }, valueRange = 1f..30f, steps = 28)
                Text(text = "Días: $loanDays", modifier = Modifier.align(Alignment.End))
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Siguiente") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun PaymentDialog(
    cardNumber: String, onCardNumberChange: (String) -> Unit,
    expiryDate: String, onExpiryDateChange: (String) -> Unit,
    cvv: String, onCvvChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Realizar Pago") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Introduce los datos de tu tarjeta de débito para procesar el préstamo.")
                OutlinedTextField(value = cardNumber, onValueChange = onCardNumberChange, label = { Text("Número de Tarjeta") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = expiryDate, onValueChange = onExpiryDateChange, label = { Text("Fecha de Vencimiento (MM/AA)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = cvv, onValueChange = onCvvChange, label = { Text("CVV") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = { Button(onClick = onConfirm) { Text("Pagar y Confirmar Préstamo") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
