package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.MadrassaEmblemTopBar
import com.example.ui.components.SyncStatusPill
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MadrassaTheme
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.HijriDateUtil
import com.example.util.LanguageState
import com.example.util.t

class MainActivity : FragmentActivity() {
    private val viewModel: MadrassaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isUrdu = LanguageState.isUrdu
            val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MadrassaTheme {
                    MadrassaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MadrassaApp(viewModel: MadrassaViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableIntStateOf(0) } // 0: Home/Dashboard, 1: Students, 2: Attendance, 3: Progress, 4: More
    var selectedStudentForDetail by remember { mutableStateOf<Student?>(null) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var moreSubSectionToOpen by remember { mutableStateOf<String?>(null) }
    var isMoreAtRoot by remember { mutableStateOf(true) }

    val profile by viewModel.profile.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val currentLang = LanguageState.currentLanguage
    val (hijriDate, gregDate) = HijriDateUtil.getFormattedDates(currentLang)

    // Handle system back button when in detail view
    if (selectedStudentForDetail != null) {
        val currentStudent = allStudents.find { it.id == selectedStudentForDetail!!.id } ?: selectedStudentForDetail!!
        StudentDetailScreen(
            student = currentStudent,
            viewModel = viewModel,
            onBack = { selectedStudentForDetail = null },
            onEditStudent = { student ->
                studentToEdit = student
                showAddStudentDialog = true
            }
        )
    } else {
        Scaffold(
            topBar = {
                MadrassaEmblemTopBar(
                    madrassaNameAr = if (profile.nameUr.isNotBlank()) profile.nameUr else "جامعہ دارالعلوم الاسلامیہ",
                    madrassaSubtitleAr = "للعلوم القرآن والسنة للبنين والبنات",
                    madrassaNameEn = if (profile.nameEn.isNotBlank()) profile.nameEn else "Jamia Darul Uloom Al-Islamia",
                    logoUri = profile.logoUri,
                    hijriDate = hijriDate,
                    gregorianDate = gregDate,
                    adminName = if (profile.principalName.isNotBlank()) profile.principalName else "Admin",
                    onNotificationClick = { currentScreen = 3 },
                    onProfileClick = {
                        moreSubSectionToOpen = "SETTINGS"
                        currentScreen = 4
                    },
                    onLanguageToggle = { LanguageState.toggle() },
                    syncPill = { SyncStatusPill(viewModel = viewModel) }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF042418),
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen == 0,
                        onClick = {
                            moreSubSectionToOpen = null
                            currentScreen = 0
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = t("home")) },
                        label = { Text(text = t("home"), fontSize = 11.sp, fontWeight = if (currentScreen == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE5C07B),
                            selectedTextColor = Color(0xFFE5C07B),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.22f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == 1,
                        onClick = {
                            moreSubSectionToOpen = null
                            currentScreen = 1
                        },
                        icon = { Icon(Icons.Default.Groups, contentDescription = t("students")) },
                        label = { Text(text = t("students"), fontSize = 11.sp, fontWeight = if (currentScreen == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE5C07B),
                            selectedTextColor = Color(0xFFE5C07B),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.22f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == 2,
                        onClick = {
                            moreSubSectionToOpen = null
                            currentScreen = 2
                        },
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = t("attendance")) },
                        label = { Text(text = t("attendance"), fontSize = 11.sp, fontWeight = if (currentScreen == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE5C07B),
                            selectedTextColor = Color(0xFFE5C07B),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.22f)
                        )
                    )
                    NavigationBarItem(
                        selected = currentScreen == 4,
                        onClick = {
                            moreSubSectionToOpen = null
                            currentScreen = 4
                        },
                        icon = { Icon(Icons.Default.Menu, contentDescription = t("more")) },
                        label = { Text(text = t("more"), fontSize = 11.sp, fontWeight = if (currentScreen == 4) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE5C07B),
                            selectedTextColor = Color(0xFFE5C07B),
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color(0xFFD4AF37).copy(alpha = 0.22f)
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToStudents = { currentScreen = 1 },
                        onNavigateToAttendance = { currentScreen = 2 },
                        onNavigateToProgress = { currentScreen = 3 },
                        onNavigateToFees = {
                            moreSubSectionToOpen = "FEES"
                            currentScreen = 4
                        },
                        onNavigateToExams = {
                            moreSubSectionToOpen = "EXAMS"
                            currentScreen = 4
                        },
                        onNavigateToCourses = {
                            moreSubSectionToOpen = "COURSES"
                            currentScreen = 4
                        },
                        onNavigateToTeachers = {
                            moreSubSectionToOpen = "TEACHERS"
                            currentScreen = 4
                        },
                        onAddStudentClick = {
                            studentToEdit = null
                            showAddStudentDialog = true
                        }
                    )
                    1 -> StudentsScreen(
                        viewModel = viewModel,
                        onStudentClick = { student -> selectedStudentForDetail = student },
                        onAddStudentClick = {
                            studentToEdit = null
                            showAddStudentDialog = true
                        }
                    )
                    2 -> AttendanceScreen(viewModel = viewModel)
                    3 -> ProgressScreen(viewModel = viewModel)
                    4 -> MoreMenuScreen(
                        viewModel = viewModel,
                        initialSubSection = moreSubSectionToOpen,
                        onSubSectionClosed = {
                            moreSubSectionToOpen = null
                            isMoreAtRoot = true
                        },
                        onActiveSubSectionChanged = { active ->
                            isMoreAtRoot = (active == null)
                        }
                    )
                }
            }
        }

        if (currentScreen != 0) {
            BackHandler(enabled = (currentScreen != 4 || isMoreAtRoot)) {
                if (currentScreen == 4) {
                    if (moreSubSectionToOpen != null) {
                        moreSubSectionToOpen = null
                    } else {
                        currentScreen = 0
                    }
                } else {
                    currentScreen = 0
                }
            }
        }
    }

    if (showAddStudentDialog) {
        AddEditStudentDialog(
            studentToEdit = studentToEdit,
            viewModel = viewModel,
            onDismiss = {
                showAddStudentDialog = false
                studentToEdit = null
            }
        )
    }
}
