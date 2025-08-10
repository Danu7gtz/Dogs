package com.example.dogs.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.dogs.data.AuthRepository
import com.example.dogs.data.remote.LoginResponse
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val data: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository()

    private val _state = MutableLiveData<LoginState>(LoginState.Idle)
    val state: LiveData<LoginState> = _state

    fun login(email: String, password: String) {
        _state.value = LoginState.Loading
        viewModelScope.launch {
            val result = repo.login(email, password)
            _state.value = result.fold(
                onSuccess = { LoginState.Success(it) },
                onFailure = { LoginState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun reset() { _state.value = LoginState.Idle }
}