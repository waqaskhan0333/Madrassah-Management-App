package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    onLanguageToggle: () -> Unit = {}
) {
    var show3dEmblemDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "topBar3d")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblemFloat"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblemGlow"
    )

    Surface(
        color = Color(0xFF062D1F),
        shadowElevation = 6.dp
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
                // Left: 3D Animated Emblem / Logo + Titles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                translationY = floatAnim
                            }
                            .clip(CircleShape)
                            .background(Color(0xFFE5C07B).copy(alpha = glowPulse))
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xFFE5C07B), Color(0xFF0E6647), Color(0xFFE5C07B))
                                ),
                                CircleShape
                            )
                            .clickable { show3dEmblemDialog = true },
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

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onProfileClick() }
                    ) {
                        Text(
                            text = if (LanguageState.isUrdu) madrassaNameAr else madrassaNameEn,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B),
                            maxLines = 2,
                            lineHeight = 17.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = madrassaSubtitleAr,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (LanguageState.isUrdu) madrassaNameEn else madrassaNameAr,
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right: Language Switcher + Profile Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Switcher Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE5C07B).copy(alpha = 0.22f))
                            .clickable { onLanguageToggle() }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (LanguageState.isUrdu) "English" else "اردو",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B)
                        )
                    }

                    // Profile Avatar with Gold Ring Border
                    Box(
                        modifier = Modifier
                            .size(36.dp)
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
                                modifier = Modifier.size(20.dp)
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

    if (show3dEmblemDialog) {
        Madrassa3dEmblemDialog(
            nameAr = madrassaNameAr,
            nameEn = madrassaNameEn,
            onDismiss = { show3dEmblemDialog = false }
        )
    }
}

@Composable
fun Madrassa3dEmblemDialog(
    nameAr: String,
    nameEn: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emblem3dSpin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float3d"
    )
    val glowSize by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF042418)),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE5C07B),
                        Color(0xFF8B7330),
                        Color(0xFFE5C07B),
                        Color(0xFF0E6647)
                    )
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 3D Animated Floating & Rotating Emblem
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            translationY = floatY
                            rotationY = rotationAngle
                            cameraDistance = 18f * density
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Gold Halo
                    Box(
                        modifier = Modifier
                            .size((115 * glowSize).dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFE5C07B).copy(alpha = 0.45f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 3D Layered Golden Emblem
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF0B4A33), Color(0xFF032216))
                                )
                            )
                            .border(
                                2.5.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xFFE5C07B), Color(0xFFFFF0C2), Color(0xFFD4AF37))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_madrassa_gold_emblem),
                            contentDescription = "3D Emblem",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Titles
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = nameAr,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5C07B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = nameEn,
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE5C07B).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "★ 3D ISLAMIC INSTITUTION SEAL ★",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Offline SQLite Enterprise Engine with Real-Time Firebase Cloud Synchronization & Islamic Education Modules.",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close (بند کریں)")
                }
            }
        }
    }
}

@Composable
fun MadrassaHeroGreetingCard(
    adminName: String = "Admin",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero3d")
    val goldShimmer by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroGoldGlow"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroFloat"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F5A3E),
                            Color(0xFF083824),
                            Color(0xFF031E13)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE5C07B).copy(alpha = goldShimmer),
                            Color(0xFF8B6B23).copy(alpha = 0.5f),
                            Color(0xFFD4AF37).copy(alpha = goldShimmer * 0.8f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column: Greetings & Calligraphy
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Bismillah Header
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5C07B),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (LanguageState.isUrdu) "السلام عليكم ورحمة الله" else "As-Salamu 'Alaykum",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFD4AF37).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5C07B).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "★ $adminName",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFF0C2),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = if (LanguageState.isUrdu) "خوش آمدید" else "Welcome to Portal",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "رَّبِّ زِدْنِي عِلْمًا  •  (My Lord, increase me in knowledge)",
                        fontSize = 10.5.sp,
                        color = Color(0xFFE5C07B).copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right: 3D Mihrab Arch Emblem with Quran / Rehal Motif
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            translationY = floatY
                            rotationZ = floatY * 0.5f
                        }
                        .clip(RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFE5C07B).copy(alpha = 0.35f),
                                    Color(0xFF042718),
                                    Color(0xFF02170E)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(Color(0xFFFFF0C2), Color(0xFFD4AF37), Color(0xFF8B6B23))
                            ),
                            RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_madrassa_gold_emblem),
                        contentDescription = "Quran Rehal Emblem",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "metricScale"
    )
    val pressTiltX by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pressTiltX"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        label = "metricElevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "metric3dAmbient")
    val ambientTiltY by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTiltY"
    )
    val ambientTiltX by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = -1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTiltX"
    )
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenProgress"
    )
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                cameraDistance = 14f * density
                rotationX = pressTiltX + ambientTiltX
                rotationY = ambientTiltY
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = androidx.compose.foundation.BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF0C2).copy(alpha = borderGlow),
                    Color(0xFFE5C07B).copy(alpha = borderGlow * 0.9f),
                    Color(0xFF0E6647).copy(alpha = 0.45f),
                    Color(0xFF8B6B23).copy(alpha = 0.7f),
                    Color(0xFFE5C07B).copy(alpha = borderGlow)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F5A3E),
                            Color(0xFF073824),
                            Color(0xFF021B11)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    // 3D Embossed Radial Golden Orb
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF1E7052),
                                        Color(0xFF04291B)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color(0xFFE5C07B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFF0C2)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5C07B))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = trendText,
                        fontSize = 10.5.sp,
                        color = Color(0xFFE5C07B).copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Dynamic 3D metallic light reflection sweeping across the card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFF0C2).copy(alpha = 0.05f),
                                Color(0xFFE5C07B).copy(alpha = 0.14f),
                                Color(0xFFFFF0C2).copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            start = androidx.compose.ui.geometry.Offset(
                                x = sheenProgress * 600f,
                                y = 0f
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                x = (sheenProgress + 0.35f) * 600f,
                                y = 400f
                            )
                        )
                    )
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "attCardScale"
    )
    val pressTiltX by animateFloatAsState(
        targetValue = if (isPressed) 3.5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pressTiltX"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 8.dp,
        label = "attCardElev"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "dark3d")
    val ambientTiltY by infiniteTransition.animateFloat(
        initialValue = -1.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTiltY"
    )
    val ambientTiltX by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = -1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientTiltX"
    )
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheenProgress"
    )
    val goldPulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "goldPulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                cameraDistance = 14f * density
                rotationX = pressTiltX + ambientTiltX
                rotationY = ambientTiltY
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF042418)),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF0C2).copy(alpha = goldPulse),
                    Color(0xFFE5C07B).copy(alpha = goldPulse),
                    Color(0xFF8B7330),
                    Color(0xFF0E6647),
                    Color(0xFFE5C07B).copy(alpha = goldPulse)
                )
            )
        )
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
            QuickActionButton(
                title = if (LanguageState.isUrdu) "رپورٹس" else "Reports",
                icon = Icons.Default.PictureAsPdf,
                badge = null,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToReports
            )
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "btnScale"
    )
    val pressTiltX by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pressTiltX"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        label = "btnElev"
    )

    Card(
        modifier = modifier
            .height(86.dp)
            .graphicsLayer {
                cameraDistance = 12f * density
                rotationX = pressTiltX
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFFFFF0C2).copy(alpha = 0.6f),
                    Color(0xFFE5C07B).copy(alpha = 0.8f),
                    Color(0xFF0E6647),
                    Color(0xFF8B6B23)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0D5037),
                            Color(0xFF052B1C)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 3D Embossed Icon Orb
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF1E7052), Color(0xFF04291B))
                            )
                        )
                        .border(1.2.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color(0xFFE5C07B),
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
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
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .border(1.dp, Color(0xFFFFF0C2), CircleShape),
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFFFFF0C2).copy(alpha = 0.7f),
                    Color(0xFFE5C07B).copy(alpha = 0.85f),
                    Color(0xFF0E6647),
                    Color(0xFF8B6B23)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F5A3E),
                            Color(0xFF083824),
                            Color(0xFF021B11)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF1E7052), Color(0xFF04291B))))
                                .border(1.dp, Color(0xFFE5C07B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color(0xFFE5C07B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = t("student_progress"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Text(
                        text = t("view_all"),
                        fontSize = 12.sp,
                        color = Color(0xFFE5C07B),
                        fontWeight = FontWeight.Bold,
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
                        progressColor = Color(0xFFE5C07B)
                    )

                    ProgressGaugeItem(
                        label = t("nazra"),
                        percentage = naziraPercentage,
                        countText = naziraCount,
                        icon = Icons.Default.Book,
                        progressColor = Color(0xFF4DD0E1)
                    )

                    ProgressGaugeItem(
                        label = t("qaida"),
                        percentage = qaidaPercentage,
                        countText = qaidaCount,
                        icon = Icons.Default.AccountBalance,
                        progressColor = Color(0xFF81C784)
                    )
                }
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFFFF0C2).copy(alpha = 0.6f), Color(0xFFE5C07B).copy(alpha = 0.7f), Color(0xFF0E6647))))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0F5A3E), Color(0xFF073824), Color(0xFF021B11))))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = t("todays_classes"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text(text = t("view_all"), fontSize = 10.sp, color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onViewClassesClick() })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (courses.isEmpty()) {
                        Text(
                            text = t("no_classes_yet"),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
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
        }

        // Right Column: Real Recent Activity from Database
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(Color(0xFFFFF0C2).copy(alpha = 0.6f), Color(0xFFE5C07B).copy(alpha = 0.7f), Color(0xFF0E6647))))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0F5A3E), Color(0xFF073824), Color(0xFF021B11))))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = t("recent_activity"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text(text = t("view_all"), fontSize = 10.sp, color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onViewActivityClick() })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (recentStudents.isEmpty()) {
                        Text(
                            text = t("no_activity_yet"),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
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
