package com.kulaan.app.data.repository

import com.kulaan.app.data.model.AuthResponse
import com.kulaan.app.data.model.LoginRequest
import com.kulaan.app.data.model.RegisterRequest
import com.kulaan.app.data.network.RetrofitInstance
import retrofit2.Response

import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager

class AuthRepository(private val sessionManager: SessionManager) {
    private val api = RetrofitInstance.api
    private val authApi get() = ApiClient.getInstance(sessionManager)

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }

    suspend fun getMe(): Response<com.kulaan.app.data.model.UserMeResponse> {
        return authApi.getMe()
    }

    suspend fun updateProfile(name: String, email: String, phone: String, address: String?): Response<com.kulaan.app.data.model.UserMeResponse> {
        val body = mapOf(
            "name" to name,
            "email" to email,
            "phone_number" to phone,
            "address" to address
        )
        return authApi.updateProfile(body)
    }

    suspend fun logout(): Response<com.kulaan.app.data.model.NotificationActionResponse> {
        return authApi.logout()
    }
}
