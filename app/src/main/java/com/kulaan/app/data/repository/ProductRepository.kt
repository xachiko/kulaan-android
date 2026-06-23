package com.kulaan.app.data.repository

import com.kulaan.app.data.model.CategoryResponse
import com.kulaan.app.data.model.ProductResponse
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager
import retrofit2.Response

class ProductRepository(private val sessionManager: SessionManager) {
    private val api = ApiClient.getInstance(sessionManager)

    suspend fun getProducts(search: String?, category: Int?, page: Int): Response<ProductResponse> {
        return api.getProducts(keyword = search, category = category, page = page)
    }

    suspend fun getCategories(): Response<List<com.kulaan.app.data.model.Category>> {
        return api.getCategories()
    }

    suspend fun getProductDetail(id: Int): Response<com.kulaan.app.data.model.ProductDetailResponse> {
        return api.getProductDetail(id)
    }

    suspend fun getPopularProducts(): Response<com.kulaan.app.data.model.PopularProductsResponse> {
        return api.getPopularProducts()
    }
}
