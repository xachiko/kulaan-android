package com.kulaan.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.data.repository.StoreRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val storeRepository: StoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(authRepository, storeRepository) as T
    }
}
