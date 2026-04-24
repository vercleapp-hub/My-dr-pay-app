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

    init {
        fetchPendingRequests()
    }

    fun fetchPendingRequests() {
        viewModelScope.launch {
            try {
                _pendingProfiles.value = supabase.postgrest.from("profiles")
                    .select { filter { eq("is_active", false) } }
                    .decodeList<Profile>()
                
                _pendingDeposits.value = supabase.postgrest.from("deposits")
                    .select { filter { eq("status", "pending") } }
                    .decodeList<DepositRequest>()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun approveProfile(profileId: String) {
        viewModelScope.launch {
            try {
                supabase.postgrest.from("profiles").update({
                    Profile::is_active setTo true
                }) {
                    filter { eq("id", profileId) }
                }
                fetchPendingRequests()
            } catch (e: Exception) { }
        }
    }

    fun approveDeposit(depositId: Int) {
        viewModelScope.launch {
            try {
                supabase.postgrest.from("deposits").update({
                    DepositRequest::status setTo "approved"
                }) {
                    filter { eq("id", depositId) }
                }
                fetchPendingRequests()
            } catch (e: Exception) { }
        }
    }
}
