package com.kulaan.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kulaan.app.R
import com.kulaan.app.ui.buyer.BuyerMainActivity
import com.kulaan.app.ui.seller.SellerMainActivity
import com.kulaan.app.utils.SessionManager

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        val session = SessionManager(this)

        // Auto-redirect jika sudah punya sesi aktif
        if (session.isLoggedIn()) {
            val intent = if (session.isSeller())
                Intent(this, SellerMainActivity::class.java)
            else
                Intent(this, BuyerMainActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Kalau belum login → NavHostFragment otomatis tampilkan WelcomeFragment
    }
}
