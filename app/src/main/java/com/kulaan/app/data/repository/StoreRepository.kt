package com.kulaan.app.data.repository

import com.kulaan.app.data.model.*
import com.kulaan.app.data.network.ApiService
import com.kulaan.app.utils.Resource

class StoreRepository(private val api: ApiService) {

    suspend fun setupStore(
        storeName: String,
        description: String?,
        address: String,
        district: String,
        operatingHours: String?
    ): Resource<Store> {
        return try {
            val req = StoreSetupRequest(storeName, description, address, district, operatingHours)
            val res = api.setupStore(req)
            if (res.isSuccessful && res.body()?.success == true) {
                Resource.Success(res.body()!!.data!!)
            } else {
                Resource.Error(res.body()?.message ?: "Gagal membuat toko")
            }
        } catch (e: Exception) {
            Resource.Error("Tidak dapat terhubung ke server")
        }
    }
}
