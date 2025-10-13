@file:OptIn(ExperimentalMaterial3Api::class)


package com.empresa.libra_users.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.empresa.libra_users.screen.HomeScreen
import com.empresa.libra_users.screen.LoginScreen
import com.empresa.libra_users.screen.RegisterScreen
import com.empresa.libra_users.ui.theme.components.AppDrawer
import com.empresa.libra_users.ui.theme.components.buildDefaultNavDrawerItems
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val routeNow = navController.currentBackStackEntryAsState().value?.destination?.route

    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            launchSingleTop = true; restoreState = true
            popUpTo(navController.graph.startDestinationId) { saveState = true }
        }
    }
    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path) {
            launchSingleTop = true; restoreState = true
            popUpTo(navController.graph.startDestinationId) { saveState = true }
        }
    }
    val goRegister: () -> Unit = {
        navController.navigate(Route.Register.path) {
            launchSingleTop = true; restoreState = true
            popUpTo(navController.graph.startDestinationId) { saveState = true }
        }
    }

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
                val active = MaterialTheme.colorScheme.primary
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
                            Icon(Icons.Filled.Home, "Home",
                                tint = if (routeNow == Route.Home.path) active else inactive)
                        }
                        IconButton(onClick = goLogin) {
                            Icon(Icons.AutoMirrored.Filled.Login, "Login",
                                tint = if (routeNow == Route.Login.path) active else inactive)
                        }
                        IconButton(onClick = goRegister) {
                            Icon(Icons.Filled.PersonAdd, "Registro",
                                tint = if (routeNow == Route.Register.path) active else inactive)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Route.Home.path,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Route.Home.path) { HomeScreen(onGoLogin = goLogin, onGoRegister = goRegister) }
                composable(Route.Login.path) { LoginScreen(onLoginOkNavigateHome = goHome, onGoRegister = goRegister) }
                composable(Route.Register.path) { RegisterScreen(onRegisteredNavigateLogin = goLogin, onGoLogin = goLogin) }
            }
        }
    }
}
