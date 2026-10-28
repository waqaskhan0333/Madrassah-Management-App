package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.FeeVoucher
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.ui.components.DmcCardPreviewDialog
import com.example.ui.components.GenerateInvoiceDialog
import com.example.ui.components.HifzProgressRechartsTracker
import com.example.ui.components.IdCardDialog
import com.example.ui.components.InvoiceSlipDialog
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.PaymentDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.StatusChip
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import com.example.util.UserRoleManager
import com.example.util.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    student: Student,
    viewModel: MadrassaViewModel,
    onBack: () -> Unit,
    onEditStudent: (Student) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showGenerateInvoiceDialog by remember { mutableStateOf(false) }
    var selectedVoucherForSlip by remember { mutableStateOf<FeeVoucher?>(null) }
    var selectedVoucherForPayment by remember { mutableStateOf<FeeVoucher?>(null) }
    var selectedReceiptToView by remember { mutableStateOf<PaymentRecord?>(null) }
    var showIdCardDialog by remember { mutableStateOf(false) }
    var showDmcDialog by remember { mutableStateOf(false) }
    var showPhotoStorageDialog by remember { mutableStateOf(false) }

    var activeStudentId by remember { mutableLongStateOf(student.id) }
    val profile by viewModel.profile.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val student = allStudents.find { it.id == activeStudentId } ?: student
    val hifzHistory by viewModel.recentHifz.collectAsState()
    val studentHifz = hifzHistory.filter { it.studentId == student.id }
    val vouchers by viewModel.vouchers.collectAsState()
    val studentVouchers = vouchers.filter { it.studentId == student.id }
    val attendanceRecords by viewModel.studentAttendance.collectAsState()
    val studentAttendance = attendanceRecords.filter { it.personId == student.id }
    val allExams by viewModel.exams.collectAsState()
    val studentResults by viewModel.getResultsForStudent(student.id).collectAsState(initial = emptyList())

    val studentTotalInvoiced = studentVouchers.sumOf { it.totalPayable }
    val studentTotalPaid = studentVouchers.sumOf { it.paidAmount }
    val studentOutstanding = studentVouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Student Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDmcDialog = true }) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "DMC Report Card", tint = Color.White)
                    }
                    IconButton(onClick = { showIdCardDialog = true }) {
                        Icon(imageVector = Icons.Default.Badge, contentDescription = "Create ID Card", tint = Color.White)
                    }
                    IconButton(onClick = { onEditStudent(student) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    if (UserRoleManager.isAdmin) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF8A80))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Scrollable Student Switcher to show all student names
            if (allStudents.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(allStudents, key = { it.id }) { st ->
                        val isSelected = st.id == student.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeStudentId = st.id },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White.copy(alpha = 0.2f) else EmeraldPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (st.photoUri.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageStorageHelper.toImageModel(st.photoUri),
                                            contentDescription = st.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = st.name.trim().take(1),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else EmeraldPrimary
                                        )
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = st.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Profile Card Header (3D luxury card with metallic gold rim and orb)
            Madrassa3dCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFF1B6B4F), Color(0xFF042418))))
                            .border(2.dp, Color(0xFFE5C07B), CircleShape)
                            .clickable { showPhotoStorageDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (student.photoUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageStorageHelper.toImageModel(student.photoUri),
                                contentDescription = student.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = student.name.firstOrNull()?.uppercase() ?: "S",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE5C07B)
                                )
                            )
                        }

                        // Small camera/vault icon badge in bottom corner
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD4AF37))
                                .border(1.dp, Color(0xFF042418), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderShared,
                                contentDescription = "Manage Photo",
                                tint = Color(0xFF042418),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${student.rollNo}  |  Reg: 2025-001",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                    Text(
                        text = "Class: ${student.department}  |  ${student.section}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE5C07B),
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contact & Action Shortcuts Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.guardianPhone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B6B4F)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Call", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val cleanNum = student.guardianPhone.replace("-", "").replace(" ", "")
                                val formatted = if (cleanNum.startsWith("0")) "+92" + cleanNum.substring(1) else cleanNum
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formatted"))
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "WhatsApp", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showPhotoStorageDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Photo Vault", modifier = Modifier.size(16.dp), tint = Color(0xFF042418))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Photo Vault", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF042418))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showDmcDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF8A80))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "DMC Card", fontSize = 11.5.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showIdCardDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE5C07B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "ID Card", fontSize = 11.5.sp, color = Color(0xFFE5C07B))
                        }
                    }
                }
            }

            // Tabs
            val tabs = listOf("Basic Info", "Exams / DMC", "Progress", "Fee Invoices", "Attendance")
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                tabs.forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = tabName,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Basic Info - Fully editable
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onEditStudent(student) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EditNote, contentDescription = null, tint = EmeraldPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = "All Profile Fields Editable", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = EmeraldPrimary)
                                            Text(text = "Tap any field below or tap here to update", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = EmeraldPrimary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            // Biometric Registration Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (student.isBiometricEnrolled) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (student.isBiometricEnrolled) Color(0xFF81C784) else Color(0xFFFFB74D)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = if (student.isBiometricEnrolled) Icons.Default.Fingerprint else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (student.isBiometricEnrolled) Color(0xFF2E7D32) else Color(0xFFE65100),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (student.isBiometricEnrolled) "Biometric Data Registered" else "Biometrics Not Enrolled",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = if (student.isBiometricEnrolled) Color(0xFF1B5E20) else Color(0xFFE65100)
                                            )
                                            Text(
                                                text = if (student.isBiometricEnrolled) "Template: ${student.fingerprintTemplate.take(12)}... • Face ID Active" else "Capture fingerprint and face for attendance",
                                                fontSize = 10.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            val tpl = com.example.util.BiometricSecurityManager.generateFingerprintTemplate(student)
                                            val emb = com.example.util.BiometricSecurityManager.generateFaceEmbedding(student)
                                            viewModel.enrollStudentBiometrics(student.id, fingerprintTemplate = tpl, faceEmbedding = emb)
                                            Toast.makeText(context, "Enrolled biometrics for ${student.name}!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (student.isBiometricEnrolled) Color(0xFF2E7D32) else EmeraldPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = if (student.isBiometricEnrolled) "Re-Enroll" else "Enroll Now", fontSize = 11.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        item {
                            InfoRow(label = t("father_name"), value = student.fatherName, onEdit = { onEditStudent(student) })
                            InfoRow(label = t("guardian_phone"), value = student.guardianPhone, onEdit = { onEditStudent(student) })
                            InfoRow(label = t("campus"), value = student.campus, onEdit = { onEditStudent(student) })
                            InfoRow(label = t("paray"), value = student.paray, onEdit = { onEditStudent(student) })
                            InfoRow(label = t("section"), value = student.section, onEdit = { onEditStudent(student) })
                            InfoRow(label = t("department"), value = student.department, onEdit = { onEditStudent(student) })
                            InfoRow(label = "Date of Birth", value = student.dob, onEdit = { onEditStudent(student) })
                            InfoRow(label = "Gender", value = student.gender, onEdit = { onEditStudent(student) })
                            InfoRow(label = "CNIC / B-Form", value = if (student.cnicBForm.isNotBlank()) student.cnicBForm else "Not set", onEdit = { onEditStudent(student) })
                            InfoRow(label = t("address"), value = student.address, onEdit = { onEditStudent(student) })
                            InfoRow(label = "Admission Date", value = student.admissionDate, onEdit = { onEditStudent(student) })
                            InfoRow(label = "Monthly Fee", value = "Rs. ${student.monthlyFee.toInt()}", onEdit = { onEditStudent(student) })
                            InfoRow(label = "Discount", value = "Rs. ${student.discount.toInt()}", onEdit = { onEditStudent(student) })
                            InfoRow(label = t("status"), value = student.status, onEdit = { onEditStudent(student) })
                        }
                    }
                }
                1 -> {
                    // Exams / DMC Report Card Page
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // DMC Header & Print Banner
                        item {
                            Madrassa3dCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = 5.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Detailed Marks Certificate (DMC)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "امتحانی رزلٹ کارڈ • Official Evaluation Records",
                                                fontSize = 11.sp,
                                                color = Color(0xFFE5C07B)
                                            )
                                        }

                                        Button(
                                            onClick = { showDmcDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF042418), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "View DMC Card", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF042418))
                                        }
                                    }

                                    if (studentResults.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        val totalMarksObt = studentResults.sumOf { it.kulNumber.toInt() }
                                        val totalMax = studentResults.sumOf { if (it.totalMaxMarks > 0) it.totalMaxMarks.toInt() else 100 }
                                        val overallPct = if (totalMax > 0) ((totalMarksObt * 100) / totalMax) else 0
                                        val overallGrade = when {
                                            overallPct >= 90 -> "ممتاز (A+)"
                                            overallPct >= 80 -> "جید جداً (A)"
                                            overallPct >= 65 -> "جید (B)"
                                            overallPct >= 50 -> "مقبول (C)"
                                            else -> "راسب (Needs Improvement)"
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF042418).copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                                .border(1.dp, Color(0xFFE5C07B).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = "Total Obtained", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                                Text(text = "$totalMarksObt / $totalMax", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                            }
                                            Column {
                                                Text(text = "Percentage", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                                Text(text = "$overallPct%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00E676))
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = "Final Position / Grade", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                                                Text(text = overallGrade, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE5C07B))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (studentResults.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Assessment, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No exam marks recorded yet in database for this student.\nEnter results in Exam Marksheet to generate official DMC scores.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(studentResults) { res ->
                                val exam = allExams.find { it.id == res.examId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = if (res.subject.isNotBlank()) res.subject else (exam?.title ?: "Oral Evaluation"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "${exam?.title ?: "Exam"} • ${exam?.date ?: "Session 2025"}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE8F5E9)
                                            ) {
                                                Text(
                                                    text = res.kaifiyat.ifBlank { res.grade },
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Detailed 4-Section Marks Breakdown Table
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(EmeraldPrimaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "Q1 (25)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "${res.question1.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "Q2 (25)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "${res.question2.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "Q3 (25)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "${res.question3.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "Tajweed (25)", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "${res.talaffuz.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "Total (100)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                                Text(text = "${res.kulNumber.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                                            }
                                        }

                                        if (res.remarks.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Remarks / کیفیت: ${res.remarks}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Academic / Hifz Progress Tracker
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Recharts-style 30-Day Memorization Tracker
                        item {
                            HifzProgressRechartsTracker(hifzHistory = studentHifz)
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Memorization Log",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${studentHifz.size} Entries",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        if (studentHifz.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No custom progress entries logged yet. Sample 30-day baseline is displayed above.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(studentHifz) { prog ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFE8F5E9)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Surah ${prog.sabaqSurah} (${prog.sabaqAyahRange})",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            StatusChip(status = prog.grade)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Para: ${prog.sabaqPara} • Sabqi: ${prog.sabqiRange}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = prog.date, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
                                        }
                                        if (prog.remarks.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "Remarks: ${prog.remarks}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Fee Invoices & Ledger
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Student Financial Summary Box
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Student Fee Account", fontWeight = FontWeight.Bold)
                                        Button(
                                            onClick = { showGenerateInvoiceDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "New Invoice", fontSize = 11.5.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = "Invoiced", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "Rs. ${studentTotalInvoiced.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Column {
                                            Text(text = "Paid", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                            Text(text = "Rs. ${studentTotalPaid.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "Balance Due", fontSize = 11.sp, color = Color(0xFFC62828))
                                            Text(text = "Rs. ${studentOutstanding.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFC62828))
                                        }
                                    }
                                }
                            }
                        }

                        if (studentVouchers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No fee invoices generated yet for this student.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            item {
                                Text(text = "Invoices History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            items(studentVouchers) { voucher ->
                                val due = voucher.totalPayable - voucher.paidAmount
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = voucher.monthYear, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                Text(text = "Invoice #: ${voucher.voucherNo} • Due: ${voucher.dueDate}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            StatusChip(status = voucher.status)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Total: Rs. ${voucher.totalPayable.toInt()}", fontSize = 12.sp)
                                            Text(text = "Paid: Rs. ${voucher.paidAmount.toInt()}", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "Due: Rs. ${due.toInt()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (due > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = { selectedVoucherForSlip = voucher },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "Invoice Slip", fontSize = 12.sp)
                                            }

                                            if (voucher.status != "PAID") {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Button(
                                                    onClick = { selectedVoucherForPayment = voucher },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(30.dp)
                                                ) {
                                                    Text(text = "Collect", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Attendance History
                    if (studentAttendance.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No attendance recorded yet for this date.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(studentAttendance) { att ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "Date: ${att.date}", fontWeight = FontWeight.Bold)
                                            if (att.checkInTime.isNotBlank()) {
                                                Text(text = "Time: ${att.checkInTime}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        StatusChip(status = att.status)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invoice Slip Dialog Modal
    selectedVoucherForSlip?.let { voucher ->
        InvoiceSlipDialog(
            voucher = voucher,
            student = student,
            profile = profile,
            onDismiss = { selectedVoucherForSlip = null },
            onPayClick = { selectedVoucherForPayment = voucher }
        )
    }

    // Generate Invoice Dialog specifically for this student
    if (showGenerateInvoiceDialog) {
        GenerateInvoiceDialog(
            students = allStudents,
            preselectedStudentId = student.id,
            onDismiss = { showGenerateInvoiceDialog = false },
            onGenerate = { studentId, monthYear, tuition, admission, exam, discount, dueDate ->
                viewModel.generateInvoice(studentId, monthYear, tuition, admission, exam, discount, dueDate) {
                    showGenerateInvoiceDialog = false
                }
            }
        )
    }

    // Payment Collection Dialog
    selectedVoucherForPayment?.let { voucher: FeeVoucher ->
        PaymentDialog(
            voucher = voucher,
            studentName = student.name,
            onDismiss = { selectedVoucherForPayment = null },
            onConfirmPayment = { amount: Double, mode: String, notes: String ->
                viewModel.collectFeePayment(voucher, amount, mode, notes) { payment: PaymentRecord ->
                    selectedVoucherForPayment = null
                    selectedReceiptToView = payment
                    Toast.makeText(context, "Payment collected successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Receipt Modal Dialog
    selectedReceiptToView?.let { payment: PaymentRecord ->
        ReceiptDialog(
            payment = payment,
            studentName = student.name,
            rollNo = student.rollNo,
            onDismiss = { selectedReceiptToView = null }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(text = "Move to Recycle Bin?") },
            text = { Text(text = "Student ${student.name} will be moved to the Recycle Bin. You can restore this record at any time.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.softDeleteStudent(student.id)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showIdCardDialog) {
        IdCardDialog(
            student = student,
            profile = profile,
            viewModel = viewModel,
            onDismiss = { showIdCardDialog = false }
        )
    }

    if (showDmcDialog) {
        DmcCardPreviewDialog(
            student = student,
            viewModel = viewModel,
            onDismiss = { showDmcDialog = false }
        )
    }

    if (showPhotoStorageDialog) {
        Dialog(
            onDismissRequest = { showPhotoStorageDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                InternalPhotoManagerScreen(
                    viewModel = viewModel,
                    onBack = { showPhotoStorageDialog = false }
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    onEdit: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onEdit != null) { onEdit?.invoke() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                if (onEdit != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit $label",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
