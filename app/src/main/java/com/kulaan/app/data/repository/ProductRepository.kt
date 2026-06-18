package com.kulaan.app.data.repository

import com.kulaan.app.data.model.CategoryResponse
import com.kulaan.app.data.model.ProductResponse
import com.kulaan.app.data.network.RetrofitInstance
import retrofit2.Response

class ProductRepository {
    private val api = RetrofitInstance.api

    suspend fun getProducts(search: String?, category: Int?, page: Int): Response<ProductResponse> {
        return api.getProducts(search = search, category = category, page = page)
    }

    suspend fun getCategories(): Response<CategoryResponse> {
        return api.getCategories()
    }
}
