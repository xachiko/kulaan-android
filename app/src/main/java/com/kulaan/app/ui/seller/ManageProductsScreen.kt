package com.kulaan.app.ui.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.kulaan.app.utils.toFullImageUrl
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.SellerProduct
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.ui.theme.TextSecondary
import com.kulaan.app.utils.UiState

@Composable
fun ManageProductsScreen(
    productsState: UiState<List<SellerProduct>>,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onDeleteProduct: (Int) -> Unit
) {
    var productToDelete by remember { mutableStateOf<SellerProduct?>(null) }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (productsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = productsState.message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onAddProduct) {
                                Text("Tambah Produk")
                            }
                        }
                    }
                }
                is UiState.Success -> {
                    if (productsState.data.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Store,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada produk",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Tekan + untuk menambahkan produk",
                                    fontSize = 13.sp,
                                    color = TextSecondary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(productsState.data) { product ->
                                SellerProductCard(
                                    product = product,
                                    onEdit = { onEditProduct(product.idProduct) },
                                    onDelete = { productToDelete = product }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Hapus Produk") },
            text = { Text("Apakah Anda yakin ingin menghapus produk \"${productToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { onDeleteProduct(it.idProduct) }
                        productToDelete = null
                    }
                ) {
                    Text("Ya, Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SellerProductCard(
    product: SellerProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                if (product.imageUrl != null) {
                    AsyncImage(
                        model = product.imageUrl.toFullImageUrl(),
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(72.dp)
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val productEmoji = com.kulaan.app.utils.getProductPlaceholderEmoji(product.category?.nameCategory, product.idProduct)
                    val thumbGradients = listOf(
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFF3D6), Color(0xFFFFE8A3))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE6F0FF), Color(0xFFC5D8FF))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF0FFF4), Color(0xFFC6F6D5))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFF0F0), Color(0xFFFFD6D6))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFF3F0FF), Color(0xFFDDD6FF))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFFF9EE), Color(0xFFFEF3C7))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE8F4F0), Color(0xFFD1EDE5))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFDF3E3), Color(0xFFFDEDCC))),
                        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFFCEBEB), Color(0xFFFDDDD8)))
                    )
                    val gradient = thumbGradients[product.idProduct % thumbGradients.size]
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(gradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = productEmoji, fontSize = 28.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rp${String.format("%,.0f", product.price).replace(",", ".")}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Stok: ${product.stock}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        if (product.unit != null) {
                            Text(
                                text = "/ ${product.unit}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
                // Status badge
                if (product.status != null) {
                    val statusColor = when (product.status) {
                        "aktif" -> Color(0xFF4CAF50)
                        "ditolak" -> Color(0xFFE24B4A)
                        else -> Color(0xFFFFA726)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = product.status.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", fontSize = 12.sp)
                }
            }
        }
    }
}
