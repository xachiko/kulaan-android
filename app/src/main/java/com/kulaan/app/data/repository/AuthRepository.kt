package com.kulaan.app.data.repository

import com.kulaan.app.data.model.AuthResponse
import com.kulaan.app.data.model.LoginRequest
import com.kulaan.app.data.model.RegisterRequest
import com.kulaan.app.data.network.RetrofitInstance
import retrofit2.Response

class AuthRepository {
    private val api = RetrofitInstance.api

    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return api.login(request)
    }
}
