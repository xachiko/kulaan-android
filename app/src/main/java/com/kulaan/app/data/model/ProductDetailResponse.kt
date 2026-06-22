package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class ProductDetailResponse(
    val success: Boolean,
    val data: ProductDetail
)

data class ProductDetail(
    @SerializedName("id_product") val idProduct: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val unit: String?,
    @SerializedName("min_order") val minOrder: Int,
    val rating: Double,
    @SerializedName("review_count") val reviewCount: Int,
    @SerializedName("image_url") val imageUrl: String?,
    val category: CategoryShort?,
    val store: StoreDetailed?,
    val reviews: List<Review>
)

data class StoreDetailed(
    @SerializedName("id_store") val idStore: Int,
    @SerializedName("store_name") val storeName: String,
    val logo: String?,
    val description: String?,
    val address: String?,
    val district: String?,
    @SerializedName("operating_hours") val operatingHours: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("payment_accounts") val paymentAccounts: List<PaymentAccount>
)

data class PaymentAccount(
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("account_number") val accountNumber: String,
    @SerializedName("account_name") val accountName: String,
    @SerializedName("qris_code") val qrisCode: String?
)

data class Review(
    val rating: Int,
    val comment: String?,
    val user: ReviewUser?,
    @SerializedName("created_at") val createdAt: String?
)

data class ReviewUser(
    val name: String
)
