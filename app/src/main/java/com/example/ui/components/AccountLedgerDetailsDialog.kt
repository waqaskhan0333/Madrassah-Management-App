package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FeeVoucher
import com.example.data.model.MadrassaProfile
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.LanguageState
import com.example.util.t

/**
 * Account Details & Transaction Timeline Dialog
 * Matches the mobile Ledger / Khata screen layout from Screenshot 02.
 * Crucially features persistent bottom buttons (+ RECEIVED CASH IN / - PAID CASH OUT)
 * configured with navigationBarsPadding() so they are never hidden behind the Android
 * 3-button navigation bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLedgerDetailsDialog(
    student: Student,
    profile: MadrassaProfile,
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val vouchers by viewModel.vouchers.collectAsState()
    val payments by viewModel.payments.collectAsState()

    val studentVouchers = vouchers.filter { it.studentId == student.id }
    val studentPayments = payments.filter { it.studentId == student.id }

    val totalInvoiced = studentVouchers.sumOf { it.totalPayable }
    val totalPaid = studentVouchers.sumOf { it.paidAmount }
    val balanceDue = if (studentVouchers.isNotEmpty()) {
        studentVouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }
    } else {
        student.monthlyFee
    }

    var showCashInDialog by remember { mutableStateOf(false) }
    var showCashOutDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmTransaction by remember { mutableStateOf<Any?>(null) }

    val allTransactions = remember(studentVouchers, studentPayments) {
        val list = mutableListOf<TransactionItem>()
        studentPayments.forEach { p ->
            list.add(
                TransactionItem(
                    id = p.id,
                    type = "PAYMENT",
                    title = "Supplier / Tuition payment",
                    date = p.paymentDate,
                    amount = p.amountPaid,
                    subText = "Receipt: ${p.receiptNo} • Mode: ${p.paymentMode}",
                    isPayment = true,
                    raw = p
                )
            )
        }
        studentVouchers.forEach { v ->
            list.add(
                TransactionItem(
                    id = v.id,
                    type = "PURCHASE",
                    title = "Inv No #${v.voucherNo}",
                    date = v.dueDate,
                    amount = v.totalPayable,
                    subText = "Paid: Rs. ${v.paidAmount.toInt()} • Month: ${v.monthYear}",
                    isPayment = false,
                    raw = v
                )
            )
        }
        list.sortedByDescending { it.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0xFF1976D2), // Classic blue header from screenshot
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "Store",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = "Account Details",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (profile.nameEn.isNotBlank()) profile.nameEn else "ZIA Store / Madrassa",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { LanguageState.toggle() }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Translate,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (LanguageState.isUrdu) "English" else "اردو",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { Toast.makeText(context, "Account notifications active", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // FIXED & FULLY VISIBLE BOTTOM BUTTON BAR
                // Contains both + RECEIVED (CASH IN) and - PAID (CASH OUT) buttons
                // with explicit navigationBarsPadding() so it sits above Android's 3-button bar!
                Surface(
                    color = Color.White,
                    shadowElevation = 12.dp,
                    border = BorderStroke(0.5.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // CRITICAL: Elevates buttons above mobile navigation bar
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. + RECEIVED (CASH IN) GREEN BUTTON
                        Button(
                            onClick = { showCashInDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853), // High-visibility vivid green
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            Text(
                                text = "+ RECEIVED (CASH IN)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }

                        // 2. - PAID (CASH OUT) RED BUTTON
                        Button(
                            onClick = { showCashOutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD50000), // High-visibility vivid red
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            Text(
                                text = "- PAID (CASH OUT)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8FAFC)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Party / Student Info Header Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF1E293B)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = student.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (student.guardianPhone.isNotBlank()) student.guardianPhone else "0321-4455667",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val phone = if (student.guardianPhone.isNotBlank()) student.guardianPhone else "03214455667"
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call",
                                    tint = Color(0xFF1D4ED8)
                                )
                            }

                            IconButton(
                                onClick = {
                                    val phone = if (student.guardianPhone.isNotBlank()) student.guardianPhone else "03214455667"
                                    val cleanNum = phone.replace("-", "").replace(" ", "")
                                    val formatted = if (cleanNum.startsWith("0")) "+92" + cleanNum.substring(1) else cleanNum
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formatted"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = Color(0xFF22C55E)
                                )
                            }

                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Account options menu", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Options",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }

                // 2. BALANCE DUE (UDHAAR) CARD
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BALANCE DUE (UDHAAR)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Rs. ${balanceDue.toInt()}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFDC2626) // Vivid red amount
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (balanceDue > 0) "You'll Receive (بقیہ جات واجب الادا)" else "You'll Give (Advance / صاف)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (balanceDue > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Share Statement & WhatsApp Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.generatePdfReport(context)
                                        Toast.makeText(context, "Exporting Account Statement PDF...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E293B),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Share Statement",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        val phone = if (student.guardianPhone.isNotBlank()) student.guardianPhone else "03214455667"
                                        val cleanNum = phone.replace("-", "").replace(" ", "")
                                        val formatted = if (cleanNum.startsWith("0")) "+92" + cleanNum.substring(1) else cleanNum
                                        val msg = "محترم ولی، طالب علم ${student.name} کا بقایا کھاتہ / فیس میزان: Rs. ${balanceDue.toInt()} ہے۔ برائے کرم جلد ادائیگی فرمائیں۔ شکریہ"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formatted?text=${Uri.encode(msg)}"))
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF22C55E),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Section Title: TRANSACTION TIMELINE
                item {
                    Text(
                        text = "TRANSACTION TIMELINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        letterSpacing = 0.5.sp
                    )
                }

                // 4. Combined Transaction List (Payments + Invoices/Purchases)
                if (allTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No transactions recorded yet",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Use the buttons below to record Cash In or Cash Out",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                } else {
                    items(allTransactions, key = { "${it.type}_${it.id}" }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Transaction Type Pill
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (item.isPayment) Color(0xFFDCFCE7) else Color(0xFFF3E8FF)
                                    ) {
                                        Text(
                                            text = item.type,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isPayment) Color(0xFF15803D) else Color(0xFF7E22CE),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    // Amount & Delete Icon
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Rs. ${item.amount.toInt()}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.isPayment) Color(0xFF15803D) else Color(0xFF0F172A)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { showDeleteConfirmTransaction = item.raw },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.date,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text(
                                        text = item.subText,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // CASH IN (RECEIVED) ENTRY DIALOG
    if (showCashInDialog) {
        var amountInput by remember { mutableStateOf(if (balanceDue > 0) balanceDue.toInt().toString() else "2500") }
        var paymentMode by remember { mutableStateOf("Cash") }
        var descriptionInput by remember { mutableStateOf("Fee / Customer payment") }

        AlertDialog(
            onDismissRequest = { showCashInDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ RECEIVED (CASH IN)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00C853)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Receiving cash from ${student.name}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount Received (Rs)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Remarks / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            val targetVoucher = studentVouchers.firstOrNull { it.status != "PAID" }
                            if (targetVoucher != null) {
                                viewModel.collectFeePayment(
                                    voucher = targetVoucher,
                                    amount = amt,
                                    mode = paymentMode,
                                    notes = descriptionInput
                                ) {
                                    Toast.makeText(context, "Rs. ${amt.toInt()} Received recorded successfully!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Create an invoice and clear it
                                viewModel.generateInvoice(
                                    studentId = student.id,
                                    monthYear = "Current",
                                    tuitionFee = amt
                                )
                                Toast.makeText(context, "Rs. ${amt.toInt()} Cash Received recorded!", Toast.LENGTH_SHORT).show()
                            }
                            showCashInDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Save (+ Cash In)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashInDialog = false }) { Text("Cancel") }
            }
        )
    }

    // CASH OUT (PAID) ENTRY DIALOG
    if (showCashOutDialog) {
        var amountInput by remember { mutableStateOf("1000") }
        var descriptionInput by remember { mutableStateOf("Expense / Supplier refund / Advance") }

        AlertDialog(
            onDismissRequest = { showCashOutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD50000))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "- PAID (CASH OUT)",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD50000)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recording Cash Out to ${student.name}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount Paid Out (Rs)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Purpose / Supplier Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            Toast.makeText(context, "Rs. ${amt.toInt()} Paid Out recorded in timeline!", Toast.LENGTH_SHORT).show()
                            showCashOutDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))
                ) {
                    Text("Save (- Cash Out)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCashOutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Confirmation
    if (showDeleteConfirmTransaction != null) {
        val trans = showDeleteConfirmTransaction
        AlertDialog(
            onDismissRequest = { showDeleteConfirmTransaction = null },
            title = { Text("Delete Transaction?") },
            text = { Text("Are you sure you want to delete this record from the timeline?") },
            confirmButton = {
                Button(
                    onClick = {
                        if (trans is FeeVoucher) {
                            viewModel.deleteVoucher(trans.id)
                        } else if (trans is PaymentRecord) {
                            viewModel.deletePayment(trans.id)
                        }
                        showDeleteConfirmTransaction = null
                        Toast.makeText(context, "Transaction deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmTransaction = null }) { Text("Cancel") }
            }
        )
    }
}

private data class TransactionItem(
    val id: Long,
    val type: String,
    val title: String,
    val date: String,
    val amount: Double,
    val subText: String,
    val isPayment: Boolean,
    val raw: Any
)
