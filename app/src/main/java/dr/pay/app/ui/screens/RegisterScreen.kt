package dr.pay.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import dr.pay.app.ui.viewmodel.AuthState
import dr.pay.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("merchant") }
    var idCardFrontUri by remember { mutableStateOf<Uri?>(null) }
    var idCardBackUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        idCardFrontUri = uri
    }
    val backLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        idCardBackUri = uri
    }

    // استخراج المحافظة من الرقم القومي
    fun getProvinceFromNationalId(id: String): String {
        if (id.length < 9) return ""
        val code = id.substring(7, 9)
        return when (code) {
            "01" -> "القاهرة"
            "02" -> "الإسكندرية"
            "03" -> "بور سعيد"
            "04" -> "السويس"
            "11" -> "دمياط"
            "12" -> "الدقهلية"
            "13" -> "الشرقية"
            "14" -> "القليوبية"
            "15" -> "كفر الشيخ"
            "16" -> "الغربية"
            "17" -> "المنوفية"
            "18" -> "البحيرة"
            "19" -> "الإسماعيلية"
            "21" -> "الجيزة"
            "22" -> "بني سويف"
            "23" -> "الفيوم"
            "24" -> "المنيا"
            "25" -> "أسيوط"
            "26" -> "سوهاج"
            "27" -> "قنا"
            "28" -> "أسوان"
            "29" -> "الأقصر"
            "31" -> "البحر الأحمر"
            "32" -> "الوادي الجديد"
            "33" -> "مطروح"
            "34" -> "شمال سيناء"
            "35" -> "جنوب سيناء"
            "88" -> "خارج الجمهورية"
            else -> ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إنشاء حساب جديد") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("نوع الحساب", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                FilterChip(
                    selected = selectedRole == "merchant",
                    onClick = { selectedRole = "merchant" },
                    label = { Text("تاجر") }
                )
                FilterChip(
                    selected = selectedRole == "distributor",
                    onClick = { selectedRole = "distributor" },
                    label = { Text("موزع") }
                )
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("الاسم الكامل") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { if (it.length <= 11) phoneNumber = it },
                label = { Text("رقم الهاتف") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = nationalId,
                onValueChange = { 
                    if (it.length <= 14) {
                        nationalId = it
                        if (it.length >= 9) {
                            province = getProvinceFromNationalId(it)
                        }
                    }
                },
                label = { Text("الرقم القومي (14 رقم)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            OutlinedTextField(
                value = province,
                onValueChange = { province = it },
                label = { Text("المحافظة") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                readOnly = province.isNotEmpty() // حماية إذا تم استخراجها تلقائياً
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("العنوان بالتفصيل") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("صور البطاقة الشخصية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // وجه البطاقة
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable { frontLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (idCardFrontUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(idCardFrontUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                                Text("وجه البطاقة", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // ظهر البطاقة
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                            .clickable { backLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (idCardBackUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(idCardBackUri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray)
                                Text("ظهر البطاقة", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = when {
                        errorMessage!!.contains("phone") -> "رقم الهاتف مسجل مسبقاً"
                        errorMessage!!.contains("national") -> "الرقم القومي مسجل مسبقاً"
                        else -> errorMessage!!
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Button(
                onClick = {
                    if (fullName.isEmpty()) { errorMessage = "يرجى إدخال الاسم الكامل"; return@Button }
                    if (phoneNumber.length < 11) { errorMessage = "رقم الهاتف غير صحيح"; return@Button }
                    if (nationalId.length < 14) { errorMessage = "الرقم القومي يجب أن يكون 14 رقم"; return@Button }
                    if (idCardFrontUri == null) { errorMessage = "يرجى اختيار صورة وجه البطاقة"; return@Button }
                    if (idCardBackUri == null) { errorMessage = "يرجى اختيار صورة ظهر البطاقة"; return@Button }
                    
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val profile = dr.pay.app.data.Profile(
                            id = "", 
                            phone_number = phoneNumber,
                            full_name = fullName,
                            national_id = nationalId,
                            province = province,
                            address = address,
                            user_type = selectedRole,
                            balance = 0.0,
                            is_active = false,
                            id_card_front_url = idCardFrontUri.toString(),
                            id_card_back_url = idCardBackUri.toString()
                        )
                        authViewModel.signUp(phoneNumber, password, profile, emptyMap())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("إنشاء حساب")
            }

            // مراقبة حالة التسجيل
            val authState by authViewModel.authState.collectAsState()
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Authenticated -> onRegisterSuccess()
                    is AuthState.Error -> {
                        errorMessage = (authState as AuthState.Error).message
                        isLoading = false
                    }
                    else -> {}
                }
            }
        }
    }
}
