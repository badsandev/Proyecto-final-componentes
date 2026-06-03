package com.taller.proyectofinalcomponentes.presentacion.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taller.proyectofinalcomponentes.dominio.model.Rol
import com.taller.proyectofinalcomponentes.presentacion.viewmodel.*
import com.taller.proyectofinalcomponentes.presentacion.ui.screens.*
import com.taller.proyectofinalcomponentes.presentacion.ui.screens.admin.AdminPanelScreen

@Composable
fun AppNavHost() {
    val navController     = rememberNavController()
    val authVM            : AuthViewModel           = viewModel()
    val carritoVM         : CarritoViewModel        = viewModel()
    val ordenVM           : OrdenViewModel          = viewModel()
    val notificacionesVM  : NotificacionesViewModel = viewModel()
    val favoritosVM       : FavoritosViewModel      = viewModel()
    val productoVM        : ProductoViewModel       = viewModel()

    val usuario = authVM.usuarioActual.collectAsState().value

    // Cargar datos del usuario cuando inicia sesión
    LaunchedEffect(usuario?.uid) {
        usuario?.uid?.let { uid ->
            favoritosVM.cargarFavoritos(uid)
            notificacionesVM.cargarNotificaciones(uid)
        }
    }

    NavHost(
        navController    = navController,
        startDestination = AppRoutes.Splash.route
    ) {

        // ── Splash ────────────────────────────────────────────────────────
        composable(AppRoutes.Splash.route) {
            SplashScreen(
                authViewModel    = authVM,
                onSplashComplete = { user ->
                    val destino = when {
                        user == null         -> AppRoutes.Login.route
                        user.rol == Rol.ADMIN -> AppRoutes.AdminPanel.route
                        else                 -> AppRoutes.Home.route
                    }
                    navController.navigate(destino) {
                        popUpTo(AppRoutes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ─────────────────────────────────────────────────────────
        composable(AppRoutes.Login.route) {
            LoginScreen(
                authViewModel = authVM,
                onLoginExito  = { user ->
                    val destino = if (user.rol == Rol.ADMIN)
                        AppRoutes.AdminPanel.route else AppRoutes.Home.route
                    navController.navigate(destino) {
                        popUpTo(AppRoutes.Login.route) { inclusive = true }
                    }
                },
                onIrARegistro = { navController.navigate(AppRoutes.Registro.route) }
            )
        }

        // ── Registro ──────────────────────────────────────────────────────
        composable(AppRoutes.Registro.route) {
            RegistroScreen(
                authViewModel   = authVM,
                onRegistroExito = { _ ->
                    navController.navigate(AppRoutes.Home.route) {
                        popUpTo(AppRoutes.Login.route) { inclusive = true }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(AppRoutes.Home.route) {
            HomeScreen(
                usuario               = usuario,
                carritoVM             = carritoVM,
                productoVM            = productoVM,
                favoritosVM           = favoritosVM,
                onCategoryClick       = { navController.navigate(AppRoutes.ProductList.createRoute(it)) },
                onCartClick           = { navController.navigate(AppRoutes.Carrito.route) },
                onProductClick        = { navController.navigate(AppRoutes.ProductDetail.createRoute(it)) },
                onNotificacionesClick = { navController.navigate(AppRoutes.Notificaciones.route) },
                onPerfilClick         = { navController.navigate(AppRoutes.Perfil.route) },
                onOrdenesClick        = { navController.navigate(AppRoutes.Ordenes.route) },
                onFavoritosClick      = { navController.navigate(AppRoutes.Favoritos.route) }
            )
        }

        // ── Admin Panel ───────────────────────────────────────────────────
        composable(AppRoutes.AdminPanel.route) {
            AdminPanelScreen(
                ordenViewModel  = ordenVM,
                authViewModel   = authVM,
                productoViewModel = productoVM,
                onCerrarSesion = {
                    authVM.cerrarSesion()
                    navController.navigate(AppRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Carrito ───────────────────────────────────────────────────────
        composable(AppRoutes.Carrito.route) {
            CartScreen(
                carritoVM  = carritoVM,
                onBack     = { navController.popBackStack() },
                onCheckout = { navController.navigate(AppRoutes.Checkout.route) }
            )
        }

        // ── Checkout ──────────────────────────────────────────────────────
        composable(AppRoutes.Checkout.route) {
            CheckoutScreen(
                carritoVM    = carritoVM,
                ordenVM      = ordenVM,
                usuarioId    = usuario?.uid ?: "",
                usuarioEmail = usuario?.email ?: "",
                onBack       = { navController.popBackStack() },
                onFinish     = {
                    carritoVM.vaciarCarrito()
                    navController.navigate(AppRoutes.Ordenes.route) {
                        popUpTo(AppRoutes.Checkout.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Órdenes ───────────────────────────────────────────────────────
        composable(AppRoutes.Ordenes.route) {
            OrdenesScreen(
                ordenVM   = ordenVM,
                usuarioId = usuario?.uid ?: "",
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Perfil ────────────────────────────────────────────────────────
        composable(AppRoutes.Perfil.route) {
            PerfilScreen(
                usuario             = usuario,
                onBack              = { navController.popBackStack() },
                onCerrarSesion      = {
                    authVM.cerrarSesion()
                    favoritosVM.limpiarFavoritos()
                    navController.navigate(AppRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onIrAOrdenes        = { navController.navigate(AppRoutes.Ordenes.route) },
                onIrANotificaciones = { navController.navigate(AppRoutes.Notificaciones.route) },
                onIrAFavoritos      = { navController.navigate(AppRoutes.Favoritos.route) }
            )
        }

        // ── Notificaciones ────────────────────────────────────────────────
        composable(AppRoutes.Notificaciones.route) {
            NotificacionesScreen(
                usuarioId = usuario?.uid ?: "",
                viewModel = notificacionesVM,
                onBack    = { navController.popBackStack() }
            )
        }

        // ── Favoritos ─────────────────────────────────────────────────────
        composable(AppRoutes.Favoritos.route) {
            FavoritosScreen(
                usuarioId   = usuario?.uid ?: "",
                favoritosVM = favoritosVM,
                onBack      = { navController.popBackStack() }
            )
        }

        // ── Lista de productos por categoría ──────────────────────────────
        composable(
            route     = AppRoutes.ProductList.route,
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { back ->
            val cat = back.arguments?.getString("category") ?: ""
            ProductListScreen(
                category       = cat,
                carritoVM      = carritoVM,
                productoVM     = productoVM,
                favoritosVM    = favoritosVM,
                onBack         = { navController.popBackStack() },
                onProductClick = { navController.navigate(AppRoutes.ProductDetail.createRoute(it)) },
                onCartClick    = { navController.navigate(AppRoutes.Carrito.route) }
            )
        }

        // ── Detalle de producto ───────────────────────────────────────────
        composable(
            route     = AppRoutes.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { back ->
            val id = back.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId   = id,
                carritoVM   = carritoVM,
                favoritosVM = favoritosVM,
                productoVM  = productoVM,
                onBack      = { navController.popBackStack() },
                onGoToCart  = { navController.navigate(AppRoutes.Carrito.route) }
            )
        }
    }
}