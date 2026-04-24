package dr.pay.app.data.repository

import dr.pay.app.data.Transaction
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionRepository {

    suspend fun getTransactions(userId: String): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("transactions").select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeList<Transaction>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createTransaction(transaction: Transaction): Result<Transaction> = withContext(Dispatchers.IO) {
        try {
            val result = supabase.postgrest.from("transactions").insert(transaction) {
                select()
            }.decodeSingle<Transaction>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
