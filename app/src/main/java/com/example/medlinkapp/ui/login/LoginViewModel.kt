package com.example.medlinkapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.AuthRepository
import com.example.medlinkapp.model.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = repository.authenticateUser(email, password)

            result.onSuccess { role ->
                _loginState.value = LoginState.Success(role, "mock_jwt_token_123")
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "An unknown error occurred")
            }
        }
    }

}