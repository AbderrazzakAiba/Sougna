package com.example.sougna.presentation.viewmodel

import android.net.Uri
package com.example.sougna.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sougna.data.model.Product
import com.example.sougna.domain.usecase.AddProductUseCase
import com.example.sougna.domain.usecase.GetAllProductsUseCase
import com.example.sougna.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getAllProductsUseCase: GetAllProductsUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val searchProductsUseCase: SearchProductsUseCase // Inject search use case
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    // Combine search parameters and trigger search
    private val searchParameters = combine(
        _uiState.map { it.searchQuery },
        _uiState.map { it.selectedCategory },
        _uiState.map { it.sortBy },
        _uiState.map { it.sortOrder }
    ) { query, category, sortBy, sortOrder ->
        Triple(query, category, sortBy, sortOrder)
    }.debounce(300) // Debounce search queries

    init {
        // Observe search parameters and trigger search
        searchParameters
            .flatMapLatest { (query, category, sortBy, sortOrder) ->
                _uiState.value = _uiState.value.copy(isLoading = true)
                if (query.isBlank() && category == null && sortBy == null) {
                    // If no search parameters, fetch all products
                    getAllProductsUseCase()
                } else {
                    // Otherwise, perform search
                    searchProductsUseCase(query, category, sortBy, sortOrder)
                }
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
            .onEach { products ->
                _uiState.value = _uiState.value.copy(products = products, isLoading = false)
            }
            .launchIn(viewModelScope)

        // Initial load of all products
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getAllProductsUseCase().catch {
                _uiState.value = _uiState.value.copy(errorMessage = it.message, isLoading = false)
            }.collect { products ->
                _uiState.value = _uiState.value.copy(products = products, isLoading = false)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onCategoryFilterChange(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onSortByChange(sortBy: String?) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
    }

    fun onSortOrderChange(sortOrder: String?) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
    }


    fun addProduct(name: String, description: String, price: String, imageUri: Uri?, onSuccess: () -> Unit) {
        val priceValue = price.toDoubleOrNull() ?: return

        val newProduct = Product(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            price = priceValue,
            thumbnailUrl = imageUri?.toString() ?: "",
            rating = 4.5 // TODO: Get actual rating from UI
        )

        viewModelScope.launch {
            try {
                addProductUseCase(newProduct)
                // After adding, clear search and fetch all products or navigate
                _uiState.value = _uiState.value.copy(searchQuery = "", selectedCategory = null, sortBy = null, sortOrder = null)
                fetchProducts()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}
