package com.kulaan.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kulaan.app.data.model.AuthData
import com.kulaan.app.data.model.Store
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _authState = MutableLiveData<Resource<AuthData>>()
    val authState: LiveData<Resource<AuthData>> = _authState

    private val _storeState = MutableLiveData<Resource<Store>>()
    val storeState: LiveData<Resource<Store>> = _storeState

    fun login(email: String, password: String) {
        if (email.isBlank()) { _authState.value = Resource.Error("Email wajib diisi"); return }
        if (password.isBlank()) { _authState.value = Resource.Error("Password wajib diisi"); return }

        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.login(email, password)
        }
    }

    fun registerBuyer(name: String, email: String, password: String, confirmPassword: String) {
        if (!validate(name, email, password, confirmPassword)) return
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.registerBuyer(name, email, password)
        }
    }

    fun registerSeller(name: String, email: String, password: String, confirmPassword: String) {
        if (!validate(name, email, password, confirmPassword)) return
        viewModelScope.launch {
            _authState.value = Resource.Loading
            _authState.value = authRepository.registerSeller(name, email, password)
        }
    }

    fun setupStore(
        storeName: String, description: String?,
        address: String, district: String, operatingHours: String?
    ) {
        if (storeName.isBlank()) { _storeState.value = Resource.Error("Nama toko wajib diisi"); return }
        if (address.isBlank()) { _storeState.value = Resource.Error("Alamat wajib diisi"); return }
        if (district.isBlank()) { _storeState.value = Resource.Error("Kecamatan wajib diisi"); return }

        viewModelScope.launch {
            _storeState.value = Resource.Loading
            _storeState.value = storeRepository.setupStore(
                storeName, description, address, district, operatingHours
            )
        }
    }

    private fun validate(
        name: String, email: String, password: String, confirmPassword: String
    ): Boolean {
        when {
            name.isBlank()     -> { _authState.value = Resource.Error("Nama wajib diisi"); return false }
            email.isBlank()    -> { _authState.value = Resource.Error("Email wajib diisi"); return false }
            password.length < 8 -> { _authState.value = Resource.Error("Password minimal 8 karakter"); return false }
            password != confirmPassword -> { _authState.value = Resource.Error("Konfirmasi password tidak cocok"); return false }
        }
        return true
    }
}
