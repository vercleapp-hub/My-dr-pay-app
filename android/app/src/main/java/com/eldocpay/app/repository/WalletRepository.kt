package com.eldocpay.app.repository

import com.eldocpay.app.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class Transaction(
    val id: String,
    val type: String,
    val amount: Double,
    val balanceBefore: Double,
    val balanceAfter: Double,
    val status: String,
    val createdAt: String,
    val notes: String?
)

data class Deposit(
    val id: String,
    val amount: Double,
    val method: String,
    val imageUrl: String?,
    val status: String,
    val createdAt: String
)

class WalletRepository {

    suspend fun getUserTransactions(userId: String, limit: Int = 50): List<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client.postgrest["transactions"]
                    .select(columns = Columns.list(
                        "id", "type", "amount", "balance_before", "balance_after",
                        "status", "created_at", "notes"
                    )) {
                        eq("user_id", userId)
                        order("created_at", ascending = false)
                        limit(limit)
                    }
                    .decodeList<Map<String, Any>>()

                result.map { map ->
                    Transaction(
                        id = map["id"] as String,
                        type = map["type"] as String,
                        amount = (map["amount"] as Number).toDouble(),
                        balanceBefore = (map["balance_before"] as Number).toDouble(),
                        balanceAfter = (map["balance_after"] as Number).toDouble(),
                        status = map["status"] as String,
                        createdAt = map["created_at"] as String,
                        notes = map["notes"] as? String
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun createTransaction(
        userId: String,
        serviceId: Int?,
        type: String,
        amount: Double,
        notes: String? = null
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val params = JSONObject()
                    .put("p_user_id", userId)
                    .put("p_service_id", serviceId)
                    .put("p_type", type)
                    .put("p_amount", amount)
                    .put("p_notes", notes)

                SupabaseClient.client.postgrest.rpc(
                    "create_transaction",
                    params
                )
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun createDeposit(
        userId: String,
        amount: Double,
        method: String,
        imageUrl: String?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                SupabaseClient.client.postgrest["deposits"]
                    .insert({
                        set("user_id", userId)
                        set("amount", amount)
                        set("method", method)
                        set("image_url", imageUrl)
                    })
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getUserDeposits(userId: String): List<Deposit> {
        return withContext(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client.postgrest["deposits"]
                    .select(columns = Columns.list(
                        "id", "amount", "method", "image_url", "status", "created_at"
                    )) {
                        eq("user_id", userId)
                        order("created_at", ascending = false)
                    }
                    .decodeList<Map<String, Any>>()

                result.map { map ->
                    Deposit(
                        id = map["id"] as String,
                        amount = (map["amount"] as Number).toDouble(),
                        method = map["method"] as String,
                        imageUrl = map["image_url"] as? String,
                        status = map["status"] as String,
                        createdAt = map["created_at"] as String
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}