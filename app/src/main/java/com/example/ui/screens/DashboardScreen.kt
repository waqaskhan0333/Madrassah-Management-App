package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Student
import com.example.ui.components.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.HijriDateUtil
import com.example.util.ImageStorageHelper
import com.example.util.LanguageState
import com.example.util.t

@Composable
fun DashboardScreen(
    viewModel: MadrassaViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToCourses: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onAddStudentClick: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val todayAttendance by viewModel.studentAttendance.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    var showSyncDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("All Depts") }
    var selectedCampus by remember { mutableStateOf("All Campuses") }

    val filteredStudents = remember(students, searchQuery, selectedDept, selectedCampus) {
        students.filter { st ->
            val matchCampus = selectedCampus == "All Campuses" || st.campus.equals(selectedCampus, ignoreCase = true)
            val matchDept = when (selectedDept) {
                "All Depts" -> true
                "Hifz" -> st.department.contains("Hifz", true)
                "Nazra" -> st.department.contains("Nazra", true)
                "Qaida" -> st.department.contains("Qaida", true)
                "Tajweed" -> st.department.contains("Tajweed", true) || st.department.contains("Qirat", true)
                else -> st.department.equals(selectedDept, true)
            }
            val matchSearch = searchQuery.isBlank() ||
                st.name.contains(searchQuery, ignoreCase = true) ||
                st.rollNo.contains(searchQuery, ignoreCase = true) ||
                st.fatherName.contains(searchQuery, ignoreCase = true) ||
                st.department.contains(searchQuery, ignoreCase = true)
            matchCampus && matchDept && matchSearch
        }
    }

    val totalStudents = students.size
    val teacherCount = teachers.size
    val classCount = courses.size

    val presentCount = todayAttendance.count { it.status == "PRESENT" && it.personType == "STUDENT" }
    val absentCount = todayAttendance.count { it.status == "ABSENT" && it.personType == "STUDENT" }
    val lateCount = todayAttendance.count { it.status == "LATE" && it.personType == "STUDENT" }
    val totalMarked = presentCount + absentCount + lateCount
    val attendanceRate = if (totalStudents > 0 && totalMarked > 0) {
        ((presentCount + lateCount) * 100) / totalStudents
    } else 0

    val hifzStudents = students.count { it.department.contains("Hifz", ignoreCase = true) }
    val nazraStudents = students.count { it.department.contains("Nazra", ignoreCase = true) }
    val qaidaStudents = students.count { it.department.contains("Qaida", ignoreCase = true) }
    val hifzPct = if (totalStudents > 0) (hifzStudents * 100) / totalStudents else 0
    val nazraPct = if (totalStudents > 0) (nazraStudents * 100) / totalStudents else 0
    val qaidaPct = if (totalStudents > 0) (qaidaStudents * 100) / totalStudents else 0

    val todayFormatted = HijriDateUtil.getTodayFullFormatted(LanguageState.currentLanguage)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Greeting Card with Arched Mihrab & Quran on Rehal Stand
        item {
            MadrassaHeroGreetingCard(
                adminName = if (profile.principalName.isNotBlank()) profile.principalName else "Admin"
            )
        }

        // 2. 4 Key Stat Cards (Total Students, Teachers, Classes, Attendance)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = t("total_students"),
                        value = String.format("%,d", totalStudents),
                        trendText = t("enrolled_active"),
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudents
                    )

                    DashboardMetricCard(
                        title = t("asatiza"),
                        value = "$teacherCount",
                        trendText = t("faculty_members"),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTeachers
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardMetricCard(
                        title = t("classes"),
                        value = "$classCount",
                        trendText = t("active_courses"),
                        icon = Icons.Default.Class,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCourses
                    )

                    DashboardMetricCard(
                        title = t("attendance"),
                        value = "$attendanceRate%",
                        trendText = t("marked_today"),
                        icon = Icons.Default.TrackChanges,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAttendance
                    )
                }
            }
        }

        // 3. Today's Attendance Dark Emerald Card with Real Database Data
        item {
            TodayAttendanceDarkCard(
                dateString = todayFormatted,
                attendanceRate = attendanceRate,
                presentCount = presentCount,
                absentCount = absentCount,
                lateCount = lateCount,
                onClick = onNavigateToAttendance
            )
        }

        // 4. Quick Actions
        item {
            val syncBadge = when (syncState.status) {
                com.example.data.sync.SyncStatus.SYNCING -> "..."
                com.example.data.sync.SyncStatus.SUCCESS -> "OK"
                com.example.data.sync.SyncStatus.ERROR -> "!"
                else -> if (syncState.isOnline) "ON" else null
            }
            QuickActionsSection(
                onNavigateToStudents = onNavigateToStudents,
                onNavigateToAttendance = onNavigateToAttendance,
                onNavigateToProgress = onNavigateToProgress,
                onNavigateToFees = onNavigateToFees,
                onNavigateToExams = onNavigateToExams,
                onNavigateToTeachers = onNavigateToTeachers,
                onNavigateToReports = { viewModel.generatePdfReport(context) },
                onNavigateToCourses = onNavigateToCourses,
                onCloudBackupClick = { showSyncDialog = true },
                cloudSyncBadge = syncBadge
            )
        }

        // 5. Student Progress Section (Real-time counts from Room Database)
        item {
            StudentProgressGaugesSection(
                hifzPercentage = hifzPct,
                hifzCount = "$hifzStudents / $totalStudents",
                naziraPercentage = nazraPct,
                naziraCount = "$nazraStudents / $totalStudents",
                qaidaPercentage = qaidaPct,
                qaidaCount = "$qaidaStudents / $totalStudents",
                onViewAllClick = onNavigateToProgress
            )
        }

        // 6. Two-Column Lower Section: Real Classes & Real Recent Activity
        item {
            ClassesAndActivitySection(
                courses = courses,
                recentStudents = students.takeLast(6).reversed(),
                onViewClassesClick = onNavigateToCourses,
                onViewActivityClick = onNavigateToStudents
            )
        }

        // 7. Universal Enrolled Students Directory (Full Scrollable Student Name Roster)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تمام طلباء کی ڈائریکٹری (All Enrolled Students)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )
                        Text(
                            text = "کل رجسٹرڈ طلباء: ${filteredStudents.size} • حاضری اور جائزہ شارٹ کٹ",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(onClick = onNavigateToStudents) {
                        Text("مزید تفصیلات", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar for Students
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name, roll no, father name... (تلاش کریں)", fontSize = 12.sp) },
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val depts = listOf("All Depts", "Hifz", "Nazra", "Qaida", "Tajweed")
                    val campuses = listOf("All Campuses", "Main Campus", "Campus 2", "Campus 3")

                    items(depts) { d ->
                        FilterChip(
                            selected = selectedDept == d,
                            onClick = { selectedDept = d },
                            label = { Text(d, fontSize = 11.sp) }
                        )
                    }

                    items(campuses) { camp ->
                        FilterChip(
                            selected = selectedCampus == camp,
                            onClick = { selectedCampus = camp },
                            label = { Text(camp, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Student Cards in the Scrollable Roster
        if (filteredStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                        if (searchQuery.isNotEmpty() || selectedDept != "All Depts" || selectedCampus != "All Campuses") {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedDept = "All Depts"
                                selectedCampus = "All Campuses"
                            }) {
                                Text("تمام طلباء دکھائیں (Show All Students)")
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredStudents, key = { it.id }) { student ->
                val attRecord = todayAttendance.find { it.personId == student.id }
                val attStatus = attRecord?.status ?: "UNMARKED"

                Madrassa3dCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = onNavigateToStudents,
                    shape = RoundedCornerShape(16.dp),
                    elevation = 5.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3D Embossed Avatar Orb with Gold Ring
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFF1B6B4F), Color(0xFF042418))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
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
                                        text = student.name.firstOrNull()?.uppercase() ?: "ط",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFF0C2),
                                        fontSize = 18.sp
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
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                                        softWrap = true,
                                        maxLines = 2,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    Madrassa3dBadge(
                                        text = "Roll #${student.rollNo}",
                                        containerColor = Color(0xFFE5C07B).copy(alpha = 0.22f),
                                        contentColor = Color(0xFFFFF0C2),
                                        borderColor = Color(0xFFE5C07B).copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "ولدیت: ${student.fatherName} • ${student.department} (${student.section})",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val (badgeBg, badgeText, badgeColor) = when (attStatus) {
                                        "PRESENT" -> Triple(Color(0xFF2E7D32).copy(alpha = 0.35f), "✓ حاضر (Present)", Color(0xFF81C784))
                                        "ABSENT" -> Triple(Color(0xFFC62828).copy(alpha = 0.35f), "✗ غیر حاضر (Absent)", Color(0xFFEF9A9A))
                                        "LATE" -> Triple(Color(0xFFE65100).copy(alpha = 0.35f), "⏰ تاخیر (Late)", Color(0xFFFFCC80))
                                        else -> Triple(Color.White.copy(alpha = 0.1f), "حاضری باقی (Unmarked)", Color.White.copy(alpha = 0.7f))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeBg,
                                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = "نصاب: ${student.paray}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE5C07B).copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { onNavigateToProgress() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFE5C07B), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "جائزہ درج کریں (Progress)", fontSize = 11.sp, color = Color(0xFFFFF0C2), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE5C07B),
                                modifier = Modifier.clickable { onNavigateToStudents() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF042418), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "پروفائل (Profile)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF042418))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSyncDialog) {
        SyncDetailsDialog(
            viewModel = viewModel,
            onDismiss = { showSyncDialog = false }
        )
    }
}
