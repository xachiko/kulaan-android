package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    val success: Boolean,
    val data: List<Product>,
    val meta: PaginationMeta?
)

data class PaginationMeta(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    val total: Int
)

data class PopularProductsResponse(
    val success: Boolean,
    val data: List<Product>
)
