package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class StoreListResponse(
    val success: Boolean,
    val data: List<Store>,
    val meta: PaginationMeta?
)

data class StoreDetailResponse(
    val success: Boolean,
    val data: Store
)
