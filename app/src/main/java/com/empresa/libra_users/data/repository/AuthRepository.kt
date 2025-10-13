package com.empresa.libra_users.data.repository

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

// Modelo de datos para el usuario de demostración.
// Podría vivir en su propio paquete (ej. data.model) si el proyecto crece.
data class DemoUser(
    val name: String,
    val email: String,
    val phone: String,
    val pass: String
)

/**
 * Repositorio encargado de gestionar la autenticación de usuarios.
 * En una app real, aquí se harían las llamadas a una API (con Retrofit)
 * o a una base de datos local (con Room).
 *
 * @Inject constructor() le dice a Hilt cómo crear una instancia de este repositorio.
 * @Singleton asegura que solo existirá una instancia en toda la aplicación.
 */
@Singleton
class AuthRepository @Inject constructor() {

    // Simulación de una tabla de usuarios en una base de datos.
    // En una app real, esta lista no existiría; los datos se obtendrían de una fuente externa.
    private val users = mutableListOf(
        DemoUser(name = "Demo User", email = "demo@duoc.cl", phone = "912345678", pass = "Demo123!")
    )

    /**
     * Simula el proceso de inicio de sesión.
     * @param email El correo del usuario.
     * @param pass La contraseña del usuario.
     * @return Result<Unit> que indica éxito (Success) o fracaso (Failure) con un mensaje.
     */
    suspend fun login(email: String, pass: String): Result<Unit> {
        delay(800) // Simular la latencia de una llamada de red.
        val user = users.firstOrNull { it.email.equals(email, ignoreCase = true) }

        return if (user != null && user.pass == pass) {
            Result.success(Unit) // Éxito: las credenciales son correctas.
        } else {
            Result.failure(Exception("Credenciales inválidas. Inténtalo de nuevo.")) // Fracaso.
        }
    }

    /**
     * Simula el registro de un nuevo usuario.
     * @param newUser El objeto DemoUser con los datos del nuevo usuario.
     * @return Result<Unit> que indica éxito o fracaso si el usuario ya existe.
     */
    suspend fun register(newUser: DemoUser): Result<Unit> {
        delay(1200) // Simular una latencia de red un poco más larga para el registro.
        val isDuplicated = users.any { it.email.equals(newUser.email, ignoreCase = true) }

        return if (isDuplicated) {
            Result.failure(Exception("El correo electrónico ya está en uso por otro usuario.")) // Fracaso.
        } else {
            users.add(newUser)
            println("Usuario registrado: ${newUser.email}. Total de usuarios: ${users.size}") // Log para depuración.
            Result.success(Unit) // Éxito.
        }
    }
}

