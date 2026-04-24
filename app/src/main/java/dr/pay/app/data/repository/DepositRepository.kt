package dr.pay.app.data.repository

import dr.pay.app.data.DepositRequest
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DepositRepository {

    suspend fun getDeposits(userId: String): List<DepositRequest> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("deposits").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<DepositRequest>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createDeposit(deposit: DepositRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("deposits").insert(deposit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
