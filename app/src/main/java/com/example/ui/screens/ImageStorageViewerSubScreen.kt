package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class StoredImageItem(
    val file: File,
    val folderName: String,
    val sizeKb: Long,
    val modifiedDate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageStorageViewerSubScreen(
    students: List<Student> = emptyList(),
    viewModel: MadrassaViewModel? = null,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Student Photos, 1: Folders & Galleries
    var folderRefreshKey by remember { mutableIntStateOf(0) }
    var selectedImageForPreview by remember { mutableStateOf<StoredImageItem?>(null) }
    var imageToDelete by remember { mutableStateOf<File?>(null) }

    // Folder Management State
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderToEdit by remember { mutableStateOf<String?>(null) }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    var selectedFolderName by remember { mutableStateOf("student_photos") }

    // Student photo upload state
    var studentForPhotoUpload by remember { mutableStateOf<Student?>(null) }

    // Search query for student list
    var studentSearchQuery by remember { mutableStateOf("") }

    // Image Picker for Student Photo
    val studentPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && studentForPhotoUpload != null) {
            val targetStudent = studentForPhotoUpload!!
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(
                context = context,
                sourceUri = uri,
                subFolder = "student_photos",
                prefix = "student_${targetStudent.id}"
            )
            if (savedPath != null) {
                val updated = targetStudent.copy(photoUri = savedPath)
                viewModel?.updateStudent(updated)
                folderRefreshKey++
                Toast.makeText(context, "Photo updated for ${targetStudent.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
            }
            studentForPhotoUpload = null
        }
    }

    // Image Picker for Custom Folder Upload
    val folderImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(
                context = context,
                sourceUri = uri,
                subFolder = selectedFolderName,
                prefix = "img"
            )
            if (savedPath != null) {
                folderRefreshKey++
                Toast.makeText(context, "Image added to $selectedFolderName", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load all folders dynamically
    val availableFolders: List<String> = remember(folderRefreshKey) {
        ImageStorageHelper.getAllFolders(context)
    }

    // Load all images from all folders
    val storedImages = remember(folderRefreshKey, availableFolders) {
        val list = mutableListOf<StoredImageItem>()
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        availableFolders.forEach { folderName ->
            val dir = File(context.filesDir, folderName)
            if (dir.exists()) {
                dir.listFiles()?.forEach { file ->
                    if (file.isFile && (file.name.endsWith(".jpg") || file.name.endsWith(".png") ||
                                file.name.endsWith(".jpeg") || file.name.endsWith(".webp"))) {
                        list.add(
                            StoredImageItem(
                                file = file,
                                folderName = folderName,
                                sizeKb = file.length() / 1024,
                                modifiedDate = sdf.format(Date(file.lastModified()))
                            )
                        )
                    }
                }
            }
        }
        list
    }

    val totalBytes = storedImages.sumOf { it.file.length() }
    val totalSizeMb = String.format(Locale.US, "%.2f MB", totalBytes / (1024.0 * 1024.0))

    val filteredStudents = remember(students, studentSearchQuery) {
        if (studentSearchQuery.isBlank()) students
        else students.filter {
            it.name.contains(studentSearchQuery, ignoreCase = true) ||
            it.rollNo.contains(studentSearchQuery, ignoreCase = true) ||
            it.department.contains(studentSearchQuery, ignoreCase = true) ||
            it.fatherName.contains(studentSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "Internal Image Storage & Folders", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "App Private Directory: /data/user/0/files/", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateFolderDialog = true }) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = "New Folder", tint = Color.White)
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
            // Storage Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Secure Internal App Storage", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Text(text = "100% Offline Encrypted Private Storage", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Text(text = totalSizeMb, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Files", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${storedImages.size} Images", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "Folders Count", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${availableFolders.size} Folders", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column {
                            Text(text = "Enrolled Students", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${students.size} Profiles", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Tabs: 0 -> Student Photos (All student names scrollable), 1 -> Folders & Galleries (Folder add/edit/delete)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Student Photos (${students.size})", fontSize = 12.5.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Image Folders (${availableFolders.size})", fontSize = 12.5.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: STUDENT PHOTOS WITH SCROLL FEATURE TO SHOW ALL STUDENT NAMES
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = studentSearchQuery,
                            onValueChange = { studentSearchQuery = it },
                            placeholder = { Text("Search student name, roll number, or class (کلاس)...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (studentSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { studentSearchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "All Enrolled Students (${filteredStudents.size}):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (filteredStudents.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "No students found matching '$studentSearchQuery'", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            // Smoothly scrollable LazyColumn showing all student names
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredStudents, key = { it.id }) { student ->
                                    val hasPhoto = student.photoUri.isNotBlank()
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Photo Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldPrimaryContainer)
                                                    .clickable {
                                                        if (hasPhoto) {
                                                            val f = File(student.photoUri)
                                                            if (f.exists()) {
                                                                selectedImageForPreview = StoredImageItem(
                                                                    file = f,
                                                                    folderName = "student_photos",
                                                                    sizeKb = f.length() / 1024,
                                                                    modifiedDate = "Student Photo"
                                                                )
                                                            } else {
                                                                studentForPhotoUpload = student
                                                                studentPhotoPickerLauncher.launch("image/*")
                                                            }
                                                        } else {
                                                            studentForPhotoUpload = student
                                                            studentPhotoPickerLauncher.launch("image/*")
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (hasPhoto) {
                                                    AsyncImage(
                                                        model = ImageStorageHelper.toImageModel(student.photoUri),
                                                        contentDescription = student.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.AddAPhoto,
                                                        contentDescription = "Upload Photo",
                                                        tint = EmeraldPrimary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Full Student Name & Info (Expanded full width)
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = student.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    lineHeight = 22.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    softWrap = true
                                                )
                                                Text(
                                                    text = "Roll: ${student.rollNo} • ${student.department} (${student.section})",
                                                    fontSize = 11.5.sp,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = if (hasPhoto) "Stored in student_photos" else "No Photo Uploaded",
                                                    fontSize = 10.5.sp,
                                                    color = if (hasPhoto) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Action Buttons: Change Photo, Preview, Remove
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        studentForPhotoUpload = student
                                                        studentPhotoPickerLauncher.launch("image/*")
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CloudUpload,
                                                        contentDescription = "Upload Photo",
                                                        tint = EmeraldPrimary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }

                                                if (hasPhoto) {
                                                    IconButton(
                                                        onClick = {
                                                            val updated = student.copy(photoUri = "")
                                                            viewModel?.updateStudent(updated)
                                                            folderRefreshKey++
                                                            Toast.makeText(context, "Photo removed for ${student.name}", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "Remove Photo",
                                                            tint = Color(0xFFD32F2F).copy(alpha = 0.8f),
                                                            modifier = Modifier.size(20.dp)
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
                }
                1 -> {
                    // TAB 1: FOLDERS & GALLERIES WITH ADD, EDIT, DELETE FOLDER
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Folder Selector Horizontal Scroll Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Storage Folders:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(
                                onClick = { showCreateFolderDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Add Folder", fontSize = 12.sp, color = EmeraldPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Scrollable Folder Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableFolders) { folderName ->
                                val isSelected = folderName == selectedFolderName
                                val folderImagesCount = storedImages.count { it.folderName == folderName }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFolderName = folderName },
                                    label = {
                                        Text(
                                            text = "$folderName ($folderImagesCount)",
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else EmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Selected Folder Actions Card (Rename, Delete, Upload)
                        val currentFolderImages = storedImages.filter { it.folderName == selectedFolderName }
                        val currentFolderBytes = currentFolderImages.sumOf { it.file.length() }
                        val currentFolderSizeMb = String.format(Locale.US, "%.2f MB", currentFolderBytes / (1024.0 * 1024.0))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = selectedFolderName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = EmeraldPrimary
                                            )
                                            Text(
                                                text = "${currentFolderImages.size} files • $currentFolderSizeMb",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Action buttons for the folder
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Edit / Rename Folder
                                        IconButton(
                                            onClick = { folderToEdit = selectedFolderName },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename Folder",
                                                tint = EmeraldPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Delete Folder
                                        IconButton(
                                            onClick = { folderToDelete = selectedFolderName },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Folder",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Upload Image Button into Selected Folder
                                Button(
                                    onClick = { folderImagePickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Upload Image into '$selectedFolderName'", fontSize = 12.5.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Images Grid inside selected folder
                        if (currentFolderImages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(44.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "No images in '$selectedFolderName'", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "Tap 'Upload Image' above to add photos to this folder.", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentPadding = PaddingValues(bottom = 60.dp)
                            ) {
                                items(currentFolderImages) { item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.85f)
                                            .clickable { selectedImageForPreview = item },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFF1F8F5)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = item.file,
                                                    contentDescription = item.file.name,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = item.file.name.take(12),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${item.sizeKb} KB",
                                                    fontSize = 8.5.sp,
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
        }
    }

    // 1. CREATE NEW FOLDER DIALOG
    if (showCreateFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text(text = "Add New Image Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Create a custom folder in internal sandboxed storage to organize certificates, receipts, or event pictures.")
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name (e.g., certificates)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = newFolderName.trim()
                        if (clean.isNotBlank()) {
                            val success = ImageStorageHelper.createFolder(context, clean)
                            if (success) {
                                selectedFolderName = clean
                                folderRefreshKey++
                                Toast.makeText(context, "Folder '$clean' created!", Toast.LENGTH_SHORT).show()
                                showCreateFolderDialog = false
                            } else {
                                Toast.makeText(context, "Failed to create folder", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Create Folder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 2. EDIT / RENAME FOLDER DIALOG
    folderToEdit?.let { oldName ->
        var renameValue by remember { mutableStateOf(oldName) }
        AlertDialog(
            onDismissRequest = { folderToEdit = null },
            title = { Text(text = "Rename Image Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Enter a new name for the folder '$oldName':")
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it },
                        label = { Text("New Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clean = renameValue.trim()
                        if (clean.isNotBlank() && clean != oldName) {
                            val success = ImageStorageHelper.renameFolder(context, oldName, clean)
                            if (success) {
                                selectedFolderName = clean
                                folderRefreshKey++
                                Toast.makeText(context, "Folder renamed to '$clean'!", Toast.LENGTH_SHORT).show()
                                folderToEdit = null
                            } else {
                                Toast.makeText(context, "Could not rename folder", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToEdit = null }) { Text("Cancel") }
            }
        )
    }

    // 3. DELETE FOLDER CONFIRMATION DIALOG
    folderToDelete?.let { folderName ->
        val fileCount = storedImages.count { it.folderName == folderName }
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text(text = "Delete Folder '$folderName'?", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
            text = {
                Text(
                    text = if (fileCount > 0)
                        "Warning: This folder contains $fileCount image(s). Deleting it will permanently delete all files inside."
                    else
                        "Are you sure you want to delete the empty folder '$folderName'?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = ImageStorageHelper.deleteFolder(context, folderName)
                        if (success) {
                            selectedFolderName = "student_photos"
                            folderRefreshKey++
                            Toast.makeText(context, "Folder '$folderName' deleted!", Toast.LENGTH_SHORT).show()
                            folderToDelete = null
                        } else {
                            Toast.makeText(context, "Failed to delete folder", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete Folder")
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // 4. IMAGE FULL PREVIEW & DELETE DIALOG
    if (selectedImageForPreview != null) {
        val item = selectedImageForPreview!!
        Dialog(onDismissRequest = { selectedImageForPreview = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = item.file,
                        contentDescription = "Full Preview",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = item.file.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Folder: ${item.folderName}", fontSize = 11.5.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                    Text(text = "Path: ${item.file.absolutePath}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Size: ${item.sizeKb} KB  •  Modified: ${item.modifiedDate}", fontSize = 11.sp, color = EmeraldPrimary)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val success = ImageStorageHelper.deleteFile(item.file)
                                if (success) {
                                    folderRefreshKey++
                                    Toast.makeText(context, "Image deleted", Toast.LENGTH_SHORT).show()
                                    selectedImageForPreview = null
                                } else {
                                    Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }

                        Button(
                            onClick = { selectedImageForPreview = null },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(text = "Close Preview")
                        }
                    }
                }
            }
        }
    }
}
