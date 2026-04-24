package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.data.Ticket
import dr.pay.app.ui.viewmodel.AuthViewModel
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val profile by authViewModel.profile.collectAsState()
    val scope = rememberCoroutineScope()
    
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var ticketType by remember { mutableStateOf("عام") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الدعم الفني") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("فتح تذكرة دعم جديدة", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("الموضوع") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("رسالتك") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            Button(
                onClick = {
                    profile?.let { user ->
                        isLoading = true
                        scope.launch {
                            try {
                                val ticket = Ticket(
                                    user_id = user.id,
                                    ticket_type = ticketType,
                                    subject = subject,
                                    message = message
                                )
                                supabase.postgrest.from("tickets").insert(ticket)
                                onBack()
                            } catch (e: Exception) {
                                // Error
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && subject.isNotEmpty() && message.isNotEmpty()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("إرسال التذكرة")
            }
        }
    }
}
