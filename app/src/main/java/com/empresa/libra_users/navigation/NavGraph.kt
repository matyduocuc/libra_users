@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.empresa.libra_users.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.empresa.libra_users.screen.HomeScreen
import com.empresa.libra_users.screen.LoginScreen
import com.empresa.libra_users.screen.RegisterScreen
import com.empresa.libra_users.ui.theme.components.AppDrawer
import com.empresa.libra_users.ui.theme.components.authenticatedDrawerItems
import com.empresa.libra_users.viewmodel.AuthState
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        AuthState.AUTHENTICATED -> {
            AuthenticatedView(navController = navController, vm = mainViewModel)
        }
        AuthState.UNAUTHENTICATED -> {
            UnauthenticatedView(navController = navController, vm = mainViewModel)
        }
    }
}

@Composable
private fun AuthenticatedView(navController: NavHostController, vm: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val routeNow = navController.currentBackStackEntryAsState().value?.destination?.route
    val isDarkMode by vm.isDarkMode.collectAsStateWithLifecycle()

    val onLogout = { vm.logout() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = routeNow,
                isDarkMode = isDarkMode,
                onToggleDarkMode = { vm.toggleDarkMode() },
                items = authenticatedDrawerItems(
                    onHome = { scope.launch { drawerState.close() }; navController.navigate("home") { launchSingleTop = true } },
                    onLogout = { scope.launch { drawerState.close() }; onLogout() },
                    onCatalog = { scope.launch { drawerState.close() } },
                    onNews = { scope.launch { drawerState.close() } },
                    onAccountSettings = { scope.launch { drawerState.close() } }
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Libra Users") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("home") }) {
                            Icon(Icons.Filled.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(
                        vm = vm,
                        onLogout = onLogout
                    )
                }
                // La ruta "book_details" se ha eliminado porque ahora se gestiona con un diálogo.
            }
        }
    }
}

@Composable
private fun UnauthenticatedView(navController: NavHostController, vm: MainViewModel) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                vm = vm,
                onLoginOkNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                vm = vm,
                onRegisteredNavigateLogin = { navController.popBackStack() },
                onGoLogin = { navController.popBackStack() }
            )
        }
    }
}
