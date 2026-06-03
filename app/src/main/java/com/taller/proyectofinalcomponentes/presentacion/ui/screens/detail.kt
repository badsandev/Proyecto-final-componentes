package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.dominio.model.ItemCarrito
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.CarritoViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.FavoritosViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    carritoVM: CarritoViewModel,
    favoritosVM: FavoritosViewModel,
    productoVM: ProductoViewModel,
    onBack: () -> Unit,
    onGoToCart: () -> Unit
) {
    val productos by productoVM.productos.collectAsState()
    val product   = productos.find { it.id == productId }

    var cantidad          by remember { mutableIntStateOf(1) }
    var agregado          by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(agregado) {
        if (agregado) {
            snackbarHostState.showSnackbar(
                message  = "✓ Producto agregado al carrito",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle del producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    product?.let { p ->
                        IconButton(onClick = { favoritosVM.toggleFavorito(p) }) {
                            Icon(
                                if (favoritosVM.esFavorito(p.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                "Favorito",
                                tint = if (favoritosVM.esFavorito(p.id)) Color(0xFFEF4444) else Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onGoToCart) {
                        Icon(Icons.Default.ShoppingCart, "Carrito")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->

        if (product == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando producto...", color = Color.Gray)
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Imagen grande del producto ────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxWidth().height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                ImagenProducto(
                    imageUrl     = product.imageUrl,
                    nombre       = product.name,
                    categoria    = product.category,
                    size         = 280.dp,
                    cornerRadius = 0.dp
                )

                // Badges de stock encima de la imagen
                if (product.stock == 0) {
                    Surface(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), color = Color(0xFFEF4444), shape = RoundedCornerShape(8.dp)) {
                        Text("Agotado", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                } else if (product.stock <= 5) {
                    Surface(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp), color = Color(0xFFF97316), shape = RoundedCornerShape(8.dp)) {
                        Text("Solo ${product.stock} disponibles", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {

                Surface(color = Color(0xFF2563EB).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text(product.category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color(0xFF2563EB), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))
                Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i ->
                        Icon(Icons.Default.Star, null, tint = if (i < product.rating.toInt()) Color(0xFFF59E0B) else Color(0xFFE2E8F0), modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("${product.rating}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$${product.price}", style = MaterialTheme.typography.headlineMedium, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    product.oldPrice?.let {
                        Spacer(Modifier.width(10.dp))
                        Text("$$it", style = MaterialTheme.typography.titleMedium, color = Color.LightGray)
                        val descuento = (((it - product.price) / it) * 100).toInt()
                        Spacer(Modifier.width(8.dp))
                        Surface(color = Color(0xFF10B981).copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                            Text("-$descuento%", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color(0xFF10B981), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text("Descripción", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    product.description.ifEmpty { "Producto de alta calidad con garantía y envío disponible." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4B5563)
                )

                Spacer(Modifier.height(20.dp))

                if (product.stock > 0) {
                    Text("Cantidad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick  = { if (cantidad > 1) cantidad-- },
                            modifier = Modifier.size(38.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        ) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp)) }
                        Text("$cantidad", modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick  = { if (cantidad < product.stock) cantidad++ },
                            modifier = Modifier.size(38.dp).background(Color(0xFF2563EB), RoundedCornerShape(10.dp))
                        ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(16.dp))
                        Text("Total: $${String.format("%.2f", product.price * cantidad)}", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(28.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick  = { favoritosVM.toggleFavorito(product) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (favoritosVM.esFavorito(product.id)) Color(0xFFEF4444) else Color(0xFF64748B)
                        )
                    ) {
                        Icon(if (favoritosVM.esFavorito(product.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (favoritosVM.esFavorito(product.id)) "Guardado" else "Favorito")
                    }

                    Button(
                        onClick  = {
                            if (product.stock > 0) {
                                repeat(cantidad) {
                                    carritoVM.agregarItem(ItemCarrito(
                                        productoId = product.id,
                                        nombre     = product.name,
                                        precio     = product.price,
                                        cantidad   = 1,
                                        categoria  = product.category
                                    ))
                                }
                                agregado = true
                            }
                        },
                        modifier = Modifier.weight(2f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        enabled  = product.stock > 0,
                        colors   = ButtonDefaults.buttonColors(
                            containerColor        = if (agregado) Color(0xFF10B981) else Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFFE2E8F0)
                        )
                    ) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (product.stock == 0) "Agotado" else if (agregado) "¡Agregado!" else "Agregar al carrito",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}