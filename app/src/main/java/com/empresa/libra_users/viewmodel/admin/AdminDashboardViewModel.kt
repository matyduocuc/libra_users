package com.empresa.libra_users.viewmodel.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.empresa.libra_users.data.local.user.BookEntity
import com.empresa.libra_users.data.local.user.LoanEntity
import com.empresa.libra_users.data.local.user.UserEntity
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// --- Data classes para los estados de la UI ---
data class AdminDashboardUiState(val totalBooks: Int = 0, val totalUsers: Int = 0, val pendingLoans: Int = 0, val totalLoans: Int = 0, val isLoading: Boolean = true, val error: String? = null)
data class AdminBooksUiState(val books: List<BookEntity> = emptyList(), val isLoading: Boolean = true, val error: String? = null)
data class AdminUsersUiState(val users: List<UserEntity> = emptyList(), val isLoading: Boolean = true, val error: String? = null)
data class LoanDetails(val loan: LoanEntity, val book: BookEntity?, val user: UserEntity?)
data class AdminLoansUiState(val loans: List<LoanDetails> = emptyList(), val isLoading: Boolean = true, val error: String? = null)
data class BookLoanStats(val book: BookEntity, val loanCount: Int)
data class UserLoanStats(val user: UserEntity, val loanCount: Int)
data class LibraryStatus(val available: Int, val loaned: Int, val damaged: Int)
data class AdminReportsUiState(val topBooks: List<BookLoanStats> = emptyList(), val topUsers: List<UserLoanStats> = emptyList(), val libraryStatus: LibraryStatus = LibraryStatus(0, 0, 0), val isLoading: Boolean = true, val error: String? = null)

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

    private val _usersUiState = MutableStateFlow(AdminUsersUiState())
    val usersUiState: StateFlow<AdminUsersUiState> = _usersUiState.asStateFlow()

    private val _loansUiState = MutableStateFlow(AdminLoansUiState())
    val loansUiState: StateFlow<AdminLoansUiState> = _loansUiState.asStateFlow()

    private val _reportsUiState = MutableStateFlow(AdminReportsUiState())
    val reportsUiState: StateFlow<AdminReportsUiState> = _reportsUiState.asStateFlow()

    init {
        loadDashboardTotals()
        loadBooks()
        loadUsers()
        loadLoanDetails()
        loadReports()
    }

    fun loadDashboardTotals() {
        viewModelScope.launch {
            _dashboardUiState.update { it.copy(isLoading = true) }
            try {
                _dashboardUiState.update {
                    it.copy(totalBooks = bookRepository.count(), totalUsers = userRepository.countUsers(), pendingLoans = loanRepository.countActiveLoans(), totalLoans = loanRepository.countAllLoans(), isLoading = false)
                }
            } catch (e: Exception) {
                _dashboardUiState.update { it.copy(isLoading = false, error = "Error al cargar totales: ${e.message}") }
            }
        }
    }

    fun loadBooks() {
        bookRepository.getAllBooks()
            .onStart { _booksUiState.update { it.copy(isLoading = true) } }
            .catch { e -> _booksUiState.update { it.copy(isLoading = false, error = e.message) } }
            .onEach { books -> _booksUiState.update { it.copy(isLoading = false, books = books) } }
            .launchIn(viewModelScope)
    }

    fun loadUsers() {
        userRepository.getUsers()
            .onStart { _usersUiState.update { it.copy(isLoading = true) } }
            .catch { e -> _usersUiState.update { it.copy(isLoading = false, error = e.message) } }
            .onEach { users -> _usersUiState.update { it.copy(isLoading = false, users = users) } }
            .launchIn(viewModelScope)
    }

    fun loadLoanDetails() {
        viewModelScope.launch {
            loanRepository.getAllLoansFlow()
                .onStart { _loansUiState.update { it.copy(isLoading = true) } }
                .catch { e -> _loansUiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { loans ->
                    val details = loans.map { loan ->
                        LoanDetails(loan = loan, book = bookRepository.getBookById(loan.bookId), user = userRepository.getUserById(loan.userId))
                    }
                    _loansUiState.update { it.copy(isLoading = false, loans = details) }
                }
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _reportsUiState.update { it.copy(isLoading = true) }
            try {
                val allLoans = loanRepository.getAllLoans()
                val allBooks = bookRepository.getAllBooks().first()
                val allUsers = userRepository.getUsers().first()

                val topBooks = allLoans.groupBy { it.bookId }.mapNotNull { (bookId, loans) -> allBooks.find { it.id == bookId }?.let { BookLoanStats(it, loans.size) } }.sortedByDescending { it.loanCount }.take(5)
                val topUsers = allLoans.groupBy { it.userId }.mapNotNull { (userId, loans) -> allUsers.find { it.id == userId }?.let { UserLoanStats(it, loans.size) } }.sortedByDescending { it.loanCount }.take(5)
                val libraryStatus = LibraryStatus(available = allBooks.count { it.status == "Available" }, loaned = allBooks.count { it.status == "Loaned" }, damaged = allBooks.count { it.status == "Damaged" })

                _reportsUiState.update { it.copy(isLoading = false, topBooks = topBooks, topUsers = topUsers, libraryStatus = libraryStatus) }
            } catch (e: Exception) {
                _reportsUiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun getLoansWithDetailsForUser(userId: Long): Flow<List<LoanDetails>> {
        return loanRepository.getLoansByUser(userId).map { loans ->
            loans.mapNotNull { loan -> bookRepository.getBookById(loan.bookId)?.let { LoanDetails(loan = loan, book = it, user = null) } }
        }
    }

    fun updateUser(user: UserEntity) = viewModelScope.launch { userRepository.updateUser(user) }

    fun addBook(title: String, author: String, coverUrl: String, isbn: String, publisher: String, categoryId: Int, homeSection: String) = viewModelScope.launch {
        val newBook = BookEntity(title = title, author = author, coverUrl = coverUrl, categoryId = categoryId.toLong(), isbn = isbn, publisher = publisher, homeSection = homeSection, publishDate = "N/A", inventoryCode = "N/A", status = "Available")
        bookRepository.insert(newBook)
    }

    fun deleteBook(book: BookEntity) = viewModelScope.launch { bookRepository.delete(book) }

    fun markLoanAsReturned(loan: LoanEntity) {
        viewModelScope.launch {
            val updatedLoan = loan.copy(status = "Returned", returnDate = java.time.LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            loanRepository.update(updatedLoan)
            bookRepository.getBookById(loan.bookId)?.let { bookRepository.update(it.copy(status = "Available")) }
        }
    }
}
