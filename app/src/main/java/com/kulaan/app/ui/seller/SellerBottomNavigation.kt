package com.kulaan.app.ui.seller

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kulaan.app.MainActivity
import com.kulaan.app.data.local.UserPreferences
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.ui.seller.viewmodel.SellerViewModel
import com.kulaan.app.ui.seller.viewmodel.SellerViewModelFactory
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState
import kotlinx.coroutines.launch

sealed class SellerNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Dashboard : SellerNavItem("dashboard", Icons.Default.Dashboard, "Toko")
    object Products : SellerNavItem("products", Icons.Default.Inventory, "Produk")
    object Settings : SellerNavItem("settings", Icons.Default.Settings, "Pengaturan")
}

@Composable
fun SellerBottomNavigation(
    sessionManager: SessionManager
) {
    val navController = rememberNavController()
    val repository = remember { StoreRepository(sessionManager) }
    val viewModel: SellerViewModel = viewModel(
        factory = SellerViewModelFactory(repository, sessionManager)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    SellerNavItem.Dashboard,
                    SellerNavItem.Products,
                    SellerNavItem.Settings
                )

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontSize = 11.sp) },
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
            startDestination = SellerNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(SellerNavItem.Dashboard.route) {
                SellerDashboardScreen(
                    storeName = (uiState.storeProfile as? UiState.Success)?.data?.storeName,
                    storeStatus = (uiState.storeProfile as? UiState.Success)?.data?.status,
                    onKelolaProfil = {
                        navController.navigate("profile")
                    }
                )
            }
            composable(SellerNavItem.Products.route) {
                ManageProductsScreen(
                    productsState = uiState.products,
                    onAddProduct = {
                        navController.navigate("add_product")
                    }
                )
            }
            composable(SellerNavItem.Settings.route) {
                SellerSettingsScreen(
                    sessionManager = sessionManager,
                    onProfile = { navController.navigate("profile") }
                )
            }
            composable("profile") {
                StoreProfileScreen(
                    storeState = uiState.storeProfile,
                    onEditProfile = { navController.popBackStack() },
                    onAddProduct = {
                        navController.navigate("add_product") {
                            popUpTo(SellerNavItem.Products.route) { inclusive = true }
                        }
                    }
                )
            }
            composable("add_product") {
                AddProductScreen(
                    sessionManager = sessionManager,
                    onBack = { navController.popBackStack() },
                    onSuccess = {
                        navController.popBackStack()
                        viewModel.loadProducts()
                    }
                )
            }
        }
    }
}

@Composable
fun SellerSettingsScreen(
    sessionManager: SessionManager,
    onProfile: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferences(context) }

    Column(modifier = Modifier.fillMaxSize()) {
        ListItem(
            headlineContent = { Text("Profil Toko", fontWeight = FontWeight.Medium) },
            supportingContent = { Text("Kelola informasi toko Anda") },
            leadingContent = { Icon(Icons.Default.Store, contentDescription = null) },
            modifier = Modifier.clickable(onClick = onProfile)
        )

        HorizontalDivider()

        ListItem(
            headlineContent = {
                Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            },
            supportingContent = { Text("Keluar dari akun Anda") },
            leadingContent = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            modifier = Modifier.clickable {
                sessionManager.clearSession()
                scope.launch {
                    userPreferences.clear()
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                }
            }
        )
    }
}
