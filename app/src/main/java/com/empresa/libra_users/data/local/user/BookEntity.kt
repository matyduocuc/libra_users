package com.empresa.libra_users.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val author: String,
    val isbn: String,
    val categoryId: Long, // Relación con categoría (usado para filtrar en el catálogo)
    val publisher: String,
    val publishDate: String,
    val status: String,  // 'Available', 'Loaned', 'Damaged', 'Retired'
    val inventoryCode: String,

    // AÑADIDO: Campo esencial para mostrar la portada en HomeScreen
    val coverUrl: String = ""
)