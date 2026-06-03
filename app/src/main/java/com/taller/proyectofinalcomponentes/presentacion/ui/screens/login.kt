package com.taller.proyectofinalcomponentes.presentacion.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.taller.proyectofinalcomponentes.dominio.model.User
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.AuthViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.EstadoAuth

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginExito: (User) -> Unit,
    onIrARegistro: () -> Unit
) {
    val context  = LocalContext.current
    val estadoUI by authViewModel.estadoUI.collectAsState()

    var email            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var mostrarRecuperar by remember { mutableStateOf(false) }
    var emailRecuperar   by remember { mutableStateOf("") }

    // ─── Navegación tras éxito ────────────────────────────────────────────
    LaunchedEffect(estadoUI) {
        if (estadoUI is EstadoAuth.Exito) {
            onLoginExito((estadoUI as EstadoAuth.Exito).usuario)
            authViewModel.limpiarEstado()
        }
    }

    // ─── Google Sign-In launcher ──────────────────────────────────────────
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    authViewModel.loginConGoogle(account)
                } catch (e: ApiException) {
                    authViewModel.setError("Error Google código: ${e.statusCode}")
                }
            }
            Activity.RESULT_CANCELED -> {
                authViewModel.setError("Inicio con Google cancelado")
            }
            else -> {
                authViewModel.setError("Error inesperado al iniciar con Google")
            }
        }
    }

    fun iniciarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("374798934542-kavuo8pjapph2k39ulcjm49rhtii92le.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()

        val cliente = GoogleSignIn.getClient(context, gso)

        // Siempre forzar selección de cuenta
        cliente.signOut().addOnCompleteListener {
            googleLauncher.launch(cliente.signInIntent)
        }
    }

    val gradiente = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF1E3A8A), Color(0xFF3B82F6))
    )

    // ─── Diálogo recuperar contraseña ─────────────────────────────────────
    if (mostrarRecuperar) {
        AlertDialog(
            onDismissRequest = {
                mostrarRecuperar = false
                authViewModel.limpiarEstado()
            },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text(
                        text  = "Te enviaremos un correo para restablecer tu contraseña.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = emailRecuperar,
                        onValueChange = { emailRecuperar = it },
                        label         = { Text("Correo electrónico") },
                        leadingIcon   = { Icon(Icons.Default.Email, null) },
                        shape         = RoundedCornerShape(12.dp),
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                    if (estadoUI is EstadoAuth.CorreoEnviado) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = "✓ Correo enviado. Revisa tu bandeja.",
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (estadoUI is EstadoAuth.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text  = (estadoUI as EstadoAuth.Error).mensaje,
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick  = { authViewModel.recuperarPassword(emailRecuperar) },
                    enabled  = estadoUI !is EstadoAuth.Cargando
                ) {
                    if (estadoUI is EstadoAuth.Cargando) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarRecuperar = false
                    authViewModel.limpiarEstado()
                }) { Text("Cancelar") }
            }
        )
    }

    // ─── UI principal ─────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradiente)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Logo",
                    tint     = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text       = "ShopWave",
                style      = MaterialTheme.typography.headlineMedium,
                color      = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text  = "Tu tienda del barrio, ahora digital",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(28.dp))

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(24.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {

                    Text(
                        text       = "Iniciar sesión",
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))

                    // ─── Email ────────────────────────────────────────────
                    OutlinedTextField(
                        value         = email,
                        onValueChange = { email = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Correo electrónico") },
                        leadingIcon   = { Icon(Icons.Default.Email, null) },
                        shape         = RoundedCornerShape(14.dp),
                        singleLine    = true
                    )

                    Spacer(Modifier.height(14.dp))

                    // ─── Contraseña ───────────────────────────────────────
                    OutlinedTextField(
                        value                = password,
                        onValueChange        = { password = it },
                        modifier             = Modifier.fillMaxWidth(),
                        label                = { Text("Contraseña") },
                        leadingIcon          = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Ocultar" else "Ver")
                            }
                        },
                        shape      = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    // ─── Error ────────────────────────────────────────────
                    if (estadoUI is EstadoAuth.Error) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = (estadoUI as EstadoAuth.Error).mensaje,
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // ─── Recuperar contraseña ─────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            emailRecuperar   = email
                            mostrarRecuperar = true
                            authViewModel.limpiarEstado()
                        }) {
                            Text(
                                text  = "¿Olvidaste tu contraseña?",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // ─── Botón entrar ─────────────────────────────────────
                    Button(
                        onClick  = { authViewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape   = RoundedCornerShape(14.dp),
                        enabled = estadoUI !is EstadoAuth.Cargando
                    ) {
                        if (estadoUI is EstadoAuth.Cargando) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = "Entrar",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── Divisor ──────────────────────────────────────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier          = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text  = "  o  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── Botón Google ─────────────────────────────────────
                    OutlinedButton(
                        onClick  = { iniciarGoogleSignIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape   = RoundedCornerShape(14.dp),
                        enabled = estadoUI !is EstadoAuth.Cargando
                    ) {
                        Text(
                            text       = "Continuar con Google",
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // ─── Ir a registro ────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = onIrARegistro) {
                            Text("¿No tienes cuenta? Regístrate")
                        }
                    }
                }
            }
        }
    }
}