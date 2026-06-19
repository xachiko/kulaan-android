package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class Store(
    @SerializedName("id_store") val idStore: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("address") val address: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("operating_hours") val operatingHours: String?,
    @SerializedName("store_logo") val storeLogo: String?,
    @SerializedName("status") val status: String,
    @SerializedName("id_category") val idCategory: Int? = null,
    @SerializedName("category_name") val categoryName: String? = null,
    val whatsapp: String? = null,
    val instagram: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
