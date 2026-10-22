package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HifzProgress
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer

data class DailyMemorizationPoint(
    val dayNumber: Int,
    val dateString: String,
    val ayahsMemorized: Int,
    val chaptersCompleted: Float, // e.g. 0.25 para, 0.5 para, 1.0 para
    val surahName: String,
    val ayahRange: String,
    val grade: String = "Mumtaz",
    val mistakes: Int = 0
)

@Composable
fun HifzProgressRechartsTracker(
    hifzHistory: List<HifzProgress>,
    modifier: Modifier = Modifier
) {
    var trackerMode by remember { mutableIntStateOf(0) } // 0: Daily Ayahs Memorized, 1: Chapters / Paras Completed
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    // Generate 30-day historical points based on real progress or synthesized consecutive 30 days
    val points = remember(hifzHistory) {
        val list = mutableListOf<DailyMemorizationPoint>()
        val baseCount = if (hifzHistory.isNotEmpty()) hifzHistory.size else 0
        for (i in 1..30) {
            val matchedRecord = hifzHistory.getOrNull(i % (baseCount.coerceAtLeast(1)))
            val ayahs = when {
                i % 7 == 0 -> 25 // Peak weekend test day
                i % 5 == 0 -> 18
                i % 3 == 0 -> 14
                i % 2 == 0 -> 12
                else -> 10
            }
            val chapters = (ayahs / 20.0f).coerceIn(0.1f, 1.25f)
            val surah = matchedRecord?.sabaqSurah ?: if (i < 15) "Al-Mulk" else "Al-Qalam"
            val range = "Ayah ${(i * 2) % 30 + 1}-${(i * 2) % 30 + ayahs}"

            list.add(
                DailyMemorizationPoint(
                    dayNumber = i,
                    dateString = "Day $i",
                    ayahsMemorized = ayahs,
                    chaptersCompleted = chapters,
                    surahName = surah,
                    ayahRange = range,
                    grade = matchedRecord?.grade ?: if (i % 4 == 0) "Jayyid" else "Mumtaz",
                    mistakes = if (i % 6 == 0) 1 else 0
                )
            )
        }
        list
    }

    val totalAyahs30Days = points.sumOf { it.ayahsMemorized }
    val totalChapters30Days = points.sumOf { it.chaptersCompleted.toDouble() }
    val avgAyahs = (totalAyahs30Days / 30.0).toInt()
    val peakPoint = points.maxByOrNull { it.ayahsMemorized }

    val activePoint = selectedDayIndex?.let { points.getOrNull(it) } ?: peakPoint

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900),
        label = "chartAnim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Recharts title & Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "30-Day Memorization Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Interactive visual progress analytics",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Total badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimaryContainer
                ) {
                    Text(
                        text = if (trackerMode == 0) "$totalAyahs30Days Ayahs" else "${String.format("%.1f", totalChapters30Days)} Paras",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Switcher Tabs: Daily Ayahs vs Chapters / Paras
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (trackerMode == 0) EmeraldPrimary else Color.Transparent)
                        .clickable { trackerMode = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daily Memorization (Ayahs)",
                        fontSize = 11.5.sp,
                        fontWeight = if (trackerMode == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (trackerMode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (trackerMode == 1) EmeraldPrimary else Color.Transparent)
                        .clickable { trackerMode = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chapters / Paras Completed",
                        fontSize = 11.5.sp,
                        fontWeight = if (trackerMode == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (trackerMode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recharts-style Tooltip Card (updates on tap)
            activePoint?.let { pt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Day ${pt.dayNumber} • Surah ${pt.surahName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${pt.ayahRange} (${pt.ayahsMemorized} Ayahs • Mistakes: ${pt.mistakes})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = pt.grade,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive 30-Day Bar Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val barSlot = size.width / 30f
                                val tappedIndex = (offset.x / barSlot).toInt().coerceIn(0, 29)
                                selectedDayIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 20.dp.toPx()
                    val chartHeight = height - bottomPadding

                    val maxVal = if (trackerMode == 0) 30f else 2.0f
                    val slotWidth = width / 30f
                    val barWidth = (slotWidth * 0.65f).coerceAtLeast(3f)

                    // Draw Target / Average Dotted Guideline
                    val avgY = chartHeight * (1f - (avgAyahs / maxVal))
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.4f),
                        start = Offset(0f, avgY),
                        end = Offset(width, avgY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // Render 30 Bars
                    points.forEachIndexed { index, pt ->
                        val value = if (trackerMode == 0) pt.ayahsMemorized.toFloat() else pt.chaptersCompleted
                        val barHeight = (chartHeight * (value / maxVal) * animationProgress).coerceAtLeast(4f)
                        val left = (index * slotWidth) + ((slotWidth - barWidth) / 2f)
                        val top = chartHeight - barHeight

                        val isSelected = selectedDayIndex == index || (selectedDayIndex == null && pt == peakPoint)

                        val barBrush = if (isSelected) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE5C07B), EmeraldPrimary),
                                startY = top,
                                endY = chartHeight
                            )
                        } else if (value >= 18) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF4CAF50), EmeraldPrimary),
                                startY = top,
                                endY = chartHeight
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(EmeraldPrimary.copy(alpha = 0.85f), EmeraldPrimary.copy(alpha = 0.4f)),
                                startY = top,
                                endY = chartHeight
                            )
                        }

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // X-Axis Day Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Day 1", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Day 8", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Day 15", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Day 22", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Day 30 (Today)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Hifz Summary Counter Cards (Matching image: Daily Sabaq, Sabqi, Manzil, Revision)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HifzCounterCard(label = "Daily Sabaq", count = "1 / 1", modifier = Modifier.weight(1f))
                HifzCounterCard(label = "Manzil", count = "3 / 30", modifier = Modifier.weight(1f))
                HifzCounterCard(label = "Para", count = "2 / 30", modifier = Modifier.weight(1f))
                HifzCounterCard(label = "Revision", count = "4 / 7", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HifzCounterCard(
    label: String,
    count: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = EmeraldPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
