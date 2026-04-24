package dr.pay.app.data.repository

import dr.pay.app.data.EMisrService
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServiceRepository {

    suspend fun getAllServices(): List<EMisrService> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("services").select().decodeList<EMisrService>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getServiceById(id: String): EMisrService? = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest.from("services").select {
                filter {
                    eq("id", id)
                }
            }.decodeSingle<EMisrService>()
        } catch (e: Exception) {
            null
        }
    }
}
