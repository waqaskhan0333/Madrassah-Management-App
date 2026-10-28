package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MadrassaProfile
import com.example.data.model.Student
import com.example.ui.components.MadrassaEmblemTopBar
import com.example.ui.screens.*
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MadrassaTheme
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.HijriDateUtil
import com.example.util.ImageStorageHelper
import com.example.util.LanguageState
import com.example.util.t
import kotlinx.coroutines.delay

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
    var showSplash by remember { mutableStateOf(true) }
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

    if (showSplash) {
        Animated3dSplashLaunchScreen(
            profile = profile,
            onFinish = { showSplash = false }
        )
    } else if (selectedStudentForDetail != null) {
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
                    onLanguageToggle = { LanguageState.toggle() }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
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

@Composable
fun Animated3dSplashLaunchScreen(
    profile: MadrassaProfile,
    onFinish: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onFinish()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash3d")
    val angleY by infiniteTransition.animateFloat(
        initialValue = -22f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "angleY"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF02170F),
                        Color(0xFF042418),
                        Color(0xFF063323)
                    )
                )
            )
            .clickable { onFinish() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // 3D Animated Floating & Tilting Emblem
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer {
                        rotationY = angleY
                        translationY = floatY
                        cameraDistance = 16f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                // Expanding Golden Aura
                Box(
                    modifier = Modifier
                        .size((150 * auraScale).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE5C07B).copy(alpha = 0.35f),
                                    Color(0xFF0E6647).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Outer Rotating Islamic Star Ring
                Box(
                    modifier = Modifier
                        .size(134.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFFE5C07B),
                                    Color(0xFFFFF0C2),
                                    Color(0xFF8B7330),
                                    Color(0xFFE5C07B)
                                )
                            ),
                            CircleShape
                        )
                )

                // Center Emblem Circle with Beveled Gold Rim
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0B4A33), Color(0xFF032216))
                            )
                        )
                        .border(
                            2.5.dp,
                            Brush.linearGradient(
                                listOf(Color(0xFFE5C07B), Color(0xFFFFF3D1), Color(0xFFD4AF37))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.logoUri.isNotBlank()) {
                        AsyncImage(
                            model = ImageStorageHelper.toImageModel(profile.logoUri),
                            contentDescription = "Madrassa Logo",
                            modifier = Modifier
                                .size(105.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_madrassa_gold_emblem),
                            contentDescription = "3D Madrassa Emblem",
                            modifier = Modifier
                                .size(105.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Arabic Calligraphy Name
            Text(
                text = if (profile.nameUr.isNotBlank()) profile.nameUr else "جامعہ دارالعلوم الاسلامیہ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE5C07B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // English Institutional Name
            Text(
                text = if (profile.nameEn.isNotBlank()) profile.nameEn else "JAMIA DARUL ULOOM AL-ISLAMIA",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.95f),
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Metallic Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFE5C07B).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE5C07B).copy(alpha = 0.7f))
            ) {
                Text(
                    text = "★ 3D ISLAMIC ENTERPRISE EDITION ★",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE5C07B),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Loading / Ready Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFFE5C07B),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "100% Offline SQLite • Cloud Synchronized",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap anywhere to continue",
                fontSize = 10.5.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}
