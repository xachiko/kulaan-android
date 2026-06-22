package com.kulaan.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kulaan.app.ui.theme.Background
import com.kulaan.app.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onNavigateToBuyer: () -> Unit,
    onNavigateToSeller: () -> Unit,
    onShowRolePicker: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.authResult) {
        when (val result = state.authResult) {
            is AuthResult.Success -> {
                result.notice?.let {
                    scope.launch { snackbarHostState.showSnackbar(it) }
                }
                val roles = result.user.roles
                if (roles.contains("buyer") && roles.contains("seller")) {
                    onShowRolePicker()
                } else if (roles.contains("seller")) {
                    onNavigateToSeller()
                } else {
                    onNavigateToBuyer()
                }
            }
            is AuthResult.Error -> {
                scope.launch { snackbarHostState.showSnackbar(result.message) }
                viewModel.clearErrors()
            }
            null -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = PrimaryBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Blue header box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "KULAAN.id",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Katalog UMKM Lokal Jebres",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // 2. Tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                TabItem(
                    title = "Masuk",
                    isSelected = state.selectedTab == AuthTab.LOGIN,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onTabSwitch(AuthTab.LOGIN) }
                )
                TabItem(
                    title = "Daftar",
                    isSelected = state.selectedTab == AuthTab.REGISTER,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onTabSwitch(AuthTab.REGISTER) }
                )
            }

            // 3. White card content area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (state.selectedTab == AuthTab.LOGIN) {
                        LoginContent(
                            state = state,
                            onRoleSelected = { viewModel.onRoleSelected(it) },
                            onLoginClick = { email, pass -> viewModel.login(email, pass) }
                        )
                        SwitchPrompt(
                            text = "Belum punya akun? ",
                            clickableText = "Daftar sekarang",
                            onClick = { viewModel.onTabSwitch(AuthTab.REGISTER) }
                        )
                    } else {
                        RegisterContent(
                            state = state,
                            onRoleSelected = { viewModel.onRoleSelected(it) },
                            onRegisterClick = { name, email, phone, pass ->
                                viewModel.register(name, email, phone, pass)
                            }
                        )
                        SwitchPrompt(
                            text = "Sudah punya akun? ",
                            clickableText = "Masuk di sini",
                            onClick = { viewModel.onTabSwitch(AuthTab.LOGIN) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color.White else PrimaryBlue.copy(alpha = 0.4f)
    val textColor = if (isSelected) PrimaryBlue else Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SwitchPrompt(
    text: String,
    clickableText: String,
    onClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(text)
        withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
            append(clickableText)
        }
    }

    Text(
        text = annotatedString,
        fontSize = 13.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .clickable { onClick() }
    )
}
