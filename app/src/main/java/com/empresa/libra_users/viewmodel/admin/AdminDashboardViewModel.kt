package com.empresa.libra_users.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Data class representing the state for the admin dashboard home screen.
 */
data class AdminDashboardUiState(
    val totalBooks: Int = 0,
    val totalUsers: Int = 0,
    val pendingLoans: Int = 0,
    val totalLoans: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Data class representing the state for the book management screen.
 */
data class AdminBooksUiState(
    val books: List<BookEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _dashboardUiState = MutableStateFlow(AdminDashboardUiState())
    val dashboardUiState: StateFlow<AdminDashboardUiState> = _dashboardUiState.asStateFlow()

    private val _booksUiState = MutableStateFlow(AdminBooksUiState())
    val booksUiState: StateFlow<AdminBooksUiState> = _booksUiState.asStateFlow()

    init {
        loadDashboardTotals()
        loadBooks()
    }

    /**
     * Loads the total counts from the repositories and updates the UI state.
     */
    fun loadDashboardTotals() {
        _dashboardUiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val totalBooks = bookRepository.count()
                val totalUsers = userRepository.countUsers()
                val pendingLoans = loanRepository.countActiveLoans()
                val totalLoans = loanRepository.countAllLoans()

                _dashboardUiState.update {
                    it.copy(
                        totalBooks = totalBooks,
                        totalUsers = totalUsers,
                        pendingLoans = pendingLoans,
                        totalLoans = totalLoans,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _dashboardUiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar los totales."
                    )
                }
            }
        }
    }

    /**
     * Loads the list of books from the repository and updates the book management UI state.
     */
    fun loadBooks() {
        _booksUiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Assuming getAllBooks is a suspend function returning a List
                val books = bookRepository.getAllBooks()
                _booksUiState.update {
                    it.copy(
                        books = books,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _booksUiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar los libros."
                    )
                }
            }
        }
    }

    /**
     * Adds a new book to the database and refreshes the book list.
     */
    fun addBook(
        title: String,
        author: String,
        isbn: String = "N/A",
        publisher: String = "N/A",
        categoryId: Int = 1
    ) {
        viewModelScope.launch {
            _booksUiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Campos obligatorios reales
                val publishDateMillis = System.currentTimeMillis()
                val inventoryCode = "INV-${publishDateMillis.toString().takeLast(6)}"
                val publishDateStr = Instant.ofEpochMilli(publishDateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE)

                val newBook = BookEntity(
                    title = title,
                    author = author,
                    categoryId = categoryId.toLong(),
                    isbn = isbn,
                    publisher = publisher,
                    publishDate = publishDateStr,
                    inventoryCode = inventoryCode,
                    status = "Available"
                )

                bookRepository.insert(newBook)
                loadBooks()  // refrescar lista tras la inserción
            } catch (_: Exception) {
                _booksUiState.update { it.copy(isLoading = false, error = "Error al añadir el libro.") }
            }
        }
    }

    /**
     * Deletes a book from the database and refreshes the book list.
     */
    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            _booksUiState.update { it.copy(isLoading = true) }
            try {
                bookRepository.delete(book)
                loadBooks() // Refresh the list after deletion
            } catch (_: Exception) {
                _booksUiState.update {
                    it.copy(isLoading = false, error = "Error al eliminar el libro.")
                }
            }
        }
    }
}
