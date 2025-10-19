@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.empresa.libra_users.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

// Pantallas
import com.empresa.libra_users.screen.HomeScreen
import com.empresa.libra_users.screen.LoginScreen
import com.empresa.libra_users.screen.RegisterScreen

// Drawer / items
import com.empresa.libra_users.ui.theme.components.AppDrawer
import com.empresa.libra_users.ui.theme.components.buildDefaultNavDrawerItems

// ViewModel
import com.empresa.libra_users.viewmodel.MainViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel   // <-- Recibimos el ViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val routeNow = navController.currentBackStackEntryAsState().value?.destination?.route

    // Lógica para navegación SingleTop
    val navigateSingleTop: (String) -> Unit = { route ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            // PopUpTo al inicio para evitar múltiples pilas, pero guarda el estado para volver.
            popUpTo(navController.graph.startDestinationId) { saveState = true }
        }
    }
    val goHome     = { navigateSingleTop(Route.Home.path) }
    val goLogin    = { navigateSingleTop(Route.Login.path) }
    val goRegister = { navigateSingleTop(Route.Register.path) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = routeNow,
                items = buildDefaultNavDrawerItems(
                    onHome = { scope.launch { drawerState.close() }; goHome() },
                    onLogin = { scope.launch { drawerState.close() }; goLogin() },
                    onRegister = { scope.launch { drawerState.close() }; goRegister() }
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                val active   = MaterialTheme.colorScheme.primary
                val inactive = MaterialTheme.colorScheme.onSurfaceVariant
                CenterAlignedTopAppBar(
                    title = { Text("Libra Users") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    },
                    actions = {
                        IconButton(onClick = goHome) {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home",
                                tint = if (routeNow == Route.Home.path) active else inactive
                            )
                        }
                        IconButton(onClick = goLogin) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = "Login",
                                tint = if (routeNow == Route.Login.path) active else inactive
                            )
                        }
                        IconButton(onClick = goRegister) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = "Registro",
                                tint = if (routeNow == Route.Register.path) active else inactive
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                )
            }
        ) { innerPadding ->
            // EL NAVHOST PRINCIPAL Y ÚNICO
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(
                        vm = mainViewModel, // <-- CORREGIDO: Se pasa el VM
                        onGoLogin = goLogin,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Login.path) {
                    LoginScreen(
                        vm = mainViewModel, // <-- CORREGIDO: Se pasa el VM
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreen(
                        vm = mainViewModel, // <-- CORREGIDO: Se pasa el VM
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
                // Aquí irían las rutas adicionales (ej. libros, perfil)
            }
        }
    }
}