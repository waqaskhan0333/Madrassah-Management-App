package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FullCalendarAttendanceView(
    selectedDate: String, // "yyyy-MM-dd"
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    var displayCalendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                try {
                    val parsed = sdf.parse(selectedDate)
                    if (parsed != null) time = parsed
                } catch (_: Exception) {}
            }
        )
    }

    val currentMonthYear = remember(displayCalendar.timeInMillis) {
        monthYearFormat.format(displayCalendar.time)
    }

    // Calculate days for the calendar grid
    val daysInMonth = remember(displayCalendar.timeInMillis) {
        val cal = displayCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday, 2=Monday...

        val list = mutableListOf<CalendarDay>()
        // Blank leading days
        for (i in 1 until firstDayOfWeek) {
            list.add(CalendarDay(0, ""))
        }
        // Month days
        for (d in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, d)
            val dateStr = sdf.format(cal.time)
            list.add(CalendarDay(d, dateStr))
        }
        list
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Calendar Header: Previous Month, Month Name, Next Month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newCal = displayCalendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        displayCalendar = newCal
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month", tint = EmeraldPrimary)
                }

                Text(
                    text = currentMonthYear,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                )

                IconButton(
                    onClick = {
                        val newCal = displayCalendar.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        displayCalendar = newCal
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month", tint = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
            Row(modifier = Modifier.fillMaxWidth()) {
                val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 7-Column Days Grid
            val rows = daysInMonth.chunked(7)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (i in 0 until 7) {
                            val dayItem = week.getOrNull(i)
                            if (dayItem != null && dayItem.dayNumber > 0) {
                                val isSelected = dayItem.dateString == selectedDate
                                val isToday = dayItem.dateString == sdf.format(Date())

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.1f)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> EmeraldPrimary
                                                isToday -> EmeraldPrimaryContainer
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { onDateSelected(dayItem.dateString) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${dayItem.dayNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isToday -> EmeraldPrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CalendarDay(
    val dayNumber: Int,
    val dateString: String
)
