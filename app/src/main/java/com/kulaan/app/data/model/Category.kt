package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id_category") val idCategory: Int,
    @SerializedName("name_category") val nameCategory: String,
    val description: String?
)

data class CategoryResponse(
    val success: Boolean,
    val data: List<Category>
)
