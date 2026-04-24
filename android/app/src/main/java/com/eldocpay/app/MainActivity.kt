package com.eldocpay.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.eldocpay.app.databinding.ActivityMainBinding
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val session = SupabaseClient.client.auth.currentSessionOrNull()
                if (session != null) {
                    // المستخدم مسجل دخول، انتقل إلى Dashboard
                    startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                } else {
                    // غير مسجل، انتقل إلى Login
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
                finish()
            } catch (e: Exception) {
                // في حالة خطأ، انتقل إلى Login
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}