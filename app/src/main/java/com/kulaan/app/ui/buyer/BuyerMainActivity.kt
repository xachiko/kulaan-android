package com.kulaan.app.ui.buyer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kulaan.app.ui.theme.KulaanTheme

class BuyerMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KulaanTheme {
                BuyerBottomNavigation()
            }
        }
    }
}
