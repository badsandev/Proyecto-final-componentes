package com.taller.proyectofinalcomponentes.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.taller.proyectofinalcomponentes.dominio.model.Notificacion
import kotlinx.coroutines.tasks.await

class NotificacionRepositoryImpl {

    private val firestore      = FirebaseFirestore.getInstance()
    private val notificaciones = firestore.collection("notificaciones")

    // ─── Obtener notificaciones del usuario ───────────────────────────────
    suspend fun obtenerNotificaciones(usuarioId: String): Result<List<Notificacion>> {
        return try {
            val snapshot = notificaciones
                .whereEqualTo("usuarioId", usuarioId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()
            val lista = snapshot.toObjects(Notificacion::class.java)
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Crear notificación ───────────────────────────────────────────────
    suspend fun crearNotificacion(notificacion: Notificacion): Result<Unit> {
        return try {
            val doc = notificaciones.document()
            val conId = notificacion.copy(id = doc.id)
            doc.set(conId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Marcar como leída ────────────────────────────────────────────────
    suspend fun marcarComoLeida(notificacionId: String): Result<Unit> {
        return try {
            notificaciones.document(notificacionId)
                .update("leida", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Marcar todas como leídas ─────────────────────────────────────────
    suspend fun marcarTodasComoLeidas(usuarioId: String): Result<Unit> {
        return try {
            val snapshot = notificaciones
                .whereEqualTo("usuarioId", usuarioId)
                .whereEqualTo("leida", false)
                .get()
                .await()
            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "leida", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Eliminar notificación ────────────────────────────────────────────
    suspend fun eliminarNotificacion(notificacionId: String): Result<Unit> {
        return try {
            notificaciones.document(notificacionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}