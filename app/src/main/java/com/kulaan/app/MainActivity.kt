package com.kulaan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kulaan.app.data.local.UserPreferences
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.ui.auth.AuthScreen
import com.kulaan.app.ui.auth.AuthViewModel
import com.kulaan.app.ui.auth.AuthViewModelFactory
import com.kulaan.app.ui.auth.RolePickerScreen
import com.kulaan.app.ui.seller.SellerBottomNavigation
import com.kulaan.app.ui.seller.StoreSetupScreen
import com.kulaan.app.ui.theme.KulaanTheme
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences(this)
        val sessionManager = SessionManager(this)
        val authRepository = AuthRepository(sessionManager)
        val authViewModelFactory = AuthViewModelFactory(authRepository, userPreferences, sessionManager)

        setContent {
            KulaanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()
                    val token by userPreferences.authToken.collectAsState(initial = null)
                    val roles by userPreferences.userRoles.collectAsState(initial = null)

                    LaunchedEffect(token, roles) {
                        // Wait until the DataStore has emitted its first value (roles transitions from null to List)
                        if (roles == null) return@LaunchedEffect

                        if (token == null) {
                            // Not logged in
                            navController.navigate("auth") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            // Logged in, navigate based on roles
                            when {
                                roles!!.contains("admin") -> {
                                    navController.navigate("admin_dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                                roles!!.contains("buyer") && roles!!.contains("seller") -> {
                                    navController.navigate("role_picker") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                                roles!!.contains("seller") -> {
                                    navController.navigate("seller_dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                                roles!!.contains("buyer") -> {
                                    navController.navigate("buyer_dashboard") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                                else -> {
                                    navController.navigate("auth") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        composable("auth") {
                            val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                            AuthScreen(
                                viewModel = authViewModel,
                                onNavigateToBuyer = {
                                    navController.navigate("buyer_dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onNavigateToSeller = {
                                    navController.navigate("seller_dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onShowRolePicker = {
                                    navController.navigate("role_picker") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin_dashboard") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("buyer_dashboard") {
                            com.kulaan.app.ui.buyer.BuyerBottomNavigation(sessionManager = sessionManager)
                        }
                        composable("seller_dashboard") {
                            val context = LocalContext.current
                            val repo = remember { StoreRepository(sessionManager) }
                            var dashboardState by remember { mutableStateOf<UiState<Boolean>>(UiState.Loading) }

                            LaunchedEffect(Unit) {
                                if (sessionManager.hasStore()) {
                                    dashboardState = UiState.Success(true)
                                } else {
                                    val result = repo.getStoreProfile()
                                    result.onSuccess {
                                        sessionManager.setHasStore(true)
                                        dashboardState = UiState.Success(true)
                                    }.onFailure {
                                        dashboardState = UiState.Success(false)
                                    }
                                }
                            }

                            when (val state = dashboardState) {
                                is UiState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is UiState.Success -> {
                                    if (state.data) {
                                        SellerBottomNavigation(sessionManager = sessionManager)
                                    } else {
                                        StoreSetupScreen(
                                            sessionManager = sessionManager,
                                            onSetupComplete = {
                                                navController.navigate("seller_dashboard") {
                                                    popUpTo("seller_dashboard") { inclusive = true }
                                                }
                                            }
                                        )
                                    }
                                }
                                is UiState.Error -> {
                                    SellerBottomNavigation(sessionManager = sessionManager)
                                }
                            }
                        }
                        composable("role_picker") {
                            RolePickerScreen(
                                onNavigateToBuyer = {
                                    navController.navigate("buyer_dashboard") {
                                        popUpTo("role_picker") { inclusive = true }
                                    }
                                },
                                onNavigateToSeller = {
                                    navController.navigate("seller_dashboard") {
                                        popUpTo("role_picker") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("admin_dashboard") {
                            val adminRepo = remember { com.kulaan.app.data.repository.AdminRepository(sessionManager) }
                            val adminViewModel: com.kulaan.app.ui.admin.AdminViewModel = viewModel(
                                factory = com.kulaan.app.ui.admin.AdminViewModelFactory(adminRepo)
                            )
                            com.kulaan.app.ui.admin.AdminDashboardScreen(
                                viewModel = adminViewModel,
                                onLogoutClick = {
                                    scope.launch {
                                        try {
                                            authRepository.logout()
                                        } catch (e: Exception) {}
                                        userPreferences.clear()
                                        sessionManager.clearSession()
                                        navController.navigate("auth") {
                                            popUpTo("admin_dashboard") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
