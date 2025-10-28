package com.empresa.libra_users.screen.admin.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.empresa.libra_users.viewmodel.admin.AdminDashboardViewModel
import com.empresa.libra_users.viewmodel.admin.BookLoanStats
import com.empresa.libra_users.viewmodel.admin.LibraryStatus
import com.empresa.libra_users.viewmodel.admin.UserLoanStats

@Composable
fun AdminReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.reportsUiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            uiState.error != null -> Text(
                text = uiState.error ?: "Error desconocido",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    item { LibraryStatusSection(status = uiState.libraryStatus) }
                    item { TopBooksSection(topBooks = uiState.topBooks) }
                    item { TopUsersSection(topUsers = uiState.topUsers) }
                }
            }
        }
    }
}

@Composable
private fun LibraryStatusSection(status: LibraryStatus) {
    val total = (status.available + status.loaned + status.damaged).toFloat()
    if (total == 0f) return

    val chartData = mapOf(
        "Disponibles" to status.available.toFloat(),
        "Prestados" to status.loaned.toFloat(),
        "Dañados" to status.damaged.toFloat()
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error
    )

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado de la Biblioteca", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PieChart(data = chartData, colors = colors, modifier = Modifier.size(150.dp))
                ChartLegend(data = chartData, colors = colors)
            }
        }
    }
}

@Composable
private fun TopBooksSection(topBooks: List<BookLoanStats>) {
    if (topBooks.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Libros Más Populares", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            BarChart(items = topBooks.map { it.book.title to it.loanCount })
        }
    }
}

@Composable
private fun TopUsersSection(topUsers: List<UserLoanStats>) {
    if (topUsers.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Usuarios Más Activos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            BarChart(items = topUsers.map { it.user.name to it.loanCount })
        }
    }
}

@Composable
fun PieChart(data: Map<String, Float>, colors: List<Color>, modifier: Modifier = Modifier) {
    var startAngle = -90f
    val total = data.values.sum()

    Canvas(modifier = modifier) {
        data.values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun ChartLegend(data: Map<String, Float>, colors: List<Color>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.keys.forEachIndexed { index, label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(16.dp).background(colors[index % colors.size]))
                Spacer(Modifier.width(8.dp))
                Text(text = "$label (${data[label]?.toInt()})", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun BarChart(items: List<Pair<String, Int>>) {
    val maxValue = items.maxOfOrNull { it.second }?.toFloat() ?: 1f

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (label, value) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, modifier = Modifier.weight(0.4f), fontSize = 14.sp, maxLines = 1)
                Row(modifier = Modifier.weight(0.6f).height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.fillMaxWidth(fraction = (value / maxValue)).height(20.dp).background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small))
                    Text(text = " $value", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
