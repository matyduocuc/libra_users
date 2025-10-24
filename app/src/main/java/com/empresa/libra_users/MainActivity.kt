package com.empresa.libra_users

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.empresa.libra_users.data.local.database.AppDatabase
import com.empresa.libra_users.data.repository.BookRepository
import com.empresa.libra_users.data.repository.LoanRepository
import com.empresa.libra_users.data.repository.NotificationRepository
import com.empresa.libra_users.data.repository.UserRepository
import com.empresa.libra_users.navigation.AppNavigation
import com.empresa.libra_users.ui.theme.LibrausersTheme
import com.empresa.libra_users.util.RequestPermissions
import com.empresa.libra_users.viewmodel.MainViewModel
import com.empresa.libra_users.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LibraAppRoot() }
    }
}

@Composable
fun LibraAppRoot() {
    val context = LocalContext.current.applicationContext
    val db = AppDatabase.getInstance(context)

    // Creación de Repositorios
    val userRepository = UserRepository(db.userDao())
    val bookRepository = BookRepository(db.bookDao())
    val loanRepository = LoanRepository(db.loanDao())
    val notificationRepository = NotificationRepository(db.notificationDao())

    // Creación del ViewModel con Factory
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            userRepository = userRepository,
            bookRepository = bookRepository,
            loanRepository = loanRepository,
            notificationRepository = notificationRepository
        )
    )

    // Obtenemos el estado del modo oscuro del ViewModel
    val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

    // CAMBIO: LibrausersTheme ahora envuelve toda la app
    // Se le pasa el estado del modo oscuro para que se aplique globalmente
    LibrausersTheme(darkTheme = isDarkMode) {
        RequestPermissions {
            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavigation(vm = mainViewModel)
            }
        }
    }
}
