package com.kulaan.app.data.repository

import com.kulaan.app.data.model.OrderDetailResponse
import com.kulaan.app.data.model.NotificationActionResponse
import com.kulaan.app.data.model.SellerDashboardResponse
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager
import retrofit2.Response

class SellerRepository(private val sessionManager: SessionManager) {
    private val api = ApiClient.getInstance(sessionManager)

    suspend fun getSellerDashboard(): Response<SellerDashboardResponse> {
        return api.getSellerDashboard()
    }

    suspend fun getSellerOrderDetail(id: Int): Response<OrderDetailResponse> {
        return api.getSellerOrderDetail(id)
    }

    suspend fun updateOrderStatus(id: Int, status: String): Response<NotificationActionResponse> {
        return api.updateOrderStatus(id, mapOf("status" to status))
    }
}
