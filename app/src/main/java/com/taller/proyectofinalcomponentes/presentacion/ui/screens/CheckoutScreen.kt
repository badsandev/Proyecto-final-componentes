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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.CarritoViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.OrdenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    carritoVM: CarritoViewModel,
    ordenVM: OrdenViewModel,
    usuarioId: String,
    usuarioEmail: String,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val items by carritoVM.items.collectAsState()
    val cargando by ordenVM.cargando.collectAsState()

    val subtotal = items.sumOf { it.precio * it.cantidad }
    val envio = 5.0
    val total = subtotal + envio

    var direccion by remember { mutableStateOf("") }
    var metodoPago by remember { mutableStateOf("") }
    var errorDireccion by remember { mutableStateOf(false) }
    var errorPago by remember { mutableStateOf(false) }
    var ordenConfirmada by remember { mutableStateOf(false) }

    // Métodos de pago disponibles
    val metodosPago = listOf(
        MetodoPago("Tarjeta de crédito/débito", Icons.Default.CreditCard),
        MetodoPago("Google Pay", Icons.Default.Payments),
        MetodoPago("PayPal", Icons.Default.AccountBalanceWallet),
        MetodoPago("Efectivo", Icons.Default.Money)
    )

    // Cuando se confirma la orden navegar a órdenes
    LaunchedEffect(ordenConfirmada) {
        if (ordenConfirmada) {
            onFinish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Finalizar compra",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ─── Paso 1: Dirección ────────────────────────────────────────
            SeccionCheckout(
                numero = "1",
                titulo = "Dirección de entrega",
                icono = Icons.Default.LocationOn
            ) {
                OutlinedTextField(
                    value = direccion,
                    onValueChange = {
                        direccion = it
                        errorDireccion = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ej: Calle 123 #45-67, Bogotá") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFF2563EB)
                        )
                    },
                    isError = errorDireccion,
                    supportingText = {
                        if (errorDireccion) {
                            Text(
                                text = "Ingresa tu dirección de entrega",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // ─── Paso 2: Método de pago ───────────────────────────────────
            SeccionCheckout(
                numero = "2",
                titulo = "Método de pago",
                icono = Icons.Default.Payment
            ) {
                if (errorPago) {
                    Text(
                        text = "Selecciona un método de pago",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    metodosPago.forEach { metodo ->
                        TarjetaMetodoPago(
                            metodo = metodo,
                            seleccionado = metodoPago == metodo.nombre,
                            onClick = {
                                metodoPago = metodo.nombre
                                errorPago = false
                            }
                        )
                    }
                }
            }

            // ─── Paso 3: Resumen ──────────────────────────────────────────
            SeccionCheckout(
                numero = "3",
                titulo = "Resumen del pedido",
                icono = Icons.Default.Receipt
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.nombre} x${item.cantidad}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$${"%.2f".format(item.precio * item.cantidad)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    FilaResumenCheckout(
                        label = "Subtotal",
                        valor = "$${"%.2f".format(subtotal)}"
                    )
                    FilaResumenCheckout(
                        label = "Envío",
                        valor = "$${"%.2f".format(envio)}"
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(4.dp))

                    FilaResumenCheckout(
                        label = "Total",
                        valor = "$${"%.2f".format(total)}",
                        negrita = true,
                        colorValor = Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ─── Botón confirmar ──────────────────────────────────────────
            Button(
                onClick = {
                    errorDireccion = direccion.isBlank()
                    errorPago = metodoPago.isBlank()

                    if (!errorDireccion && !errorPago) {
                        ordenVM.crearOrden(
                            usuarioId    = usuarioId,
                            usuarioEmail = usuarioEmail,
                            items        = items,
                            direccion    = direccion,
                            metodoPago   = metodoPago
                        )
                        ordenConfirmada = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB)
                ),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmar pedido",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── Sección checkout ─────────────────────────────────────────────────────────

@Composable
private fun SeccionCheckout(
    numero: String,
    titulo: String,
    icono: ImageVector,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado sección
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2563EB), RoundedCornerShape(50.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numero,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            contenido()
        }
    }
}

// ─── Tarjeta método de pago ───────────────────────────────────────────────────

data class MetodoPago(
    val nombre: String,
    val icono: ImageVector
)

@Composable
private fun TarjetaMetodoPago(
    metodo: MetodoPago,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado)
                Color(0xFF2563EB).copy(alpha = 0.08f)
            else
                Color(0xFFF8FAFC)
        ),
        border = CardDefaults.outlinedCardBorder().let {
            if (seleccionado) it else it
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (seleccionado) Color(0xFF2563EB).copy(alpha = 0.15f)
                        else Color(0xFFE2E8F0),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = metodo.icono,
                    contentDescription = null,
                    tint = if (seleccionado) Color(0xFF2563EB) else Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = metodo.nombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
                color = if (seleccionado) Color(0xFF2563EB) else Color(0xFF0F172A)
            )

            RadioButton(
                selected = seleccionado,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF2563EB)
                )
            )
        }
    }
}

// ─── Fila resumen ─────────────────────────────────────────────────────────────

@Composable
private fun FilaResumenCheckout(
    label: String,
    valor: String,
    negrita: Boolean = false,
    colorValor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF64748B)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            color = colorValor
        )
    }
}