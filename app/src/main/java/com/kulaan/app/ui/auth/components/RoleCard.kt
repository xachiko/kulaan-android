package com.kulaan.app.ui.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import com.kulaan.app.ui.auth.Role
import com.kulaan.app.ui.theme.BorderGray
import com.kulaan.app.ui.theme.LightBlueBg
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.ui.theme.TextPrimary
import com.kulaan.app.ui.theme.TextSecondary

@Composable
fun RoleSelectionSection(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit
) {
    Column {
        Text(
            text = "Pilih Peran",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            RoleCard(
                modifier = Modifier.weight(1f),
                isSelected = selectedRole == Role.BUYER,
                iconVector = Icons.Default.ShoppingCart,
                title = "Pembeli",
                subtitle = "Cari & Pesan",
                onClick = { onRoleSelected(Role.BUYER) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            RoleCard(
                modifier = Modifier.weight(1f),
                isSelected = selectedRole == Role.SELLER,
                iconVector = Icons.Default.Home,
                title = "Pemilik UMKM",
                subtitle = "Daftarkan toko",
                onClick = { onRoleSelected(Role.SELLER) }
            )
        }
    }
}

@Composable
fun RoleCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    iconVector: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryBlue else BorderGray
    val borderWidth = if (isSelected) 2.dp else 1.5.dp
    val bgColor = if (isSelected) LightBlueBg else Color.White

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = title,
            tint = if (isSelected) PrimaryBlue else TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
