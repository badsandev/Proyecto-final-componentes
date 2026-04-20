package com.taller.proyectofinalcomponentes.navigation


sealed class AppRoutes(val route: String) {
    data object Login : AppRoutes("login")
    data object Home : AppRoutes("home")
    data object ProductList : AppRoutes("product_list/{category}") {
        fun createRoute(category: String) = "product_list/$category"
    }
    data object ProductDetail : AppRoutes("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
    data object Cart : AppRoutes("cart")
    data object Checkout : AppRoutes("checkout")
}