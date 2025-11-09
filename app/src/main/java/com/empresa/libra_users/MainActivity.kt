package com.empresa.libra_users

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.navigation.AppNavigation
import com.empresa.libra_users.ui.theme.LibrausersTheme
import com.empresa.libra_users.ui.theme.appBackground
import com.empresa.libra_users.util.RequestPermissions
import com.empresa.libra_users.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            LibraAppRoot(windowSizeClass = windowSizeClass)
        }
    }
}

@Composable
fun LibraAppRoot(windowSizeClass: WindowSizeClass) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val isDarkMode by mainViewModel.isDarkMode.collectAsStateWithLifecycle()

    LibrausersTheme(darkTheme = isDarkMode) {
        RequestPermissions {
            Surface(
                modifier = Modifier.fillMaxSize().appBackground(darkTheme = isDarkMode),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f) // Un poco de transparencia para dejar ver el fondo
            ) {
                AppNavigation(
                    vm = mainViewModel,
                    windowSizeClass = windowSizeClass
                )
            }
        }
    }
}
