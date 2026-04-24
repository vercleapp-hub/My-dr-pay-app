package dr.pay.app.ui.screens.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dr.pay.app.data.repository.WalletRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(walletRepo: WalletRepository, onNavigateBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("InstaPay") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("طلب شحن رصيد") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text("اختر طريقة التحويل", fontWeight = FontWeight.Bold)
            val methods = listOf("InstaPay", "Vodafone Cash", "Etisalat Cash", "Bank Transfer")
            
            methods.forEach { method ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = (selectedMethod == method), onClick = { selectedMethod = method })
                    Text(method)
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("المبلغ المراد شحنه (ج.م)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // كارت رفع الصورة
            Card(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                onClick = { /* هنا يتم استدعاء ملف من الهاتف */ }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(48.dp))
                    Text("اضغط لرفع صورة الإيصال")
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        val res = walletRepo.requestDeposit(amount.toDoubleOrNull() ?: 0.0, selectedMethod, "fake_image_url")
                        isSubmitting = false
                        if (res.isSuccess) {
                            snackbarHostState.showSnackbar("تم إرسال طلبك بنجاح! بانتظار موافقة الأدمن.")
                            onNavigateBack()
                        } else {
                            snackbarHostState.showSnackbar("فشل الإرسال: ${res.exceptionOrNull()?.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = amount.isNotEmpty() && !isSubmitting
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White)
                else Text("إرسال الطلب للمراجعة", fontSize = 18.sp)
            }
        }
    }
}
