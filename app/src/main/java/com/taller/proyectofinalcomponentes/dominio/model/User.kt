package com.taller.proyectofinalcomponentes.dominio.model

data class User(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val rol: Rol = Rol.USUARIO
)