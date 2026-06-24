package com.kulaan.app.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.kulaan.app.ui.buyer.components.ProductCard
import com.kulaan.app.ui.buyer.viewmodel.ProductViewModel
import com.kulaan.app.ui.buyer.viewmodel.ProductViewModelFactory
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToStores: () -> Unit,
    onNavigateToPopularProducts: () -> Unit,
    onProductClick: (Int) -> Unit,
    viewModel: ProductViewModel = viewModel(
        factory = ProductViewModelFactory(SessionManager(LocalContext.current))
    )
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userName = remember { sessionManager.getName() ?: "Pembeli" }

    val productsState by viewModel.productsState.collectAsState()
    val categoriesState by viewModel.categoriesState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val gridState = rememberLazyGridState()

    // Pagination logic
    LaunchedEffect(gridState.layoutInfo.visibleItemsInfo) {
        val lastVisibleItem = gridState.layoutInfo.visibleItemsInfo.lastOrNull()
        if (lastVisibleItem != null && lastVisibleItem.index >= gridState.layoutInfo.totalItemsCount - 2) {
            viewModel.loadNextPage()
        }
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val minPrice by viewModel.minPrice.collectAsState()
    val maxPrice by viewModel.maxPrice.collectAsState()
    var tempMinPrice by remember { mutableStateOf("") }
    var tempMaxPrice by remember { mutableStateOf("") }

    LaunchedEffect(showFilterSheet) {
        if (showFilterSheet) {
            tempMinPrice = minPrice?.toString() ?: ""
            tempMaxPrice = maxPrice?.toString() ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Welcome Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF185FA5), Color(0xFF42A5F5))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "Halo, $userName! 👋",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Temukan produk UMKM lokal terbaik di Jebres hari ini",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchProducts(it) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                placeholder = { Text("Cari produk atau toko...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF185FA5)
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF185FA5), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF185FA5))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showFilterSheet = true },
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Filter", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Cards (Stores and Popular Products)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                onClick = onNavigateToStores,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE8F1FB), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏪", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Toko UMKM", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF282724))
                        Text("Direktori toko", fontSize = 10.sp, color = Color(0xFF8A8980))
                    }
                }
            }

            Card(
                onClick = onNavigateToPopularProducts,
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFEF3E3), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔥", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Produk Populer", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF282724))
                        Text("Terlaris & favorit", fontSize = 10.sp, color = Color(0xFF8A8980))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CategoryPill(
                    name = "🔥 Semua",
                    isSelected = selectedCategory == null,
                    onClick = { viewModel.filterByCategory(null) }
                )
            }
            items(categoriesState) { category ->
                val emoji = getCategoryEmoji(category.nameCategory)
                CategoryPill(
                    name = "$emoji ${category.nameCategory.lowercase()}",
                    isSelected = selectedCategory == category.idCategory,
                    onClick = { viewModel.filterByCategory(category.idCategory) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Produk UMKM",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Product Grid
        when (val state = productsState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF185FA5))
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = Color.Red)
                }
            }
            is UiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.data) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product.idProduct) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text("Filter Harga", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text("Pilih Rentang Harga", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllSelected = tempMinPrice.isEmpty() && tempMaxPrice.isEmpty()
                    PriceRangePill(
                        label = "Semua Harga",
                        isSelected = isAllSelected,
                        onClick = {
                            tempMinPrice = ""
                            tempMaxPrice = ""
                        },
                        modifier = Modifier.weight(1f)
                    )
                    val isUnder10Selected = tempMinPrice.isEmpty() && tempMaxPrice == "10000"
                    PriceRangePill(
                        label = "< Rp 10.000",
                        isSelected = isUnder10Selected,
                        onClick = {
                            tempMinPrice = ""
                            tempMaxPrice = "10000"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val is10to25Selected = tempMinPrice == "10000" && tempMaxPrice == "25000"
                    PriceRangePill(
                        label = "Rp 10rb - 25rb",
                        isSelected = is10to25Selected,
                        onClick = {
                            tempMinPrice = "10000"
                            tempMaxPrice = "25000"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    val is25to50Selected = tempMinPrice == "25000" && tempMaxPrice == "50000"
                    PriceRangePill(
                        label = "Rp 25rb - 50rb",
                        isSelected = is25to50Selected,
                        onClick = {
                            tempMinPrice = "25000"
                            tempMaxPrice = "50000"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isOver50Selected = tempMinPrice == "50000" && tempMaxPrice.isEmpty()
                    PriceRangePill(
                        label = "> Rp 50.000",
                        isSelected = isOver50Selected,
                        onClick = {
                            tempMinPrice = "50000"
                            tempMaxPrice = ""
                        },
                        modifier = Modifier.weight(0.5f)
                    )
                    Spacer(modifier = Modifier.weight(0.5f))
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Custom Harga", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tempMinPrice,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                tempMinPrice = input
                            }
                        },
                        placeholder = { Text("Rp min", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF185FA5)
                        )
                    )
                    Text(" - ", modifier = Modifier.padding(horizontal = 8.dp))
                    OutlinedTextField(
                        value = tempMaxPrice,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                tempMaxPrice = input
                            }
                        },
                        placeholder = { Text("Rp max", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF185FA5)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        viewModel.filterByPrice(
                            min = tempMinPrice.toIntOrNull(),
                            max = tempMaxPrice.toIntOrNull()
                        )
                        showFilterSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185FA5))
                ) {
                    Text("Terapkan Filter", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CategoryPill(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF1976D2) else Color.White
    val contentColor = if (isSelected) Color.White else Color.DarkGray
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFE0E0E0)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PriceRangePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF1976D2) else Color.White
    val contentColor = if (isSelected) Color.White else Color.DarkGray
    val borderColor = if (isSelected) Color.Transparent else Color(0xFFE0E0E0)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

fun getCategoryEmoji(name: String): String {
    return when (name.lowercase()) {
        "pakaian" -> "👕"
        "fashion & aksesoris" -> "🕶️"
        "makanan & minuman" -> "🍱"
        "perawatan & kecantikan" -> "💄"
        "perlengkapan rumah" -> "🏠"
        "hobi & koleksi" -> "🎨"
        "kesehatan" -> "💊"
        "olahraga & outdoor" -> "⚽"
        "buku & alat tulis" -> "📚"
        "kerajinan tangan" -> "🧶"
        "sembako & kebutuhan pokok" -> "🛒"
        "jasa & layanan" -> "🛠️"
        "katering" -> "🥘"
        "lain lain", "lain-lain" -> "📦"
        else -> "🏷️"
    }
}
