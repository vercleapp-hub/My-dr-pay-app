package dr.pay.app.util

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    private const val TIMEZONE_CAIRO = "Africa/Cairo"
    private const val DATE_FORMAT = "yyyy-MM-dd hh:mm:ss a"

    fun getCurrentCairoTime(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale("ar", "EG"))
        sdf.timeZone = TimeZone.getTimeZone(TIMEZONE_CAIRO)
        return sdf.format(Date())
    }

    fun formatToCairoTime(date: Date): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale("ar", "EG"))
        sdf.timeZone = TimeZone.getTimeZone(TIMEZONE_CAIRO)
        return sdf.format(date)
    }
}
