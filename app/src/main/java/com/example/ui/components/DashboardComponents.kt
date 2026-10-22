package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Course
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.util.ImageStorageHelper
import com.example.util.LanguageState
import com.example.util.t

@Composable
fun MadrassaEmblemTopBar(
    madrassaNameAr: String = "جامعہ دارالعلوم الاسلامیہ",
    madrassaSubtitleAr: String = "للعلوم القرآن والسنة للبنين والبنات",
    madrassaNameEn: String = "Jamia Darul Uloom Al-Islamia",
    logoUri: String = "",
    hijriDate: String = "15 Rabi-ul-Awwal 1447 AH",
    gregorianDate: String = "2025-10-04",
    adminName: String = "Admin",
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLanguageToggle: () -> Unit = {},
    syncPill: @Composable (() -> Unit)? = null
) {
    Surface(
        color = Color(0xFF062D1F),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF042418),
                            Color(0xFF083726)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Main Top Bar: Logo + Titles + Notifications + Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Emblem / Logo + Titles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4AF37).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageStorageHelper.toImageModel(logoUri),
                                contentDescription = "Madrassa Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_madrassa_gold_emblem),
                                contentDescription = "Madrassa Emblem",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (LanguageState.isUrdu) madrassaNameAr else madrassaNameEn,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = madrassaSubtitleAr,
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (LanguageState.isUrdu) madrassaNameEn else madrassaNameAr,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Sync Pill + Language Switcher + Profile Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (syncPill != null) {
                        syncPill()
                    }
                    // Language Switcher Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE5C07B).copy(alpha = 0.22f))
                            .clickable { onLanguageToggle() }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (LanguageState.isUrdu) "English" else "اردو",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Profile Avatar with Gold Ring Border
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4AF37).copy(alpha = 0.35f))
                            .clickable { onProfileClick() }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF0B422B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFFE5C07B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Bar: Hijri & Gregorian Dates | Admin Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFE5C07B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$hijriDate   •   $gregorianDate",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onProfileClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Color(0xFFE5C07B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = adminName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5C07B)
                    )
                }
            }
        }
    }
}

@Composable
fun MadrassaHeroGreetingCard(
    adminName: String = "Admin"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_madrassa_hero_banner),
                contentDescription = "Quran Hero Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF042418).copy(alpha = 0.92f),
                                Color(0xFF063323).copy(alpha = 0.82f),
                                Color(0x660B4A33)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = t("greeting"),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE5C07B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t("app_tagline"),
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = t("online_ready"),
                        fontSize = 11.sp,
                        color = Color(0xFFB0DEC4),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    trendText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F4EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = trendText,
                fontSize = 10.5.sp,
                color = EmeraldPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TodayAttendanceDarkCard(
    dateString: String,
    attendanceRate: Int,
    presentCount: Int,
    absentCount: Int,
    lateCount: Int,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF042418)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF042418),
                            Color(0xFF083A27)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header Row: Calendar + Title + Date + Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFE5C07B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = t("today_attendance"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = dateString,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Circular Gauge on Left + Counts on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Gauge
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { attendanceRate / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeWidth = 7.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$attendanceRate%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Breakdown: Present, Absent, Late
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AttendanceMetricRow(
                        label = t("present"),
                        count = "$presentCount",
                        dotColor = Color(0xFF4CAF50)
                    )
                    AttendanceMetricRow(
                        label = t("absent"),
                        count = "$absentCount",
                        dotColor = Color(0xFFE53935)
                    )
                    AttendanceMetricRow(
                        label = t("late"),
                        count = "$lateCount",
                        dotColor = Color(0xFFFFA000)
                    )
                }
            }
        }
    }
}

@Composable
fun AttendanceMetricRow(
    label: String,
    count: String,
    dotColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Text(
            text = count,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToStudents: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToExams: () -> Unit,
    onNavigateToTeachers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCourses: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = t("quick_actions"),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(title = t("students"), icon = Icons.Default.Groups, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToStudents)
            QuickActionButton(title = t("attendance"), icon = Icons.Default.FactCheck, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToAttendance)
            QuickActionButton(title = t("progress"), icon = Icons.AutoMirrored.Filled.MenuBook, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToProgress)
            QuickActionButton(title = t("fees"), icon = Icons.Default.Payments, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToFees)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(title = t("exams"), icon = Icons.Default.Assignment, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToExams)
            QuickActionButton(title = t("asatiza"), icon = Icons.Default.Person, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToTeachers)
            QuickActionButton(title = t("classes"), icon = Icons.Default.Class, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToCourses)
            QuickActionButton(title = t("pdf_backup"), icon = Icons.Default.BarChart, badge = null, modifier = Modifier.weight(1f), onClick = onNavigateToReports)
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    badge: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F4EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 6.dp)
                        .align(Alignment.TopEnd)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = badge, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StudentProgressGaugesSection(
    hifzPercentage: Int,
    hifzCount: String,
    naziraPercentage: Int,
    naziraCount: String,
    qaidaPercentage: Int,
    qaidaCount: String,
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = t("student_progress"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = t("view_all"),
                    fontSize = 12.sp,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressGaugeItem(
                    label = t("hifz"),
                    percentage = hifzPercentage,
                    countText = hifzCount,
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    progressColor = EmeraldPrimary
                )

                ProgressGaugeItem(
                    label = t("nazra"),
                    percentage = naziraPercentage,
                    countText = naziraCount,
                    icon = Icons.Default.Book,
                    progressColor = Color(0xFF1976D2)
                )

                ProgressGaugeItem(
                    label = t("qaida"),
                    percentage = qaidaPercentage,
                    countText = qaidaCount,
                    icon = Icons.Default.AccountBalance,
                    progressColor = Color(0xFF00796B)
                )
            }
        }
    }
}

@Composable
fun ProgressGaugeItem(
    label: String,
    percentage: Int,
    countText: String,
    icon: ImageVector,
    progressColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { (percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxSize(),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f),
                strokeWidth = 5.dp
            )
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = progressColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(text = "$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = progressColor)
        Text(text = countText, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ClassesAndActivitySection(
    courses: List<Course>,
    recentStudents: List<Student>,
    onViewClassesClick: () -> Unit = {},
    onViewActivityClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column: Real Classes from Database
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = t("todays_classes"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = t("view_all"), fontSize = 10.sp, color = EmeraldPrimary, modifier = Modifier.clickable { onViewClassesClick() })
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (courses.isEmpty()) {
                    Text(
                        text = t("no_classes_yet"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    courses.take(4).forEachIndexed { idx, crs ->
                        ClassScheduleItem(
                            time = crs.duration,
                            name = if (LanguageState.isUrdu && crs.nameUr.isNotBlank()) crs.nameUr else crs.nameEn,
                            isOngoing = idx == 0
                        )
                    }
                }
            }
        }

        // Right Column: Real Recent Activity from Database
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = t("recent_activity"), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = t("view_all"), fontSize = 10.sp, color = EmeraldPrimary, modifier = Modifier.clickable { onViewActivityClick() })
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentStudents.isEmpty()) {
                    Text(
                        text = t("no_activity_yet"),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    recentStudents.take(4).forEach { st ->
                        RecentActivityItem(
                            title = "${st.name} (${st.rollNo})",
                            sub = "${st.department} • ${st.campus}",
                            time = st.admissionDate,
                            iconBg = EmeraldPrimary,
                            icon = Icons.Default.Person
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassScheduleItem(time: String, name: String, isOngoing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = time, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = name, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isOngoing) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF2E7D32)
            ) {
                Text(text = t("ongoing"), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 8.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(text = t("upcoming"), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RecentActivityItem(title: String, sub: String, time: String, iconBg: Color, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = sub, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(text = time, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
