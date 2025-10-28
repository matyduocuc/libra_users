package com.empresa.libra_users.screen.admin.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel
import com.empresa.libra_users.viewmodel.admin.LoanDetails
import androidx.compose.ui.unit.sp

@Composable
fun AdminUsersScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.usersUiState.collectAsStateWithLifecycle()
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(text = uiState.error ?: "Error desconocido", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.users, key = { it.id }) { user ->
                        UserListItem(user = user, onClick = { selectedUser = user })
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        val loans by viewModel.getLoansWithDetailsForUser(user.id).collectAsStateWithLifecycle(initialValue = emptyList())
        UserDetailsDialog(
            user = user,
            loans = loans,
            onDismiss = { selectedUser = null },
            onSave = { updatedUser ->
                viewModel.updateUser(updatedUser)
                selectedUser = null
            },
            onMarkLoanAsReturned = { loan ->
                viewModel.markLoanAsReturned(loan)
            }
        )
    }
}

@Composable
private fun UserListItem(user: UserEntity, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(user.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun UserDetailsDialog(
    user: UserEntity,
    loans: List<LoanDetails>,
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit,
    onMarkLoanAsReturned: (com.empresa.libra_users.data.local.user.LoanEntity) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var role by remember { mutableStateOf(user.role) }
    var password by remember { mutableStateOf(user.password) }
    var status by remember { mutableStateOf(user.status) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalles de Usuario") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") })
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Rol") })
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") })

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Cuenta Bloqueada: ")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = status == "blocked", onCheckedChange = { isChecked -> status = if (isChecked) "blocked" else "active" })
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Préstamos Activos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val activeLoans = loans.filter { it.loan.status == "Active" }
                if (activeLoans.isEmpty()) {
                    Text("El usuario no tiene préstamos activos.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        activeLoans.forEach { loanDetails ->
                            LoanDetailItem(loanDetails = loanDetails, onMarkAsReturned = { onMarkLoanAsReturned(loanDetails.loan) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedUser = user.copy(name = name, email = email, phone = phone, role = role, password = password, status = status)
                onSave(updatedUser)
            }) { Text("Guardar Cambios") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun LoanDetailItem(loanDetails: LoanDetails, onMarkAsReturned: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(loanDetails.book?.coverUrl).crossfade(true).build(),
            contentDescription = "Portada de ${loanDetails.book?.title}",
            modifier = Modifier.size(60.dp, 80.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(loanDetails.book?.title ?: "Libro no encontrado", fontWeight = FontWeight.Bold, maxLines = 2)
            Text("Prestado el: ${loanDetails.loan.loanDate}", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onMarkAsReturned, shape = MaterialTheme.shapes.small, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            Text("Devolver", fontSize = 12.sp)
        }
    }
}
