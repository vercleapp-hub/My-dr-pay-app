package com.eldocpay.app

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    // استبدل هذا بـ Supabase URL و Anon Key الخاص بك
    private const val SUPABASE_URL = "https://wwkllpornhzwovsvyikv.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_QcDiCoyD921ZNJ-pBd_KKw_LgQzeIpU"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    // دالة لتحويل رقم الهاتف إلى Email
    fun phoneToEmail(phone: String): String {
        // إزالة أي رموز غير رقمية وإضافة u في البداية
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return "u${cleanPhone}@drpay.app"
    }

    // دالة للتحقق من صحة رقم الهاتف المصري
    fun isValidEgyptianPhone(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return cleanPhone.length == 11 && cleanPhone.startsWith("01")
    }
}