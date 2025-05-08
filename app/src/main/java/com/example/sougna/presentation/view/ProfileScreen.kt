package com.example.sougna.presentation.view

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sougna.presentation.viewmodel.AuthViewModel
import com.example.sougna.domain.usecase.GetProductsByUserIdUseCase // Import UseCase
import com.example.sougna.data.model.Product // Import Product
import androidx.navigation.NavController // Import NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    getProductsByUserIdUseCase: GetProductsByUserIdUseCase = hiltViewModel(), // Inject UseCase
    navController: NavController, // Receive NavController
    onLogoutSuccess: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // State for changing email and password
    var newEmail by remember { mutableStateOf("") }
    var currentPasswordForEmailChange by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var currentPasswordForPasswordChange by remember { mutableStateOf("") }

    // State for user's products
    var userProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoadingUserProducts by remember { mutableStateOf(false) }
    var userProductsError by remember { mutableStateOf<String?>(null) }

    // Fetch user's products when currentUser changes
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            isLoadingUserProducts = true
            userProductsError = null
            try {
                getProductsByUserIdUseCase(user.uid).collect { products ->
                    userProducts = products
                    isLoadingUserProducts = false
                }
            } catch (e: Exception) {
                userProductsError = "Failed to load your products: ${e.message}"
                isLoadingUserProducts = false
            }
        } ?: run {
            userProducts = emptyList()
            isLoadingUserProducts = false
            userProductsError = null
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("User Information", style = MaterialTheme.typography.headlineSmall)

            currentUser?.let { user ->
                Text("Email: ${user.email ?: "N/A"}")
                // Display other user information if available (e.g., display name)
            } ?: Text("Not logged in")

            Divider()

            Text("Change Email", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("New Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currentPasswordForEmailChange,
                onValueChange = { currentPasswordForEmailChange = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    authViewModel.changeEmail(
                        newEmail,
                        currentPasswordForEmailChange,
                        onSuccess = { Toast.makeText(context, "Email changed successfully", Toast.LENGTH_SHORT).show() },
                        onError = { errorMessage -> Toast.makeText(context, "Failed to change email: $errorMessage", Toast.LENGTH_LONG).show() }
                    )
                },
                enabled = !isLoading && newEmail.isNotBlank() && currentPasswordForEmailChange.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Email")
            }

            Divider()

            Text("Change Password", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currentPasswordForPasswordChange,
                onValueChange = { currentPasswordForPasswordChange = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    authViewModel.changePassword(
                        newPassword,
                        currentPasswordForPasswordChange,
                        onSuccess = { Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show() },
                        onError = { errorMessage -> Toast.makeText(context, "Failed to change password: $errorMessage", Toast.LENGTH_LONG).show() }
                    )
                },
                enabled = !isLoading && newPassword.isNotBlank() && currentPasswordForPasswordChange.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Password")
            }

            Divider()

            Text("My Products", style = MaterialTheme.typography.titleMedium)

            when {
                isLoadingUserProducts -> {
                    CircularProgressIndicator()
                }
                userProductsError != null -> {
                    Text("Error loading products: $userProductsError")
                }
                userProducts.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f), // Take remaining space
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(userProducts) { product ->
                            ProductListItem(product = product, onProductClick = { productId ->
                                navController.navigate("productDetail/$productId") // Navigate to product detail
                            })
                        }
                    }
                }
                currentUser != null -> {
                    Text("You haven't added any products yet.")
                }
                else -> {
                    Text("Log in to see your products.")
                }
            }


            Spacer(modifier = Modifier.height(16.dp)) // Add some space before logout button

            Button(
                onClick = {
                    authViewModel.logout(
                        onSuccess = onLogoutSuccess,
                        onError = { errorMessage -> Toast.makeText(context, "Failed to logout: $errorMessage", Toast.LENGTH_LONG).show() }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
