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

    // -------------------------------------------------------------------
    // FUNCIÓN FINAL Y CORREGIDA PARA EL DISEÑO DE CATÁLOGO
    // -------------------------------------------------------------------

    suspend fun getCategorizedBooks(): Map<String, List<BookEntity>> {
        val CUENTOS_CHILENOS_ID = 10L
        val NARRATIVA_LATINA_ID = 20L
        val AUTHOR_TAG = "chilena"

        return mapOf(
            "Recién llegados" to bookDao.getRecentBooks(limit = 6),
            "Escritoras chilenas" to bookDao.getBooksByAuthorTag(authorTag = AUTHOR_TAG),
            "Cuentos chilenos" to bookDao.getBooksByCategoryId(categoryId = CUENTOS_CHILENOS_ID),
            "Nueva narrativa latinoamericana" to bookDao.getBooksByCategoryId(categoryId = NARRATIVA_LATINA_ID)
        )
    }

    // AÑADIDO: FUNCIÓN PARA LA PANTALLA DE BÚSQUEDA
    suspend fun searchBooks(query: String): List<BookEntity> {
        return bookDao.searchBooks(query) // Llama al nuevo método del DAO
    }
}