package dr.pay.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.BackHandler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dr.pay.app.ui.screens.*
import dr.pay.app.ui.theme.MyApplicationDrpayAppTheme
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationDrpayAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CAMERA
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissions.add(Manifest.permission.BLUETOOTH_SCAN)
    } else {
        permissions.add(Manifest.permission.BLUETOOTH)
        permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest)
        }
    }

    var showExitDialog by remember { mutableStateOf(false) }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("تأكيد الخروج") },
            text = { Text("هل تريد إغلاق التطبيق؟") },
            confirmButton = {
                Button(onClick = { exitProcess(0) }) {
                    Text("خروج")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") { 
                BackHandler { showExitDialog = true }
                LoginScreen(
                    onLoginSuccess = { 
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = { 
                        navController.navigate("login") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }
            composable("dashboard") {
                BackHandler { showExitDialog = true }
                UserDashboard(
                    onNavigateToService = { category -> navController.navigate("services/$category") },
                    onNavigateToDeposit = { navController.navigate("deposit") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToSupport = { navController.navigate("support") },
                    onNavigateToMerchantTree = { navController.navigate("merchant_tree") },
                    onNavigateToAdmin = { navController.navigate("admin") },
                    onLogout = { 
                        navController.navigate("login") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )
            }
            composable("services/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                ServiceListScreen(
                    category = category,
                    onServiceClick = { serviceId -> navController.navigate("service_form/$serviceId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("service_form/{serviceId}") { backStackEntry ->
                val serviceId = backStackEntry.arguments?.getString("serviceId") ?: ""
                ServiceFormScreen(
                    serviceId = serviceId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("deposit") {
                DepositScreen(onBack = { navController.popBackStack() })
            }
            composable("history") {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable("support") {
                SupportScreen(onBack = { navController.popBackStack() })
            }
            composable("merchant_tree") {
                MerchantTreeScreen(onBack = { navController.popBackStack() })
            }
            composable("admin") {
                AdminDashboard(onBack = { navController.popBackStack() })
            }
        }
    }
}
