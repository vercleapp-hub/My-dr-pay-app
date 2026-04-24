package dr.pay.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dr.pay.app.data.EMisrService
import dr.pay.app.data.Transaction
import dr.pay.app.data.repository.ServiceRepository
import dr.pay.app.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceViewModel(
    private val serviceRepository: ServiceRepository = ServiceRepository(),
    private val transactionRepository: TransactionRepository = TransactionRepository()
) : ViewModel() {

    private val _services = MutableStateFlow<List<EMisrService>>(emptyList())
    val services: StateFlow<List<EMisrService>> = _services

    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Idle)
    val transactionState: StateFlow<TransactionState> = _transactionState

    init {
        fetchServices()
    }

    private fun fetchServices() {
        viewModelScope.launch {
            _services.value = serviceRepository.getAllServices()
        }
    }

    fun executeTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.Loading
            val result = transactionRepository.createTransaction(transaction)
            if (result.isSuccess) {
                _transactionState.value = TransactionState.Success(result.getOrThrow())
            } else {
                _transactionState.value = TransactionState.Error(result.exceptionOrNull()?.message ?: "Transaction failed")
            }
        }
    }
}

sealed class TransactionState {
    object Idle : TransactionState()
    object Loading : TransactionState()
    data class Success(val transaction: Transaction) : TransactionState()
    data class Error(val message: String) : TransactionState()
}
