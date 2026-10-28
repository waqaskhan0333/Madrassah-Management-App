package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.components.Madrassa3dBadge
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.Madrassa3dMetricTile
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import com.example.util.t
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternalPhotoManagerScreen(
    viewModel: MadrassaViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val allStudents by viewModel.allStudents.collectAsState()
    val allTeachers by viewModel.teachers.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var selectedFolder by remember { mutableStateOf("student_photos") }
    var refreshKey by remember { mutableIntStateOf(0) }
    var previewPhoto by remember { mutableStateOf<ImageStorageHelper.StoredPhotoInfo?>(null) }
    var photoToDelete by remember { mutableStateOf<ImageStorageHelper.StoredPhotoInfo?>(null) }

    val photos = remember(selectedFolder, refreshKey) {
        ImageStorageHelper.getPhotosInFolder(context, selectedFolder)
    }

    val totalSize = remember(refreshKey) {
        ImageStorageHelper.getTotalStorageSizeBytes(context)
    }

    val totalCount = remember(refreshKey) {
        val f1 = ImageStorageHelper.getPhotosInFolder(context, "student_photos").size
        val f2 = ImageStorageHelper.getPhotosInFolder(context, "teacher_photos").size
        val f3 = ImageStorageHelper.getPhotosInFolder(context, "madrassa_logos").size
        f1 + f2 + f3
    }

    val uploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val prefix = when (selectedFolder) {
                "student_photos" -> "student"
                "teacher_photos" -> "teacher"
                else -> "logo"
            }
            val path = ImageStorageHelper.saveImageToInternalStorage(context, uri, selectedFolder, prefix)
            if (path != null) {
                refreshKey++
                Toast.makeText(context, "Image uploaded to internal storage!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Internal Photo Storage",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFF0C2)
                        )
                        Text(
                            text = "فوٹو گیلری اور اسٹوریج مینجر • Offline Filesystem",
                            fontSize = 11.sp,
                            color = Color(0xFFE5C07B).copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFF0C2)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { uploadLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Photo",
                            tint = Color(0xFFE5C07B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF042418)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { uploadLauncher.launch("image/*") },
                containerColor = Color(0xFFE5C07B),
                contentColor = Color(0xFF042418),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload Image")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF031910))
        ) {
            // 1. Top 3D Metric Overview Tiles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Madrassa3dMetricTile(
                    title = "Total Photos",
                    value = "$totalCount",
                    icon = Icons.Default.PhotoLibrary,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFFE5C07B)
                )
                Madrassa3dMetricTile(
                    title = "Disk Usage",
                    value = ImageStorageHelper.formatFileSize(totalSize),
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF81C784)
                )
                Madrassa3dMetricTile(
                    title = "Active Folder",
                    value = "${photos.size}",
                    icon = Icons.Default.Folder,
                    modifier = Modifier.weight(1f),
                    accentColor = Color(0xFF4DD0E1)
                )
            }

            // 2. Folder Selection Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val folders = listOf(
                    "student_photos" to "Students (طلباء)",
                    "teacher_photos" to "Teachers (اساتذہ)",
                    "madrassa_logos" to "Logos (لوگو)"
                )
                folders.forEach { (key, label) ->
                    val isSelected = selectedFolder == key
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF1B6B4F) else Color(0xFF042418),
                        border = BorderStroke(
                            1.2.dp,
                            if (isSelected) Color(0xFFE5C07B) else Color(0xFFE5C07B).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedFolder = key }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFFFFF0C2) else Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Photo Grid
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFFE5C07B).copy(alpha = 0.4f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "اس فولڈر میں کوئی تصویر محفوظ نہیں\n(No photos saved in this internal folder)",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { uploadLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C07B))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF042418))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نئی تصویر شامل کریں (Upload Photo)", color = Color(0xFF042418), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos, key = { it.path }) { photo ->
                        val matchingStudent = remember(photo.path, allStudents) {
                            allStudents.find { it.photoUri == photo.path || photo.path.endsWith(it.photoUri.substringAfterLast("/")) }
                        }
                        val matchingTeacher = remember(photo.path, allTeachers) {
                            allTeachers.find { it.photoUri == photo.path || photo.path.endsWith(it.photoUri.substringAfterLast("/")) }
                        }

                        Madrassa3dCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { previewPhoto = photo },
                            shape = RoundedCornerShape(16.dp),
                            elevation = 5.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Thumbnail
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF042418)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = File(photo.path),
                                        contentDescription = photo.fileName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    // Size Tag Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.Black.copy(alpha = 0.7f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = ImageStorageHelper.formatFileSize(photo.sizeBytes),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFF0C2)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Linked Person
                                val personName = when {
                                    matchingStudent != null -> "${matchingStudent.name} (Roll #${matchingStudent.rollNo})"
                                    matchingTeacher != null -> "${matchingTeacher.name} (Teacher)"
                                    selectedFolder == "madrassa_logos" -> "Madrassa Official Logo"
                                    else -> "Unassigned (غیر منسلک)"
                                }

                                Text(
                                    text = personName,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(photo.lastModified))
                                Text(
                                    text = dateStr,
                                    fontSize = 9.5.sp,
                                    color = Color(0xFFE5C07B).copy(alpha = 0.8f),
                                    maxLines = 1
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Action Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.4f)),
                                        modifier = Modifier.clickable { previewPhoto = photo }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color(0xFFE5C07B), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("View", fontSize = 10.sp, color = Color(0xFFFFF0C2), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    IconButton(
                                        onClick = { photoToDelete = photo },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Photo Preview Dialog
    if (previewPhoto != null) {
        val photo = previewPhoto!!
        Dialog(onDismissRequest = { previewPhoto = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF042418)),
                border = BorderStroke(1.5.dp, Color(0xFFE5C07B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Photo Inspector",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFF0C2)
                        )
                        IconButton(onClick = { previewPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(photo.path),
                            contentDescription = photo.fileName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("File: ${photo.fileName}", fontSize = 11.5.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Size: ${ImageStorageHelper.formatFileSize(photo.sizeBytes)}", fontSize = 11.5.sp, color = Color(0xFFE5C07B))
                        Text("Path: ${photo.path}", fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { previewPhoto = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5C07B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Inspector", color = Color(0xFF042418), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 5. Delete Confirmation Dialog
    if (photoToDelete != null) {
        val photo = photoToDelete!!
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text("Delete Internal Photo?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${photo.fileName}' from internal app storage?") },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = ImageStorageHelper.deleteFile(File(photo.path))
                        if (deleted) {
                            refreshKey++
                            Toast.makeText(context, "Photo removed from storage!", Toast.LENGTH_SHORT).show()
                        }
                        photoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
