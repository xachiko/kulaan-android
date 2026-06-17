package com.kulaan.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kulaan.app.ui.auth.components.AuthTextField
import com.kulaan.app.ui.auth.components.RoleSelectionSection
import com.kulaan.app.ui.theme.PrimaryBlue
import com.kulaan.app.ui.theme.TextSecondary

@Composable
fun RegisterContent(
    state: AuthState,
    onRoleSelected: (Role) -> Unit,
    onRegisterClick: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.padding(24.dp)) {
        RoleSelectionSection(
            selectedRole = state.selectedRole,
            onRoleSelected = onRoleSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nama Lengkap *",
            placeholder = "Masukkan nama lengkap",
            errorMessage = state.fieldErrors["name"],
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email *",
            placeholder = "email@contoh.com",
            errorMessage = state.fieldErrors["email"],
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "No. WhatsApp/ Telepon *",
            placeholder = "08xxxxxxxxxx",
            errorMessage = state.fieldErrors["phone_number"],
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password *",
            placeholder = "Minimal 8 karakter",
            isPassword = true,
            errorMessage = state.fieldErrors["password"],
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (!state.isLoading) onRegisterClick(name, email, phone, password)
                }
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (!state.isLoading) onRegisterClick(name, email, phone, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Buat Akun", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (state.selectedRole == Role.SELLER) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pemilik UMKM → Menunggu verifikasi admin",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
