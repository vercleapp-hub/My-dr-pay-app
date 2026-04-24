package dr.pay.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Profile(
    val id: String,
    val full_name: String? = null,
    val phone_number: String,
    val national_id: String? = null,
    val province: String? = null,
    val address: String? = null,
    val id_card_front_url: String? = null,
    val id_card_back_url: String? = null,
    val merchant_code: String? = null,
    val user_type: String = "merchant",
    val parent_id: String? = null,
    val balance: Double = 0.0,
    val is_active: Boolean = false,
    
    // Device & Security Info
    val device_id: String? = null,
    val device_model: String? = null,
    val os_name: String? = null,
    val browser_name: String? = null,
    val language: String? = null,
    val connection_type: String? = null,
    val download_speed: String? = null,
    val cpu_cores: Int? = null,
    val screen_resolution: String? = null,
    val color_depth: Int? = null,
    val device_fingerprint: String? = null,
    val browser_fingerprint: String? = null,

    val last_ip_private: String? = null,
    val last_ip_public: String? = null,
    val location_lat: Double? = null,
    val location_lng: Double? = null,
    val location_accuracy: Double? = null,

    val last_login: String? = null,
    val created_at: String? = null
)

@Serializable
data class Transaction(
    val id: String? = null,
    val user_id: String,
    val service_id: Int? = null,
    val amount: Double,
    val fee: Double = 0.0,
    val total_amount: Double,
    val status: String = "pending",
    val external_id: String? = null,
    val response_data: JsonElement? = null,
    val location_lat: Double? = null,
    val location_lng: Double? = null,
    val ip_address: String? = null,
    val created_at: String? = null
)

@Serializable
data class DepositRequest(
    val id: Int? = null,
    val user_id: String,
    val bank_name: String,
    val amount: Double,
    val depositor_name: String,
    val order_number: String? = null,
    val receipt_image_url: String? = null,
    val notes: String? = null,
    val status: String = "pending",
    val created_at: String? = null
)

@Serializable
data class Ticket(
    val id: Int? = null,
    val user_id: String,
    val ticket_type: String,
    val invoice_number: String? = null,
    val subject: String,
    val message: String,
    val status: String = "open",
    val created_at: String? = null
)

@Serializable
data class AuditLog(
    val id: Int? = null,
    val user_id: String?,
    val action: String,
    val details: JsonElement,
    val created_at: String? = null
)

// e-misr models
@Serializable
data class EMisrService(
    val id: String,
    val sid: String,
    val srv: String? = null,
    val name: String,
    val ico: String,
    val inp: List<EMisrInput>? = null,
    val lst: List<EMisrListOption>? = null,
    val sel: List<EMisrSelection>? = null
)

@Serializable
data class EMisrInput(
    val name: String,
    val type: String,
    val title: String? = null,
    val rex: String? = null
)

@Serializable
data class EMisrListOption(
    val id: String? = null,
    val value: String,
    val title: String
)

@Serializable
data class EMisrSelection(
    val title: String,
    val value: String,
    val vsa: Double? = null,
    val pvsa: Double? = null
)
