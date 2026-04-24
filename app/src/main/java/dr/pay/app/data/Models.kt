package dr.pay.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val full_name: String = "",
    val phone_number: String = "",
    val national_id: String = "",
    val province: String = "",
    val address: String = "",
    val id_card_front_url: String? = null,
    val id_card_back_url: String? = null,
    val is_active: Boolean = false,
    val role: String = "merchant",
    val device_id: String? = null,
    val last_ip_public: String? = null,
    val device_model: String? = null,
    val created_at: String? = null
)

@Serializable
data class ServiceItem(
    val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val provider: String = "e-misr"
)
