package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.taller.proyectofinalcomponentes.dominio.model.Product
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.CarritoViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.FavoritosViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    category: String,
    carritoVM: CarritoViewModel,
    productoVM: ProductoViewModel,
    favoritosVM: FavoritosViewModel,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit
) {
    val todosProductos by productoVM.productos.collectAsState()
    val cargando       by productoVM.cargando.collectAsState()
    var busqueda       by remember { mutableStateOf("") }
    var orden          by remember { mutableStateOf("precio_asc") }
    var menuAbierto    by remember { mutableStateOf(false) }

   /* LaunchedEffect(category) {
        productoVM.cargarPorCategoria(category)
    }*/

    val productosFiltrados = remember(todosProductos, busqueda, orden, category) {
        var lista = todosProductos.filter {
            (category == "Todos" || it.category == category) &&
                    (busqueda.isBlank() || it.name.contains(busqueda, ignoreCase = true))
        }
        lista = when (orden) {
            "precio_asc"  -> lista.sortedBy { it.price }
            "precio_desc" -> lista.sortedByDescending { it.price }
            "rating"      -> lista.sortedByDescending { it.rating }
            else -> lista
        }
        lista
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuAbierto = true }) {
                            Icon(Icons.Default.Sort, "Ordenar")
                        }
                        DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                            DropdownMenuItem(text = { Text("Precio: menor a mayor") }, onClick = { orden = "precio_asc";  menuAbierto = false })
                            DropdownMenuItem(text = { Text("Precio: mayor a menor") }, onClick = { orden = "precio_desc"; menuAbierto = false })
                            DropdownMenuItem(text = { Text("Mejor valorados") },       onClick = { orden = "rating";      menuAbierto = false })
                        }
                    }
                    BadgedBox(badge = {
                        if (carritoVM.cantidadTotal > 0) Badge { Text("${carritoVM.cantidadTotal}") }
                    }) {
                        IconButton(onClick = onCartClick) {
                            Icon(Icons.Default.ShoppingCart, "Carrito")
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value         = busqueda,
                onValueChange = { busqueda = it },
                modifier      = Modifier.fillMaxWidth().padding(12.dp),
                placeholder   = { Text("Buscar en $category...") },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                trailingIcon  = {
                    if (busqueda.isNotEmpty()) IconButton(onClick = { busqueda = "" }) {
                        Icon(Icons.Default.Close, null)
                    }
                },
                shape  = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor   = Color.White
                )
            )

            when {
                cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
                productosFiltrados.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Search, null, Modifier.size(56.dp), tint = Color(0xFFE2E8F0))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (busqueda.isNotEmpty()) "Sin resultados para \"$busqueda\""
                            else "No hay productos en esta categoría",
                            color = Color.Gray
                        )
                    }
                }
                else -> LazyVerticalGrid(
                    columns               = GridCells.Fixed(2),
                    contentPadding        = PaddingValues(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productosFiltrados, key = { it.id }) { product ->
                        TarjetaProductoGrid(
                            product          = product,
                            esFavorito       = favoritosVM.esFavorito(product.id),
                            onClick          = { onProductClick(product.id) },
                            onFavorito       = { favoritosVM.toggleFavorito(product) },
                            onAgregarCarrito = {
                                carritoVM.agregarItem(ItemCarrito(
                                    productoId = product.id,
                                    nombre     = product.name,
                                    precio     = product.price,
                                    cantidad   = 1,
                                    categoria  = product.category
                                ))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaProductoGrid(
    product: Product,
    esFavorito: Boolean,
    onClick: () -> Unit,
    onFavorito: () -> Unit,
    onAgregarCarrito: () -> Unit
) {
    var agregado by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(10.dp)) {

            // ── Imagen del producto ───────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                ImagenProducto(
                    imageUrl     = product.imageUrl,
                    nombre       = product.name,
                    categoria    = product.category,
                    size         = 120.dp,
                    cornerRadius = 12.dp
                )
                // Botón favorito encima de la imagen
                IconButton(
                    onClick  = onFavorito,
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                ) {
                    Icon(
                        if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint     = if (esFavorito) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
                // Badge stock bajo
                if (product.stock in 1..5) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                        color    = Color(0xFFF97316).copy(alpha = 0.9f),
                        shape    = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Stock: ${product.stock}",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color    = Color.White,
                            style    = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                // Badge agotado
                if (product.stock == 0) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
                        color    = Color(0xFFEF4444).copy(alpha = 0.9f),
                        shape    = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "Agotado",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color    = Color.White,
                            style    = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(product.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 2, minLines = 2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                Text(" ${product.rating}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text("$${product.price}", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick        = { if (product.stock > 0) { onAgregarCarrito(); agregado = true } },
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 6.dp),
                shape          = RoundedCornerShape(10.dp),
                enabled        = product.stock > 0,
                colors         = ButtonDefaults.buttonColors(
                    containerColor        = if (agregado) Color(0xFF10B981) else Color(0xFF2563EB),
                    disabledContainerColor = Color(0xFFE2E8F0)
                )
            ) {
                Text(
                    when {
                        product.stock == 0 -> "Agotado"
                        agregado           -> "✓ Agregado"
                        else               -> "Añadir"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}