package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.LoanDao
import com.empresa.libra_users.data.local.user.LoanEntity

class LoanRepository(private val loanDao: LoanDao) {
    suspend fun insert(loan: LoanEntity): Long = loanDao.insert(loan)
    suspend fun getAllLoans(): List<LoanEntity> = loanDao.getAllLoans()
    suspend fun getLoanById(id: Long) = loanDao.getLoanById(id)
    suspend fun getLoansByUser(userId: Long): List<LoanEntity> = loanDao.getLoansByUser(userId)
    suspend fun update(loan: LoanEntity) = loanDao.update(loan)
    suspend fun countActiveLoans(): Int = loanDao.countActiveLoans()

    /**
     * Counts all loans. For the admin dashboard.
     */
    suspend fun countAllLoans(): Int {
        return loanDao.countAllLoans()
    }
}
