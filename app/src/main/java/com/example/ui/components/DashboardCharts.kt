package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusPresent

data class DayAttendancePoint(
    val day: Int,
    val percentage: Float // 0..100
)

@Composable
fun MonthlyAttendanceTrendChart(
    dataPoints: List<DayAttendancePoint>,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<DayAttendancePoint?>(null) }
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnimation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Month Attendance Trend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Daily Student Attendance Rate (%)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                selectedPoint?.let { pt ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimaryContainer
                    ) {
                        Text(
                            text = "Day ${pt.day}: ${pt.percentage.toInt()}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                    }
                } ?: run {
                    val avg = if (dataPoints.isNotEmpty()) dataPoints.map { it.percentage }.average().toInt() else 85
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimaryContainer
                    ) {
                        Text(
                            text = "Avg: $avg%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { offset ->
                                val stepX = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                                selectedPoint = dataPoints[index]
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 24.dp.toPx()
                    val chartHeight = height - paddingBottom
                    val maxVal = 100f

                    // Draw Horizontal Grid lines (25%, 50%, 75%, 100%)
                    val gridLines = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                    gridLines.forEach { ratio ->
                        val y = chartHeight * (1f - ratio)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    if (dataPoints.size < 2) return@Canvas

                    val stepX = width / (dataPoints.size - 1)
                    val points = dataPoints.mapIndexed { idx, pt ->
                        val x = idx * stepX
                        val y = chartHeight * (1f - (pt.percentage / maxVal) * animatedProgress)
                        Offset(x, y)
                    }

                    // Build Cubic Bézier Line Path and Fill Path
                    val linePath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val cx = (p0.x + p1.x) / 2
                            cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(linePath)
                        lineTo(points.last().x, chartHeight)
                        lineTo(points.first().x, chartHeight)
                        close()
                    }

                    // Draw Gradient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.35f),
                                EmeraldPrimary.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw Line Path
                    drawPath(
                        path = linePath,
                        color = EmeraldPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw Selected Point Marker or today's marker
                    val activePt = selectedPoint ?: dataPoints.lastOrNull()
                    if (activePt != null) {
                        val activeIdx = dataPoints.indexOf(activePt).coerceAtLeast(0)
                        val activeOffset = points[activeIdx]

                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = activeOffset
                        )
                        drawCircle(
                            color = EmeraldPrimary,
                            radius = 4.dp.toPx(),
                            center = activeOffset
                        )
                    }
                }
            }

            // X-Axis Date markers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1st", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "7th", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "14th", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "21st", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Today", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
            }
        }
    }
}

@Composable
fun FeePaymentDonutChart(
    collectedAmount: Double,
    pendingAmount: Double,
    partialAmount: Double,
    onNavigateToFees: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = (collectedAmount + pendingAmount + partialAmount).coerceAtLeast(1.0)
    val paidAngle = (collectedAmount / total * 360f).toFloat()
    val partialAngle = (partialAmount / total * 360f).toFloat()
    val pendingAngle = (pendingAmount / total * 360f).toFloat()

    val animatedAngle by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900),
        label = "donutAnimation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fee Collection & Pending Invoices",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Real-time payment clearance status",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                TextButton(onClick = onNavigateToFees) {
                    Text(text = "View Ledger", color = EmeraldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Canvas
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 18.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        // 1. Paid Arc
                        drawArc(
                            color = StatusPresent,
                            startAngle = -90f,
                            sweepAngle = paidAngle * animatedAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 2. Partial Arc
                        drawArc(
                            color = Color(0xFFFFA000),
                            startAngle = -90f + (paidAngle * animatedAngle),
                            sweepAngle = partialAngle * animatedAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // 3. Pending/Unpaid Arc
                        drawArc(
                            color = StatusAbsent,
                            startAngle = -90f + ((paidAngle + partialAngle) * animatedAngle),
                            sweepAngle = pendingAngle * animatedAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Center % text
                    val collectedPct = ((collectedAmount / total) * 100).toInt()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$collectedPct%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary
                            )
                        )
                        Text(
                            text = "Collected",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.5.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Breakdown list
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeeLegendItem(
                        label = "Paid / Cleared",
                        amount = "Rs. ${collectedAmount.toInt()}",
                        color = StatusPresent
                    )
                    FeeLegendItem(
                        label = "Partial Payments",
                        amount = "Rs. ${partialAmount.toInt()}",
                        color = Color(0xFFFFA000)
                    )
                    FeeLegendItem(
                        label = "Pending Dues",
                        amount = "Rs. ${pendingAmount.toInt()}",
                        color = StatusAbsent
                    )
                }
            }
        }
    }
}

@Composable
fun FeeLegendItem(label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = amount, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DepartmentEnrollmentBarChart(
    hifzCount: Int,
    nazraCount: Int,
    qaidaCount: Int,
    tajweedCount: Int,
    modifier: Modifier = Modifier
) {
    val total = (hifzCount + nazraCount + qaidaCount + tajweedCount).coerceAtLeast(1)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Class Enrollment (درجات کے طلبہ)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Distribution across classes & Islamic courses",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(14.dp))

            DepartmentProgressBar(label = "Hifz-ul-Quran", count = hifzCount, total = total, color = EmeraldPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            DepartmentProgressBar(label = "Nazra Quran", count = nazraCount, total = total, color = Color(0xFF1976D2))
            Spacer(modifier = Modifier.height(8.dp))
            DepartmentProgressBar(label = "Noorani Qaida", count = qaidaCount, total = total, color = Color(0xFF8E24AA))
            Spacer(modifier = Modifier.height(8.dp))
            DepartmentProgressBar(label = "Qirat & Tajweed", count = tajweedCount, total = total, color = Color(0xFFE65100))
        }
    }
}

@Composable
fun DepartmentProgressBar(label: String, count: Int, total: Int, color: Color) {
    val fraction = count.toFloat() / total.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 700),
        label = "depProgress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "$count (${(fraction * 100).toInt()}%)", fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
