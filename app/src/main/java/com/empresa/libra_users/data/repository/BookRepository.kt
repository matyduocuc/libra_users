package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.BookDao
import com.empresa.libra_users.data.local.user.BookEntity

class BookRepository(private val bookDao: BookDao) {
    suspend fun insert(book: BookEntity): Long = bookDao.insert(book)
    suspend fun getAllBooks(): List<BookEntity> = bookDao.getAllBooks()
    suspend fun getBookById(id: Long) = bookDao.getBookById(id)
    suspend fun update(book: BookEntity) = bookDao.update(book)
    suspend fun delete(book: BookEntity) = bookDao.delete(book)
    suspend fun count(): Int = bookDao.count()
}

