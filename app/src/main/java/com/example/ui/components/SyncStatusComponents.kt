package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthManager
import com.example.data.sync.SyncState
import com.example.data.sync.SyncStatus
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.viewmodel.MadrassaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SyncStatusPill(
    viewModel: MadrassaViewModel,
    modifier: Modifier = Modifier
) {
    val syncState by viewModel.syncState.collectAsState()
    var showDetailsSheet by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val (bgColor, contentColor, icon, label) = when (syncState.status) {
        SyncStatus.SYNCING -> {
            Quad(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.Sync, "Syncing...")
        }
        SyncStatus.SUCCESS -> {
            val timeText = if (syncState.lastSyncTime > 0) {
                val diff = (System.currentTimeMillis() - syncState.lastSyncTime) / 1000
                if (diff < 60) "Synced just now" else "${diff / 60}m ago"
            } else "Synced"
            Quad(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CloudDone, timeText)
        }
        SyncStatus.OFFLINE -> {
            Quad(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.CloudOff, "Offline (Saved)")
        }
        SyncStatus.NOT_AUTHENTICATED -> {
            Quad(Color(0xFFEDE7F6), Color(0xFF512DA8), Icons.Default.CloudQueue, "Cloud Backup")
        }
        SyncStatus.ERROR -> {
            Quad(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.SyncProblem, "Sync Issue")
        }
        SyncStatus.IDLE -> {
            Quad(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Cloud, "Offline + Cloud")
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { showDetailsSheet = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Sync status",
                tint = contentColor,
                modifier = Modifier
                    .size(15.dp)
                    .then(if (syncState.status == SyncStatus.SYNCING) Modifier.rotate(rotation) else Modifier)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }

    if (showDetailsSheet) {
        SyncDetailsDialog(
            viewModel = viewModel,
            onDismiss = { showDetailsSheet = false }
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncDetailsDialog(
    viewModel: MadrassaViewModel,
    onDismiss: () -> Unit
) {
    val syncState by viewModel.syncState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSigningIn by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val lastSyncString = if (syncState.lastSyncTime > 0) sdf.format(Date(syncState.lastSyncTime)) else "Never"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cloud Sync & Offline Database",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Online/Offline status banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncState.isOnline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (syncState.isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (syncState.isOnline) "Internet Connected (Online)" else "Device is Offline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = if (syncState.isOnline) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            Text(
                                text = if (syncState.isOnline)
                                    "Changes automatically sync to Firebase Cloud"
                                else
                                    "All records are safely saved locally on device",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Cloud Account Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Cloud Account", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            if (syncState.currentUserEmail != null) {
                                TextButton(
                                    onClick = {
                                        viewModel.authManager.signOut(coroutineScope)
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Disconnect", fontSize = 11.sp, color = Color(0xFFC62828))
                                }
                            }
                        }

                        if (syncState.currentUserEmail != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = syncState.currentUserEmail!!,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "Sign in with Google to enable automatic cloud backup across devices.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    isSigningIn = true
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        viewModel.authManager.signInWithGoogle(
                                            activity = activity,
                                            scope = coroutineScope,
                                            onSuccess = {
                                                isSigningIn = false
                                                viewModel.triggerManualSync()
                                            },
                                            onError = {
                                                isSigningIn = false
                                            }
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSigningIn
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign in with Google", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Metrics summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF7FBF9))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Last Synced Time:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = lastSyncString, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Records in Cloud:", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = if (syncState.syncedCount > 0) "${syncState.syncedCount} items" else "Ready", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                    if (syncState.status == SyncStatus.SYNCING && syncState.currentEntity.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Syncing now:", fontSize = 11.5.sp, color = EmeraldPrimary)
                            Text(text = syncState.currentEntity, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                    if (syncState.errorMessage != null) {
                        Text(
                            text = syncState.errorMessage!!,
                            fontSize = 11.sp,
                            color = Color(0xFFC62828),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.triggerManualSync()
                },
                enabled = syncState.status != SyncStatus.SYNCING,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (syncState.status == SyncStatus.SYNCING) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Syncing...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Now", fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close", fontSize = 12.sp)
            }
        }
    )
}
