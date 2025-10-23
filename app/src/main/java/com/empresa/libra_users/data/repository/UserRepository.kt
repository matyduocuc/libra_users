package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.local.user.UserEntity

class UserRepository(
    private val userDao: UserDao
) {
    // Login
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)
        return if (user != null && user.password == pass) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    // Register
    suspend fun register(name: String, email: String, phone: String, pass: String): Result<Long> {
        val exists = userDao.getByEmail(email) != null
        if (exists) {
            return Result.failure(IllegalArgumentException("Correo ya existente"))
        } else {
            val id = userDao.insert(
                UserEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    password = pass
                )
            )
            return Result.success(id)
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getByEmail(email)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }
}