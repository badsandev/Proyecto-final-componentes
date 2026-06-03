package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.dominio.model.User
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.AuthViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.EstadoAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onSplashComplete: (User?) -> Unit
) {
    val scale          = remember { Animatable(0f) }
    val alpha          = remember { Animatable(0f) }
    val estadoAuth     by authViewModel.estadoUI.collectAsState()
    val usuario        by authViewModel.usuarioActual.collectAsState()
    var animacionLista by remember { mutableStateOf(false) }

    // Animación de entrada
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue   = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessLow
            )
        )
        alpha.animateTo(
            targetValue   = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(1500)
        animacionLista = true
    }

    // Navegar cuando la animación terminó y ya se sabe el estado de auth
    LaunchedEffect(animacionLista, estadoAuth) {
        if (!animacionLista) return@LaunchedEffect
        when (estadoAuth) {
            is EstadoAuth.Exito    -> onSplashComplete(usuario)
            is EstadoAuth.Inactivo -> onSplashComplete(null)
            else                   -> Unit // Cargando → esperar
        }
    }

    val gradiente = Brush.verticalGradient(
        listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF3B82F6))
    )

    Box(
        modifier         = Modifier.fillMaxSize().background(gradiente),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.ShoppingCart,
                    contentDescription = "ShopWave",
                    tint               = Color.White,
                    modifier           = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text       = "ShopWave",
                style      = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color      = Color.White.copy(alpha = alpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "Tu tienda del barrio, ahora digital",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = alpha.value * 0.8f)
            )

            Spacer(Modifier.height(60.dp))

            CircularProgressIndicator(
                color       = Color.White.copy(alpha = 0.6f),
                strokeWidth = 2.dp,
                modifier    = Modifier.size(28.dp)
            )
        }
    }
}