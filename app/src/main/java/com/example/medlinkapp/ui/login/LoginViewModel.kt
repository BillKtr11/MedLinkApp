package com.example.medlinkapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medlinkapp.data.AuthRepository
import com.example.medlinkapp.model.UserData
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

            result.onSuccess { user ->
                _loginState.value = LoginState.Success(user.role, "mock_token", user.amka)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "An unknown error occurred")
            }
        }
    }

    fun register(userData: UserData) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.registerUser(userData)
            result.onSuccess {
                _loginState.value = LoginState.Success(userData.role, "mock_token", userData.amka)
            }.onFailure { exception ->
                _loginState.value = LoginState.Error(exception.message ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    fun logout() {
        com.example.medlinkapp.data.DBManager.setCurrentUser("")
        _loginState.value = LoginState.Idle
    }
}
