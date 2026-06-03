package com.taller.proyectofinalcomponentes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taller.proyectofinalcomponentes.dominio.model.Notificacion
import com.taller.proyectofinalcomponentes.dominio.model.TipoNotificacion

class ShopWaveMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ORDENES = "shopwave_ordenes"
        const val CHANNEL_PROMOS  = "shopwave_promos"
        const val CHANNEL_STOCK   = "shopwave_stock"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title ?: message.data["title"] ?: "ShopWave"
        val body  = message.notification?.body  ?: message.data["body"]  ?: ""
        val tipo  = message.data["tipo"]        ?: "GENERAL"

        // 1. Notificación visual (fuera y dentro de la app)
        mostrarNotificacion(title, body, tipo)

        // 2. Guardar en Firestore (centro de notificaciones in-app)
        guardarEnFirestore(title, body, tipo)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .update("fcmToken", token)
    }

    private fun mostrarNotificacion(titulo: String, cuerpo: String, tipo: String) {
        val channelId = when (tipo) {
            "ORDEN"   -> CHANNEL_ORDENES
            "PROMO"   -> CHANNEL_PROMOS
            "STOCK"   -> CHANNEL_STOCK
            else      -> CHANNEL_ORDENES
        }

        crearCanales()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("tipo", tipo)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val icono = when (tipo) {
            "ORDEN"   -> android.R.drawable.ic_dialog_info
            "PROMO"   -> android.R.drawable.ic_dialog_alert
            "STOCK"   -> android.R.drawable.ic_dialog_dialer
            else      -> android.R.drawable.ic_dialog_info
        }

        val notificacion = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(icono)
            .setContentTitle(titulo)
            .setContentText(cuerpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuerpo))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notificacion)
    }

    private fun crearCanales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            listOf(
                Triple(CHANNEL_ORDENES, "Órdenes",      NotificationManager.IMPORTANCE_HIGH),
                Triple(CHANNEL_PROMOS,  "Promociones",   NotificationManager.IMPORTANCE_DEFAULT),
                Triple(CHANNEL_STOCK,   "Stock",         NotificationManager.IMPORTANCE_HIGH)
            ).forEach { (id, nombre, importancia) ->
                if (manager.getNotificationChannel(id) == null) {
                    NotificationChannel(id, nombre, importancia).also {
                        it.enableLights(true)
                        it.enableVibration(true)
                        manager.createNotificationChannel(it)
                    }
                }
            }
        }
    }

    private fun guardarEnFirestore(titulo: String, mensaje: String, tipoStr: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val tipo = try { TipoNotificacion.valueOf(tipoStr) }
        catch (e: Exception) { TipoNotificacion.GENERAL }

        FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .add(
                Notificacion(
                    usuarioId = uid,
                    titulo    = titulo,
                    mensaje   = mensaje,
                    tipo      = tipo,
                    leida     = false,
                    fecha     = System.currentTimeMillis()
                )
            )
    }
}