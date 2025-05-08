package com.example.sougna.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sougna.domain.usecase.AddProductUseCase
import com.example.sougna.domain.usecase.GetProductByIdUseCase
import com.example.sougna.domain.usecase.UpdateProductUseCase
import com.example.sougna.data.model.Product
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val addProductUseCase: AddProductUseCase,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val firebaseAuth: FirebaseAuth, // Inject FirebaseAuth
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    private val productId: String? = savedStateHandle["productId"]

    // State for the edit form
    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName

    private val _productDescription = MutableStateFlow("")
    val productDescription: StateFlow<String> = _productDescription

    private val _productPrice = MutableStateFlow("")
    val productPrice: StateFlow<String> = _productPrice

    private val _productRating = MutableStateFlow("")
    val productRating: StateFlow<String> = _productRating

    private val _productCategoryId = MutableStateFlow("")
    val productCategoryId: StateFlow<String> = _productCategoryId

    private val _productThumbnailUrl = MutableStateFlow("")
    val productThumbnailUrl: StateFlow<String> = _productThumbnailUrl

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _isAuthorizedToEdit = MutableStateFlow(false)
    val isAuthorizedToEdit: StateFlow<Boolean> = _isAuthorizedToEdit


    init {
        productId?.let {
            fetchProductDetails(it)
        } ?: run {
            // If adding a new product, user is authorized
            _isAuthorizedToEdit.value = firebaseAuth.currentUser != null
        }
    }

    private fun fetchProductDetails(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val product = getProductByIdUseCase(productId)
                _product.value = product
                product?.let {
                    _productName.value = it.name
                    _productDescription.value = it.description
                    _productPrice.value = it.price.toString()
                    _productRating.value = it.rating.toString()
                    _productCategoryId.value = it.categoryId
                    _productThumbnailUrl.value = it.thumbnailUrl ?: ""
                    _isAuthorizedToEdit.value = firebaseAuth.currentUser?.uid == it.userId
                } ?: run {
                    _error.value = "Product not found."
                    _isAuthorizedToEdit.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load product details: ${e.message}"
                _isAuthorizedToEdit.value = false
            } finally {
                _loading.value = false
            }
        }
    }

    fun onProductNameChange(name: String) {
        _productName.value = name
    }

    fun onProductDescriptionChange(description: String) {
        _productDescription.value = description
    }

    fun onProductPriceChange(price: String) {
        _productPrice.value = price
    }

    fun onProductRatingChange(rating: String) {
        _productRating.value = rating
    }

    fun onProductCategoryIdChange(categoryId: String) {
        _productCategoryId.value = categoryId
    }

    fun onProductThumbnailUrlChange(thumbnailUrl: String) {
        _productThumbnailUrl.value = thumbnailUrl
    }


    fun saveProduct() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _saveSuccess.value = false

            if (productId != null && !_isAuthorizedToEdit.value) {
                _error.value = "You are not authorized to edit this product."
                _loading.value = false
                return@launch
            }

            // TODO: Validate input
            val productToSave = Product(
                id = productId ?: "", // Generate new ID if adding
                name = _productName.value,
                description = _productDescription.value,
                price = _productPrice.value.toDoubleOrNull() ?: 0.0,
                userId = firebaseAuth.currentUser?.uid ?: "", // Assign current user ID
                rating = _productRating.value.toDoubleOrNull() ?: 0.0,
                categoryId = _productCategoryId.value,
                thumbnailUrl = _productThumbnailUrl.value
            )

            try {
                if (productId == null) {
                    // Add new product
                    addProductUseCase(productToSave)
                } else {
                    // Update existing product
                    updateProductUseCase(productToSave)
                }
                _saveSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to save product: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
