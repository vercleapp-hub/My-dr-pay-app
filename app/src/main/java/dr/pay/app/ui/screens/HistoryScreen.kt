package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.data.Transaction
import dr.pay.app.ui.viewmodel.AuthViewModel
import dr.pay.app.data.repository.TransactionRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val profile by authViewModel.profile.collectAsState()
    val transactionRepo = remember { TransactionRepository() }
    var transactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile) {
        profile?.let {
            transactions = transactionRepo.getTransactions(it.id)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل العمليات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    ListItem(
        headlineContent = { Text("خدمة: ${transaction.service_id ?: "تحويل"}") },
        supportingContent = { Text(transaction.created_at ?: "") },
        trailingContent = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    "${transaction.total_amount} EGP",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.status == "success") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                Text(
                    when(transaction.status) {
                        "success" -> "ناجحة"
                        "pending" -> "معلقة"
                        else -> "فاشلة"
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}
