package com.kulaan.app.ui.buyer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.Product
import com.kulaan.app.data.model.Store
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.ui.buyer.components.ProductCard
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.StoreUtils
import com.kulaan.app.utils.formatWhatsApp
import com.kulaan.app.utils.toFullImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    storeId: Int,
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    sessionManager: SessionManager
) {
    val repository = remember { StoreRepository(sessionManager) }

    var isLoading by remember { mutableStateOf(true) }
    var storeDetail by remember { mutableStateOf<Store?>(null) }
    var storeProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(storeId) {
        isLoading = true
        errorMessage = null
        try {
            val result = repository.getStoreDetailPublic(storeId)
            result.onSuccess { res ->
                storeDetail = res.data
                // Map store products
                // In Laravel, the response returns the store with its loaded products
                // Let's parse them or handle them cleanly. Let's see: Store model contains list of products?
                // Wait! In StoreResource.php line 18:
                // if ($this->relationLoaded('products')) {
                //    $data['products'] = $this->products->map(...)
                // }
                // So the products array is indeed loaded inside `Store`'s `products` field!
                // Wait, let's verify if Store model has a products field. In Store.kt, it currently does NOT have it.
                // Let's view Store.kt:
                // No, Store.kt only has idStore, storeName, description, address, district, operatingHours, storeLogo, status, etc.
                // Let's check how we can represent products list inside Store.kt.
                // We should add: `val products: List<Product>? = null` to the `Store` data class!
                // This is extremely simple and will allow gson to deserialize store products automatically!
                // Let's first look at Store.kt to check if we can modify it. Yes, let's append `val products: List<Product>? = null` in Store.kt!
            }
            .onFailure {
                errorMessage = "Gagal memuat detail toko."
            }
        } catch (e: Exception) {
            errorMessage = "Koneksi terputus."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(storeDetail?.storeName ?: "Profil Toko", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                    Text(text = errorMessage ?: "Error", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Kembali")
                    }
                }
            } else if (storeDetail != null) {
                val store = storeDetail!!
                // We will add the products field to Store.kt, so we can access it here.
                val products = store.products ?: emptyList()
                val isOpen = StoreUtils.isStoreOpen(store.operatingHours)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Section
                    item(span = { GridItemSpan(2) }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (store.storeLogo != null) {
                                        AsyncImage(
                                            model = store.storeLogo.toFullImageUrl(),
                                            contentDescription = store.storeName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(Color(0xFFE8F1FB), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🏪", fontSize = 24.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = store.storeName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF282724)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE8F1FB), RoundedCornerShape(100.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "✓ Verifikasi",
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF185FA5),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (!isOpen) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFFCEBEB), RoundedCornerShape(100.dp))
                                                        .border(1.dp, Color(0xFFE24B4A), RoundedCornerShape(100.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "TUTUP",
                                                        fontSize = 8.sp,
                                                        color = Color(0xFFE24B4A),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = store.categoryName ?: "Kategori Umum",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF185FA5)
                                        )
                                    }
                                }

                                if (!store.description.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = store.description,
                                        fontSize = 13.sp,
                                        color = Color(0xFF5C5B54),
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color(0xFFF4F3F0))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isOpen) "🟢 BUKA" else "🔴 TUTUP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isOpen) Color(0xFF137333) else Color(0xFFE24B4A)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = store.operatingHours ?: "Jam tidak diatur",
                                        fontSize = 12.sp,
                                        color = Color(0xFF8A8980)
                                    )
                                }

                                if (!store.address.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "📍 ${store.address}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF5C5B54)
                                    )
                                }

                                // Kontak & Sosial Media Section
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider(color = Color(0xFFF4F3F0))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Kontak & Sosial Media",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF282724)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    
                                    // WhatsApp Button
                                    Button(
                                        onClick = {
                                            if (!store.whatsapp.isNullOrBlank()) {
                                                val cleanedPhone = store.whatsapp.formatWhatsApp()
                                                val uri = Uri.parse("https://wa.me/$cleanedPhone")
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    android.widget.Toast.makeText(context, "Gagal membuka WhatsApp.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        enabled = !store.whatsapp.isNullOrBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF25D366),
                                            disabledContainerColor = Color(0xFFF5F5F5),
                                            disabledContentColor = Color.Gray
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFD4E8D5)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (!store.whatsapp.isNullOrBlank()) "💬 WhatsApp" else "WhatsApp tidak tersedia",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Instagram Button
                                    Button(
                                        onClick = {
                                            if (!store.instagram.isNullOrBlank()) {
                                                val igUsername = store.instagram.replace("@", "").trim()
                                                val uri = Uri.parse("https://instagram.com/$igUsername")
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    android.widget.Toast.makeText(context, "Gagal membuka Instagram.", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        enabled = !store.instagram.isNullOrBlank(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFF4F3F0),
                                            contentColor = Color(0xFF3E3D38),
                                            disabledContainerColor = Color(0xFFF5F5F5),
                                            disabledContentColor = Color.Gray
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFE8E7E2)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (!store.instagram.isNullOrBlank()) "📸 @${store.instagram.replace("@", "").trim()}" else "Instagram tidak tersedia",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Store products grid title
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "Katalog Produk Toko",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF282724),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (products.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada produk aktif di toko ini.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8A8980)
                                )
                            }
                        }
                    } else {
                        items(products) { product ->
                            // Map public products list
                            // We will format product card for store page
                            // Since ProductCard accepts Product model, let's supply it
                            // Wait! In Store detail public, the products items contain category and store properties.
                            // If they are missing, let's supply default store info
                            val mappedProduct = product.copy(store = com.kulaan.app.data.model.StoreShort(store.idStore, store.storeName, store.district, store.operatingHours))
                            ProductCard(
                                product = mappedProduct,
                                onClick = { onProductClick(product.idProduct) }
                            )
                        }
                    }
                }
            }
        }
    }
}
