package dr.pay.app.util

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseConfig {
    const val URL = "https://wwkllpornhzwovsvyikv.supabase.co"
    const val KEY = "sb_publishable_QcDiCoyD921ZNJ-pBd_KKw_LgQzeIpU"
}

val supabase = createSupabaseClient(
    supabaseUrl = SupabaseConfig.URL,
    supabaseKey = SupabaseConfig.KEY
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}
