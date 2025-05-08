package com.example.sougna.presentation.view

package com.example.sougna.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sougna.data.model.Product
import com.example.sougna.presentation.viewmodel.CategoryViewModel
import com.example.sougna.presentation.viewmodel.ProductViewModel
import com.example.sougna.presentation.viewmodel.UIState
import com.example.sougna.data.model.Category // Import Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    productViewModel: ProductViewModel = hiltViewModel(),
    categoryViewModel: CategoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit // Navigation lambda
) {
    val uiState by productViewModel.uiState.collectAsState()
    val categories by categoryViewModel.categories.collectAsState() // Assuming CategoryViewModel exposes categories

    var showCategoryFilterDropdown by remember { mutableStateOf(false) }
    var showSortByDropdown by remember { mutableStateOf(false) }
    var showSortOrderDropdown by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Sougna") })
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Search Input
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { productViewModel.onSearchQueryChange(it) },
                label = { Text("Search Products") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Filter
                Box {
                    Button(onClick = { showCategoryFilterDropdown = true }) {
                        Text("Category: ${categories.find { it.id == uiState.selectedCategory }?.name ?: "All"}")
                    }
                    DropdownMenu(
                        expanded = showCategoryFilterDropdown,
                        onDismissRequest = { showCategoryFilterDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                productViewModel.onCategoryFilterChange(null)
                                showCategoryFilterDropdown = false
                            }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    productViewModel.onCategoryFilterChange(category.id)
                                    showCategoryFilterDropdown = false
                                }
                            )
                        }
                    }
                }

                // Sorting Options
                Box {
                    Button(onClick = { showSortByDropdown = true }) {
                        Text("Sort By: ${uiState.sortBy ?: "None"}")
                    }
                    DropdownMenu(
                        expanded = showSortByDropdown,
                        onDismissRequest = { showSortByDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                productViewModel.onSortByChange(null)
                                showSortByDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Price") },
                            onClick = {
                                productViewModel.onSortByChange("price")
                                showSortByDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rating") },
                            onClick = {
                                productViewModel.onSortByChange("rating")
                                showSortByDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Date Added") },
                            onClick = {
                                productViewModel.onSortByChange("createdAt")
                                showSortByDropdown = false
                            }
                        )
                    }
                }

                // Sort Order
                if (uiState.sortBy != null) {
                    Box {
                        Button(onClick = { showSortOrderDropdown = true }) {
                            Text("Order: ${uiState.sortOrder ?: "asc"}")
                        }
                        DropdownMenu(
                            expanded = showSortOrderDropdown,
                            onDismissRequest = { showSortOrderDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Ascending") },
                                onClick = {
                                    productViewModel.onSortOrderChange("asc")
                                    showSortOrderDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Descending") },
                                onClick = {
                                    productViewModel.onSortOrderChange("desc")
                                    showSortOrderDropdown = false
                                }
                            )
                        }
                    }
                }
            }


            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (uiState.errorMessage != null) {
                Text("Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductListItem(product = product, onProductClick = onProductClick)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListItem(product: Product, onProductClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onProductClick(product.id) } // Add clickable modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleMedium)
            Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "$${product.price}", style = MaterialTheme.typography.bodySmall)
            // Display other product details like rating, category, thumbnail
        }
    }
}
