package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class SellerDashboardResponse(
    val success: Boolean,
    val data: SellerDashboardData
)

data class SellerDashboardData(
    val store: SellerStoreInfo,
    val metrics: SellerMetrics,
    @SerializedName("recent_orders") val recentOrders: List<RecentOrder>
)

data class SellerStoreInfo(
    @SerializedName("store_name") val storeName: String,
    @SerializedName("verification_status") val verificationStatus: String
)

data class SellerMetrics(
    @SerializedName("total_orders") val totalOrders: Int,
    @SerializedName("monthly_revenue") val monthlyRevenue: Double,
    @SerializedName("active_products") val activeProducts: Int,
    @SerializedName("low_stock_count") val lowStockCount: Int,
    @SerializedName("new_orders") val newOrders: Int,
    @SerializedName("processed_orders") val processedOrders: Int
)

data class RecentOrder(
    @SerializedName("id_order") val idOrder: Int,
    @SerializedName("buyer_name") val buyerName: String?,
    @SerializedName("buyer_phone") val buyerPhone: String?,
    val products: List<RecentOrderProduct>,
    @SerializedName("total_order") val totalOrder: Double,
    val status: String, // 'menunggu', 'diproses', 'selesai', 'dibatalkan'
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("shipping_address") val shippingAddress: String,
    @SerializedName("order_date") val orderDate: String?
)

data class RecentOrderProduct(
    val name: String?,
    val quantity: Int,
    val unit: String?
)
