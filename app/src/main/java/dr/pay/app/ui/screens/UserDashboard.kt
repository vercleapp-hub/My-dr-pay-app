package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.Logout
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.ui.viewmodel.AuthViewModel

@Composable
fun UserDashboard(
    onNavigateToService: (String) -> Unit,
    onNavigateToDeposit: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToMerchantTree: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val profile by authViewModel.profile.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Top Bar / User Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("مرحباً،", style = MaterialTheme.typography.bodyMedium)
                Text(profile?.full_name ?: "مستخدم", style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = onLogout) {
                Icon(Icons.Default.Logout, contentDescription = "Logout")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text("الرصيد المتاح", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Text("${profile?.balance ?: 0.0} EGP", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                Text(
                    "كود التاجر: ${profile?.merchant_code ?: "---"}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.BottomStart)
                )
                if (profile?.is_active == true) {
                    Surface(
                        color = Color(0xFF4CAF50),
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("نشط", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("الخدمات الرئيسية", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            item { DashboardItem("شحن رصيد", Icons.Default.PhoneAndroid) { onNavigateToService("topup") } }
            item { DashboardItem("تحويل رصيد", Icons.Default.Send) { onNavigateToService("transfer") } }
            item { DashboardItem("تقارير مالية", Icons.Default.Assessment) { onNavigateToHistory() } }
            item { DashboardItem("عرض العمليات", Icons.Default.History) { onNavigateToHistory() } }
            item { DashboardItem("طلب إيداع", Icons.Default.AccountBalance) { onNavigateToDeposit() } }
            item { DashboardItem("شجرة التجار", Icons.Default.AccountTree) { onNavigateToMerchantTree() } }
            item { DashboardItem("الدعم الفني", Icons.Default.SupportAgent) { onNavigateToSupport() } }
            if (profile?.user_type == "admin") {
                item { DashboardItem("لوحة المسؤول", Icons.Default.AdminPanelSettings) { onNavigateToAdmin() } }
            }
        }
    }
}

@Composable
fun DashboardItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title)
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}
