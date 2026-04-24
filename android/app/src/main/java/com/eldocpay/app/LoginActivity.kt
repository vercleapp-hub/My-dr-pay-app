package com.eldocpay.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.eldocpay.app.databinding.ActivityLoginBinding
import com.eldocpay.app.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال رقم الهاتف وكلمة المرور", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(phone, password)
        }

        binding.btnRegister.setOnClickListener {
            // الانتقال إلى شاشة التسجيل
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this, Observer { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.btnLogin.text = "جاري تسجيل الدخول..."
                }
                is LoginViewModel.LoginState.Success -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "تسجيل الدخول"
                    Toast.makeText(this, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                    // الانتقال إلى Dashboard
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                is LoginViewModel.LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "تسجيل الدخول"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                is LoginViewModel.LoginState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "تسجيل الدخول"
                }
            }
        })
    }
}