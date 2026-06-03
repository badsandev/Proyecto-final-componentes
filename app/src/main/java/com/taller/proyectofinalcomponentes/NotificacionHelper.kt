package com.taller.proyectofinalcomponentes.core.utils

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.taller.proyectofinalcomponentes.dominio.model.Notificacion
import com.taller.proyectofinalcomponentes.dominio.model.TipoNotificacion
import kotlinx.coroutines.tasks.await

object NotificacionHelper {

    private val firestore = FirebaseFirestore.getInstance()

    // ─── Guardar notificación en Firestore (in-app) ───────────────────────
    suspend fun enviarNotificacionInApp(
        usuarioId: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion
    ) {
        try {
            firestore.collection("notificaciones").add(
                Notificacion(
                    usuarioId = usuarioId,
                    titulo    = titulo,
                    mensaje   = mensaje,
                    tipo      = tipo,
                    leida     = false,
                    fecha     = System.currentTimeMillis()
                )
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ─── Suscribir usuario a tópicos FCM ──────────────────────────────────
    fun suscribirATemas() {
        FirebaseMessaging.getInstance().apply {
            subscribeToTopic("promociones")   // recibe promos
            subscribeToTopic("nuevos_productos") // recibe nuevos productos
            subscribeToTopic("todos")         // notificaciones generales
        }
    }

    // ─── Desuscribir al cerrar sesión ─────────────────────────────────────
    fun desuscribirDeTemas() {
        FirebaseMessaging.getInstance().apply {
            unsubscribeFromTopic("promociones")
            unsubscribeFromTopic("nuevos_productos")
            unsubscribeFromTopic("todos")
        }
    }
}