package com.kulaan.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.kulaan.app.data.local.UserPreferences
import com.kulaan.app.data.model.AuthResponse
import com.kulaan.app.data.model.LoginRequest
import com.kulaan.app.data.model.RegisterRequest
import com.kulaan.app.data.model.User
import com.kulaan.app.data.repository.AuthRepository
import com.kulaan.app.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response

enum class AuthTab { LOGIN, REGISTER }
enum class Role { BUYER, SELLER }

sealed class AuthResult {
    data class Success(val user: User, val token: String, val notice: String?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class AuthState(
    val selectedTab: AuthTab = AuthTab.LOGIN,
    val selectedRole: Role = Role.BUYER,
    val isLoading: Boolean = false,
    val authResult: AuthResult? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val userPreferences: UserPreferences,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState: StateFlow<AuthState> = _uiState.asStateFlow()

    fun onTabSwitch(tab: AuthTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                fieldErrors = emptyMap(),
                authResult = null,
                selectedRole = Role.BUYER
            )
        }
    }

    fun onRoleSelected(role: Role) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun clearErrors() {
        _uiState.update { it.copy(fieldErrors = emptyMap(), authResult = null) }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, fieldErrors = emptyMap(), authResult = null) }
            try {
                val request = LoginRequest(email, pass)
                val response = repository.login(request)
                handleResponse(response)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authResult = AuthResult.Error("Debug Login: ${e.localizedMessage ?: e.message ?: "Unknown Error"}")
                    )
                }
            }
        }
    }

    fun register(name: String, email: String, phone: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, fieldErrors = emptyMap(), authResult = null) }
            try {
                val roleStr = if (uiState.value.selectedRole == Role.BUYER) "buyer" else "seller"
                val request = RegisterRequest(name, email, phone, pass, pass, roleStr)
                val response = repository.register(request)
                handleResponse(response)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        authResult = AuthResult.Error("Debug Register: ${e.localizedMessage ?: e.message ?: "Unknown Error"}")
                    )
                }
            }
        }
    }

    private suspend fun handleResponse(response: Response<AuthResponse>) {
        if (response.isSuccessful && response.body()?.success == true) {
            val body = response.body()!!
            val user = body.data!!.user
            val token = body.data.token
            val notice = body.data.notice
            saveAuthToDataStore(token, user)
            saveAuthToSessionManager(token, user)
            _uiState.update {
                it.copy(isLoading = false, authResult = AuthResult.Success(user, token, notice))
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val parsedError = try {
                Gson().fromJson(errorBody, AuthResponse::class.java)
            } catch (e: Exception) { null }

            val fieldErrors = mutableMapOf<String, String>()
            parsedError?.errors?.forEach { (key, messages) ->
                if (messages.isNotEmpty()) {
                    fieldErrors[key] = messages.first()
                }
            }

            val errorMessage = if (response.code() == 401) {
                "Email atau password salah. Silakan coba lagi."
            } else if (parsedError?.message != null && fieldErrors.isEmpty()) {
                parsedError.message
            } else if (fieldErrors.isEmpty()) {
                "Terjadi kesalahan. Code: ${response.code()}. Body: ${errorBody?.take(100)}"
            } else {
                null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    fieldErrors = fieldErrors,
                    authResult = errorMessage?.let { msg -> AuthResult.Error(msg) }
                )
            }
        }
    }

    private suspend fun saveAuthToDataStore(token: String, user: User) {
        userPreferences.saveAuthData(token, user)
    }

    private fun saveAuthToSessionManager(token: String, user: User) {
        val roleStr = user.roles.firstOrNull() ?: ""
        sessionManager.saveSession(
            token = token,
            userId = user.id_user,
            name = user.name,
            email = user.email,
            role = roleStr,
            hasStore = false
        )
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val userPreferences: UserPreferences,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository, userPreferences, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
