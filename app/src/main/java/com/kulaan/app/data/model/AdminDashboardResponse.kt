package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class AdminDashboardResponse(
    val success: Boolean,
    val data: AdminDashboardData
)

data class AdminDashboardData(
    val metrics: AdminMetrics,
    val stores: List<Store>
)

data class AdminMetrics(
    @SerializedName("total_stores") val totalStores: Int,
    @SerializedName("active_stores") val activeStores: Int,
    @SerializedName("pending_stores") val pendingStores: Int
)

data class VerifyStoreRequest(
    val status: String // "disetujui" or "dibatalkan"
)
