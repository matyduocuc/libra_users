package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*       // PasswordVisualTransformation, VisualTransformation, etc.
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.data.repository.AuthViewModel

// 🔽 estos dos son los que faltaban
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType


@Composable
fun RegisterScreen(
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.register.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
    }

    RegisterContent(
        name          = state.name,
        email         = state.email,
        phone         = state.phone,
        pass          = state.pass,
        confirm       = state.confirm,
        nameError     = state.nameError,
        emailError    = state.emailError,
        phoneError    = state.phoneError,
        passError     = state.passError,
        confirmError  = state.confirmError,
        canSubmit     = state.canSubmit,
        isSubmitting  = state.isSubmitting,
        errorMsg      = state.errorMsg,
        onNameChange      = vm::onNameChange,
        onEmailChange     = vm::onRegisterEmailChange,
        onPhoneChange     = vm::onPhoneChange,
        onPassChange      = vm::onRegisterPassChange,
        onConfirmChange   = vm::onConfirmChange,
        onSubmit          = vm::submitRegister,
        onGoLogin         = onGoLogin
    )
}

@Composable
private fun RegisterContent(
    name: String,
    email: String,
    phone: String,
    pass: String,
    confirm: String,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    passError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Registro", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name, onValueChange = onNameChange,
                label = { Text("Nombre") }, singleLine = true,
                isError = nameError != null, modifier = Modifier.fillMaxWidth()
            )
            nameError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = email, onValueChange = onEmailChange,
                label = { Text("Email") }, singleLine = true,
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            emailError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = phone, onValueChange = onPhoneChange,
                label = { Text("Teléfono") }, singleLine = true,
                isError = phoneError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = pass, onValueChange = onPassChange,
                label = { Text("Contraseña") }, singleLine = true,
                isError = passError != null,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            passError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = confirm, onValueChange = onConfirmChange,
                label = { Text("Confirmar contraseña") }, singleLine = true,
                isError = confirmError != null,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            confirmError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Registrar")
                }
            }

            errorMsg?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Ir a Login")
            }
        }
    }
}
