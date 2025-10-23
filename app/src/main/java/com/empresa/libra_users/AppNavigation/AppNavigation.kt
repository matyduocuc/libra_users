@file:OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar y Scaffold

package com.empresa.libra_users.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.* // Import all filled icons
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme // <-- Importante para usar los colores del tema
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults // <-- Importante para los colores de la TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.empresa.libra_users.screen.BookDetailsScreen
import com.empresa.libra_users.screen.HomeScreen
import com.empresa.libra_users.screen.LoginScreen
import com.empresa.libra_users.screen.RegisterScreen
import com.empresa.libra_users.ui.theme.components.AppDrawer
import com.empresa.libra_users.ui.theme.components.authenticatedDrawerItems
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// El objeto Routes se queda igual
object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val BOOK_DETAILS = "book_details/{bookId}" // Ruta con argumento
}

@Composable
fun AppNavigation(vm: MainViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()

    // La lógica principal sigue siendo la misma: mostramos una vista u otra.
    if (isLoggedIn) {
        AuthenticatedView(navController = navController, vm = vm)
    } else {
        UnauthenticatedView(navController = navController, vm = vm)
    }
}

// ===================================================================
// VISTA PARA USUARIOS CON SESIÓN (AHORA CON DRAWER Y TOPAPPBAR)
// ===================================================================
@Composable
private fun AuthenticatedView(navController: NavHostController, vm: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val routeNow = navController.currentBackStackEntryAsState().value?.destination?.route

    val onLogout = {
        vm.logout()
        // No es necesario navegar, el cambio de `isLoggedIn` a `false` reconstruirá
        // la UI para mostrar `UnauthenticatedView` automáticamente.
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = routeNow,
                items = authenticatedDrawerItems( // Usamos la lista de items para usuarios logueados
                    onHome = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.HOME) { launchSingleTop = true }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
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
                        IconButton(onClick = { navController.navigate(Routes.HOME) { launchSingleTop = true } }) {
                            Icon(Icons.Filled.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar Sesión"
                            )
                        }
                    },
                    // =======================================================
                    // ===             AQUÍ ESTÁ EL CAMBIO DE COLOR        ===
                    // =======================================================
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        // Color para el icono de navegación (menú)
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        // Color para los iconos de acción (home, logout)
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        // Opcional: También puedes cambiar el color del título
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            // Aquí va el contenido principal de la app (HomeScreen, etc.)
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        vm = vm,
                        onLogout = onLogout, // Se lo pasamos por si acaso, aunque el botón ya está arriba.
                        onBookClick = {
                            navController.navigate("book_details/$it")
                        }
                    )
                }
                composable(
                    route = Routes.BOOK_DETAILS,
                    arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0
                    BookDetailsScreen(
                        vm = vm,
                        bookId = bookId,
                        onBack = { navController.popBackStack() }
                    )
                }
                // Aquí puedes añadir más pantallas como Search, Profile, etc.
                // composable("search") { ... }
            }
        }
    }
}

// ===================================================================
// VISTA PARA USUARIOS SIN SESIÓN (ESTA PARTE NO CAMBIA)
// ===================================================================
@Composable
private fun UnauthenticatedView(navController: NavHostController, vm: MainViewModel) {
    // Un NavHost simple solo con las pantallas de autenticación, sin Scaffold ni menús.
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                vm = vm,
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onLoginOkNavigateHome = {
                    // Al iniciar sesión correctamente, la variable `isLoggedIn` cambiará a `true`
                    // y la UI se reconstruirá para mostrar `AuthenticatedView`.
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                vm = vm,
                onGoLogin = { navController.popBackStack() },
                onRegisteredNavigateLogin = { navController.popBackStack() }
            )
        }
    }
}
