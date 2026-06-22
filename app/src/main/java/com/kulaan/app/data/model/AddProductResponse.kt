package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class AddProductResponse(
    val success: Boolean,
    val message: String?,
    val data: AddedProduct?
)

data class AddedProduct(
    @SerializedName("id_product") val idProduct: Int?,
    val name: String?,
    val price: Double?,
    val unit: String?,
    val stock: Int?,
    @SerializedName("min_order") val minOrder: Int?,
    val description: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val status: String?,
    val store: StoreShort?,
    val category: CategoryShort?
)
