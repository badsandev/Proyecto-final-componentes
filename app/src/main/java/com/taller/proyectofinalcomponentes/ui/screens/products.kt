package com.taller.proyectofinalcomponentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    category: String,
    onBack: () -> Unit,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit
) {

    val products = listOf(
        Product(1, "iPhone 15 Pro", category, 999.0, 1099.0, 4.9),
        Product(2, "Samsung Galaxy S23", category, 850.0, 950.0, 4.7),
        Product(3, "Wireless Headphones", category, 199.0, 249.0, 4.6),
        Product(4, "Smart Watch", category, 299.0, 350.0, 4.5),
        Product(5, "Gaming Mouse", category, 120.0, 150.0, 4.4),
        Product(6, "Bluetooth Speaker", category, 89.0, 120.0, 4.3)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(products) { product ->
                ProductGridCard(product, onProductClick)
            }
        }
    }
}

@Composable
private fun ProductGridCard(
    product: Product,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(product.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(modifier = Modifier.padding(12.dp)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(product.category, color = Color(0xFF475569))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(product.rating.toString(), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "$${product.price}",
                color = Color(0xFF2563EB),
                fontWeight = FontWeight.Bold
            )
        }
    }
}