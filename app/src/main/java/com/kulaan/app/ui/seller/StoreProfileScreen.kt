package com.kulaan.app.ui.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.Store
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.ui.theme.TextSecondary
import com.kulaan.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProfileScreen(
    storeState: UiState<Store>,
    onEditProfile: () -> Unit,
    onAddProduct: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        when (storeState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = storeState.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onEditProfile) {
                            Text("Lengkapi Profil Toko")
                        }
                    }
                }
            }
            is UiState.Success -> {
                val store = storeState.data

                // Store Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = store.storeLogo,
                            contentDescription = "Logo Toko",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = store.storeName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val statusBadge = when (store.status) {
                            "aktif" -> Pair("Aktif", Color(0xFF4CAF50))
                            "ditolak" -> Pair("Ditolak", Color(0xFFE24B4A))
                            else -> Pair("Menunggu Verifikasi", Color(0xFFFFA726))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusBadge.second.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = statusBadge.first,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = statusBadge.second
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description & Location
                if (!store.description.isNullOrBlank()) {
                    Text(
                        text = store.description,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Info items
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (!store.district.isNullOrBlank()) {
                        StoreInfoRow(Icons.Default.LocationOn, "Lokasi", store.district)
                    }
                    if (store.createdAt != null) {
                        StoreInfoRow(Icons.Default.CalendarMonth, "Bergabung", store.createdAt.take(10))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (!store.operatingHours.isNullOrBlank()) {
                            StoreDetailRow(Icons.Default.AccessTime, "Jam Buka", store.operatingHours)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        if (store.categoryName != null) {
                            StoreDetailRow(Icons.Default.Category, "Kategori", store.categoryName)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        if (!store.address.isNullOrBlank()) {
                            StoreDetailRow(Icons.Default.LocationOn, "Alamat", store.address)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Profile Button
                OutlinedButton(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profil")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Produk Unggulan Section
                Text(
                    text = "Produk Unggulan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada produk",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddProduct,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("+ Tambah Produk", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StoreInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 13.sp, color = TextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StoreDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
