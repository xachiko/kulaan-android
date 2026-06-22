package com.kulaan.app.ui.buyer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kulaan.app.ui.theme.KulaanTheme
import com.kulaan.app.utils.SessionManager

class BuyerMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionManager = SessionManager(this)
        setContent {
            KulaanTheme {
                BuyerBottomNavigation(sessionManager = sessionManager)
            }
        }
    }
}
