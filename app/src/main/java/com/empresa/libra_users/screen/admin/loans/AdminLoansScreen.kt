package com.empresa.libra_users.screen.admin.loans

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
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

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 4.dp
            ) {
                TopAppBar(
                    title = { 
                        Text(
                            "Gestión de Préstamos",
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
        }
    ) { paddingValues ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                uiState.loans.isEmpty() -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
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
                                Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                "No hay préstamos registrados",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
}

@Composable
private fun LoanCard(loanDetails: LoanDetails, onMarkAsReturned: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Portada del libro
                Card(
                    shape = MaterialTheme.shapes.small,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(loanDetails.book?.coverUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Portada de ${loanDetails.book?.title}",
                        modifier = Modifier.size(80.dp, 120.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        loanDetails.book?.title ?: "Libro no encontrado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        loanDetails.user?.name ?: "Usuario no encontrado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    // Información de fechas
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            InfoRow("Préstamo", loanDetails.loan.loanDate)
                            Spacer(Modifier.height(4.dp))
                            InfoRow("Devolución", loanDetails.loan.dueDate)
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Badge de estado
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (loanDetails.loan.status) {
                            "Active" -> MaterialTheme.colorScheme.primaryContainer
                            "Returned" -> MaterialTheme.colorScheme.secondaryContainer
                            "Overdue" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = "Estado: ${loanDetails.loan.status}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (loanDetails.loan.status) {
                                "Active" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Returned" -> MaterialTheme.colorScheme.onSecondaryContainer
                                "Overdue" -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            
            if (loanDetails.loan.status == "Active") {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onMarkAsReturned,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Marcar como Devuelto")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
