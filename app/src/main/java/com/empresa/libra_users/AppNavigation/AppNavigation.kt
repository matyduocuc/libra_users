@file:OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar y Scaffold

package com.empresa.libra_users.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.empresa.libra_users.screen.*
import com.empresa.libra_users.ui.theme.components.AppDrawer
import com.empresa.libra_users.ui.theme.components.authenticatedDrawerItems
import com.empresa.libra_users.viewmodel.MainViewModel
import kotlinx.coroutines.launch

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val BOOK_DETAILS = "book_details/{bookId}"
    const val CATALOG = "catalog"
    const val NEWS = "news"
    const val ACCOUNT_SETTINGS = "account_settings"
}

@Composable
fun AppNavigation(vm: MainViewModel) {
    val navController = rememberNavController()
    val isLoggedIn by vm.isLoggedIn.collectAsStateWithLifecycle()

    if (isLoggedIn) {
        AuthenticatedView(navController = navController, vm = vm)
    } else {
        UnauthenticatedView(navController = navController, vm = vm)
    }
}

@Composable
private fun AuthenticatedView(navController: NavHostController, vm: MainViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val routeNow = navController.currentBackStackEntryAsState().value?.destination?.route

    val onLogout = { vm.logout() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = routeNow,
                items = authenticatedDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.HOME) { launchSingleTop = true }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    onCatalog = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.CATALOG) { launchSingleTop = true }
                    },
                    onNews = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.NEWS) { launchSingleTop = true }
                    },
                    onAccountSettings = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.ACCOUNT_SETTINGS) { launchSingleTop = true }
                    }
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
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        vm = vm,
                        onLogout = onLogout,
                        onBookClick = { navController.navigate("book_details/$it") }
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
                composable(Routes.CATALOG) {
                    CatalogScreen(vm = vm, onBookClick = { navController.navigate("book_details/$it") })
                }
                composable(Routes.NEWS) {
                    NewsScreen()
                }
                composable(Routes.ACCOUNT_SETTINGS) {
                    AccountSettingsScreen(vm = vm)
                }
            }
        }
    }
}

@Composable
private fun UnauthenticatedView(navController: NavHostController, vm: MainViewModel) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                vm = vm,
                onGoRegister = { navController.navigate(Routes.REGISTER) },
                onLoginOkNavigateHome = {
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
