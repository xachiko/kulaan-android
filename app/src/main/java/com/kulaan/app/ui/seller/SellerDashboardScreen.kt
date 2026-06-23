package com.kulaan.app.ui.seller

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kulaan.app.data.model.RecentOrder
import com.kulaan.app.data.model.SellerDashboardData
import com.kulaan.app.data.repository.SellerRepository
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.ui.theme.TextSecondary
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.formatWhatsApp
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SellerRepository(sessionManager) }

    var isLoading by remember { mutableStateOf(true) }
    var dashboardData by remember { mutableStateOf<SellerDashboardData?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Semua, 1 = Menunggu, 2 = Diproses
    val orderTabs = listOf("Semua", "Menunggu", "Diproses")

    // Update Status Dialog state
    var showStatusDialog by remember { mutableStateOf(false) }
    var orderToUpdate by remember { mutableStateOf<RecentOrder?>(null) }
    var isUpdatingStatus by remember { mutableStateOf(false) }

    val loadDashboard = suspend {
        isLoading = true
        errorMessage = null
        try {
            val response = repository.getSellerDashboard()
            if (response.isSuccessful && response.body()?.success == true) {
                dashboardData = response.body()?.data
            } else {
                errorMessage = "Gagal memuat statistik dashboard toko."
            }
        } catch (e: Exception) {
            errorMessage = "Koneksi ke server terputus."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadDashboard()
    }

    val priceFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val filteredOrders = remember(dashboardData, selectedTab) {
        val orders = dashboardData?.recentOrders ?: emptyList()
        when (selectedTab) {
            1 -> orders.filter { it.status == "menunggu" }
            2 -> orders.filter { it.status == "diproses" }
            else -> orders
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F3F0))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryBlue)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "Dashboard UMKM",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dashboardData?.store?.storeName ?: "Toko Saya",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val storeStatus = dashboardData?.store?.verificationStatus ?: "menunggu"
                val statusBadge = when (storeStatus) {
                    "disetujui" -> Pair("Terverifikasi", Color(0xFF4CAF50))
                    "ditolak" -> Pair("Ditolak", Color(0xFFE24B4A))
                    else -> Pair("Menunggu Verifikasi", Color(0xFFFFA726))
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusBadge.first,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("😕", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage ?: "Error", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { scope.launch { loadDashboard() } }) {
                    Text("Coba Lagi")
                }
            }
        } else if (dashboardData != null) {
            val metrics = dashboardData!!.metrics

            // Stats Cards Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Default.ShoppingCart,
                        label = "Total Pesanan",
                        value = metrics.totalOrders.toString(),
                        color = PrimaryBlue
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Default.Payments,
                        label = "Pendapatan Bulan Ini",
                        value = priceFormatter.format(metrics.monthlyRevenue),
                        color = Color(0xFF4CAF50)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Default.PendingActions,
                        label = "Menunggu",
                        value = metrics.newOrders.toString(),
                        color = Color(0xFFFFA726)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Default.DirectionsRun,
                        label = "Diproses",
                        value = metrics.processedOrders.toString(),
                        color = Color(0xFF42A5F5)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Default.Inventory,
                        label = "Produk Aktif",
                        value = metrics.activeProducts.toString(),
                        color = Color(0xFF7F77DD)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        icon = Icons.Default.Warning,
                        label = "Perlu Restok",
                        value = metrics.lowStockCount.toString(),
                        color = Color(0xFFE24B4A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pesanan Masuk Section
            Text(
                text = "Pesanan Masuk Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF282724)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                orderTabs.forEachIndexed { index, tab ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(tab, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredOrders.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada pesanan masuk",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredOrders.forEach { order ->
                        SellerOrderCard(
                            order = order,
                            priceFormatter = priceFormatter,
                            onUpdateStatus = {
                                orderToUpdate = order
                                showStatusDialog = true
                            },
                            onWhatsAppClick = {
                                val phone = order.buyerPhone
                                if (phone != null) {
                                    val cleanPhone = phone.formatWhatsApp()
                                    val orderIdPadded = String.format("#ORD-%05d", order.idOrder)
                                    val msg = "Halo ${order.buyerName ?: ""}, saya dari toko ${dashboardData?.store?.storeName ?: ""} ingin mengonfirmasi pesanan Anda $orderIdPadded."
                                    val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(msg)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuka WhatsApp.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Status Update Dialog
    if (showStatusDialog && orderToUpdate != null) {
        val order = orderToUpdate!!
        AlertDialog(
            onDismissRequest = { if (!isUpdatingStatus) showStatusDialog = false },
            title = { Text("Ubah Status Pesanan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pilih status terbaru untuk pesanan #ORD-${String.format("%05d", order.idOrder)}:", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val statuses = listOf(
                        "menunggu" to "Menunggu Konfirmasi",
                        "diproses" to "Proses Pesanan",
                        "selesai" to "Selesaikan Pesanan",
                        "dibatalkan" to "Batalkan Pesanan"
                    )

                    statuses.forEach { (statusKey, label) ->
                        Button(
                            onClick = {
                                isUpdatingStatus = true
                                scope.launch {
                                    try {
                                        val response = repository.updateOrderStatus(order.idOrder, statusKey)
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            Toast.makeText(context, "Status pesanan diperbarui!", Toast.LENGTH_SHORT).show()
                                            showStatusDialog = false
                                            loadDashboard()
                                        } else {
                                            Toast.makeText(context, "Gagal memperbarui status.", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Kesalahan jaringan.", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isUpdatingStatus = false
                                    }
                                }
                            },
                            enabled = !isUpdatingStatus,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (order.status == statusKey) PrimaryBlue else Color(0xFFE8E7E2),
                                contentColor = if (order.status == statusKey) Color.White else Color(0xFF282724)
                            )
                        ) {
                            Text(label, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showStatusDialog = false },
                    enabled = !isUpdatingStatus
                ) {
                    Text("Kembali", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SellerOrderCard(
    order: RecentOrder,
    priceFormatter: NumberFormat,
    onUpdateStatus: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("#ORD-%05d", order.idOrder),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = order.orderDate?.split("T")?.firstOrNull() ?: "",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                SellerStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF4F3F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Buyer info
            Text(
                text = "Pemesan: ${order.buyerName ?: "Anonymous"}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF282724)
            )
            Text(
                text = "Alamat: ${order.shippingAddress}",
                fontSize = 12.sp,
                color = Color(0xFF5C5B54)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Products
            Text(
                text = "Item Pesanan:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            order.products.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${item.name ?: "Produk"}",
                        fontSize = 12.sp,
                        color = Color(0xFF282724)
                    )
                    Text(
                        text = "${item.quantity} ${item.unit ?: "pcs"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF282724)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF4F3F0))
            Spacer(modifier = Modifier.height(12.dp))

            // Total & actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Pembayaran", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = priceFormatter.format(order.totalOrder),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryBlue
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE6F4EA), CircleShape)
                    ) {
                        Text("💬", fontSize = 16.sp)
                    }

                    Button(
                        onClick = onUpdateStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Ubah Status", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerStatusBadge(status: String) {
    val (backgroundColor, textColor, label) = when (status) {
        "menunggu" -> Triple(Color(0xFFFDF3E3), Color(0xFFE6962A), "Menunggu")
        "diproses" -> Triple(Color(0xFFE8F1FB), Color(0xFF185FA5), "Diproses")
        "selesai" -> Triple(Color(0xFFE6F4EA), Color(0xFF137333), "Selesai")
        "dibatalkan" -> Triple(Color(0xFFFCEBEB), Color(0xFFE24B4A), "Dibatalkan")
        else -> Triple(Color(0xFFF4F3F0), Color(0xFF5C5B54), status)
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF282724),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
