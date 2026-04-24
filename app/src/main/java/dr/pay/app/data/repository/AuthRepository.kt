package dr.pay.app.data.repository

import dr.pay.app.data.Profile
import dr.pay.app.util.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    suspend fun signUp(phone: String, password: String, profile: Profile, deviceInfo: Map<String, Any?>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // التحقق أولاً من عدم وجود الرقم القومي أو الهاتف
            val existing = supabase.postgrest.from("profiles")
                .select(columns = Columns.list("phone_number", "national_id")) {
                    filter {
                        or {
                            eq("phone_number", phone)
                            profile.national_id?.let { eq("national_id", it) }
                        }
                    }
                }.decodeList<Profile>()

            if (existing.isNotEmpty()) {
                return@withContext Result.failure(Exception("رقم الهاتف أو الرقم القومي مسجل بالفعل"))
            }

            val cleanPhone = phone.trim()
            val email = "u$cleanPhone@drpay.app"
            val user = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            if (user != null) {
                // دمج بيانات الجهاز مع الملف الشخصي
                val finalProfile = profile.copy(
                    id = user.id,
                    last_ip_public = deviceInfo["ip"] as? String,
                    device_model = deviceInfo["model"] as? String,
                    os_name = deviceInfo["os"] as? String,
                    device_fingerprint = deviceInfo["fingerprint"] as? String,
                    location_lat = deviceInfo["lat"] as? Double,
                    location_lng = deviceInfo["lng"] as? Double
                )
                supabase.postgrest.from("profiles").insert(finalProfile)
                Result.success(Unit)
            } else {
                Result.failure(Exception("فشل إنشاء الحساب"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(phone: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = phone.trim()
            val email = "u$cleanPhone@drpay.app"
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        supabase.auth.signOut()
    }

    suspend fun getCurrentProfile(): Profile? = withContext(Dispatchers.IO) {
        try {
            val currentUser = supabase.auth.currentUserOrNull() ?: return@withContext null
            supabase.postgrest.from("profiles")
                .select(columns = Columns.ALL) {
                    filter {
                        eq("id", currentUser.id)
                    }
                }
                .decodeSingle<Profile>()
        } catch (e: Exception) {
            null
        }
    }
}
