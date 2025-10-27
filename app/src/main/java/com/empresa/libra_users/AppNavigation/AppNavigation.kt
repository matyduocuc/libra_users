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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.empresa.libra_users.screen.*
import com.empresa.libra_users.viewmodel.AuthState
import com.empresa.libra_users.viewmodel.MainViewModel

object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val CATALOG = "catalog"
    const val NEWS = "news"
    const val ACCOUNT_SETTINGS = "account_settings"
    const val CART = "cart"
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val mainNavItems = listOf(
    NavItem(Routes.HOME, "Inicio", Icons.Default.Home),
    NavItem(Routes.CATALOG, "Catálogo", Icons.Default.MenuBook),
    NavItem(Routes.NEWS, "Noticias", Icons.Default.Article),
    NavItem(Routes.ACCOUNT_SETTINGS, "Cuenta", Icons.Default.AccountCircle)
)

@Composable
fun AppNavigation(
    vm: MainViewModel,
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()
    val authState by vm.authState.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        AuthState.AUTHENTICATED -> {
            AuthenticatedView(
                navController = navController,
                vm = vm,
                windowSizeClass = windowSizeClass
            )
        }
        AuthState.UNAUTHENTICATED -> {
            UnauthenticatedView(navController = navController, vm = vm)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthenticatedView(
    navController: NavHostController,
    vm: MainViewModel,
    windowSizeClass: WindowSizeClass
) {
    val showNavigationRail = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    val onLogout = { vm.logout() }
    val cartItems by vm.cart.collectAsStateWithLifecycle()

    Row {
        if (showNavigationRail) {
            AppNavigationRail(navController = navController, items = mainNavItems)
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Libra Users") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.CART) }) {
                            BadgedBox(badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge { Text("${cartItems.size}") }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Carrito"
                                )
                            }
                        }
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
            bottomBar = {
                if (!showNavigationRail) {
                    AppBottomBar(navController = navController, items = mainNavItems)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(vm = vm, onLogout = onLogout)
                }
                composable(Routes.CATALOG) {
                    CatalogScreen(vm = vm)
                }
                composable(Routes.NEWS) {
                    NewsScreen()
                }
                composable(Routes.ACCOUNT_SETTINGS) {
                    AccountSettingsScreen(vm = vm)
                }
                composable(Routes.CART) {
                    CartScreen(vm = vm, navController = navController)
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
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
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
