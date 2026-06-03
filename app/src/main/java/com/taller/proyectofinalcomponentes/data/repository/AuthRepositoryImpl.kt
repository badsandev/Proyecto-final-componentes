package com.taller.proyectofinalcomponentes.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.taller.proyectofinalcomponentes.dominio.model.Rol
import com.taller.proyectofinalcomponentes.dominio.model.User
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ─── Email / Password ────────────────────────────────────────────────

    override suspend fun loginConEmail(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = obtenerUsuarioActualCompleto()
                ?: throw Exception("No se pudo obtener el perfil")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registrarConEmail(
        email: String,
        password: String,
        nombre: String
    ): Result<User> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser!!.uid
            val nuevoUsuario = User(
                uid = uid,
                nombre = nombre,
                email = email,
                rol = Rol.USUARIO
            )
            firestore.collection("usuarios")
                .document(uid)
                .set(nuevoUsuario)
                .await()
            Result.success(nuevoUsuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Google Sign-In ──────────────────────────────────────────────────

    override suspend fun loginConGoogle(cuenta: GoogleSignInAccount): Result<User> {
        return try {
            val idToken = cuenta.idToken ?: throw Exception("Google ID Token nulo")
            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credencial).await()

            val uid = auth.currentUser!!.uid
            val docRef = firestore.collection("usuarios").document(uid)
            val doc = docRef.get().await()

            val user = if (!doc.exists()) {
                val nuevoUsuario = User(
                    uid = uid,
                    nombre = cuenta.displayName ?: "Usuario Google",
                    email = cuenta.email ?: "",
                    rol = Rol.USUARIO
                )
                docRef.set(nuevoUsuario).await()
                nuevoUsuario
            } else {
                doc.toObject(User::class.java)
                    ?: throw Exception("Error al convertir perfil")
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Utilidades ──────────────────────────────────────────────────────

    override suspend fun recuperarPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cerrarSesion() {
        auth.signOut()
    }

    override fun estaAutenticado(): Boolean = auth.currentUser != null

    // Síncrono — solo datos básicos de FirebaseAuth (para chequeo rápido)
    override fun obtenerUsuarioActual(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            nombre = firebaseUser.displayName ?: "Usuario",
            email = firebaseUser.email ?: "",
            rol = Rol.USUARIO
        )
    }

    // Suspendido — lee Firestore para obtener el rol real (ADMIN o USUARIO)
    suspend fun obtenerUsuarioActualCompleto(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("usuarios").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}