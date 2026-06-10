package com.kulaan.app.data.network

import com.kulaan.app.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

// Otomatis tambahkan "Authorization: Bearer <token>" ke setiap request
class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.getToken()
        val request = chain.request().newBuilder().apply {
            addHeader("Accept", "application/json")
            addHeader("Content-Type", "application/json")
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
