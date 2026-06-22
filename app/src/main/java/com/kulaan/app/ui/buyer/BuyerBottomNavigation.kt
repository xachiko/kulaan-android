package com.kulaan.app.ui.buyer

import android.content.Intent
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import com.kulaan.app.MainActivity
import com.kulaan.app.utils.SessionManager

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Beranda")
    object Orders : BottomNavItem("orders", Icons.Default.List, "Pesanan")
    object Notifications : BottomNavItem("notifications", Icons.Default.Notifications, "Notifikasi")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
}

@Composable
fun BuyerBottomNavigation(
    sessionManager: SessionManager
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Orders,
                    BottomNavItem.Notifications,
                    BottomNavItem.Profile
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onNavigateToStores = { navController.navigate("stores") },
                    onNavigateToPopularProducts = { navController.navigate("popular_products") },
                    onProductClick = { productId -> navController.navigate("product_detail/$productId") }
                )
            }
            composable(BottomNavItem.Orders.route) {
                MyOrdersScreen(sessionManager = sessionManager)
            }
            composable(BottomNavItem.Notifications.route) {
                NotificationScreen(sessionManager = sessionManager)
            }
            composable(BottomNavItem.Profile.route) {
                UserProfileScreen(sessionManager = sessionManager)
            }
            composable(
                route = "product_detail/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                ProductDetailScreen(
                    productId = productId,
                    onBackClick = { navController.popBackStack() },
                    onStoreClick = { storeId -> navController.navigate("store_detail/$storeId") },
                    onOrderClick = { prodId, qty -> navController.navigate("checkout/$prodId/$qty") },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = "checkout/{productId}/{quantity}",
                arguments = listOf(
                    navArgument("productId") { type = NavType.IntType },
                    navArgument("quantity") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: 0
                val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
                CheckoutScreen(
                    productId = productId,
                    initialQuantity = quantity,
                    onBackClick = { navController.popBackStack() },
                    onGoToProfile = {
                        navController.navigate(BottomNavItem.Profile.route) {
                            popUpTo(BottomNavItem.Home.route)
                        }
                    },
                    onOrderSuccess = {
                        navController.navigate(BottomNavItem.Orders.route) {
                            popUpTo(BottomNavItem.Home.route)
                        }
                    },
                    sessionManager = sessionManager
                )
            }
            composable("stores") {
                StoreListScreen(
                    onBackClick = { navController.popBackStack() },
                    onStoreClick = { storeId -> navController.navigate("store_detail/$storeId") },
                    sessionManager = sessionManager
                )
            }
            composable(
                route = "store_detail/{storeId}",
                arguments = listOf(navArgument("storeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val storeId = backStackEntry.arguments?.getInt("storeId") ?: 0
                StoreDetailScreen(
                    storeId = storeId,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId -> navController.navigate("product_detail/$productId") },
                    sessionManager = sessionManager
                )
            }
            composable("popular_products") {
                PopularProductsScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId -> navController.navigate("product_detail/$productId") },
                    sessionManager = sessionManager
                )
            }
        }
    }
}
