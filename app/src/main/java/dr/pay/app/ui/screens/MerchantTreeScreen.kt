package dr.pay.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.data.Profile
import dr.pay.app.ui.viewmodel.AuthViewModel
import dr.pay.app.util.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantTreeScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val profile by authViewModel.profile.collectAsState()
    var subMerchants by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile) {
        profile?.let { parent ->
            try {
                subMerchants = supabase.postgrest.from("profiles")
                    .select { filter { eq("parent_id", parent.id) } }
                    .decodeList<Profile>()
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("شجرة التجار") },
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
                item {
                    Text(
                        "التجار التابعين لك (${subMerchants.size})",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(subMerchants) { merchant ->
                    ListItem(
                        headlineContent = { Text(merchant.full_name ?: "تاجر بدون اسم") },
                        supportingContent = { Text("الرصيد: ${merchant.balance} EGP") },
                        leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                        trailingContent = {
                            Text(if (merchant.is_active) "نشط" else "غير نشط", 
                                color = if (merchant.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
