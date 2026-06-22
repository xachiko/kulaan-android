package com.kulaan.app.ui.buyer

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kulaan.app.MainActivity
import com.kulaan.app.data.local.UserPreferences
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AuthRepository(sessionManager) }
    val userPreferences = remember { UserPreferences(context) }

    var isLoading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val loadProfile = suspend {
        isLoading = true
        try {
            val response = repository.getMe()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.data
                name = user?.name ?: ""
                email = user?.email ?: ""
                phoneNumber = user?.phoneNumber ?: ""
                address = user?.address ?: ""
            } else {
                Toast.makeText(context, "Gagal memuat profil.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Kesalahan jaringan.", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Saya", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF4F3F0))
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF185FA5))
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Edit Fields Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Informasi Profil",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF282724)
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Nama Lengkap") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = false // Email is generally read-only on Laravel profile update
                            )

                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("No. WhatsApp") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = address,
                                onValueChange = { address = it },
                                label = { Text("Alamat Pengiriman") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (name.isBlank() || phoneNumber.isBlank()) {
                                        Toast.makeText(context, "Harap lengkapi nama dan no WhatsApp.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isSaving = true
                                    scope.launch {
                                        try {
                                            val response = repository.updateProfile(name, email, phoneNumber, address.ifBlank { null })
                                            if (response.isSuccessful && response.body()?.success == true) {
                                                Toast.makeText(context, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                                                // Sync locally cached user preferences
                                                val freshUser = response.body()?.data
                                                if (freshUser != null) {
                                                    val roleStr = freshUser.roles.firstOrNull() ?: ""
                                                    sessionManager.saveSession(
                                                        token = sessionManager.getToken() ?: "",
                                                        userId = freshUser.id_user,
                                                        name = freshUser.name,
                                                        email = freshUser.email,
                                                        role = roleStr,
                                                        hasStore = freshUser.store != null
                                                    )
                                                    // Sync DataStore
                                                    userPreferences.saveAuthData(
                                                        sessionManager.getToken() ?: "",
                                                        com.kulaan.app.data.model.User(
                                                            id_user = freshUser.id_user,
                                                            name = freshUser.name,
                                                            email = freshUser.email,
                                                            roles = freshUser.roles,
                                                            phoneNumber = freshUser.phoneNumber,
                                                            address = freshUser.address
                                                        )
                                                    )
                                                }
                                            } else {
                                                Toast.makeText(context, "Gagal memperbarui profil.", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Kesalahan koneksi jaringan.", Toast.LENGTH_SHORT).show()
                                        } finally {
                                            isSaving = false
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185FA5)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Logout Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        try {
                                            // Call Laravel logout
                                            repository.logout()
                                        } catch (e: Exception) {
                                            // Fail silently, clear session anyway
                                        }
                                        userPreferences.clear()
                                        sessionManager.clearSession()
                                        val intent = Intent(context, MainActivity::class.java).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE24B4A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Keluar (Logout)", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
