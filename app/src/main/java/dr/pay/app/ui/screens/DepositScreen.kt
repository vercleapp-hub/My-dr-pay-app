package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.data.DepositRequest
import dr.pay.app.ui.viewmodel.AuthViewModel
import dr.pay.app.data.repository.DepositRepository
import kotlinx.coroutines.launch

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val profile by authViewModel.profile.collectAsState()
    val scope = rememberCoroutineSafe()
    val depositRepo = remember { DepositRepository() }
    
    var bankName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var depositorName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("طلب إيداع بنكي") }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("اسم البنك") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("المبلغ") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = depositorName,
                onValueChange = { depositorName = it },
                label = { Text("اسم المودع") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("ملاحظات") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    profile?.let { user ->
                        isLoading = true
                        scope.launch {
                            val request = DepositRequest(
                                user_id = user.id,
                                bank_name = bankName,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                depositor_name = depositorName,
                                notes = notes
                            )
                            depositRepo.createDeposit(request)
                            isLoading = false
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && bankName.isNotEmpty() && amount.isNotEmpty()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("إرسال الطلب")
            }
        }
    }
}

@Composable
fun rememberCoroutineSafe() = rememberCoroutineScope()
