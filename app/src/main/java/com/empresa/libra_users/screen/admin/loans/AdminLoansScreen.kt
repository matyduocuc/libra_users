package com.empresa.libra_users.screen.admin.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel
import com.empresa.libra_users.viewmodel.admin.LoanDetails

@Composable
fun AdminLoansScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.loansUiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(
                text = uiState.error ?: "Error desconocido",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            uiState.loans.isEmpty() -> Text(
                "No hay préstamos registrados.",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.loans, key = { it.loan.id }) { loanDetails ->
                        LoanCard(loanDetails = loanDetails, onMarkAsReturned = {
                            viewModel.markLoanAsReturned(loanDetails.loan)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanCard(loanDetails: LoanDetails, onMarkAsReturned: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(loanDetails.book?.coverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Portada de ${loanDetails.book?.title}",
                    modifier = Modifier.size(80.dp, 120.dp),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(loanDetails.book?.title ?: "Libro no encontrado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(loanDetails.user?.name ?: "Usuario no encontrado", style = MaterialTheme.typography.bodyMedium)
                    Text("Fecha de Préstamo: ${loanDetails.loan.loanDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Fecha de Devolución: ${loanDetails.loan.dueDate}", style = MaterialTheme.typography.bodySmall)
                    Text("Estado: ${loanDetails.loan.status}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
            if (loanDetails.loan.status == "Active") {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onMarkAsReturned, modifier = Modifier.fillMaxWidth()) {
                    Text("Marcar como Devuelto")
                }
            }
        }
    }
}
