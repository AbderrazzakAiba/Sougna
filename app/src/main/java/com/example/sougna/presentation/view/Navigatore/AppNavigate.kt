package com.example.sougna.presentation.view.Navigatore

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.sougna.presentation.view.pages.HomePage
import com.example.sougna.presentation.view.IntroScreen
import com.example.sougna.presentation.view.pages.AddProductScreen
import com.example.sougna.presentation.view.pages.ProductListScreen
import com.example.sougna.presentation.view.pages.ProfilePage
import com.example.sougna.presentation.view.AuthScreen
import com.example.sougna.presentation.view.ProductDetailScreen
import com.example.sougna.presentation.view.EditProductScreen
import com.example.sougna.presentation.view.MainScreen // Import MainScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigator() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "intro") {
        composable("intro") {
            IntroScreen(navController)
        }

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("home") { HomePage(navController) }
        composable("profile") {
            ProfilePage(
                navController = navController, // Pass navController
                onLogoutSuccess = {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true } // Pop up to home and include it to clear back stack
                    }
                }
            )
        }
        composable("addProduct") { AddProductScreen(navController) }
        composable("productList") {
            MainScreen( // Use MainScreen here
                onProductClick = { productId ->
                    navController.navigate("productDetail/$productId")
                }
            )
        }

        composable(
            route = "productDetail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            if (productId != null) {
                ProductDetailScreen(
                    productId = productId,
                    onEditProduct = { id -> navController.navigate("editProduct/$id") },
                    onProductDeleted = { navController.popBackStack() }
                )
            }
        }

        composable(
            route = "editProduct/{productId}?productId={productId}",
            arguments = listOf(navArgument("productId") {
                type = NavType.StringType
                nullable = true
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            EditProductScreen(
                productId = productId,
                onProductSaved = { navController.popBackStack() }
            )
        }
    }
}
