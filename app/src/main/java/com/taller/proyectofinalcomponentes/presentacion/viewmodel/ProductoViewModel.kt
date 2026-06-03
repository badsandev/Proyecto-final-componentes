package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.proyectofinalcomponentes.core.utils.NotificacionHelper
import com.taller.proyectofinalcomponentes.data.repository.ProductoRepositoryImpl
import com.taller.proyectofinalcomponentes.dominio.model.Product
import com.taller.proyectofinalcomponentes.dominio.model.TipoNotificacion
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {

    private val repo = ProductoRepositoryImpl()

    private val _cargando   = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _mensajeUI  = MutableStateFlow<String?>(null)
    val mensajeUI: StateFlow<String?> = _mensajeUI.asStateFlow()

    private val _productoEditando = MutableStateFlow<Product?>(null)
    val productoEditando: StateFlow<Product?> = _productoEditando.asStateFlow()

    // ─── Flow permanente — se actualiza en tiempo real automáticamente ────
    val productos: StateFlow<List<Product>> = repo.obtenerProductos()
        .catch { e -> _mensajeUI.value = "Error: ${e.message}" }
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5000),
            initialValue  = emptyList()
        )

    fun cargarProductos() { /* no-op, Flow ya está activo */ }

    fun cargarPorCategoria(categoria: String): Flow<List<Product>> =
        repo.obtenerProductosPorCategoria(categoria)
            .catch { e -> _mensajeUI.value = "Error: ${e.message}" }

    // ─── Agregar producto ─────────────────────────────────────────────────
    fun agregarProducto(
        nombre: String,
        categoria: String,
        precio: String,
        precioAntes: String,
        descripcion: String,
        stock: String,
        imageUrl: String = ""
    ) {
        val precioDouble = precio.toDoubleOrNull()
        val stockInt     = stock.toIntOrNull()

        if (nombre.isBlank()) { _mensajeUI.value = "El nombre es requerido"; return }
        if (precioDouble == null || precioDouble <= 0) { _mensajeUI.value = "Ingresa un precio válido"; return }
        if (stockInt == null || stockInt < 0) { _mensajeUI.value = "Ingresa un stock válido"; return }

        viewModelScope.launch {
            _cargando.value = true
            val nuevo = Product(
                name        = nombre.trim(),
                category    = categoria,
                price       = precioDouble,
                oldPrice    = precioAntes.toDoubleOrNull(),
                description = descripcion.trim(),
                stock       = stockInt,
                imageUrl    = imageUrl.trim(),
                activo      = true
            )
            repo.agregarProducto(nuevo)
                .onSuccess { _mensajeUI.value = "✓ Producto agregado correctamente" }
                .onFailure { e -> _mensajeUI.value = "Error al agregar: ${e.message}" }
            _cargando.value = false
        }
    }

    // ─── Actualizar producto ──────────────────────────────────────────────
    fun actualizarProducto(
        id: String,
        nombre: String,
        categoria: String,
        precio: String,
        precioAntes: String,
        descripcion: String,
        stock: String,
        imageUrl: String = ""
    ) {
        val precioDouble = precio.toDoubleOrNull()
        val stockInt     = stock.toIntOrNull()

        if (nombre.isBlank() || precioDouble == null || stockInt == null) {
            _mensajeUI.value = "Verifica los campos del formulario"
            return
        }

        viewModelScope.launch {
            _cargando.value = true
            val actualizado = Product(
                id          = id,
                name        = nombre.trim(),
                category    = categoria,
                price       = precioDouble,
                oldPrice    = precioAntes.toDoubleOrNull(),
                description = descripcion.trim(),
                stock       = stockInt,
                imageUrl    = imageUrl.trim(),
                activo      = true
            )
            repo.actualizarProducto(actualizado)
                .onSuccess {
                    _mensajeUI.value = "✓ Producto actualizado"

                    val productoAnterior = _productoEditando.value
                    if (productoAnterior != null) {
                        val nuevoPrecio = precioDouble
                        val precioAnterior = productoAnterior.price
                        if (nuevoPrecio < precioAnterior) {
                            viewModelScope.launch {
                                NotificacionHelper.enviarNotificacionInApp(
                                    usuarioId = "todos",
                                    titulo    = " ¡Precio reducido!",
                                    mensaje   = "${nombre.trim()} bajó de $${precioAnterior} a $${nuevoPrecio}",
                                    tipo      = TipoNotificacion.PROMO
                                )
                            }
                        }
                    }

                    _productoEditando.value = null
                }
                .onFailure { e -> _mensajeUI.value = "Error al actualizar: ${e.message}" }
            _cargando.value = false
        }
    }

    fun eliminarProducto(id: String) {
        viewModelScope.launch {
            repo.eliminarProducto(id)
                .onSuccess { _mensajeUI.value = "Producto eliminado" }
                .onFailure { e -> _mensajeUI.value = "Error: ${e.message}" }
        }
    }

    fun actualizarStock(id: String, nuevoStock: Int) {
        if (nuevoStock < 0) { _mensajeUI.value = "El stock no puede ser negativo"; return }
        viewModelScope.launch {
            repo.actualizarStock(id, nuevoStock)
                .onSuccess { _mensajeUI.value = "✓ Stock actualizado" }
                .onFailure { e -> _mensajeUI.value = "Error: ${e.message}" }
        }
    }

    fun seleccionarProductoParaEditar(producto: Product) { _productoEditando.value = producto }
    fun limpiarFormulario() { _productoEditando.value = null }
    fun limpiarMensaje() { _mensajeUI.value = null }
}