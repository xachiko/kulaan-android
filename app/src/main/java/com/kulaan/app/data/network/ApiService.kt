package com.kulaan.app.data.network


import com.kulaan.app.data.model.AuthResponse
import com.kulaan.app.data.model.LoginRequest
import com.kulaan.app.data.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Accept: application/json")
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @Headers("Accept: application/json")
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
