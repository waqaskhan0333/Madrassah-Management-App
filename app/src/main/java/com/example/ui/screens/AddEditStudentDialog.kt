package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper
import com.example.util.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStudentDialog(
    studentToEdit: Student? = null,
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isEdit = studentToEdit != null

    var rollNo by remember { mutableStateOf(studentToEdit?.rollNo ?: "STU0${(10..99).random()}") }
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var fatherName by remember { mutableStateOf(studentToEdit?.fatherName ?: "") }
    var guardianName by remember { mutableStateOf(studentToEdit?.guardianName ?: "") }
    var guardianPhone by remember { mutableStateOf(studentToEdit?.guardianPhone ?: "0300-") }
    var phone2 by remember { mutableStateOf(studentToEdit?.phone2 ?: "") }
    var dob by remember { mutableStateOf(studentToEdit?.dob ?: "2012-05-10") }
    var gender by remember { mutableStateOf(studentToEdit?.gender ?: "Male") }
    var cnicBForm by remember { mutableStateOf(studentToEdit?.cnicBForm ?: "") }
    var admissionDate by remember { mutableStateOf(studentToEdit?.admissionDate ?: "2024-09-01") }
    var department by remember { mutableStateOf(studentToEdit?.department ?: "Hifz") }
    var section by remember { mutableStateOf(studentToEdit?.section ?: "Class 1A") }
    var monthlyFeeStr by remember { mutableStateOf(studentToEdit?.monthlyFee?.toInt()?.toString() ?: "2500") }
    var discountStr by remember { mutableStateOf(studentToEdit?.discount?.toInt()?.toString() ?: "0") }
    var address by remember { mutableStateOf(studentToEdit?.address ?: "Lahore") }
    var photoUri by remember { mutableStateOf(studentToEdit?.photoUri ?: "") }
    var campus by remember { mutableStateOf(studentToEdit?.campus ?: "Main Campus") }
    var paray by remember { mutableStateOf(studentToEdit?.paray ?: "پارہ 1") }
    var status by remember { mutableStateOf(studentToEdit?.status ?: "ACTIVE") }
    var notes by remember { mutableStateOf(studentToEdit?.notes ?: "") }

    // Universal SAF GetContent image picker works reliably across all devices & emulators
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageToInternalStorage(
                context = context,
                sourceUri = uri,
                subFolder = "student_photos",
                prefix = "student"
            )
            if (savedPath != null) {
                photoUri = savedPath
                Toast.makeText(context, "Photo uploaded successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val departments = listOf("Hifz", "Nazra", "Qaida", "Qirat & Tajweed")
    val campuses = listOf("Main Campus", "Campus 2 (Girls)", "Campus 3 (Boys)", "Campus 4 (Branch)")
    val parayList = (1..30).map { "پارہ $it" }

    var deptExpanded by remember { mutableStateOf(false) }
    var campusExpanded by remember { mutableStateOf(false) }
    var parayExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEdit) t("edit_student") else t("add_student"),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )

                // Student Photo Upload Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimaryContainer)
                            .clickable {
                                imagePickerLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotBlank()) {
                            AsyncImage(
                                model = ImageStorageHelper.toImageModel(photoUri),
                                contentDescription = "Student Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Text(
                                text = if (photoUri.isBlank()) t("upload_photo") else t("change_photo"),
                                fontSize = 12.sp,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (photoUri.isNotBlank()) {
                            TextButton(onClick = { photoUri = "" }) {
                                Text(
                                    text = t("delete"),
                                    fontSize = 12.sp,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }
                    }
                }

                // Roll No
                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it },
                    label = { Text(t("roll_no")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Student Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(t("name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Father's Name
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text(t("father_name")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Guardian Phone
                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text(t("guardian_phone")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Campus Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = campus,
                        onValueChange = { campus = it },
                        label = { Text(t("campus")) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { campusExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Campus")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = campusExpanded,
                        onDismissRequest = { campusExpanded = false }
                    ) {
                        campuses.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c) },
                                onClick = {
                                    campus = c
                                    campusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Paray Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = paray,
                        onValueChange = { paray = it },
                        label = { Text(t("paray")) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { parayExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Para")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = parayExpanded,
                        onDismissRequest = { parayExpanded = false }
                    ) {
                        parayList.take(15).forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    paray = p
                                    parayExpanded = false
                                }
                            )
                        }
                    }
                }

                // Department Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text(t("department")) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { deptExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Class")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = deptExpanded,
                        onDismissRequest = { deptExpanded = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    department = dept
                                    deptExpanded = false
                                }
                            )
                        }
                    }
                }

                // Class / Section
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text(t("section")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Monthly Fee & Discount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = monthlyFeeStr,
                        onValueChange = { monthlyFeeStr = it },
                        label = { Text("Fee (PKR)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Discount") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(t("address")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Date of Birth & Gender
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Admission Date & CNIC / B-Form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = admissionDate,
                        onValueChange = { admissionDate = it },
                        label = { Text("Admission Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cnicBForm,
                        onValueChange = { cnicBForm = it },
                        label = { Text("B-Form / CNIC") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Guardian Name & Phone 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = guardianName,
                        onValueChange = { guardianName = it },
                        label = { Text("Guardian Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone2,
                        onValueChange = { phone2 = it },
                        label = { Text("Alt Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Status Selector (ACTIVE, LEFT, GRADUATED)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ACTIVE", "LEFT", "GRADUATED").forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = t("cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (name.isBlank() || rollNo.isBlank()) {
                                Toast.makeText(context, "Name and Roll No are required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val fee = monthlyFeeStr.toDoubleOrNull() ?: 2500.0
                            val disc = discountStr.toDoubleOrNull() ?: 0.0

                            val student = studentToEdit?.copy(
                                rollNo = rollNo.trim(),
                                name = name.trim(),
                                fatherName = fatherName.trim(),
                                guardianName = guardianName.trim(),
                                guardianPhone = guardianPhone.trim(),
                                phone2 = phone2.trim(),
                                dob = dob.trim(),
                                gender = gender.trim(),
                                cnicBForm = cnicBForm.trim(),
                                admissionDate = admissionDate.trim(),
                                department = department,
                                section = section.trim(),
                                campus = campus.trim(),
                                paray = paray.trim(),
                                monthlyFee = fee,
                                discount = disc,
                                address = address.trim(),
                                photoUri = photoUri,
                                status = status,
                                notes = notes.trim()
                            ) ?: Student(
                                id = 0,
                                rollNo = rollNo.trim(),
                                name = name.trim(),
                                fatherName = fatherName.trim(),
                                guardianName = guardianName.trim(),
                                guardianPhone = guardianPhone.trim(),
                                phone2 = phone2.trim(),
                                dob = dob.trim(),
                                gender = gender.trim(),
                                cnicBForm = cnicBForm.trim(),
                                admissionDate = admissionDate.trim(),
                                department = department,
                                section = section.trim(),
                                campus = campus.trim(),
                                paray = paray.trim(),
                                monthlyFee = fee,
                                discount = disc,
                                address = address.trim(),
                                photoUri = photoUri,
                                status = status,
                                notes = notes.trim()
                            )

                            viewModel.saveStudent(
                                student = student,
                                isEdit = isEdit,
                                onSuccess = {
                                    Toast.makeText(context, t("success_saved"), Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(text = t("save"))
                    }
                }
            }
        }
    }
}
