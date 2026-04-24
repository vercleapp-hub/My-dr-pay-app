package com.eldocpay.app.repository

import com.eldocpay.app.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class UserRepository {

    suspend fun getUserBalance(userId: String): Double {
        return withContext(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client.postgrest.rpc(
                    "get_user_balance",
                    JSONObject().put("user_uuid", userId)
                )
                result.toString().toDoubleOrNull() ?: 0.0
            } catch (e: Exception) {
                0.0
            }
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return withContext(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client.postgrest["users"]
                    .select(columns = Columns.list("id", "name", "phone", "email", "status", "role_id")) {
                        eq("id", userId)
                    }
                    .decodeSingle<Map<String, Any>>()
                result
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateUserDeviceInfo(userId: String, deviceInfo: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["users"]
                    .update({
                        set("device_info", deviceInfo)
                        set("updated_at", "now()")
                    }) {
                        eq("id", userId)
                    }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}