package com.kulaan.app.data.repository

import com.kulaan.app.data.model.*
import com.kulaan.app.data.network.ApiService
import com.kulaan.app.utils.Resource

class AuthRepository(private val api: ApiService) {

    suspend fun login(email: String, password: String): Resource<AuthData> {
        return try {
            val res = api.login(LoginRequest(email, password))
            if (res.isSuccessful && res.body()?.success == true) {
                Resource.Success(res.body()!!.data!!)
            } else {
                Resource.Error(res.body()?.message ?: "Email atau password salah")
            }
        } catch (e: Exception) {
            Resource.Error("Tidak dapat terhubung ke server")
        }
    }

    suspend fun registerBuyer(
        name: String, email: String, password: String
    ): Resource<AuthData> {
        return register(name, email, password, role = "buyer")
    }

    suspend fun registerSeller(
        name: String, email: String, password: String
    ): Resource<AuthData> {
        return register(name, email, password, role = "seller")
    }

    private suspend fun register(
        name: String, email: String, password: String, role: String
    ): Resource<AuthData> {
        return try {
            val req = RegisterRequest(name, email, password, password, role)
            val res = api.register(req)
            if (res.isSuccessful && res.body()?.success == true) {
                Resource.Success(res.body()!!.data!!)
            } else {
                Resource.Error(res.body()?.message ?: "Registrasi gagal")
            }
        } catch (e: Exception) {
            Resource.Error("Tidak dapat terhubung ke server")
        }
    }

    suspend fun logout(): Resource<Unit> {
        return try {
            api.logout()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Success(Unit) // Tetap logout walau network error
        }
    }
}
