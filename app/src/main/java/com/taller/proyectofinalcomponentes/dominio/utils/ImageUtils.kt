package com.taller.proyectofinalcomponentes.core.utils

object ImageUtils {


    fun getFullUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path.trim()

        return if (path.startsWith("/")) path else "/$path"
    }


    fun esUrlValida(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase().trim()
        return (lower.startsWith("http://") || lower.startsWith("https://")) &&
                (lower.contains(".jpg")  ||
                        lower.contains(".jpeg") ||
                        lower.contains(".png")  ||
                        lower.contains(".webp") ||
                        lower.contains(".gif")  ||
                        lower.contains("unsplash") ||
                        lower.contains("picsum")   ||
                        lower.contains("firebasestorage") ||
                        lower.contains("googleusercontent"))
    }
}