package dr.pay.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dr.pay.app.data.EMisrService
import dr.pay.app.ui.viewmodel.ServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    category: String,
    onServiceClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ServiceViewModel = viewModel()
) {
    val services by viewModel.services.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredServices = services.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("خدمات $category") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("بحث عن خدمة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            LazyColumn {
                items(filteredServices) { service ->
                    ListItem(
                        headlineContent = { Text(service.name) },
                        supportingContent = { Text("ID: ${service.sid}") },
                        modifier = Modifier.clickable { onServiceClick(service.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
