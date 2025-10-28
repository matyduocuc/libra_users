package com.empresa.libra_users.data.local.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Insert
    suspend fun insert(loan: LoanEntity): Long

    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    fun getAllLoansFlow(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans ORDER BY loanDate DESC")
    suspend fun getAllLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getLoanById(id: Long): LoanEntity?

    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY loanDate DESC")
    fun getLoansByUser(userId: Long): Flow<List<LoanEntity>>

    @Update
    suspend fun update(loan: LoanEntity)

    @Query("SELECT COUNT(*) FROM loans WHERE status = 'Active'")
    suspend fun countActiveLoans(): Int

    @Query("SELECT COUNT(*) FROM loans")
    suspend fun countAllLoans(): Int
}
