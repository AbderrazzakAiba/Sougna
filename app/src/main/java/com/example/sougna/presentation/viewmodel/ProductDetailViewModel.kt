package com.example.sougna.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sougna.domain.usecase.DeleteProductUseCase
import com.example.sougna.domain.usecase.GetProductByIdUseCase
import com.example.sougna.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _deletionSuccess = MutableStateFlow(false)
    val deletionSuccess: StateFlow<Boolean> = _deletionSuccess

    private val productId: String? = savedStateHandle["productId"]

    init {
        productId?.let {
            fetchProductDetails(it)
        }
    }

    private fun fetchProductDetails(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _product.value = getProductByIdUseCase(productId)
            } catch (e: Exception) {
                _error.value = "Failed to load product details: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                deleteProductUseCase(productId)
                _deletionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Failed to delete product: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }
}
