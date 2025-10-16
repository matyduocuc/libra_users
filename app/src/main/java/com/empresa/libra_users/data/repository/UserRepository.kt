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

    // Update User (cambiar contraseña o algún detalle)
    suspend fun updateUser(user: UserEntity): Result<Boolean> {
        val existingUser = userDao.getById(user.id)
        return if (existingUser != null) {
            userDao.update(user)  // Actualiza el usuario con los nuevos datos
            Result.success(true)
        } else {
            Result.failure(IllegalArgumentException("Usuario no encontrado"))
        }
    }

    // Delete User (eliminar un usuario)
    suspend fun deleteUser(user: UserEntity): Result<Boolean> {
        val existingUser = userDao.getById(user.id)
        return if (existingUser != null) {
            userDao.delete(user)  // Elimina el usuario de la base de datos
            Result.success(true)
        } else {
            Result.failure(IllegalArgumentException("Usuario no encontrado"))
        }
    }
}
