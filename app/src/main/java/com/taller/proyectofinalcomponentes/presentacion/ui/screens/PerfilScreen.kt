package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.dominio.model.Rol
import com.taller.proyectofinalcomponentes.dominio.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: User?,
    onBack: () -> Unit,
    onCerrarSesion: () -> Unit,
    onIrAOrdenes: () -> Unit        = {},
    onIrANotificaciones: () -> Unit = {},
    onIrAFavoritos: () -> Unit      = {}
) {
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .verticalScroll(rememberScrollState())
        ) {
            // ── Encabezado ───────────────────────────────────────────────
            Box(
                modifier         = Modifier.fillMaxWidth().background(Color.White).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier         = Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = usuario?.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            style      = MaterialTheme.typography.headlineLarge,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text       = usuario?.nombre ?: "Usuario",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = usuario?.email ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(10.dp))
                    val esAdmin = usuario?.rol == Rol.ADMIN
                    Surface(
                        color = if (esAdmin) Color(0xFF7C3AED).copy(alpha = 0.1f)
                        else         Color(0xFF2563EB).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text     = if (esAdmin) " Administrador" else "👤 Usuario",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (esAdmin) Color(0xFF7C3AED) else Color(0xFF2563EB)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Opciones ─────────────────────────────────────────────────
            SeccionPerfil("Opciones") {
                FilaOpcionPerfil(
                    icono   = Icons.Default.ListAlt,
                    texto   = "Mis órdenes",
                    color   = Color(0xFF2563EB),
                    onClick = onIrAOrdenes
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                FilaOpcionPerfil(
                    icono   = Icons.Default.Favorite,
                    texto   = "Mis favoritos",
                    color   = Color(0xFFEF4444),
                    onClick = onIrAFavoritos
                )
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                FilaOpcionPerfil(
                    icono   = Icons.Default.Notifications,
                    texto   = "Notificaciones",
                    color   = Color(0xFFF59E0B),
                    onClick = onIrANotificaciones
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Cerrar sesión ─────────────────────────────────────────────
            Button(
                onClick  = { mostrarDialogoCerrarSesion = true },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Diálogo confirmar cierre de sesión ────────────────────────────────
    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            title            = { Text("Cerrar sesión") },
            text             = { Text("¿Estás seguro que deseas salir?") },
            confirmButton    = {
                Button(
                    onClick = { onCerrarSesion() },
                    colors  = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ── Composables privados ──────────────────────────────────────────────────────

@Composable
private fun SeccionPerfil(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text     = titulo,
            style    = MaterialTheme.typography.labelMedium,
            color    = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(content = contenido)
        }
    }
}

@Composable
private fun FilaOpcionPerfil(
    icono: ImageVector,
    texto: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier          = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text     = texto,
            modifier = Modifier.weight(1f),
            style    = MaterialTheme.typography.bodyMedium
        )
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
    }
}