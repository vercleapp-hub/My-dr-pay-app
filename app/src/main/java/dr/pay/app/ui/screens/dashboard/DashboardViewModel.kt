package dr.pay.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dr.pay.app.data.Profile
import dr.pay.app.data.ServiceItem
import dr.pay.app.data.Transaction
import dr.pay.app.data.repository.AuthRepository
import dr.pay.app.data.repository.WalletRepository
import dr.pay.app.data.repository.ServicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val authRepo: AuthRepository,
    private val walletRepo: WalletRepository,
    private val servicesRepo: ServicesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val profile = authRepo.getCurrentProfile()
                val services = servicesRepo.getAvailableServices()
                // هنا يمكن إضافة جلب آخر 5 معاملات
                if (profile != null) {
                    _uiState.value = DashboardUiState.Success(
                        profile = profile,
                        services = services,
                        recentTransactions = emptyList() // سيتم ربطها لاحقاً
                    )
                } else {
                    _uiState.value = DashboardUiState.Error("فشل تحميل البيانات")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "خطأ غير معروف")
            }
        }
    }
}

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val profile: Profile,
        val services: List<ServiceItem>,
        val recentTransactions: List<Transaction>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}
