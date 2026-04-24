package dr.pay.app.data

object ServicesData {
    val categories = listOf(
        ServiceCategory("topup", "شحن رصيد", "📱"),
        ServiceCategory("bills", "فواتير مرافق", "⚡"),
        ServiceCategory("internet", "إنترنت ومنزلي", "🌐"),
        ServiceCategory("games", "ألعاب وترفيه", "🎮"),
        ServiceCategory("education", "خدمات تعليمية", "🎓"),
        ServiceCategory("insurance", "تأمين ونقابات", "🛡️")
    )

    // هذه البيانات سيتم رفعها لـ Supabase لتعمل كـ Cache أو مرجع
    val initialServices = listOf(
        EMisrService(id = "1", name = "فودافون شحن", category = "topup", provider = "Vodafone", code = "101"),
        EMisrService(id = "2", name = "أورانج شحن", category = "topup", provider = "Orange", code = "102"),
        EMisrService(id = "3", name = "اتصالات شحن", category = "topup", provider = "Etisalat", code = "103"),
        EMisrService(id = "4", name = "وي شحن", category = "topup", provider = "WE", code = "104"),
        EMisrService(id = "10", name = "فاتورة الكهرباء", category = "bills", provider = "EEHC", code = "201"),
        EMisrService(id = "11", name = "فاتورة المياه", category = "bills", provider = "HCWW", code = "202"),
        EMisrService(id = "12", name = "فاتورة الغاز", category = "bills", provider = "Petrotrade", code = "203")
    )
}

data class ServiceCategory(
    val id: String,
    val name: String,
    val icon: String
)
