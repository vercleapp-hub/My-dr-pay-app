package dr.pay.app.util

import android.content.Context
import android.os.Build
import dr.pay.app.data.AuditLog
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.NetworkInterface
import java.util.Collections

class TelemetryManager(private val context: Context) {

    suspend fun logAction(action: String, details: Map<String, String> = emptyMap()) {
        val userId = supabase.auth.currentUserOrNull()?.id
        val deviceFingerprint = getDeviceFingerprint()
        
        val logDetails = buildJsonObject {
            details.forEach { (k, v) -> put(k, v) }
            put("device_model", Build.MODEL)
            put("device_brand", Build.BRAND)
            put("os_version", Build.VERSION.RELEASE)
            put("os_name", "Android")
            put("fingerprint", deviceFingerprint)
            put("ip_public", getIPAddress()) // في بيئة حقيقية سنستخدم API خارجي للـ Public IP
            put("cpu_cores", Runtime.getRuntime().availableProcessors())
            put("screen_res", context.resources.displayMetrics.let { "${it.widthPixels}x${it.heightPixels}" })
            put("connection_type", getConnectionType())
        }

        val auditLog = AuditLog(
            user_id = userId,
            action = action,
            details = logDetails
        )

        try {
            supabase.postgrest.from("audit_logs").insert(auditLog)
        } catch (e: Exception) {
            // Silently fail telemetry in production or log to crashlytics
        }
    }

    private fun getDeviceFingerprint(): String {
        return "${Build.BRAND}-${Build.MODEL}-${Build.ID}"
    }

    private fun getIPAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) return sAddr
                    }
                }
            }
        } catch (ex: Exception) { }
        return "0.0.0.0"
    }

    private fun getConnectionType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return when (activeNetwork?.type) {
            android.net.ConnectivityManager.TYPE_WIFI -> "WiFi"
            android.net.ConnectivityManager.TYPE_MOBILE -> "Mobile Data"
            android.net.ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
            else -> "Unknown"
        }
    }
}
