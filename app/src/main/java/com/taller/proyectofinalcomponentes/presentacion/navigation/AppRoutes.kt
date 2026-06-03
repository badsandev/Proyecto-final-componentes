package com.taller.proyectofinalcomponentes.presentacion.navigation

sealed class AppRoutes(val route: String) {
    object Splash         : AppRoutes("splash")
    object Login          : AppRoutes("login")
    object Registro       : AppRoutes("registro")
    object Home           : AppRoutes("home")
    object AdminPanel     : AppRoutes("admin_panel")
    object Carrito        : AppRoutes("carrito")
    object Checkout       : AppRoutes("checkout")
    object Ordenes        : AppRoutes("ordenes")
    object Perfil         : AppRoutes("perfil")
    object Notificaciones : AppRoutes("notificaciones")
    object Favoritos      : AppRoutes("favoritos")

    object ProductList : AppRoutes("products/{category}") {
        fun createRoute(category: String) = "products/$category"
    }
    object ProductDetail : AppRoutes("detail/{productId}") {
        fun createRoute(id: String) = "detail/$id"
    }
}