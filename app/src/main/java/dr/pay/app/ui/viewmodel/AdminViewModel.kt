package dr.pay.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dr.pay.app.data.Profile
import dr.pay.app.data.DepositRequest
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val _pendingProfiles = MutableStateFlow<List<Profile>>(emptyList())
    val pendingProfiles: StateFlow<List<Profile>> = _pendingProfiles

    private val _pendingDeposits = MutableStateFlow<List<DepositRequest>>(emptyList())
    val pendingDeposits: StateFlow<List<DepositRequest>> = _pendingDeposits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        fetchPendingRequests()
    }

    fun fetchPendingRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _pendingProfiles.value = supabase.postgrest.from("profiles")
                    .select { filter { eq("is_active", false) } }
                    .decodeList<Profile>()
                
                _pendingDeposits.value = supabase.postgrest.from("deposit_requests")
                    .select { filter { eq("status", "pending") } }
                    .decodeList<DepositRequest>()
            } catch (e: Exception) {
                _message.value = "خطأ في جلب البيانات: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveProfile(profileId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabase.postgrest.from("profiles").update({
                    set("is_active", true)
                }) {
                    filter { eq("id", profileId) }
                }
                _message.value = "تم تفعيل التاجر بنجاح"
                fetchPendingRequests()
            } catch (e: Exception) {
                _message.value = "فشل التفعيل: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveDeposit(depositId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ملاحظة: الـ Trigger في قاعدة البيانات سيتكفل بتحديث الرصيد تلقائياً عند تغيير الحالة لـ approved
                supabase.postgrest.from("deposit_requests").update({
                    set("status", "approved")
                }) {
                    filter { eq("id", depositId) }
                }
                _message.value = "تم تأكيد الإيداع وشحن الرصيد"
                fetchPendingRequests()
            } catch (e: Exception) {
                _message.value = "فشل تأكيد الإيداع: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
