package com.kulaan.app.data.repository

import com.kulaan.app.data.model.OrderDetailResponse
import com.kulaan.app.data.model.OrderListResponse
import com.kulaan.app.data.model.StoreOrderRequest
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager
import retrofit2.Response

class OrderRepository(private val sessionManager: SessionManager) {
    private val api = ApiClient.getInstance(sessionManager)

    suspend fun getOrders(): Response<OrderListResponse> {
        return api.getOrders()
    }

    suspend fun getOrderDetail(id: Int): Response<OrderDetailResponse> {
        return api.getOrderDetail(id)
    }

    suspend fun createOrder(request: StoreOrderRequest): Response<OrderDetailResponse> {
        return api.createOrder(request)
    }
}
