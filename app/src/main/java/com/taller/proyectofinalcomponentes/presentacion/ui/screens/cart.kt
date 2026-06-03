package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.CarritoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    carritoVM: CarritoViewModel,
    onBack: () -> Unit,
    onCheckout: () -> Unit
) {
    val items by carritoVM.items.collectAsState()
    val subtotal = items.sumOf { it.precio * it.cantidad }
    val envio = if (items.isEmpty()) 0.0 else 5.0
    val total = subtotal + envio

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Mi carrito", fontWeight = FontWeight.Bold)
                        if (items.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge { Text("${carritoVM.cantidadTotal}") }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { carritoVM.vaciarCarrito() }) {
                            Text(text = "Vaciar", color = Color.Red)
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (items.isEmpty()) {
            EstadoVacioCarrito(Modifier.fillMaxSize().padding(padding), onBack)
        } else {
            Column(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(items, key = { it.productoId }) { item ->
                        TarjetaItemCarrito(
                            item = item,
                            onAumentar = { carritoVM.aumentarCantidad(item.productoId) },
                            onDisminuir = { carritoVM.disminuirCantidad(item.productoId) },
                            onEliminar = { carritoVM.eliminarItem(item.productoId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        FilaResumen("Subtotal", "$${String.format(Locale.US, "%.2f", subtotal)}")
                        FilaResumen("Envío", "$${String.format(Locale.US, "%.2f", envio)}")
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        FilaResumen("Total", "$${String.format(Locale.US, "%.2f", total)}", true)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Proceder al pago")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaItemCarrito(item: ItemCarrito, onAumentar: () -> Unit, onDisminuir: () -> Unit, onEliminar: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenProducto(
                imageUrl     = item.imageUrl,
                nombre       = item.nombre,
                categoria    = item.categoria,
                size         = 60.dp,
                cornerRadius = 8.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.nombre, fontWeight = FontWeight.Bold)
                Text("$${String.format(Locale.US, "%.2f", item.precio)}", color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDisminuir) { Icon(Icons.Default.Remove, null) }
                    Text("${item.cantidad}")
                    IconButton(onClick = onAumentar) { Icon(Icons.Default.Add, null) }
                }
            }
            IconButton(onClick = onEliminar) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        }
    }
}

@Composable
private fun FilaResumen(label: String, valor: String, negrita: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal)
        Text(valor, fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun EstadoVacioCarrito(modifier: Modifier, onBack: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ShoppingCart, null, Modifier.size(80.dp), tint = Color.LightGray)
        Text("Tu carrito está vacío", style = MaterialTheme.typography.titleMedium)
        Button(onClick = onBack, Modifier.padding(top = 16.dp)) { Text("Seguir comprando") }
    }
}