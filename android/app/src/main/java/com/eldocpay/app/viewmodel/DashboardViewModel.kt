package com.eldocpay.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eldocpay.app.repository.UserRepository
import com.eldocpay.app.repository.WalletRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val walletRepository = WalletRepository()

    private val _dashboardState = MutableLiveData<DashboardState>()
    val dashboardState: LiveData<DashboardState> = _dashboardState

    sealed class DashboardState {
        object Loading : DashboardState()
        data class Success(val userName: String, val balance: Double) : DashboardState()
        data class Error(val message: String) : DashboardState()
    }

    fun loadDashboardData(userId: String) {
        _dashboardState.value = DashboardState.Loading

        viewModelScope.launch {
            try {
                val userDeferred = async { userRepository.getUserProfile(userId) }
                val balanceDeferred = async { userRepository.getUserBalance(userId) }

                val user = userDeferred.await()
                val balance = balanceDeferred.await()

                if (user != null) {
                    _dashboardState.value = DashboardState.Success(
                        userName = user["name"] as? String ?: "مستخدم",
                        balance = balance
                    )
                } else {
                    _dashboardState.value = DashboardState.Error("فشل في تحميل البيانات")
                }
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error("خطأ: ${e.message}")
            }
        }
    }
}