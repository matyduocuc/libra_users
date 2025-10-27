package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.empresa.libra_users.screen.admin.books.AdminBooksScreen
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel

// Data class to represent a navigation item
data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // List of navigation items for the side rail (using your suggested routes)
    val navItems = listOf(
        AdminNavItem("Panel", Icons.Filled.Dashboard, "admin_dashboard"),
        AdminNavItem("Libros", Icons.Filled.Book, "admin_books"),
        AdminNavItem("Usuarios", Icons.Filled.Group, "admin_users"),
        AdminNavItem("Préstamos", Icons.Filled.SwapHoriz, "admin_loans"),
        AdminNavItem("Informes", Icons.Filled.BarChart, "admin_reports")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración – Biblioteca") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                // Placeholder for avatar
            )
        }
    ) { innerPadding ->
        Row(modifier = modifier.padding(innerPadding)) {
            // Navigation Rail on the left
            AdminNavigationRail(navController = navController, navItems = navItems)

            // Main content area
            NavHost(navController = navController, startDestination = "admin_dashboard") {
                composable("admin_dashboard") {
                    AdminHomeScreen() // Our newly created home screen, now connected to ViewModel
                }
                composable("admin_books") { AdminBooksScreen() }
                composable("admin_users") { PlaceholderScreen("Gestión de Usuarios") }
                composable("admin_loans") { PlaceholderScreen("Control de Préstamos") }
                composable("admin_reports") { PlaceholderScreen("Informes y Estadísticas") }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Panel de control",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            // FlowRow arranges cards and wraps to the next line if space is needed.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                maxItemsInEachRow = 4
            ) {
                StatCard(
                    icon = Icons.Filled.Book,
                    value = if (uiState.isLoading) "..." else uiState.totalBooks.toString(),
                    label = "Total de libros"
                )
                StatCard(
                    icon = Icons.Filled.Group,
                    value = if (uiState.isLoading) "..." else uiState.totalUsers.toString(),
                    label = "Usuarios registrados"
                )
                StatCard(
                    icon = Icons.Filled.PendingActions,
                    value = if (uiState.isLoading) "..." else uiState.pendingLoans.toString(),
                    label = "Préstamos pendientes"
                )
                StatCard(
                    icon = Icons.Filled.Verified,
                    value = if (uiState.isLoading) "..." else uiState.totalLoans.toString(),
                    label = "Préstamos totales"
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier.width(220.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
fun AdminNavigationRail(navController: NavController, navItems: List<AdminNavItem>) {
    NavigationRail {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navItems.forEach { screen ->
            NavigationRailItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

// A simple placeholder screen for sections not yet built
@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.headlineMedium)
    }
}
