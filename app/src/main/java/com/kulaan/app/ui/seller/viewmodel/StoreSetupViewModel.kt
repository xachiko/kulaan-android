package com.kulaan.app.ui.seller.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kulaan.app.data.model.Category
import com.kulaan.app.data.repository.StoreRepository
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StoreSetupFormState(
    val storeName: String = "",
    val description: String = "",
    val address: String = "",
    val selectedDistrict: String = "",
    val selectedCategoryId: Int? = null,
    val logoUri: Uri? = null,
    val openEveryDay: Boolean = true,
    val selectedDays: Set<String> = setOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"),
    val openTime: String = "08:00",
    val closeTime: String = "17:00"
)

data class StoreSetupUiState(
    val formState: StoreSetupFormState = StoreSetupFormState(),
    val categories: List<Category> = emptyList(),
    val districts: List<String> = emptyList(),
    val submitState: UiState<Unit>? = null,
    val isLoadingCategories: Boolean = true
)

class StoreSetupViewModel(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreSetupUiState())
    val uiState: StateFlow<StoreSetupUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true) }
            val result = repository.getCategories()
            result.onSuccess { response ->
                val fetchedCategories = if (response.data.isNotEmpty()) response.data else DUMMY_CATEGORIES
                _uiState.update {
                    it.copy(
                        categories = fetchedCategories,
                        isLoadingCategories = false,
                        districts = JEBRES_KELURAHAN.toList()
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        categories = DUMMY_CATEGORIES,
                        isLoadingCategories = false,
                        districts = JEBRES_KELURAHAN.toList()
                    )
                }
            }
        }
    }

    fun updateStoreName(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(storeName = value)) }
    }

    fun updateDescription(value: String) {
        if (value.length <= 200) {
            _uiState.update { it.copy(formState = it.formState.copy(description = value)) }
        }
    }

    fun updateAddress(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(address = value)) }
    }

    fun updateDistrict(value: String) {
        _uiState.update { it.copy(formState = it.formState.copy(selectedDistrict = value)) }
    }

    fun updateCategory(id: Int?) {
        _uiState.update { it.copy(formState = it.formState.copy(selectedCategoryId = id)) }
    }

    fun updateLogo(uri: Uri?) {
        _uiState.update { it.copy(formState = it.formState.copy(logoUri = uri)) }
    }

    fun toggleOpenEveryDay() {
        val current = _uiState.value.formState
        if (current.openEveryDay) {
            _uiState.update {
                it.copy(formState = it.formState.copy(openEveryDay = false, selectedDays = emptySet()))
            }
        } else {
            _uiState.update {
                it.copy(
                    formState = it.formState.copy(
                        openEveryDay = true,
                        selectedDays = setOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                    )
                )
            }
        }
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.formState
        val newDays = if (day in current.selectedDays) {
            current.selectedDays - day
        } else {
            current.selectedDays + day
        }
        _uiState.update {
            it.copy(
                formState = it.formState.copy(
                    selectedDays = newDays,
                    openEveryDay = false
                )
            )
        }
    }

    fun updateOpenTime(time: String) {
        _uiState.update { it.copy(formState = it.formState.copy(openTime = time)) }
    }

    fun updateCloseTime(time: String) {
        _uiState.update { it.copy(formState = it.formState.copy(closeTime = time)) }
    }

    fun getOperatingHoursText(): String {
        val form = _uiState.value.formState
        val days = if (form.openEveryDay) "Setiap hari" else {
            form.selectedDays.sortedBy { DAY_ORDER.indexOf(it) }.joinToString(", ")
        }
        return "$days: ${form.openTime}-${form.closeTime}"
    }

    fun submit(context: Context) {
        val form = _uiState.value.formState
        if (form.storeName.isBlank()) {
            _uiState.update { it.copy(submitState = UiState.Error("Nama toko harus diisi")) }
            return
        }
        if (form.selectedCategoryId == null) {
            _uiState.update { it.copy(submitState = UiState.Error("Kategori usaha harus dipilih")) }
            return
        }
        if (form.selectedDistrict.isBlank()) {
            _uiState.update { it.copy(submitState = UiState.Error("Kecamatan / Kelurahan harus dipilih")) }
            return
        }
        _uiState.update { it.copy(submitState = UiState.Loading) }
        viewModelScope.launch {
            val categoryName = form.selectedCategoryId?.let { id ->
                _uiState.value.categories.find { it.idCategory == id }?.nameCategory
            }

            val operatingHours = getOperatingHoursText()
            val result = repository.createStore(
                storeName = form.storeName,
                description = form.description.ifBlank { null },
                address = form.address.ifBlank { null },
                district = form.selectedDistrict.ifBlank { null },
                operatingHours = operatingHours,
                category = categoryName,
                logoUri = form.logoUri,
                context = context
            )
            result.onSuccess {
                sessionManager.setHasStore(true)
                _uiState.update { it.copy(submitState = UiState.Success(Unit)) }
            }.onFailure { e ->
                _uiState.update { it.copy(submitState = UiState.Error(e.message ?: "Gagal menyimpan toko")) }
            }
        }
    }

    companion object {
        val JEBRES_KELURAHAN = listOf(
            "Jebres", "Gandekan", "Jagalan", "Kepatihan Kulon",
            "Kepatihan Wetan", "Mojosongo", "Pucang Sawit",
            "Purwodiningratan", "Sewu", "Tegalharjo"
        )
        val DUMMY_CATEGORIES = listOf(
            Category(1, "Makanan & Minuman", null),
            Category(2, "Fashion & Batik", null),
            Category(3, "Kerajinan Tangan", null),
            Category(4, "Elektronik", null),
            Category(5, "Kecantikan", null),
            Category(6, "Pertanian", null),
            Category(7, "Jasa", null),
            Category(8, "Lainnya", null)
        )
        val DAYS = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val DAY_ORDER = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    }
}

class StoreSetupViewModelFactory(
    private val repository: StoreRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreSetupViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreSetupViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
