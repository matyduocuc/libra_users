package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(vm: MainViewModel) {
    val updateUserState by vm.updateUserState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cargar los datos del usuario actual cuando la pantalla se muestra por primera vez
    LaunchedEffect(Unit) {
        vm.loadCurrentUserData()
    }

    // Mostrar un Snackbar cuando la actualización sea exitosa
    LaunchedEffect(updateUserState.success) {
        if (updateUserState.success) {
            scope.launch {
                snackbarHostState.showSnackbar("¡Datos actualizados con éxito!")
            }
            vm.clearUpdateUserState()
        }
    }

    // Mostrar un Snackbar si hay un error
    LaunchedEffect(updateUserState.errorMsg) {
        updateUserState.errorMsg?.let {
            scope.launch {
                snackbarHostState.showSnackbar("Error: $it")
            }
            vm.clearUpdateUserState()
        }
    }

    // Si debemos mostrar el diálogo de verificación, lo componemos
    if (updateUserState.showVerificationDialog) {
        VerificationDialog(
            vm = vm,
            onDismiss = { vm.cancelEmailUpdate() }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text("Configuración de la cuenta", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // Campo para el ID de usuario (solo lectura)
            OutlinedTextField(
                value = updateUserState.userId,
                onValueChange = {},
                label = { Text("ID de Usuario") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            Spacer(Modifier.height(16.dp))

            // Campos para el nombre y teléfono (editables)
            OutlinedTextField(
                value = updateUserState.name,
                onValueChange = { vm.onUpdateUserNameChange(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = updateUserState.phone,
                onValueChange = { vm.onUpdateUserPhoneChange(it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            // Sección para el correo electrónico con botón de cambio
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = updateUserState.email,
                    onValueChange = { vm.onUpdateUserEmailChange(it) },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.weight(1f),
                    // Se deshabilita si el diálogo de verificación está abierto
                    enabled = !updateUserState.showVerificationDialog
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.initiateEmailUpdate() }) {
                    Text("Cambiar")
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.updateUser() },
                enabled = !updateUserState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (updateUserState.isSubmitting) "Guardando..." else "Guardar Cambios")
            }
        }
    }
}

@Composable
private fun VerificationDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val updateUserState by vm.updateUserState.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verificar correo electrónico") },
        text = {
            Column {
                Text("Se ha enviado un código de verificación a su correo. Por favor, introdúzcalo a continuación.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = updateUserState.verificationCode,
                    onValueChange = { vm.onVerificationCodeChange(it) },
                    label = { Text("Código de Verificación") },
                    isError = updateUserState.verificationError != null,
                    singleLine = true
                )
                if (updateUserState.verificationError != null) {
                    Text(
                        text = updateUserState.verificationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { vm.confirmEmailUpdate() },
                enabled = !updateUserState.isVerifying
            ) {
                if (updateUserState.isVerifying) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
