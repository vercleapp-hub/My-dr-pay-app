package dr.pay.app.data.repository

import dr.pay.app.data.User
import dr.pay.app.util.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository {

    suspend fun signUp(phone: String, password: String, name: String, deviceInfo: Map<String, Any?>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = phone.trim().replace(Regex("[^0-9]"), "")
            val email = "${cleanPhone}@drpay.app"

            // 1. إنشاء الحساب في Auth
            val authUser = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            } ?: return@withContext Result.failure(Exception("فشل الاتصال بخادم الهوية"))

            // 2. إدراج البيانات في جدول users الموحد
            val newUser = User(
                id = authUser.id,
                phone = cleanPhone,
                name = name,
                email = email,
                status = "pending",
                role_id = 2, // Merchant
                device_info = buildJsonObject {
                    deviceInfo.forEach { (key, value) ->
                        put(key, value.toString())
                    }
                }.toString()
            )

            supabase.postgrest.from("users").insert(newUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("فشل التسجيل: ${e.message}"))
        }
    }

    suspend fun signIn(phone: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = phone.trim().replace(Regex("[^0-9]"), "")
            val email = "${cleanPhone}@drpay.app"
            
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = getCurrentUser() ?: run {
                supabase.auth.signOut()
                return@withContext Result.failure(Exception("لم يتم العثور على بيانات المستخدم"))
            }

            if (user.status != "active") {
                supabase.auth.signOut()
                return@withContext Result.failure(Exception("🔒 حسابك بانتظار تفعيل الإدارة"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("رقم الهاتف أو كلمة المرور غير صحيحة"))
        }
    }

    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        try {
            val session = supabase.auth.currentUserOrNull() ?: return@withContext null
            supabase.postgrest.from("users")
                .select(columns = Columns.ALL) {
                    filter { eq("id", session.id) }
                }
                .decodeSingle<User>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}
