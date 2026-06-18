package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id_product") val idProduct: Int,
    val name: String,
    val price: Double,
    val unit: String?,
    val stock: Int,
    val rating: Double,
    @SerializedName("review_count") val reviewCount: Int,
    @SerializedName("image_url") val imageUrl: String?,
    val store: StoreShort?,
    val category: CategoryShort?
)

data class StoreShort(
    @SerializedName("id_store") val idStore: Int,
    @SerializedName("store_name") val storeName: String,
    val district: String?,
    @SerializedName("operating_hours") val operatingHours: String?
)

data class CategoryShort(
    @SerializedName("name_category") val nameCategory: String
)
