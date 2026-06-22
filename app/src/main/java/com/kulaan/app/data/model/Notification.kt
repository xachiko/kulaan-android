package com.kulaan.app.data.model

import com.google.gson.annotations.SerializedName

data class NotificationListResponse(
    val success: Boolean,
    val data: List<Notification>
)

data class NotificationActionResponse(
    val success: Boolean,
    val message: String?
)

data class Notification(
    @SerializedName("id_notification") val idNotification: Int,
    @SerializedName("id_user") val idUser: Int,
    @SerializedName("id_order") val idOrder: Int,
    val message: String,
    @SerializedName("is_read") val isRead: Int, // 0 = unread, 1 = read
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)
