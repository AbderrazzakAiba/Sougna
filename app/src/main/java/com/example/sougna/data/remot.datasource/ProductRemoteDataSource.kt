package com.example.sougna.data.remote.datasource

import com.example.sougna.data.model.Product

interface ProductRemoteDataSource {
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: String)
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductById(productId: String): Product?
    suspend fun getProductsByUserId(userId: String): List<Product> // Add function to get products by user ID

    suspend fun searchProducts(
        query: String? = null,
        categoryId: String? = null,
        sortBy: String? = null, // e.g., "price", "rating", "dateAdded"
        sortOrder: String? = null // e.g., "asc", "desc"
    ): List<Product>
}
