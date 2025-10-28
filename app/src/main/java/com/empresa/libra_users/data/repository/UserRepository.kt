package com.empresa.libra_users.data.repository

import com.empresa.libra_users.data.local.user.UserDao
import com.empresa.libra_users.data.local.user.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun login(email: String, pass: String): Result<String> {
        if (email.equals("admin123@gmail.com", ignoreCase = true) && pass == "admin12345678") {
            return Result.success("ADMIN")
        }
        val user = userDao.getByEmail(email)
        if (user != null && user.status == "blocked") {
            return Result.failure(IllegalArgumentException("Tu cuenta ha sido bloqueada."))
        }
        return if (user != null && user.password == pass) {
            Result.success("USER")
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    suspend fun register(name: String, email: String, phone: String, pass: String, profilePictureUri: String?): Result<Long> {
        if (email.equals("admin123@gmail.com", ignoreCase = true)) {
            return Result.failure(IllegalArgumentException("Este correo no se puede registrar."))
        }
        if (userDao.getByEmail(email) != null) {
            return Result.failure(IllegalArgumentException("Correo ya existente"))
        }
        val id = userDao.insert(UserEntity(name = name, email = email, phone = phone, password = pass, profilePictureUri = profilePictureUri))
        return Result.success(id)
    }

    fun getUsers(): Flow<List<UserEntity>> = userDao.getAll()

    suspend fun getUserById(id: Long): UserEntity? = userDao.getById(id)

    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getByEmail(email)

    suspend fun updateUser(user: UserEntity) = userDao.update(user)

    suspend fun countUsers(): Int = userDao.countUsers()
}
