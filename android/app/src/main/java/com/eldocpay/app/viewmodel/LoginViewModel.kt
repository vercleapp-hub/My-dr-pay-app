package com.eldocpay.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldocpay.app.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val userId: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    init {
        _loginState.value = LoginState.Idle
    }

    fun login(phone: String, password: String) {
        if (!SupabaseClient.isValidEgyptianPhone(phone)) {
            _loginState.value = LoginState.Error("رقم الهاتف غير صحيح")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val email = SupabaseClient.phoneToEmail(phone)
                val result = SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                if (result != null) {
                    _loginState.value = LoginState.Success(result.user?.id ?: "")
                } else {
                    _loginState.value = LoginState.Error("فشل في تسجيل الدخول")
                }
            } catch (e: RestException) {
                val errorMessage = when (e.error) {
                    "invalid_credentials" -> "بيانات الدخول غير صحيحة"
                    "email_not_confirmed" -> "البريد الإلكتروني غير مفعل"
                    else -> "خطأ في تسجيل الدخول: ${e.message}"
                }
                _loginState.value = LoginState.Error(errorMessage)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("خطأ غير متوقع: ${e.message}")
            }
        }
    }
}