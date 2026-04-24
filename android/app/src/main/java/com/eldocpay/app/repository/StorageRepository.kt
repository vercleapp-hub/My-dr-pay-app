package com.eldocpay.app.repository

import android.net.Uri
import com.eldocpay.app.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StorageRepository {

    private val kycBucket = SupabaseClient.client.storage["kyc"]
    private val depositsBucket = SupabaseClient.client.storage["deposits"]

    suspend fun uploadKycImage(userId: String, imageUri: Uri, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(imageUri.path!!)
                val path = "$userId/$fileName"

                val result = kycBucket.upload(path, file.readBytes())
                result.path
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun uploadDepositImage(userId: String, imageUri: Uri, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(imageUri.path!!)
                val path = "$userId/$fileName"

                val result = depositsBucket.upload(path, file.readBytes())
                result.path
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getPublicUrl(bucket: String, path: String): String {
        return when (bucket) {
            "kyc" -> kycBucket.publicUrl(path)
            "deposits" -> depositsBucket.publicUrl(path)
            else -> ""
        }
    }
}