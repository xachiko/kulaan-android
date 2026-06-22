package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class OrderListResponse(
    val success: Boolean,
    val data: List<Order>
)

data class OrderDetailResponse(
    val success: Boolean,
    val data: Order
)

data class Order(
    @SerializedName("id_order") val idOrder: Int,
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("total_order") val totalOrder: Double,
    val status: String, // 'menunggu', 'diproses', 'selesai', 'dibatalkan'
    @SerializedName("payment_method") val paymentMethod: String, // 'cod', 'transfer'
    @SerializedName("shipping_address") val shippingAddress: String,
    val note: String?,
    @SerializedName("order_date") val orderDate: String?,
    val items: List<OrderItem>
)

data class OrderItem(
    @SerializedName("id_order_detail") val idOrderDetail: Int,
    val quantity: Int,
    @SerializedName("price_at_purchase") val priceAtPurchase: Double,
    val product: OrderItemProduct?
)

data class OrderItemProduct(
    @SerializedName("id_product") val idProduct: Int,
    val name: String,
    val unit: String?,
    @SerializedName("image_url") val imageUrl: String?,
    val store: OrderItemStore?
)

data class OrderItemStore(
    @SerializedName("id_store") val idStore: Int,
    @SerializedName("store_name") val storeName: String,
    @SerializedName("phone_number") val phoneNumber: String?
)

data class StoreOrderRequest(
    @SerializedName("id_product") val idProduct: Int,
    val quantity: Int,
    val name: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("shipping_address") val shippingAddress: String,
    @SerializedName("payment_method") val paymentMethod: String, // 'cod', 'transfer'
    val note: String? = null
)
