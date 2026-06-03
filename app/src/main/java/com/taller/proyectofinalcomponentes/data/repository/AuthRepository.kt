package com.taller.proyectofinalcomponentes.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.taller.proyectofinalcomponentes.dominio.model.User

interface AuthRepository {

    suspend fun loginConEmail(email: String, password: String): Result<User>
    suspend fun registrarConEmail(email: String, password: String, nombre: String): Result<User>
    suspend fun loginConGoogle(cuenta: GoogleSignInAccount): Result<User>
    suspend fun cerrarSesion()
    suspend fun recuperarPassword(email: String): Result<Unit>
    fun obtenerUsuarioActual(): User?
    fun estaAutenticado(): Boolean

}