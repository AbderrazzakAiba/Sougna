package com.example.sougna.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sougna.presentation.viewmodel.ProductDetailViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import com.example.sougna.presentation.viewmodel.AuthViewModel // Import AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(), // Inject AuthViewModel
    productId: String,
    onEditProduct: (String) -> Unit,
    onProductDeleted: () -> Unit // Callback to navigate back after deletion
) {
    val product by viewModel.product.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val deletionSuccess by viewModel.deletionSuccess.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState() // Observe current user

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Navigate back when deletion is successful
    LaunchedEffect(deletionSuccess) {
        if (deletionSuccess) {
            onProductDeleted()
        }
    }

    val isProductOwner = currentUser != null && product?.userId == currentUser.uid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                actions = {
                    if (product != null && !loading && isProductOwner) { // Conditionally show icons
                        IconButton(onClick = { onEditProduct(productId) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Product")
                        }
                        IconButton(onClick = { showDeleteConfirmationDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete Product")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                loading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text("Error: $error")
                }
                product != null -> {
                    // Display product details
                    product?.let {
                        Text("Name: ${it.name}")
                        Text("Description: ${it.description}")
                        Text("Price: ${it.price}")
                        Text("Category: ${it.category}")
                        Text("Rating: ${it.rating}")
                        // Add more product details as needed
                    }
                }
                else -> {
                    Text("Product not found.")
                }
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this product?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProduct(productId)
                    showDeleteConfirmationDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
