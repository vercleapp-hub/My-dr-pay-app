package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.data.Transaction
import dr.pay.app.ui.components.DynamicServiceForm
import dr.pay.app.ui.viewmodel.AuthViewModel
import dr.pay.app.ui.viewmodel.ServiceViewModel
import dr.pay.app.ui.viewmodel.TransactionState
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormScreen(
    serviceId: String,
    onBack: () -> Unit,
    serviceViewModel: ServiceViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val services by serviceViewModel.services.collectAsState()
    val service = services.find { it.id == serviceId }
    val profile by authViewModel.profile.collectAsState()
    val transactionState by serviceViewModel.transactionState.collectAsState()
    
    var formValues by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var amount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(service?.name ?: "طلب خدمة") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (service == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                DynamicServiceForm(
                    service = service,
                    onValueChange = { formValues = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("المبلغ المراد دفعه") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        profile?.let { user ->
                            val trans = Transaction(
                                user_id = user.id,
                                service_id = service.sid.toIntOrNull(),
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                total_amount = amount.toDoubleOrNull() ?: 0.0, // Should include fees
                                response_data = buildJsonObject {
                                    formValues.forEach { (k, v) -> put(k, v) }
                                }
                            )
                            serviceViewModel.executeTransaction(trans)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = transactionState !is TransactionState.Loading && amount.isNotEmpty()
                ) {
                    if (transactionState is TransactionState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("تأكيد الدفع")
                    }
                }

                if (transactionState is TransactionState.Success) {
                    AlertDialog(
                        onDismissRequest = { /* No-op */ },
                        confirmButton = {
                            Button(onClick = { onBack() }) { Text("حسناً") }
                        },
                        title = { Text("تمت العملية بنجاح") },
                        text = { Text("رقم العملية: ${(transactionState as TransactionState.Success).transaction.id}") }
                    )
                }
                
                if (transactionState is TransactionState.Error) {
                    Text(
                        text = (transactionState as TransactionState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
