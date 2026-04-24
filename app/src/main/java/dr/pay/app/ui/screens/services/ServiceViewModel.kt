package dr.pay.app.ui.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dr.pay.app.data.ServiceItem
import dr.pay.app.data.repository.ServicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceViewModel(private val servicesRepo: ServicesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceUiState>(ServiceUiState.Idle)
    val uiState: StateFlow<ServiceUiState> = _uiState

    fun performPayment(service: ServiceItem, targetNumber: String, amount: Double) {
        if (targetNumber.length < 10 || amount <= 0) {
            _uiState.value = ServiceUiState.Error("يرجى التأكد من البيانات المدخلة")
            return
        }

        viewModelScope.launch {
            _uiState.value = ServiceUiState.Processing
            val result = servicesRepo.payService(service, targetNumber, amount)
            
            result.onSuccess { refId ->
                _uiState.value = ServiceUiState.Success(refId)
            }.onFailure { error ->
                _uiState.value = ServiceUiState.Error(error.message ?: "فشل تنفيذ العملية")
            }
        }
    }

    fun resetState() {
        _uiState.value = ServiceUiState.Idle
    }
}

sealed class ServiceUiState {
    object Idle : ServiceUiState()
    object Processing : ServiceUiState()
    data class Success(val referenceId: String) : ServiceUiState()
    data class Error(val message: String) : ServiceUiState()
}
