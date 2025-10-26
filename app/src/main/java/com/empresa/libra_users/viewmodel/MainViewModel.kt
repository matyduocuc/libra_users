package com.empresa.libra_users.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.UserPreferencesRepository
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    val profileImageUri: String? = null, // Campo para la foto de perfil
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

data class HomeUiState(
    val currentCoverIndex: Int = 0,
    val coverImages: List<String> = emptyList(),
    val featuredBooks: List<BookEntity> = emptyList(),
    val categorizedBooks: Map<String, List<BookEntity>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

data class SearchUiState(
    val query: String = "",
    val results: List<BookEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val initialSearchPerformed: Boolean = false
)

data class UpdateUserUiState(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUri: String? = null, // Campo para la foto de perfil
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val showVerificationDialog: Boolean = false,
    val verificationCode: String = "",
    val isVerifying: Boolean = false,
    val verificationError: String? = null
)

data class CartItem(
    val book: BookEntity,
    val loanDays: Int = 7
) {
    val price: Double
        get() = loanDays * 0.15
}

enum class AuthState {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    private val notificationRepository: NotificationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _updateUserState = MutableStateFlow(UpdateUserUiState())
    val updateUserState: StateFlow<UpdateUserUiState> = _updateUserState.asStateFlow()

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private var searchDebounceJob: Job? = null
    private val searchDebounceMillis = 500L

    init {
        // Quitamos la carga de libros del inicio para mejorar el rendimiento
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        userPreferencesRepository.userEmail
            .onEach { email ->
                if (email == null) {
                    _authState.value = AuthState.UNAUTHENTICATED
                    _user.value = null
                } else {
                    loadUserSession(email)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUserSession(email: String) {
        viewModelScope.launch {
            try {
                val userProfile = userRepository.getUserByEmail(email)
                if (userProfile != null) {
                    _user.value = userProfile
                    _authState.value = AuthState.AUTHENTICATED
                    // Cargamos los libros DESPUÉS de confirmar que el usuario está autenticado
                    loadCategorizedBooks()
                } else {
                    logout()
                }
            } catch (e: Exception) {
                logout()
            }
        }
    }

    fun loadCategorizedBooks() {
        viewModelScope.launch {
            _home.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                val categorized = bookRepository.getCategorizedBooks()
                _home.update {
                    it.copy(
                        isLoading = false,
                        categorizedBooks = categorized,
                        featuredBooks = categorized.values.flatten().distinct().take(6)
                    )
                }
            } catch (e: Exception) {
                _home.update { it.copy(isLoading = false, errorMsg = "Error al cargar categorías: ${e.message}") }
            }
        }
    }

    suspend fun getBookById(bookId: Long): BookEntity? {
        return bookRepository.getBookById(bookId)
    }

    fun addToCart(book: BookEntity) {
        _cart.update { currentCart ->
            if (currentCart.any { it.book.id == book.id }) {
                currentCart
            } else {
                currentCart + CartItem(book = book)
            }
        }
    }

    fun removeFromCart(bookId: Long) {
        _cart.update { currentCart ->
            currentCart.filterNot { it.book.id == bookId }
        }
    }

    fun updateLoanDays(bookId: Long, days: Int) {
        _cart.update { currentCart ->
            currentCart.map {
                if (it.book.id == bookId) {
                    it.copy(loanDays = days.coerceIn(1, 30))
                } else {
                    it
                }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _search.update { it.copy(query = newQuery) }
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(searchDebounceMillis)
            performSearchInternal(newQuery)
        }
    }

    fun performSearch() {
        performSearchInternal(_search.value.query)
    }

    private fun performSearchInternal(query: String) {
        viewModelScope.launch {
            _search.update { it.copy(isLoading = true, errorMsg = null, initialSearchPerformed = true) }
            try {
                val searchResults = bookRepository.searchBooks(query)
                _search.update { it.copy(isLoading = false, results = searchResults, errorMsg = null) }
            } catch (e: Exception) {
                _search.update { it.copy(isLoading = false, errorMsg = "Error al buscar: ${e.message}") }
            }
        }
    }

    fun clearSearchResults() {
        _search.update { SearchUiState() }
        searchDebounceJob?.cancel()
    }

    fun onLoginEmailChange(value: String) {
        val emailError = if (value.contains("@") && value.isNotBlank()) null else "Correo inválido"
        _login.update { it.copy(email = value, emailError = emailError) }
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
            val result = try {
                userRepository.login(s.email.trim(), s.pass)
            } catch (e: Exception) {
                _login.update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Error de login") }
                return@launch
            }

            if (result.isSuccess) {
                val userEmail = s.email.trim()
                userPreferencesRepository.saveUserEmail(userEmail)
                _login.update { it.copy(isSubmitting = false, success = true) }
            } else {
                _login.update {
                    it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message ?: "Credenciales inválidas")
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearAll()
            _login.update { LoginUiState() }
            _register.update { RegisterUiState() }
            clearSearchResults()
            _cart.value = emptyList()
        }
    }

    fun onRegisterProfileImageChange(uri: Uri?) {
        _register.update { it.copy(profileImageUri = uri?.toString()) }
    }

    fun onRegisterNameChange(value: String) {
        val error = if (value.isBlank()) "El nombre es obligatorio" else null
        _register.update { it.copy(name = value, nameError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        val error = if (value.contains("@") && value.isNotBlank()) null else "Formato de correo inválido"
        _register.update { it.copy(email = value, emailError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPhoneChange(value: String) {
        val error = if (value.isNotBlank() && value.all { it.isDigit() }) null else "Solo se permiten dígitos"
        _register.update { it.copy(phone = value, phoneError = error) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        val error = if (value.length >= 8) null else "Mínimo 8 caracteres"
        val confirmError = if (value != _register.value.confirm) "Las contraseñas no coinciden" else null
        _register.update { it.copy(pass = value, passError = error, confirmError = confirmError) }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterConfirmChange(value: String) {
        val pass = _register.value.pass
        val error = if (value == pass) null else "Las contraseñas no coinciden"
        _register.update { it.copy(confirm = value, confirmError = error) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val allFieldsValid = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        val noErrors = s.nameError == null && s.emailError == null && s.phoneError == null && s.passError == null && s.confirmError == null
        _register.update { it.copy(canSubmit = allFieldsValid && noErrors) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            val result = try {
                userRepository.register(s.name.trim(), s.email.trim(), s.phone.trim(), s.pass, s.profileImageUri)
            } catch (e: Exception) {
                _register.update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Error de registro") }
                return@launch
            }
            _register.update {
                if (result.isSuccess) it.copy(isSubmitting = false, success = true)
                else it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    fun registerLoan(userId: Long, bookId: Long, loanDate: String, dueDate: String) {
        viewModelScope.launch {
            val bookResult = runCatching { bookRepository.getBookById(bookId) }
            val currentBook = bookResult.getOrNull()

            if (currentBook == null || currentBook.status != "Available") {
                _home.update { it.copy(errorMsg = "Error: Libro no disponible para préstamo.") }
                return@launch
            }

            try {
                val newLoan = LoanEntity(userId = userId, bookId = bookId, loanDate = loanDate, dueDate = dueDate, returnDate = null, status = "Active")
                loanRepository.insert(newLoan)

                val updatedBook = currentBook.copy(status = "Loaned")
                bookRepository.update(updatedBook)

            } catch (e: Exception) {
                _home.update { it.copy(errorMsg = "Error al registrar el préstamo: ${e.message}") }
            }
        }
    }

    fun onUpdateUserProfileImageChange(uri: Uri?) {
        _updateUserState.update { it.copy(profileImageUri = uri?.toString()) }
    }

    fun onUpdateUserNameChange(name: String) {
        _updateUserState.update { it.copy(name = name) }
    }

    fun onUpdateUserEmailChange(email: String) {
        _updateUserState.update { it.copy(email = email) }
    }

    fun onUpdateUserPhoneChange(phone: String) {
        _updateUserState.update { it.copy(phone = phone) }
    }

    fun loadCurrentUserData() {
        _user.value?.let { user ->
            _updateUserState.update {
                it.copy(
                    userId = formatUserId(user.id, user.email),
                    name = user.name ?: "",
                    email = user.email ?: "",
                    phone = user.phone ?: "",
                    profileImageUri = user.profilePictureUri // Cargar la foto de perfil
                )
            }
        }
    }

    private fun formatUserId(id: Long, email: String): String {
        val lettersSource = kotlin.math.abs(email.hashCode().toLong()).toString(36)
        val letters = lettersSource.filter { it.isLetter() }.take(5).uppercase().padEnd(5, 'X')
        val lastDigit = id.toString().last()
        return "$letters$lastDigit"
    }

    fun updateUser() {
        viewModelScope.launch {
            _updateUserState.update { it.copy(isSubmitting = true) }
            try {
                val currentUser = _user.value
                val state = _updateUserState.value
                val updatedUser = currentUser?.copy(
                    name = state.name,
                    email = state.email, 
                    phone = state.phone,
                    profilePictureUri = state.profileImageUri // Guardar la foto de perfil
                )
                if (updatedUser != null) {
                    userRepository.updateUser(updatedUser)
                    _user.value = updatedUser
                    _updateUserState.update { it.copy(isSubmitting = false, success = true) }
                } else {
                    _updateUserState.update { it.copy(isSubmitting = false, errorMsg = "Usuario no encontrado") }
                }
            } catch (e: Exception) {
                _updateUserState.update { it.copy(isSubmitting = false, errorMsg = e.message) }
            }
        }
    }

    fun clearUpdateUserState() {
        _updateUserState.update { it.copy(success = false, errorMsg = null) }
    }

    fun onVerificationCodeChange(code: String) {
        _updateUserState.update { it.copy(verificationCode = code, verificationError = null) }
    }

    fun initiateEmailUpdate() {
        _updateUserState.update { it.copy(showVerificationDialog = true, verificationCode = "", verificationError = null) }
    }

    fun confirmEmailUpdate() {
        viewModelScope.launch {
            _updateUserState.update { it.copy(isVerifying = true, verificationError = null) }
            delay(1000) 
            if (_updateUserState.value.verificationCode.isNotBlank()) {
                _updateUserState.update { it.copy(isVerifying = false, showVerificationDialog = false) }
                updateUser()
            } else {
                _updateUserState.update { it.copy(isVerifying = false, verificationError = "El código no puede estar vacío.") }
            }
        }
    }

    fun cancelEmailUpdate() {
        _updateUserState.update { it.copy(showVerificationDialog = false, verificationError = null, verificationCode = "") }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
}
