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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LibraAppRoot() }
    }
}

@Composable
fun LibraAppRoot() {
    // DB
    val context = LocalContext.current.applicationContext
    val db = AppDatabase.getInstance(context)

    // Repos
    val userRepository = UserRepository(db.userDao())
    val bookRepository = BookRepository(db.bookDao())
    val loanRepository = LoanRepository(db.loanDao())
    val notificationRepository = NotificationRepository(db.notificationDao())

    // VM principal con Factory
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            userRepository = userRepository,
            bookRepository = bookRepository,
            loanRepository = loanRepository,
            notificationRepository = notificationRepository
        )
    )

    val navController = rememberNavController()

    MaterialTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(
                navController = navController,
                mainViewModel = mainViewModel   // <- pasamos el VM correcto
            )
        }
    }
}
