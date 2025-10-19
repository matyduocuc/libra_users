package com.empresa.libra_users

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

import com.empresa.libra_users.data.local.database.AppDatabase
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import com.empresa.libra_users.navigation.AppNavGraph
import com.empresa.libra_users.viewmodel.MainViewModel
import com.empresa.libra_users.viewmodel.MainViewModelFactory
import com.empresa.libra_users.ui.theme.LibrausersTheme // Asumiendo que existe

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LibraAppRoot() }
    }
}

@Composable
fun LibraAppRoot() {
    // 1. Configuración de la Base de Datos (Singleton)
    val context = LocalContext.current.applicationContext
    // Asegúrate de que AppDatabase.getInstance(context) llama a tu clase de Room
    val db = AppDatabase.getInstance(context)

    // 2. Creación de Repositorios (Inyección Manual)
    val userRepository = UserRepository(db.userDao())
    val bookRepository = BookRepository(db.bookDao())
    val loanRepository = LoanRepository(db.loanDao())
    val notificationRepository = NotificationRepository(db.notificationDao()) // Asumiendo que constructor existe

    // 3. Creación del ViewModel con Factory
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            userRepository = userRepository,
            bookRepository = bookRepository,
            loanRepository = loanRepository,
            notificationRepository = notificationRepository
        )
    )

    val navController = rememberNavController()

    LibrausersTheme { // Uso de tu tema
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(
                navController = navController,
                mainViewModel = mainViewModel
            )
        }
    }
}