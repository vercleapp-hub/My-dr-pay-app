package dr.pay.app.ui.screens.services

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dr.pay.app.data.ServiceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicePaymentScreen(
    service: ServiceItem,
    viewModel: ServiceViewModel,
    onNavigateBack: () -> Unit
) {
    var targetNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(service.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // كارت تفاصيل الخدمة
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("القسم: ${service.category}", fontSize = 14.sp)
                    Text("العمولة: ${service.commission} ج.م", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            // مدخلات العملية
            OutlinedTextField(
                value = targetNumber,
                onValueChange = { if (it.length <= 11) targetNumber = it },
                label = { Text("رقم الهاتف / رقم الحساب") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.PhoneIphone, null) }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("المبلغ (ج.م)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(32.dp))

            // معالجة الحالات
            when (val state = uiState) {
                is ServiceUiState.Processing -> CircularProgressIndicator()
                is ServiceUiState.Success -> {
                    Text("✅ نجحت العملية!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    Text("رقم المرجع: ${state.referenceId}")
                    Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 16.dp)) {
                        Text("العودة للرئيسية")
                    }
                }
                is ServiceUiState.Error -> {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    Button(
                        onClick = { 
                            viewModel.performPayment(service, targetNumber, amount.toDoubleOrNull() ?: 0.0) 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = targetNumber.isNotEmpty() && amount.isNotEmpty()
                    ) {
                        Text("تأكيد ودفع الآن", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
