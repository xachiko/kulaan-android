package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val email: String,
    @SerializedName("phone_number")
    val phoneNumber: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String,
    val role: String
)

data class LoginRequest(
    val email: String,
    val password: String
)
