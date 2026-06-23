package com.kulaan.app.ui.buyer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kulaan.app.data.repository.NotificationRepository
import com.kulaan.app.data.repository.OrderRepository
import com.kulaan.app.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuyerSharedViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val notificationRepository = NotificationRepository(sessionManager)
    private val orderRepository = OrderRepository(sessionManager)

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    private val _activeOrdersCount = MutableStateFlow(0)
    val activeOrdersCount: StateFlow<Int> = _activeOrdersCount.asStateFlow()

    init {
        refreshBadges()
    }

    fun refreshBadges() {
        viewModelScope.launch {
            try {
                val notifResponse = notificationRepository.getNotifications()
                if (notifResponse.isSuccessful && notifResponse.body()?.success == true) {
                    val count = notifResponse.body()?.data?.count { it.isRead == 0 } ?: 0
                    _unreadNotificationsCount.value = count
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                val orderResponse = orderRepository.getOrders()
                if (orderResponse.isSuccessful && orderResponse.body()?.success == true) {
                    val count = orderResponse.body()?.data?.count { 
                        it.status == "menunggu" || it.status == "diproses"
                    } ?: 0
                    _activeOrdersCount.value = count
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class BuyerSharedViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BuyerSharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BuyerSharedViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
