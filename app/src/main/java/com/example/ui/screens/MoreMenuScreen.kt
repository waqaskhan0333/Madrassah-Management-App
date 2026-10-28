package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Course
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.MadrassaProfile
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.ui.components.ExamListScannerDialog
import com.example.ui.components.ExamResultsTableView
import com.example.ui.components.IdCardDialog
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.Madrassa3dIconOrb
import com.example.ui.components.StatusChip
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import com.example.util.LanguageState
import com.example.util.UserRoleManager
import com.example.util.t

@Composable
fun MoreMenuScreen(
    viewModel: MadrassaViewModel,
    initialSubSection: String? = null,
    onSubSectionClosed: (() -> Unit)? = null,
    onActiveSubSectionChanged: ((String?) -> Unit)? = null
) {
    val context = LocalContext.current
    var activeSubSection by remember { mutableStateOf(initialSubSection) }
    var selectedTeacherForClass by remember { mutableStateOf<Teacher?>(null) }
    var showIdCardStudent by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(initialSubSection) {
        activeSubSection = initialSubSection
        onActiveSubSectionChanged?.invoke(initialSubSection)
    }

    LaunchedEffect(activeSubSection) {
        onActiveSubSectionChanged?.invoke(activeSubSection)
    }

    if (activeSubSection != null) {
        androidx.activity.compose.BackHandler {
            activeSubSection = null
            onSubSectionClosed?.invoke()
            onActiveSubSectionChanged?.invoke(null)
        }
    }

    val profile by viewModel.profile.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val recycleStudents by viewModel.recycleBinStudents.collectAsState()
    val recycleTeachers by viewModel.recycleBinTeachers.collectAsState()

    val closeSubSection: () -> Unit = {
        activeSubSection = null
        onSubSectionClosed?.invoke()
        onActiveSubSectionChanged?.invoke(null)
    }

    when (activeSubSection) {
        "TEACHER_CLASS" -> {
            selectedTeacherForClass?.let { teacher ->
                TeacherClassScreen(
                    teacher = teacher,
                    viewModel = viewModel,
                    onBack = { activeSubSection = "TEACHERS" },
                    onStudentClick = { /* Can view detail if needed */ }
                )
            } ?: run { activeSubSection = "TEACHERS" }
        }
        "IMAGE_STORAGE" -> InternalPhotoManagerScreen(
            viewModel = viewModel,
            onBack = closeSubSection
        )
        "EXAMS" -> ExamsSubScreen(
            exams = exams,
            students = students,
            viewModel = viewModel,
            onBack = closeSubSection,
            onAddExam = { viewModel.addExam(it) },
            onUpdateExam = { viewModel.updateExam(it) },
            onDeleteExam = { viewModel.deleteExam(it) }
        )
        "TEACHERS" -> TeachersSubScreen(
            teachers = teachers,
            onBack = closeSubSection,
            onTeacherClick = { teacher ->
                selectedTeacherForClass = teacher
                activeSubSection = "TEACHER_CLASS"
            },
            onSaveTeacher = { teacher, isEdit -> viewModel.saveTeacher(teacher, isEdit) },
            onDeleteTeacher = { id -> viewModel.softDeleteTeacher(id) }
        )
        "COURSES" -> CoursesSubScreen(
            courses = courses,
            onBack = closeSubSection,
            onSaveCourse = { course, isEdit -> viewModel.saveCourse(course, isEdit) },
            onDeleteCourse = { id -> viewModel.deleteCourse(id) }
        )
        "SETTINGS" -> ProfileSettingsSubScreen(
            profile = profile,
            students = students,
            viewModel = viewModel,
            onBack = closeSubSection,
            onSaveProfile = { viewModel.updateProfile(it) }
        )
        "RECYCLE_BIN" -> RecycleBinSubScreen(
            recycleStudents = recycleStudents,
            recycleTeachers = recycleTeachers,
            onBack = closeSubSection,
            onRestoreStudent = { viewModel.restoreStudent(it) },
            onPermanentDeleteStudent = { viewModel.permanentlyDeleteStudent(it) },
            onRestoreTeacher = { viewModel.restoreTeacher(it) }
        )
        "BACKUP_RESTORE" -> BackupRestoreSubScreen(
            viewModel = viewModel,
            onBack = closeSubSection
        )
        "FEES" -> FeesScreen(
            viewModel = viewModel,
            onBack = closeSubSection
        )
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Two Mode: Admin Mode vs Teacher Mode Switcher Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (UserRoleManager.isAdmin) Color(0xFF042418) else Color(0xFF1E3A8A)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (UserRoleManager.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (UserRoleManager.isAdmin) Color(0xFFE5C07B) else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (UserRoleManager.isAdmin) "Admin Mode 👑" else "Teacher Mode 👨‍🏫",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (UserRoleManager.isAdmin) "Authority to Add, Edit, Delete All" else "Restricted: Attendance & Class view",
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    UserRoleManager.toggleRole()
                                    Toast.makeText(context, "Switched to ${if (UserRoleManager.isAdmin) "Admin" else "Teacher"} Mode", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (UserRoleManager.isAdmin) Color(0xFFD4AF37) else Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (UserRoleManager.isAdmin) "Switch Teacher" else "Switch Admin",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (UserRoleManager.isAdmin) Color(0xFF042418) else Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }

                // Admin & Madrassa Info Card with Logo
                item {
                    Madrassa3dCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(Color(0xFF1B6B4F), Color(0xFF042418))))
                                    .border(1.5.dp, Color(0xFFE5C07B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profile.logoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageStorageHelper.toImageModel(profile.logoUri),
                                        contentDescription = "Madrassa Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color(0xFFE5C07B),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (LanguageState.isUrdu) profile.nameUr else profile.nameEn,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                )
                                Text(
                                    text = "${t("principal_name")}: ${profile.principalName}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                                )
                                Text(
                                    text = "${t("session_year")}: ${profile.sessionYear}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE5C07B), fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }

                // Menu Items
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            MenuItemRow(
                                icon = Icons.Default.Person,
                                title = t("asatiza"),
                                subtitle = "${teachers.size} Faculty Members (Add, Edit, Delete)",
                                iconColor = Color(0xFF1976D2),
                                onClick = { activeSubSection = "TEACHERS" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.Assignment,
                                title = t("exams"),
                                subtitle = "${exams.size} Scheduled Exams (Table Marksheet, Add, Edit, Delete)",
                                iconColor = Color(0xFF8E24AA),
                                onClick = { activeSubSection = "EXAMS" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.AutoStories,
                                title = t("courses"),
                                subtitle = "${courses.size} Active Courses (Add, Edit, Delete)",
                                iconColor = Color(0xFF0288D1),
                                onClick = { activeSubSection = "COURSES" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.Payments,
                                title = t("fees"),
                                subtitle = "Student Invoices, Vouchers & Collection",
                                iconColor = Color(0xFF2E7D32),
                                onClick = { activeSubSection = "FEES" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.PictureAsPdf,
                                title = t("pdf_backup"),
                                subtitle = "Export Complete Madrassa Report (Crash-free PDF)",
                                iconColor = Color(0xFFD32F2F),
                                onClick = { viewModel.generatePdfReport(context) }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.Badge,
                                title = "ID Cards Generator",
                                subtitle = "Generate & Export Printable Student & Teacher ID Cards",
                                iconColor = Color(0xFF00796B),
                                onClick = {
                                    if (students.isNotEmpty()) {
                                        showIdCardStudent = students.first()
                                    } else {
                                        viewModel.generateIdCardsPdf(context)
                                    }
                                }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.FolderShared,
                                title = "Image Storage & Media Folder",
                                subtitle = "Browse Saved Photos & App Directory Records",
                                iconColor = EmeraldPrimary,
                                onClick = { activeSubSection = "IMAGE_STORAGE" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.Backup,
                                title = t("backup_restore"),
                                subtitle = "100% Offline SQLite Backup & Restore (JSON / SQL)",
                                iconColor = EmeraldPrimary,
                                onClick = { activeSubSection = "BACKUP_RESTORE" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.DeleteSweep,
                                title = t("recycle_bin"),
                                subtitle = "${recycleStudents.size + recycleTeachers.size} deleted items",
                                iconColor = Color(0xFFE65100),
                                onClick = { activeSubSection = "RECYCLE_BIN" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.Settings,
                                title = t("settings"),
                                subtitle = "Upload Madrassa Logo, Change Name, Contact & Info",
                                iconColor = EmeraldPrimary,
                                onClick = { activeSubSection = "SETTINGS" }
                            )
                            Divider(modifier = Modifier.padding(horizontal = 16.dp))
                            MenuItemRow(
                                icon = Icons.Default.Translate,
                                title = t("lang_name"),
                                subtitle = if (LanguageState.isUrdu) "اردو (Urdu RTL) - کلک کر کے انگریزی کریں" else "English (LTR) - Click to switch to Urdu",
                                iconColor = Color(0xFF00796B),
                                onClick = {
                                    LanguageState.toggle()
                                    Toast.makeText(context, "Language switched!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    showIdCardStudent?.let { st ->
        IdCardDialog(
            student = st,
            profile = profile,
            viewModel = viewModel,
            onDismiss = { showIdCardStudent = null }
        )
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Madrassa3dIconOrb(
            icon = icon,
            tint = iconColor,
            borderColor = iconColor.copy(alpha = 0.5f),
            size = 40.dp,
            iconSize = 20.dp
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// 1. Teachers SubScreen with Add, Edit, Delete and Photo Upload
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersSubScreen(
    teachers: List<Teacher>,
    onBack: () -> Unit,
    onTeacherClick: (Teacher) -> Unit,
    onSaveTeacher: (Teacher, Boolean) -> Unit,
    onDeleteTeacher: (Long) -> Unit
) {
    var teacherToEdit by remember { mutableStateOf<Teacher?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var teacherToDelete by remember { mutableStateOf<Teacher?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = t("asatiza"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (UserRoleManager.isAdmin) {
                        IconButton(onClick = {
                            teacherToEdit = null
                            showDialog = true
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Teacher")
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
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(teachers) { teacher ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTeacherClick(teacher) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (teacher.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageStorageHelper.toImageModel(teacher.photoUri),
                                    contentDescription = teacher.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = teacher.name.firstOrNull()?.toString() ?: "U",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = teacher.name, fontWeight = FontWeight.Bold)
                            Text(text = "${teacher.role} • ${teacher.qualification}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Class: ${teacher.assignedDepartment} (${teacher.assignedSections})", fontSize = 11.5.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = "Phone: ${teacher.phone}  |  Rs. ${teacher.monthlySalary.toInt()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (UserRoleManager.isAdmin) {
                            Row {
                                IconButton(onClick = {
                                    teacherToEdit = teacher
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Teacher", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { teacherToDelete = teacher }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Teacher", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                                }
                            }
                        } else {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Open Class", tint = EmeraldPrimary)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        val context = LocalContext.current
        var tName by remember { mutableStateOf(teacherToEdit?.name ?: "") }
        var tPhone by remember { mutableStateOf(teacherToEdit?.phone ?: "0300-") }
        var tDept by remember { mutableStateOf(teacherToEdit?.assignedDepartment ?: "Hifz") }
        var tSalary by remember { mutableStateOf(teacherToEdit?.monthlySalary?.toInt()?.toString() ?: "40000") }
        var tQual by remember { mutableStateOf(teacherToEdit?.qualification ?: "Al-Alimiyyah") }
        var tRole by remember { mutableStateOf(teacherToEdit?.role ?: "Ustaz") }
        var tPhotoUri by remember { mutableStateOf(teacherToEdit?.photoUri ?: "") }

        val teacherImagePickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                val savedPath = ImageStorageHelper.saveImageToInternalStorage(
                    context = context,
                    sourceUri = uri,
                    subFolder = "teacher_photos",
                    prefix = "teacher"
                )
                if (savedPath != null) {
                    tPhotoUri = savedPath
                    Toast.makeText(context, "Teacher photo uploaded!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (teacherToEdit != null) t("edit_teacher") else t("add_teacher"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EmeraldPrimary
                    )

                    // Teacher Photo Upload Box
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimaryContainer)
                                .clickable { teacherImagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (tPhotoUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageStorageHelper.toImageModel(tPhotoUri),
                                    contentDescription = "Teacher Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Upload Photo", tint = EmeraldPrimary)
                            }
                        }
                        TextButton(onClick = { teacherImagePickerLauncher.launch("image/*") }) {
                            Text(if (tPhotoUri.isBlank()) t("upload_photo") else t("change_photo"), fontSize = 11.5.sp)
                        }
                    }

                    OutlinedTextField(value = tName, onValueChange = { tName = it }, label = { Text(t("name")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tPhone, onValueChange = { tPhone = it }, label = { Text(t("phone")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tDept, onValueChange = { tDept = it }, label = { Text(t("department")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tQual, onValueChange = { tQual = it }, label = { Text("Qualification (الشهادة)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tRole, onValueChange = { tRole = it }, label = { Text("Role (Ustaz / Ustaza / Mohtamim)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tSalary, onValueChange = { tSalary = it }, label = { Text("Monthly Salary (PKR)") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDialog = false }) { Text(t("cancel")) }
                        Button(
                            onClick = {
                                if (tName.isNotBlank()) {
                                    val teacher = teacherToEdit?.copy(
                                        name = tName,
                                        phone = tPhone,
                                        assignedDepartment = tDept,
                                        qualification = tQual,
                                        role = tRole,
                                        photoUri = tPhotoUri,
                                        monthlySalary = tSalary.toDoubleOrNull() ?: 35000.0
                                    ) ?: Teacher(
                                        name = tName,
                                        phone = tPhone,
                                        assignedDepartment = tDept,
                                        qualification = tQual,
                                        role = tRole,
                                        photoUri = tPhotoUri,
                                        monthlySalary = tSalary.toDoubleOrNull() ?: 35000.0
                                    )
                                    onSaveTeacher(teacher, teacherToEdit != null)
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(t("save"))
                        }
                    }
                }
            }
        }
    }

    teacherToDelete?.let { tch ->
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            title = { Text(t("delete")) },
            text = { Text(t("delete_confirm")) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTeacher(tch.id)
                        teacherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { teacherToDelete = null }) { Text(t("cancel")) }
            }
        )
    }
}

// 2. Exams SubScreen with Table Marksheet (Name, Father Name, Class, Paray, Q1, Q2, Q3, Talaffuz, Kul Number, Kaifiyat)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsSubScreen(
    exams: List<Exam>,
    students: List<Student>,
    viewModel: MadrassaViewModel,
    onBack: () -> Unit,
    onAddExam: (Exam) -> Unit,
    onUpdateExam: (Exam) -> Unit,
    onDeleteExam: (Long) -> Unit
) {
    var examToEdit by remember { mutableStateOf<Exam?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var examToDelete by remember { mutableStateOf<Exam?>(null) }
    var selectedExamForTable by remember { mutableStateOf<Exam?>(null) }
    var showOcrScannerDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "HIFZ_ORAL", "TAJWEED_TEST", "MID_TERM", "ANNUAL"

    val filteredExams = remember(exams, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") exams
        else exams.filter {
            when (selectedCategoryFilter) {
                "HIFZ_ORAL" -> it.type.equals("HIFZ_ORAL", true) ||
                        it.type.contains("ORAL", true) ||
                        it.type.contains("HIFZ", true) ||
                        it.title.contains("Oral", true) ||
                        it.title.contains("Hifz", true) ||
                        it.title.contains("زبانی", true) ||
                        it.title.contains("حفظ", true)
                "TAJWEED_TEST" -> it.type.equals("TAJWEED_TEST", true) ||
                        it.title.contains("Tajweed", true) ||
                        it.title.contains("تجوید", true)
                "MID_TERM" -> it.type.equals("MID_TERM", true) ||
                        it.title.contains("Mid", true) ||
                        it.title.contains("ششماہی", true)
                "ANNUAL" -> it.type.equals("ANNUAL", true) ||
                        it.title.contains("Annual", true) ||
                        it.title.contains("سالانہ", true)
                else -> it.type.equals(selectedCategoryFilter, true) || it.title.contains(selectedCategoryFilter, true)
            }
        }
    }

    // If an exam is selected, show its full Exam Marksheet Table
    if (selectedExamForTable != null) {
        val exam = selectedExamForTable!!
        val results by viewModel.getResultsForExam(exam.id).collectAsState(initial = emptyList())
        val context = LocalContext.current

        BackHandler { selectedExamForTable = null }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(text = exam.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "امتحانی جدول • ${exam.section}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedExamForTable = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showOcrScannerDialog = true }) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Written List", tint = Color.White)
                        }
                        IconButton(onClick = { viewModel.generateClassExamMarksheetPdf(context, exam) }) {
                            Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = Color.White)
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
        ) { p ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(p)
                    .padding(14.dp)
            ) {
                ExamResultsTableView(
                    exam = exam,
                    results = results,
                    students = students,
                    onSaveResult = { res, isEdit -> viewModel.saveExamResult(res, isEdit) },
                    onDeleteResult = { id -> viewModel.deleteExamResult(id) },
                    onAutoPopulateClass = { viewModel.autoPopulateExamForClass(exam) }
                )
            }

            if (showOcrScannerDialog) {
                ExamListScannerDialog(
                    exam = exam,
                    viewModel = viewModel,
                    onDismiss = { showOcrScannerDialog = false }
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = t("exams"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = {
                        examToEdit = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exam")
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
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category Filter Pills: All, Hifz Oral, Tajweed Test, Mid Term, Annual
            item {
                Column {
                    Text(
                        text = "Exams & Oral Assessments (امتحانات و زبانی جائزے):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterOptions = listOf(
                            "ALL" to "All Exams (${exams.size})",
                            "HIFZ_ORAL" to "Hifz Oral (${exams.count { it.type == "HIFZ_ORAL" || it.title.contains("Oral", ignoreCase = true) || it.title.contains("Hifz", ignoreCase = true) }})",
                            "TAJWEED_TEST" to "Tajweed Test (${exams.count { it.type == "TAJWEED_TEST" || it.title.contains("Tajweed", ignoreCase = true) }})",
                            "MID_TERM" to "Mid Term (${exams.count { it.type == "MID_TERM" }})",
                            "ANNUAL" to "Annual (${exams.count { it.type == "ANNUAL" }})"
                        )
                        items(filterOptions) { (key, label) ->
                            FilterChip(
                                selected = selectedCategoryFilter == key,
                                onClick = { selectedCategoryFilter = key },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selectedCategoryFilter == key) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Prominent Hifz Oral Assessment Spotlight Card (Always visible and easy to access)
            item {
                val hifzExam = exams.find {
                    it.type == "HIFZ_ORAL" || it.title.contains("Oral", true) || it.title.contains("Hifz", true)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF042418)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD4AF37)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFF042418),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "حفظ زبانی جائزہ (Hifz Oral Assessment)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = Color(0xFFD4AF37)
                                    )
                                    Text(
                                        text = "Oral evaluation: Q1, Q2, Q3 + Tajweed & Talaffuz (100 Marks)",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (hifzExam != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${hifzExam.title} • ${hifzExam.section}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Button(
                                    onClick = { selectedExamForTable = hifzExam },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF042418), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Marksheet (جدول کھولیں)", color = Color(0xFF042418), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ready to evaluate Hifz students",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )

                                Button(
                                    onClick = {
                                        viewModel.addExam(
                                            Exam(
                                                id = 0,
                                                title = "Hifz Oral Assessment (Para 29-30)",
                                                type = "HIFZ_ORAL",
                                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                                department = "Hifz",
                                                section = "Class 1A",
                                                totalMarks = 100,
                                                passMarks = 50,
                                                status = "SCHEDULED"
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF042418), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Hifz Oral Exam", color = Color(0xFF042418), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (filteredExams.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No exams found for this category.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    examToEdit = null
                                    showDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Text("Create Exam / Assessment")
                            }
                        }
                    }
                }
            } else {
                items(filteredExams) { exam ->
                    Madrassa3dCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedExamForTable = exam },
                        shape = RoundedCornerShape(16.dp),
                        elevation = 5.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exam.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    val typeDisplay = when (exam.type) {
                                        "HIFZ_ORAL" -> "حفظ زبانی جائزہ (Hifz Oral Assessment)"
                                        "TAJWEED_TEST" -> "تجوید ٹیسٹ (Tajweed Test)"
                                        "ANNUAL" -> "سالانہ امتحان (Annual Exam)"
                                        else -> "ششماہی امتحان (Mid-Term Exam)"
                                    }
                                    Text(
                                        text = typeDisplay,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFE5C07B),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusChip(status = exam.status)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = {
                                        examToEdit = exam
                                        showDialog = true
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Exam", tint = Color(0xFFE5C07B), modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { examToDelete = exam }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = Color(0xFFFF8A80), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Date: ${exam.date}  |  Class/Section: ${exam.section}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Total Marks: ${exam.totalMarks}  |  Passing Marks: ${exam.passMarks}",
                                fontSize = 12.sp,
                                color = Color(0xFFE5C07B),
                                fontWeight = FontWeight.SemiBold
                            )

                            val examResults by viewModel.getResultsForExam(exam.id).collectAsState(initial = emptyList())
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF042418).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFE5C07B).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Real-Time: ${examResults.size} Students Evaluated",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE5C07B)
                                    )
                                }
                                if (examResults.isNotEmpty()) {
                                    Text(
                                        text = "Pass: ${examResults.count { it.isPassed }} | Fail: ${examResults.count { !it.isPassed }}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                } else {
                                    Text(
                                        text = "Pending (تیار)",
                                        fontSize = 10.5.sp,
                                        color = Color.White.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Open Table Button
                            Button(
                                onClick = { selectedExamForTable = exam },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF042418), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "امتحانی مارک شیٹ جدول دیکھیں (Open Marksheet Table)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF042418)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf(examToEdit?.title ?: "") }
        var examType by remember { mutableStateOf(examToEdit?.type ?: "HIFZ_ORAL") }
        var date by remember { mutableStateOf(examToEdit?.date ?: "2025-10-25") }
        var dept by remember { mutableStateOf(examToEdit?.department ?: "Hifz") }
        var section by remember { mutableStateOf(examToEdit?.section ?: "Class 1A") }
        var totalMarks by remember { mutableStateOf(examToEdit?.totalMarks?.toString() ?: "100") }
        var passMarks by remember { mutableStateOf(examToEdit?.passMarks?.toString() ?: "40") }
        var status by remember { mutableStateOf(examToEdit?.status ?: "PUBLISHED") }

        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (examToEdit != null) t("edit_exam") else t("create_exam"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EmeraldPrimary
                    )
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Exam Title (امتحان کا نام)") }, modifier = Modifier.fillMaxWidth())

                    Text(text = "Assessment / Exam Type (نوعیت امتحان):", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val types = listOf(
                            "HIFZ_ORAL" to "Hifz Oral (حفظ زبانی)",
                            "TAJWEED_TEST" to "Tajweed Test (تجوید ٹیسٹ)",
                            "MID_TERM" to "Mid-Term (ششماہی)",
                            "ANNUAL" to "Annual (سالانہ)"
                        )
                        items(types) { (key, label) ->
                            FilterChip(
                                selected = examType == key,
                                onClick = {
                                    examType = key
                                    if (title.isBlank()) {
                                        title = when (key) {
                                            "HIFZ_ORAL" -> "Hifz Oral Assessment (زبانی جائزہ)"
                                            "TAJWEED_TEST" -> "Tajweed & Makharij Test (تجوید ٹیسٹ)"
                                            "ANNUAL" -> "Annual Examination (سالانہ امتحان)"
                                            else -> "Mid-Term Examination (ششماہی امتحان)"
                                        }
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dept, onValueChange = { dept = it }, label = { Text(t("department")) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = section, onValueChange = { section = it }, label = { Text(t("section")) }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = totalMarks, onValueChange = { totalMarks = it }, label = { Text("Total Marks") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = passMarks, onValueChange = { passMarks = it }, label = { Text("Pass Marks") }, modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDialog = false }) { Text(t("cancel")) }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val exam = examToEdit?.copy(
                                        title = title,
                                        type = examType,
                                        date = date,
                                        department = dept,
                                        section = section,
                                        totalMarks = totalMarks.toIntOrNull() ?: 100,
                                        passMarks = passMarks.toIntOrNull() ?: 40,
                                        status = status
                                    ) ?: Exam(
                                        id = 0,
                                        title = title,
                                        type = examType,
                                        date = date,
                                        department = dept,
                                        section = section,
                                        totalMarks = totalMarks.toIntOrNull() ?: 100,
                                        passMarks = passMarks.toIntOrNull() ?: 40,
                                        status = status
                                    )
                                    if (examToEdit != null) onUpdateExam(exam) else onAddExam(exam)
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(t("save"))
                        }
                    }
                }
            }
        }
    }

    examToDelete?.let { exam ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text(t("delete_exam")) },
            text = { Text("Are you sure you want to delete ${exam.title}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExam(exam.id)
                        examToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { examToDelete = null }) { Text(t("cancel")) }
            }
        )
    }
}

// 3. Courses / Classes SubScreen with Add, Edit, Delete
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesSubScreen(
    courses: List<Course>,
    onBack: () -> Unit,
    onSaveCourse: (Course, Boolean) -> Unit,
    onDeleteCourse: (Long) -> Unit
) {
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var courseToDelete by remember { mutableStateOf<Course?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = t("courses"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = {
                        courseToEdit = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Course")
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
    ) { p ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(p),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(courses) { course ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = course.nameEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = course.nameUr,
                                    fontSize = 14.sp,
                                    color = Color(0xFFE5C07B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row {
                                IconButton(onClick = {
                                    courseToEdit = course
                                    showDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Course", tint = Color(0xFFE5C07B), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { courseToDelete = course }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Course", tint = Color(0xFFFF8A80), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = course.description,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Instructor: ${course.teacherName}  |  Duration: ${course.duration}",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "Monthly Fee: Rs. ${course.monthlyFee.toInt()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B)
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        var nameEn by remember { mutableStateOf(courseToEdit?.nameEn ?: "") }
        var nameUr by remember { mutableStateOf(courseToEdit?.nameUr ?: "") }
        var desc by remember { mutableStateOf(courseToEdit?.description ?: "") }
        var teacher by remember { mutableStateOf(courseToEdit?.teacherName ?: "") }
        var duration by remember { mutableStateOf(courseToEdit?.duration ?: "1 Year") }
        var fee by remember { mutableStateOf(courseToEdit?.monthlyFee?.toInt()?.toString() ?: "2500") }

        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (courseToEdit != null) "Edit Course / Class" else "Add New Course / Class",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EmeraldPrimary
                    )
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Course Name (English)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nameUr, onValueChange = { nameUr = it }, label = { Text("Course Name (Urdu)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Assigned Teacher") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = fee, onValueChange = { fee = it }, label = { Text("Fee (Rs)") }, modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDialog = false }) { Text(t("cancel")) }
                        Button(
                            onClick = {
                                if (nameEn.isNotBlank()) {
                                    val course = courseToEdit?.copy(
                                        nameEn = nameEn,
                                        nameUr = nameUr,
                                        description = desc,
                                        teacherName = teacher,
                                        duration = duration,
                                        monthlyFee = fee.toDoubleOrNull() ?: 2500.0
                                    ) ?: Course(
                                        id = 0,
                                        nameEn = nameEn,
                                        nameUr = nameUr,
                                        description = desc,
                                        teacherName = teacher,
                                        duration = duration,
                                        monthlyFee = fee.toDoubleOrNull() ?: 2500.0
                                    )
                                    onSaveCourse(course, courseToEdit != null)
                                    showDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(t("save"))
                        }
                    }
                }
            }
        }
    }

    courseToDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { courseToDelete = null },
            title = { Text(t("delete")) },
            text = { Text("Are you sure you want to delete ${course.nameEn}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCourse(course.id)
                        courseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(t("delete"))
                }
            },
            dismissButton = {
                TextButton(onClick = { courseToDelete = null }) { Text(t("cancel")) }
            }
        )
    }
}

// 4. Profile Settings SubScreen with Madrassa Logo Upload
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsSubScreen(
    profile: MadrassaProfile,
    students: List<Student> = emptyList(),
    viewModel: MadrassaViewModel,
    onBack: () -> Unit,
    onSaveProfile: (MadrassaProfile) -> Unit
) {
    val context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }

    var nameEn by remember { mutableStateOf(profile.nameEn) }
    var nameUr by remember { mutableStateOf(profile.nameUr) }
    var mottoUr by remember { mutableStateOf(profile.mottoUr) }
    var mottoEn by remember { mutableStateOf(profile.mottoEn) }
    var principal by remember { mutableStateOf(profile.principalName) }
    var phone1 by remember { mutableStateOf(profile.phone1) }
    var phone2 by remember { mutableStateOf(profile.phone2) }
    var email by remember { mutableStateOf(profile.email) }
    var regNo by remember { mutableStateOf(profile.registrationNo) }
    var addressEn by remember { mutableStateOf(profile.addressEn) }
    var addressUr by remember { mutableStateOf(profile.addressUr) }
    var session by remember { mutableStateOf(profile.sessionYear) }
    var footer by remember { mutableStateOf(profile.receiptFooter) }
    var logoUri by remember { mutableStateOf(profile.logoUri) }

    // Universal SAF GetContent image picker for Madrassa Logo
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(
                context = context,
                sourceUri = uri,
                subFolder = "madrassa_logos",
                prefix = "logo"
            )
            if (savedPath != null) {
                logoUri = savedPath
                Toast.makeText(context, "Madrassa Logo updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Madrassa Profile & Logo Settings", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = {
                        val updated = profile.copy(
                            nameEn = nameEn,
                            nameUr = nameUr,
                            mottoUr = mottoUr,
                            mottoEn = mottoEn,
                            principalName = principal,
                            phone1 = phone1,
                            phone2 = phone2,
                            email = email,
                            registrationNo = regNo,
                            addressEn = addressEn,
                            addressUr = addressUr,
                            sessionYear = session,
                            receiptFooter = footer,
                            logoUri = logoUri
                        )
                        onSaveProfile(updated)
                        Toast.makeText(context, t("success_saved"), Toast.LENGTH_SHORT).show()
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
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
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 0. Dedicated Cloud Data Synchronization Card (The ONLY place for Synchronize)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Cloud Sync",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (LanguageState.isUrdu) "کلاؤڈ سنکرونائزیشن" else "Cloud Data Synchronization",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = EmeraldPrimary
                                )
                                Text(
                                    text = if (syncState.isOnline) "🟢 Online • Connected to Internet" else "⚡ Offline Mode • Local SQLite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                            }
                        }

                        // Synchronize Button
                        Button(
                            onClick = {
                                if (syncState.currentUserEmail != null) {
                                    viewModel.triggerManualSync()
                                    Toast.makeText(context, "Syncing data with cloud...", Toast.LENGTH_SHORT).show()
                                } else {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        isSigningIn = true
                                        viewModel.authManager.signInWithGoogle(
                                            activity = activity,
                                            scope = coroutineScope,
                                            onSuccess = {
                                                isSigningIn = false
                                                viewModel.triggerManualSync()
                                                Toast.makeText(context, "Signed in & sync started!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = {
                                                isSigningIn = false
                                                Toast.makeText(context, "Sign-in cancelled or failed", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            enabled = syncState.status != com.example.data.sync.SyncStatus.SYNCING && !isSigningIn
                        ) {
                            if (syncState.status == com.example.data.sync.SyncStatus.SYNCING || isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Syncing...", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (syncState.currentUserEmail != null) "Synchronize" else "Sign In & Sync",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val lastSyncStr = if (syncState.lastSyncTime > 0) {
                        val diff = (System.currentTimeMillis() - syncState.lastSyncTime) / 1000
                        if (diff < 60) "Just now" else "${diff / 60}m ago"
                    } else "Never"

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Account: ${syncState.currentUserEmail ?: "Unconnected"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Last synced: $lastSyncStr",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Live Preview Card (Emerald & Gold)
            Madrassa3dCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "LIVE EMBLEM PREVIEW", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE5C07B), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF1B6B4F), Color(0xFF042418))))
                                .border(1.5.dp, Color(0xFFE5C07B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageStorageHelper.toImageModel(logoUri),
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFFE5C07B), modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (nameUr.isNotBlank()) nameUr else "جامعہ دارالعلوم الاسلامیہ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFE5C07B))
                            Text(text = if (nameEn.isNotBlank()) nameEn else "Jamia Darul Uloom Al-Islamia", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                            Text(text = if (mottoUr.isNotBlank()) mottoUr else "علم • تربیت • بہترین مستقبل", fontSize = 10.5.sp, color = Color.White.copy(alpha = 0.75f))
                            Text(text = "Reg: $regNo  •  Session: $session", fontSize = 10.sp, color = Color(0xFFE5C07B).copy(alpha = 0.85f))
                        }
                    }
                }
            }

            // Madrassa Logo Upload Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Madrassa Official Logo / Emblem", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Appears in Top Header, PDF Report Cards, ID Cards & Fee Vouchers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimaryContainer)
                            .clickable {
                                logoPickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageStorageHelper.toImageModel(logoUri),
                                contentDescription = "Madrassa Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Upload Madrassa Logo",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { logoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (logoUri.isBlank()) t("upload_logo") else t("change_logo"), fontSize = 12.sp)
                        }

                        if (logoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { logoUri = "" }) {
                                Text(text = t("delete"), color = Color(0xFFD32F2F), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Names & Mottos
            OutlinedTextField(value = nameUr, onValueChange = { nameUr = it }, label = { Text("Madrassa Full Name in Urdu (جامعہ کا مکمل نام)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Madrassa Full Name in English") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = mottoUr, onValueChange = { mottoUr = it }, label = { Text("Motto in Urdu (علم • تربیت • بہترین مستقبل)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = mottoEn, onValueChange = { mottoEn = it }, label = { Text("Motto in English") }, modifier = Modifier.fillMaxWidth())

            // Principal & Registration
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = principal, onValueChange = { principal = it }, label = { Text(t("principal_name")) }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = regNo, onValueChange = { regNo = it }, label = { Text("Registration No") }, modifier = Modifier.weight(1f))
            }

            // Contact & Session
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = phone1, onValueChange = { phone1 = it }, label = { Text("Primary Phone") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = phone2, onValueChange = { phone2 = it }, label = { Text("Secondary Phone") }, modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = session, onValueChange = { session = it }, label = { Text(t("session_year")) }, modifier = Modifier.weight(1f))
            }

            // Addresses
            OutlinedTextField(value = addressUr, onValueChange = { addressUr = it }, label = { Text("Address in Urdu (پتہ اردو میں)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = addressEn, onValueChange = { addressEn = it }, label = { Text("Address in English") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = footer, onValueChange = { footer = it }, label = { Text("Receipt Footer Note (فیس رسید پر نوٹ)") }, modifier = Modifier.fillMaxWidth())

            // Madrassa Enrolled Students Roster Card with Scroll Feature
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Madrassa Enrolled Students (${students.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = EmeraldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Official enrolled student body registered under this Madrassa profile",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    var studentFilterQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = studentFilterQuery,
                        onValueChange = { studentFilterQuery = it },
                        placeholder = { Text("Search enrolled student name or roll...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (studentFilterQuery.isNotBlank()) {
                                IconButton(onClick = { studentFilterQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredProfileStudents = if (studentFilterQuery.isBlank()) students else students.filter {
                        it.name.contains(studentFilterQuery, ignoreCase = true) ||
                        it.rollNo.contains(studentFilterQuery, ignoreCase = true) ||
                        it.department.contains(studentFilterQuery, ignoreCase = true)
                    }

                    // Scrollable container to show all student names smoothly
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (filteredProfileStudents.isEmpty()) {
                                Text(
                                    text = "No enrolled students found",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            } else {
                                filteredProfileStudents.forEach { st ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldPrimaryContainer),
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
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = EmeraldPrimary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = st.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp,
                                                    softWrap = true
                                                )
                                                Text(
                                                    text = "Roll: ${st.rollNo} • ${st.department} (${st.section})" + (if (st.fatherName.isNotBlank()) " • ولد: ${st.fatherName}" else ""),
                                                    fontSize = 10.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val updated = profile.copy(
                        nameEn = nameEn,
                        nameUr = nameUr,
                        mottoUr = mottoUr,
                        mottoEn = mottoEn,
                        principalName = principal,
                        phone1 = phone1,
                        phone2 = phone2,
                        email = email,
                        registrationNo = regNo,
                        addressEn = addressEn,
                        addressUr = addressUr,
                        sessionYear = session,
                        receiptFooter = footer,
                        logoUri = logoUri
                    )
                    onSaveProfile(updated)
                    Toast.makeText(context, t("success_saved"), Toast.LENGTH_SHORT).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Save Profile & Logo")
            }
        }
    }
}

// 5. Recycle Bin SubScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinSubScreen(
    recycleStudents: List<Student>,
    recycleTeachers: List<Teacher>,
    onBack: () -> Unit,
    onRestoreStudent: (Long) -> Unit,
    onPermanentDeleteStudent: (Long) -> Unit,
    onRestoreTeacher: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = t("recycle_bin"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { p ->
        if (recycleStudents.isEmpty() && recycleTeachers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(p),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = t("empty_bin"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(p),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (recycleStudents.isNotEmpty()) {
                    item {
                        Text(text = "Deleted Students (${recycleStudents.size})", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                    items(recycleStudents) { st ->
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
                                    Text(text = st.name, fontWeight = FontWeight.Bold)
                                    Text(text = "Roll: ${st.rollNo}  |  Dept: ${st.department}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row {
                                    Button(
                                        onClick = { onRestoreStudent(st.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = t("restore"), fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { onPermanentDeleteStudent(st.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = t("permanent_delete"), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (recycleTeachers.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Deleted Teachers (${recycleTeachers.size})", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    }
                    items(recycleTeachers) { tch ->
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
                                    Text(text = tch.name, fontWeight = FontWeight.Bold)
                                    Text(text = "${tch.role}  |  Class: ${tch.assignedDepartment}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { onRestoreTeacher(tch.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(text = t("restore"), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
