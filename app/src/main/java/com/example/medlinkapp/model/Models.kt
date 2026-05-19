package com.example.medlinkapp.model
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
enum class UserRole {
    PATIENT, DOCTOR, CAREGIVER
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: UserRole, val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = repository.authenticateUser(email, password)

            result.onSuccess { role ->
                // In a real app, save the token to EncryptedSharedPreferences here
                _loginState.value = LoginState.Success(role, "mock_jwt_token_123")
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "An unknown error occurred")
            }
        }
    }

}