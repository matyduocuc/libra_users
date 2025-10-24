package com.empresa.libra_users

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.navigation.AppNavigation
import com.empresa.libra_users.ui.theme.LibrausersTheme
import com.empresa.libra_users.util.RequestPermissions
import com.empresa.libra_users.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LibraAppRoot() }
    }
}

@Composable
fun LibraAppRoot() {
    // El ViewModel ahora es obtenido directamente a través de Hilt
    val mainViewModel: MainViewModel = hiltViewModel()

    val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

    LibrausersTheme(darkTheme = isDarkMode) {
        RequestPermissions {
            Surface(color = MaterialTheme.colorScheme.background) {
                AppNavigation(vm = mainViewModel)
            }
        }
    }
}
