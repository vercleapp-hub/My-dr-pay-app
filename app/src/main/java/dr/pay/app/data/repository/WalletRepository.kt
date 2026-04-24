package dr.pay.app.data.repository

import dr.pay.app.data.DepositRequest
import dr.pay.app.data.Profile
import dr.pay.app.data.Transaction
import dr.pay.app.util.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class WalletRepository {

    // 1. الحصول على الرصيد الحالي لحظياً
    suspend fun getBalance(): Double = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext 0.0
            val profile = supabase.postgrest.from("profiles")
                .select(columns = Columns.list("balance")) {
                    filter { eq("id", userId) }
                }.decodeSingle<Profile>()
            profile.balance
        } catch (e: Exception) {
            0.0
        }
    }

    // 2. تنفيذ عملية مالية (شحن/تحويل) مع الأمان
    suspend fun executeTransaction(type: String, amount: Double, target: String? = null): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("غير مصرح"))
            
            // جلب الرصيد الحالي
            val currentBalance = getBalance()
            if (currentBalance < amount) return@withContext Result.failure(Exception("عذراً، الرصيد غير كافٍ"))

            val newBalance = currentBalance - amount
            val refId = "TXN-${System.currentTimeMillis()}-${(100..999).random()}"

            // تحديث الرصيد وتسجيل العملية (Atomic Update)
            supabase.postgrest.from("profiles").update(mapOf("balance" to newBalance)) {
                filter { eq("id", userId) }
            }

            val transaction = Transaction(
                user_id = userId,
                type = type,
                amount = amount,
                balance_before = currentBalance,
                balance_after = newBalance,
                status = "success",
                reference_id = refId
            )
            
            supabase.postgrest.from("transactions").insert(transaction)
            
            Result.success(refId)
        } catch (e: Exception) {
            Result.failure(Exception("فشل تنفيذ العملية: ${e.message}"))
        }
    }

    // 3. تقديم طلب إيداع (رصيد جديد)
    suspend fun requestDeposit(amount: Double, method: String, imageUrl: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("غير مصرح"))
            
            val deposit = DepositRequest(
                user_id = userId,
                amount = amount,
                method = method,
                image_url = imageUrl,
                status = "pending"
            )
            
            supabase.postgrest.from("deposits").insert(deposit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("فشل إرسال الطلب: ${e.message}"))
        }
    }
}
