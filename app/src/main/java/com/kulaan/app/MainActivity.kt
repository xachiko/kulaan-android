package com.kulaan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kulaan.app.data.local.UserPreferences
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.ui.auth.AuthScreen
import com.kulaan.app.ui.auth.AuthViewModel
import com.kulaan.app.ui.auth.AuthViewModelFactory
import com.kulaan.app.ui.theme.KulaanTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val userPreferences = UserPreferences(this)
        val authRepository = AuthRepository()
        val authViewModelFactory = AuthViewModelFactory(authRepository, userPreferences)

        setContent {
            KulaanTheme {
                val navController = rememberNavController()
                val token by userPreferences.authToken.collectAsState(initial = null)
                val roles by userPreferences.userRoles.collectAsState(initial = null)

                LaunchedEffect(token, roles) {
                    if (token != null) {
                        if (roles != null) {
                            when {
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
                                    // Fallback if token exists but no known roles
                                    navController.navigate("auth") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            }
                        }
                    } else if (token == null && roles != null) {
                        // Definitely not logged in
                        navController.navigate("auth") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }

                NavHost(navController = navController, startDestination = "splash") {
                    composable("splash") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading...")
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
                            }
                        )
                    }
                    composable("buyer_dashboard") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Buyer Dashboard Screen")
                        }
                    }
                    composable("seller_dashboard") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Seller Dashboard Screen")
                        }
                    }
                    composable("role_picker") {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Role Picker Screen")
                        }
                    }
                }
            }
        }
    }
}
