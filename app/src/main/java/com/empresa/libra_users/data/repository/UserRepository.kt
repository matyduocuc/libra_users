package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.local.user.UserEntity

class UserRepository(
    private val userDao: UserDao
) {
    // Login
    suspend fun login(email: String, pass: String): Result<String> {
        // Check for admin credentials first
        if (email.equals("admin123@gmail.com", ignoreCase = true) && pass == "admin12345678") {
            return Result.success("ADMIN")
        }

        val user = userDao.getByEmail(email)
        return if (user != null && user.password == pass) {
            Result.success("USER") // It's a regular user
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    // Register
    suspend fun register(name: String, email: String, phone: String, pass: String, profilePictureUri: String?): Result<Long> {
        if (email.equals("admin123@gmail.com", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Este correo no se puede registrar."))
        }

        val exists = userDao.getByEmail(email) != null
        if (exists) {
            return Result.failure(IllegalArgumentException("Correo ya existente"))
        } else {
            val id = userDao.insert(
                UserEntity(
                    name = name,
                    email = email,
                    phone = phone,
                    password = pass,
                    profilePictureUri = profilePictureUri // Guardamos la foto
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

    /**
     * Counts all users. For the admin dashboard.
     */
    suspend fun countUsers(): Int {
        return userDao.countUsers()
    }
}
