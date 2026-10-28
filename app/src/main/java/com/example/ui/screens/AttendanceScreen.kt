package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AttendanceSheetPreviewDialog
import com.example.ui.components.BiometricAttendanceDialog
import com.example.ui.components.FullCalendarAttendanceView
import com.example.ui.components.Madrassa3dBadge
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.Madrassa3dMetricTile
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.UserRoleManager
import com.example.util.t
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: MadrassaViewModel
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Students, 1: Asatiza (Teachers)
    val selectedDate by viewModel.selectedDate.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val studentAttendance by viewModel.studentAttendance.collectAsState()
    val teacherAttendance by viewModel.teacherAttendance.collectAsState()

    // Filters for Class, Section, Campus & Teacher-wise
    var selectedCampus by remember { mutableStateOf("All Campuses") }
    var selectedDept by remember { mutableStateOf("All Depts") }
    var selectedTeacherId by remember { mutableStateOf<Long?>(null) }

    var showFullCalendar by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showPrintDialog by remember { mutableStateOf(false) }
    var showAttendancePreviewDialog by remember { mutableStateOf(false) }
    var previewIsMonthly by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val isToday = selectedDate == viewModel.todayDate

    // Filter students
    val filteredStudents = remember(students, selectedCampus, selectedDept, selectedTeacherId, teachers, searchQuery) {
        students.filter { st ->
            val matchCampus = selectedCampus == "All Campuses" || st.campus.equals(selectedCampus, ignoreCase = true)
            val matchDept = selectedDept == "All Depts" || st.department.equals(selectedDept, ignoreCase = true)
            val matchTeacher = if (selectedTeacherId == null) true else {
                val t = teachers.find { it.id == selectedTeacherId }
                t != null && (t.assignedDepartment.equals(st.department, ignoreCase = true) || t.assignedSections.contains(st.section, ignoreCase = true))
            }
            val matchSearch = searchQuery.isBlank() ||
                st.name.contains(searchQuery, ignoreCase = true) ||
                st.rollNo.contains(searchQuery, ignoreCase = true) ||
                st.fatherName.contains(searchQuery, ignoreCase = true)
            matchCampus && matchDept && matchTeacher && matchSearch
        }
    }

    // Active attendance list based on current tab
    val currentAttendanceList = if (selectedTab == 0) {
        val filteredIds = filteredStudents.map { it.id }.toSet()
        studentAttendance.filter { filteredIds.contains(it.personId) }
    } else {
        teacherAttendance
    }

    val totalCount = if (selectedTab == 0) filteredStudents.size else teachers.size
    val presentCount = currentAttendanceList.count { it.status == "PRESENT" }
    val absentCount = currentAttendanceList.count { it.status == "ABSENT" }
    val lateCount = currentAttendanceList.count { it.status == "LATE" }
    val leaveCount = currentAttendanceList.count { it.status == "LEAVE" }

    // DatePicker setup
    val calendar = Calendar.getInstance()
    try {
        val parsed = sdf.parse(selectedDate)
        if (parsed != null) calendar.time = parsed
    } catch (_: Exception) {}

    val datePickerDialog = remember(selectedDate) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                viewModel.setAttendanceDate(sdf.format(newCal.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F7))
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Top Hero Banner styled like Home page
            item {
                Madrassa3dCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = 6.dp,
                    gradientColors = listOf(Color(0xFF0F5A3E), Color(0xFF083824), Color(0xFF031E13))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (selectedTab == 0) "طلبا کی حاضری رجسٹر" else "اساتذہ کرام کی حاضری",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE5C07B)
                                    )
                                )
                                Text(
                                    text = "Daily & Monthly Attendance Management",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            // Quick Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { showBiometricDialog = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0A4B2D))
                                ) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Biometric Attendance", tint = Color(0xFFE5C07B), modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = { showPrintDialog = true },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0A4B2D))
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = "Print Attendance Sheet", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date Picker & Navigator Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showFullCalendar = !showFullCalendar },
                                color = Color(0xFF0A4B2D)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    IconButton(
                                        onClick = { datePickerDialog.show() },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pick Date Dialog",
                                            tint = Color(0xFFE5C07B),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedDate,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (showFullCalendar) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val cal = Calendar.getInstance()
                                        try {
                                            val d = sdf.parse(selectedDate)
                                            if (d != null) cal.time = d
                                        } catch (_: Exception) {}
                                        cal.add(Calendar.DAY_OF_YEAR, -1)
                                        viewModel.setAttendanceDate(sdf.format(cal.time))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.White)
                                }

                                if (!isToday) {
                                    TextButton(
                                        onClick = { viewModel.setAttendanceDate(viewModel.todayDate) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Today", color = Color(0xFFE5C07B), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        val cal = Calendar.getInstance()
                                        try {
                                            val d = sdf.parse(selectedDate)
                                            if (d != null) cal.time = d
                                        } catch (_: Exception) {}
                                        cal.add(Calendar.DAY_OF_YEAR, 1)
                                        viewModel.setAttendanceDate(sdf.format(cal.time))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // 2. Full Calendar if open
            if (showFullCalendar) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                        FullCalendarAttendanceView(
                            selectedDate = selectedDate,
                            onDateSelected = { newDate ->
                                viewModel.setAttendanceDate(newDate)
                            }
                        )
                    }
                }
            }

            // 3. Students / Asatiza Tab Selector & Stats
            item {
                Madrassa3dCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = 5.dp,
                    gradientColors = listOf(Color(0xFF0F5A3E), Color(0xFF073322), Color(0xFF031A10))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0xFF042418),
                            indicator = {},
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "${t("students")} (${filteredStudents.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 0) Color(0xFFFFF0C2) else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "${t("asatiza")} (${teachers.size})",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 1) Color(0xFFFFF0C2) else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AttendancePill(label = t("present"), count = presentCount, color = StatusPresent)
                            AttendancePill(label = t("absent"), count = absentCount, color = StatusAbsent)
                            AttendancePill(label = t("late"), count = lateCount, color = StatusLate)
                            AttendancePill(label = t("leave"), count = leaveCount, color = StatusLeave)
                        }
                    }
                }
            }

            // 4. Filters & Search Box (only for students tab)
            if (selectedTab == 0) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Box to easily find and scroll to any student
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        placeholder = { Text("Search by student name, roll no or father... (تلاش کریں)", fontSize = 12.sp) },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val campuses = listOf("All Campuses", "Main Campus", "Campus 2", "Campus 3")
                        val depts = listOf("All Depts", "Hifz", "Nazra", "Qaida", "Qirat & Tajweed")

                        items(campuses) { camp ->
                            FilterChip(
                                selected = selectedCampus == camp,
                                onClick = { selectedCampus = camp },
                                label = { Text(camp, fontSize = 11.sp) }
                            )
                        }

                        items(depts) { d ->
                            FilterChip(
                                selected = selectedDept == d,
                                onClick = { selectedDept = d },
                                label = { Text(d, fontSize = 11.sp) }
                            )
                        }

                        if (teachers.isNotEmpty()) {
                            items(teachers) { teacher ->
                                FilterChip(
                                    selected = selectedTeacherId == teacher.id,
                                    onClick = {
                                        selectedTeacherId = if (selectedTeacherId == teacher.id) null else teacher.id
                                    },
                                    label = { Text("Ustaz: ${teacher.name}", fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Action Row: "Mark All Present" & Sheet Title
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTab == 0) "All Students (${filteredStudents.size} طلباء)" else "Asatiza Teachers (${teachers.size} اساتذہ)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                val targetIds = filteredStudents.map { it.id }
                                viewModel.markAllStudentsPresent(targetIds)
                                Toast.makeText(context, "Marked ${targetIds.size} students present!", Toast.LENGTH_SHORT).show()
                            } else {
                                val targetIds = teachers.map { it.id }
                                viewModel.markAllTeachersPresent(targetIds)
                                Toast.makeText(context, "Marked ${targetIds.size} asatiza present!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = t("mark_all_present"), fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 6. Attendance List Items
            if (selectedTab == 0) {
                if (filteredStudents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "کوئی طالب علم موجود نہیں (No students found)",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (searchQuery.isNotEmpty() || selectedCampus != "All Campuses" || selectedDept != "All Depts" || selectedTeacherId != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(onClick = {
                                        searchQuery = ""
                                        selectedCampus = "All Campuses"
                                        selectedDept = "All Depts"
                                        selectedTeacherId = null
                                    }) {
                                        Text("تمام طلباء دکھائیں (Show All Students)")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(filteredStudents, key = { it.id }) { student ->
                        val record = studentAttendance.find { it.personId == student.id }
                        val currentStatus = record?.status ?: "UNMARKED"

                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                            AttendanceItemRow(
                                name = student.name,
                                rollNo = student.rollNo,
                                classInfo = "${student.department} (${student.section})",
                                photoUri = student.photoUri,
                                subText = "ID: ${student.rollNo} • ${student.department} (${student.section})",
                                currentStatus = currentStatus,
                                onSelectStatus = { status: String ->
                                    viewModel.markAttendance(
                                        personId = student.id,
                                        personType = "STUDENT",
                                        status = status
                                    )
                                }
                            )
                        }
                    }
                }
            } else {
                if (teachers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "کوئی استاد موجود نہیں ہے",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(teachers, key = { it.id }) { teacher ->
                        val record = teacherAttendance.find { it.personId == teacher.id }
                        val currentStatus = record?.status ?: "UNMARKED"

                        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                            AttendanceItemRow(
                                name = teacher.name,
                                rollNo = teacher.role,
                                classInfo = "${teacher.assignedDepartment} (${teacher.assignedSections})",
                                photoUri = teacher.photoUri,
                                subText = "${teacher.role} • ${teacher.assignedDepartment}",
                                currentStatus = currentStatus,
                                onSelectStatus = { status: String ->
                                    viewModel.markAttendance(
                                        personId = teacher.id,
                                        personType = "TEACHER",
                                        status = status
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Quick Scroll To Top Button
        if (showScrollToTop) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 90.dp),
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
            }
        }
    }

    // Biometric Attendance Dialog
    if (showBiometricDialog) {
        BiometricAttendanceDialog(
            viewModel = viewModel,
            onDismiss = { showBiometricDialog = false }
        )
    }

    // Printable Attendance Dialog
    if (showPrintDialog) {
        PrintAttendanceOptionsDialog(
            selectedDate = selectedDate,
            onDismiss = { showPrintDialog = false },
            onPrint = { isMonthly ->
                previewIsMonthly = isMonthly
                showAttendancePreviewDialog = true
                showPrintDialog = false
            }
        )
    }

    if (showAttendancePreviewDialog) {
        AttendanceSheetPreviewDialog(
            selectedDate = selectedDate,
            isMonthly = previewIsMonthly,
            department = selectedDept,
            campus = selectedCampus,
            viewModel = viewModel,
            onDismiss = { showAttendancePreviewDialog = false }
        )
    }
}

@Composable
fun PrintAttendanceOptionsDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onPrint: (isMonthly: Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Print Attendance Register (PDF)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Select print format for attendance records:", fontSize = 13.sp)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPrint(false) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Today, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Daily Attendance Sheet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Print present/absent list for $selectedDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPrint(true) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = "Monthly Attendance Register", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Print full monthly attendance summary & percentage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AttendancePill(label: String, count: Int, color: Color) {
    val brightAccent = when (color) {
        StatusPresent -> Color(0xFF00E676)
        StatusAbsent -> Color(0xFFFF5252)
        StatusLate -> Color(0xFFFFB300)
        StatusLeave -> Color(0xFF40C4FF)
        else -> Color(0xFFFFF0C2)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF042418).copy(alpha = 0.9f),
        border = BorderStroke(1.2.dp, brightAccent.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(brightAccent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$label: ",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = brightAccent
            )
        }
    }
}

@Composable
fun AttendanceItemRow(
    name: String,
    rollNo: String = "",
    classInfo: String = "",
    photoUri: String? = null,
    subText: String = "",
    currentStatus: String,
    onSelectStatus: (String) -> Unit
) {
    Madrassa3dCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = 4.dp,
        gradientColors = listOf(
            Color(0xFF0E533A),
            Color(0xFF073020),
            Color(0xFF031A11)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student / Teacher Avatar inside 3D Gold-Rimmed Orb
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(Color(0xFF1E6C50), Color(0xFF062B1D))
                        )
                    )
                    .border(1.5.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!photoUri.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = com.example.util.ImageStorageHelper.toImageModel(photoUri),
                        contentDescription = name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = name.trim().take(1),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color(0xFFFFF0C2)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Student Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = Color.White
                    ),
                    maxLines = 2,
                    softWrap = true,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                val detailText = when {
                    rollNo.isNotBlank() && classInfo.isNotBlank() -> "Roll: $rollNo • $classInfo"
                    rollNo.isNotBlank() -> "Roll: $rollNo"
                    classInfo.isNotBlank() -> classInfo
                    else -> subText
                }
                if (detailText.isNotBlank()) {
                    Text(
                        text = detailText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE5C07B).copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Inline 3D Status Buttons: P (Present), A (Absent), L (Late), Lv (Leave)
            // Enhanced with luminous high-contrast labels and distinctive borders
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                StatusBadgeButton(
                    label = "P",
                    isSelected = currentStatus == "PRESENT",
                    color = StatusPresent,
                    onClick = { onSelectStatus("PRESENT") }
                )
                StatusBadgeButton(
                    label = "A",
                    isSelected = currentStatus == "ABSENT",
                    color = StatusAbsent,
                    onClick = { onSelectStatus("ABSENT") }
                )
                StatusBadgeButton(
                    label = "L",
                    isSelected = currentStatus == "LATE",
                    color = StatusLate,
                    onClick = { onSelectStatus("LATE") }
                )
                StatusBadgeButton(
                    label = "Lv",
                    isSelected = currentStatus == "LEAVE",
                    color = StatusLeave,
                    onClick = { onSelectStatus("LEAVE") }
                )
            }
        }
    }
}

@Composable
fun StatusBadgeButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "statusBtnScale"
    )

    val brightAccent = when (color) {
        StatusPresent -> Color(0xFF00E676)
        StatusAbsent -> Color(0xFFFF5252)
        StatusLate -> Color(0xFFFFB300)
        StatusLeave -> Color(0xFF40C4FF)
        else -> color
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(brightAccent, color)
                    )
                } else {
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFF0F3E2B), Color(0xFF072418))
                    )
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.2.dp,
                color = if (isSelected) Color(0xFFFFF0C2) else brightAccent.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) Color(0xFF042418) else Color.White
        )
    }
}
