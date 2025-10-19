package com.empresa.libra_users.data.local.user

import androidx.room.*

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    // -------------------------------------------------------------------
    // MÉTODOS DE CONSULTA Y LECTURA (READ QUERIES)
    // -------------------------------------------------------------------

    @Query("SELECT * FROM books ORDER BY id ASC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentBooks(limit: Int): List<BookEntity>

    @Query("SELECT * FROM books WHERE categoryId = :categoryId")
    suspend fun getBooksByCategoryId(categoryId: Long): List<BookEntity>

    @Query("SELECT * FROM books WHERE author LIKE '%' || :authorTag || '%'")
    suspend fun getBooksByAuthorTag(authorTag: String): List<BookEntity>

    // AÑADIDO: CONSULTA PARA LA FUNCIÓN DE BÚSQUEDA
    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    suspend fun searchBooks(query: String): List<BookEntity>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int
}