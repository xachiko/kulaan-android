package com.kulaan.app.ui.seller

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.ui.seller.viewmodel.StoreSetupViewModel
import com.kulaan.app.ui.seller.viewmodel.StoreSetupViewModelFactory
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.ui.theme.TextSecondary
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSetupScreen(
    sessionManager: SessionManager,
    onSetupComplete: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { StoreRepository(sessionManager) }
    val viewModel: StoreSetupViewModel = viewModel(
        factory = StoreSetupViewModelFactory(repository, sessionManager)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitState) {
        if (uiState.submitState is UiState.Success) {
            onSetupComplete()
        }
    }

    val scrollState = rememberScrollState()

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDistrictDropdown by remember { mutableStateOf(false) }
    var showOpenTimePicker by remember { mutableStateOf(false) }
    var showCloseTimePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateLogo(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lengkapi Toko Anda", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Section 1: Identitas Toko
            SectionTitle("Identitas Toko")

            // Logo Upload
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val logoUri = uiState.formState.logoUri
                if (logoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(logoUri),
                        contentDescription = "Logo Toko",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, PrimaryBlue, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                            .border(2.dp, Color.Gray, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Tambah Logo", tint = Color.Gray)
                            Text("Logo / Foto Toko", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Store Name
            OutlinedTextField(
                value = uiState.formState.storeName,
                onValueChange = { viewModel.updateStoreName(it) },
                label = { Text("Nama Toko *") },
                placeholder = { Text("Masukkan nama toko") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kategori Usaha Dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.categories.find { it.idCategory == uiState.formState.selectedCategoryId }?.nameCategory ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori Usaha") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    uiState.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.nameCategory) },
                            onClick = {
                                viewModel.updateCategory(category.idCategory)
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description with char counter
            OutlinedTextField(
                value = uiState.formState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Deskripsi Singkat") },
                placeholder = { Text("Ceritakan tentang toko Anda...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(
                        "${uiState.formState.description.length}/200",
                        color = if (uiState.formState.description.length >= 200) Color.Red else TextSecondary
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Lokasi & Alamat
            SectionTitle("Lokasi & Alamat")

            // Kecamatan/Kelurahan Dropdown
            ExposedDropdownMenuBox(
                expanded = showDistrictDropdown,
                onExpandedChange = { showDistrictDropdown = it }
            ) {
                OutlinedTextField(
                    value = uiState.formState.selectedDistrict,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kecamatan / Kelurahan") },
                    placeholder = { Text("Pilih kelurahan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDistrictDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = showDistrictDropdown,
                    onDismissRequest = { showDistrictDropdown = false }
                ) {
                    uiState.districts.forEach { district ->
                        DropdownMenuItem(
                            text = { Text(district) },
                            onClick = {
                                viewModel.updateDistrict(district)
                                showDistrictDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Full Address
            OutlinedTextField(
                value = uiState.formState.address,
                onValueChange = { viewModel.updateAddress(it) },
                label = { Text("Alamat Lengkap") },
                placeholder = { Text("Nama jalan, nomor, RT/RW, catatan") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Jam Operasional
            SectionTitle("Jam Operasional")

            // Buka setiap hari toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Buka setiap hari", fontSize = 14.sp)
                Switch(
                    checked = uiState.formState.openEveryDay,
                    onCheckedChange = { viewModel.toggleOpenEveryDay() },
                    colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue)
                )
            }

            if (!uiState.formState.openEveryDay) {
                Spacer(modifier = Modifier.height(8.dp))
                // Day selection chips
                Text("Pilih hari operasional:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(StoreSetupViewModel.DAYS) { day ->
                        val isSelected = day in uiState.formState.selectedDays
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.toggleDay(day) },
                            label = { Text(day, fontSize = 12.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.formState.openTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jam Buka") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showOpenTimePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledContainerColor = Color.Transparent
                    )
                )
                OutlinedTextField(
                    value = uiState.formState.closeTime,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jam Tutup") },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showCloseTimePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }

            // Operating hours preview
            if (uiState.formState.selectedDays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F1FB))
                ) {
                    Text(
                        text = viewModel.getOperatingHoursText(),
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = PrimaryBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = { viewModel.submit(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState.submitState !is UiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (uiState.submitState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Simpan & Lanjutkan \u2192",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.submitState is UiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState.submitState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Time Pickers
    if (showOpenTimePicker) {
        TimePickerDialog(
            onConfirm = { hour, minute ->
                viewModel.updateOpenTime(String.format("%02d:%02d", hour, minute))
                showOpenTimePicker = false
            },
            onDismiss = { showOpenTimePicker = false }
        )
    }
    if (showCloseTimePicker) {
        TimePickerDialog(
            onConfirm = { hour, minute ->
                viewModel.updateCloseTime(String.format("%02d:%02d", hour, minute))
                showCloseTimePicker = false
            },
            onDismiss = { showCloseTimePicker = false }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = PrimaryBlue,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Waktu", fontWeight = FontWeight.Bold) },
        text = { TimePicker(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("Pilih", color = PrimaryBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
