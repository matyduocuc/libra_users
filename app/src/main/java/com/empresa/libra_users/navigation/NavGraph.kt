@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.empresa.libra_users.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.empresa.libra_users.ui.theme.components.authenticatedDrawerItems // <-- CAMBIO IMPORTANTE
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    // Observamos el estado de login. Esta variable decidirá qué UI mostrar.
    val isLoggedIn by mainViewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (isLoggedIn) {
        // --- VISTA PARA USUARIO CON SESIÓN INICIADA ---
        AuthenticatedView(navController = navController, vm = mainViewModel)
    } else {
        // --- VISTA PARA USUARIO SIN SESIÓN ---
        UnauthenticatedView(navController = navController, vm = mainViewModel)
    }
}

// ===================================================================
// VISTA PARA USUARIOS CON SESIÓN (CON DRAWER Y TOPAPPBAR)
// ===================================================================
@Composable
private fun AuthenticatedView(navController: NavHostController, vm: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val routeNow = navController.currentBackStackEntryAsState().value?.destination?.route

    val onLogout = {
        vm.logout()
        // No es necesario navegar, el cambio de `isLoggedIn` hará el resto.
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Usamos el nuevo builder para el Drawer del usuario logueado
            AppDrawer(
                currentRoute = routeNow,
                items = authenticatedDrawerItems( // <-- CAMBIO IMPORTANTE
                    onHome = { scope.launch { drawerState.close() }; navController.navigate(Route.Home.path) },
                    onLogout = { scope.launch { drawerState.close() }; onLogout() }
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
                        IconButton(onClick = { navController.navigate(Route.Home.path) }) {
                            Icon(Icons.Filled.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión")
                        }
                    }
                )
            }
        ) { innerPadding ->
            // NavHost para el usuario autenticado
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) {
                    HomeScreen(
                        vm = vm,
                        onLogout = onLogout // Se lo pasamos por si lo necesitas dentro
                    )
                }
                // Aquí puedes añadir más pantallas (Search, Profile, etc.)
            }
        }
    }
}

// ===================================================================
// VISTA PARA USUARIOS SIN SESIÓN (SOLO LOGIN Y REGISTRO)
// ===================================================================
@Composable
private fun UnauthenticatedView(navController: NavHostController, vm: MainViewModel) {
    // NavHost solo con las pantallas de autenticación
    NavHost(
        navController = navController,
        startDestination = Route.Login.path
    ) {
        composable(Route.Login.path) {
            LoginScreen(
                vm = vm,
                onLoginOkNavigateHome = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Route.Register.path) }
            )
        }
        composable(Route.Register.path) {
            RegisterScreen(
                vm = vm,
                onRegisteredNavigateLogin = { navController.popBackStack() },
                onGoLogin = { navController.popBackStack() }
            )
        }
    }
}
