package com.kulaan.app.ui.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kulaan.app.data.model.Category
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.ui.seller.viewmodel.SellerViewModel
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    sessionManager: SessionManager,
    productId: Int,
    viewModel: SellerViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StoreRepository(sessionManager) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minOrder by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isActive by remember { mutableStateOf(true) }
    
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var submitState by remember { mutableStateOf<UiState<Unit>?>(null) }
    
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }

    val units = listOf("pcs", "kg", "liter", "bungkus", "porsi", "box", "meter", "lusin")
    
    val defaultCategories = listOf(
        Category(1, "pakaian", null),
        Category(2, "fashion & aksesoris", null),
        Category(3, "makanan & minuman", null),
        Category(4, "perawatan & kecantikan", null),
        Category(5, "perlengkapan rumah", null),
        Category(6, "hobi & koleksi", null),
        Category(7, "kesehatan", null),
        Category(8, "olahraga & outdoor", null),
        Category(9, "buku & alat tulis", null),
        Category(10, "kerajinan tangan", null),
        Category(11, "sembako & kebutuhan pokok", null),
        Category(12, "Jasa & Layanan", null),
        Category(13, "Katering", null),
        Category(14, "lain lain", null)
    )

    // Load initial data
    LaunchedEffect(productId) {
        val product = viewModel.getProductById(productId)
        if (product != null) {
            name = product.name
            price = String.format("%.0f", product.price)
            unit = product.unit ?: ""
            stock = product.stock.toString()
            isActive = product.status != "ditolak"
            // We use default values for missing details in SellerProduct listing model
        }

        val result = repository.getCategories()
        result.onSuccess { 
            categories = if (it.data.isNotEmpty()) it.data else defaultCategories
            if (product?.category != null) {
                // Attempt to match category
                selectedCategoryId = categories.find { cat -> cat.nameCategory.equals(product.category.nameCategory, ignoreCase = true) }?.idCategory
            }
        }.onFailure {
            categories = defaultCategories
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Produk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nama Produk
            Column {
                Text("Nama Produk *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nama produk", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            // Kategori
            Column {
                Text("Kategori *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.idCategory == selectedCategoryId }?.nameCategory ?: "Pilih kategori...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.nameCategory) },
                                onClick = {
                                    selectedCategoryId = category.idCategory
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Harga and Stok Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Harga *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Stok *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it.filter { c -> c.isDigit() } },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Satuan and Min Pesan Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Satuan *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    ExposedDropdownMenuBox(
                        expanded = showUnitDropdown,
                        onExpandedChange = { showUnitDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = if (unit.isEmpty()) "Pilih satuan" else unit,
                            onValueChange = { unit = it },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u) },
                                    onClick = {
                                        unit = u
                                        showUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Min. Pesan *", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = minOrder,
                        onValueChange = { minOrder = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Deskripsi
            Column {
                Text("Deskripsi", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Deskripsi produk (opsional)", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 5
                )
            }

            // Gambar Produk
            Column {
                Text("Gambar Produk", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (imageUri != null) "Gambar baru terpilih" else "Biarkan kosong jika tidak diubah",
                        fontSize = 13.sp,
                        color = if (imageUri != null) PrimaryBlue else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Pilih", color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Batal", color = Color.DarkGray)
                }
                Button(
                    onClick = {
                        if (name.isBlank() || price.isBlank() || stock.isBlank() || selectedCategoryId == null) {
                            submitState = UiState.Error("Lengkapi field yang wajib diisi")
                            return@Button
                        }
                        
                        submitState = UiState.Loading
                        coroutineScope.launch {
                            val priceValue = price.toDoubleOrNull() ?: 0.0
                            val stockValue = stock.toIntOrNull() ?: 0
                            val minOrderValue = minOrder.toIntOrNull() ?: 1
                            val selectedCategory = categories.find { it.idCategory == selectedCategoryId }
                            
                            val result = repository.updateProduct(
                                id = productId,
                                name = name,
                                price = priceValue,
                                unit = unit.ifBlank { null },
                                stock = stockValue,
                                minOrder = minOrderValue,
                                description = description.ifBlank { null },
                                idCategory = selectedCategoryId ?: return@launch,
                                categoryName = selectedCategory?.nameCategory ?: "",
                                imageUri = imageUri,
                                context = context
                            )
                            
                            result.onSuccess {
                                snackbarHostState.showSnackbar(
                                    message = "Produk \"$name\" berhasil diubah!",
                                    duration = SnackbarDuration.Short
                                )
                                submitState = UiState.Success(Unit)
                                delay(800)
                                onSuccess()
                            }.onFailure { e ->
                                submitState = UiState.Error(e.message ?: "Gagal mengubah produk")
                            }
                        }
                    },
                    modifier = Modifier.weight(2f).height(48.dp),
                    enabled = submitState !is UiState.Loading,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (submitState is UiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (submitState is UiState.Error) {
                Text(
                    text = (submitState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
