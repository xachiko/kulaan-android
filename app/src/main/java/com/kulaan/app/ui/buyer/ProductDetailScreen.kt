package com.kulaan.app.ui.buyer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.ProductDetail
import com.kulaan.app.data.repository.ProductRepository
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.StoreUtils
import com.kulaan.app.utils.formatWhatsApp
import com.kulaan.app.utils.toFullImageUrl
import com.kulaan.app.utils.getProductPlaceholderEmoji
import com.kulaan.app.utils.formatCategoryName
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBackClick: () -> Unit,
    onStoreClick: (Int) -> Unit,
    onOrderClick: (productId: Int, quantity: Int) -> Unit,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { ProductRepository(sessionManager) }

    var isLoading by remember { mutableStateOf(true) }
    var productDetail by remember { mutableStateOf<ProductDetail?>(null) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(1) }

    LaunchedEffect(productId) {
        isLoading = true
        isError = false
        try {
            val response = repository.getProductDetail(productId)
            if (response.isSuccessful && response.body() != null) {
                val detail = response.body()!!.data
                productDetail = detail
                quantity = detail?.minOrder ?: 1
            } else {
                isError = true
                errorMessage = "Gagal memuat detail produk."
            }
        } catch (e: Exception) {
            isError = true
            errorMessage = "Terjadi kesalahan jaringan."
        } finally {
            isLoading = false
        }
    }

    val priceFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
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
            } else if (isError) {
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
                        text = errorMessage ?: "Terjadi kesalahan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF282724)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onBackClick() }) {
                        Text("Kembali ke Beranda")
                    }
                }
            } else if (productDetail != null) {
                val product = productDetail!!
                val store = product.store
                val isClosed = !StoreUtils.isStoreOpen(store?.operatingHours)
                val totalPrice = product.price * quantity

                val productEmoji = getProductPlaceholderEmoji(product.category?.nameCategory, product.idProduct)

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Product Image/Placeholder Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (product.imageUrl != null) {
                                AsyncImage(
                                    model = product.imageUrl.toFullImageUrl(),
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFE8F1FB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(productEmoji, fontSize = 96.sp)
                                }
                            }

                            if (isClosed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Red.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE24B4A), RoundedCornerShape(24.dp))
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "TUTUP",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Product Basic Info Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = formatCategoryName(product.category?.nameCategory).uppercase(Locale.getDefault()),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF185FA5)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF282724)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { starIndex ->
                                    val isFilled = starIndex < Math.round(product.rating)
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isFilled) Color(0xFFFFB300) else Color(0xFFCFCEC7),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${product.rating} · ${product.reviewCount} ulasan",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8A8980)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = priceFormatter.format(product.price),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF185FA5)
                            )
                            Text(
                                text = "per ${product.unit ?: "pcs"} · Min. Order: ${product.minOrder} · Stok: ${product.stock}",
                                fontSize = 12.sp,
                                color = Color(0xFF8A8980)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description Card
                        if (!product.description.isNullOrBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Deskripsi Produk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF282724)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = product.description,
                                    fontSize = 13.sp,
                                    color = Color(0xFF5C5B54),
                                    lineHeight = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Store Card info
                        if (store != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Penjual",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8A8980)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    onClick = { onStoreClick(store.idStore) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F3F0)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (store.logo != null) {
                                            AsyncImage(
                                                model = store.logo.toFullImageUrl(),
                                                contentDescription = store.storeName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        Color(0xFFE8F1FB),
                                                        RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("🏪", fontSize = 18.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = store.storeName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF282724)
                                            )
                                            Text(
                                                text = store.address ?: store.district ?: "Jebres",
                                                fontSize = 12.sp,
                                                color = Color(0xFF8A8980)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    Color(0xFFE8F1FB),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "✓ Verifikasi",
                                                fontSize = 10.sp,
                                                color = Color(0xFF185FA5),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (store.paymentAccounts.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Metode Pembayaran Transfer",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF8A8980)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    store.paymentAccounts.forEach { pa ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Text("💳", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${pa.bankName} — ${pa.accountNumber}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF282724)
                                                )
                                                Text(
                                                    text = pa.accountName,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF8A8980)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Closed warning banner
                        if (isClosed) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFCEBEB))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("⚠️", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Toko sedang tutup",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE24B4A)
                                        )
                                        Text(
                                            text = "Pemesanan hanya dapat dilakukan selama jam operasional toko.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE24B4A)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Reviews Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Ulasan Pembeli",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF282724)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (product.reviews.isEmpty()) {
                                Text(
                                    text = "Belum ada ulasan untuk produk ini.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8A8980),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                product.reviews.forEach { review ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val initials = review.user?.name?.firstOrNull()?.toString() ?: "A"
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(Color(0xFFC8DBED), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = initials,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF185FA5)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = review.user?.name ?: "Anonymous",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF282724)
                                                )
                                                Row {
                                                    repeat(5) { starIndex ->
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = if (starIndex < review.rating) Color(0xFFFFB300) else Color(0xFFCFCEC7),
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = review.createdAt?.split("T")?.firstOrNull() ?: "",
                                                fontSize = 11.sp,
                                                color = Color(0xFF8A8980)
                                            )
                                        }
                                        if (!review.comment.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = review.comment,
                                                fontSize = 12.sp,
                                                color = Color(0xFF5C5B54),
                                                modifier = Modifier.padding(start = 40.dp)
                                            )
                                        }
                                        Divider(
                                            color = Color(0xFFF4F3F0),
                                            thickness = 1.dp,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Order Placement Panel
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Qty selectors
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Jumlah Pesanan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF5C5B54)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (quantity > product.minOrder) quantity--
                                        },
                                        enabled = quantity > product.minOrder
                                    ) {
                                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFFE8E7E2), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = quantity.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF282724)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            if (quantity < product.stock) quantity++
                                        },
                                        enabled = quantity < product.stock
                                    ) {
                                        Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onOrderClick(product.idProduct, quantity) },
                                    enabled = product.stock > 0 && !isClosed,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185FA5)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "🛒 Pesan — ${priceFormatter.format(totalPrice)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                if (store?.phoneNumber != null) {
                                    OutlinedButton(
                                        onClick = {
                                            val cleanPhone = store.phoneNumber.formatWhatsApp()
                                            val textMsg = "Halo, saya ingin memesan ${product.name} ($quantity ${product.unit ?: "pcs"}) seharga ${priceFormatter.format(totalPrice)}."
                                            val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(textMsg)}")
                                            val intent = Intent(Intent.ACTION_VIEW, uri)
                                            try {
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Gagal membuka WhatsApp.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.width(60.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366)),
                                        border = BorderStroke(1.dp, Color(0xFFD4E8D5)),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("💬", fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
