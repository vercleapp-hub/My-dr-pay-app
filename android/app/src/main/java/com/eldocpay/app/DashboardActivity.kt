package com.eldocpay.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.eldocpay.app.databinding.ActivityDashboardBinding
import com.eldocpay.app.repository.UserRepository
import com.eldocpay.app.repository.WalletRepository
import com.eldocpay.app.viewmodel.DashboardViewModel

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private val userRepository = UserRepository()
    private val walletRepository = WalletRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
        loadData()
    }

    private fun setupUI() {
        binding.btnRecharge.setOnClickListener {
            // Navigate to mobile recharge
            startActivity(Intent(this, ServiceActivity::class.java).apply {
                putExtra("service_type", "mobile_recharge")
            })
        }

        binding.btnPayBill.setOnClickListener {
            // Navigate to bill payment
            startActivity(Intent(this, ServiceActivity::class.java).apply {
                putExtra("service_type", "bill_payment")
            })
        }

        binding.btnTransfer.setOnClickListener {
            // Navigate to transfer
            startActivity(Intent(this, TransferActivity::class.java))
        }

        binding.btnDeposit.setOnClickListener {
            // Navigate to deposit
            startActivity(Intent(this, DepositActivity::class.java))
        }

        binding.btnReports.setOnClickListener {
            // Navigate to reports
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        binding.btnComplaints.setOnClickListener {
            // Navigate to complaints
            startActivity(Intent(this, ComplaintsActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.dashboardState.observe(this, Observer { state ->
            when (state) {
                is DashboardViewModel.DashboardState.Loading -> {
                    // Show loading
                }
                is DashboardViewModel.DashboardState.Success -> {
                    binding.tvUserName.text = "مرحباً، ${state.userName}"
                    binding.tvBalance.text = "الرصيد: ${String.format("%.2f", state.balance)} ج.م"
                }
                is DashboardViewModel.DashboardState.Error -> {
                    // Show error
                }
            }
        })
    }

    private fun loadData() {
        val userId = SupabaseClient.client.auth.currentUser?.id ?: return
        viewModel.loadDashboardData(userId)
    }
}