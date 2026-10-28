package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FeeVoucher
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    viewModel: MadrassaViewModel,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Invoices & Vouchers, 1: Defaulters, 2: Payments History
    var selectedVoucherForPayment by remember { mutableStateOf<FeeVoucher?>(null) }
    var selectedReceiptToView by remember { mutableStateOf<PaymentRecord?>(null) }
    var selectedVoucherForSlip by remember { mutableStateOf<FeeVoucher?>(null) }
    var voucherToDelete by remember { mutableStateOf<FeeVoucher?>(null) }
    var selectedStudentForAccountDetails by remember { mutableStateOf<Student?>(null) }

    var showGenerateInvoiceDialog by remember { mutableStateOf(false) }
    var showBulkInvoiceDialog by remember { mutableStateOf(false) }

    var preselectedStudentForInvoice by remember { mutableStateOf<Long?>(null) }
    var accountSearchQuery by remember { mutableStateOf("") }
    var accountStatusFilter by remember { mutableStateOf("ALL") }
    var accountDeptFilter by remember { mutableStateOf("All Classes") }
    var accountCampusFilter by remember { mutableStateOf("All Campuses") }

    var invoiceSearchQuery by remember { mutableStateOf("") }
    var invoiceStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "PAID", "UNPAID", "PARTIAL"

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 2 }
    }

    val vouchers by viewModel.vouchers.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val studentLookup by viewModel.studentLookupMap.collectAsState()
    val profile by viewModel.profile.collectAsState()

    val totalCollected = vouchers.sumOf { it.paidAmount }
    val totalPending = vouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }
    val totalInvoiced = vouchers.sumOf { it.totalPayable }
    val defaulters = vouchers.filter { it.status != "PAID" }

    val availableDepartments = remember(students) {
        listOf("All Classes") + students.map { it.department.trim() }.filter { it.isNotBlank() }.distinct()
    }
    val availableCampuses = remember(students) {
        val camps = students.map { it.campus.trim() }.filter { it.isNotBlank() }.distinct()
        if (camps.size > 1) listOf("All Campuses") + camps else emptyList()
    }

    if (onBack != null) {
        androidx.activity.compose.BackHandler {
            onBack()
        }
    }

    // Filtered student accounts
    val filteredStudentsForAccount = remember(students, vouchers, accountSearchQuery, accountDeptFilter, accountCampusFilter, accountStatusFilter) {
        students.filter { st ->
            val matchesQuery = accountSearchQuery.isBlank() ||
                    st.name.contains(accountSearchQuery, ignoreCase = true) ||
                    st.rollNo.contains(accountSearchQuery, ignoreCase = true) ||
                    st.department.contains(accountSearchQuery, ignoreCase = true) ||
                    st.section.contains(accountSearchQuery, ignoreCase = true) ||
                    st.fatherName.contains(accountSearchQuery, ignoreCase = true) ||
                    st.campus.contains(accountSearchQuery, ignoreCase = true)

            val matchesDept = accountDeptFilter == "All Classes" || st.department.equals(accountDeptFilter, ignoreCase = true)
            val matchesCampus = accountCampusFilter == "All Campuses" || st.campus.equals(accountCampusFilter, ignoreCase = true)

            val stVouchers = vouchers.filter { it.studentId == st.id }
            val due = stVouchers.filter { it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }

            val matchesFilter = when (accountStatusFilter) {
                "HAS_DUES" -> due > 0
                "PAID_IN_FULL" -> stVouchers.isNotEmpty() && due <= 0
                "UNBILLED" -> stVouchers.isEmpty()
                else -> true
            }
            matchesQuery && matchesDept && matchesCampus && matchesFilter
        }
    }

    // Filtered invoices
    val filteredVouchers = vouchers.filter { v ->
        val student = studentLookup[v.studentId] ?: students.find { it.id == v.studentId }
        val matchesSearch = invoiceSearchQuery.isBlank() ||
                (student?.name?.contains(invoiceSearchQuery, ignoreCase = true) == true) ||
                (student?.rollNo?.contains(invoiceSearchQuery, ignoreCase = true) == true) ||
                (student?.fatherName?.contains(invoiceSearchQuery, ignoreCase = true) == true) ||
                v.voucherNo.contains(invoiceSearchQuery, ignoreCase = true) ||
                v.monthYear.contains(invoiceSearchQuery, ignoreCase = true)

        val matchesStatus = invoiceStatusFilter == "ALL" || v.status.equals(invoiceStatusFilter, ignoreCase = true)

        matchesSearch && matchesStatus
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text(text = t("fees"), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = EmeraldPrimary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val firstWithDues = students.firstOrNull { st ->
                                val due = vouchers.filter { it.studentId == st.id && it.status != "PAID" }.sumOf { it.totalPayable - it.paidAmount }
                                due > 0
                            } ?: students.firstOrNull()
                            if (firstWithDues != null) {
                                selectedStudentForAccountDetails = firstWithDues
                            } else {
                                showGenerateInvoiceDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "+ RECEIVED (CASH IN)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            val firstStudent = students.firstOrNull()
                            if (firstStudent != null) {
                                selectedStudentForAccountDetails = firstStudent
                            } else {
                                Toast.makeText(context, "No accounts to record Cash Out", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = "- PAID (CASH OUT)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF6F8F7))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                // 1. Summary Cards Header
                item {
                    Madrassa3dCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp),
                        elevation = 6.dp,
                        gradientColors = listOf(Color(0xFF0F5A3E), Color(0xFF083824), Color(0xFF031E13))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Fee Invoices & Accounts",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "Islamic Financial Management System",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                }
                                Madrassa3dBadge(
                                    text = "Invoiced: Rs. ${totalInvoiced.toInt()}",
                                    containerColor = Color(0xFFE5C07B).copy(alpha = 0.25f),
                                    contentColor = Color(0xFFFFF0C2),
                                    borderColor = Color(0xFFE5C07B).copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Madrassa3dMetricTile(
                                    title = "Collected (Paid)",
                                    value = "Rs. ${totalCollected.toInt()}",
                                    icon = Icons.Default.CheckCircle,
                                    modifier = Modifier.weight(1f),
                                    accentColor = Color(0xFF81C784)
                                )

                                Madrassa3dMetricTile(
                                    title = "Pending (بقیہ)",
                                    value = "Rs. ${totalPending.toInt()}",
                                    icon = Icons.Default.PendingActions,
                                    modifier = Modifier.weight(1f),
                                    accentColor = Color(0xFFFFB4AB)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons: + Generate Invoice & Bulk Invoicing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showGenerateInvoiceDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE5C07B),
                                        contentColor = Color(0xFF042418)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "+ Generate Invoice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showBulkInvoiceDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFFE5C07B).copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFF0C2)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 7.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Bulk Invoicing", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // 2. TabRow
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = EmeraldPrimary
                        ) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Accounts (${students.size})", fontSize = 11.5.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) })
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Invoices (${vouchers.size})", fontSize = 11.5.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) })
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Defaulters (${defaulters.size})", fontSize = 11.5.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) })
                            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Payments (${payments.size})", fontSize = 11.5.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) })
                        }
                    }
                }

                // 3. Search & Filters Bar
                item {
                    if (selectedTab == 0) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            OutlinedTextField(
                                value = accountSearchQuery,
                                onValueChange = { accountSearchQuery = it },
                                placeholder = { Text("Search student name, roll number, father...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (accountSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { accountSearchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Department Filter Chips
                            if (availableDepartments.size > 1) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(availableDepartments) { dept ->
                                        FilterChip(
                                            selected = accountDeptFilter == dept,
                                            onClick = { accountDeptFilter = dept },
                                            label = { Text(text = dept, fontSize = 11.sp) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // Campus filter chips if multiple campuses exist
                            if (availableCampuses.size > 1) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(availableCampuses) { camp ->
                                        FilterChip(
                                            selected = accountCampusFilter == camp,
                                            onClick = { accountCampusFilter = camp },
                                            label = { Text(text = camp, fontSize = 11.sp) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            // Fee Status Filter Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val filterOptions = listOf(
                                    "ALL" to "All Students (${students.size})",
                                    "HAS_DUES" to "Pending Dues",
                                    "PAID_IN_FULL" to "Paid in Full",
                                    "UNBILLED" to "Unbilled"
                                )
                                items(filterOptions) { (key, label) ->
                                    FilterChip(
                                        selected = accountStatusFilter == key,
                                        onClick = { accountStatusFilter = key },
                                        label = { Text(text = label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Showing ${filteredStudentsForAccount.size} of ${students.size} students",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (selectedTab == 1) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            OutlinedTextField(
                                value = invoiceSearchQuery,
                                onValueChange = { invoiceSearchQuery = it },
                                placeholder = { Text("Search by student, roll no, or invoice #", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (invoiceSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { invoiceSearchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val filterOptions = listOf("ALL", "UNPAID", "PARTIAL", "PAID")
                                items(filterOptions) { opt ->
                                    FilterChip(
                                        selected = invoiceStatusFilter == opt,
                                        onClick = { invoiceStatusFilter = opt },
                                        label = { Text(text = if (opt == "ALL") "All Invoices (${vouchers.size})" else opt, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Content items per tab
                when (selectedTab) {
                    0 -> {
                        // Student Accounts List with complete scroll feature showing ALL student names
                        if (filteredStudentsForAccount.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "No student accounts found matching filter.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(filteredStudentsForAccount, key = { "account_${it.id}" }) { student ->
                                val stVouchers = vouchers.filter { it.studentId == student.id }
                                val totalInv = stVouchers.sumOf { it.totalPayable }
                                val totalPaid = stVouchers.sumOf { it.paidAmount }
                                val due = (totalInv - totalPaid).coerceAtLeast(0.0)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp)
                                        .clickable { selectedStudentForAccountDetails = student },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        // Header: Student Avatar & Full Name & Status
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(CircleShape)
                                                        .background(EmeraldPrimaryContainer),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (student.photoUri.isNotBlank()) {
                                                        coil.compose.AsyncImage(
                                                            model = com.example.util.ImageStorageHelper.toImageModel(student.photoUri),
                                                            contentDescription = student.name,
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = student.name.trim().take(1),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 17.sp,
                                                            color = EmeraldPrimary
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = student.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.5.sp,
                                                        lineHeight = 22.sp,
                                                        softWrap = true,
                                                        maxLines = 2,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = EmeraldPrimaryContainer.copy(alpha = 0.7f)
                                                        ) {
                                                            Text(
                                                                text = "Roll: ${student.rollNo}",
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = EmeraldPrimary,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = "${student.department} (${student.section})" + (if (student.fatherName.isNotBlank()) " • ولد: ${student.fatherName}" else ""),
                                                            fontSize = 11.5.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when {
                                                    due > 0 -> Color(0xFFFFEBEE)
                                                    stVouchers.isNotEmpty() -> Color(0xFFE8F5E9)
                                                    else -> Color(0xFFEDE7F6)
                                                }
                                            ) {
                                                Text(
                                                    text = when {
                                                        due > 0 -> "Due: Rs. ${due.toInt()}"
                                                        stVouchers.isNotEmpty() -> "Paid in Full"
                                                        else -> "Unbilled"
                                                    },
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when {
                                                        due > 0 -> Color(0xFFC62828)
                                                        stVouchers.isNotEmpty() -> Color(0xFF2E7D32)
                                                        else -> Color(0xFF512DA8)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Financial Summary Grid
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFF7FBF9))
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(text = "Monthly Fee", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "Rs. ${student.monthlyFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Column {
                                                Text(text = "Invoiced", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "Rs. ${totalInv.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                            Column {
                                                Text(text = "Collected", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(text = "Rs. ${totalPaid.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(text = "Balance Due", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = "Rs. ${due.toInt()}",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (due > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Action buttons row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        preselectedStudentForInvoice = student.id
                                                        showGenerateInvoiceDialog = true
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(text = "+ Invoice", fontSize = 11.5.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = { selectedStudentForAccountDetails = student },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(text = "Khata", fontSize = 11.sp)
                                                }

                                                if (stVouchers.isNotEmpty()) {
                                                    TextButton(
                                                        onClick = {
                                                            invoiceSearchQuery = student.name
                                                            selectedTab = 1
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Text(text = "View Invoices (${stVouchers.size})", fontSize = 11.sp, color = EmeraldPrimary)
                                                    }
                                                }
                                            }

                                            if (due > 0) {
                                                val pendingVoucher = stVouchers.firstOrNull { it.status != "PAID" }
                                                if (pendingVoucher != null) {
                                                    Button(
                                                        onClick = { selectedVoucherForPayment = pendingVoucher },
                                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(32.dp)
                                                    ) {
                                                        Text(text = "Collect", fontSize = 11.5.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Invoices List
                        if (filteredVouchers.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "No invoices found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            items(filteredVouchers, key = { "invoice_${it.id}" }) { voucher ->
                                val student = studentLookup[voucher.studentId] ?: students.find { it.id == voucher.studentId }
                                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                                    InvoiceCardItem(
                                        voucher = voucher,
                                        student = student,
                                        onViewSlip = { selectedVoucherForSlip = voucher },
                                        onPayClick = { selectedVoucherForPayment = voucher },
                                        onDeleteClick = { voucherToDelete = voucher }
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        // Defaulters List
                        if (defaulters.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "Alhamdulillah! No pending fee defaulters.", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            items(defaulters, key = { "defaulter_${it.id}" }) { voucher ->
                                val student = studentLookup[voucher.studentId] ?: students.find { it.id == voucher.studentId }
                                val due = voucher.totalPayable - voucher.paidAmount
                                val studentDisplayName = student?.name ?: "Student #${voucher.studentId}"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFEBEE)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (!student?.photoUri.isNullOrBlank()) {
                                                        coil.compose.AsyncImage(
                                                            model = com.example.util.ImageStorageHelper.toImageModel(student!!.photoUri),
                                                            contentDescription = studentDisplayName,
                                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = studentDisplayName.trim().take(1),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 17.sp,
                                                            color = Color(0xFFC62828)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = studentDisplayName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        softWrap = true,
                                                        maxLines = 2
                                                    )
                                                    Text(
                                                        text = "Roll: ${student?.rollNo ?: "—"}" + (if (!student?.fatherName.isNullOrBlank()) " • ولد: ${student?.fatherName}" else "") + " • Due Date: ${voucher.dueDate}",
                                                        fontSize = 11.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            StatusChip(status = voucher.status)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Outstanding Due: Rs. ${due.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD32F2F)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    student?.guardianPhone?.let { phone ->
                                                        val clean = phone.replace("-", "").replace(" ", "")
                                                        val formatted = if (clean.startsWith("0")) "+92" + clean.substring(1) else clean
                                                        val msg = "Assalam-o-Alaikum, this is a fee reminder from ${profile.nameEn} for ${student.name} (Roll: ${student.rollNo}). Pending dues for ${voucher.monthYear}: Rs. ${due.toInt()} (Due: ${voucher.dueDate})."
                                                        val uri = Uri.parse("https://wa.me/$formatted?text=${Uri.encode(msg)}")
                                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) } catch (e: Exception) {
                                                            Toast.makeText(context, "WhatsApp not available", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = "WhatsApp Reminder", fontSize = 12.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { selectedVoucherForPayment = voucher },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(text = "Collect", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Payments History
                        if (payments.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "No payment records found.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                    }
                                }
                            }
                        } else {
                            items(payments, key = { "payment_${it.id}" }) { payment ->
                                val student = studentLookup[payment.studentId] ?: students.find { it.id == payment.studentId }
                                val studentDisplayName = student?.name ?: "Student #${payment.studentId}"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Avatar
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(EmeraldPrimaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!student?.photoUri.isNullOrBlank()) {
                                                    coil.compose.AsyncImage(
                                                        model = com.example.util.ImageStorageHelper.toImageModel(student!!.photoUri),
                                                        contentDescription = studentDisplayName,
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                    )
                                                } else {
                                                    Text(
                                                        text = studentDisplayName.trim().take(1),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = EmeraldPrimary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = studentDisplayName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    softWrap = true,
                                                    maxLines = 2
                                                )
                                                Text(
                                                    text = (if (!student?.rollNo.isNullOrBlank()) "Roll: ${student?.rollNo} • " else "") + "Receipt: ${payment.receiptNo}  |  ${payment.paymentDate}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "Mode: ${payment.paymentMode}",
                                                    fontSize = 11.sp,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(text = "Rs. ${payment.amountPaid.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 16.sp)
                                            TextButton(onClick = { selectedReceiptToView = payment }) {
                                                Text(text = "View Receipt", fontSize = 11.sp, color = EmeraldPrimary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Quick Scroll To Top Button
            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 90.dp),
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                }
            }
        }
    }

    // Generate Invoice Dialog
    if (showGenerateInvoiceDialog) {
        GenerateInvoiceDialog(
            students = students,
            preselectedStudentId = preselectedStudentForInvoice,
            onDismiss = {
                showGenerateInvoiceDialog = false
                preselectedStudentForInvoice = null
            },
            onGenerate = { studentId, monthYear, tuition, admission, exam, discount, dueDate ->
                viewModel.generateInvoice(studentId, monthYear, tuition, admission, exam, discount, dueDate) {
                    showGenerateInvoiceDialog = false
                    preselectedStudentForInvoice = null
                }
            }
        )
    }

    // Bulk Invoice Dialog
    if (showBulkInvoiceDialog) {
        val activeCount = students.count { it.status == "ACTIVE" }
        BulkInvoiceDialog(
            activeStudentCount = activeCount,
            onDismiss = { showBulkInvoiceDialog = false },
            onConfirmBulk = { monthYear, dueDate ->
                viewModel.bulkGenerateMonthlyInvoices(monthYear, dueDate) { count ->
                    Toast.makeText(context, "$count new invoices generated for $monthYear!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Invoice Slip Dialog Modal
    selectedVoucherForSlip?.let { voucher ->
        val student = studentLookup[voucher.studentId] ?: students.find { it.id == voucher.studentId }
        InvoiceSlipDialog(
            voucher = voucher,
            student = student,
            profile = profile,
            onDismiss = { selectedVoucherForSlip = null },
            onPayClick = {
                selectedVoucherForPayment = voucher
            }
        )
    }

    // Payment Collection Dialog
    selectedVoucherForPayment?.let { voucher: FeeVoucher ->
        val student = studentLookup[voucher.studentId] ?: students.find { it.id == voucher.studentId }
        PaymentDialog(
            voucher = voucher,
            studentName = student?.name ?: "Student #${voucher.studentId}",
            onDismiss = { selectedVoucherForPayment = null },
            onConfirmPayment = { amount: Double, mode: String, notes: String ->
                viewModel.collectFeePayment(voucher, amount, mode, notes) { payment: PaymentRecord ->
                    selectedVoucherForPayment = null
                    selectedReceiptToView = payment
                    Toast.makeText(context, "Payment collected successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Receipt Modal Dialog
    selectedReceiptToView?.let { payment: PaymentRecord ->
        val student = studentLookup[payment.studentId] ?: students.find { it.id == payment.studentId }
        ReceiptDialog(
            payment = payment,
            studentName = student?.name ?: "Student #${payment.studentId}",
            rollNo = student?.rollNo ?: "",
            onDismiss = { selectedReceiptToView = null }
        )
    }

    // Delete Invoice Confirmation Dialog
    voucherToDelete?.let { voucher ->
        AlertDialog(
            onDismissRequest = { voucherToDelete = null },
            title = { Text("Delete Invoice?") },
            text = { Text("Are you sure you want to delete invoice ${voucher.voucherNo} for ${voucher.monthYear}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVoucher(voucher.id)
                        voucherToDelete = null
                        Toast.makeText(context, "Invoice deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { voucherToDelete = null }) { Text("Cancel") }
            }
        )
    }

    selectedStudentForAccountDetails?.let { st ->
        AccountLedgerDetailsDialog(
            student = st,
            profile = profile,
            viewModel = viewModel,
            onDismiss = { selectedStudentForAccountDetails = null }
        )
    }
}

@Composable
fun InvoiceCardItem(
    voucher: FeeVoucher,
    student: Student?,
    onViewSlip: () -> Unit,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val due = voucher.totalPayable - voucher.paidAmount

    Madrassa3dCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewSlip,
        elevation = 5.dp,
        shape = RoundedCornerShape(16.dp),
        gradientColors = listOf(
            Color(0xFF0F5338),
            Color(0xFF083221),
            Color(0xFF031A10)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Student avatar, name, details, Status Chip
            val studentDisplayName = student?.name ?: "Student #${voucher.studentId}"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Student Avatar inside 3D Gold-Rimmed Orb
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(
                                    listOf(Color(0xFF1E6C50), Color(0xFF062B1D))
                                )
                            )
                            .border(1.5.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!student?.photoUri.isNullOrBlank()) {
                            coil.compose.AsyncImage(
                                model = com.example.util.ImageStorageHelper.toImageModel(student!!.photoUri),
                                contentDescription = studentDisplayName,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = studentDisplayName.trim().take(1),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFFFFF0C2)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = studentDisplayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            color = Color.White,
                            softWrap = true,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (!student?.rollNo.isNullOrBlank()) {
                                Madrassa3dBadge(
                                    text = "Roll: ${student!!.rollNo}",
                                    containerColor = Color(0xFFE5C07B).copy(alpha = 0.22f),
                                    contentColor = Color(0xFFFFF0C2),
                                    borderColor = Color(0xFFE5C07B).copy(alpha = 0.6f)
                                )
                            }
                            if (!student?.department.isNullOrBlank()) {
                                Text(
                                    text = "${student!!.department} (${student.section})",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE5C07B).copy(alpha = 0.9f)
                                )
                            }
                        }
                        Text(
                            text = "Inv: ${voucher.voucherNo} • ${voucher.monthYear}" + (if (!student?.fatherName.isNullOrBlank()) " • ولد: ${student!!.fatherName}" else ""),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                }
                val statusBg = when (voucher.status) {
                    "PAID" -> Color(0xFF81C784)
                    "PARTIAL" -> Color(0xFFFFB74D)
                    else -> Color(0xFFE57373)
                }
                Madrassa3dBadge(
                    text = voucher.status,
                    containerColor = statusBg.copy(alpha = 0.2f),
                    contentColor = statusBg,
                    borderColor = statusBg.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Amounts Row with 3D Depth Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Payable: Rs. ${voucher.totalPayable.toInt()}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "Paid: Rs. ${voucher.paidAmount.toInt()}",
                        fontSize = 12.5.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Balance: Rs. ${due.toInt()}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (due > 0) Color(0xFFFF8A80) else Color(0xFF81C784)
                    )
                    Text(
                        text = "Due: ${voucher.dueDate}",
                        fontSize = 10.5.sp,
                        color = Color(0xFFE5C07B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewSlip,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color(0xFFE5C07B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "View Invoice Slip", fontSize = 12.sp, color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (voucher.status != "PAID") {
                        Button(
                            onClick = onPayClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE5C07B),
                                contentColor = Color(0xFF042418)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = "Collect", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Invoice",
                            tint = Color(0xFFFF8A80).copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
