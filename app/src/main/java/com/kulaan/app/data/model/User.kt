package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id_user: Int,
    val name: String,
    val email: String,
    val roles: List<String>,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val address: String? = null
)
