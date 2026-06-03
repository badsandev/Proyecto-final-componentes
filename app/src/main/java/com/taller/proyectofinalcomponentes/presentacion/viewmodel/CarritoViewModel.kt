package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import com.taller.proyectofinalcomponentes.dominio.model.ItemCarrito
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CarritoViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val items: StateFlow<List<ItemCarrito>> = _items

    val total: Double get() = _items.value.sumOf { it.precio * it.cantidad }
    val cantidadTotal: Int get() = _items.value.sumOf { it.cantidad }

    fun agregarItem(item: ItemCarrito) {
        val lista = _items.value.toMutableList()
        val existente = lista.indexOfFirst { it.productoId == item.productoId }
        if (existente >= 0) {
            lista[existente] = lista[existente].copy(
                cantidad = lista[existente].cantidad + 1
            )
        } else {
            lista.add(item)
        }
        _items.value = lista
    }

    fun eliminarItem(productoId: String) {
        _items.value = _items.value.filter { it.productoId != productoId }
    }

    fun aumentarCantidad(productoId: String) {
        _items.value = _items.value.map {
            if (it.productoId == productoId) it.copy(cantidad = it.cantidad + 1) else it
        }
    }

    fun disminuirCantidad(productoId: String) {
        val lista = _items.value.map {
            if (it.productoId == productoId) it.copy(cantidad = it.cantidad - 1) else it
        }
        _items.value = lista.filter { it.cantidad > 0 }
    }

    fun vaciarCarrito() {
        _items.value = emptyList()
    }
}
