package dr.pay.app.data.repository

import dr.pay.app.data.DepositRequest
import dr.pay.app.data.Transaction
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull

class WalletRepository {

    suspend fun getBalance(userId: String): Double = withContext(Dispatchers.IO) {
        try {
            val params = buildJsonObject {
                put("user_uuid", userId)
            }
            val response = supabase.postgrest.rpc("get_user_balance", params)
            // استخدام decodeAs إذا كانت الدالة ترجع رقم مباشر
            response.data.jsonPrimitive.doubleOrNull ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    suspend fun getTransactions(userId: String): List<Transaction> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("transactions")
                .select(columns = Columns.ALL) {
                    filter { eq("user_id", userId) }
                    order("created_at", order = Order.DESCENDING)
                }.decodeList<Transaction>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun requestDeposit(userId: String, amount: Double, method: String, imageUrl: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = DepositRequest(
                user_id = userId,
                amount = amount,
                method = method,
                image_url = imageUrl,
                status = "pending"
            )
            supabase.postgrest.from("deposits").insert(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
