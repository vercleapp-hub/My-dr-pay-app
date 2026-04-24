package dr.pay.app.util

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.realtime.Realtime

val supabase = createSupabaseClient(
    supabaseUrl = "https://wwkllpornhzwovsvyikv.supabase.co",
    supabaseKey = "sb_publishable_QcDiCoyD921ZNJ-pBd_KKw_LgQzeIpU"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
    install(Realtime)
}
