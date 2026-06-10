package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("role") val role: String,       // "buyer" atau "seller"
    @SerializedName("store") val store: Store?
)
