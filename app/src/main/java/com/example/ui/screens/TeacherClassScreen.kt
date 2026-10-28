package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.ui.components.Madrassa3dBadge
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.Madrassa3dMetricTile
import com.example.ui.components.StatusChip
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import com.example.util.UserRoleManager
import com.example.util.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassScreen(
    teacher: Teacher,
    viewModel: MadrassaViewModel,
    onBack: () -> Unit,
    onStudentClick: (Student) -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val allStudents by viewModel.allStudents.collectAsState()
    val todayAttendance by viewModel.studentAttendance.collectAsState()

    var showAssignDialog by remember { mutableStateOf(false) }
    var currentTeacher by remember { mutableStateOf(teacher) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSectionFilter by remember { mutableStateOf("All Sections") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, PRESENT, ABSENT, UNMARKED

    // Filter students belonging to this teacher's department or assigned sections
    val assignedStudents = remember(allStudents, currentTeacher) {
        allStudents.filter { student ->
            student.department.equals(currentTeacher.assignedDepartment, ignoreCase = true) ||
            currentTeacher.assignedSections.contains(student.section, ignoreCase = true) ||
            currentTeacher.assignedSections.contains(student.department, ignoreCase = true)
        }
    }

    val availableSections = remember(assignedStudents) {
        listOf("All Sections") + assignedStudents.map { it.section }.distinct().filter { it.isNotBlank() }
    }

    val filteredClassStudents = remember(assignedStudents, searchQuery, selectedSectionFilter, selectedStatusFilter, todayAttendance) {
        assignedStudents.filter { st ->
            val matchSection = selectedSectionFilter == "All Sections" || st.section.equals(selectedSectionFilter, ignoreCase = true)
            val matchSearch = searchQuery.isBlank() ||
                st.name.contains(searchQuery, ignoreCase = true) ||
                st.rollNo.contains(searchQuery, ignoreCase = true) ||
                st.fatherName.contains(searchQuery, ignoreCase = true)
            val attRecord = todayAttendance.find { it.personId == st.id }
            val status = attRecord?.status ?: "UNMARKED"
            val matchStatus = when (selectedStatusFilter) {
                "ALL" -> true
                "PRESENT" -> status == "PRESENT"
                "ABSENT" -> status == "ABSENT"
                "UNMARKED" -> status == "UNMARKED"
                else -> true
            }
            matchSection && matchSearch && matchStatus
        }
    }

    val presentCount = assignedStudents.count { st ->
        todayAttendance.find { it.personId == st.id }?.status == "PRESENT"
    }
    val absentCount = assignedStudents.count { st ->
        todayAttendance.find { it.personId == st.id }?.status == "ABSENT"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = "${currentTeacher.name}'s Class", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${currentTeacher.assignedDepartment} • ${currentTeacher.assignedSections}",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (UserRoleManager.isAdmin) {
                        IconButton(onClick = { showAssignDialog = true }) {
                            Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = "Assign Class", tint = Color.White)
                        }
                    }
                    IconButton(onClick = {
                        viewModel.generateAttendanceSheetPdf(
                            context = context,
                            isMonthly = false,
                            department = currentTeacher.assignedDepartment,
                            section = currentTeacher.assignedSections
                        )
                    }) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Print Class List", tint = Color.White)
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Teacher Header Card
            item {
                Madrassa3dCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 6.dp,
                    gradientColors = listOf(Color(0xFF0F5A3E), Color(0xFF083824), Color(0xFF031E13))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Teacher Photo in 3D Gold-Rimmed Orb
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.radialGradient(
                                        listOf(Color(0xFF1E6C50), Color(0xFF062B1D))
                                    )
                                )
                                .border(1.8.dp, Color(0xFFE5C07B).copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentTeacher.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageStorageHelper.toImageModel(currentTeacher.photoUri),
                                    contentDescription = currentTeacher.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = currentTeacher.name.firstOrNull()?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFFF0C2),
                                        fontSize = 22.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentTeacher.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "${currentTeacher.role}  |  ${currentTeacher.qualification}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.5.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Madrassa3dBadge(
                                text = "Class: ${currentTeacher.assignedDepartment} (${currentTeacher.assignedSections})",
                                containerColor = Color(0xFFE5C07B).copy(alpha = 0.22f),
                                contentColor = Color(0xFFFFF0C2),
                                borderColor = Color(0xFFE5C07B).copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Phone: ${currentTeacher.phone}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE5C07B),
                                    fontSize = 11.sp
                                )
                            )
                        }

                        if (UserRoleManager.isAdmin) {
                            Button(
                                onClick = { showAssignDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE5C07B),
                                    contentColor = Color(0xFF042418)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Assign", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. 3D Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Madrassa3dMetricTile(
                        title = "Students",
                        value = "${assignedStudents.size}",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFFE5C07B)
                    )

                    Madrassa3dMetricTile(
                        title = "Present Today",
                        value = "$presentCount",
                        trendText = "Out of ${assignedStudents.size}",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFF81C784)
                    )

                    Madrassa3dMetricTile(
                        title = "Absent Today",
                        value = "$absentCount",
                        icon = Icons.Default.Cancel,
                        modifier = Modifier.weight(1f),
                        accentColor = Color(0xFFFF8A80)
                    )
                }
            }

            // 3. Search Bar & Section Filter Chips
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search student name, roll no, father name... (تلاش کریں)", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSections) { sec ->
                            FilterChip(
                                selected = selectedSectionFilter == sec,
                                onClick = { selectedSectionFilter = sec },
                                label = { Text(sec, fontSize = 11.sp) }
                            )
                        }

                        items(listOf("ALL", "PRESENT", "ABSENT", "UNMARKED")) { statusOption ->
                            FilterChip(
                                selected = selectedStatusFilter == statusOption,
                                onClick = { selectedStatusFilter = statusOption },
                                label = {
                                    Text(
                                        text = when (statusOption) {
                                            "ALL" -> "All Status"
                                            "PRESENT" -> "حاضر (Present)"
                                            "ABSENT" -> "غیر حاضر (Absent)"
                                            else -> "غیر نشان شدہ (Unmarked)"
                                        },
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // 4. Action Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Students in this Class (${filteredClassStudents.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    TextButton(
                        onClick = {
                            viewModel.markAllStudentsPresent()
                            Toast.makeText(context, "Marked present for all students!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Mark All Present", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 5. Scrollable Students List
            if (filteredClassStudents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No students match the current search or filters.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            if (searchQuery.isNotEmpty() || selectedSectionFilter != "All Sections" || selectedStatusFilter != "ALL") {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    searchQuery = ""
                                    selectedSectionFilter = "All Sections"
                                    selectedStatusFilter = "ALL"
                                }) {
                                    Text("Reset Filters (تمام طلباء دکھائیں)")
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredClassStudents, key = { it.id }) { student ->
                    val record = todayAttendance.find { it.personId == student.id }
                    val currentStatus = record?.status ?: "UNMARKED"

                    Madrassa3dCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onStudentClick(student) },
                        elevation = 5.dp,
                        shape = RoundedCornerShape(16.dp),
                        gradientColors = listOf(
                            Color(0xFF0F5338),
                            Color(0xFF083221),
                            Color(0xFF031A10)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Photo inside 3D Gold-Rimmed Orb
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(
                                            androidx.compose.ui.graphics.Brush.radialGradient(
                                                listOf(Color(0xFF1E6C50), Color(0xFF062B1D))
                                            )
                                        )
                                        .border(1.5.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (student.photoUri.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageStorageHelper.toImageModel(student.photoUri),
                                            contentDescription = student.name,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = student.name.firstOrNull()?.uppercase() ?: "S",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFFF0C2),
                                            fontSize = 19.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            ),
                                            softWrap = true,
                                            maxLines = 2,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )

                                        Madrassa3dBadge(
                                            text = "Roll: ${student.rollNo}",
                                            containerColor = Color(0xFFE5C07B).copy(alpha = 0.22f),
                                            contentColor = Color(0xFFFFF0C2),
                                            borderColor = Color(0xFFE5C07B).copy(alpha = 0.6f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "ولدیت: ${student.fatherName} • نصاب: ${student.paray}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.5.sp
                                        )
                                    )
                                    Text(
                                        text = "${student.department} (${student.section}) • ${student.campus}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFE5C07B),
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Attendance Quick-Action Buttons (P / A / L) + View Profile
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Present Button
                                    Button(
                                        onClick = {
                                            viewModel.markAttendance(student.id, "STUDENT", "PRESENT")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentStatus == "PRESENT") Color(0xFF81C784) else Color.White.copy(alpha = 0.12f),
                                            contentColor = if (currentStatus == "PRESENT") Color(0xFF042418) else Color(0xFF81C784)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("✓ حاضر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Absent Button
                                    Button(
                                        onClick = {
                                            viewModel.markAttendance(student.id, "STUDENT", "ABSENT")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentStatus == "ABSENT") Color(0xFFFF8A80) else Color.White.copy(alpha = 0.12f),
                                            contentColor = if (currentStatus == "ABSENT") Color(0xFF042418) else Color(0xFFFF8A80)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("✗ غیر حاضر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Late Button
                                    Button(
                                        onClick = {
                                            viewModel.markAttendance(student.id, "STUDENT", "LATE")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (currentStatus == "LATE") Color(0xFFFFB74D) else Color.White.copy(alpha = 0.12f),
                                            contentColor = if (currentStatus == "LATE") Color(0xFF042418) else Color(0xFFFFB74D)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("⏰ تاخیر", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                TextButton(
                                    onClick = { onStudentClick(student) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("پروفائل", fontSize = 11.5.sp, color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Assign Class Dialog
    if (showAssignDialog) {
        AssignClassDialog(
            teacher = currentTeacher,
            onDismiss = { showAssignDialog = false },
            onSave = { updatedTeacher ->
                viewModel.saveTeacher(updatedTeacher, isEdit = true)
                currentTeacher = updatedTeacher
                showAssignDialog = false
                Toast.makeText(context, "Class assignment updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AssignClassDialog(
    teacher: Teacher,
    onDismiss: () -> Unit,
    onSave: (Teacher) -> Unit
) {
    var selectedDept by remember { mutableStateOf(teacher.assignedDepartment) }
    var selectedSections by remember { mutableStateOf(teacher.assignedSections) }

    val departments = listOf("Hifz", "Nazra", "Qaida", "Qirat & Tajweed")
    val sectionsList = listOf("Section A", "Section B", "Class 1A", "Class 1B", "Class 2A", "Class 2B", "Morning Hifz", "Evening Nazra")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Assign Class to ${teacher.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Select Class / درجہ:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    departments.take(2).forEach { dept ->
                        FilterChip(
                            selected = selectedDept == dept,
                            onClick = { selectedDept = dept },
                            label = { Text(dept, fontSize = 12.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    departments.drop(2).forEach { dept ->
                        FilterChip(
                            selected = selectedDept == dept,
                            onClick = { selectedDept = dept },
                            label = { Text(dept, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Assigned Section / Class Name:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                OutlinedTextField(
                    value = selectedSections,
                    onValueChange = { selectedSections = it },
                    label = { Text("e.g. Hifz Class 1A, Section A") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Quick Presets:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sectionsList.take(3).forEach { sec ->
                        SuggestionChip(
                            onClick = { selectedSections = sec },
                            label = { Text(sec, fontSize = 10.5.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = teacher.copy(
                        assignedDepartment = selectedDept,
                        assignedSections = selectedSections
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(text = "Save Assignment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
