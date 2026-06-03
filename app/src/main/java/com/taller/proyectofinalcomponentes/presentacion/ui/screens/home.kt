package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.dominio.model.User
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.CarritoViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.FavoritosViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    usuario: User?,
    carritoVM: CarritoViewModel,
    productoVM: ProductoViewModel,
    favoritosVM: FavoritosViewModel,
    onCategoryClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onNotificacionesClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onOrdenesClick: () -> Unit,
    onFavoritosClick: () -> Unit
) {
    val productos   by productoVM.productos.collectAsState()
    val cargando    by productoVM.cargando.collectAsState()
    val cantCarrito = carritoVM.cantidadTotal

    LaunchedEffect(Unit) { productoVM.cargarProductos() }

    val categorias = listOf(
        DatoCategoria("Electrónica",  Icons.Default.Star,          Color(0xFF3B82F6)),
        DatoCategoria("Moda",         Icons.Default.Favorite,      Color(0xFFEC4899)),
        DatoCategoria("Hogar",        Icons.Default.Home,          Color(0xFF10B981)),
        DatoCategoria("Belleza",      Icons.Default.Face,          Color(0xFF8B5CF6)),
        DatoCategoria("Deportes",     Icons.Default.DirectionsRun, Color(0xFFF59E0B)),
        DatoCategoria("Alimentación", Icons.Default.ShoppingCart,  Color(0xFFEF4444))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.clickable { onPerfilClick() }) {
                        Text("ShopWave", fontWeight = FontWeight.Bold)
                        Text(
                            "Hola, ${usuario?.nombre ?: "Bienvenido"} 👋",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onFavoritosClick) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favoritos")
                    }
                    IconButton(onClick = onNotificacionesClick) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    BadgedBox(badge = {
                        if (cantCarrito > 0) Badge { Text("$cantCarrito") }
                    }) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = "", onValueChange = {},
                    modifier    = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar productos...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape  = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor   = Color.White
                    )
                )
                Spacer(Modifier.height(16.dp))
                BannerHome()
                Spacer(Modifier.height(22.dp))
                Text("Categorías", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(categorias) { cat ->
                        BotonCategoria(cat) { onCategoryClick(cat.nombre) }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOrdenesClick) { Text("Mis órdenes") }
                }
                Spacer(Modifier.height(8.dp))
            }

            when {
                cargando -> item {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                }
                productos.isEmpty() -> item {
                    Box(
                        Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingCart, null,
                                modifier = Modifier.size(48.dp),
                                tint     = Color(0xFFE2E8F0)
                            )
                            Text("No hay productos disponibles", color = Color.Gray)
                        }
                    }
                }
                else -> items(productos.take(10), key = { it.id }) { product ->
                    TarjetaProductoHome(
                        product    = product,
                        esFavorito = favoritosVM.esFavorito(product.id),
                        onClick    = { onProductClick(product.id) },
                        onFavorito = { favoritosVM.toggleFavorito(product) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

data class DatoCategoria(val nombre: String, val icono: ImageVector, val color: Color)

@Composable
private fun BannerHome() {
    val gradiente = Brush.horizontalGradient(listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)))
    Card(shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Box(Modifier.fillMaxWidth().background(gradiente).padding(20.dp)) {
            Column {
                Text(
                    "🔥 Ofertas Flash",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Hasta 40% de descuento",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BotonCategoria(dato: DatoCategoria, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier         = Modifier.size(58.dp).clip(CircleShape).background(dato.color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(dato.icono, null, tint = dato.color, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(dato.nombre, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TarjetaProductoHome(
    product: com.taller.proyectofinalcomponentes.dominio.model.Product,
    esFavorito: Boolean,
    onClick: () -> Unit,
    onFavorito: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenProducto(
                imageUrl     = product.imageUrl,
                nombre       = product.name,
                categoria    = product.category,
                size         = 68.dp,
                cornerRadius = 12.dp
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(product.category, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                    Text(" ${product.rating}", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$${product.price}",
                        color      = Color(0xFF2563EB),
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleSmall
                    )
                }
            }
            IconButton(onClick = onFavorito) {
                Icon(
                    if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    null,
                    tint = if (esFavorito) Color(0xFFEF4444) else Color(0xFF94A3B8)
                )
            }
        }
    }
}