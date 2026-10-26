package com.example.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.KalyanpurViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    isHindi: Boolean,
    viewModel: KalyanpurViewModel,
    projects: List<Project>,
    donors: List<Donor>,
    expenses: List<Expense>,
    notices: List<Notice>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pinInput by remember { mutableStateOf("") }
    var isAdminLoggedIn by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    // activeAdminMode: null = Yojana list, "ADD" = Add Yojana, Project = Manage specific Yojana
    var activeAdminMode by remember { mutableStateOf<Any?>(null) }
    var isEditingYojana by remember { mutableStateOf(false) }

    // New/Edit Yojana Form States
    var yojanaTitle by remember { mutableStateOf("") }
    var yojanaDesc by remember { mutableStateOf("") }
    var yojanaCategory by remember { mutableStateOf("विकास योजना (Development Yojana)") }
    var yojanaBudget by remember { mutableStateOf("") }
    var yojanaDate by remember { mutableStateOf("14 Oct 2026") }
    var yojanaStatus by remember { mutableStateOf("प्रगति पर (Ongoing)") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    var imageZoomScale by remember { mutableStateOf(1f) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // Chanda Form States in Yojana Detail
    var donorName by remember { mutableStateOf("") }
    var donorFatherName by remember { mutableStateOf("") }
    var donorMobile by remember { mutableStateOf("") }
    var donorAddress by remember { mutableStateOf("") }
    var donorAmount by remember { mutableStateOf("") }
    var donorDate by remember { mutableStateOf("14 Oct 2026") }
    var paymentMode by remember { mutableStateOf("नकद (Cash)") }

    val registeredDonors by viewModel.registeredDonors.collectAsState()

    // Master Registered Donor Form States
    var regDonorName by remember { mutableStateOf("") }
    var regDonorFatherName by remember { mutableStateOf("") }
    var regDonorMobile by remember { mutableStateOf("") }
    var regDonorAddress by remember { mutableStateOf("") }
    var editingRegDonorId by remember { mutableStateOf<String?>(null) }
    var regDonorSearchQuery by remember { mutableStateOf("") }
    var showDonorSelectDialog by remember { mutableStateOf(false) }
    var donorSelectSearchQuery by remember { mutableStateOf("") }

    // Expense Form States in Yojana Detail
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf("14 Oct 2026") }
    var billRef by remember { mutableStateOf("Bill #1") }
    var selectedBillUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingBill by remember { mutableStateOf(false) }

    // Notice Form States
    var noticeTitle by remember { mutableStateOf("") }
    var noticeContent by remember { mutableStateOf("") }
    var noticeDate by remember { mutableStateOf("14 Oct 2026") }
    var noticePriority by remember { mutableStateOf("महत्वपूर्ण (Important)") }

    var selectedAdminTab by remember { mutableStateOf(0) } // 0 = Yojnas, 1 = Registered Donors, 2 = Notices
    var yojanaSubTab by remember { mutableStateOf(0) } // 0 = Chanda, 1 = Expenses

    var successMsg by remember { mutableStateOf("") }
    var successDialogMsg by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imageZoomScale = 1f
        }
    }

    val billPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedBillUri = uri
        }
    }

    val titleText = if (isHindi) "🔐 एडमिन नियंत्रण कक्ष (Admin Panel)" else "🔐 Admin Control Room"
    val subtitleText = if (isHindi) "योजनाएं प्रबंधित करें, संपादित करें तथा सूचनाएं प्रकाशित करें।" else "Manage yojnas, edit details & publish notices."

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (!isAdminLoggedIn) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (isHindi) "एडमिन सुरक्षा पिन" else "Admin Security PIN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it
                                pinError = false
                            },
                            label = { Text(if (isHindi) "पिन दर्ज करें (जैसे 1234)" else "Enter PIN (e.g. 1234)") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (pinError) {
                            Text(if (isHindi) "गलत पिन! (डिफ़ॉल्ट पिन: 1234)" else "Incorrect PIN! (Default PIN: 1234)", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (pinInput == "1234" || pinInput == "7860" || pinInput == "0000" || pinInput.lowercase() == "admin") {
                                    isAdminLoggedIn = true
                                    pinError = false
                                } else {
                                    pinError = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isHindi) "लॉगिन करें (Login)" else "Login")
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (isHindi) "स्थिति: एडमिन लॉगिन सक्रिय" else "Status: Admin Logged In", fontSize = 12.sp)
                            Text("Kalyanpur Panchayat Control", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        OutlinedButton(onClick = {
                            isAdminLoggedIn = false
                            pinInput = ""
                            activeAdminMode = null
                            isEditingYojana = false
                        }) {
                            Text(if (isHindi) "लॉग आउट" else "Sign Out")
                        }
                    }
                }
            }

            if (successMsg.isNotBlank()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f))) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = successMsg,
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            TextButton(onClick = { successDialogMsg = "" }) {
                                Text("X", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            }
                        }
                    }
                }
            }

            // Main Admin Tabs: 0 = Yojnas, 1 = Registered Donors, 2 = Notices
            if (activeAdminMode == null) {
                item {
                    TabRow(selectedTabIndex = selectedAdminTab) {
                        Tab(
                            selected = selectedAdminTab == 0,
                            onClick = { selectedAdminTab = 0 },
                            text = { Text(if (isHindi) "📋 योजनाएं" else "📋 Yojnas") }
                        )
                        Tab(
                            selected = selectedAdminTab == 1,
                            onClick = { selectedAdminTab = 1 },
                            text = { Text(if (isHindi) "👥 पंजीकृत दाता" else "👥 Donors") }
                        )
                        Tab(
                            selected = selectedAdminTab == 2,
                            onClick = { selectedAdminTab = 2 },
                            text = { Text(if (isHindi) "📢 सूचनाएं" else "📢 Notices") }
                        )
                    }
                }
            }

            when {
                activeAdminMode == "ADD" -> {
                    // Add New Yojana Form with 4:3 Image Picker & Zoom
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (isHindi) "➕ नई विकास योजना जोड़ें" else "➕ Add New Yojana", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    TextButton(onClick = { activeAdminMode = null }) {
                                        Text(if (isHindi) "← वापस" else "← Back")
                                    }
                                }

                                OutlinedTextField(value = yojanaTitle, onValueChange = { yojanaTitle = it }, label = { Text("योजना शीर्षक (Title)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = yojanaDesc, onValueChange = { yojanaDesc = it }, label = { Text("विवरण (Description)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = yojanaCategory, onValueChange = { yojanaCategory = it }, label = { Text("श्रेणी (Category)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = yojanaBudget, onValueChange = { yojanaBudget = it }, label = { Text("लक्ष्य बजट (Target Budget ₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                OutlinedTextField(value = yojanaDate, onValueChange = { yojanaDate = it }, label = { Text("दिनांक (Date)") }, modifier = Modifier.fillMaxWidth())

                                Text(if (isHindi) "योजना की फोटो (4:3 Ratio with Zoom)" else "Yojana Photo (4:3 Ratio)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(scaleX = imageZoomScale, scaleY = imageZoomScale)
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(if (isHindi) "कोई फोटो नहीं चुनी गई" else "No photo selected", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isHindi) "फोटो चुनें" else "Pick Photo")
                                    }
                                    if (selectedImageUri != null) {
                                        OutlinedButton(onClick = { selectedImageUri = null }, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                                            Text(if (isHindi) "हटाएं" else "Remove")
                                        }
                                    }
                                }

                                if (selectedImageUri != null) {
                                    Column {
                                        Text("Zoom: ${String.format("%.1f", imageZoomScale)}x", fontSize = 11.sp)
                                        Slider(
                                            value = imageZoomScale,
                                            onValueChange = { imageZoomScale = it },
                                            valueRange = 1f..3f
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (yojanaTitle.isNotBlank() && yojanaBudget.isNotBlank()) {
                                            coroutineScope.launch {
                                                isUploadingImage = true
                                                var uploadedUrl = ""
                                                if (selectedImageUri != null) {
                                                    try {
                                                        uploadedUrl = ImgBBClient.uploadUri(context, selectedImageUri!!) ?: ""
                                                    } catch (e: Exception) {
                                                        Log.e("Admin", "Image upload error", e)
                                                    }
                                                }
                                                viewModel.addProject(
                                                    Project(
                                                        title = yojanaTitle,
                                                        description = yojanaDesc,
                                                        category = yojanaCategory,
                                                        targetBudget = yojanaBudget.toLongOrNull() ?: 0L,
                                                        startDate = yojanaDate,
                                                        imageUrl = uploadedUrl,
                                                        createdBy = "Panchayat Admin"
                                                    )
                                                 ) {
                                                    successDialogMsg = "✅ नई योजना सफलतापूर्वक जोड़ी गई!"
                                                    Toast.makeText(context, "✅ नई योजना सफलतापूर्वक जोड़ी गई!", Toast.LENGTH_SHORT).show()
                                                    Toast.makeText(context, "✅ नई योजना सफलतापूर्वक जोड़ी गई!", Toast.LENGTH_LONG).show()

                                                    Toast.makeText(context, "✅ नई योजना सफलतापूर्वक जोड़ी गई!", Toast.LENGTH_LONG).show()
                                                    yojanaTitle = ""
                                                    yojanaDesc = ""
                                                    yojanaBudget = ""
                                                    selectedImageUri = null
                                                    isUploadingImage = false
                                                    activeAdminMode = null
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isUploadingImage,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (isUploadingImage) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Text(if (isHindi) "योजना सुरक्षित करें (Save Yojana)" else "Save Yojana")
                                    }
                                }
                            }
                        }
                    }
                }
                activeAdminMode is Project -> {
                    val rawProj = activeAdminMode as Project
                    val targetYojana = projects.find { it.id == rawProj.id } ?: rawProj
                    val currentYojanaDonors = donors.filter { it.projectId == targetYojana.id }
                    val currentYojanaExpenses = expenses.filter { it.projectId == targetYojana.id }
                    val totalCollected = currentYojanaDonors.sumOf { it.amount }
                    val totalSpent = currentYojanaExpenses.sumOf { it.amount }
                    val balance = totalCollected - totalSpent

                    if (isEditingYojana) {
                        // Edit Yojana Form
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isHindi) "✏️ योजना संपादित करें (Edit Yojana)" else "✏️ Edit Yojana", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        TextButton(onClick = { isEditingYojana = false }) {
                                            Text(if (isHindi) "← वापस" else "← Cancel")
                                        }
                                    }

                                    OutlinedTextField(value = yojanaTitle, onValueChange = { yojanaTitle = it }, label = { Text("योजना शीर्षक (Title)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = yojanaDesc, onValueChange = { yojanaDesc = it }, label = { Text("विवरण (Description)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = yojanaCategory, onValueChange = { yojanaCategory = it }, label = { Text("श्रेणी (Category)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = yojanaBudget, onValueChange = { yojanaBudget = it }, label = { Text("लक्ष्य बजट (Target Budget ₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                    OutlinedTextField(value = yojanaDate, onValueChange = { yojanaDate = it }, label = { Text("दिनांक (Date)") }, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = yojanaStatus, onValueChange = { yojanaStatus = it }, label = { Text("स्थिति (Status - प्रगति पर / पूर्ण)") }, modifier = Modifier.fillMaxWidth())

                                    Text(if (isHindi) "योजना की फोटो (4:3 Ratio)" else "Yojana Photo (4:3 Ratio)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(4f / 3f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val displayImage = selectedImageUri ?: (if (existingImageUrl.isNotBlank()) existingImageUrl else null)
                                        if (displayImage != null) {
                                            AsyncImage(
                                                model = displayImage,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(if (isHindi) "कोई फोटो नहीं" else "No photo", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isHindi) "नई फोटो चुनें" else "Choose New Photo")
                                    }

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isUploadingImage = true
                                                var finalUrl = existingImageUrl
                                                if (selectedImageUri != null) {
                                                    try {
                                                        finalUrl = ImgBBClient.uploadUri(context, selectedImageUri!!) ?: existingImageUrl
                                                    } catch (e: Exception) {
                                                        Log.e("Admin", "Image update error", e)
                                                    }
                                                }
                                                val updatedProj = targetYojana.copy(
                                                    title = yojanaTitle,
                                                    description = yojanaDesc,
                                                    category = yojanaCategory,
                                                    targetBudget = yojanaBudget.toLongOrNull() ?: targetYojana.targetBudget,
                                                    startDate = yojanaDate,
                                                    status = yojanaStatus,
                                                    imageUrl = finalUrl
                                                )
                                                viewModel.updateProject(updatedProj) {
                                                    val refreshed = projects.find { it.id == updatedProj.id } ?: updatedProj
                                                    successDialogMsg = "✅ योजना सफलतापूर्वक अपडेट हो गई!"
                                                    Toast.makeText(context, "✅ योजना सफलतापूर्वक अपडेट हो गई!", Toast.LENGTH_SHORT).show()
                                                    Toast.makeText(context, "✅ योजना सफलतापूर्वक अपडेट हो गई!", Toast.LENGTH_LONG).show()

                                                    Toast.makeText(context, "✅ योजना सफलतापूर्वक अपडेट हो गई!", Toast.LENGTH_LONG).show()
                                                    isUploadingImage = false
                                                    isEditingYojana = false
                                                    activeAdminMode = refreshed
                                                }
                                            }
                                        },
                                        enabled = !isUploadingImage,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isUploadingImage) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text(if (isHindi) "अपडेट सुरक्षित करें (Save Changes)" else "Save Changes")
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Specific Yojana Dashboard & Account Management
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(targetYojana.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                        Row {
                                            TextButton(onClick = {
                                                yojanaTitle = targetYojana.title
                                                yojanaDesc = targetYojana.description
                                                yojanaCategory = targetYojana.category
                                                yojanaBudget = targetYojana.targetBudget.toString()
                                                yojanaDate = targetYojana.startDate
                                                yojanaStatus = targetYojana.status
                                                existingImageUrl = targetYojana.imageUrl
                                                selectedImageUri = null
                                                isEditingYojana = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isHindi) "संपादित करें" else "Edit")
                                            }
                                            TextButton(onClick = { activeAdminMode = null }) {
                                                Text(if (isHindi) "← सूची" else "← List")
                                            }
                                        }
                                    }

                                    if (targetYojana.imageUrl.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(4f / 3f)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            AsyncImage(model = targetYojana.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                                        }
                                    }

                                    Text(targetYojana.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Divider()

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(if (isHindi) "चंदा जमा" else "Collected", fontSize = 11.sp, color = Color.Gray)
                                            Text("₹$totalCollected", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                        }
                                        Column {
                                            Text(if (isHindi) "कुल खर्च" else "Spent", fontSize = 11.sp, color = Color.Gray)
                                            Text("₹$totalSpent", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                                        }
                                        Column {
                                            Text(if (isHindi) "शेष राशि" else "Balance", fontSize = 11.sp, color = Color.Gray)
                                            Text("₹$balance", fontWeight = FontWeight.Bold, color = Color(0xFF0277BD))
                                        }
                                    }
                                }
                            }
                        }

                        // Left to Right Tabs for Chanda vs Expenses
                        item {
                            TabRow(selectedTabIndex = yojanaSubTab) {
                                Tab(
                                    selected = yojanaSubTab == 0,
                                    onClick = { yojanaSubTab = 0 },
                                    text = { Text(if (isHindi) "💰 चंदा जमा (${currentYojanaDonors.size})" else "💰 Chanda (${currentYojanaDonors.size})", fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = yojanaSubTab == 1,
                                    onClick = { yojanaSubTab = 1 },
                                    text = { Text(if (isHindi) "💸 खर्च विवरण (${currentYojanaExpenses.size})" else "💸 Expenses (${currentYojanaExpenses.size})", fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        if (showDonorSelectDialog) {
                            item {
                                AlertDialog(
                                    onDismissRequest = { showDonorSelectDialog = false },
                                    title = { Text(if (isHindi) "पंजीकृत दाता चुनें" else "Select Registered Donor") },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = donorSelectSearchQuery,
                                                onValueChange = { donorSelectSearchQuery = it },
                                                label = { Text(if (isHindi) "नाम, पिता या मोबाइल से खोजें" else "Search by Name, Father or Mobile") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            val filteredRegistered = registeredDonors.filter {
                                                it.name.contains(donorSelectSearchQuery, ignoreCase = true) ||
                                                it.fatherName.contains(donorSelectSearchQuery, ignoreCase = true) ||
                                                it.mobile.contains(donorSelectSearchQuery, ignoreCase = true)
                                            }
                                            if (filteredRegistered.isEmpty()) {
                                                Column(modifier = Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(if (isHindi) "कोई पंजीकृत दाता नहीं मिला।" else "No registered donor found.", fontSize = 12.sp, color = Color.Gray)
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    OutlinedButton(
                                                        onClick = {
                                                            showDonorSelectDialog = false
                                                            selectedAdminTab = 1
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(if (isHindi) "➕ नया दाता रजिस्टर करें" else "➕ Register New Donor")
                                                    }
                                                }
                                            } else {
                                                LazyColumn(modifier = Modifier.height(250.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    items(filteredRegistered) { rd ->
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            onClick = {
                                                                donorName = rd.name
                                                                donorFatherName = rd.fatherName
                                                                donorMobile = rd.mobile
                                                                donorAddress = rd.address
                                                                showDonorSelectDialog = false
                                                                donorSelectSearchQuery = ""
                                                            }
                                                        ) {
                                                            Column(modifier = Modifier.padding(10.dp)) {
                                                                Text(rd.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                                Text(if (isHindi) "पिता: ${rd.fatherName.ifBlank { "N/A" }} | मोबाइल: ${rd.mobile.ifBlank { "N/A" }}" else "Father: ${rd.fatherName} | Mobile: ${rd.mobile}", fontSize = 11.sp, color = Color.Gray)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    showDonorSelectDialog = false
                                                    selectedAdminTab = 1
                                                }
                                            ) {
                                                Text(if (isHindi) "➕ नया दाता रजिस्टर करें" else "➕ Register New")
                                            }
                                            TextButton(onClick = { showDonorSelectDialog = false }) {
                                                Text(if (isHindi) "बंद करें" else "Close")
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        if (yojanaSubTab == 0) {
                            // Sub-Tab 0: Chanda Management (Add Chanda & Donors List)
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(if (isHindi) "➕ इस योजना में नया चंदा जोड़ें" else "➕ Add Chanda for this Yojana", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        
                                        // Selected Donor Display Card / Donor Picker Button
                                        if (donorName.isNotBlank()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("👤 चयनित दाता: $donorName", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                                        if (donorFatherName.isNotBlank() || donorMobile.isNotBlank()) {
                                                            Text("पिता: ${donorFatherName.ifBlank { "N/A" }} | मो: ${donorMobile.ifBlank { "N/A" }}", fontSize = 11.sp, color = Color.DarkGray)
                                                        }
                                                    }
                                                    TextButton(onClick = { showDonorSelectDialog = true }) {
                                                        Text("🔄 बदलें", fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        } else {
                                            OutlinedCard(
                                                onClick = { showDonorSelectDialog = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(if (isHindi) "👇 पंजीकृत दाता सूची से दाता चुनें" else "👇 Select Registered Donor", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                            Text(if (isHindi) "केवल पंजीकृत दाताओं के नाम से ही चंदा जुड़ सकता है" else "Chanda entry requires choosing from donor directory", fontSize = 11.sp, color = Color.Gray)
                                                        }
                                                    }
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                                }
                                            }

                                            OutlinedButton(
                                                onClick = { selectedAdminTab = 1 },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(if (isHindi) "➕ दाता सूची में नया दाता जोड़ें" else "➕ Register New Donor First")
                                            }
                                        }

                                        OutlinedTextField(
                                            value = donorAmount,
                                            onValueChange = { donorAmount = it },
                                            label = { Text("राशि (Amount ₹)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                        )

                                        OutlinedTextField(
                                            value = donorDate,
                                            onValueChange = { donorDate = it },
                                            label = { Text("दिनांक (Date)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Button(
                                            onClick = {
                                                val isRegistered = registeredDonors.any { it.name.trim().equals(donorName.trim(), ignoreCase = true) }
                                                if (donorName.isBlank() || !isRegistered) {
                                                    Toast.makeText(context, "⚠️ कृपया पहले पंजीकृत दाता सूची से दाता चुनें! यदि दाता नहीं है तो 'दाता सूची' में नया दाता जोड़ें।", Toast.LENGTH_LONG).show()
                                                    showDonorSelectDialog = true
                                                    return@Button
                                                }
                                                if (donorAmount.isBlank()) {
                                                    Toast.makeText(context, "⚠️ कृपया चंदा राशि दर्ज करें!", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }

                                                viewModel.addDonor(
                                                    Donor(
                                                        projectId = targetYojana.id,
                                                        donorName = donorName,
                                                        fatherName = donorFatherName,
                                                        mobile = donorMobile,
                                                        address = donorAddress,
                                                        amount = donorAmount.toLongOrNull() ?: 0L,
                                                        date = donorDate,
                                                        paymentMode = paymentMode
                                                    )
                                                ) {
                                                    successDialogMsg = "✅ ₹$donorAmount चंदा ${donorName} से सफलतापूर्वक जुड़ गया!"
                                                    Toast.makeText(context, "✅ ₹$donorAmount चंदा ${donorName} से सफलतापूर्वक जुड़ गया!", Toast.LENGTH_SHORT).show()
                                                    donorName = ""
                                                    donorFatherName = ""
                                                    donorMobile = ""
                                                    donorAddress = ""
                                                    donorAmount = ""
                                                    donorDate = "14 Oct 2026"
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(if (isHindi) "चंदा सुरक्षित करें (Save Chanda)" else "Save Chanda")
                                        }
                                    }
                                }
                            }

                            item {
                                Text(if (isHindi) "इस योजना के चंदा दाता:" else "Donors list:", fontWeight = FontWeight.Bold)
                            }
                            if (currentYojanaDonors.isEmpty()) {
                                item { Text(if (isHindi) "कोई चंदा नहीं है।" else "No donors yet.", fontSize = 12.sp, color = Color.Gray) }
                            } else {
                                val groupedAdminDonors = currentYojanaDonors.groupBy { Pair(it.donorName.trim(), it.mobile.trim()) }.entries.toList()
                                items(groupedAdminDonors) { (key, entries) ->
                                    val donorName = key.first
                                    val donorMobile = key.second
                                    val sampleAddress = entries.firstOrNull()?.address ?: ""
                                    val totalPersonAmt = entries.sumOf { it.amount }
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(donorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    if (donorMobile.isNotBlank() || sampleAddress.isNotBlank()) {
                                                        Text(
                                                            text = buildString {
                                                                if (donorMobile.isNotBlank()) append("📱 $donorMobile")
                                                                if (donorMobile.isNotBlank() && sampleAddress.isNotBlank()) append(" • ")
                                                                if (sampleAddress.isNotBlank()) append("📍 $sampleAddress")
                                                            },
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Text("कुल: ₹$totalPersonAmt", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            entries.sortedBy { it.date }.forEach { d ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text("${d.date} • ${d.paymentMode} • ₹${d.amount}", fontSize = 12.sp, color = Color(0xFF2E7D32))
                                                    }
                                                    IconButton(onClick = { viewModel.deleteDonor(d.id, d.projectId, d.amount) }, modifier = Modifier.size(32.dp)) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Sub-Tab 1: Expenses Management (Add Expense & Expenses List)
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(if (isHindi) "➕ इस योजना में नया खर्च जोड़ें (बिल फोटो वैकल्पिक)" else "➕ Add Expense (Optional Bill Receipt)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        OutlinedTextField(value = expenseTitle, onValueChange = { expenseTitle = it }, label = { Text("खर्च का कारण (Purpose)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = expenseAmount, onValueChange = { expenseAmount = it }, label = { Text("खर्च राशि (Amount ₹)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                                        OutlinedTextField(value = expenseDate, onValueChange = { expenseDate = it }, label = { Text("दिनांक (Date)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = billRef, onValueChange = { billRef = it }, label = { Text("रसीद संख्या (Bill Ref)") }, modifier = Modifier.fillMaxWidth())

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(
                                                onClick = { billPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isHindi) "बिल रसीद फोटो जोड़ें (वैकल्पिक)" else "Add Bill Receipt (Optional)")
                                            }
                                            if (selectedBillUri != null) {
                                                TextButton(onClick = { selectedBillUri = null }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
                                            }
                                        }

                                        if (selectedBillUri != null) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            ) {
                                                AsyncImage(model = selectedBillUri, contentDescription = null, modifier = Modifier.fillMaxSize())
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (expenseTitle.isNotBlank() && expenseAmount.isNotBlank()) {
                                                    coroutineScope.launch {
                                                        isUploadingBill = true
                                                        var billUrl = ""
                                                        if (selectedBillUri != null) {
                                                            try {
                                                                billUrl = ImgBBClient.uploadUri(context, selectedBillUri!!) ?: ""
                                                            } catch (e: Exception) {
                                                                Log.e("Admin", "Bill upload error", e)
                                                            }
                                                        }
                                                        viewModel.addExpense(
                                                            Expense(
                                                                projectId = targetYojana.id,
                                                                title = expenseTitle,
                                                                amount = expenseAmount.toLongOrNull() ?: 0L,
                                                                date = expenseDate,
                                                                billRef = billRef,
                                                                imageUrl = billUrl
                                                            )
                                                        ) {
                                                            successDialogMsg = "✅ ₹$expenseAmount खर्च सफलतापूर्वक जुड़ गया!"
                                                            Toast.makeText(context, "✅ ₹$expenseAmount खर्च सफलतापूर्वक जुड़ गया!", Toast.LENGTH_SHORT).show()
                                                            expenseTitle = ""
                                                            expenseAmount = ""
                                                            selectedBillUri = null
                                                            isUploadingBill = false
                                                        }
                                                    }
                                                }
                                            },
                                            enabled = !isUploadingBill,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            if (isUploadingBill) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                            } else {
                                                Text(if (isHindi) "खर्च सुरक्षित करें (Save Expense)" else "Save Expense")
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (isHindi) "इस योजना के खर्च:" else "Expenses list:", fontWeight = FontWeight.Bold)
                            }
                            if (currentYojanaExpenses.isEmpty()) {
                                item { Text(if (isHindi) "कोई खर्च नहीं है।" else "No expenses yet.", fontSize = 12.sp, color = Color.Gray) }
                            } else {
                                items(currentYojanaExpenses) { e ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(e.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${e.date} • ₹${e.amount} (${e.billRef})", fontSize = 12.sp, color = Color(0xFFC62828))
                                                if (e.imageUrl.isNotBlank()) {
                                                    Text(if (isHindi) "📄 बिल रसीद संलग्न है" else "📄 Bill receipt attached", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                            IconButton(onClick = {
                                                viewModel.deleteExpense(e.id, e.projectId, e.amount)
                                                successDialogMsg = "🗑️ खर्च सफलतापूर्वक हटा दिया गया!"
                                                Toast.makeText(context, "🗑️ खर्च हटा दिया गया!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    when (selectedAdminTab) {
                        0 -> {
                            // Tab 0: Yojnas & Accounts Management
                            item {
                                Button(
                                    onClick = {
                                        yojanaTitle = ""
                                        yojanaDesc = ""
                                        yojanaCategory = "विकास योजना (Development Yojana)"
                                        yojanaBudget = ""
                                        yojanaDate = "14 Oct 2026"
                                        selectedImageUri = null
                                        existingImageUrl = ""
                                        imageZoomScale = 1f
                                        activeAdminMode = "ADD"
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isHindi) "+ नई विकास योजना जोड़ें (Add New Yojana)" else "+ Add New Yojana")
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(if (isHindi) "📋 सभी योजनाएं (संपादित करने या हिसाब के लिए क्लिक करें):" else "📋 All Yojnas (Click to edit or manage accounts):", fontWeight = FontWeight.Bold)
                            }

                            if (projects.isEmpty()) {
                                item { Text(if (isHindi) "कोई योजना उपलब्ध नहीं है।" else "No yojnas available.", color = Color.Gray) }
                            } else {
                                items(projects) { p ->
                                    val balance = p.totalCollected - p.totalSpent
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { activeAdminMode = p }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(p.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text("जमा: ₹${p.totalCollected} | खर्च: ₹${p.totalSpent} | शेष: ₹$balance", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    yojanaTitle = p.title
                                                    yojanaDesc = p.description
                                                    yojanaCategory = p.category
                                                    yojanaBudget = p.targetBudget.toString()
                                                    yojanaDate = p.startDate
                                                    yojanaStatus = p.status
                                                    existingImageUrl = p.imageUrl
                                                    selectedImageUri = null
                                                    activeAdminMode = p
                                                    isEditingYojana = true
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                                }
                                                IconButton(onClick = {
                                                    viewModel.deleteProject(p.id)
                                                    successDialogMsg = "🗑️ योजना '${p.title}' सफलतापूर्वक हटा दी गई!"
                                                    Toast.makeText(context, "🗑️ योजना हटा दी गई!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Tab 1: Registered Donors Master Directory & Creation
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(if (editingRegDonorId != null) (if (isHindi) "✏️ पंजीकृत दाता विवरण संपादित करें" else "✏️ Edit Registered Donor") else (if (isHindi) "➕ नया दाता मास्टर रजिस्टर करें" else "➕ Register New Master Donor"), fontWeight = FontWeight.Bold)
                                        OutlinedTextField(value = regDonorName, onValueChange = { regDonorName = it }, label = { Text("दाता का पूरा नाम (Full Name)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = regDonorFatherName, onValueChange = { regDonorFatherName = it }, label = { Text("पिता/पति का नाम (Father's Name)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = regDonorMobile, onValueChange = { regDonorMobile = it }, label = { Text("मोबाइल नंबर (Mobile No)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                                        OutlinedTextField(value = regDonorAddress, onValueChange = { regDonorAddress = it }, label = { Text("गाँव / पता (Village / Address)") }, modifier = Modifier.fillMaxWidth())
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (editingRegDonorId != null) {
                                                OutlinedButton(
                                                    onClick = {
                                                        editingRegDonorId = null
                                                        regDonorName = ""
                                                        regDonorFatherName = ""
                                                        regDonorMobile = ""
                                                        regDonorAddress = ""
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(if (isHindi) "रद्द करें" else "Cancel")
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    if (regDonorName.isNotBlank()) {
                                                        if (editingRegDonorId != null) {
                                                            viewModel.updateRegisteredDonor(
                                                                RegisteredDonor(
                                                                    id = editingRegDonorId!!,
                                                                    name = regDonorName,
                                                                    fatherName = regDonorFatherName,
                                                                    mobile = regDonorMobile,
                                                                    address = regDonorAddress
                                                                )
                                                            ) {
                                                                successDialogMsg = "✅ दाता '${regDonorName}' का विवरण अपडेट हो गया!"
                                                                Toast.makeText(context, "✅ दाता '${regDonorName}' का विवरण अपडेट हो गया!", Toast.LENGTH_SHORT).show()
                                                                Toast.makeText(context, "✅ दाता '${regDonorName}' का विवरण अपडेट हो गया!", Toast.LENGTH_LONG).show()

                                                                Toast.makeText(context, "✅ दाता '${regDonorName}' का विवरण अपडेट हो गया!", Toast.LENGTH_LONG).show()
                                                                regDonorName = ""
                                                                regDonorFatherName = ""
                                                                regDonorMobile = ""
                                                                regDonorAddress = ""
                                                                editingRegDonorId = null
                                                            }
                                                        } else {
                                                            viewModel.addRegisteredDonor(
                                                                RegisteredDonor(
                                                                    name = regDonorName,
                                                                    fatherName = regDonorFatherName,
                                                                    mobile = regDonorMobile,
                                                                    address = regDonorAddress
                                                                )
                                                            ) {
                                                                successDialogMsg = "✅ दाता '${regDonorName}' सफलता पूर्वक पंजीकृत हो गए!"
                                                                Toast.makeText(context, "✅ दाता '${regDonorName}' सफलता पूर्वक पंजीकृत हो गए!", Toast.LENGTH_SHORT).show()
                                                                Toast.makeText(context, "✅ दाता '${regDonorName}' सफलता पूर्वक पंजीकृत हो गए!", Toast.LENGTH_LONG).show()

                                                                Toast.makeText(context, "✅ दाता '${regDonorName}' सफलता पूर्वक पंजीकृत हो गए!", Toast.LENGTH_LONG).show()
                                                                regDonorName = ""
                                                                regDonorFatherName = ""
                                                                regDonorMobile = ""
                                                                regDonorAddress = ""
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(if (editingRegDonorId != null) (if (isHindi) "अपडेट करें" else "Update") else (if (isHindi) "पंजीकृत करें" else "Register"))
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = regDonorSearchQuery,
                                    onValueChange = { regDonorSearchQuery = it },
                                    label = { Text(if (isHindi) "🔍 पंजीकृत दाताओं को खोजें (नाम/मोबाइल)" else "🔍 Search registered donors...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item {
                                Text(if (isHindi) "कुल पंजीकृत दाता: ${registeredDonors.size}" else "Total Registered Donors: ${registeredDonors.size}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            val filteredMasterDonors = registeredDonors.filter {
                                it.name.contains(regDonorSearchQuery, ignoreCase = true) ||
                                it.fatherName.contains(regDonorSearchQuery, ignoreCase = true) ||
                                it.mobile.contains(regDonorSearchQuery, ignoreCase = true)
                            }

                            if (filteredMasterDonors.isEmpty()) {
                                item { Text(if (isHindi) "कोई पंजीकृत दाता नहीं है।" else "No registered donors found.", color = Color.Gray, fontSize = 12.sp) }
                            } else {
                                items(filteredMasterDonors) { rd ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(rd.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(if (isHindi) "पिता: ${rd.fatherName.ifBlank { "N/A" }} | मोबाइल: ${rd.mobile.ifBlank { "N/A" }}" else "Father: ${rd.fatherName} | Mobile: ${rd.mobile}", fontSize = 12.sp, color = Color.Gray)
                                                if (rd.address.isNotBlank()) {
                                                    Text("पता: ${rd.address}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            }
                                            Row {
                                                IconButton(onClick = {
                                                    regDonorName = rd.name
                                                    regDonorFatherName = rd.fatherName
                                                    regDonorMobile = rd.mobile
                                                    regDonorAddress = rd.address
                                                    editingRegDonorId = rd.id
                                                }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                                }
                                                IconButton(onClick = {
                                                    viewModel.deleteRegisteredDonor(rd.id)
                                                    successDialogMsg = "🗑️ पंजीकृत दाता '${rd.name}' सफलतापूर्वक हटा दिए गए!"
                                                    Toast.makeText(context, "🗑️ पंजीकृत दाता हटा दिए गए!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // Tab 2: Publish Notices (सूचनाएं प्रकाशित करें)
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(if (isHindi) "📢 पंचायत सूचना / घोषणा प्रकाशित करें" else "📢 Publish Panchayat Notice", fontWeight = FontWeight.Bold)
                                        OutlinedTextField(value = noticeTitle, onValueChange = { noticeTitle = it }, label = { Text("सूचना शीर्षक (Title)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = noticeContent, onValueChange = { noticeContent = it }, label = { Text("सूचना विस्तार (Content)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = noticeDate, onValueChange = { noticeDate = it }, label = { Text("दिनांक (Date)") }, modifier = Modifier.fillMaxWidth())
                                        Button(
                                            onClick = {
                                                if (noticeTitle.isNotBlank() && noticeContent.isNotBlank()) {
                                                    viewModel.addNotice(
                                                        Notice(
                                                            title = noticeTitle,
                                                            content = noticeContent,
                                                            date = noticeDate,
                                                            priority = noticePriority
                                                        )
                                                    ) {
                                                        successDialogMsg = "✅ सूचना सफलतापूर्वक प्रकाशित की गई!"
                                                        Toast.makeText(context, "✅ सूचना सफलतापूर्वक प्रकाशित की गई!", Toast.LENGTH_SHORT).show()
                                                        Toast.makeText(context, "✅ सूचना सफलतापूर्वक प्रकाशित की गई!", Toast.LENGTH_LONG).show()

                                                        Toast.makeText(context, "✅ सूचना सफलतापूर्वक प्रकाशित की गई!", Toast.LENGTH_LONG).show()
                                                        noticeTitle = ""
                                                        noticeContent = ""
                                                    }
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(if (isHindi) "सूचना प्रकाशित करें (Publish Notice)" else "Publish Notice")
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(if (isHindi) "प्रकाशित सूचनाएं:" else "Published Notices:", fontWeight = FontWeight.Bold)
                            }

                            if (notices.isEmpty()) {
                                item { Text(if (isHindi) "कोई सूचना नहीं है।" else "No notices published yet.", color = Color.Gray) }
                            } else {
                                items(notices) { n ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(n.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(n.content, fontSize = 12.sp, color = Color.Gray)
                                            }
                                            IconButton(onClick = {
                                                viewModel.deleteNotice(n.id)
                                                successDialogMsg = "🗑️ सूचना सफलतापूर्वक हटा दी गई!"
                                                Toast.makeText(context, "🗑️ सूचना हटा दी गई!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (successDialogMsg != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { successDialogMsg = null },
            title = { androidx.compose.material3.Text(if (isHindi) "🎉 सफलता (Success)" else "🎉 Success") },
            text = { androidx.compose.material3.Text(successDialogMsg!!, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
            confirmButton = {
                androidx.compose.material3.Button(onClick = { successDialogMsg = null }) {
                    androidx.compose.material3.Text(if (isHindi) "ठीक है (OK)" else "OK")
                }
            }
        )
    }

}
