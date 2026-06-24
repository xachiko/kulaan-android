package com.kulaan.app.ui.buyer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.Product
import com.kulaan.app.utils.StoreUtils
import com.kulaan.app.utils.toFullImageUrl
import com.kulaan.app.utils.getProductPlaceholderEmoji
import com.kulaan.app.utils.formatCategoryName
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOpen = StoreUtils.isStoreOpen(product.store?.operatingHours)
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }
    val priceString = formatter.format(product.price)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(270.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Bagian Atas: Gambar / Thumbnail Tempat Produk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl.toFullImageUrl(),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val productEmoji = getProductPlaceholderEmoji(product.category?.nameCategory, product.idProduct)
                    val thumbGradients = listOf(
                        Brush.linearGradient(listOf(Color(0xFFFFF3D6), Color(0xFFFFE8A3))),
                        Brush.linearGradient(listOf(Color(0xFFE6F0FF), Color(0xFFC5D8FF))),
                        Brush.linearGradient(listOf(Color(0xFFF0FFF4), Color(0xFFC6F6D5))),
                        Brush.linearGradient(listOf(Color(0xFFFFF0F0), Color(0xFFFFD6D6))),
                        Brush.linearGradient(listOf(Color(0xFFF3F0FF), Color(0xFFDDD6FF))),
                        Brush.linearGradient(listOf(Color(0xFFFFF9EE), Color(0xFFFEF3C7))),
                        Brush.linearGradient(listOf(Color(0xFFE8F4F0), Color(0xFFD1EDE5))),
                        Brush.linearGradient(listOf(Color(0xFFFDF3E3), Color(0xFFFDEDCC))),
                        Brush.linearGradient(listOf(Color(0xFFFCEBEB), Color(0xFFFDDDD8)))
                    )
                    val gradient = thumbGradients[product.idProduct % thumbGradients.size]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = productEmoji, fontSize = 56.sp)
                    }
                }

                // Badge Kategori
                if (product.category?.nameCategory != null) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formatCategoryName(product.category.nameCategory),
                            fontSize = 10.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Overlay Toko Tutup
                if (!isOpen) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TUTUP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // 2. Bagian Bawah: Informasi Detail Produk (Menggunakan SpaceBetween agar sejajar)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Kelompok Teks Atas (Nama Toko & Produk)
                Column {
                    Text(
                        text = product.store?.storeName ?: "Toko Tidak Diketahui",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                // Kelompok Teks Bawah (Harga & Rating)
                Column {
                    Text(
                        text = priceString,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${product.rating} (${product.reviewCount})",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}