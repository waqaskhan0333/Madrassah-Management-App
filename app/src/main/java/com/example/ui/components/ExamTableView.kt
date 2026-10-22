package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Exam
import com.example.data.model.ExamResult
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.util.ImageStorageHelper

@Composable
fun ExamResultsTableView(
    exam: Exam,
    results: List<ExamResult>,
    students: List<Student>,
    onSaveResult: (ExamResult, Boolean) -> Unit,
    onDeleteResult: (Long) -> Unit,
    onAutoPopulateClass: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var resultToEdit by remember { mutableStateOf<ExamResult?>(null) }
    var resultToDelete by remember { mutableStateOf<ExamResult?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSectionFilter by remember { mutableStateOf("All Sections") }
    var viewMode by remember { mutableIntStateOf(0) } // 0: Student Cards Roster, 1: Table Grid View

    val distinctSections = remember(results, exam) {
        val fromResults = results.map { it.studentClass }.filter { it.isNotBlank() }
        (listOf("All Sections", exam.section) + fromResults).distinct()
    }

    val filteredResults = remember(results, searchQuery, selectedSectionFilter, students) {
        results.filter { res ->
            val matchSection = selectedSectionFilter == "All Sections" ||
                res.studentClass.contains(selectedSectionFilter, ignoreCase = true) ||
                (exam.section.isNotBlank() && selectedSectionFilter.equals(exam.section, ignoreCase = true))
            val matchingStudent = students.find { it.id == res.studentId }
            val roll = matchingStudent?.rollNo ?: ""
            val matchSearch = searchQuery.isBlank() ||
                res.studentName.contains(searchQuery, ignoreCase = true) ||
                res.fatherName.contains(searchQuery, ignoreCase = true) ||
                res.studentClass.contains(searchQuery, ignoreCase = true) ||
                roll.contains(searchQuery, ignoreCase = true)
            matchSection && matchSearch
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Live Real-Time Room Database indicator banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmeraldPrimaryContainer.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Real-Time Sync (براہ راست ڈیٹا بیس سے ہم آہنگ)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = EmeraldPrimary
                ) {
                    Text(
                        text = if (exam.type == "HIFZ_ORAL") "Hifz Oral (حفظ زبانی)" else exam.type,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Header: Title & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "امتحانی جدول (Exam Marksheet Table)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    )
                    Text(
                        text = "${exam.title} • ${exam.section} (${results.size} طلباء درج شدہ)",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (onAutoPopulateClass != null) {
                        OutlinedButton(
                            onClick = onAutoPopulateClass,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "تمام طلباء شامل کریں", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            resultToEdit = null
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "نتیجہ درج کریں", fontSize = 11.5.sp)
                    }
                }
            }

            // Quick live stats bar
            if (results.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "کل طلباء: ${results.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "کامیاب: ${results.count { it.isPassed }}", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Text(text = "راسب: ${results.count { !it.isPassed }}", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    val avg = if (results.isNotEmpty()) String.format(Locale.ENGLISH, "%.1f", results.map { it.kulNumber }.average()) else "0"
                    Text(text = "اوسط نمبر: $avg / 100", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Filter Chips
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search student name, roll no, father name... (تلاش کریں)", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.6f),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(distinctSections) { sec ->
                    FilterChip(
                        selected = selectedSectionFilter == sec,
                        onClick = { selectedSectionFilter = sec },
                        label = { Text(sec, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // View Mode Tab: Student Cards vs Table Grid
            TabRow(
                selectedTabIndex = viewMode,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = EmeraldPrimary
            ) {
                Tab(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    text = {
                        Text(
                            text = "طلباء کارڈ لسٹ (${filteredResults.size})",
                            fontSize = 12.sp,
                            fontWeight = if (viewMode == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    text = {
                        Text(
                            text = "امتحانی جدول (Table Grid)",
                            fontSize = 12.sp,
                            fontWeight = if (viewMode == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (results.isEmpty()) "اس امتحان میں ابھی کوئی نتیجہ درج نہیں ہوا۔ کلاس کے تمام طلباء شامل کریں یا نیا نتیجہ درج کریں۔"
                            else "کوئی طالب علم موجود نہیں (No matching students found)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (onAutoPopulateClass != null && results.isEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onAutoPopulateClass,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "کلاس کے تمام طلباء شامل کریں (Auto-Add Class)")
                            }
                        } else if (searchQuery.isNotEmpty() || selectedSectionFilter != "All Sections") {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedSectionFilter = "All Sections"
                            }) {
                                Text("تمام طلباء دکھائیں (Show All)")
                            }
                        }
                    }
                }
            } else if (viewMode == 0) {
                // View Mode 0: Full-Width Student Cards Roster
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredResults.forEachIndexed { index, res ->
                        val matchingStudent = students.find { it.id == res.studentId }
                        val rollNo = matchingStudent?.rollNo ?: "Roll-${index + 1}"
                        val photoUri = matchingStudent?.photoUri ?: ""

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFDFB)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Student Photo Circle
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldPrimaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (photoUri.isNotBlank()) {
                                            AsyncImage(
                                                model = ImageStorageHelper.toImageModel(photoUri),
                                                contentDescription = res.studentName,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = res.studentName.firstOrNull()?.uppercase() ?: "ط",
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldPrimary,
                                                fontSize = 17.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = res.studentName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                softWrap = true,
                                                maxLines = 2,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when {
                                                    res.kaifiyat.contains("ممتاز") -> Color(0xFFE8F5E9)
                                                    res.kaifiyat.contains("جید") -> Color(0xFFE3F2FD)
                                                    res.kaifiyat.contains("راسب") -> Color(0xFFFFEBEE)
                                                    else -> Color(0xFFFFF3E0)
                                                }
                                            ) {
                                                Text(
                                                    text = "${res.kaifiyat} (${res.kulNumber.toInt()}/100)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        res.kaifiyat.contains("ممتاز") -> Color(0xFF2E7D32)
                                                        res.kaifiyat.contains("جید") -> Color(0xFF1565C0)
                                                        res.kaifiyat.contains("راسب") -> Color(0xFFC62828)
                                                        else -> Color(0xFFE65100)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "Roll: $rollNo • ولدیت: ${res.fatherName} • کلاس: ${res.studentClass}",
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (res.paray.isNotBlank()) {
                                            Text(
                                                text = "نصاب: ${res.paray}",
                                                fontSize = 11.sp,
                                                color = EmeraldPrimary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Detailed Questions Marks Breakdown
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "س1: ${res.question1.toInt()}", fontSize = 11.sp)
                                    Text(text = "س2: ${res.question2.toInt()}", fontSize = 11.sp)
                                    Text(text = "س3: ${res.question3.toInt()}", fontSize = 11.sp)
                                    Text(text = "تجوید: ${res.talaffuz.toInt()}", fontSize = 11.sp)
                                    Text(text = "کل: ${res.kulNumber.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick Actions: Edit & Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            resultToEdit = res
                                            showDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "تبدیل کریں (Edit)", fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedButton(
                                        onClick = { resultToDelete = res },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "حذف کریں (Delete)", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // View Mode 1: Horizontal scrollable table container
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Column {
                        // Table Header Row
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF063323))
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableHeaderCell(text = "شمار (#)", width = 50.dp)
                            TableHeaderCell(text = "نام طالب علم", width = 130.dp)
                            TableHeaderCell(text = "ولدیت", width = 120.dp)
                            TableHeaderCell(text = "درجہ / کلاس", width = 100.dp)
                            TableHeaderCell(text = "پارے / نصاب", width = 110.dp)
                            TableHeaderCell(text = "سوال اول", width = 75.dp)
                            TableHeaderCell(text = "سوال دوم", width = 75.dp)
                            TableHeaderCell(text = "سوال سوم", width = 75.dp)
                            TableHeaderCell(text = "تلفظ و تجوید", width = 85.dp)
                            TableHeaderCell(text = "کل نمبر", width = 75.dp)
                            TableHeaderCell(text = "کیفیت / نتیجہ", width = 95.dp)
                            TableHeaderCell(text = "ایکشن", width = 80.dp)
                        }

                        // Data Rows
                        filteredResults.forEachIndexed { index, res ->
                            val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF9FBF9)
                            Row(
                                modifier = Modifier
                                    .background(rowBg)
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableDataCell(text = "${index + 1}", width = 50.dp, isBold = true)
                                TableDataCell(text = res.studentName, width = 130.dp, isBold = true)
                                TableDataCell(text = res.fatherName, width = 120.dp)
                                TableDataCell(text = res.studentClass, width = 100.dp)
                                TableDataCell(text = res.paray, width = 110.dp)
                                TableDataCell(text = "${res.question1.toInt()}", width = 75.dp)
                                TableDataCell(text = "${res.question2.toInt()}", width = 75.dp)
                                TableDataCell(text = "${res.question3.toInt()}", width = 75.dp)
                                TableDataCell(text = "${res.talaffuz.toInt()}", width = 85.dp)
                                TableDataCell(text = "${res.kulNumber.toInt()}", width = 75.dp, textColor = EmeraldPrimary, isBold = true)

                                // Kaifiyat / Grade Badge
                                Box(
                                    modifier = Modifier
                                        .width(95.dp)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            res.kaifiyat.contains("ممتاز") -> Color(0xFFE8F5E9)
                                            res.kaifiyat.contains("جید") -> Color(0xFFE3F2FD)
                                            res.kaifiyat.contains("راسب") -> Color(0xFFFFEBEE)
                                            else -> Color(0xFFFFF3E0)
                                        }
                                    ) {
                                        Text(
                                            text = res.kaifiyat,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                res.kaifiyat.contains("ممتاز") -> Color(0xFF2E7D32)
                                                res.kaifiyat.contains("جید") -> Color(0xFF1565C0)
                                                res.kaifiyat.contains("راسب") -> Color(0xFFC62828)
                                                else -> Color(0xFFE65100)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Actions (Edit / Delete)
                                Row(
                                    modifier = Modifier.width(80.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    IconButton(
                                        onClick = {
                                            resultToEdit = res
                                            showDialog = true
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = EmeraldPrimary, modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(
                                        onClick = { resultToDelete = res },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F), modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Adding / Editing Student Exam Result
    if (showDialog) {
        val isEdit = resultToEdit != null
        var selectedStudentId by remember { mutableLongStateOf(resultToEdit?.studentId ?: students.firstOrNull()?.id ?: 0L) }
        val currentSt = students.find { it.id == selectedStudentId }

        var studentName by remember { mutableStateOf(resultToEdit?.studentName ?: currentSt?.name ?: "") }
        var fatherName by remember { mutableStateOf(resultToEdit?.fatherName ?: currentSt?.fatherName ?: "") }
        var studentClass by remember { mutableStateOf(resultToEdit?.studentClass ?: currentSt?.section ?: exam.section) }
        var paray by remember { mutableStateOf(resultToEdit?.paray ?: "پارہ 1 تا 5") }

        var q1Text by remember { mutableStateOf(resultToEdit?.question1?.toInt()?.toString() ?: "24") }
        var q2Text by remember { mutableStateOf(resultToEdit?.question2?.toInt()?.toString() ?: "23") }
        var q3Text by remember { mutableStateOf(resultToEdit?.question3?.toInt()?.toString() ?: "23") }
        var talaffuzText by remember { mutableStateOf(resultToEdit?.talaffuz?.toInt()?.toString() ?: "22") }

        val q1 = q1Text.toDoubleOrNull() ?: 0.0
        val q2 = q2Text.toDoubleOrNull() ?: 0.0
        val q3 = q3Text.toDoubleOrNull() ?: 0.0
        val tal = talaffuzText.toDoubleOrNull() ?: 0.0
        val total = q1 + q2 + q3 + tal

        val defaultKaifiyat = when {
            total >= 85 -> "ممتاز"
            total >= 75 -> "جید جداً"
            total >= 60 -> "جید"
            total >= 40 -> "مقبول"
            else -> "راسب"
        }
        var kaifiyat by remember { mutableStateOf(resultToEdit?.kaifiyat ?: defaultKaifiyat) }

        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isEdit) "طالب علم کا امتحانی نتیجہ تبدیل کریں" else "امتحانی نتیجہ درج کریں",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                    )

                    // Student selection picker
                    if (!isEdit && students.isNotEmpty()) {
                        Text(text = "رجسٹرڈ طالب علم منتخب کریں:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = currentSt?.name ?: "طالب علم منتخب کریں", fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                students.forEach { st ->
                                    DropdownMenuItem(
                                        text = { Text("${st.rollNo} - ${st.name} (${st.fatherName})") },
                                        onClick = {
                                            selectedStudentId = st.id
                                            studentName = st.name
                                            fatherName = st.fatherName
                                            studentClass = st.section
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = { Text("نام طالب علم (Student Name)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fatherName,
                        onValueChange = { fatherName = it },
                        label = { Text("ولدیت (Father Name)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = studentClass,
                            onValueChange = { studentClass = it },
                            label = { Text("کلاس / درجہ") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = paray,
                            onValueChange = { paray = it },
                            label = { Text("پارے / نصاب") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Marks row 1: Q1 & Q2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = q1Text,
                            onValueChange = { q1Text = it },
                            label = { Text("سوال اول (Max 25)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = q2Text,
                            onValueChange = { q2Text = it },
                            label = { Text("سوال دوم (Max 25)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Marks row 2: Q3 & Talaffuz
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = q3Text,
                            onValueChange = { q3Text = it },
                            label = { Text("سوال سوم (Max 25)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = talaffuzText,
                            onValueChange = { talaffuzText = it },
                            label = { Text("تلفظ و تجوید (Max 25)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Kul Number & Kaifiyat
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "کل حاصل کردہ نمبر: ${total.toInt()} / 100", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            Text(text = "کیفیت: $defaultKaifiyat", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }

                    OutlinedTextField(
                        value = kaifiyat,
                        onValueChange = { kaifiyat = it },
                        label = { Text("کیفیت / درجہ بندی (ممتاز / جید جداً / جید / مقبول / راسب)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("منسوخ (Cancel)")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val newResult = ExamResult(
                                    id = resultToEdit?.id ?: 0L,
                                    examId = exam.id,
                                    studentId = selectedStudentId,
                                    studentName = studentName,
                                    fatherName = fatherName,
                                    studentClass = studentClass,
                                    paray = paray,
                                    question1 = q1,
                                    question2 = q2,
                                    question3 = q3,
                                    talaffuz = tal,
                                    kulNumber = total,
                                    totalMaxMarks = 100.0,
                                    kaifiyat = kaifiyat,
                                    subject = exam.title,
                                    marksObtained = total,
                                    totalMarks = 100.0,
                                    grade = kaifiyat,
                                    remarks = "امتحان میں شمولیت",
                                    isPassed = total >= exam.passMarks
                                )
                                onSaveResult(newResult, isEdit)
                                showDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("محفوظ کریں (Save)")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    resultToDelete?.let { res ->
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            title = { Text("طالب علم کا نتیجہ خارج کریں؟") },
            text = { Text("کیا آپ واقعی ${res.studentName} کا یہ امتحانی نتیجہ ڈیلیٹ کرنا چاہتے ہیں؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteResult(res.id)
                        resultToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("ڈیلیٹ کریں")
                }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) {
                    Text("منسوخ")
                }
            }
        )
    }
}

@Composable
fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        color = Color(0xFFE5C07B),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TableDataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    textColor: Color = Color.Unspecified,
    isBold: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        fontSize = 11.sp,
        color = textColor,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}
