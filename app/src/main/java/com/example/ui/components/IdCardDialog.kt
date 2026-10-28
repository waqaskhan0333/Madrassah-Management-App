package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.MadrassaProfile
import com.example.data.model.Student
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import com.example.util.ImageStorageHelper

@Composable
fun IdCardDialog(
    student: Student,
    profile: MadrassaProfile,
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Student Official ID Card",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        )
                        Text(text = "طالب علم کا شناختی کارڈ", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // The Physical ID Card Container (Preview)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Top Emerald Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF042418))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (profile.logoUri.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageStorageHelper.toImageModel(profile.logoUri),
                                        contentDescription = "Logo",
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = profile.nameUr,
                                        color = Color(0xFFE5C07B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Gold divider line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(Color(0xFFD4AF37))
                        )

                        // Middle Body: Photo & Particulars
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Student Photo
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.5.dp, Color(0xFF0A4B2D), RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8F5E9)),
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
                                        text = student.name.firstOrNull()?.uppercase() ?: "S",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Student Particulars
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF042418)
                                )
                                Text(
                                    text = "Roll No: ${student.rollNo}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF0A4B2D)
                                )
                                Text(text = "Father: ${student.fatherName}", fontSize = 11.sp, color = Color.DarkGray)
                                Text(text = "Class: ${student.department} (${student.section})", fontSize = 11.sp, color = Color.DarkGray)
                                Text(text = "Para: ${student.paray}  •  ${student.campus}", fontSize = 10.5.sp, color = Color.DarkGray)
                                Text(text = "Phone: ${student.guardianPhone}", fontSize = 10.5.sp, color = Color.DarkGray)
                            }
                        }

                        // Bottom Footer Band
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F8F5))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Session: ${profile.sessionYear}",
                                fontSize = 9.5.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Authorized Seal & Sign",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A4B2D)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Close")
                    }

                    Button(
                        onClick = {
                            viewModel.generateIdCardsPdf(context)
                            Toast.makeText(context, "Exporting printable ID cards...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Print Cards PDF")
                    }
                }
            }
        }
    }
}
