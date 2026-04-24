package dr.pay.app.ui.screens.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dr.pay.app.data.Profile
import dr.pay.app.data.ServiceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ELDoctor Pay", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadDashboardData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DashboardUiState.Success -> {
                    DashboardContent(state.profile, state.services)
                }
                is DashboardUiState.Error -> {
                    Text(state.message, color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun DashboardContent(profile: Profile, services: List<ServiceItem>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. كارت المحفظة
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("مرحباً، ${profile.full_name}", fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text("رصيدك الحالي", fontWeight = FontWeight.Bold)
                Text(
                    text = "${String.format("%.2f", profile.balance)} ج.م",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* فتح واجهة الإيداع */ }) {
                        Icon(Icons.Default.Add, null)
                        Text("إيداع رصيد")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 2. شبكة الخدمات
        Text("خدمات الدفع", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(12.dp))
        
        // استخدام LazyVerticalGrid بدلاً من FlowRow لترتيب أدق
        val gridHeight = if (services.size > 3) 240.dp else 120.dp
        Box(modifier = Modifier.height(gridHeight)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(services) { service ->
                    ServiceButton(service)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 3. آخر العمليات (نموذج)
        Text("آخر العمليات", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        repeat(3) {
            TransactionItem()
        }
    }
}

@Composable
fun ServiceButton(service: ServiceItem) {
    Card(
        onClick = { /* تنفيذ الخدمة */ },
        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn, // سيتم ربطه بأيقونة الخدمة لاحقاً
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(service.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun TransactionItem() {
    ListItem(
        headlineContent = { Text("شحن فودافون") },
        supportingContent = { Text("منذ ساعتين • ناجحة") },
        trailingContent = { Text("-20.00 ج.م", color = Color.Red, fontWeight = FontWeight.Bold) },
        leadingContent = { Icon(Icons.Default.History, null) }
    )
}
