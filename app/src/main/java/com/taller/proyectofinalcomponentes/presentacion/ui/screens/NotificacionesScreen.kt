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
import com.taller.proyectofinalcomponentes.dominio.model.Notificacion
import com.taller.proyectofinalcomponentes.dominio.model.TipoNotificacion
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.NotificacionesViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    usuarioId: String,
    viewModel: NotificacionesViewModel,
    onBack: () -> Unit
) {
    val notificaciones by viewModel.notificaciones.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    var filtroSeleccionado by remember { mutableStateOf<TipoNotificacion?>(null) }

    val notificacionesFiltradas = if (filtroSeleccionado == null) {
        notificaciones
    } else {
        notificaciones.filter { it.tipo == filtroSeleccionado }
    }

    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotEmpty()) {
            viewModel.cargarNotificaciones(usuarioId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.marcarTodasComoLeidas() }) {
                        Text("Leer todas")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtros
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TipoNotificacion.entries.take(3).forEach { tipo ->
                    FilterChip(
                        selected = filtroSeleccionado == tipo,
                        onClick = { filtroSeleccionado = if (filtroSeleccionado == tipo) null else tipo },
                        label = { Text(tipo.name) }
                    )
                }
            }

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (notificacionesFiltradas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes notificaciones", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notificacionesFiltradas, key = { it.id }) { notificacion ->
                        TarjetaNotificacion(
                            notificacion = notificacion,
                            onMarcarLeida = { viewModel.marcarComoLeida(notificacion.id) },
                            onEliminar = { viewModel.eliminarNotificacion(notificacion.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    notificacion: Notificacion,
    onMarcarLeida: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) Color.White else Color(0xFFF0F7FF)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(notificacion.titulo, fontWeight = FontWeight.Bold)
                Text(notificacion.mensaje, style = MaterialTheme.typography.bodySmall)
            }
            if (!notificacion.leida) {
                IconButton(onClick = onMarcarLeida) {
                    Icon(Icons.Default.Check, contentDescription = "Leer", tint = Color.Green)
                }
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}
