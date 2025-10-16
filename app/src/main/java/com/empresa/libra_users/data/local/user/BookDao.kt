package com.empresa.libra_users.data.local.user

import androidx.room.*

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(book: BookEntity): Long

    @Query("SELECT * FROM books ORDER BY id ASC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Update
    suspend fun update(book: BookEntity)

    @Delete
    suspend fun delete(book: BookEntity)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int
}