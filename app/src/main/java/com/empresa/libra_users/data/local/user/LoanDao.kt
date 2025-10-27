package com.empresa.libra_users.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LoanDao {

    // Insertar un préstamo
    @Insert
    suspend fun insert(loan: LoanEntity): Long

    // Obtener todos los préstamos
    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    suspend fun getAllLoans(): List<LoanEntity>

    // Obtener un préstamo por su ID
    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Long): LoanEntity?

    // Obtener préstamos de un usuario
    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY loanDate DESC")
    suspend fun getLoansByUser(userId: Long): List<LoanEntity>

    // Actualizar un préstamo (por ejemplo, cuando se devuelve un libro)
    @Update
    suspend fun update(loan: LoanEntity)

    // Contar los préstamos activos
    @Query("SELECT COUNT(*) FROM loans WHERE status = 'Active'")
    suspend fun countActiveLoans(): Int

    /**
     * Counts all loans in the loans table.
     * Added for the admin dashboard as per specific requirements.
     */
    @Query("SELECT COUNT(*) FROM loans")
    suspend fun countAllLoans(): Int
}
