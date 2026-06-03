package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.dominio.model.Favorito
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.FavoritosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritosScreen(
    usuarioId: String,
    favoritosVM: FavoritosViewModel,
    onBack: () -> Unit
) {
    val favoritos by favoritosVM.favoritos.collectAsState()

    LaunchedEffect(usuarioId) {
        if (usuarioId.isNotBlank()) favoritosVM.cargarFavoritos(usuarioId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis favoritos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (favoritos.isEmpty()) {
            // ── Estado vacío ─────────────────────────────────────────────
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Favorite, null,
                        modifier = Modifier.size(72.dp),
                        tint     = Color(0xFFE2E8F0)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Sin favoritos aún",
                        style      = MaterialTheme.typography.titleMedium,
                        color      = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Guarda los productos que te interesen",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier              = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(favoritos, key = { it.id }) { fav ->
                    TarjetaFavorito(
                        favorito   = fav,
                        onEliminar = { favoritosVM.eliminarFavorito(fav.productoId) }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun TarjetaFavorito(
    favorito: Favorito,
    onEliminar: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de categoría
            Surface(
                modifier = Modifier.size(52.dp),
                shape    = RoundedCornerShape(12.dp),
                color    = Color(0xFFF1F5F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Favorite, null,
                        tint     = Color(0xFFEF4444),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    favorito.nombre,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1
                )
                Text(
                    favorito.categoria,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$${String.format("%.2f", favorito.precio)}",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Color(0xFF2563EB)
                    )
                    val old = favorito.oldPrice ?: 0.0
                    if (old > favorito.precio) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "$${String.format("%.2f", old)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444))
            }
        }
    }
}