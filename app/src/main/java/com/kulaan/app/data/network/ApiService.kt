package com.kulaan.app.data.network

import com.kulaan.app.data.model.AuthResponse
import com.kulaan.app.data.model.CategoryResponse
import com.kulaan.app.data.model.LoginRequest
import com.kulaan.app.data.model.ProductResponse
import com.kulaan.app.data.model.RegisterRequest
import com.kulaan.app.data.model.SellerProductResponse
import com.kulaan.app.data.model.StoreResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Headers("Accept: application/json")
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @Headers("Accept: application/json")
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @Headers("Accept: application/json")
    @GET("products")
    suspend fun getProducts(
        @Query("search") search: String? = null,
        @Query("category") category: Int? = null,
        @Query("page") page: Int = 1
    ): Response<ProductResponse>

    @Headers("Accept: application/json")
    @GET("categories")
    suspend fun getCategories(): Response<CategoryResponse>

    @Multipart
    @Headers("Accept: application/json")
    @POST("store/setup")
    suspend fun createStore(
        @Part("store_name") storeName: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("district") district: RequestBody?,
        @Part("operating_hours") operatingHours: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part logo: MultipartBody.Part?
    ): Response<StoreResponse>

    @Headers("Accept: application/json")
    @GET("seller/store")
    suspend fun getStore(): Response<StoreResponse>

    @Multipart
    @Headers("Accept: application/json")
    @POST("seller/store")
    suspend fun updateStore(
        @Part("store_name") storeName: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("district") district: RequestBody?,
        @Part("operating_hours") operatingHours: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part storeLogo: MultipartBody.Part?
    ): Response<StoreResponse>

    @Headers("Accept: application/json")
    @GET("seller/products")
    suspend fun getSellerProducts(
        @Query("page") page: Int = 1
    ): Response<SellerProductResponse>

    @Multipart
    @Headers("Accept: application/json")
    @POST("seller/products")
    suspend fun createProduct(
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("unit") unit: RequestBody?,
        @Part("stock") stock: RequestBody,
        @Part("min_order") minOrder: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("id_category") idCategory: RequestBody,
        @Part("category_name") categoryName: RequestBody,
        @Part productImage: MultipartBody.Part?
    ): Response<StoreResponse>
}
