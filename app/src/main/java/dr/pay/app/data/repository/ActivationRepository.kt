package dr.pay.app.data.repository

import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivationRepository {

    suspend fun redeemCode(userId: String, code: String): Result<Double> = withContext(Dispatchers.IO) {
        try {
            // 1. التحقق من الكود في جدول activation_codes
            val response = supabase.postgrest.from("activation_codes")
                .select() {
                    filter {
                        eq("code", code)
                        eq("is_used", false)
                    }
                }.decodeSingleOrNull<ActivationCode>()

            if (response == null) {
                return@withContext Result.failure(Exception("كود غير صالح أو تم استخدامه مسبقاً"))
            }

            // 2. تحديث الكود كـ "مستخدم"
            supabase.postgrest.from("activation_codes")
                .update(mapOf("is_used" to true, "used_by" to userId)) {
                    filter { eq("code", code) }
                }

            // 3. تحديث رصيد المستخدم (سيتم عبر Trigger في قاعدة البيانات لضمان الأمان)
            // لكن سنقوم بجلب الرصيد الجديد للتأكيد
            Result.success(response.amount)
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في تفعيل الكود: ${e.message}"))
        }
    }
}

@kotlinx.serialization.Serializable
data class ActivationCode(
    val id: Int,
    val code: String,
    val amount: Double,
    val is_used: Boolean,
    val used_by: String? = null
)
