package com.kulaan.app.data.network

import com.kulaan.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
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
        @Query("keyword") keyword: String? = null,
        @Query("category") category: Int? = null,
        @Query("page") page: Int = 1
    ): Response<ProductResponse>

    @Headers("Accept: application/json")
    @GET("categories")
    suspend fun getCategories(): Response<List<Category>>

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
    ): Response<AddProductResponse>

    @Headers("Accept: application/json")
    @DELETE("seller/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<StoreResponse>

    @Multipart
    @Headers("Accept: application/json")
    @POST("seller/products/{id}?_method=PUT")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("unit") unit: RequestBody?,
        @Part("stock") stock: RequestBody,
        @Part("min_order") minOrder: RequestBody,
        @Part("description") description: RequestBody?,
        @Part("id_category") idCategory: RequestBody,
        @Part("category_name") categoryName: RequestBody,
        @Part productImage: MultipartBody.Part?
    ): Response<AddProductResponse>

    // ── Public / Buyer Products & Stores ───────────────────────────────
    @Headers("Accept: application/json")
    @GET("products/popular")
    suspend fun getPopularProducts(): Response<PopularProductsResponse>

    @Headers("Accept: application/json")
    @GET("products/{id}")
    suspend fun getProductDetail(
        @Path("id") id: Int
    ): Response<ProductDetailResponse>

    @Headers("Accept: application/json")
    @GET("stores")
    suspend fun getStores(
        @Query("keyword") keyword: String? = null,
        @Query("category") category: String? = null
    ): Response<StoreListResponse>

    @Headers("Accept: application/json")
    @GET("stores/{id}")
    suspend fun getStoreDetailPublic(
        @Path("id") id: Int
    ): Response<StoreDetailResponse>

    // ── Buyer Orders ─────────────────────────────────────────────────────
    @Headers("Accept: application/json")
    @GET("orders")
    suspend fun getOrders(): Response<OrderListResponse>

    @Headers("Accept: application/json")
    @GET("orders/{id}")
    suspend fun getOrderDetail(
        @Path("id") id: Int
    ): Response<OrderDetailResponse>

    @Headers("Accept: application/json")
    @POST("orders")
    suspend fun createOrder(
        @Body request: StoreOrderRequest
    ): Response<OrderDetailResponse>

    // ── Notifications ───────────────────────────────────────────────────
    @Headers("Accept: application/json")
    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationListResponse>

    @Headers("Accept: application/json")
    @POST("notifications/mark-read")
    suspend fun markAllNotificationsAsRead(): Response<NotificationActionResponse>

    @Headers("Accept: application/json")
    @POST("notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") id: Int
    ): Response<NotificationActionResponse>

    // ── Seller Operations ───────────────────────────────────────────────
    @Headers("Accept: application/json")
    @GET("seller/dashboard")
    suspend fun getSellerDashboard(): Response<SellerDashboardResponse>

    @Headers("Accept: application/json")
    @GET("seller/orders/{id}")
    suspend fun getSellerOrderDetail(
        @Path("id") id: Int
    ): Response<OrderDetailResponse>

    @Headers("Accept: application/json")
    @PUT("seller/orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<NotificationActionResponse>

    // ── Auth Profile & Session ──────────────────────────────────────────
    @Headers("Accept: application/json")
    @GET("auth/me")
    suspend fun getMe(): Response<UserMeResponse>

    @Headers("Accept: application/json")
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Body body: Map<String, String?>
    ): Response<UserMeResponse>

    @Headers("Accept: application/json")
    @POST("auth/logout")
    suspend fun logout(): Response<NotificationActionResponse>

    // ── Admin Endpoints ─────────────────────────────────────────────────
    @Headers("Accept: application/json")
    @GET("admin/dashboard")
    suspend fun getAdminDashboard(): Response<AdminDashboardResponse>

    @Headers("Accept: application/json")
    @POST("admin/stores/{id}/verify")
    suspend fun verifyStore(
        @Path("id") idStore: Int,
        @Body request: VerifyStoreRequest
    ): Response<NotificationActionResponse>
}

