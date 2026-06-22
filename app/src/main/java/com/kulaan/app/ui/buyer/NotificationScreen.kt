package com.kulaan.app.ui.buyer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kulaan.app.data.model.Notification
import com.kulaan.app.data.repository.NotificationRepository
import com.kulaan.app.utils.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    sessionManager: SessionManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { NotificationRepository(sessionManager) }

    var isLoading by remember { mutableStateOf(true) }
    var notificationsList by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loadNotifications = suspend {
        isLoading = true
        errorMessage = null
        try {
            val response = repository.getNotifications()
            if (response.isSuccessful && response.body()?.success == true) {
                notificationsList = response.body()?.data ?: emptyList()
            } else {
                errorMessage = "Gagal memuat notifikasi."
            }
        } catch (e: Exception) {
            errorMessage = "Koneksi bermasalah."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadNotifications()
    }

    val unreadCount = remember(notificationsList) {
        notificationsList.count { it.isRead == 0 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Notifikasi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("$unreadCount belum dibaca", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val response = repository.markAllNotificationsAsRead()
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            notificationsList = notificationsList.map { it.copy(isRead = 1) }
                                            Toast.makeText(context, "Semua notifikasi dibaca", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Koneksi bermasalah.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text("Tandai Semua Dibaca", fontWeight = FontWeight.Bold, color = Color(0xFF185FA5))
                        }
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
                    Button(onClick = {
                        scope.launch { loadNotifications() }
                    }) {
                        Text("Coba Lagi")
                    }
                }
            } else if (notificationsList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🔔", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tidak ada notifikasi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF282724)
                    )
                    Text(
                        text = "Notifikasi tentang status pesanan Anda akan muncul di sini.",
                        fontSize = 12.sp,
                        color = Color(0xFF8A8980),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notificationsList) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                if (notification.isRead == 0) {
                                    scope.launch {
                                        try {
                                            val response = repository.markNotificationAsRead(notification.idNotification)
                                            if (response.isSuccessful && response.body()?.success == true) {
                                                notificationsList = notificationsList.map {
                                                    if (it.idNotification == notification.idNotification) it.copy(isRead = 1) else it
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Silent fail
                                        }
                                    }
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
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit
) {
    val isUnread = notification.isRead == 0
    val cardBackground = if (isUnread) Color(0xFFE8F1FB) else Color.White
    val cardBorder = if (isUnread) Color(0xFFC8DBED) else Color(0xFFE8E7E2)

    val (title, icon, iconBg) = remember(notification.message) {
        val msg = notification.message
        when {
            msg.contains("berhasil dibuat") -> Triple("Pesanan Berhasil Dibuat", "📦", Color(0xFFE6F1FB))
            msg.contains("diubah menjadi: Diproses") -> Triple("Pesanan Dikonfirmasi", "✅", Color(0xFFE6F4EA))
            msg.contains("diubah menjadi: Selesai") -> Triple("Pesanan Selesai!", "🎉", Color(0xFFE6F4EA))
            msg.contains("diubah menjadi: Dibatalkan") || msg.contains("ditolak") -> Triple("Pesanan Dibatalkan", "⚠️", Color(0xFFFCEBEB))
            msg.contains("Pesanan baru masuk") -> Triple("Pesanan Baru Masuk", "📥", Color(0xFFE6F4EA))
            else -> Triple("Notifikasi", "🔔", Color(0xFFF4F3F0))
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isUnread) Color(0xFF124880) else Color(0xFF282724)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color(0xFF5C5B54),
                    lineHeight = 16.sp
                )
            }

            if (isUnread) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF185FA5), CircleShape)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}
