package com.example.sougna.domain.usecase

import com.example.sougna.data.model.Product
import com.example.sougna.data.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(
        query: String? = null,
        categoryId: String? = null,
        sortBy: String? = null,
        sortOrder: String? = null
    ): Flow<List<Product>> {
        return productRepository.searchProducts(query, categoryId, sortBy, sortOrder)
    }
}
