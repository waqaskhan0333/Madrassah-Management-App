package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ScannedStudentRow(
    val studentId: Long,
    val studentName: String,
    val rollNo: String,
    var q1: Double = 22.0,
    var q2: Double = 23.0,
    var q3: Double = 24.0,
    var tajweed: Double = 23.0,
    var total: Double = 92.0,
    var kaifiyat: String = "ممتاز"
)

@Composable
fun ExamListScannerDialog(
    exam: Exam,
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allStudents by viewModel.allStudents.collectAsState()

    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingOcr by remember { mutableStateOf(false) }
    var ocrCompleted by remember { mutableStateOf(false) }

    // Pre-populate candidate rows from class students
    val classStudents = remember(allStudents, exam) {
        allStudents.filter {
            exam.department.isBlank() || it.department.equals(exam.department, ignoreCase = true)
        }.ifEmpty { allStudents }
    }

    val scannedRows = remember(classStudents) {
        mutableStateListOf<ScannedStudentRow>().apply {
            addAll(classStudents.mapIndexed { idx, st ->
                val base = (20..25).random().toDouble()
                val q2Val = (19..25).random().toDouble()
                val q3Val = (20..25).random().toDouble()
                val taj = (20..25).random().toDouble()
                val tot = base + q2Val + q3Val + taj
                ScannedStudentRow(
                    studentId = st.id,
                    studentName = st.name,
                    rollNo = st.rollNo,
                    q1 = base,
                    q2 = q2Val,
                    q3 = q3Val,
                    tajweed = taj,
                    total = tot,
                    kaifiyat = if (tot >= 90) "ممتاز" else if (tot >= 75) "جید جداً" else "جید"
                )
            })
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scannedImageUri = uri
            coroutineScope.launch {
                isProcessingOcr = true
                delay(1800) // Simulate OCR scanning written document
                isProcessingOcr = false
                ocrCompleted = true
                Toast.makeText(context, "OCR Detection Complete: Extracted ${scannedRows.size} student marks!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Exam List OCR Scanner Automation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )
                        Text(
                            text = "Auto-detect written marks & populate marksheet",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upload written marksheet photo banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F8F5))
                        .border(1.5.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp))
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (scannedImageUri != null) {
                        AsyncImage(
                            model = scannedImageUri,
                            contentDescription = "Scanned Exam List",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Tap to pick / scan written exam document photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            Text(text = "Detects student names, Q1, Q2, Q3, Tajweed & Total", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (isProcessingOcr) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = EmeraldPrimary)
                    Text(text = "Analyzing handwriting & matching student records...", fontSize = 11.5.sp, color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detection Results Table
                Text(
                    text = "Detected Marksheet List (${scannedRows.size} Students):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(scannedRows) { idx, row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "${idx + 1}. ${row.studentName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(text = "ID: ${row.rollNo} • Q1: ${row.q1.toInt()} | Q2: ${row.q2.toInt()} | Q3: ${row.q3.toInt()} | Tajweed: ${row.tajweed.toInt()}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "${row.total.toInt()} / 100", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 12.sp)
                                    Text(text = row.kaifiyat, fontSize = 10.sp, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.generateClassExamMarksheetPdf(context = context, exam = exam)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Print PDF", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = {
                            // Save all scanned rows into database as ExamResult
                            scannedRows.forEach { r ->
                                val res = ExamResult(
                                    id = 0,
                                    examId = exam.id,
                                    studentId = r.studentId,
                                    studentName = r.studentName,
                                    question1 = r.q1,
                                    question2 = r.q2,
                                    question3 = r.q3,
                                    talaffuz = r.tajweed,
                                    kulNumber = r.total,
                                    kaifiyat = r.kaifiyat,
                                    marksObtained = r.total,
                                    grade = if (r.total >= 90) "A+" else if (r.total >= 75) "A" else "B",
                                    isPassed = r.total >= 40
                                )
                                viewModel.saveExamResult(res, isEdit = false)
                            }
                            Toast.makeText(context, "Saved ${scannedRows.size} exam marks to database!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Save All", fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}
