package com.empresa.libra_users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.repository.UserRepository
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -----------------------------
// ESTADOS DE PANTALLA
// -----------------------------

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class BookUiState(
    val books: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

data class LoanUiState(
    val loans: List<LoanEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

// -----------------------------
// VIEWMODEL PRINCIPAL
// -----------------------------

class MainViewModel(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    notificationRepository: NotificationRepository
) : ViewModel() {

    // Estados de UI
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    private val _books = MutableStateFlow(BookUiState())
    val books: StateFlow<BookUiState> = _books

    private val _loans = MutableStateFlow(LoanUiState())
    val loans: StateFlow<LoanUiState> = _loans

    // -----------------------------
    // LOGIN / REGISTER
    // -----------------------------
    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = if (value.contains("@")) null else "Correo inválido") }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null && s.email.isNotBlank() && s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }

            val result = userRepository.login(s.email.trim(), s.pass)
            _login.update {
                if (result.isSuccess) it.copy(isSubmitting = false, success = true)
                else it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message ?: "Credenciales inválidas")
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    // REGISTRO
    fun onRegisterNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update { it.copy(name = filtered, nameError = if (filtered.length < 3) "Nombre demasiado corto" else null) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = if (value.contains("@")) null else "Correo inválido") }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPhoneChange(value: String) {
        val digits = value.filter { it.isDigit() }
        _register.update { it.copy(phone = digits, phoneError = if (digits.length < 8) "Teléfono inválido" else null) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = if (value.length < 6) "Contraseña muy corta" else null) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = if (value != _register.value.pass) "No coincide" else null) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = listOf(s.name, s.email, s.phone, s.pass, s.confirm).all { it.isNotBlank() }
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            val result = userRepository.register(s.name.trim(), s.email.trim(), s.phone.trim(), s.pass)
            _register.update {
                if (result.isSuccess) it.copy(isSubmitting = false, success = true)
                else it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // -----------------------------
    // LIBROS
    // -----------------------------
    fun loadBooks() {
        viewModelScope.launch {
            _books.update { it.copy(isLoading = true) }
            try {
                val data = bookRepository.getAllBooks()
                _books.update { it.copy(isLoading = false, books = data) }
            } catch (e: Exception) {
                _books.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
        }
    }

    fun addBook(book: BookEntity) {
        viewModelScope.launch {
            try {
                bookRepository.insert(book)
                loadBooks()
            } catch (e: Exception) {
                _books.update { it.copy(errorMsg = "Error al agregar libro: ${e.message}") }
            }
        }
    }

    // -----------------------------
    // PRÉSTAMOS
    // -----------------------------
    fun loadLoans(userId: Long) {
        viewModelScope.launch {
            _loans.update { it.copy(isLoading = true) }
            try {
                val data = loanRepository.getLoansByUser(userId)
                _loans.update { it.copy(isLoading = false, loans = data) }
            } catch (e: Exception) {
                _loans.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
        }
    }

    fun addLoan(loan: LoanEntity) {
        viewModelScope.launch {
            try {
                loanRepository.insert(loan)
                loadLoans(loan.userId)
            } catch (e: Exception) {
                _loans.update { it.copy(errorMsg = "Error al registrar préstamo: ${e.message}") }
            }
        }
    }
}


