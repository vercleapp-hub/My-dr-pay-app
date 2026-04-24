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
            // تنظيف صارم لرقم الهاتف
            val cleanPhone = phone.trim().replace(Regex("[^0-9]"), "")
            
            if (cleanPhone.length < 10) {
                return@withContext Result.failure(Exception("رقم الهاتف غير صحيح"))
            }

            // صيغة إيميل نظيفة ومقبولة عالمياً
            val email = "${cleanPhone}@drpay.app"

            // 1. التحقق من وجود الهاتف مسبقاً
            val checkExisting = supabase.postgrest.from("profiles")
                .select(columns = Columns.list("phone_number")) {
                    filter { eq("phone_number", cleanPhone) }
                }.decodeList<Profile>()

            if (checkExisting.isNotEmpty()) {
                return@withContext Result.failure(Exception("رقم الهاتف مسجل بالفعل"))
            }

            // 2. إنشاء الحساب في Auth
            val authUser = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            } ?: return@withContext Result.failure(Exception("فشل الاتصال بخادم الهوية"))

            // 3. إدراج البيانات مع ربط كافة تفاصيل الجهاز
            val finalProfile = profile.copy(
                id = authUser.id,
                phone_number = cleanPhone,
                is_active = false, 
                merchant_code = (100000..999999).random().toString(),
                balance = 0.0,
                // دمج بيانات التتبع من deviceInfo
                device_id = deviceInfo["device_id"] as? String,
                device_model = deviceInfo["device_model"] as? String,
                os_name = deviceInfo["os_name"] as? String,
                cpu_cores = deviceInfo["cpu_cores"] as? Int,
                screen_resolution = deviceInfo["screen_resolution"] as? String,
                connection_type = deviceInfo["connection_type"] as? String,
                download_speed = deviceInfo["download_speed"] as? String,
                last_ip_public = deviceInfo["last_ip_public"] as? String,
                location_lat = deviceInfo["location_lat"] as? Double,
                location_lng = deviceInfo["location_lng"] as? Double,
                device_fingerprint = deviceInfo["device_fingerprint"] as? String
            )

            supabase.postgrest.from("profiles").insert(finalProfile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("فشل التسجيل: ${e.message}"))
        }
    }

    suspend fun signIn(phone: String, password: String, currentDeviceId: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val cleanPhone = phone.trim().replace(Regex("[^0-9]"), "")
            val email = "${cleanPhone}@drpay.app"
            
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val profile = getCurrentProfile() ?: return@withContext Result.failure(Exception("لم يتم العثور على الحساب"))
            
            if (!profile.is_active) {
                supabase.auth.signOut()
                return@withContext Result.failure(Exception("حسابك بانتظار تفعيل الإدارة"))
            }

            // منطق التحكم في الجهاز
            if (currentDeviceId != null) {
                if (profile.device_id != null && profile.device_id != currentDeviceId) {
                    supabase.auth.signOut()
                    return@withContext Result.failure(Exception("هذا الحساب مقيد بجهاز آخر. تواصل مع الدعم الفني."))
                } else if (profile.device_id == null) {
                    // ربط الجهاز الأول
                    supabase.postgrest.from("profiles")
                        .update(mapOf("device_id" to currentDeviceId)) {
                            filter { eq("id", profile.id) }
                        }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("خطأ في الدخول: ${e.message}"))
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
