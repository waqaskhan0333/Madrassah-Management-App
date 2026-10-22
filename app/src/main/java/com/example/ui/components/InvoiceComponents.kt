package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.FeeVoucher
import com.example.data.model.MadrassaProfile
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer

@Composable
fun InvoiceSlipDialog(
    voucher: FeeVoucher,
    student: Student?,
    profile: MadrassaProfile,
    onDismiss: () -> Unit,
    onPayClick: () -> Unit
) {
    val context = LocalContext.current
    val balanceDue = voucher.totalPayable - voucher.paidAmount

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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.logoUri.isNotBlank()) {
                            AsyncImage(
                                model = profile.logoUri,
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.nameEn,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )
                        Text(
                            text = profile.nameUr,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Invoice Title & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FEE INVOICE",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Text(
                            text = "Invoice #: ${voucher.voucherNo}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        )
                    }
                    StatusChip(status = voucher.status)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Student Details Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        InvoiceDetailRow(label = "Student Name:", value = student?.name ?: "Unknown")
                        InvoiceDetailRow(label = "Roll Number:", value = student?.rollNo ?: "N/A")
                        InvoiceDetailRow(label = "Father's Name:", value = student?.fatherName ?: "N/A")
                        InvoiceDetailRow(label = "Class / Department:", value = "${student?.department} (${student?.section})")
                        InvoiceDetailRow(label = "Billing Month:", value = voucher.monthYear)
                        InvoiceDetailRow(label = "Due Date:", value = voucher.dueDate)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Line Items Table
                Text(
                    text = "Invoice Breakdown",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InvoiceItemRow(label = "Monthly Tuition Fee", amount = "Rs. ${voucher.tuitionFee.toInt()}")
                    if (voucher.admissionFee > 0) {
                        InvoiceItemRow(label = "Admission Fee", amount = "Rs. ${voucher.admissionFee.toInt()}")
                    }
                    if (voucher.examFee > 0) {
                        InvoiceItemRow(label = "Exam / Assessment Fee", amount = "Rs. ${voucher.examFee.toInt()}")
                    }
                    if (voucher.discount > 0) {
                        InvoiceItemRow(label = "Fee Concession / Discount", amount = "-Rs. ${voucher.discount.toInt()}", isDiscount = true)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Total, Paid & Balance
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Payable:", fontWeight = FontWeight.Bold)
                        Text(text = "Rs. ${voucher.totalPayable.toInt()}", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Paid Amount:", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                        Text(text = "Rs. ${voucher.paidAmount.toInt()}", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Outstanding Balance:", color = if (balanceDue > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text(text = "Rs. ${balanceDue.toInt()}", color = if (balanceDue > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer note
                Text(
                    text = profile.receiptFooter,
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, textAlign = TextAlign.Center)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // WhatsApp Share
                    OutlinedButton(
                        onClick = {
                            val guardianPhone = student?.guardianPhone ?: ""
                            val clean = guardianPhone.replace("-", "").replace(" ", "")
                            val formatted = if (clean.startsWith("0")) "+92" + clean.substring(1) else clean
                            val msg = """
                                *${profile.nameEn}*
                                *Fee Invoice #${voucher.voucherNo}*
                                --------------------------------
                                *Student:* ${student?.name} (Roll: ${student?.rollNo})
                                *Month:* ${voucher.monthYear}
                                *Total Payable:* Rs. ${voucher.totalPayable.toInt()}
                                *Amount Paid:* Rs. ${voucher.paidAmount.toInt()}
                                *Balance Due:* Rs. ${balanceDue.toInt()}
                                *Due Date:* ${voucher.dueDate}
                                *Status:* ${voucher.status}
                                
                                ${profile.receiptFooter}
                            """.trimIndent()

                            val uri = Uri.parse("https://wa.me/$formatted?text=${Uri.encode(msg)}")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (_: Exception) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "WhatsApp", fontSize = 12.sp)
                    }

                    if (voucher.status != "PAID") {
                        Button(
                            onClick = {
                                onDismiss()
                                onPayClick()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Pay Now", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text(text = "Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InvoiceItemRow(label: String, amount: String, isDiscount: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 12.sp, color = if (isDiscount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface)
        Text(text = amount, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isDiscount) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateInvoiceDialog(
    students: List<Student>,
    preselectedStudentId: Long? = null,
    onDismiss: () -> Unit,
    onGenerate: (studentId: Long, monthYear: String, tuition: Double, admission: Double, exam: Double, discount: Double, dueDate: String) -> Unit
) {
    val context = LocalContext.current
    val initialIndex = if (preselectedStudentId != null) {
        students.indexOfFirst { it.id == preselectedStudentId }.coerceAtLeast(0)
    } else 0

    var selectedIndex by remember { mutableIntStateOf(initialIndex) }
    var studentDropdownExpanded by remember { mutableStateOf(false) }

    val currentStudent = students.getOrNull(selectedIndex)

    var monthYear by remember { mutableStateOf("November 2025") }
    var tuitionFeeStr by remember { mutableStateOf(currentStudent?.monthlyFee?.toInt()?.toString() ?: "2500") }
    var admissionFeeStr by remember { mutableStateOf("0") }
    var examFeeStr by remember { mutableStateOf("0") }
    var discountStr by remember { mutableStateOf(currentStudent?.discount?.toInt()?.toString() ?: "0") }
    var dueDate by remember { mutableStateOf("2025-11-15") }

    // Update defaults when student changes
    LaunchedEffect(selectedIndex) {
        currentStudent?.let { st ->
            tuitionFeeStr = st.monthlyFee.toInt().toString()
            discountStr = st.discount.toInt().toString()
        }
    }

    val tuition = tuitionFeeStr.toDoubleOrNull() ?: 0.0
    val admission = admissionFeeStr.toDoubleOrNull() ?: 0.0
    val exam = examFeeStr.toDoubleOrNull() ?: 0.0
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val totalCalculated = (tuition + admission + exam - discount).coerceAtLeast(0.0)

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Generate Fee Invoice",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                )

                // Select Student
                ExposedDropdownMenuBox(
                    expanded = studentDropdownExpanded,
                    onExpandedChange = { if (preselectedStudentId == null) studentDropdownExpanded = !studentDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${currentStudent?.name ?: ""} (${currentStudent?.rollNo ?: ""})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Student") },
                        trailingIcon = {
                            if (preselectedStudentId == null) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = studentDropdownExpanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp),
                        enabled = preselectedStudentId == null
                    )
                    ExposedDropdownMenu(
                        expanded = studentDropdownExpanded,
                        onDismissRequest = { studentDropdownExpanded = false },
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        students.forEachIndexed { idx, st ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(st.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                        Text("Roll: ${st.rollNo} • ${st.department} (${st.section})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                onClick = {
                                    selectedIndex = idx
                                    studentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Month Year
                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Billing Month & Year (e.g., November 2025)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Tuition & Due Date
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tuitionFeeStr,
                        onValueChange = { tuitionFeeStr = it },
                        label = { Text("Tuition Fee (Rs)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Additional Fees
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = admissionFeeStr,
                        onValueChange = { admissionFeeStr = it },
                        label = { Text("Admission (Rs)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = examFeeStr,
                        onValueChange = { examFeeStr = it },
                        label = { Text("Exam Fee (Rs)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Discount
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it },
                    label = { Text("Fee Discount / Concession (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Calculated total banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldPrimaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total Payable Amount:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Rs. ${totalCalculated.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (currentStudent == null) {
                                Toast.makeText(context, "Please select a student", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onGenerate(currentStudent.id, monthYear, tuition, admission, exam, discount, dueDate)
                            Toast.makeText(context, "Invoice generated successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Create Invoice")
                    }
                }
            }
        }
    }
}

@Composable
fun BulkInvoiceDialog(
    activeStudentCount: Int,
    onDismiss: () -> Unit,
    onConfirmBulk: (monthYear: String, dueDate: String) -> Unit
) {
    var monthYear by remember { mutableStateOf("November 2025") }
    var dueDate by remember { mutableStateOf("2025-11-15") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Bulk Monthly Invoicing",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                )
                Text(
                    text = "Automatically generate monthly invoices for all $activeStudentCount active students based on their enrolled monthly fee.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                OutlinedTextField(
                    value = monthYear,
                    onValueChange = { monthYear = it },
                    label = { Text("Billing Month & Year") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirmBulk(monthYear, dueDate)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Generate for All ($activeStudentCount)")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDialog(
    voucher: FeeVoucher,
    studentName: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double, mode: String, notes: String) -> Unit
) {
    val due = voucher.totalPayable - voucher.paidAmount
    var amountStr by remember { mutableStateOf(due.toInt().toString()) }
    var selectedMode by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("Fee payment") }

    val modes = listOf("Cash", "EasyPaisa", "JazzCash", "Bank Transfer")

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
                    text = "Collect Fee Payment",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )

                Text(text = "Student: $studentName", fontWeight = FontWeight.Bold)
                Text(text = "Outstanding Due: Rs. ${due.toInt()}", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount to Pay (Rs)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(text = "Payment Mode", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    modes.forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(text = mode, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Remarks / Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                onConfirmPayment(amt, selectedMode, notes)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("Confirm Payment")
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptDialog(
    payment: PaymentRecord,
    studentName: String,
    rollNo: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Official Fee Receipt",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                )
                Text(text = "Jamia Darul Uloom Al-Islamia", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReceiptRow(label = "Receipt No:", value = payment.receiptNo)
                    ReceiptRow(label = "Date:", value = payment.paymentDate)
                    ReceiptRow(label = "Student:", value = studentName)
                    ReceiptRow(label = "Roll No:", value = rollNo)
                    ReceiptRow(label = "Amount Paid:", value = "Rs. ${payment.amountPaid.toInt()}")
                    ReceiptRow(label = "Payment Mode:", value = payment.paymentMode)
                    ReceiptRow(label = "Receiver:", value = payment.receiverName)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Done")
                }
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
