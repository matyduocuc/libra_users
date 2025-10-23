package com.empresa.libra_users.ui.theme.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// Data class para representar cada opción del drawer (ESTO SE QUEDA IGUAL)
data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// Componente AppDrawer para usar en ModalNavigationDrawer (ESTO SE QUEDA IGUAL)
@Composable
fun AppDrawer(
    currentRoute: String?,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
    ) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier,
                colors = NavigationDrawerItemDefaults.colors()
            )
        }
    }
}

// Helper para construir la lista para usuarios SIN sesión (ESTO SE QUEDA IGUAL)
@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
): List<DrawerItem> = listOf(
    DrawerItem("Home", Icons.Filled.Home, onHome),
    DrawerItem("Login", Icons.Filled.AccountCircle, onLogin),
    DrawerItem("Registro", Icons.Filled.Person, onRegister)
)

@Composable
fun authenticatedDrawerItems(
    onHome: () -> Unit,
    onLogout: () -> Unit,
    // Añadimos callbacks para las nuevas opciones
    onCatalog: () -> Unit,
    onNews: () -> Unit,
    onAccountSettings: () -> Unit
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            label = "Novedades",
            icon = Icons.Filled.NewReleases,
            onClick = onHome
        ),
        DrawerItem(
            label = "Catálogo",
            icon = Icons.Filled.MenuBook,
            onClick = onCatalog
        ),
        DrawerItem(
            label = "Noticias",
            icon = Icons.Filled.Feed,
            onClick = onNews
        ),
        DrawerItem(
            label = "Configuración de la cuenta",
            icon = Icons.Filled.Settings,
            onClick = onAccountSettings
        ),
        DrawerItem(
            label = "Cerrar Sesión",
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogout
        )
    )
}
