package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

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

data class UserMeResponse(
    val success: Boolean,
    val message: String? = null,
    val data: UserMeDetail
)

data class UserMeDetail(
    val id_user: Int,
    val name: String,
    val email: String,
    val roles: List<String>,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val address: String? = null,
    val store: Store? = null
)
