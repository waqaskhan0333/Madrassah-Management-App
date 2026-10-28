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

    var showAddMarksDialog by remember { mutableStateOf(false) }
    var resultToEdit by remember { mutableStateOf<ExamResult?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar with Prominent Student Name & Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(EmeraldPrimary, Color(0xFF042418))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5C07B).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (student.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = ImageStorageHelper.toImageModel(student.photoUri),
                                    contentDescription = student.name,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = student.name.firstOrNull()?.uppercase() ?: "S",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFE5C07B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "DMC: ${student.name}",
                                color = Color(0xFFE5C07B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Roll: #${student.rollNo}  •  Class: ${student.department} (${student.section})",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                // Pinned Student Profile Summary Bar (Always visible above scroll)
                Surface(
                    color = EmeraldPrimaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "والد: ${student.fatherName}  •  کیمپس: ${student.campus}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "پارہ: ${student.paray}  •  داخلہ: ${student.admissionDate}",
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (student.status == "ACTIVE") Color(0xFF1B8A4C) else Color(0xFFD32F2F)
                        ) {
                            Text(
                                text = student.status,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Scrollable Document Body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Madrassa Header
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF042418))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
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
                                    text = "STUDENT DETAILED MARKS CERTIFICATE • ${profile.sessionYear}",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 10.sp
                                )
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
                                Text(text = "1. ATTENDANCE SUMMARY (حاضری کا خلاصہ)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Present: $presentCount", fontSize = 11.5.sp, color = Color(0xFF1B8A4C), fontWeight = FontWeight.Bold)
                                    Text(text = "Absent: $absentCount", fontSize = 11.5.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                                    Text(text = "Late/Leave: $lateCount", fontSize = 11.5.sp, color = Color(0xFFE67E22))
                                    val pct = if (presentCount + absentCount > 0) ((presentCount * 100) / (presentCount + absentCount)) else 100
                                    Text(text = "Ratio: $pct%", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                }
                            }
                        }
                    }

                    // Real Examination Marks Table with Add & Delete capability
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "2. EXAMINATION & EVALUATION MARKS (امتحانی نتیجہ)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                    FilledTonalButton(
                                        onClick = {
                                            resultToEdit = null
                                            showAddMarksDialog = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = EmeraldPrimaryContainer)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "+ Record Marks", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(EmeraldPrimaryContainer)
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Subject / Exam", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                                    Text(text = "Q1", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                                    Text(text = "Q2", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                                    Text(text = "Q3", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                                    Text(text = "Taj", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                                    Text(text = "Total", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                                    Text(text = "Grade", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                    Text(text = "Action", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                                }

                                if (studentResults.isNotEmpty()) {
                                    studentResults.forEachIndexed { idx, res ->
                                        val exam = allExams.find { it.id == res.examId }
                                        val displaySubject = if (res.subject.isNotBlank()) res.subject else (exam?.title ?: "Hifz / Tajweed")
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(if (idx % 2 == 1) Color(0xFFF9FBF9) else Color.White)
                                                .padding(vertical = 4.dp, horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = displaySubject, fontSize = 10.5.sp, modifier = Modifier.weight(1.3f), maxLines = 1)
                                            Text(text = "${res.question1.toInt()}", fontSize = 10.5.sp, modifier = Modifier.weight(0.5f))
                                            Text(text = "${res.question2.toInt()}", fontSize = 10.5.sp, modifier = Modifier.weight(0.5f))
                                            Text(text = "${res.question3.toInt()}", fontSize = 10.5.sp, modifier = Modifier.weight(0.5f))
                                            Text(text = "${res.talaffuz.toInt()}", fontSize = 10.5.sp, modifier = Modifier.weight(0.5f))
                                            Text(text = "${res.kulNumber.toInt()}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, modifier = Modifier.weight(0.7f))
                                            Text(text = res.kaifiyat.ifBlank { res.grade }, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B8A4C), modifier = Modifier.weight(0.8f))

                                            // Delete / Edit actions
                                            Row(modifier = Modifier.weight(0.5f)) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteExamResult(res.id)
                                                        Toast.makeText(context, "Record removed", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(15.dp))
                                                }
                                            }
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
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "کوئی امتحانی نتیجہ درج نہیں ہے\nNo official exam marks recorded yet for ${student.name}.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                resultToEdit = null
                                                showAddMarksDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Record Real Exam Marks Now", fontSize = 11.sp)
                                        }
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

    if (showAddMarksDialog) {
        StudentExamMarksInputDialog(
            student = student,
            existingResult = resultToEdit,
            exams = allExams,
            onSave = { res, isEdit ->
                viewModel.saveExamResult(res, isEdit)
                showAddMarksDialog = false
                resultToEdit = null
                Toast.makeText(context, "Exam marks recorded successfully!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = {
                showAddMarksDialog = false
                resultToEdit = null
            }
        )
    }
}

@Composable
fun StudentExamMarksInputDialog(
    student: Student,
    existingResult: ExamResult?,
    exams: List<Exam>,
    onSave: (ExamResult, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf(existingResult?.subject ?: "حفظ القرآن الکریم") }
    var q1Text by remember { mutableStateOf(existingResult?.question1?.toInt()?.toString() ?: "23") }
    var q2Text by remember { mutableStateOf(existingResult?.question2?.toInt()?.toString() ?: "23") }
    var q3Text by remember { mutableStateOf(existingResult?.question3?.toInt()?.toString() ?: "22") }
    var tajweedText by remember { mutableStateOf(existingResult?.talaffuz?.toInt()?.toString() ?: "23") }
    var remarks by remember { mutableStateOf(existingResult?.remarks ?: "ماشاء اللہ عمدہ کارکردگی") }

    val q1 = q1Text.toDoubleOrNull() ?: 0.0
    val q2 = q2Text.toDoubleOrNull() ?: 0.0
    val q3 = q3Text.toDoubleOrNull() ?: 0.0
    val taj = tajweedText.toDoubleOrNull() ?: 0.0
    val total = (q1 + q2 + q3 + taj).coerceAtMost(100.0)

    val grade = when {
        total >= 90 -> "ممتاز (A+)"
        total >= 80 -> "جید جداً (A)"
        total >= 65 -> "جید (B)"
        total >= 50 -> "مقبول (C)"
        else -> "راسب (Fail)"
    }
    val kaifiyat = when {
        total >= 90 -> "ممتاز"
        total >= 80 -> "جید جداً"
        total >= 65 -> "جید"
        total >= 50 -> "مقبول"
        else -> "راسب"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (existingResult == null) "Record Exam Marks for ${student.name}" else "Edit Exam Marks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = EmeraldPrimary
                )
                Text(
                    text = "Roll: #${student.rollNo} • Class: ${student.department} (${student.section})",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / مضمون (حفظ، ناظرہ، فقہ)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = q1Text,
                        onValueChange = { if (it.length <= 2) q1Text = it },
                        label = { Text("Q1 (25)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = q2Text,
                        onValueChange = { if (it.length <= 2) q2Text = it },
                        label = { Text("Q2 (25)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = q3Text,
                        onValueChange = { if (it.length <= 2) q3Text = it },
                        label = { Text("Q3 (25)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tajweedText,
                        onValueChange = { if (it.length <= 2) tajweedText = it },
                        label = { Text("Tajweed (25)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Marks: ${total.toInt()} / 100", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 12.5.sp)
                        Text(text = "Grade: $grade", fontWeight = FontWeight.Bold, color = Color(0xFF1B8A4C), fontSize = 12.5.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val targetExamId = existingResult?.examId ?: (exams.firstOrNull()?.id ?: 1L)
                            val res = ExamResult(
                                id = existingResult?.id ?: 0L,
                                examId = targetExamId,
                                studentId = student.id,
                                studentName = student.name,
                                fatherName = student.fatherName,
                                studentClass = "${student.department} (${student.section})",
                                paray = student.paray,
                                question1 = q1,
                                question2 = q2,
                                question3 = q3,
                                talaffuz = taj,
                                kulNumber = total,
                                totalMaxMarks = 100.0,
                                kaifiyat = kaifiyat,
                                subject = subject,
                                marksObtained = total,
                                totalMarks = 100.0,
                                grade = grade,
                                remarks = remarks,
                                isPassed = total >= 50.0
                            )
                            onSave(res, existingResult != null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Save Marks")
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
                                department = if (department != "All Depts" && department != "All Classes") department else "",
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
                                Text("Class", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.4f))
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
                                    department = if (department != "All Depts" && department != "All Classes") department else "",
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
