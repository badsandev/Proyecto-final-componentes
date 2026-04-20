package com.taller.proyectofinalcomponentes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectofinal.ui.screens.CartScreen
import com.example.proyectofinal.ui.screens.CheckoutScreen

import com.taller.proyectofinalcomponentes.ui.screens.LoginScreen
import com.taller.proyectofinalcomponentes.ui.screens.HomeScreen
import com.taller.proyectofinalcomponentes.ui.screens.ProductDetailScreen
import com.taller.proyectofinalcomponentes.ui.screens.ProductListScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Login.route
    ) {

        // 🔹 LOGIN
        composable(AppRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoutes.Home.route) {
                        popUpTo(AppRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 🔹 HOME
        composable(AppRoutes.Home.route) {
            HomeScreen(
                onCategoryClick = { category ->
                    navController.navigate(AppRoutes.ProductList.createRoute(category))
                },
                onCartClick = {
                    navController.navigate(AppRoutes.Cart.route)
                },
                onProductClick = { productId ->
                    navController.navigate(AppRoutes.ProductDetail.createRoute(productId))
                }
            )
        }

        // 🔹 PRODUCT LIST
        composable(
            route = AppRoutes.ProductList.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""

            ProductListScreen(
                category = category,
                onBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(AppRoutes.ProductDetail.createRoute(productId))
                },
                onCartClick = {
                    navController.navigate(AppRoutes.Cart.route)
                }
            )
        }

        // 🔹 PRODUCT DETAIL
        composable(
            route = AppRoutes.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0

            ProductDetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onGoToCart = {
                    navController.navigate(AppRoutes.Cart.route)
                }
            )
        }

        // 🔹 CART
        composable(AppRoutes.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = {
                    navController.navigate(AppRoutes.Checkout.route)
                }
            )
        }

        // 🔹 CHECKOUT
        composable(AppRoutes.Checkout.route) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onFinish = {
                    navController.navigate(AppRoutes.Home.route) {
                        popUpTo(AppRoutes.Home.route) { inclusive = false }
                    }
                }
            )
        }
    }
}