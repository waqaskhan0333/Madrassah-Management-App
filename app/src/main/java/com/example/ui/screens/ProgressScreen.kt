package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.data.model.*
import com.example.ui.components.Madrassa3dBadge
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.Madrassa3dMetricTile
import com.example.ui.components.StatusChip
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import com.example.util.t

@Composable
fun ProgressScreen(
    viewModel: MadrassaViewModel
) {
    val context = LocalContext.current
    var activeModeTab by remember { mutableIntStateOf(0) } // 0: All Students Roster, 1: Recent Logs History
    var searchQuery by remember { mutableStateOf("") }
    var selectedDeptFilter by remember { mutableStateOf("All Classes") }
    var selectedCampusFilter by remember { mutableStateOf("All Campuses") }
    var preselectedStudentId by remember { mutableStateOf<Long?>(null) }

    var selectedDeptTab by remember { mutableIntStateOf(0) } // 0: Hifz, 1: Nazra, 2: Qaida, 3: Tajweed
    var showAddDialog by remember { mutableStateOf(false) }

    var hifzToEdit by remember { mutableStateOf<HifzProgress?>(null) }
    var nazraToEdit by remember { mutableStateOf<NazraProgress?>(null) }
    var qaidaToEdit by remember { mutableStateOf<QaidaProgress?>(null) }
    var tajweedToEdit by remember { mutableStateOf<TajweedProgress?>(null) }

    var deleteTarget by remember { mutableStateOf<Pair<String, Long>?>(null) } // Pair(Type, Id)

    val students by viewModel.allStudents.collectAsState()
    val recentHifz by viewModel.recentHifz.collectAsState()
    val recentNazra by viewModel.recentNazra.collectAsState()
    val recentQaida by viewModel.recentQaida.collectAsState()
    val recentTajweed by viewModel.recentTajweed.collectAsState()

    val filteredStudents = remember(students, searchQuery, selectedDeptFilter, selectedCampusFilter) {
        students.filter { st ->
            val matchCampus = selectedCampusFilter == "All Campuses" || st.campus.equals(selectedCampusFilter, ignoreCase = true)
            val matchDept = when (selectedDeptFilter) {
                "All Classes", "All Depts" -> true
                "Hifz" -> st.department.contains("Hifz", true)
                "Nazra" -> st.department.contains("Nazra", true)
                "Qaida" -> st.department.contains("Qaida", true)
                "Tajweed" -> st.department.contains("Tajweed", true) || st.department.contains("Qirat", true)
                else -> st.department.equals(selectedDeptFilter, true)
            }
            val matchSearch = searchQuery.isBlank() ||
                st.name.contains(searchQuery, ignoreCase = true) ||
                st.rollNo.contains(searchQuery, ignoreCase = true) ||
                st.fatherName.contains(searchQuery, ignoreCase = true) ||
                st.department.contains(searchQuery, ignoreCase = true)
            matchCampus && matchDept && matchSearch
        }
    }

    val deptTabs = listOf(t("hifz"), t("nazra"), t("qaida"), t("tajweed"))

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    hifzToEdit = null
                    nazraToEdit = null
                    qaidaToEdit = null
                    tajweedToEdit = null
                    showAddDialog = true
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Log Progress")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Primary Navigation: All Students Roster vs Recent Logs
            TabRow(
                selectedTabIndex = activeModeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = activeModeTab == 0,
                    onClick = { activeModeTab = 0 },
                    text = {
                        Text(
                            text = "تمام طلباء کا ریکارڈ (${filteredStudents.size})",
                            fontWeight = if (activeModeTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = activeModeTab == 1,
                    onClick = { activeModeTab = 1 },
                    text = {
                        Text(
                            text = "حالیہ اندراجات (Recent Logs)",
                            fontWeight = if (activeModeTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                placeholder = { Text("Search by student name, roll no, father name... (تلاش کریں)", fontSize = 12.sp) },
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

            // Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val depts = listOf("All Classes", "Hifz", "Nazra", "Qaida", "Tajweed")
                val campuses = listOf("All Campuses", "Main Campus", "Campus 2", "Campus 3")

                items(depts) { d ->
                    FilterChip(
                        selected = selectedDeptFilter == d,
                        onClick = { selectedDeptFilter = d },
                        label = { Text(d, fontSize = 11.sp) }
                    )
                }

                items(campuses) { camp ->
                    FilterChip(
                        selected = selectedCampusFilter == camp,
                        onClick = { selectedCampusFilter = camp },
                        label = { Text(camp, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeModeTab == 0) {
                // Mode 0: Universal Scrollable All Students Roster
                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "کوئی طالب علم موجود نہیں (No students found)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (searchQuery.isNotEmpty() || (selectedDeptFilter != "All Classes" && selectedDeptFilter != "All Depts") || selectedCampusFilter != "All Campuses") {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = {
                                    searchQuery = ""
                                    selectedDeptFilter = "All Classes"
                                    selectedCampusFilter = "All Campuses"
                                }) {
                                    Text("تمام طلباء دکھائیں (Show All Students)")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredStudents, key = { it.id }) { student ->
                            val latestHifz = recentHifz.firstOrNull { it.studentId == student.id }
                            val latestNazra = recentNazra.firstOrNull { it.studentId == student.id }
                            val latestQaida = recentQaida.firstOrNull { it.studentId == student.id }
                            val latestTajweed = recentTajweed.firstOrNull { it.studentId == student.id }

                            Madrassa3dCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = 5.dp,
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
                                        // Student Photo inside 3D Gold-Rimmed Orb
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
                                                    text = student.name.firstOrNull()?.uppercase() ?: "ط",
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
                                                text = "ولدیت: ${student.fatherName} • Class: ${student.department} (${student.section})",
                                                fontSize = 11.5.sp,
                                                color = Color.White.copy(alpha = 0.78f)
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Progress summary badge
                                            val progressSummary = when {
                                                student.department.contains("Hifz", true) -> {
                                                    if (latestHifz != null) "حفظ: پارہ ${latestHifz.sabaqPara}، سبق: ${latestHifz.sabaqSurah} • درجہ: ${latestHifz.grade}"
                                                    else "حفظ نصاب: ${student.paray} (کوئی حالیہ جائزہ نہیں)"
                                                }
                                                student.department.contains("Nazra", true) -> {
                                                    if (latestNazra != null) "ناظرہ: پارہ ${latestNazra.currentPara} (${latestNazra.pageRange}) • درجہ: ${latestNazra.grade}"
                                                    else "ناظرہ نصاب: ${student.paray}"
                                                }
                                                student.department.contains("Qaida", true) -> {
                                                    if (latestQaida != null) "قاعدہ: سبق نمبر ${latestQaida.lessonNo} (${latestQaida.topic}) • درجہ: ${latestQaida.grade}"
                                                    else "نورانی قاعدہ"
                                                }
                                                else -> {
                                                    if (latestTajweed != null) "تجوید: ${latestTajweed.topic}"
                                                    else "تجوید و قرأت"
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White.copy(alpha = 0.1f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = progressSummary,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFFFF0C2),
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Quick Action Button: + Log Progress
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val targetTab = when {
                                            student.department.contains("Hifz", true) -> 0
                                            student.department.contains("Nazra", true) -> 1
                                            student.department.contains("Qaida", true) -> 2
                                            else -> 3
                                        }

                                        Button(
                                            onClick = {
                                                preselectedStudentId = student.id
                                                selectedDeptTab = targetTab
                                                hifzToEdit = null
                                                nazraToEdit = null
                                                qaidaToEdit = null
                                                tajweedToEdit = null
                                                showAddDialog = true
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFE5C07B),
                                                contentColor = Color(0xFF042418)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "+ Log Progress (+ جائزہ درج کریں)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Mode 1: Recent Activity Logs by Department
                TabRow(
                    selectedTabIndex = selectedDeptTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = EmeraldPrimary
                ) {
                    deptTabs.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedDeptTab == index,
                            onClick = { selectedDeptTab = index },
                            text = {
                                Text(
                                    text = label,
                                    fontWeight = if (selectedDeptTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

            when (selectedDeptTab) {
                0 -> {
                    // Hifz List
                    if (recentHifz.isEmpty()) {
                        EmptyProgressBox()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recentHifz) { hifz ->
                                val student = students.find { it.id == hifz.studentId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = student?.name ?: "Student #${hifz.studentId}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StatusChip(status = hifz.grade)
                                                IconButton(
                                                    onClick = {
                                                        hifzToEdit = hifz
                                                        showAddDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = { deleteTarget = Pair("HIFZ", hifz.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Sabaq: Para ${hifz.sabaqPara}, Surah ${hifz.sabaqSurah} (${hifz.sabaqAyahRange})",
                                            fontWeight = FontWeight.SemiBold,
                                            color = EmeraldPrimary
                                        )
                                        Text(text = "Sabqi (Revision): ${hifz.sabqiRange}", fontSize = 12.sp)
                                        Text(text = "Manzil: ${hifz.manzilParas}", fontSize = 12.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Mistakes: ${hifz.mistakesCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(text = "Total Paras: ${hifz.totalParasCompleted}/30", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Nazra List
                    if (recentNazra.isEmpty()) {
                        EmptyProgressBox()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recentNazra) { nazra ->
                                val student = students.find { it.id == nazra.studentId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = student?.name ?: "Student #${nazra.studentId}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StatusChip(status = nazra.grade)
                                                IconButton(
                                                    onClick = {
                                                        nazraToEdit = nazra
                                                        showAddDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = { deleteTarget = Pair("NAZRA", nazra.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Current Para: ${nazra.currentPara}  |  Range: ${nazra.pageRange}", fontWeight = FontWeight.SemiBold)
                                        Text(text = "Mistakes: ${nazra.mistakesCount}", fontSize = 12.sp)
                                        if (nazra.remarks.isNotBlank()) {
                                            Text(text = "Remarks: ${nazra.remarks}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Qaida List
                    if (recentQaida.isEmpty()) {
                        EmptyProgressBox()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recentQaida) { qaida ->
                                val student = students.find { it.id == qaida.studentId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = student?.name ?: "Student #${qaida.studentId}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StatusChip(status = qaida.passOrRepeat)
                                                IconButton(
                                                    onClick = {
                                                        qaidaToEdit = qaida
                                                        showAddDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = { deleteTarget = Pair("QAIDA", qaida.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Lesson #${qaida.lessonNo}: ${qaida.topic}", fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
                                        Text(text = "Grade: ${qaida.grade}", fontSize = 12.sp)
                                        if (qaida.remarks.isNotBlank()) {
                                            Text(text = "Remarks: ${qaida.remarks}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Tajweed List
                    if (recentTajweed.isEmpty()) {
                        EmptyProgressBox()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(recentTajweed) { tajweed ->
                                val student = students.find { it.id == tajweed.studentId }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = student?.name ?: "Student #${tajweed.studentId}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StatusChip(status = tajweed.grade)
                                                IconButton(
                                                    onClick = {
                                                        tajweedToEdit = tajweed
                                                        showAddDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = { deleteTarget = Pair("TAJWEED", tajweed.id) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Rule: ${tajweed.topic}", fontWeight = FontWeight.SemiBold, color = EmeraldPrimary)
                                        Text(text = "Practical: ${tajweed.practicalAssessment}", fontSize = 12.sp)
                                        Text(text = "Style: ${tajweed.style}", fontSize = 12.sp)
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

    if (showAddDialog) {
        AddOrEditProgressDialog(
            students = students,
            selectedTab = selectedDeptTab,
            preselectedStudentId = preselectedStudentId,
            hifzInitial = hifzToEdit,
            nazraInitial = nazraToEdit,
            qaidaInitial = qaidaToEdit,
            tajweedInitial = tajweedToEdit,
            onDismiss = {
                showAddDialog = false
                preselectedStudentId = null
                hifzToEdit = null
                nazraToEdit = null
                qaidaToEdit = null
                tajweedToEdit = null
            },
            onSaveHifz = {
                if (hifzToEdit != null) viewModel.updateHifzProgress(it) else viewModel.addHifzProgress(it)
                showAddDialog = false
                preselectedStudentId = null
            },
            onSaveNazra = {
                if (nazraToEdit != null) viewModel.updateNazraProgress(it) else viewModel.addNazraProgress(it)
                showAddDialog = false
                preselectedStudentId = null
            },
            onSaveQaida = {
                if (qaidaToEdit != null) viewModel.updateQaidaProgress(it) else viewModel.addQaidaProgress(it)
                showAddDialog = false
                preselectedStudentId = null
            },
            onSaveTajweed = {
                if (tajweedToEdit != null) viewModel.updateTajweedProgress(it) else viewModel.addTajweedProgress(it)
                showAddDialog = false
                preselectedStudentId = null
            }
        )
    }

    deleteTarget?.let { (type, id) ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Progress Entry?") },
            text = { Text("Are you sure you want to delete this progress record?") },
            confirmButton = {
                Button(
                    onClick = {
                        when (type) {
                            "HIFZ" -> viewModel.deleteHifzProgress(id)
                            "NAZRA" -> viewModel.deleteNazraProgress(id)
                            "QAIDA" -> viewModel.deleteQaidaProgress(id)
                            "TAJWEED" -> viewModel.deleteTajweedProgress(id)
                        }
                        deleteTarget = null
                        Toast.makeText(context, "Progress record deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun EmptyProgressBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "No progress logged yet. Tap + to record.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditProgressDialog(
    students: List<Student>,
    selectedTab: Int,
    preselectedStudentId: Long? = null,
    hifzInitial: HifzProgress? = null,
    nazraInitial: NazraProgress? = null,
    qaidaInitial: QaidaProgress? = null,
    tajweedInitial: TajweedProgress? = null,
    onDismiss: () -> Unit,
    onSaveHifz: (HifzProgress) -> Unit,
    onSaveNazra: (NazraProgress) -> Unit,
    onSaveQaida: (QaidaProgress) -> Unit,
    onSaveTajweed: (TajweedProgress) -> Unit
) {
    val isEdit = hifzInitial != null || nazraInitial != null || qaidaInitial != null || tajweedInitial != null

    val initialStudentId = preselectedStudentId ?: hifzInitial?.studentId ?: nazraInitial?.studentId ?: qaidaInitial?.studentId ?: tajweedInitial?.studentId
    val studentFoundIndex = students.indexOfFirst { it.id == initialStudentId }.coerceAtLeast(0)

    var selectedStudentIndex by remember { mutableIntStateOf(studentFoundIndex) }
    var studentExpanded by remember { mutableStateOf(false) }

    var sabaqPara by remember {
        mutableStateOf(
            hifzInitial?.sabaqPara?.toString()
                ?: nazraInitial?.currentPara?.toString()
                ?: qaidaInitial?.lessonNo?.toString()
                ?: "1"
        )
    }
    var sabaqSurah by remember {
        mutableStateOf(
            hifzInitial?.sabaqSurah
                ?: qaidaInitial?.topic
                ?: tajweedInitial?.topic
                ?: "Al-Baqarah"
        )
    }
    var sabaqAyahRange by remember {
        mutableStateOf(
            hifzInitial?.sabaqAyahRange
                ?: nazraInitial?.pageRange
                ?: tajweedInitial?.practicalAssessment
                ?: "Ayah 1-10"
        )
    }
    var sabqiRange by remember { mutableStateOf(hifzInitial?.sabqiRange ?: "Quarter Para") }
    var manzilParas by remember { mutableStateOf(hifzInitial?.manzilParas ?: "Para 30") }
    var grade by remember {
        mutableStateOf(
            hifzInitial?.grade ?: nazraInitial?.grade ?: qaidaInitial?.grade ?: tajweedInitial?.grade ?: "Mumtaz"
        )
    }
    var mistakes by remember {
        mutableStateOf(
            (hifzInitial?.mistakesCount ?: nazraInitial?.mistakesCount ?: 0).toString()
        )
    }
    var remarks by remember {
        mutableStateOf(
            hifzInitial?.remarks ?: nazraInitial?.remarks ?: qaidaInitial?.remarks ?: tajweedInitial?.remarks ?: "Good recitation"
        )
    }

    if (students.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Students Available") },
            text = { Text("Please add students before logging daily progress.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Daily Progress Entry" else "Record Daily Lesson (Sabaq)",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )

                // Student Picker
                ExposedDropdownMenuBox(
                    expanded = studentExpanded,
                    onExpandedChange = { studentExpanded = !studentExpanded }
                ) {
                    OutlinedTextField(
                        value = "${students[selectedStudentIndex].name} (${students[selectedStudentIndex].rollNo})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Student") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = studentExpanded,
                        onDismissRequest = { studentExpanded = false }
                    ) {
                        students.forEachIndexed { idx, st ->
                            DropdownMenuItem(
                                text = { Text("${st.name} (${st.rollNo} - ${st.department})") },
                                onClick = {
                                    selectedStudentIndex = idx
                                    studentExpanded = false
                                }
                            )
                        }
                    }
                }

                // Fields based on Department
                when (selectedTab) {
                    0 -> { // Hifz
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sabaqPara,
                                onValueChange = { sabaqPara = it },
                                label = { Text("Para No") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = sabaqSurah,
                                onValueChange = { sabaqSurah = it },
                                label = { Text("Surah") },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        OutlinedTextField(
                            value = sabaqAyahRange,
                            onValueChange = { sabaqAyahRange = it },
                            label = { Text("Sabaq Ayah Range") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sabqiRange,
                            onValueChange = { sabqiRange = it },
                            label = { Text("Sabqi (Recent Revision)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = manzilParas,
                            onValueChange = { manzilParas = it },
                            label = { Text("Manzil (Old Revision)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    1 -> { // Nazra
                        OutlinedTextField(
                            value = sabaqPara,
                            onValueChange = { sabaqPara = it },
                            label = { Text("Current Para") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sabaqAyahRange,
                            onValueChange = { sabaqAyahRange = it },
                            label = { Text("Pages / Ruku Range") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    2 -> { // Qaida
                        OutlinedTextField(
                            value = sabaqPara,
                            onValueChange = { sabaqPara = it },
                            label = { Text("Lesson Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sabaqSurah,
                            onValueChange = { sabaqSurah = it },
                            label = { Text("Topic (e.g. Harkat, Tanween)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    3 -> { // Tajweed
                        OutlinedTextField(
                            value = sabaqSurah,
                            onValueChange = { sabaqSurah = it },
                            label = { Text("Tajweed Rule / Topic") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = sabaqAyahRange,
                            onValueChange = { sabaqAyahRange = it },
                            label = { Text("Practical Assessment") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Grade & Mistakes
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = mistakes,
                        onValueChange = { mistakes = it },
                        label = { Text("Mistakes") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("Grade (Mumtaz/Jayyid)") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val st = students[selectedStudentIndex]
                            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                            when (selectedTab) {
                                0 -> onSaveHifz(
                                    HifzProgress(
                                        id = hifzInitial?.id ?: 0,
                                        studentId = st.id,
                                        date = hifzInitial?.date ?: today,
                                        sabaqPara = sabaqPara.toIntOrNull() ?: 1,
                                        sabaqSurah = sabaqSurah,
                                        sabaqAyahRange = sabaqAyahRange,
                                        sabqiRange = sabqiRange,
                                        manzilParas = manzilParas,
                                        grade = grade,
                                        mistakesCount = mistakes.toIntOrNull() ?: 0,
                                        totalParasCompleted = hifzInitial?.totalParasCompleted ?: 2,
                                        remarks = remarks
                                    )
                                )
                                1 -> onSaveNazra(
                                    NazraProgress(
                                        id = nazraInitial?.id ?: 0,
                                        studentId = st.id,
                                        date = nazraInitial?.date ?: today,
                                        currentPara = sabaqPara.toIntOrNull() ?: 1,
                                        pageRange = sabaqAyahRange,
                                        mistakesCount = mistakes.toIntOrNull() ?: 0,
                                        grade = grade,
                                        remarks = remarks
                                    )
                                )
                                2 -> onSaveQaida(
                                    QaidaProgress(
                                        id = qaidaInitial?.id ?: 0,
                                        studentId = st.id,
                                        date = qaidaInitial?.date ?: today,
                                        lessonNo = sabaqPara.toIntOrNull() ?: 1,
                                        topic = sabaqSurah,
                                        grade = grade,
                                        passOrRepeat = if (grade.equals("Daeef", ignoreCase = true)) "REPEAT" else "PASS",
                                        remarks = remarks
                                    )
                                )
                                3 -> onSaveTajweed(
                                    TajweedProgress(
                                        id = tajweedInitial?.id ?: 0,
                                        studentId = st.id,
                                        date = tajweedInitial?.date ?: today,
                                        topic = sabaqSurah,
                                        practicalAssessment = sabaqAyahRange,
                                        style = "Hafs 'an 'Asim",
                                        grade = grade,
                                        remarks = remarks
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(if (isEdit) "Update Entry" else "Save Entry")
                    }
                }
            }
        }
    }
}
