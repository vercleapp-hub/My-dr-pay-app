package dr.pay.app.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val phone: String,
    val email: String? = null,
    val name: String,
    val national_id: String? = null,
    val governorate: String? = null,
    val address: String? = null,
    val status: String = "pending", // pending, active, suspended, blocked
    val role_id: Int = 2, // 1: admin, 2: merchant, 3: subuser
    val parent_id: String? = null,
    val device_info: String? = null, // JSON string
    val created_at: String? = null
)

@Serializable
data class ServiceItem(
    val id: Int? = null,
    val name: String,
    val code: String,
    val category: String? = null,
    val icon: String? = null,
    val active: Boolean = true,
    val api_service_code: String? = null
)

@Serializable
data class Transaction(
    val id: String? = null,
    val user_id: String,
    val service_id: Int? = null,
    val type: String, // deposit, withdrawal, payment, transfer, commission
    val amount: Double,
    val balance_before: Double,
    val balance_after: Double,
    val status: String = "pending", // pending, completed, failed, cancelled
    val api_transaction_id: String? = null,
    val notes: String? = null,
    val created_at: String? = null
)

@Serializable
data class DepositRequest(
    val id: String? = null,
    val user_id: String,
    val amount: Double,
    val method: String, // bank_transfer, cash, online
    val image_url: String? = null,
    val status: String = "pending", // pending, approved, rejected
    val admin_id: String? = null,
    val rejection_reason: String? = null,
    val created_at: String? = null
)
