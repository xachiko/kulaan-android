package com.kulaan.app.ui.buyer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.Order
import com.kulaan.app.data.repository.OrderRepository
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.formatWhatsApp
import com.kulaan.app.utils.toFullImageUrl
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    sessionManager: SessionManager,
    onRefreshBadges: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { OrderRepository(sessionManager) }

    var isLoading by remember { mutableStateOf(true) }
    var ordersList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadOrders() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getOrders()
                if (response.isSuccessful && response.body()?.success == true) {
                    ordersList = response.body()?.data ?: emptyList()
                    onRefreshBadges()
                } else {
                    errorMessage = "Gagal memuat daftar pesanan."
                }
            } catch (e: Exception) {
                errorMessage = "Koneksi bermasalah."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    val priceFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesanan Saya", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("😕", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Error",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF282724)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        loadOrders()
                    }) {
                        Text("Coba Lagi")
                    }
                }
            } else if (ordersList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🛍️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada pesanan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF282724)
                    )
                    Text(
                        text = "Mulailah berbelanja untuk mendukung UMKM Kelurahan Jebres!",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8980),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(ordersList) { order ->
                        OrderCard(
                            order = order,
                            priceFormatter = priceFormatter,
                            onWhatsAppClick = {
                                val item = order.items.firstOrNull()
                                val storePhone = item?.product?.store?.phoneNumber
                                if (storePhone != null) {
                                    val cleanPhone = storePhone.formatWhatsApp()
                                    val orderIdPadded = String.format("#ORD-%05d", order.idOrder)
                                    val statusStr = when (order.status) {
                                        "menunggu" -> "MENUNGGU"
                                        "diproses" -> "DIPROSES"
                                        "selesai" -> "SELESAI"
                                        "dibatalkan" -> "DIBATALKAN"
                                        else -> order.status.uppercase(Locale.getDefault())
                                    }
                                    val productName = item.product?.name ?: "produk"
                                    val noteText = if (order.items.size > 1) " dan ${order.items.size - 1} produk lainnya" else ""
                                    val message = "Halo, saya ingin menanyakan status pesanan saya $orderIdPadded ($productName$noteText) senilai ${priceFormatter.format(order.totalOrder)} yang berstatus $statusStr di Kulaan.id."

                                    val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal membuka WhatsApp.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No. WhatsApp penjual tidak tersedia.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: Order,
    priceFormatter: NumberFormat,
    onWhatsAppClick: () -> Unit
) {
    val dateFormatted = remember(order.orderDate) {
        try {
            // "2026-06-22T19:05:36.000000Z" -> parse date
            val raw = order.orderDate ?: ""
            val datePart = raw.split("T").firstOrNull() ?: ""
            val timePart = raw.split("T").getOrNull(1)?.split(".")?.firstOrNull()?.substring(0, 5) ?: ""
            "$datePart $timePart"
        } catch (e: Exception) {
            order.orderDate ?: "—"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Card Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAF9))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("#ORD-%05d", order.idOrder),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF282724)
                    )
                    Text(
                        text = dateFormatted,
                        fontSize = 11.sp,
                        color = Color(0xFF8A8980)
                    )
                }

                StatusBadge(status = order.status)
            }

            // Card Body (Items)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                order.items.forEach { item ->
                    val product = item.product
                    val foodEmojis = listOf("🍱", "🍛", "🧆", "🍲", "🥙", "🍗", "🥟", "🍚", "🥘", "🍜", "🥗", "🍣")
                    val productEmoji = if (product != null) foodEmojis[product.idProduct % foodEmojis.size] else "📦"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (product?.imageUrl != null) {
                            AsyncImage(
                                model = product.imageUrl.toFullImageUrl(),
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFF4F3F0), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(productEmoji, fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product?.name ?: "Produk",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFF282724)
                            )
                            Text(
                                text = product?.store?.storeName?.uppercase(Locale.getDefault()) ?: "TOKO UMKM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8A8980)
                            )
                            Text(
                                text = "${item.quantity} ${product?.unit ?: "pcs"} x ${priceFormatter.format(item.priceAtPurchase)}",
                                fontSize = 11.sp,
                                color = Color(0xFF5C5B54)
                            )
                        }

                        Text(
                            text = priceFormatter.format(item.priceAtPurchase * item.quantity),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF282724)
                        )
                    }
                }
            }

            Divider(color = Color(0xFFF4F3F0))

            // Card Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFAFAF9))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Belanja",
                        fontSize = 10.sp,
                        color = Color(0xFF8A8980),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = priceFormatter.format(order.totalOrder),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF185FA5)
                    )
                }

                val showWhatsApp = order.status == "menunggu" || order.status == "diproses"
                if (showWhatsApp) {
                    Button(
                        onClick = onWhatsAppClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Tanya Penjual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
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
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(textColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
