package dr.pay.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import dr.pay.app.data.AuditLog
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.NetworkInterface
import java.util.*

class TelemetryManager(private val context: Context) {

    suspend fun getFullTelemetry(): Map<String, Any?> {
        val metrics = context.resources.displayMetrics
        return mapOf(
            "os_name" to "Android ${Build.VERSION.RELEASE}",
            "device_model" to "${Build.BRAND} ${Build.MODEL}",
            "device_id" to Build.ID,
            "cpu_cores" to Runtime.getRuntime().availableProcessors(),
            "screen_resolution" to "${metrics.widthPixels}x${metrics.heightPixels}",
            "color_depth" to 24, // الافتراضي في أندرويد
            "language" to Locale.getDefault().language,
            "connection_type" to getConnectionType(),
            "download_speed" to getDownloadSpeed(),
            "last_ip_public" to getIPAddress(),
            "device_fingerprint" to generateFingerprint()
        )
    }

    private fun generateFingerprint(): String {
        return btoa("${Build.BOARD}-${Build.BRAND}-${Build.DEVICE}-${Build.DISPLAY}-${Build.HOST}-${Build.ID}-${Build.MANUFACTURER}-${Build.MODEL}-${Build.PRODUCT}-${Build.TAGS}-${Build.TYPE}-${Build.USER}")
    }

    private fun btoa(str: String): String {
        return android.util.Base64.encodeToString(str.toByteArray(), android.util.Base64.NO_WRAP)
    }

    private fun getIPAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in Collections.list(interfaces)) {
                for (addr in Collections.list(intf.inetAddresses)) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        if (sAddr.indexOf(':') < 0) return sAddr
                    }
                }
            }
        } catch (e: Exception) {}
        return "0.0.0.0"
    }

    private fun getConnectionType(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork ?: return "None"
        val actNw = cm.getNetworkCapabilities(nw) ?: return "None"
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "Unknown"
        }
    }

    private fun getDownloadSpeed(): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = cm.activeNetwork
        val actNw = cm.getNetworkCapabilities(nw)
        val speedKbps = actNw?.linkDownstreamBandwidthKbps ?: 0
        return "${speedKbps / 1000} Mbps"
    }
}
