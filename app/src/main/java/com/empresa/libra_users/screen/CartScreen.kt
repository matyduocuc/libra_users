package com.empresa.libra_users.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.empresa.libra_users.navigation.Routes
import com.empresa.libra_users.viewmodel.CartItem
import com.empresa.libra_users.viewmodel.MainViewModel
import java.text.NumberFormat

@Composable
fun CartScreen(vm: MainViewModel, navController: NavController) {
    val cartItems by vm.cart.collectAsStateWithLifecycle()

    if (cartItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tu carrito está vacío", style = MaterialTheme.typography.headlineSmall)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(cartItems, key = { it.book.id }) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onRemove = { vm.removeFromCart(cartItem.book.id) },
                    onDaysChange = { days -> vm.updateLoanDays(cartItem.book.id, days) },
                    onConfirmLoan = { 
                        vm.confirmLoanFromCart(cartItem)
                        // Optionally, navigate away or show a confirmation message
                    }
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    cartItem: CartItem,
    onRemove: () -> Unit,
    onDaysChange: (Int) -> Unit,
    onConfirmLoan: () -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(cartItem.book.coverUrl).crossfade(true).build(),
                contentDescription = cartItem.book.title,
                modifier = Modifier.width(80.dp).aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(cartItem.book.author, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                // --- Slider para los días de préstamo ---
                Text("Días de préstamo: ${cartItem.loanDays}", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = cartItem.loanDays.toFloat(),
                    onValueChange = { onDaysChange(it.toInt()) },
                    valueRange = 1f..30f,
                    steps = 28 // 30-1-1 = 28 steps
                )
                Text("Precio: ${NumberFormat.getCurrencyInstance().format(cartItem.price)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                Spacer(Modifier.height(12.dp))
                Button(onClick = onConfirmLoan, modifier = Modifier.fillMaxWidth()) {
                    Text("Confirmar Préstamo")
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar del carrito")
            }
        }
    }
}
