package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taller.proyectofinalcomponentes.data.repository.FavoritoRepositoryImpl
import com.taller.proyectofinalcomponentes.dominio.model.Favorito
import com.taller.proyectofinalcomponentes.dominio.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoritosViewModel : ViewModel() {

    private val repo = FavoritoRepositoryImpl()

    private val _favoritos = MutableStateFlow<List<Favorito>>(emptyList())
    val favoritos: StateFlow<List<Favorito>> = _favoritos

    private var _usuarioId = ""

    fun cargarFavoritos(usuarioId: String) {
        if (usuarioId.isBlank()) return
        _usuarioId = usuarioId
        viewModelScope.launch {
            repo.obtenerFavoritos(usuarioId)
                .onSuccess { lista -> _favoritos.value = lista }
        }
    }

    fun esFavorito(productoId: String): Boolean =
        _favoritos.value.any { it.productoId == productoId }

    fun toggleFavorito(product: Product) {
        val yaEsFavorito = esFavorito(product.id)
        viewModelScope.launch {
            if (yaEsFavorito) {
                val favoritoId = _favoritos.value
                    .firstOrNull { it.productoId == product.id }?.id ?: return@launch
                repo.eliminarFavoritoPorId(favoritoId)
                    .onSuccess {
                        _favoritos.value = _favoritos.value.filter { it.productoId != product.id }
                    }
            } else {
                val nuevo = Favorito(
                    id = "", // Firestore generará el ID
                    usuarioId = _usuarioId,
                    productoId = product.id,
                    nombre = product.name,
                    precio = product.price,
                    oldPrice = product.oldPrice,
                    categoria = product.category,
                    rating = product.rating
                )
                repo.agregarFavorito(nuevo)
                    .onSuccess {
                        cargarFavoritos(_usuarioId)
                    }
            }
        }
    }

    fun eliminarFavorito(productoId: String) {
        val favoritoId = _favoritos.value
            .firstOrNull { it.productoId == productoId }?.id ?: return
        viewModelScope.launch {
            repo.eliminarFavoritoPorId(favoritoId)
                .onSuccess {
                    _favoritos.value = _favoritos.value.filter { it.productoId != productoId }
                }
        }
    }

    fun limpiarFavoritos() { _favoritos.value = emptyList() }
}
