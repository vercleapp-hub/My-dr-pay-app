package dr.pay.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dr.pay.app.data.Profile
import dr.pay.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signIn(email, password)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
                fetchProfile()
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun signUp(phone: String, password: String, profile: Profile, deviceInfo: Map<String, Any?>) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.signUp(phone, password, profile, deviceInfo)
            if (result.isSuccess) {
                _authState.value = AuthState.Authenticated
                _profile.value = profile
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "خطأ غير معروف")
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            _profile.value = repository.getCurrentProfile()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _authState.value = AuthState.Idle
            _profile.value = null
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
