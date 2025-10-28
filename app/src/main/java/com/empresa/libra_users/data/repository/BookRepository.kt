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
}
