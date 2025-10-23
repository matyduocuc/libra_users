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

            Text("Aquí puedes ver y editar la información de tu cuenta.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = updateUserState.name,
                onValueChange = { vm.onUpdateUserNameChange(it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = updateUserState.email,
                onValueChange = { vm.onUpdateUserEmailChange(it) },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = updateUserState.phone,
                onValueChange = { vm.onUpdateUserPhoneChange(it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

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
