package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

// Generic response wrapper
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

// Data setelah login/register berhasil
data class AuthData(
    @SerializedName("user") val user: User,
    @SerializedName("token") val token: String,
    @SerializedName("notice") val notice: String?
)

// Request body: Login
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// Request body: Register
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("password_confirmation") val passwordConfirmation: String,
    @SerializedName("role") val role: String  // "buyer" atau "seller"
)

// Request body: Setup toko seller
data class StoreSetupRequest(
    @SerializedName("store_name") val storeName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("address") val address: String,
    @SerializedName("district") val district: String,
    @SerializedName("operating_hours") val operatingHours: String?
)
