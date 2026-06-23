package com.kulaan.app.ui.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kulaan.app.data.model.Store
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onLogoutClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    var selectedStoreForDetail by remember { mutableStateOf<Store?>(null) }
    var storeToConfirmVerify by remember { mutableStateOf<Pair<Store, String>?>(null) } // Pair of Store and status ("disetujui" or "dibatalkan")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Panel", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF282724))
                        Text("Kulaan.id", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboard(isRefresh = true) }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryBlue)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                    IconButton(onClick = onLogoutClick) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                },
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
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = state.message, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadDashboard() }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                            Text("Coba Lagi")
                        }
                    }
                }
                is UiState.Success -> {
                    val data = state.data
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 1. Metrics Cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricCard(
                                    title = "Total Toko",
                                    value = data.metrics.totalStores.toString(),
                                    color = Color(0xFF282724),
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    title = "Toko Aktif",
                                    value = data.metrics.activeStores.toString(),
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                                MetricCard(
                                    title = "Pending Verif",
                                    value = data.metrics.pendingStores.toString(),
                                    color = Color(0xFFEF6C00),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 2. Section Title
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Verifikasi Toko Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF282724))
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFEF3E3), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${data.metrics.pendingStores} pending",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        // 3. Stores List
                        if (data.stores.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tidak ada data toko.", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        } else {
                            items(data.stores) { store ->
                                AdminStoreCard(
                                    store = store,
                                    onVerifyClick = { status ->
                                        storeToConfirmVerify = Pair(store, status)
                                    },
                                    onDetailClick = {
                                        selectedStoreForDetail = store
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Verification confirmation dialog
    if (storeToConfirmVerify != null) {
        val (store, status) = storeToConfirmVerify!!
        val actionText = if (status == "disetujui") "menyetujui" else "menolak/mencabut izin"
        val statusColor = if (status == "disetujui") Color(0xFF2E7D32) else Color(0xFFC62828)

        AlertDialog(
            onDismissRequest = { storeToConfirmVerify = null },
            title = { Text("Konfirmasi Verifikasi") },
            text = { Text("Apakah Anda yakin ingin $actionText toko \"${store.storeName}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.verifyStore(store.idStore, status) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        storeToConfirmVerify = null
                    }
                ) {
                    Text("Ya, Lanjutkan", color = statusColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { storeToConfirmVerify = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Store details dialog
    if (selectedStoreForDetail != null) {
        val store = selectedStoreForDetail!!
        Dialog(onDismissRequest = { selectedStoreForDetail = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .heightIn(max = 500.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Detail Toko",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF282724)
                        )
                        TextButton(onClick = { selectedStoreForDetail = null }) {
                            Text("Tutup", color = PrimaryBlue)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        DetailItem(label = "Nama Toko", value = store.storeName, isBold = true)
                        DetailItem(label = "Kategori Toko", value = store.categoryName ?: store.idCategory?.toString() ?: "-")
                        DetailItem(label = "Pemilik Toko", value = store.owner?.name ?: "-")
                        DetailItem(label = "Deskripsi Toko", value = store.description ?: "Tidak ada deskripsi.")
                        DetailItem(label = "Jam Operasional", value = store.operatingHours ?: "-")
                        DetailItem(label = "Kecamatan", value = store.district ?: "-")
                        DetailItem(label = "Alamat Lengkap", value = store.address ?: "-")
                        DetailItem(label = "No. WhatsApp Pemilik", value = store.owner?.phoneNumber ?: "-")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AdminStoreCard(
    store: Store,
    onVerifyClick: (String) -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Store Name, Category and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = store.storeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF282724)
                    )
                    Text(
                        text = store.categoryName ?: "Kategori Umum",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status badge
                val statusText = when (store.verificationStatus) {
                    "menunggu" -> "Pending"
                    "disetujui" -> "Disetujui"
                    "dibatalkan" -> "Ditolak"
                    else -> store.verificationStatus ?: "Pending"
                }
                val badgeColor = when (store.verificationStatus) {
                    "menunggu" -> Color(0xFFFFF9EE) to Color(0xFFD97706)
                    "disetujui" -> Color(0xFFE8F4F0) to Color(0xFF2E7D32)
                    "dibatalkan" -> Color(0xFFFCEBEB) to Color(0xFFC62828)
                    else -> Color(0xFFF4F3F0) to Color.Gray
                }
                Box(
                    modifier = Modifier
                        .background(badgeColor.first, RoundedCornerShape(100.dp))
                        .border(1.dp, badgeColor.second.copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = badgeColor.second,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details: Owner, Address, Joined Date
            DetailSummaryRow(label = "Pemilik", value = store.owner?.name ?: "-")
            DetailSummaryRow(label = "Alamat", value = store.address ?: store.district ?: "-")
            DetailSummaryRow(label = "Tanggal", value = formatStoreDate(store.createdAt))

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color(0xFFE8E7E2))

            Spacer(modifier = Modifier.height(12.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lihat Data",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    modifier = Modifier
                        .clickable(onClick = onDetailClick)
                        .padding(vertical = 4.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (store.verificationStatus == "menunggu") {
                        Button(
                            onClick = { onVerifyClick("disetujui") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Setujui", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = { onVerifyClick("dibatalkan") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Tolak", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else if (store.verificationStatus == "disetujui") {
                        Button(
                            onClick = { onVerifyClick("dibatalkan") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Cabut Izin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "$label :", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(60.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF282724),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DetailItem(label: String, value: String, isBold: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label.uppercase(Locale.getDefault()), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF282724),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            lineHeight = 18.sp
        )
    }
}

private fun formatStoreDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"
    return try {
        // Handle ISO-8601 string: e.g. "2026-06-22T13:20:19.000000Z"
        val cleanString = dateString.substringBefore(".")
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(cleanString) ?: return dateString
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        formatter.format(date)
    } catch (e: Exception) {
        dateString
    }
}
