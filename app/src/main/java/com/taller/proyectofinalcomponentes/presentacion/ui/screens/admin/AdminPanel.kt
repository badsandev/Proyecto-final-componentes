package com.taller.proyectofinalcomponentes.presentacion.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taller.proyectofinalcomponentes.core.utils.ImageUtils
import com.taller.proyectofinalcomponentes.dominio.model.EstadoOrden
import com.taller.proyectofinalcomponentes.dominio.model.Orden
import com.taller.proyectofinalcomponentes.dominio.model.Product
import com.taller.proyectofinalcomponentes.presentacion.ui.screens.ImagenProducto
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.AuthViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.OrdenViewModel
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.ProductoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    ordenViewModel: OrdenViewModel,
    authViewModel: AuthViewModel,
    onCerrarSesion: () -> Unit
) {
    val productoVM: ProductoViewModel = viewModel()

    val ordenes      by ordenViewModel.ordenes.collectAsState()
    val cargandoOrd  by ordenViewModel.cargando.collectAsState()
    val productos    by productoVM.productos.collectAsState()
    val cargandoProd by productoVM.cargando.collectAsState()
    val mensajeUI    by productoVM.mensajeUI.collectAsState()
    val usuario      by authViewModel.usuarioActual.collectAsState()

    var tabSeleccionado   by remember { mutableIntStateOf(0) }
    val tabs              = listOf("Resumen", "Órdenes", "Productos")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        ordenViewModel.cargarTodasLasOrdenes()
    }

    LaunchedEffect(mensajeUI) {
        mensajeUI?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            productoVM.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Admin", fontWeight = FontWeight.Bold)
                        usuario?.let {
                            Text(
                                it.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            "Salir",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = tabSeleccionado) {
                tabs.forEachIndexed { i, titulo ->
                    Tab(
                        selected = tabSeleccionado == i,
                        onClick  = { tabSeleccionado = i },
                        text     = { Text(titulo) }
                    )
                }
            }

            when (tabSeleccionado) {
                0 -> TabResumen(ordenes, productos)
                1 -> TabOrdenes(ordenes, cargandoOrd, ordenViewModel)
                2 -> TabProductos(productos, cargandoProd, productoVM)
            }
        }
    }
}

// ─── TAB RESUMEN ──────────────────────────────────────────────────────────────

@Composable
private fun TabResumen(ordenes: List<Orden>, productos: List<Product>) {
    val totalVentas = ordenes.filter { it.estado == EstadoOrden.ENTREGADO }.sumOf { it.total }
    val pendientes  = ordenes.count { it.estado == EstadoOrden.PENDIENTE }
    val entregadas  = ordenes.count { it.estado == EstadoOrden.ENTREGADO }
    val stockBajo   = productos.count { it.stock in 1..5 }
    val sinStock    = productos.count { it.stock == 0 }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Ingresos Totales", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                    Text(String.format(Locale.US, "$%.2f", totalVentas), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("$entregadas órdenes entregadas", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaEstadistica(Modifier.weight(1f), Icons.Default.Schedule,    "Pendientes", pendientes.toString(), Color(0xFFF59E0B))
                TarjetaEstadistica(Modifier.weight(1f), Icons.Default.CheckCircle, "Entregadas", entregadas.toString(), Color(0xFF10B981))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaEstadistica(Modifier.weight(1f), Icons.Default.Warning, "Stock bajo", stockBajo.toString(), Color(0xFFF59E0B))
                TarjetaEstadistica(Modifier.weight(1f), Icons.Default.Cancel,  "Sin stock",  sinStock.toString(),  Color(0xFFEF4444))
            }
        }

        item {
            Text("Órdenes recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(ordenes.take(3)) { orden ->
            TarjetaOrdenResumen(orden)
        }
    }
}

@Composable
private fun TarjetaEstadistica(
    modifier: Modifier,
    icono: ImageVector,
    titulo: String,
    valor: String,
    color: Color
) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier         = Modifier.size(38.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, titulo, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(titulo, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
        }
    }
}

@Composable
private fun TarjetaOrdenResumen(orden: Orden) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(orden.usuarioEmail, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${orden.items.size} producto(s)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(String.format(Locale.US, "$%.2f", orden.total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                ChipEstado(orden.estado)
            }
        }
    }
}

// ─── TAB ÓRDENES ──────────────────────────────────────────────────────────────

@Composable
private fun TabOrdenes(ordenes: List<Orden>, cargando: Boolean, ordenVM: OrdenViewModel) {
    if (cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2563EB))
        }
        return
    }
    if (ordenes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("No hay órdenes", color = Color(0xFF64748B))
            }
        }
        return
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }
        items(ordenes) { orden ->
            TarjetaOrdenAdmin(orden) { nuevoEstado -> ordenVM.actualizarEstado(orden.id, nuevoEstado) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun TarjetaOrdenAdmin(orden: Orden, onCambiarEstado: (EstadoOrden) -> Unit) {
    var expandido   by remember { mutableStateOf(false) }
    var mostrarMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(3.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Orden #${orden.id.take(8).uppercase()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(orden.usuarioEmail, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChipEstado(orden.estado)
                    IconButton(onClick = { expandido = !expandido }) {
                        Icon(if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = Color(0xFF64748B))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${orden.items.size} producto(s)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                Text(String.format(Locale.US, "Total: $%.2f", orden.total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }

            if (expandido) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(Modifier.height(10.dp))
                Text("Productos:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                orden.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• ${item.nombre} x${item.cantidad}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                        Text(String.format(Locale.US, "$%.2f", item.precio * item.cantidad), style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("📍 ${orden.direccion}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                Text("💳 ${orden.metodoPago}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                Spacer(Modifier.height(10.dp))

                Box {
                    OutlinedButton(onClick = { mostrarMenu = true }, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cambiar estado")
                    }
                    DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                        EstadoOrden.entries.forEach { estado ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(10.dp).background(colorPorEstado(estado), RoundedCornerShape(50)))
                                        Spacer(Modifier.width(8.dp))
                                        Text(estado.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                },
                                onClick = { onCambiarEstado(estado); mostrarMenu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── TAB PRODUCTOS ────────────────────────────────────────────────────────────

@Composable
private fun TabProductos(productos: List<Product>, cargando: Boolean, productoVM: ProductoViewModel) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    val productoEditando  by productoVM.productoEditando.collectAsState()

    LaunchedEffect(productoEditando) {
        if (productoEditando != null) mostrarFormulario = true
    }

    if (mostrarFormulario) {
        FormularioProducto(
            producto  = productoEditando,
            onGuardar = { nombre, cat, precio, precioAntes, desc, stock, imageUrl ->
                if (productoEditando != null) {
                    productoVM.actualizarProducto(productoEditando!!.id, nombre, cat, precio, precioAntes, desc, stock, imageUrl)
                } else {
                    productoVM.agregarProducto(nombre, cat, precio, precioAntes, desc, stock, imageUrl)
                }
                mostrarFormulario = false
                productoVM.limpiarFormulario()
            },
            onCancelar = { mostrarFormulario = false; productoVM.limpiarFormulario() }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            cargando && productos.isEmpty() -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2563EB))
            productos.isEmpty() -> Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inventory2, null, tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text("No hay productos", color = Color(0xFF64748B))
            }
            else -> LazyColumn(
                modifier            = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(productos, key = { it.id }) { producto ->
                    TarjetaProductoAdmin(
                        producto   = producto,
                        onEditar   = { productoVM.seleccionarProductoParaEditar(producto) },
                        onEliminar = { productoVM.eliminarProducto(producto.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }

        FloatingActionButton(
            onClick        = { productoVM.limpiarFormulario(); mostrarFormulario = true },
            modifier       = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = Color(0xFF2563EB)
        ) {
            Icon(Icons.Default.Add, "Agregar producto", tint = Color.White)
        }
    }
}

@Composable
private fun TarjetaProductoAdmin(producto: Product, onEditar: () -> Unit, onEliminar: () -> Unit) {
    var confirmarEliminar by remember { mutableStateOf(false) }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title            = { Text("Eliminar producto") },
            text             = { Text("¿Eliminar \"${producto.name}\"?") },
            confirmButton    = {
                TextButton(onClick = { onEliminar(); confirmarEliminar = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenProducto(imageUrl = producto.imageUrl, nombre = producto.name, categoria = producto.category, size = 64.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(producto.category, style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(String.format(Locale.US, "$%.0f", producto.price), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    val colorStock = when {
                        producto.stock == 0 -> Color(0xFFEF4444)
                        producto.stock <= 5 -> Color(0xFFF59E0B)
                        else                -> Color(0xFF10B981)
                    }
                    Box(modifier = Modifier.background(colorStock.copy(alpha = 0.12f), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("Stock: ${producto.stock}", style = MaterialTheme.typography.labelSmall, color = colorStock, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Column {
                IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, "Editar", tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp)) }
                IconButton(onClick = { confirmarEliminar = true }) { Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp)) }
            }
        }
    }
}

// ─── FORMULARIO PRODUCTO ──────────────────────────────────────────────────────

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioProducto(
    producto: Product?,
    onGuardar: (String, String, String, String, String, String, String) -> Unit,
    onCancelar: () -> Unit
) {
    val categorias = listOf("Electrónica", "Moda", "Hogar", "Belleza", "Deportes", "Alimentación")

    var nombre      by remember { mutableStateOf(producto?.name ?: "") }
    var categoria   by remember { mutableStateOf(producto?.category ?: categorias[0]) }
    var precio      by remember { mutableStateOf(producto?.price?.toString() ?: "") }
    var precioAntes by remember { mutableStateOf(producto?.oldPrice?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(producto?.description ?: "") }
    var stock       by remember { mutableStateOf(producto?.stock?.toString() ?: "") }
    var imageUrl    by remember { mutableStateOf(producto?.imageUrl ?: "") }
    var expandirCat by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancelar) { Icon(Icons.Default.Close, "Cancelar") }
            Text(if (producto != null) "Editar producto" else "Nuevo producto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (ImageUtils.esUrlValida(imageUrl)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ImagenProducto(imageUrl = imageUrl, nombre = nombre, categoria = categoria, size = 80.dp, cornerRadius = 10.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Vista previa de la imagen", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
            }
        }

        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, modifier = Modifier.fillMaxWidth(), label = { Text("URL de la imagen") }, placeholder = { Text("https://ejemplo.com/imagen.jpg") }, leadingIcon = { Icon(Icons.Default.Image, null) }, shape = RoundedCornerShape(12.dp), singleLine = true)

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Nombre del producto") }, leadingIcon = { Icon(Icons.Default.ShoppingBag, null) }, shape = RoundedCornerShape(12.dp), singleLine = true)

        // ── Categoría ─────────────────────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded         = expandirCat,
            onExpandedChange = { expandirCat = it }
        ) {
            OutlinedTextField(
                value         = categoria,
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Categoría") },
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expandirCat) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape         = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = expandirCat, onDismissRequest = { expandirCat = false }) {
                categorias.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = { categoria = cat; expandirCat = false })
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = precio, onValueChange = { precio = it }, modifier = Modifier.weight(1f), label = { Text("Precio") }, leadingIcon = { Icon(Icons.Default.AttachMoney, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), singleLine = true)
            OutlinedTextField(value = precioAntes, onValueChange = { precioAntes = it }, modifier = Modifier.weight(1f), label = { Text("Precio anterior") }, leadingIcon = { Icon(Icons.Default.AttachMoney, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), singleLine = true)
        }

        OutlinedTextField(value = stock, onValueChange = { stock = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Stock disponible") }, leadingIcon = { Icon(Icons.Default.Inventory, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), singleLine = true)

        OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, modifier = Modifier.fillMaxWidth().height(100.dp), label = { Text("Descripción") }, leadingIcon = { Icon(Icons.Default.Description, null) }, shape = RoundedCornerShape(12.dp), maxLines = 4)

        Spacer(Modifier.height(8.dp))

        Button(
            onClick  = { onGuardar(nombre, categoria, precio, precioAntes, descripcion, stock, imageUrl) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            enabled  = nombre.isNotBlank() && precio.isNotBlank() && stock.isNotBlank()
        ) {
            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (producto != null) "Actualizar producto" else "Agregar producto", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Chip estado y color ──────────────────────────────────────────────────────

@Composable
fun ChipEstado(estado: EstadoOrden) {
    val color = colorPorEstado(estado)
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(estado.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

fun colorPorEstado(estado: EstadoOrden): Color = when (estado) {
    EstadoOrden.PENDIENTE  -> Color(0xFFF59E0B)
    EstadoOrden.CONFIRMADO -> Color(0xFF2196F3)
    EstadoOrden.PREPARANDO -> Color(0xFF9C27B0)
    EstadoOrden.ENVIADO    -> Color(0xFF00BCD4)
    EstadoOrden.ENTREGADO  -> Color(0xFF4CAF50)
    EstadoOrden.CANCELADO  -> Color(0xFFF44336)
}