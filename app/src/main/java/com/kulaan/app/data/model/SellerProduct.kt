package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class SellerProduct(
    @SerializedName("id_product") val idProduct: Int,
    val name: String,
    val price: Double,
    val unit: String?,
    val stock: Int,
    @SerializedName("image_url") val imageUrl: String?,
    val status: String?,
    val store: StoreShort?,
    val category: CategoryShort?
)

data class SellerProductResponse(
    val success: Boolean,
    val data: List<SellerProduct>,
    val meta: PaginationMeta?
)
