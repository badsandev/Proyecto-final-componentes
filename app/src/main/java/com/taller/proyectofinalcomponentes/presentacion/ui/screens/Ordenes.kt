package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.background
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
import com.taller.proyectofinalcomponentes.dominio.model.EstadoOrden
import com.taller.proyectofinalcomponentes.dominio.model.Orden
import com.taller.proyectofinalcomponentes.presentacion.ui.screens.admin.ChipEstado
import com.taller.proyectofinalcomponentes.presentacion.ui.screens.admin.colorPorEstado
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.OrdenViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenesScreen(
    ordenVM: OrdenViewModel,
    usuarioId: String,
    onBack: () -> Unit
) {
    val todasLasOrdenes by ordenVM.ordenes.collectAsState()
    val cargando by ordenVM.cargando.collectAsState()

    val misOrdenes = todasLasOrdenes 

    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotEmpty()) {
            ordenVM.cargarOrdenesUsuario(usuarioId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis órdenes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        when {
            cargando -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            }
            misOrdenes.isEmpty() -> {
                EstadoVacioOrdenes(Modifier.fillMaxSize().padding(padding))
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(misOrdenes, key = { it.id }) { orden ->
                        TarjetaOrdenUsuario(orden = orden)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOrdenUsuario(orden: Orden) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Orden #${orden.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${orden.items.size} productos • \$${String.format(Locale.US, "%.2f", orden.total)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChipEstado(estado = orden.estado)
                    IconButton(onClick = { expandido = !expandido }) {
                        Icon(
                            imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TimelineEstado(estadoActual = orden.estado)

            if (expandido) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                orden.items.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• ${item.nombre} x${item.cantidad}", style = MaterialTheme.typography.bodySmall)
                        Text("\$${String.format(Locale.US, "%.2f", item.precio * item.cantidad)}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                FilaCosto("Subtotal", orden.subtotal)
                FilaCosto("Envío", orden.envio)
                FilaCosto("Total", orden.total, true)

                Spacer(modifier = Modifier.height(8.dp))
                Text("📍 ${orden.direccion}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("💳 ${orden.metodoPago}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun FilaCosto(label: String, valor: Double, esTotal: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = if (esTotal) FontWeight.Bold else FontWeight.Normal)
        Text("\$${String.format(Locale.US, "%.2f", valor)}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (esTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (esTotal) Color(0xFF2563EB) else Color.Unspecified
        )
    }
}

@Composable
private fun TimelineEstado(estadoActual: EstadoOrden) {
    val pasos = listOf(EstadoOrden.PENDIENTE, EstadoOrden.CONFIRMADO, EstadoOrden.PREPARANDO, EstadoOrden.ENVIADO, EstadoOrden.ENTREGADO)

    if (estadoActual == EstadoOrden.CANCELADO) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Orden cancelada", color = Color.Red, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        return
    }

    val indiceActual = pasos.indexOf(estadoActual)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pasos.forEachIndexed { index, _ ->
            val completado = index <= indiceActual
            val color = if (completado) colorPorEstado(estadoActual) else Color(0xFFE2E8F0)

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                if (completado) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (index < pasos.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (index < indiceActual) color else Color(0xFFE2E8F0))
                )
            }
        }
    }
}

@Composable
private fun EstadoVacioOrdenes(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text("No tienes órdenes todavía", color = Color.Gray)
        }
    }
}
