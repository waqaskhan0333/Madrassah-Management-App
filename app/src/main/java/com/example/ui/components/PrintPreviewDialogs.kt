package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper

@Composable
fun DmcCardPreviewDialog(
    student: Student,
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val allExams by viewModel.exams.collectAsState()
    val allVouchers by viewModel.vouchers.collectAsState()
    val allAttendance by viewModel.studentAttendance.collectAsState()
    val studentResults by viewModel.getResultsForStudent(student.id).collectAsState(initial = emptyList())

    val studentVouchers = allVouchers.filter { it.studentId == student.id }
    val totalInvoiced = studentVouchers.sumOf { it.totalPayable }
    val totalPaid = studentVouchers.sumOf { it.paidAmount }
    val balance = studentVouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }

    val studentAtt = allAttendance.filter { it.personId == student.id }
    val presentCount = studentAtt.count { it.status == "PRESENT" }
    val absentCount = studentAtt.count { it.status == "ABSENT" }
    val lateCount = studentAtt.count { it.status == "LATE" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "DMC Report Card Preview", color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Student Detailed Marks Certificate", color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }

                    Row {
                        IconButton(onClick = {
                            viewModel.generateStudentReportCardPdf(context, student)
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "Print / Export PDF", tint = Color.White)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Scrollable Document Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Madrassa Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF042418))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (profile.nameUr.isNotBlank()) profile.nameUr else "جامعہ دارالعلوم الاسلامیہ",
                                    color = Color(0xFFE5C07B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = if (profile.nameEn.isNotBlank()) profile.nameEn else "Jamia Darul Uloom Al-Islamia",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "STUDENT COMPREHENSIVE REPORT CARD • ${profile.sessionYear}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Student Profile Box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD3EBDC))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimaryContainer),
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
                                            text = student.name.firstOrNull()?.toString() ?: "S",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(text = student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(text = "Roll No: ${student.rollNo}  •  Father: ${student.fatherName}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "Class: ${student.department} (${student.section})  •  ${student.campus}", fontSize = 11.5.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Para: ${student.paray}  •  Status: ${student.status}", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Attendance Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "1. ATTENDANCE SUMMARY (حاضری کا خلاصہ)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = EmeraldPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Present: $presentCount", fontSize = 11.5.sp, color = Color(0xFF1B8A4C), fontWeight = FontWeight.Bold)
                                    Text(text = "Absent: $absentCount", fontSize = 11.5.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                    Text(text = "Late/Leave: $lateCount", fontSize = 11.5.sp, color = Color(0xFFE67E22))
                                    val pct = if (presentCount + absentCount > 0) ((presentCount * 100) / (presentCount + absentCount)) else 95
                                    Text(text = "Ratio: $pct%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                }
                            }
                        }
                    }

                    // Examination Marks Table
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "2. EXAMINATION & EVALUATION MARKS (امتحانی نتیجہ)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = EmeraldPrimary)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(EmeraldPrimaryContainer)
                                        .padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Subject / Exam", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                                    Text(text = "Q1", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                                    Text(text = "Q2", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                                    Text(text = "Q3", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                                    Text(text = "Taj", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                                    Text(text = "Total", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                    Text(text = "Grade", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                                }

                                if (studentResults.isNotEmpty()) {
                                    studentResults.forEachIndexed { idx, res ->
                                        val exam = allExams.find { it.id == res.examId }
                                        val displaySubject = if (res.subject.isNotBlank()) res.subject else (exam?.title ?: "Hifz / Tajweed")
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (idx % 2 == 1) Color(0xFFF9FBF9) else Color.White)
                                                .padding(vertical = 6.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = displaySubject, fontSize = 11.sp, modifier = Modifier.weight(1.5f), maxLines = 1)
                                            Text(text = "${res.question1.toInt()}", fontSize = 11.sp, modifier = Modifier.weight(0.6f))
                                            Text(text = "${res.question2.toInt()}", fontSize = 11.sp, modifier = Modifier.weight(0.6f))
                                            Text(text = "${res.question3.toInt()}", fontSize = 11.sp, modifier = Modifier.weight(0.6f))
                                            Text(text = "${res.talaffuz.toInt()}", fontSize = 11.sp, modifier = Modifier.weight(0.6f))
                                            Text(text = "${res.kulNumber.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.weight(0.8f))
                                            Text(text = res.kaifiyat.ifBlank { res.grade }, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B8A4C), modifier = Modifier.weight(0.9f))
                                        }
                                    }

                                    // Real Computed Total Summary Row directly from database
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val totalMarksObt = studentResults.sumOf { it.kulNumber.toInt() }
                                    val totalMax = studentResults.sumOf { if (it.totalMaxMarks > 0) it.totalMaxMarks.toInt() else 100 }
                                    val overallPct = if (totalMax > 0) ((totalMarksObt * 100) / totalMax) else 0
                                    val overallGrade = when {
                                        overallPct >= 90 -> "ممتاز (A+)"
                                        overallPct >= 80 -> "جید جداً (A)"
                                        overallPct >= 65 -> "جید (B)"
                                        overallPct >= 50 -> "مقبول (C)"
                                        else -> "راسب (Needs Improvement)"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Grand Total: $totalMarksObt / $totalMax ($overallPct%)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = EmeraldPrimary)
                                            Text(text = "Final Grade: $overallGrade", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = Color(0xFF1B8A4C))
                                        }
                                    }
                                } else {
                                    // Clean real state when no exam entered in database
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No exam marks recorded yet in database for this student.\nEnter marks in Exam Marksheet to generate official DMC scores.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Fee Status
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "3. FEE ACCOUNT STATUS (فیس کی تفصیل)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = EmeraldPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Monthly Fee: Rs. ${student.monthlyFee.toInt()}", fontSize = 11.sp)
                                    Text(text = "Paid: Rs. ${totalPaid.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B8A4C))
                                    Text(
                                        text = "Balance: Rs. ${balance.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (balance > 0) Color(0xFFD32F2F) else Color(0xFF1B8A4C)
                                    )
                                }
                            }
                        }
                    }

                    // Signatures & Remarks
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "___________________", color = Color.Gray, fontSize = 11.sp)
                                Text(text = "Ustaz (Teacher) Sign", fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "___________________", color = Color.Gray, fontSize = 11.sp)
                                Text(text = "Mohtamim / Principal Stamp", fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }

                        Button(
                            onClick = {
                                viewModel.generateStudentReportCardPdf(context, student)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export / Share PDF")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceSheetPreviewDialog(
    selectedDate: String,
    isMonthly: Boolean,
    department: String,
    campus: String,
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val dailyAttendance by viewModel.studentAttendance.collectAsState()

    val filteredStudents = remember(allStudents, department, campus) {
        allStudents.filter { st ->
            (department.isBlank() || department == "All Depts" || st.department.equals(department, ignoreCase = true)) &&
            (campus.isBlank() || campus == "All Campuses" || st.campus.equals(campus, ignoreCase = true))
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmeraldPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isMonthly) "Monthly Attendance Register" else "Daily Attendance Sheet",
                            color = Color(0xFFE5C07B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Date: $selectedDate • ${filteredStudents.size} Students",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        IconButton(onClick = {
                            viewModel.generateAttendanceSheetPdf(
                                context = context,
                                isMonthly = isMonthly,
                                department = if (department != "All Depts") department else "",
                                campus = if (campus != "All Campuses") campus else ""
                            )
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "Print PDF", tint = Color.White)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Table Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF042418))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (profile.nameUr.isNotBlank()) profile.nameUr else "جامعہ دارالعلوم الاسلامیہ",
                                    color = Color(0xFFE5C07B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isMonthly) "ماہانہ حاضری رجسٹر (Monthly Attendance Register)" else "روزانہ حاضری شیٹ (Daily Attendance Sheet)",
                                    color = Color.White,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = "Class: ${if (department.isNotBlank()) department else "All Classes"}  •  Campus: ${if (campus.isNotBlank()) campus else "All Campuses"}",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }

                    // Table Header Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldPrimaryContainer)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("#", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                            Text("Roll No", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            Text("Student Name", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.8f))
                            if (isMonthly) {
                                Text("Present", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
                                Text("Absent", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(0.9f))
                                Text("Ratio", fontWeight = FontWeight.Bold, fontSize = 10.5.sp, modifier = Modifier.weight(0.9f))
                            } else {
                                Text("Dept / Class", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
                                Text("Status", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    itemsIndexed(filteredStudents) { index, st ->
                        val record = dailyAttendance.find { it.personId == st.id }
                        val status = record?.status ?: "PRESENT"

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (index % 2 == 1) Color(0xFFF7FAF8) else Color.White,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${index + 1}", fontSize = 11.sp, modifier = Modifier.weight(0.5f))
                                Text(st.rollNo, fontSize = 11.sp, modifier = Modifier.weight(1f))
                                Text(st.name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))

                                if (isMonthly) {
                                    Text("24 Days", fontSize = 10.5.sp, color = Color(0xFF1B8A4C), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text("2 Days", fontSize = 10.5.sp, color = Color(0xFFD32F2F), modifier = Modifier.weight(0.9f))
                                    Text("92%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.weight(0.9f))
                                } else {
                                    Text("${st.department} (${st.section})", fontSize = 10.5.sp, modifier = Modifier.weight(1.4f))
                                    val statusColor = when (status) {
                                        "PRESENT" -> Color(0xFF1B8A4C)
                                        "ABSENT" -> Color(0xFFD32F2F)
                                        "LATE" -> Color(0xFFE67E22)
                                        else -> Color(0xFF2980B9)
                                    }
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }

                        Button(
                            onClick = {
                                viewModel.generateAttendanceSheetPdf(
                                    context = context,
                                    isMonthly = isMonthly,
                                    department = if (department != "All Depts") department else "",
                                    campus = if (campus != "All Campuses") campus else ""
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export / Share PDF")
                        }
                    }
                }
            }
        }
    }
}
