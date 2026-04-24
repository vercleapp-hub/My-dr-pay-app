package dr.pay.app.data.repository

import dr.pay.app.data.ServiceItem
import dr.pay.app.data.ServiceOperation
import dr.pay.app.util.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServicesRepository(private val walletRepo: WalletRepository) {

    // 1. جلب الخدمات المتاحة (فودافون، اتصالات، فواتير...)
    suspend fun getAvailableServices(): List<ServiceItem> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("services")
                .select(columns = Columns.ALL) {
                    filter { eq("is_active", true) }
                }.decodeList<ServiceItem>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 2. تنفيذ الخدمة (الربط مع E-Misr)
    suspend fun payService(service: ServiceItem, targetNumber: String, amount: Double): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext Result.failure(Exception("غير مصرح"))

            // أولاً: خصم الرصيد من المحفظة
            val walletResult = walletRepo.executeTransaction(
                type = "payment",
                amount = amount,
                target = targetNumber
            )

            if (walletResult.isFailure) return@withContext Result.failure(walletResult.exceptionOrNull()!!)

            // ثانياً: تسجيل العملية في جدول Operations تمهيداً لطلب الـ API
            val operation = ServiceOperation(
                user_id = userId,
                service_id = service.id,
                amount = amount,
                status = "processing"
            )
            
            val opResult = supabase.postgrest.from("operations").insert(operation) {
                select()
            }.decodeSingle<ServiceOperation>()

            // ثالثاً: (محاكاة) استدعاء E-Misr API
            // هنا سيتم وضع كود Retrofit لاستدعاء: https://e-misr.com/api/v1/pay...
            val apiSuccess = true // سيتم تغييرها عند الربط الفعلي
            
            if (apiSuccess) {
                // تحديث حالة العملية لـ Success
                supabase.postgrest.from("operations").update(mapOf("status" to "success")) {
                    filter { eq("id", opResult.id!!) }
                }
                Result.success("تم تنفيذ العملية بنجاح. رقم المرجع: ${walletResult.getOrNull()}")
            } else {
                // في حالة فشل الـ API: يجب إعادة الرصيد للمستخدم (Refund)
                // (سيتم برمجة نظام الـ Refund لاحقاً للأمان الكامل)
                Result.failure(Exception("خطأ من مزود الخدمة"))
            }

        } catch (e: Exception) {
            Result.failure(Exception("حدث خطأ تقني: ${e.message}"))
        }
    }
}
