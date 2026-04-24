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
    val initialServices = listOf<EMisrService>(
        EMisrService(id = "1", sid = "101", name = "فودافون شحن", srv = "Vodafone", ico = "topup"),
        EMisrService(id = "2", sid = "102", name = "أورانج شحن", srv = "Orange", ico = "topup"),
        EMisrService(id = "3", sid = "103", name = "اتصالات شحن", srv = "Etisalat", ico = "topup"),
        EMisrService(id = "4", sid = "104", name = "وي شحن", srv = "WE", ico = "topup"),
        EMisrService(id = "10", sid = "201", name = "فاتورة الكهرباء", srv = "EEHC", ico = "bills"),
        EMisrService(id = "11", sid = "202", name = "فاتورة المياه", srv = "HCWW", ico = "bills"),
        EMisrService(id = "12", sid = "203", name = "فاتورة الغاز", srv = "Petrotrade", ico = "bills")
    )
}

data class ServiceCategory(
    val id: String,
    val name: String,
    val icon: String
)

data class EMisrService(
    val id: String,
    val sid: String,
    val name: String,
    val srv: String,
    val ico: String
)
