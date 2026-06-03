package com.taller.proyectofinalcomponentes.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.taller.proyectofinalcomponentes.data.repository.AuthRepositoryImpl
import com.taller.proyectofinalcomponentes.dominio.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepositoryImpl()

    private val _estadoUI = MutableStateFlow<EstadoAuth>(EstadoAuth.Inactivo)
    val estadoUI: StateFlow<EstadoAuth> = _estadoUI.asStateFlow()

    private val _usuarioActual = MutableStateFlow<User?>(null)
    val usuarioActual: StateFlow<User?> = _usuarioActual.asStateFlow()

    val loading: StateFlow<Boolean> = _estadoUI
        .map { it is EstadoAuth.Cargando }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val error: StateFlow<String?> = _estadoUI
        .map { if (it is EstadoAuth.Error) it.mensaje else null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ─── Sesión persistente al abrir la app ──────────────────────────────
    init {
        verificarSesionActiva()
    }

    private fun verificarSesionActiva() {
        if (repo.estaAutenticado()) {
            viewModelScope.launch {
                val user = repo.obtenerUsuarioActualCompleto()
                if (user != null) {
                    _usuarioActual.value = user
                }
            }
        }
    }

    // ─── Login email/password ─────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _estadoUI.value = EstadoAuth.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _estadoUI.value = EstadoAuth.Cargando
            repo.loginConEmail(email, password)
                .onSuccess { user ->
                    // Recargamos desde Firestore para asegurar el rol correcto
                    val userCompleto = repo.obtenerUsuarioActualCompleto() ?: user
                    _usuarioActual.value = userCompleto
                    _estadoUI.value = EstadoAuth.Exito(userCompleto)
                }
                .onFailure { e ->
                    _estadoUI.value = EstadoAuth.Error(
                        traducirError(e.message ?: "Error desconocido")
                    )
                }
        }
    }
    fun setError(mensaje: String) {
        _estadoUI.value = EstadoAuth.Error(mensaje)
    }

    // ─── Registro ─────────────────────────────────────────────────────────
    fun registrar(email: String, password: String, nombre: String) {
        when {
            nombre.isBlank() -> {
                _estadoUI.value = EstadoAuth.Error("Ingresa tu nombre")
                return
            }
            email.isBlank() -> {
                _estadoUI.value = EstadoAuth.Error("Ingresa un correo válido")
                return
            }
            password.length < 6 -> {
                _estadoUI.value = EstadoAuth.Error("La contraseña debe tener mínimo 6 caracteres")
                return
            }
        }
        viewModelScope.launch {
            _estadoUI.value = EstadoAuth.Cargando
            repo.registrarConEmail(email, password, nombre)
                .onSuccess { user ->
                    _usuarioActual.value = user
                    _estadoUI.value = EstadoAuth.Exito(user)
                }
                .onFailure { e ->
                    _estadoUI.value = EstadoAuth.Error(
                        traducirError(e.message ?: "Error desconocido")
                    )
                }
        }
    }

    // ─── Google Sign-In ───────────────────────────────────────────────────
    fun loginConGoogle(acct: GoogleSignInAccount?) {
        if (acct == null) {
            _estadoUI.value = EstadoAuth.Error("No se pudo iniciar sesión con Google")
            return
        }
        viewModelScope.launch {
            _estadoUI.value = EstadoAuth.Cargando
            repo.loginConGoogle(acct)
                .onSuccess { user ->
                    _usuarioActual.value = user
                    _estadoUI.value = EstadoAuth.Exito(user)
                }
                .onFailure { e ->
                    _estadoUI.value = EstadoAuth.Error(
                        traducirError(e.message ?: "Error con Google")
                    )
                }
        }
    }

    // ─── Recuperar contraseña ─────────────────────────────────────────────
    fun recuperarPassword(email: String) {
        if (email.isBlank()) {
            _estadoUI.value = EstadoAuth.Error("Ingresa tu correo electrónico")
            return
        }
        viewModelScope.launch {
            _estadoUI.value = EstadoAuth.Cargando
            repo.recuperarPassword(email)
                .onSuccess { _estadoUI.value = EstadoAuth.CorreoEnviado }
                .onFailure { e ->
                    _estadoUI.value = EstadoAuth.Error(
                        traducirError(e.message ?: "Error al enviar correo")
                    )
                }
        }
    }

    // ─── Cerrar sesión ────────────────────────────────────────────────────
    fun cerrarSesion() {
        viewModelScope.launch {
            repo.cerrarSesion()
            _usuarioActual.value = null
            _estadoUI.value = EstadoAuth.Inactivo
        }
    }

    fun limpiarEstado() {
        _estadoUI.value = EstadoAuth.Inactivo
    }

    // ─── Traducción de errores Firebase → español ─────────────────────────
    private fun traducirError(msg: String): String = when {
        msg.contains("no user record", ignoreCase = true)
                || msg.contains("user-not-found", ignoreCase = true)     -> "No existe cuenta con ese correo"
        msg.contains("password is invalid", ignoreCase = true)
                || msg.contains("wrong-password", ignoreCase = true)     -> "Contraseña incorrecta"
        msg.contains("email address is already", ignoreCase = true)
                || msg.contains("email-already-in-use", ignoreCase = true) -> "Ya existe una cuenta con ese correo"
        msg.contains("badly formatted", ignoreCase = true)
                || msg.contains("invalid-email", ignoreCase = true)      -> "Formato de correo inválido"
        msg.contains("network", ignoreCase = true)               -> "Sin conexión a internet"
        msg.contains("too-many-requests", ignoreCase = true)     -> "Demasiados intentos. Intenta más tarde"
        msg.contains("weak-password", ignoreCase = true)         -> "La contraseña es muy débil"
        else -> "Error inesperado. Intenta de nuevo"
    }
}



sealed class EstadoAuth {
    object Inactivo      : EstadoAuth()
    object Cargando      : EstadoAuth()
    object CorreoEnviado : EstadoAuth()
    data class Exito(val usuario: User)   : EstadoAuth()
    data class Error(val mensaje: String) : EstadoAuth()
}