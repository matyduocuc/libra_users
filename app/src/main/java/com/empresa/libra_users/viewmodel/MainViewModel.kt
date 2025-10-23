package com.empresa.libra_users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

data class HomeUiState(
    val currentCoverIndex: Int = 0,
    val coverImages: List<String> = listOf(
        "https://ejemplo.com/cover1.jpg",
        "https://ejemplo.com/cover2.jpg",
        "https://ejemplo.com/cover3.jpg"
    ),
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
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

class MainViewModel(
    private val userRepository: UserRepository,
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // Estados de UI
    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register.asStateFlow()

    private val _home = MutableStateFlow(HomeUiState())
    val home: StateFlow<HomeUiState> = _home.asStateFlow()

    private val _search = MutableStateFlow(SearchUiState())
    val search: StateFlow<SearchUiState> = _search.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    private val _updateUserState = MutableStateFlow(UpdateUserUiState())
    val updateUserState: StateFlow<UpdateUserUiState> = _updateUserState.asStateFlow()

    private var searchDebounceJob: Job? = null
    private val searchDebounceMillis = 500L

    init {
        startCoverRotation()
        loadCategorizedBooks()
    }

    private fun startCoverRotation() {
        if (_home.value.coverImages.isEmpty()) return
        viewModelScope.launch {
            while (true) {
                delay(5000)
                _home.update {
                    it.copy(currentCoverIndex = (it.currentCoverIndex + 1) % it.coverImages.size)
                }
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

    fun onSearchQueryChange(newQuery: String) {
        _search.update { it.copy(query = newQuery) }

        if (newQuery.isBlank()) {
            _search.update { it.copy(results = emptyList(), errorMsg = null, isLoading = false) }
            searchDebounceJob?.cancel()
            return
        }

        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(searchDebounceMillis)
            performSearchInternal(_search.value.query)
        }
    }

    fun performSearch() {
        val currentQuery = _search.value.query.trim()
        if (currentQuery.isBlank()) {
            _search.update { it.copy(results = emptyList(), errorMsg = null, isLoading = false) }
            return
        }
        searchDebounceJob?.cancel()
        performSearchInternal(currentQuery)
    }

    private fun performSearchInternal(query: String) {
        viewModelScope.launch {
            _search.update { it.copy(isLoading = true, errorMsg = null, initialSearchPerformed = true) }
            try {
                val searchResults = try {
                    bookRepository.searchBooks(query) ?: emptyList()
                } catch (e: Exception) {
                    _search.update { it.copy(isLoading = false, errorMsg = "Error al buscar: ${e.message}") }
                    return@launch
                }
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
            _login.update {
                if (result.isSuccess) it.copy(isSubmitting = false, success = true)
                else it.copy(isSubmitting = false, errorMsg = result.exceptionOrNull()?.message ?: "Credenciales inválidas")
            }
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _user.value = userRepository.getUserByEmail(s.email.trim())
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

    fun logout() {
        _isLoggedIn.value = false
        _user.value = null
        _login.update { LoginUiState() }
        clearSearchResults()
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
                userRepository.register(s.name.trim(), s.email.trim(), s.phone.trim(), s.pass)
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
        _user.value?.let {
            _updateUserState.update {
                it.copy(
                    name = _user.value?.name ?: "",
                    email = _user.value?.email ?: "",
                    phone = _user.value?.phone ?: ""
                )
            }
        }
    }

    fun updateUser() {
        viewModelScope.launch {
            _updateUserState.update { it.copy(isSubmitting = true) }
            try {
                val updatedUser = _user.value?.copy(
                    name = _updateUserState.value.name,
                    email = _updateUserState.value.email,
                    phone = _updateUserState.value.phone
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
}
