package com.kulaan.app.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kulaan.app.data.model.Store
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.StoreUtils
import com.kulaan.app.utils.toFullImageUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreListScreen(
    onBackClick: () -> Unit,
    onStoreClick: (Int) -> Unit,
    sessionManager: SessionManager
) {
    val repository = remember { StoreRepository(sessionManager) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua Toko") }
    var isLoading by remember { mutableStateOf(true) }
    var storesList by remember { mutableStateOf<List<Store>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Semua Toko",
        "Makanan & Minuman",
        "Katering",
        "Sembako & kebutuhan pokok",
        "Pakaian",
        "Fashion & aksesoris",
        "Kerajinan tangan",
        "Jasa & Layanan",
        "Perlengkapan rumah"
    )

    fun loadStores() {
        isLoading = true
        errorMessage = null
        val queryCat = if (selectedCategory == "Semua Toko") null else selectedCategory
        val queryKw = if (searchQuery.isBlank()) null else searchQuery

        scope.launch {
            try {
                val result = repository.getStores(queryKw, queryCat)
                result.onSuccess {
                    storesList = it.data
                }.onFailure {
                    errorMessage = "Gagal memuat daftar toko."
                }
            } catch (e: Exception) {
                errorMessage = "Koneksi terputus."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedCategory, searchQuery) {
        loadStores()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toko UMKM", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF4F3F0))
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama toko atau kategori...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF185FA5)
                ),
                singleLine = true
            )

            // Category Scroll Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    val bgColor = if (isSelected) Color(0xFF185FA5) else Color.White
                    val txtColor = if (isSelected) Color.White else Color(0xFF5C5B54)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = txtColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stores List
            Box(modifier = Modifier.weight(1f)) {
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
                    }
                } else if (storesList.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Toko tidak ditemukan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF282724)
                        )
                        Text(
                            text = "Coba gunakan kata kunci atau kategori lain",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8980)
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(storesList) { store ->
                            StoreCard(
                                store = store,
                                onClick = { onStoreClick(store.idStore) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreCard(
    store: Store,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (store.storeLogo != null) {
                AsyncImage(
                    model = store.storeLogo.toFullImageUrl(),
                    contentDescription = store.storeName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE8E7E2), RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE8F1FB), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏪", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val isClosed = !StoreUtils.isStoreOpen(store.operatingHours)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = store.storeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF282724),
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F1FB), RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✓ Verifikasi",
                            fontSize = 9.sp,
                            color = Color(0xFF185FA5),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isClosed) {
                        Spacer(modifier = Modifier.width(6.dp))
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
                    text = store.categoryName ?: store.storeName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF185FA5)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = store.description ?: "Toko produk lokal di Jebres.",
                    fontSize = 12.sp,
                    color = Color(0xFF5C5B54),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
