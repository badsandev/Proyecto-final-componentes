package com.taller.proyectofinalcomponentes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.taller.proyectofinalcomponentes.dominio.model.Favorito
import kotlinx.coroutines.tasks.await

class FavoritoRepositoryImpl {

    private val firestore = FirebaseFirestore.getInstance()
    private val favoritos = firestore.collection("favoritos")

    suspend fun obtenerFavoritos(usuarioId: String): Result<List<Favorito>> {
        return try {
            val snapshot = favoritos
                .whereEqualTo("usuarioId", usuarioId)
                .get().await()
            val lista = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Favorito::class.java)?.copy(id = doc.id)
            }
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun agregarFavorito(favorito: Favorito): Result<Unit> {
        return try {
            val doc   = favoritos.document()
            val conId = favorito.copy(id = doc.id)
            doc.set(conId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar por ID del documento (más confiable)
    suspend fun eliminarFavoritoPorId(favoritoId: String): Result<Unit> {
        return try {
            favoritos.document(favoritoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}