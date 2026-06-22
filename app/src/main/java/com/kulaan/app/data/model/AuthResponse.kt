package com.kulaan.app.data.model

data class AuthResponse(
    val success: Boolean,
    val data: UserData?,
    val message: String?,
    val errors: Map<String, List<String>>?
)

data class UserData(
    val user: User,
    val token: String,
    val notice: String? = null
)
