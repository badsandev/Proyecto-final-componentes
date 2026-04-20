package com.taller.proyectofinalcomponentes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {

    val productName = "iPhone 15 Pro"
    val price = 999.0
    val oldPrice = 1099.0
    val rating = 4.9

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onGoToCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            // IMAGEN
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Product Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                productName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFF59E0B)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("$rating")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$$price",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    "$$oldPrice",
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Descripción del producto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Este es un producto de alta calidad diseñado para ofrecer el mejor rendimiento y experiencia al usuario."
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onGoToCart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Agregar al carrito")
            }
        }
    }
}