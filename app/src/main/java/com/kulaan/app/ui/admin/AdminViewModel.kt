package com.kulaan.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kulaan.app.data.model.AdminDashboardData
import com.kulaan.app.data.repository.AdminRepository
import com.kulaan.app.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AdminDashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<AdminDashboardData>> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard(isRefresh: Boolean = false) {
        if (isRefresh) {
            _isRefreshing.value = true
        } else {
            _uiState.value = UiState.Loading
        }
        viewModelScope.launch {
            try {
                val response = repository.getAdminDashboard()
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data
                    _uiState.value = UiState.Success(data)
                } else {
                    _uiState.value = UiState.Error("Gagal memuat data dashboard: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Terjadi kesalahan jaringan.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun verifyStore(idStore: Int, status: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.verifyStore(idStore, status)
                if (response.isSuccessful && response.body()?.success == true) {
                    onComplete(true, response.body()?.message ?: "Status toko diperbarui.")
                    loadDashboard(isRefresh = true)
                } else {
                    onComplete(false, "Gagal memperbarui status: ${response.message()}")
                }
            } catch (e: Exception) {
                onComplete(false, "Terjadi kesalahan jaringan.")
            }
        }
    }
}

class AdminViewModelFactory(private val repository: AdminRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
