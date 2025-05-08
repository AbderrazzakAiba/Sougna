package com.example.sougna.domain.usecase

import com.example.sougna.data.local.ProductDao
import com.example.sougna.data.local.toEntity
import com.example.sougna.data.local.toProduct
import com.example.sougna.data.model.Product
import com.example.sougna.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    override val productDao: ProductDao
) : ProductRepository {

    override suspend fun addProduct(product: Product) {
        productDao.insertProduct(product.toEntity())
    }

    override suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toProduct()
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(productId: String) {
        productDao.deleteProductById(productId)
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { list ->
            list.map { it.toProduct() }
        }
    }
}
