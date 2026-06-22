package com.kulaan.app.data.repository

import com.kulaan.app.data.model.NotificationActionResponse
import com.kulaan.app.data.model.NotificationListResponse
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager
import retrofit2.Response

class NotificationRepository(private val sessionManager: SessionManager) {
    private val api = ApiClient.getInstance(sessionManager)

    suspend fun getNotifications(): Response<NotificationListResponse> {
        return api.getNotifications()
    }

    suspend fun markAllNotificationsAsRead(): Response<NotificationActionResponse> {
        return api.markAllNotificationsAsRead()
    }

    suspend fun markNotificationAsRead(id: Int): Response<NotificationActionResponse> {
        return api.markNotificationAsRead(id)
    }
}
