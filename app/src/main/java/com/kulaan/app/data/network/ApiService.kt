package com.kulaan.app.data.network

import com.kulaan.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ──────────────────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Any>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<User>>

    // ── STORE SETUP (Seller) ───────────────────────────────────────────────
    @POST("store/setup")
    suspend fun setupStore(
        @Body request: StoreSetupRequest
    ): Response<ApiResponse<Store>>
}
