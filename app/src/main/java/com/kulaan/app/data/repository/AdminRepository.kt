package com.kulaan.app.data.repository

import com.kulaan.app.data.model.AdminDashboardResponse
import com.kulaan.app.data.model.NotificationActionResponse
import com.kulaan.app.data.model.VerifyStoreRequest
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager
import retrofit2.Response

class AdminRepository(private val sessionManager: SessionManager) {
    private val api = ApiClient.getInstance(sessionManager)

    suspend fun getAdminDashboard(): Response<AdminDashboardResponse> {
        return api.getAdminDashboard()
    }

    suspend fun verifyStore(idStore: Int, status: String): Response<NotificationActionResponse> {
        return api.verifyStore(idStore, VerifyStoreRequest(status))
    }
}
