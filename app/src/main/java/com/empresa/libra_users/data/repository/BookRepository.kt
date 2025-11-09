package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(private val bookDao: BookDao) {
    suspend fun insert(book: BookEntity): Long = bookDao.insert(book)

    fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()

    suspend fun update(book: BookEntity) = bookDao.update(book)

    suspend fun delete(book: BookEntity) = bookDao.delete(book)

    suspend fun count(): Int = bookDao.count()

    suspend fun getBookById(id: Long): BookEntity? {
        return bookDao.getBookById(id)
    }

    suspend fun searchBooks(query: String): List<BookEntity> {
        return bookDao.searchBooks(query)
    }

    suspend fun searchBooksWithFilters(query: String, categoria: String?, soloDisponibles: Boolean): List<BookEntity> {
        return bookDao.searchBooksWithFilters(query, categoria, if (soloDisponibles) 1 else 0)
    }

    suspend fun getBooksByCategory(categoria: String): List<BookEntity> {
        return bookDao.getBooksByCategory(categoria)
    }

    suspend fun getAvailableBooks(): List<BookEntity> {
        return bookDao.getAvailableBooks()
    }

    suspend fun countActiveLoansForBook(bookId: Long): Int {
        return bookDao.countActiveLoansForBook(bookId)
    }
}
