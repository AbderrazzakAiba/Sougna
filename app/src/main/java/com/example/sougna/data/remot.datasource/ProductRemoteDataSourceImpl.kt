package com.example.sougna.data.remote.datasource

import com.example.sougna.data.model.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth
) : ProductRemoteDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val productCollection = firestore.collection("products")

    override suspend fun addProduct(product: Product) {
        productCollection.document(product.id).set(product).await()
    }

    override suspend fun updateProduct(product: Product) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.uid != product.userId) {
            throw SecurityException("User not authorized to update this product")
        }
        productCollection.document(product.id).set(product).await()
    }

    override suspend fun deleteProduct(productId: String) {
        val currentUser = firebaseAuth.currentUser
        val productToDelete = getProductById(productId) // Fetch product to check userId

        if (currentUser == null || productToDelete == null || currentUser.uid != productToDelete.userId) {
            throw SecurityException("User not authorized to delete this product")
        }
        productCollection.document(productId).delete().await()
    }

    override suspend fun getAllProducts(): List<Product> {
        val snapshot = productCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }

    override suspend fun getProductById(productId: String): Product? {
        val snapshot = productCollection.document(productId).get().await()
        return snapshot.toObject(Product::class.java)
    }

    override suspend fun getProductsByUserId(userId: String): List<Product> {
        val snapshot = productCollection.whereEqualTo("userId", userId).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }

    override suspend fun searchProducts(
        query: String?,
        categoryId: String?,
        sortBy: String? = null, // e.g., "price", "rating", "dateAdded"
        sortOrder: String? = null // e.g., "asc", "desc"
    ): List<Product> {
        var firestoreQuery = productCollection.orderBy("name") // Default order

        if (!query.isNullOrBlank()) {
            // Basic search by name - Firestore doesn't support full-text search directly
            // This will only work for exact matches or prefix matches depending on data structure
            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("name", query)
                .whereLessThanOrEqualTo("name", query + '\uf8ff')
        }

        if (!categoryId.isNullOrBlank()) {
            firestoreQuery = firestoreQuery.whereEqualTo("categoryId", categoryId)
        }

        if (!sortBy.isNullOrBlank()) {
            val direction = when (sortOrder) {
                "desc" -> com.google.firebase.firestore.Query.Direction.DESCENDING
                else -> com.google.firebase.firestore.Query.Direction.ASCENDING
            }
            firestoreQuery = firestoreQuery.orderBy(sortBy, direction)
        }

        val snapshot = firestoreQuery.get().await()
        return snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
    }
}
