@file:OptIn(ExperimentalMaterial3Api::class) // Necesario para TopAppBar y Scaffold

package com.empresa.libra_users.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.empresa.libra_users.screen.AccountSettingsScreen
import com.empresa.libra_users.screen.BookDetailsScreen
import com.empresa.libra_users.screen.CatalogScreen
import com.empresa.libra_users.screen.HomeScreen
import com.empresa.libra_users.screen.LoginScreen
import com.empresa.libra_users.screen.NewsScreen
import com.empresa.libra_users.screen.RegisterScreen
import com.empresa.libra_users.viewmodel.AuthState
import com.empresa.libra_users.viewmodel.MainViewModel

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

// Data class para representar nuestros items de navegación
data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// Lista con los items de navegación principales
val mainNavItems = listOf(
    NavItem(Routes.HOME, "Inicio", Icons.Default.Home),
    NavItem(Routes.CATALOG, "Catálogo", Icons.Default.MenuBook),
    NavItem(Routes.NEWS, "Noticias", Icons.Default.Article),
    NavItem(Routes.ACCOUNT_SETTINGS, "Cuenta", Icons.Default.AccountCircle)
)

@Composable
fun AppNavigation(
    vm: MainViewModel,
    windowSizeClass: WindowSizeClass // Recibimos la clase de tamaño
) {
    val navController = rememberNavController()
    val authState by vm.authState.collectAsStateWithLifecycle()

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
            AuthenticatedView(
                navController = navController,
                vm = vm,
                windowSizeClass = windowSizeClass // La pasamos hacia abajo
            )
        }
        AuthState.UNAUTHENTICATED -> {
            UnauthenticatedView(navController = navController, vm = vm)
        }
    }
}

@Composable
private fun AuthenticatedView(
    navController: NavHostController,
    vm: MainViewModel,
    windowSizeClass: WindowSizeClass
) {
    // Decidimos si mostrar el NavRail basado en el ancho de la pantalla
    val showNavigationRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    val onLogout = { vm.logout() }

    Row {
        // 1. Muestra el NavigationRail en pantallas medianas y grandes
        if (showNavigationRail) {
            AppNavigationRail(navController = navController, items = mainNavItems)
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Libra Users") },
                    // El icono de menú ya no es necesario
                    actions = {
                        // El botón de logout se mantiene en la barra superior
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar Sesión"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            // 2. Muestra la Barra Inferior solo en pantallas compactas
            bottomBar = {
                if (!showNavigationRail) {
                    AppBottomBar(navController = navController, items = mainNavItems)
                }
            }
        ) { innerPadding ->
            // 3. El contenido de las pantallas (NavHost) se adapta al espacio restante
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
                    // En el futuro, la configuración del modo oscuro se puede mover aquí
                    AccountSettingsScreen(vm = vm)
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController, items: List<NavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
private fun AppNavigationRail(navController: NavHostController, items: List<NavItem>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationRail {
        items.forEach { item ->
            NavigationRailItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
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
