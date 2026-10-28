package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.backup.BackupSummary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreSubScreen(
    viewModel: MadrassaViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val students by viewModel.allStudents.collectAsState()
    val studentLookup by viewModel.studentLookupMap.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val attendance by viewModel.studentAttendance.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val allExamResults by viewModel.allExamResults.collectAsState()
    val hifzList by viewModel.recentHifz.collectAsState()
    val nazraList by viewModel.recentNazra.collectAsState()
    val qaidaList by viewModel.recentQaida.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val profile by viewModel.profile.collectAsState()

    val studentsWithPhoto = students.count { it.photoUri.isNotBlank() }
    val teachersWithPhoto = teachers.count { it.photoUri.isNotBlank() }
    val totalPhotos = studentsWithPhoto + teachersWithPhoto + (if (profile.logoUri.isNotBlank()) 1 else 0)

    var isProcessing by remember { mutableStateOf(false) }
    var pendingExportType by remember { mutableStateOf<String?>(null) } // "JSON" or "SQL"
    var pendingExportContent by remember { mutableStateOf<String?>(null) }

    // Pending Import State
    var pendingImportContent by remember { mutableStateOf<String?>(null) }
    var pendingImportIsJson by remember { mutableStateOf(true) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }
    var restoredSummary by remember { mutableStateOf<BackupSummary?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Dedicated Student & Teacher Import State
    var pendingStudentImportContent by remember { mutableStateOf<String?>(null) }
    var showStudentImportDialog by remember { mutableStateOf(false) }
    var pendingTeacherImportContent by remember { mutableStateOf<String?>(null) }
    var showTeacherImportDialog by remember { mutableStateOf(false) }

    // Database Purge & Reset State
    var showPurgeConfirmDialog by remember { mutableStateOf(false) }
    var selectedPurgeMonths by remember { mutableIntStateOf(6) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var resetConfirmationInput by remember { mutableStateOf("") }
    var reseedAfterReset by remember { mutableStateOf(true) }

    // Inspection Dialog State
    var showInspectDataDialog by remember { mutableStateOf(false) }

    // SAF File Creator for Export
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null && pendingExportContent != null) {
            try {
                viewModel.backupManager.writeToUri(context, uri, pendingExportContent!!)
                Toast.makeText(context, "Backup file saved successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                pendingExportContent = null
                pendingExportType = null
            }
        }
    }

    // SAF File Picker for Import
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            try {
                val content = viewModel.backupManager.readFromUri(context, uri)
                if (content.isBlank()) {
                    Toast.makeText(context, "Selected file is empty!", Toast.LENGTH_SHORT).show()
                    isProcessing = false
                    return@rememberLauncherForActivityResult
                }

                val isJson = content.trim().startsWith("{")
                pendingImportContent = content
                pendingImportIsJson = isJson
                showImportConfirmDialog = true
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isProcessing = false
            }
        }
    }

    // SAF File Picker for Students JSON Import
    val importStudentsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = viewModel.backupManager.readFromUri(context, uri)
                if (content.isNotBlank()) {
                    pendingStudentImportContent = content
                    showStudentImportDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // SAF File Picker for Teachers JSON Import
    val importTeachersLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val content = viewModel.backupManager.readFromUri(context, uri)
                if (content.isNotBlank()) {
                    pendingTeacherImportContent = content
                    showTeacherImportDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Backup & Restore (بیک اپ و بحالی)", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showInspectDataDialog = true }) {
                        Icon(Icons.Default.Visibility, contentDescription = "Inspect Data", tint = Color.White)
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 0. Firebase Cloud Auto-Sync & Realtime Backup Banner
            item {
                val syncState by viewModel.syncState.collectAsState()
                val coroutineScope = rememberCoroutineScope()
                var isSigningIn by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Online + Offline Firebase Sync",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (syncState.isOnline) "🟢 Internet Connected (Online)" else "⚡ Offline Mode Active",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }
                            }

                            // Sync Status Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (syncState.isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ) {
                                Text(
                                    text = if (syncState.isOnline) "Auto-Sync ON" else "Local SQLite",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Work completely offline without any internet connection. Every attendance log, student profile, fee receipt, and exam record is safely stored locally in SQLite. The moment your mobile connects to Wi-Fi or mobile data, all records automatically sync to Firebase Cloud.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account status & Sync button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (syncState.currentUserEmail != null) {
                                    Text(
                                        text = "Cloud: ${syncState.currentUserEmail}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPrimary
                                    )
                                    val lastTimeStr = if (syncState.lastSyncTime > 0) {
                                        val diff = (System.currentTimeMillis() - syncState.lastSyncTime) / 1000
                                        if (diff < 60) "Just now" else "${diff / 60}m ago"
                                    } else "Ready to sync"
                                    Text(
                                        text = "Last synced: $lastTimeStr",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "Sign in to connect Google Cloud",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF512DA8)
                                    )
                                    Text(
                                        text = "Local data will automatically upload upon sign-in",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (syncState.currentUserEmail != null) {
                                Button(
                                    onClick = { viewModel.triggerManualSync() },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = syncState.status != com.example.data.sync.SyncStatus.SYNCING
                                ) {
                                    if (syncState.status == com.example.data.sync.SyncStatus.SYNCING) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Syncing...", fontSize = 11.5.sp)
                                    } else {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Now", fontSize = 11.5.sp)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = {
                                        isSigningIn = true
                                        val activity = context as? android.app.Activity
                                        if (activity != null) {
                                            viewModel.authManager.signInWithGoogle(
                                                activity = activity,
                                                scope = coroutineScope,
                                                onSuccess = {
                                                    isSigningIn = false
                                                    viewModel.triggerManualSync()
                                                },
                                                onError = {
                                                    isSigningIn = false
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !isSigningIn
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign In & Sync", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 1. Offline SQLite & Data Protection Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% Complete Offline Database Backup",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All student bio data, pictures, daily attendance, Sabaq progress, and exam marksheets are safely preserved locally.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // 2. Comprehensive Local Database Storage Breakdown (Shows EVERYTHING saved!)
            item {
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
                            Column {
                                Text(
                                    text = "Everything Saved in Local Storage",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                )
                                Text(
                                    text = "تمام محفوظ شدہ ریکارڈز اور بائیو ڈیٹا",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalButton(
                                onClick = { showInspectDataDialog = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inspect (دیکھیں)", fontSize = 11.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Row 1: Students & Bio Data + Photos
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailedDataCard(
                                icon = Icons.Default.People,
                                title = "Students Bio Data",
                                countText = "${students.size} Profiles",
                                subText = "B-Form, Guardian, DoB, Fee",
                                modifier = Modifier.weight(1f)
                            )
                            DetailedDataCard(
                                icon = Icons.Default.PhotoCamera,
                                title = "Saved Images",
                                countText = "$totalPhotos Photos",
                                subText = "Embedded Base64 in JSON",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 2: Attendance & Exam Progress
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailedDataCard(
                                icon = Icons.Default.EventAvailable,
                                title = "Attendance Logs",
                                countText = "${attendance.size} Records",
                                subText = "P / A / L / Lv with time",
                                modifier = Modifier.weight(1f)
                            )
                            DetailedDataCard(
                                icon = Icons.Default.AssignmentTurnedIn,
                                title = "Exam Marksheets",
                                countText = "${exams.size} Exams",
                                subText = "Oral & Written Evaluations",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 3: Sabaq Progress & Fees
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailedDataCard(
                                icon = Icons.Default.MenuBook,
                                title = "Sabaq Progress",
                                countText = "${hifzList.size + nazraList.size + qaidaList.size} Logs",
                                subText = "Hifz, Nazra, Qaida daily",
                                modifier = Modifier.weight(1f)
                            )
                            DetailedDataCard(
                                icon = Icons.Default.ReceiptLong,
                                title = "Fees & Faculty",
                                countText = "${vouchers.size} Invoices",
                                subText = "${teachers.size} Teachers, ${payments.size} Pays",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SQLite: madrassa_database.db • v3 (Offline)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "100% Included in Backup",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        }
                    }
                }
            }

            // 3. Export Section
            item {
                Text(
                    text = "Export Complete Backup (بیک اپ فائل بنائیں)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Export Option 1: JSON (Full Data + Images + Bio Data + Exams)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DataObject, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Complete JSON Backup (Recommended)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "تمام طلباء، تصاویر، حاضری، امتحانی مارک شیٹس اور بائیو ڈیٹا", fontSize = 11.sp, color = EmeraldPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Preserves 100% of information including student photos, guardians, CNIC/B-Forms, phone numbers, complete attendance, Sabaq progress, and all oral exam marks (Q1, Q2, Q3, Tajweed).",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportDatabaseJson { json ->
                                        isProcessing = false
                                        pendingExportContent = json
                                        pendingExportType = "JSON"
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        exportFileLauncher.launch("madrassa_complete_backup_$timeStr.json")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Save JSON", fontSize = 12.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportDatabaseJson { json ->
                                        isProcessing = false
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        viewModel.backupManager.shareBackupFile(
                                            context = context,
                                            content = json,
                                            filename = "madrassa_complete_backup_$timeStr.json",
                                            mimeType = "application/json"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Share JSON", fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }

            // Export Option 2: SQL Dump
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "SQL Script Dump (.sql)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Standard SQLite SQL script with INSERT statements for all database tables (Students, Exams, Attendance, Vouchers, Sabaq).",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportDatabaseSql { sql ->
                                        isProcessing = false
                                        pendingExportContent = sql
                                        pendingExportType = "SQL"
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        exportFileLauncher.launch("madrassa_dump_$timeStr.sql")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Save SQL", fontSize = 12.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportDatabaseSql { sql ->
                                        isProcessing = false
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        viewModel.backupManager.shareBackupFile(
                                            context = context,
                                            content = sql,
                                            filename = "madrassa_dump_$timeStr.sql",
                                            mimeType = "text/plain"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Share SQL", fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }

            // 4. Import & Restore Section
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Restore Database Backup (بیک اپ سے بحالی)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Import & Restore Backup File", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select any previously exported JSON or SQL file. It restores all student bio data, pictures, attendance logs, sabaq progress, and exam marksheets.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                importFileLauncher.launch(arrayOf("*/*", "application/json", "text/plain"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Choose Backup File (.json / .sql)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. Portable Students & Teachers JSON Backup (Safety Backup)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Students & Teachers Local JSON Backup (طلباء و اساتذہ بیک اپ)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PeopleAlt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Portable Student & Faculty JSON",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "طلباء اور اساتذہ کا آف لائن حفاظتی بیک اپ",
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Export or import standalone rosters for students (${students.size}) and teachers (${teachers.size}) as clean local JSON files with embedded photos and bio data.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Student Actions
                        Text(text = "Students Roster (${students.size} Enrolled):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportStudentsJson { json ->
                                        isProcessing = false
                                        pendingExportContent = json
                                        pendingExportType = "STUDENTS_JSON"
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        exportFileLauncher.launch("madrassa_students_$timeStr.json")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Students", fontSize = 11.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportStudentsJson { json ->
                                        isProcessing = false
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        viewModel.backupManager.shareBackupFile(
                                            context = context,
                                            content = json,
                                            filename = "madrassa_students_$timeStr.json",
                                            mimeType = "application/json"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 11.5.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    importStudentsLauncher.launch(arrayOf("*/*", "application/json"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", fontSize = 11.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Teacher Actions
                        Text(text = "Faculty & Teachers (${teachers.size} Teachers):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportTeachersJson { json ->
                                        isProcessing = false
                                        pendingExportContent = json
                                        pendingExportType = "TEACHERS_JSON"
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        exportFileLauncher.launch("madrassa_teachers_$timeStr.json")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Teachers", fontSize = 11.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    isProcessing = true
                                    viewModel.exportTeachersJson { json ->
                                        isProcessing = false
                                        val timeStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                                        viewModel.backupManager.shareBackupFile(
                                            context = context,
                                            content = json,
                                            filename = "madrassa_teachers_$timeStr.json",
                                            mimeType = "application/json"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 11.5.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    importTeachersLauncher.launch(arrayOf("*/*", "application/json"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }

            // 6. Database Cleanup & Old Records Purge
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Database Maintenance & Clean Up (ڈیٹا بیس کی صفائی)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Purge Historical Records Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Purge Old Historical Records",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "پرانے ریکارڈز، حاضری اور فیس کی صفائی",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Cleans old attendance logs, settled fee vouchers, and past sabaq entries to make the database fast and lightweight. Enrolled student profiles and teachers will NOT be removed.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Select Age Cutoff:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedPurgeMonths == 3,
                                onClick = { selectedPurgeMonths = 3 },
                                label = { Text("Older than 3 Months", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedPurgeMonths == 6,
                                onClick = { selectedPurgeMonths = 6 },
                                label = { Text("Older than 6 Months", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = selectedPurgeMonths == 12,
                                onClick = { selectedPurgeMonths = 12 },
                                label = { Text("Older than 1 Year", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showPurgeConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purge Records Older Than $selectedPurgeMonths Months", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.clearAllExamResults { count ->
                                    Toast.makeText(context, "Cleaned $count sample/test exam marks!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Sample / Test Exam Marks (امتحانی ریکارڈ صاف کریں)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }

            // Factory Reset Danger Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Reset All Database (مکمل ڈیٹا بیس ری سیٹ)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFFC62828)
                                )
                                Text(
                                    text = "Danger Zone • تمام ڈیٹا ڈیلیٹ کریں",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Deletes all students, teachers, attendance records, exam results, and fee vouchers from local storage. Requires typing 'CLEAR' to confirm.",
                            fontSize = 11.5.sp,
                            color = Color(0xFF5D1010)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                resetConfirmationInput = ""
                                showResetConfirmDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC62828)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All Database (فیکٹری ری سیٹ)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog before Restoring
    if (showImportConfirmDialog && pendingImportContent != null) {
        val isJson = pendingImportIsJson
        AlertDialog(
            onDismissRequest = {
                showImportConfirmDialog = false
                pendingImportContent = null
            },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(36.dp)) },
            title = { Text(text = "Restore Database Backup?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isJson) "Valid JSON backup file detected." else "SQL script detected.",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose how you want to restore the records into local SQLite storage:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportConfirmDialog = false
                        isProcessing = true
                        val content = pendingImportContent!!
                        if (isJson) {
                            viewModel.importDatabaseJson(
                                json = content,
                                replaceExisting = true,
                                onSuccess = { summary ->
                                    isProcessing = false
                                    restoredSummary = summary
                                    showSuccessDialog = true
                                    pendingImportContent = null
                                },
                                onError = { err ->
                                    isProcessing = false
                                    Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                    pendingImportContent = null
                                }
                            )
                        } else {
                            viewModel.importDatabaseSql(
                                sql = content,
                                onSuccess = { count ->
                                    isProcessing = false
                                    Toast.makeText(context, "SQL Dump Restored ($count statements executed)!", Toast.LENGTH_LONG).show()
                                    pendingImportContent = null
                                },
                                onError = { err ->
                                    isProcessing = false
                                    Toast.makeText(context, "SQL Error: $err", Toast.LENGTH_LONG).show()
                                    pendingImportContent = null
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text(text = "Replace & Restore")
                }
            },
            dismissButton = {
                Row {
                    if (isJson) {
                        TextButton(
                            onClick = {
                                showImportConfirmDialog = false
                                isProcessing = true
                                viewModel.importDatabaseJson(
                                    json = pendingImportContent!!,
                                    replaceExisting = false,
                                    onSuccess = { summary ->
                                        isProcessing = false
                                        restoredSummary = summary
                                        showSuccessDialog = true
                                        pendingImportContent = null
                                    },
                                    onError = { err ->
                                        isProcessing = false
                                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                        pendingImportContent = null
                                    }
                                )
                            }
                        ) {
                            Text(text = "Merge / Append")
                        }
                    }
                    TextButton(onClick = {
                        showImportConfirmDialog = false
                        pendingImportContent = null
                    }) {
                        Text(text = "Cancel")
                    }
                }
            }
        )
    }

    // Success Summary Dialog (Shows EVERYTHING that was restored!)
    restoredSummary?.let { summary ->
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    restoredSummary = null
                },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(44.dp)) },
                title = { Text(text = "Database Restored Successfully!") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(text = "Madrassa: ${summary.madrassaName}", fontWeight = FontWeight.Bold)
                        if (summary.exportedDate.isNotBlank()) {
                            Text(text = "Backup Date: ${summary.exportedDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(6.dp))

                        RestoreStatRow(label = "Students Bio Data:", value = "${summary.studentCount} students")
                        RestoreStatRow(label = "Images/Photos Restored:", value = "${summary.photoCount} photos")
                        RestoreStatRow(label = "Attendance History Logs:", value = "${summary.attendanceCount} records")
                        RestoreStatRow(label = "Exam & Oral Marksheets:", value = "${summary.examCount} exams (${summary.examResultCount} results)")
                        RestoreStatRow(label = "Hifz Progress Records:", value = "${summary.hifzCount} logs")
                        RestoreStatRow(label = "Nazra/Qaida/Tajweed:", value = "${summary.nazraCount + summary.qaidaCount + summary.tajweedCount} logs")
                        RestoreStatRow(label = "Teachers & Faculty:", value = "${summary.teacherCount} teachers")
                        RestoreStatRow(label = "Fee Invoices & Payments:", value = "${summary.voucherCount} inv, ${summary.paymentCount} pays")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            restoredSummary = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }

    // Inspect All Stored Data Dialog
    if (showInspectDataDialog) {
        Dialog(onDismissRequest = { showInspectDataDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                var inspectTab by remember { mutableIntStateOf(0) } // 0: Students Bio Data, 1: Exams & Progress, 2: Attendance

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Stored Backup Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldPrimary)
                            Text(text = "تمام محفوظ شدہ ڈیٹا کا جائزہ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { showInspectDataDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TabRow(
                        selectedTabIndex = inspectTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Tab(
                            selected = inspectTab == 0,
                            onClick = { inspectTab = 0 },
                            text = { Text("Students (${students.size})", fontSize = 11.sp, fontWeight = if (inspectTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = inspectTab == 1,
                            onClick = { inspectTab = 1 },
                            text = { Text("Attendance (${attendance.size})", fontSize = 11.sp, fontWeight = if (inspectTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = inspectTab == 2,
                            onClick = { inspectTab = 2 },
                            text = { Text("Exams (${allExamResults.size})", fontSize = 11.sp, fontWeight = if (inspectTab == 2) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = inspectTab == 3,
                            onClick = { inspectTab = 3 },
                            text = { Text("Sabaq (${hifzList.size + nazraList.size + qaidaList.size})", fontSize = 11.sp, fontWeight = if (inspectTab == 3) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (inspectTab) {
                        0 -> {
                            // Full Student Bio Data & Embedded Photos
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(students) { st ->
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Photo Thumbnail
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                        .background(EmeraldPrimaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (st.photoUri.isNotBlank()) {
                                                        coil.compose.AsyncImage(
                                                            model = com.example.util.ImageStorageHelper.toImageModel(st.photoUri),
                                                            contentDescription = st.name,
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = st.name.trim().take(1),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 18.sp,
                                                            color = EmeraldPrimary
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = st.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 15.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = if (st.photoUri.isNotBlank()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                                        ) {
                                                            Text(
                                                                text = if (st.photoUri.isNotBlank()) "Photo Saved" else "No Photo",
                                                                fontSize = 9.5.sp,
                                                                color = if (st.photoUri.isNotBlank()) Color(0xFF2E7D32) else Color(0xFFE65100),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "Roll: ${st.rollNo} • Class: ${st.department} (${st.section})",
                                                        fontSize = 11.5.sp,
                                                        color = EmeraldPrimary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                            Spacer(modifier = Modifier.height(4.dp))

                                            // Detailed Biodata
                                            Text(text = "ولد: ${st.fatherName} • سرپرست: ${st.guardianName} (${st.guardianPhone})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "شناختی کارڈ/بی فارم: ${st.cnicBForm.ifBlank { "N/A" }} • تاریخ پیدائش: ${st.dob}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "پتہ: ${st.address.ifBlank { "N/A" }} • جنس: ${st.gender}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "تاریخ داخلہ: ${st.admissionDate} • ماہانہ فیس: Rs. ${st.monthlyFee.toInt()} (رعایت: Rs. ${st.discount.toInt()})", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Attendance Logs with Student Avatar
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (attendance.isEmpty()) {
                                    item {
                                        Text(text = "حاضری کا ریکارڈ موجود نہیں ہے۔", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    items(attendance) { att ->
                                        val st = studentLookup[att.personId] ?: students.find { it.id == att.personId }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier.size(34.dp).clip(CircleShape).background(EmeraldPrimaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!st?.photoUri.isNullOrBlank()) {
                                                        coil.compose.AsyncImage(
                                                            model = com.example.util.ImageStorageHelper.toImageModel(st!!.photoUri),
                                                            contentDescription = null,
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                        )
                                                    } else {
                                                        Text(text = (st?.name ?: "S").trim().take(1), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldPrimary)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(text = st?.name ?: "Student #${att.personId}", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                                    Text(text = "Date: ${att.date} • Time: ${att.checkInTime.ifBlank { "08:00 AM" }} • ${att.remarks.ifBlank { "حاضری درج" }}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (att.status) {
                                                    "PRESENT" -> Color(0xFFE8F5E9)
                                                    "ABSENT" -> Color(0xFFFFEBEE)
                                                    "LATE" -> Color(0xFFFFF3E0)
                                                    else -> Color(0xFFE1F5FE)
                                                }
                                            ) {
                                                Text(
                                                    text = when (att.status) {
                                                        "PRESENT" -> "حاضر Present"
                                                        "ABSENT" -> "غائب Absent"
                                                        "LATE" -> "دیر Late"
                                                        "LEAVE" -> "رخصت Leave"
                                                        else -> att.status
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (att.status) {
                                                        "PRESENT" -> Color(0xFF2E7D32)
                                                        "ABSENT" -> Color(0xFFC62828)
                                                        "LATE" -> Color(0xFFE65100)
                                                        else -> Color(0xFF0288D1)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Exam Marksheets & Oral Evaluations
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (allExamResults.isEmpty()) {
                                    item {
                                        Text(text = "کوئی امتحانی مارک شیٹ درج نہیں ہے۔", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    items(allExamResults) { res ->
                                        val ex = exams.find { it.id == res.examId }
                                        val st = studentLookup[res.studentId] ?: students.find { it.id == res.studentId }
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = (res.studentName.ifBlank { st?.name ?: "Student #${res.studentId}" }) + (if (!st?.rollNo.isNullOrBlank()) " (${st!!.rollNo})" else ""),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (res.isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                                    ) {
                                                        Text(
                                                            text = "${res.kaifiyat} • ${if (res.isPassed) "کامیاب" else "راسب"}",
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (res.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "امتحان: ${ex?.title ?: "Exam #${res.examId}"} • نصاب/پارے: ${res.paray.ifBlank { "مکمل نصاب" }}",
                                                    fontSize = 11.sp,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "سوال اول: ${res.question1.toInt()} • سوال دوم: ${res.question2.toInt()} • سوال سوم: ${res.question3.toInt()} • تجوید/تلفظ: ${res.talaffuz.toInt()}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "کل نمبر: ${res.kulNumber.toInt()} / ${res.totalMaxMarks.toInt()} • گریڈ: ${res.grade} • کیفیات: ${res.remarks.ifBlank { "شاباش" }}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (res.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            // Sabaq Progress (Hifz, Nazra, Qaida)
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Hifz Logs
                                items(hifzList) { h ->
                                    val st = studentLookup[h.studentId] ?: students.find { it.id == h.studentId }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "حفظ: ${st?.name ?: "Student #${h.studentId}"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(text = "گریڈ: ${h.grade}", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 11.sp)
                                            }
                                            Text(text = "سبق: پارہ ${h.sabaqPara} (${h.sabaqSurah} ${h.sabaqAyahRange})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "سبقی: ${h.sabqiRange} • منزل: ${h.manzilParas} • غلطیاں: ${h.mistakesCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "کل مکمل پارے: ${h.totalParasCompleted} • تاریخ: ${h.date}", fontSize = 10.5.sp, color = EmeraldPrimary)
                                        }
                                    }
                                }

                                // Nazra Logs
                                items(nazraList) { n ->
                                    val st = studentLookup[n.studentId] ?: students.find { it.id == n.studentId }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "ناظرہ: ${st?.name ?: "Student #${n.studentId}"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(text = "گریڈ: ${n.grade}", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 11.sp)
                                            }
                                            Text(text = "موجودہ پارہ: ${n.currentPara} • صفحات: ${n.pageRange} • غلطیاں: ${n.mistakesCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "کیفیت: ${n.remarks} • تاریخ: ${n.date}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                // Qaida Logs
                                items(qaidaList) { q ->
                                    val st = studentLookup[q.studentId] ?: students.find { it.id == q.studentId }
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(text = "قاعدہ: ${st?.name ?: "Student #${q.studentId}"}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(text = q.passOrRepeat, fontWeight = FontWeight.Bold, color = if (q.passOrRepeat == "PASS") Color(0xFF2E7D32) else Color(0xFFC62828), fontSize = 11.sp)
                                            }
                                            Text(text = "سبق نمبر: ${q.lessonNo} • عنوان: ${q.topic}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "کیفیت: ${q.remarks} • تاریخ: ${q.date}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    // 1. Student Import Dialog
    if (showStudentImportDialog && pendingStudentImportContent != null) {
        val content = pendingStudentImportContent!!
        AlertDialog(
            onDismissRequest = {
                showStudentImportDialog = false
                pendingStudentImportContent = null
            },
            icon = { Icon(Icons.Default.PeopleAlt, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(36.dp)) },
            title = { Text(text = "Import Students JSON?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Valid Students JSON file selected.", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Choose how to import students into the local SQLite database:\n• Replace: Clears current student roster and installs imported records.\n• Merge / Append: Adds new students or updates existing roll numbers without deleting others.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStudentImportDialog = false
                        viewModel.importStudentsJson(
                            json = content,
                            replaceExisting = false,
                            onSuccess = { count ->
                                Toast.makeText(context, "Successfully imported $count students!", Toast.LENGTH_LONG).show()
                                pendingStudentImportContent = null
                            },
                            onError = { err ->
                                Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show()
                                pendingStudentImportContent = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Merge / Append")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showStudentImportDialog = false
                            viewModel.importStudentsJson(
                                json = content,
                                replaceExisting = true,
                                onSuccess = { count ->
                                    Toast.makeText(context, "Replaced student roster with $count students!", Toast.LENGTH_LONG).show()
                                    pendingStudentImportContent = null
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show()
                                    pendingStudentImportContent = null
                                }
                            )
                        }
                    ) {
                        Text("Replace All", color = Color(0xFFC62828))
                    }
                    TextButton(
                        onClick = {
                            showStudentImportDialog = false
                            pendingStudentImportContent = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // 2. Teacher Import Dialog
    if (showTeacherImportDialog && pendingTeacherImportContent != null) {
        val content = pendingTeacherImportContent!!
        AlertDialog(
            onDismissRequest = {
                showTeacherImportDialog = false
                pendingTeacherImportContent = null
            },
            icon = { Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(36.dp)) },
            title = { Text(text = "Import Teachers JSON?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Valid Teachers JSON file selected.", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Choose how to import faculty members into local SQLite storage:\n• Merge: Adds new teachers while preserving existing ones.\n• Replace: Replaces faculty records with the file content.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTeacherImportDialog = false
                        viewModel.importTeachersJson(
                            json = content,
                            replaceExisting = false,
                            onSuccess = { count ->
                                Toast.makeText(context, "Successfully imported $count teachers!", Toast.LENGTH_LONG).show()
                                pendingTeacherImportContent = null
                            },
                            onError = { err ->
                                Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show()
                                pendingTeacherImportContent = null
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Merge / Append")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showTeacherImportDialog = false
                            viewModel.importTeachersJson(
                                json = content,
                                replaceExisting = true,
                                onSuccess = { count ->
                                    Toast.makeText(context, "Replaced faculty with $count teachers!", Toast.LENGTH_LONG).show()
                                    pendingTeacherImportContent = null
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Import failed: $err", Toast.LENGTH_LONG).show()
                                    pendingTeacherImportContent = null
                                }
                            )
                        }
                    ) {
                        Text("Replace All", color = Color(0xFFC62828))
                    }
                    TextButton(
                        onClick = {
                            showTeacherImportDialog = false
                            pendingTeacherImportContent = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // 3. Purge Historical Records Dialog
    if (showPurgeConfirmDialog) {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, -selectedPurgeMonths)
        val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)

        AlertDialog(
            onDismissRequest = { showPurgeConfirmDialog = false },
            icon = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(36.dp)) },
            title = { Text(text = "Confirm Historical Data Purge") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Are you sure you want to clean old records?",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This will permanently remove attendance logs, settled fee vouchers, and sabaq progress entries logged prior to $cutoffDate ($selectedPurgeMonths months ago).\n\nActive student profiles, teachers, and current records will remain untouched.",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPurgeConfirmDialog = false
                        viewModel.purgeOldDatabaseRecords(
                            cutoffDate = cutoffDate,
                            onSuccess = { deleted ->
                                Toast.makeText(context, "Database cleaned: $deleted old records purged!", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, "Purge error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text("Clean Records Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Complete Database Reset Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(40.dp)) },
            title = { Text(text = "Factory Reset Entire Database?", color = Color(0xFFC62828)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This action will WIPE ALL database records including students, attendance, exams, fees, and teachers from this device.",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "To confirm, please type 'CLEAR' in capital letters below:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = resetConfirmationInput,
                        onValueChange = { resetConfirmationInput = it },
                        placeholder = { Text("Type CLEAR here") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = reseedAfterReset,
                            onCheckedChange = { reseedAfterReset = it }
                        )
                        Text(text = "Re-seed starter Madrassa data after reset", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (resetConfirmationInput.trim().equals("CLEAR", ignoreCase = false)) {
                            showResetConfirmDialog = false
                            viewModel.resetEntireDatabase(
                                reseed = reseedAfterReset,
                                onSuccess = {
                                    Toast.makeText(context, "Database reset complete!", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Reset failed: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Confirmation text must be 'CLEAR'!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = resetConfirmationInput.trim() == "CLEAR",
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("Wipe & Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailedDataCard(
    icon: ImageVector,
    title: String,
    countText: String,
    subText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = countText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldPrimary)
            Text(text = subText, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun RestoreStatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
