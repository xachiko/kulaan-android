package com.kulaan.app.ui.buyer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kulaan.app.data.model.Category
import com.kulaan.app.data.model.Product
import com.kulaan.app.data.repository.ProductRepository
import com.kulaan.app.utils.SessionManager
import com.kulaan.app.utils.UiState
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class ProductViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val repository = ProductRepository(sessionManager)

    private val _productsState = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<Product>>> = _productsState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _categoriesState = MutableStateFlow<List<Category>>(emptyList())
    val categoriesState: StateFlow<List<Category>> = _categoriesState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Int?>(null)
    val selectedCategory: StateFlow<Int?> = _selectedCategory.asStateFlow()

    private var currentPage = 1
    private var lastPage = 1
    private var isLoadingNextPage = false
    private val currentProductsList = mutableListOf<Product>()

    init {
        loadCategories()
        loadProducts(isRefresh = true)

        // Setup search debounce
        _searchQuery
            .debounce(400L)
            .distinctUntilChanged()
            .onEach {
                loadProducts(isRefresh = true)
            }
            .launchIn(viewModelScope)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    _categoriesState.value = response.body()?.data ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadProducts(isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 1
            currentProductsList.clear()
            _isRefreshing.value = true
            if (_productsState.value !is UiState.Success) {
                _productsState.value = UiState.Loading
            }
        }

        if (currentPage > lastPage && !isRefresh) return

        viewModelScope.launch {
            try {
                isLoadingNextPage = true
                val response = repository.getProducts(
                    search = _searchQuery.value.ifBlank { null },
                    category = _selectedCategory.value,
                    page = currentPage
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        lastPage = body.meta?.lastPage ?: 1
                        currentProductsList.addAll(body.data)
                        _productsState.value = UiState.Success(currentProductsList.toList())
                    } else {
                        if (isRefresh) _productsState.value = UiState.Error("Failed to load products")
                    }
                } else {
                    if (isRefresh) _productsState.value = UiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isRefresh) _productsState.value = UiState.Error("Network error")
            } finally {
                isLoadingNextPage = false
                if (isRefresh) _isRefreshing.value = false
            }
        }
    }

    fun refresh() {
        loadProducts(isRefresh = true)
    }

    fun searchProducts(keyword: String) {
        _searchQuery.value = keyword
    }

    fun filterByCategory(categoryId: Int?) {
        _selectedCategory.value = categoryId
        loadProducts(isRefresh = true)
    }

    fun loadNextPage() {
        if (!isLoadingNextPage && currentPage < lastPage) {
            currentPage++
            loadProducts(isRefresh = false)
        }
    }
}

class ProductViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
