package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.ui.components.Madrassa3dBadge
import com.example.ui.components.Madrassa3dCard
import com.example.ui.components.Madrassa3dMetricTile
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.t

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: MadrassaViewModel,
    onStudentClick: (Student) -> Unit,
    onAddStudentClick: () -> Unit
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val students by viewModel.filteredStudents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDept by viewModel.selectedDepartmentFilter.collectAsState()

    val totalCount = allStudents.size
    val activeCount = allStudents.count { it.status.equals("ACTIVE", ignoreCase = true) }
    val hifzCount = allStudents.count { it.department.contains("Hifz", ignoreCase = true) }

    val departments = listOf("ALL", "Hifz", "Nazra", "Qaida", "Qirat & Tajweed")
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 1 } }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = showScrollToTop,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        containerColor = Color(0xFF0F5A3E),
                        contentColor = Color(0xFFFFF0C2),
                        shape = CircleShape,
                        modifier = Modifier.border(1.2.dp, Color(0xFFE5C07B), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Scroll to top",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                FloatingActionButton(
                    onClick = onAddStudentClick,
                    containerColor = Color(0xFFE5C07B),
                    contentColor = Color(0xFF042418),
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = t("add_student"))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sticky Search & Filter Bar Section
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text(text = t("search_hint"), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFE5C07B)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color(0xFFE5C07B),
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // 3. Department / Class Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(departments) { dept ->
                    val isSelected = selectedDept.equals(dept, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedDepartmentFilter.value = dept },
                        label = {
                            Text(
                                text = if (dept == "ALL") "All Classes" else dept,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.5.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0F5A3E),
                            selectedLabelColor = Color(0xFFFFF0C2),
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color(0xFFE5C07B),
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            borderWidth = 1.2.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                }
            }

            // Results count banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${students.size} Students found",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            // Students List
            if (students.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = t("no_records"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Collapsible Top 3D Metric Tiles Row
                    item(key = "collapsible_metrics") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Madrassa3dMetricTile(
                                title = "Total Enrolled",
                                value = "$totalCount",
                                icon = Icons.Default.Groups,
                                modifier = Modifier.weight(1f),
                                accentColor = Color(0xFFE5C07B)
                            )
                            Madrassa3dMetricTile(
                                title = "Active (حاضر)",
                                value = "$activeCount",
                                icon = Icons.Default.VerifiedUser,
                                modifier = Modifier.weight(1f),
                                accentColor = Color(0xFF81C784)
                            )
                            Madrassa3dMetricTile(
                                title = "Hifz (حفاظ)",
                                value = "$hifzCount",
                                icon = Icons.Default.MenuBook,
                                modifier = Modifier.weight(1f),
                                accentColor = Color(0xFFFFF0C2)
                            )
                        }
                    }

                    items(students, key = { it.id }) { student ->
                        StudentCardItem(
                            student = student,
                            onClick = { onStudentClick(student) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    onClick: () -> Unit
) {
    Madrassa3dCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 5.dp,
        shape = RoundedCornerShape(16.dp),
        gradientColors = listOf(
            Color(0xFF0F5338),
            Color(0xFF083221),
            Color(0xFF031A10)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student Avatar inside 3D Gold-Rimmed Orb
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            listOf(Color(0xFF1E6C50), Color(0xFF062B1D))
                        )
                    )
                    .border(1.5.dp, Color(0xFFE5C07B).copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (student.photoUri.isNotBlank()) {
                    AsyncImage(
                        model = com.example.util.ImageStorageHelper.toImageModel(student.photoUri),
                        contentDescription = student.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = student.name.firstOrNull()?.uppercase() ?: "S",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFF0C2),
                            fontSize = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Student Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        ),
                        softWrap = true,
                        maxLines = 2,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val statusColor = if (student.status.equals("ACTIVE", ignoreCase = true)) Color(0xFF81C784) else Color(0xFFE57373)
                    Madrassa3dBadge(
                        text = student.status,
                        containerColor = statusColor.copy(alpha = 0.18f),
                        contentColor = statusColor,
                        borderColor = statusColor.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Madrassa3dBadge(
                        text = "#${student.rollNo}",
                        containerColor = Color(0xFFE5C07B).copy(alpha = 0.22f),
                        contentColor = Color(0xFFFFF0C2),
                        borderColor = Color(0xFFE5C07B).copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${student.campus}  •  ${student.paray}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE5C07B),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "ولدیت: ${student.fatherName}  |  Class: ${student.department} (${student.section})",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 11.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
