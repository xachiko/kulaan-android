package com.kulaan.app.ui.seller.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kulaan.app.data.model.SellerProduct
import com.kulaan.app.data.model.Store
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SellerUiState(
    val storeProfile: UiState<Store> = UiState.Loading,
    val products: UiState<List<SellerProduct>> = UiState.Loading,
    val hasStore: Boolean = false
)

class SellerViewModel(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellerUiState())
    val uiState: StateFlow<SellerUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _uiState.update { it.copy(hasStore = sessionManager.hasStore()) }
        loadStoreProfile()
        loadProducts()
    }

    fun loadStoreProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(storeProfile = UiState.Loading) }
            val result = repository.getStoreProfile()
            result.onSuccess { response ->
                response.data?.let { store ->
                    _uiState.update {
                        it.copy(
                            storeProfile = UiState.Success(store),
                            hasStore = true
                        )
                    }
                    sessionManager.setHasStore(true)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(storeProfile = UiState.Error(e.message ?: "Gagal memuat profil toko"))
                }
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(products = UiState.Loading) }
            val result = repository.getSellerProducts()
            result.onSuccess { response ->
                _uiState.update { it.copy(products = UiState.Success(response.data)) }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(products = UiState.Error(e.message ?: "Gagal memuat produk"))
                }
            }
        }
    }
}

class SellerViewModelFactory(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SellerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SellerViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
