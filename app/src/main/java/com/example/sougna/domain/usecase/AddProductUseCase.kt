package com.example.sougna.domain.usecase
import com.example.sougna.data.model.Product
import com.example.sougna.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val firebaseAuth: FirebaseAuth // Inject FirebaseAuth
) {
    suspend operator fun invoke(product: Product) {
        val currentUser = firebaseAuth.currentUser
        val productWithUserId = if (currentUser != null) {
            product.copy(userId = currentUser.uid)
        } else {
            // Handle case where user is not logged in (e.g., throw exception or return error)
            // For now, we'll add with an empty user ID, but this should be handled properly
            product.copy(userId = "")
        }
        return productRepository.addProduct(productWithUserId)
    }
}
