package com.kulaan.app.ui.buyer

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kulaan.app.data.model.ProductDetail
import com.kulaan.app.data.model.StoreOrderRequest
import com.kulaan.app.data.model.UserMeDetail
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.OrderRepository
import com.kulaan.app.data.repository.ProductRepository
import com.kulaan.app.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    productId: Int,
    initialQuantity: Int,
    onBackClick: () -> Unit,
    onGoToProfile: () -> Unit,
    onOrderSuccess: () -> Unit,
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val productRepo = remember { ProductRepository(sessionManager) }
    val orderRepo = remember { OrderRepository(sessionManager) }
    val authRepo = remember { AuthRepository(sessionManager) }

    var isLoadingProduct by remember { mutableStateOf(true) }
    var productDetail by remember { mutableStateOf<ProductDetail?>(null) }
    var userProfile by remember { mutableStateOf<UserMeDetail?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Form inputs
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedPayment by remember { mutableStateOf("cod") } // "cod", "bank_transfer", "qris"
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        isLoadingProduct = true
        errorMessage = null
        try {
            // Fetch product
            val prodResponse = productRepo.getProductDetail(productId)
            if (prodResponse.isSuccessful && prodResponse.body()?.success == true) {
                productDetail = prodResponse.body()?.data
            } else {
                errorMessage = "Gagal memuat produk."
            }

            // Fetch user profile
            val meResponse = authRepo.getMe()
            if (meResponse.isSuccessful && meResponse.body()?.success == true) {
                val user = meResponse.body()?.data
                userProfile = user
                name = user?.name ?: ""
                phoneNumber = user?.phoneNumber ?: ""
            }
        } catch (e: Exception) {
            errorMessage = "Terjadi kesalahan koneksi."
        } finally {
            isLoadingProduct = false
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
                title = { Text("Form Pesanan", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            if (isLoadingProduct) {
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
                    Button(onClick = onBackClick) {
                        Text("Kembali")
                    }
                }
            } else if (productDetail != null) {
                val product = productDetail!!
                val store = product.store
                val totalPrice = product.price * initialQuantity
                val address = userProfile?.address

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section 1: Customer Details
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF185FA5), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Informasi Pemesan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF282724))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nama Pemesan") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("No. WhatsApp") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("Alamat Pengiriman", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF8A8980))
                                Spacer(modifier = Modifier.height(8.dp))

                                if (!address.isNullOrBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF4F3F0), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFE8E7E2), RoundedCornerShape(8.dp))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = address,
                                            fontSize = 13.sp,
                                            color = Color(0xFF282724),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ubah",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF185FA5),
                                            modifier = Modifier.clickable { onGoToProfile() }
                                        )
                                    }
                                } else {
                                    // Alamat Belum Diatur Alert
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFEF3E3), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFFCD34D), RoundedCornerShape(8.dp))
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("⚠️", fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Alamat Belum Diatur", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Anda harus mengisi alamat pengiriman di profil sebelum membuat pesanan.",
                                                fontSize = 12.sp,
                                                color = Color(0xFFB45309)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = onGoToProfile,
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Atur Alamat Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section 2: Payment Selector
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF185FA5), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("2", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Metode Pembayaran", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF282724))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                PaymentOptionRow(
                                    title = "COD (Bayar di Tempat)",
                                    description = "Bayar tunai saat barang tiba",
                                    icon = "🤝",
                                    isSelected = selectedPayment == "cod",
                                    onClick = { selectedPayment = "cod" }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                PaymentOptionRow(
                                    title = "Transfer Bank",
                                    description = "Konfirmasi via WhatsApp setelah transfer",
                                    icon = "🏦",
                                    isSelected = selectedPayment == "bank_transfer",
                                    onClick = { selectedPayment = "bank_transfer" }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                PaymentOptionRow(
                                    title = "QRIS",
                                    description = "Scan kode QR dari penjual",
                                    icon = "📱",
                                    isSelected = selectedPayment == "qris",
                                    onClick = { selectedPayment = "qris" }
                                )
                            }
                        }

                        // Section 3: Notes
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFF185FA5), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("3", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Catatan Tambahan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF282724))
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    placeholder = { Text("Contoh: Rasa pedas, kurangi gula, atau instruksi khusus lainnya") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                            }
                        }
                    }

                    // Bottom Order Panel
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
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("Total Pembayaran", fontSize = 11.sp, color = Color(0xFF8A8980))
                                    Text(
                                        text = priceFormatter.format(totalPrice),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF185FA5)
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (name.isBlank() || phoneNumber.isBlank()) {
                                            Toast.makeText(context, "Harap lengkapi Nama dan No. WhatsApp.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        if (address.isNullOrBlank()) {
                                            Toast.makeText(context, "Harap lengkapi alamat pengiriman.", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        isSubmitting = true
                                        scope.launch {
                                            try {
                                                // Map visual payment option to Laravel accepted format: 'cod' or 'transfer'
                                                val paymentMethodMapped = if (selectedPayment == "cod") "cod" else "transfer"
                                                val request = StoreOrderRequest(
                                                    idProduct = product.idProduct,
                                                    quantity = initialQuantity,
                                                    name = name,
                                                    phoneNumber = phoneNumber,
                                                    shippingAddress = address,
                                                    paymentMethod = paymentMethodMapped,
                                                    note = note.ifBlank { null }
                                                )
                                                val response = orderRepo.createOrder(request)
                                                if (response.isSuccessful && response.body()?.success == true) {
                                                    Toast.makeText(context, "Pesanan berhasil dibuat!", Toast.LENGTH_SHORT).show()

                                                    // Trigger WhatsApp redirection if phone exists
                                                    val storePhone = store?.phoneNumber
                                                    if (storePhone != null) {
                                                        val cleanedPhone = storePhone.replace("[^0-9]".toRegex(), "")
                                                        val messageTemplate = "Halo, saya $name telah memesan ${product.name} sebanyak $initialQuantity ${product.unit ?: "pcs"} dengan total ${priceFormatter.format(totalPrice)} melalui Kulaan.id.\n\nAlamat: $address\nCatatan: ${note.ifBlank { "-" }}\nMetode Pembayaran: ${selectedPayment.uppercase(Locale.getDefault())}"
                                                        val uri = Uri.parse("https://wa.me/$cleanedPhone?text=${Uri.encode(messageTemplate)}")
                                                        val waIntent = Intent(Intent.ACTION_VIEW, uri)
                                                        try {
                                                            context.startActivity(waIntent)
                                                        } catch (e: Exception) {
                                                            // Fail silently or fallback
                                                        }
                                                    }
                                                    onOrderSuccess()
                                                } else {
                                                    Toast.makeText(context, "Gagal membuat pesanan: ${response.message()}", Toast.LENGTH_LONG).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Terjadi kesalahan koneksi.", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    },
                                    enabled = !isSubmitting && !address.isNullOrBlank() && name.isNotBlank() && phoneNumber.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF185FA5)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Kirim Pesanan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

@Composable
private fun PaymentOptionRow(
    title: String,
    description: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF185FA5) else Color(0xFFE8E7E2)
    val background = if (isSelected) Color(0xFFE8F1FB) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF185FA5))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF282724))
            Text(description, fontSize = 11.sp, color = Color(0xFF8A8980))
        }
    }
}
