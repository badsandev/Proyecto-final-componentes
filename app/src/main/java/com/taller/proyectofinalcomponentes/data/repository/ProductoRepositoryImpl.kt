package com.taller.proyectofinalcomponentes.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.taller.proyectofinalcomponentes.dominio.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProductoRepositoryImpl : ProductoRepository {

    private val db = FirebaseFirestore.getInstance()
    private val coleccion = db.collection("productos")

    override fun obtenerProductos(): Flow<List<Product>> = callbackFlow {
        val listener = coleccion
            .whereEqualTo("activo", true)
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val productos = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(productos)
            }
        awaitClose { listener.remove() }
    }

    override fun obtenerProductosPorCategoria(categoria: String): Flow<List<Product>> = callbackFlow {
        val query = if (categoria == "Todos") {
            coleccion.whereEqualTo("activo", true)
        } else {
            coleccion.whereEqualTo("activo", true).whereEqualTo("category", categoria)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val productos = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Product::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(productos)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun obtenerProductoPorId(id: String): Result<Product> {
        return try {
            val doc = coleccion.document(id).get().await()
            val producto = doc.toObject(Product::class.java)?.copy(id = doc.id)
                ?: throw Exception("Producto no encontrado")
            Result.success(producto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun agregarProducto(producto: Product): Result<String> {
        return try {
            val docRef = coleccion.document()

            val datos = hashMapOf(
                "id"            to docRef.id,
                "name"          to producto.name,
                "category"      to producto.category,
                "price"         to producto.price,
                "oldPrice"      to producto.oldPrice,
                "description"   to producto.description,
                "stock"         to producto.stock,
                "activo"        to true,
                "rating"        to producto.rating,
                "imageUrl"      to producto.imageUrl,
                "fechaCreacion" to System.currentTimeMillis()
            )

            // 3. Guardar con el ID correcto
            docRef.set(datos).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarProducto(producto: Product): Result<Unit> {
        return try {
            val datos = hashMapOf(
                "id"            to producto.id,
                "name"          to producto.name,
                "category"      to producto.category,
                "price"         to producto.price,
                "oldPrice"      to producto.oldPrice,
                "description"   to producto.description,
                "stock"         to producto.stock,
                "activo"        to producto.activo,
                "rating"        to producto.rating,
                "imageUrl"      to producto.imageUrl,
                "fechaCreacion" to producto.fechaCreacion
            )
            coleccion.document(producto.id).set(datos).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarProducto(id: String): Result<Unit> {
        return try {
            coleccion.document(id).update("activo", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Cambio a operación atómica para evitar errores de concurrencia
    override suspend fun actualizarStock(id: String, cantidadCambio: Int): Result<Unit> {
        return try {
            coleccion.document(id)
                .update("stock", FieldValue.increment(cantidadCambio.toLong()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
