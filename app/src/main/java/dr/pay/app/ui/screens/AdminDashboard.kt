package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val pendingProfiles by viewModel.pendingProfiles.collectAsState()
    val pendingDeposits by viewModel.pendingDeposits.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم المسؤول") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.fetchPendingRequests() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("تفعيل التجار (${pendingProfiles.size})", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("طلبات الإيداع (${pendingDeposits.size})", modifier = Modifier.padding(16.dp))
                }
            }

            if (selectedTab == 0) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(pendingProfiles) { profile ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("الاسم: ${profile.full_name}", style = MaterialTheme.typography.titleMedium)
                                Text("الهاتف: ${profile.phone_number}")
                                Text("المحافظة: ${profile.province}")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(
                                        onClick = { viewModel.approveProfile(profile.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("تفعيل الحساب")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(pendingDeposits) { deposit ->
                        ElevatedCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("المودع: ${deposit.depositor_name}", style = MaterialTheme.typography.titleMedium)
                                Text("المبلغ: ${deposit.amount} EGP", color = MaterialTheme.colorScheme.primary)
                                Text("البنك: ${deposit.bank_name}")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Button(
                                        onClick = { viewModel.approveDeposit(deposit.id!!) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("تأكيد الإيداع")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
