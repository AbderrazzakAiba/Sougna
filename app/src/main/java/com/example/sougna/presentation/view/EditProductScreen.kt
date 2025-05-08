package com.example.sougna.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sougna.presentation.viewmodel.EditProductViewModel
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    viewModel: EditProductViewModel = hiltViewModel(),
    productId: String?, // Product ID can be null for adding a new product
    onProductSaved: () -> Unit // Callback to navigate back after saving
) {
    val productName by viewModel.productName.collectAsState()
    val productDescription by viewModel.productDescription.collectAsState()
    val productPrice by viewModel.productPrice.collectAsState()
    val productRating by viewModel.productRating.collectAsState()
    val productCategoryId by viewModel.productCategoryId.collectAsState()
    val productThumbnailUrl by viewModel.productThumbnailUrl.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val isAuthorizedToEdit by viewModel.isAuthorizedToEdit.collectAsState() // Observe authorization state

    // Navigate back when save is successful
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onProductSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (productId == null) "Add Product" else "Edit Product") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else if (error != null) {
                Text("Error: $error")
            } else if (productId != null && !isAuthorizedToEdit) {
                Text("You are not authorized to edit this product.")
            }
            else {
                OutlinedTextField(
                    value = productName,
                    onValueChange = viewModel::onProductNameChange,
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAuthorizedToEdit || productId == null // Enable if authorized or adding new product
                )
                OutlinedTextField(
                    value = productDescription,
                    onValueChange = viewModel::onProductDescriptionChange,
                    label = { Text("Product Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAuthorizedToEdit || productId == null
                )
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = viewModel::onProductPriceChange,
                    label = { Text("Product Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAuthorizedToEdit || productId == null
                )
                OutlinedTextField(
                    value = productRating,
                    onValueChange = viewModel::onProductRatingChange,
                    label = { Text("Product Rating") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAuthorizedToEdit || productId == null
                )
                OutlinedTextField(
                    value = productCategoryId,
                    onValueChange = viewModel::onProductCategoryIdChange,
                    label = { Text("Category ID") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAuthorizedToEdit || productId == null
                )
                OutlinedTextField(
                    value = productThumbnailUrl,
                    onValueChange = viewModel::onProductThumbnailUrlChange,
                    label = { Text("Thumbnail URL") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isAuthorizedToEdit || productId == null
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = viewModel::saveProduct,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = (isAuthorizedToEdit || productId == null) && !loading // Enable if authorized/adding and not loading
                ) {
                    Text(if (productId == null) "Add Product" else "Save Changes")
                }
            }
        }
    }
}
