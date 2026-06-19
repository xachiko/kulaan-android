package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class StoreSetupRequest(
    @SerializedName("store_name") val storeName: String,
    val description: String?,
    val address: String?,
    val district: String?,
    @SerializedName("operating_hours") val operatingHours: String?,
    @SerializedName("id_category") val idCategory: Int?
)

data class StoreResponse(
    val success: Boolean,
    val data: Store?,
    val message: String?
)
