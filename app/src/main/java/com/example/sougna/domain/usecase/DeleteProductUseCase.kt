package com.example.sougna.domain.usecase

import com.example.sougna.data.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: String) {
        productRepository.deleteProduct(productId)
    }
}
