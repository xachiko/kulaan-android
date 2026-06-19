package com.kulaan.app.data.repository

import android.content.Context
import android.net.Uri
import com.kulaan.app.data.model.CategoryResponse
import com.kulaan.app.data.model.SellerProductResponse
import com.kulaan.app.data.model.StoreResponse
import com.kulaan.app.data.network.ApiClient
import com.kulaan.app.utils.SessionManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class StoreRepository(private val sessionManager: SessionManager) {

    private val api = ApiClient.getInstance(sessionManager)

    suspend fun createStore(
        storeName: String,
        description: String?,
        address: String?,
        district: String?,
        operatingHours: String?,
        category: String?,
        logoUri: Uri?,
        context: Context
    ): Result<StoreResponse> {
        return try {
            val namePart = storeName.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val addrPart = address?.toRequestBody("text/plain".toMediaTypeOrNull())
            val distPart = district?.toRequestBody("text/plain".toMediaTypeOrNull())
            val ohPart = operatingHours?.toRequestBody("text/plain".toMediaTypeOrNull())
            val catPart = category?.toRequestBody("text/plain".toMediaTypeOrNull())

            var logoPart: MultipartBody.Part? = null
            if (logoUri != null) {
                val inputStream = context.contentResolver.openInputStream(logoUri)
                val byteArray = inputStream?.use { it.readBytes() }
                if (byteArray != null) {
                    val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                    logoPart = MultipartBody.Part.createFormData("logo", "logo.jpg", requestBody)
                }
            }

            val response = api.createStore(namePart, descPart, addrPart, distPart, ohPart, catPart, logoPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                var msg = "Gagal membuat toko (HTTP ${response.code()})"
                try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            msg = json.optString("message", json.optString("errors", errorBody))
                        } catch (e: Exception) {
                            msg = "Error ${response.code()}: ${errorBody.take(150)}"
                        }
                    }
                } catch (e: Exception) {}
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStoreProfile(): Result<StoreResponse> {
        return try {
            val response = api.getStore()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat profil toko"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStore(
        storeName: String?,
        description: String?,
        address: String?,
        district: String?,
        operatingHours: String?,
        category: String?,
        logoUri: Uri?,
        context: Context
    ): Result<StoreResponse> {
        return try {
            val namePart = storeName?.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val addrPart = address?.toRequestBody("text/plain".toMediaTypeOrNull())
            val distPart = district?.toRequestBody("text/plain".toMediaTypeOrNull())
            val ohPart = operatingHours?.toRequestBody("text/plain".toMediaTypeOrNull())
            val catPart = category?.toRequestBody("text/plain".toMediaTypeOrNull())

            var logoPart: MultipartBody.Part? = null
            if (logoUri != null) {
                val inputStream = context.contentResolver.openInputStream(logoUri)
                val byteArray = inputStream?.use { it.readBytes() }
                if (byteArray != null) {
                    val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                    logoPart = MultipartBody.Part.createFormData("logo", "logo.jpg", requestBody)
                }
            }

            val response = api.updateStore(namePart, descPart, addrPart, distPart, ohPart, catPart, logoPart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                var msg = "Gagal memperbarui toko (HTTP ${response.code()})"
                try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            msg = json.optString("message", json.optString("errors", errorBody))
                        } catch (e: Exception) {
                            msg = "Error ${response.code()}: ${errorBody.take(150)}"
                        }
                    }
                } catch (e: Exception) {}
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSellerProducts(page: Int = 1): Result<SellerProductResponse> {
        return try {
            val response = api.getSellerProducts(page)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat produk"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(
        name: String,
        price: Double,
        unit: String?,
        stock: Int,
        minOrder: Int,
        description: String?,
        idCategory: Int,
        imageUri: Uri?,
        context: Context
    ): Result<StoreResponse> {
        return try {
            val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val unitPart = unit?.toRequestBody("text/plain".toMediaTypeOrNull())
            val stockPart = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val minOrderPart = minOrder.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            val catPart = idCategory.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            var imagePart: MultipartBody.Part? = null
            if (imageUri != null) {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val byteArray = inputStream?.use { it.readBytes() }
                if (byteArray != null) {
                    val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("product_image", "product.jpg", requestBody)
                }
            }

            val response = api.createProduct(namePart, pricePart, unitPart, stockPart, minOrderPart, descPart, catPart, imagePart)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                var msg = "Gagal menambahkan produk (HTTP ${response.code()})"
                try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        try {
                            val json = org.json.JSONObject(errorBody)
                            msg = json.optString("message", json.optString("errors", errorBody))
                        } catch (e: Exception) {
                            msg = "Error ${response.code()}: ${errorBody.take(150)}"
                        }
                    }
                } catch (e: Exception) {}
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<CategoryResponse> {
        return try {
            val response = api.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat kategori"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
