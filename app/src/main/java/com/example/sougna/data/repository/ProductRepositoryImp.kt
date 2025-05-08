package com.example.sougna.data.repository

import com.example.sougna.data.local.ProductDao
import com.example.sougna.data.local.toEntity
import com.example.sougna.data.local.toProduct
import com.example.sougna.data.model.Product
import com.example.sougna.data.remote.datasource.ProductRemoteDataSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImp @Inject constructor(
    private val productRemoteDataSource: ProductRemoteDataSource,
    override val productDao: ProductDao,
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> = flow {
        // Emit data from local cache first
        emit(productDao.getAllProducts().map { list -> list.map { it.toProduct() } }.await())

        // Fetch data from Firestore and update local cache
        productRemoteDataSource.getAllProducts().collect { remoteProducts ->
            productDao.deleteAllProducts() // Clear local cache (consider a more sophisticated sync)
            productDao.insertAllProducts(remoteProducts.map { it.toEntity() })
            emit(remoteProducts) // Emit updated data from remote
        }
    }.catch { e ->
        // Handle errors, e.g., network issues
        // You might want to log the error or emit a specific error state
        e.printStackTrace()
        // Optionally, re-throw the exception or emit an empty list/error state
        // throw e
        emit(emptyList()) // Emit empty list on error
    }


    override suspend fun addProduct(product: Product) {
        productRemoteDataSource.addProduct(product)
        productDao.insertProduct(product.toEntity()) // Update local cache
    }

    override suspend fun getProductById(productId: String): Product? {
        // Try fetching from local cache first
        val localProduct = productDao.getProductById(productId)?.toProduct()
        if (localProduct != null) {
            return localProduct
        }

        // If not in local cache, fetch from remote
        val remoteProduct = productRemoteDataSource.getProductById(productId)

        // No permission check here, allow viewing any product details
        if (remoteProduct != null) {
            productDao.insertProduct(remoteProduct.toEntity()) // Update local cache
            return remoteProduct
        }

        return null // Product not found
    }

    override suspend fun updateProduct(product: Product) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || currentUser.uid != product.userId) {
            throw SecurityException("User not authorized to update this product")
        }
        productRemoteDataSource.updateProduct(product)
        productDao.updateProduct(product.toEntity()) // Update local cache
    }

    override suspend fun deleteProduct(productId: String) {
        val currentUser = firebaseAuth.currentUser
        val productToDelete = getProductById(productId) // Fetch product to check userId

        if (currentUser == null || productToDelete == null || currentUser.uid != productToDelete.userId) {
            throw SecurityException("User not authorized to delete this product")
        }
        productRemoteDataSource.deleteProduct(productId)
        productDao.deleteProductById(productId) // Update local cache
    }

    override fun searchProducts(
        query: String?,
        categoryId: String?,
        sortBy: String?,
        sortOrder: String?
    ): Flow<List<Product>> = flow {
        // Fetch data from Firestore based on search criteria
        val searchResults = productRemoteDataSource.searchProducts(
            query = query,
            categoryId = categoryId,
            sortBy = sortBy,
            sortOrder = sortOrder
        )
        emit(searchResults)
    }.catch { e ->
        // Handle errors during search
        e.printStackTrace()
        emit(emptyList()) // Emit empty list on error
    }

    override fun getProductsByUserId(userId: String): Flow<List<Product>> = flow {
        // Fetch data from Firestore filtered by user ID
        val userProducts = productRemoteDataSource.getProductsByUserId(userId)
        emit(userProducts)
    }.catch { e ->
        // Handle errors
        e.printStackTrace()
        emit(emptyList()) // Emit empty list on error
    }
}

private suspend fun <T> Flow<List<T>>.await(): List<T> {
    var result: List<T> = emptyList()
    collect {
        result = it
    }
    return result
}
