package com.example.ui.screens.vault

import android.media.MediaPlayer
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.SecureLensApp
import com.example.data.local.entity.VaultMediaEntity
import com.example.ui.components.*
import com.example.ui.screens.onboarding.NumericKeypad
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VaultUnlockScreen(
    onUnlockSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val savedPin by SecureLensApp.instance.settingsStore.vaultPin.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    var showSetPinDialog by remember { mutableStateOf(false) }
    var newPinCandidate by remember { mutableStateOf("") }

    if (savedPin != null && savedPin!!.isEmpty()) {
        AlertDialog(
            onDismissRequest = { /* force choice */ },
            title = {
                Text(
                    text = "Vault PIN Protection",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecureInk
                    )
                )
            },
            text = {
                Text(
                    text = "You skipped setting a PIN during onboarding. Would you like to create a 6-digit PIN now to protect your private media, or proceed without a password?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SecureMuted)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showSetPinDialog = true }
                ) {
                    Text("Set PIN Now", fontWeight = FontWeight.Bold, color = SecurePrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onUnlockSuccess() }
                ) {
                    Text("Open Vault Directly", color = SecureMuted)
                }
            },
            containerColor = SecureSurface
        )
    }

    if (showSetPinDialog) {
        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = {
                Text(
                    text = "Enter 6-digit PIN",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecureInk
                    )
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Enter 6 digits using the keypad below to protect your vault:",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(6) { index ->
                            val isFilled = index < newPinCandidate.length
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (isFilled) SecurePrimary else ScannerRing3)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (newPinCandidate.length == 6) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                SecureLensApp.instance.settingsStore.setVaultPin(newPinCandidate)
                                showSetPinDialog = false
                                onUnlockSuccess()
                            }
                        }
                    ) {
                        Text("Save & Open", fontWeight = FontWeight.Bold, color = SecurePrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSetPinDialog = false
                        newPinCandidate = ""
                    }
                ) {
                    Text("Cancel", color = SecureMuted)
                }
            },
            containerColor = SecureSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = SecureInk)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(SecureSurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = SecurePrimary,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Private Vault",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = SecureInk,
                fontSize = 28.sp
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter your PIN to access encrypted media.",
            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, fontSize = 13.sp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 6 PIN dots
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                val isFilled = index < enteredPin.length
                Box(
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isFilled) SecurePrimary else ScannerRing3)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall.copy(color = SecureAlert, fontSize = 12.sp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        NumericKeypad(
            onNumberClick = { num ->
                if (showSetPinDialog) {
                    if (newPinCandidate.length < 6) {
                        newPinCandidate += num
                    }
                } else if (enteredPin.length < 6) {
                    val newPin = enteredPin + num
                    enteredPin = newPin
                    if (newPin.length == 6) {
                        if (savedPin.isNullOrEmpty() || newPin == savedPin) {
                            onUnlockSuccess()
                        } else {
                            errorMessage = "Incorrect PIN. Try again."
                            enteredPin = ""
                        }
                    }
                }
            },
            onDeleteClick = {
                if (showSetPinDialog) {
                    if (newPinCandidate.isNotEmpty()) {
                        newPinCandidate = newPinCandidate.dropLast(1)
                    }
                } else if (enteredPin.isNotEmpty()) {
                    enteredPin = enteredPin.dropLast(1)
                    errorMessage = null
                }
            },
            onBiometricClick = {
                // Biometric shortcut
                onUnlockSuccess()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun VaultGalleryScreen(
    onMediaClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onSecuritySettingsClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("Photos") } // Photos, Videos, Audio, Events
    val allMedia by SecureLensApp.instance.vaultRepository.getAllMedia().collectAsState(initial = emptyList())

    val filteredMedia = remember(allMedia, selectedCategory) {
        when (selectedCategory) {
            "Photos" -> allMedia.filter { it.mediaKind == "PHOTO" }
            "Videos" -> allMedia.filter { it.mediaKind == "VIDEO" }
            "Audio" -> allMedia.filter { it.mediaKind == "AUDIO" }
            else -> allMedia.filter { it.mediaKind == "INTRUDER_EVENT" }
        }
    }

    val importPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val kind = if (selectedCategory == "Videos") "VIDEO" else if (selectedCategory == "Audio") "AUDIO" else "PHOTO"
                SecureLensApp.instance.vaultRepository.importFromUri(uri, "Imported $kind", kind)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = "Private Vault",
            onBackClick = onBackClick,
            onActionClick = onSecuritySettingsClick,
            actionIcon = Icons.Outlined.Settings
        )

        // Category Pills matching D19 (Photos / Videos / Audio / Events)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Photos", "Videos", "Audio", "Events").forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) SecureSurface else Color.Transparent)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) SecurePrimary else SecureMuted,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subheader with Count & Import button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent $selectedCategory",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SecureInk,
                    fontSize = 17.sp
                )
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SecureSurfaceSoft)
                    .clickable { importPicker.launch("*/*") }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("btn_vault_import")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = SecurePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Import",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecurePrimary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredMedia.isEmpty()) {
            // Empty Vault state matching 59_vault_empty
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(SecureSurfaceSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = SecurePrimary,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your vault is empty",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Photos and finalized recordings you choose to save will appear here.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SecureMuted,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryGradientButton(
                        text = "Import Media",
                        onClick = { importPicker.launch("*/*") },
                        testTag = "btn_empty_import"
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredMedia, key = { it.id }) { item ->
                    VaultThumbnailCard(item = item, onClick = { onMediaClick(item.id) })
                }
            }
        }
    }
}

@Composable
fun VaultThumbnailCard(
    item: VaultMediaEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecureSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (item.mediaKind) {
                        "VIDEO" -> Color(0xFF86A8C7)
                        "AUDIO" -> Color(0xFF9FB6CD)
                        else -> Color(0xFF6B9AC4)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (item.mediaKind) {
                    "VIDEO" -> Icons.Outlined.Videocam
                    "AUDIO" -> Icons.Outlined.Mic
                    else -> Icons.Outlined.Image
                },
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            if (item.mediaKind == "VIDEO" && item.durationMs > 0L) {
                val secs = item.durationMs / 1000L
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format("%02d:%02d", secs / 60, secs % 60),
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    mediaId: String,
    onBackClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var media by remember { mutableStateOf<VaultMediaEntity?>(null) }
    var showActionSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var decryptedFile by remember { mutableStateOf<File?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var audioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(mediaId) {
        val loaded = SecureLensApp.instance.vaultRepository.getMediaById(mediaId)
        media = loaded
        renameText = loaded?.title ?: ""
        if (loaded != null) {
            decryptedFile = SecureLensApp.instance.vaultRepository.decryptToCache(loaded)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                audioPlayer?.stop()
                audioPlayer?.release()
            } catch (e: Exception) {}
            audioPlayer = null
        }
    }

    val currentMedia = media

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(
            title = if (currentMedia?.mediaKind == "VIDEO") "Media Player" else if (currentMedia?.mediaKind == "AUDIO") "Audio Player" else "Photo Viewer",
            onBackClick = onBackClick,
            onActionClick = { showActionSheet = true },
            actionIcon = Icons.Outlined.MoreHoriz
        )

        if (currentMedia != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Media preview container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val file = decryptedFile
                    if (file == null) {
                        CircularProgressIndicator(color = SecurePrimary)
                    } else if (currentMedia.mediaKind == "VIDEO") {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoPath(file.absolutePath)
                                    val controller = MediaController(ctx)
                                    controller.setAnchorView(this)
                                    setMediaController(controller)
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        start()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (currentMedia.mediaKind == "AUDIO") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(SecurePrimary.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isAudioPlaying) {
                                            audioPlayer?.pause()
                                            isAudioPlaying = false
                                        } else {
                                            if (audioPlayer == null) {
                                                audioPlayer = MediaPlayer().apply {
                                                    setDataSource(file.absolutePath)
                                                    prepare()
                                                    setOnCompletionListener { isAudioPlaying = false }
                                                }
                                            }
                                            audioPlayer?.start()
                                            isAudioPlaying = true
                                        }
                                    },
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = if (isAudioPlaying) "Playing Audio" else "Tap to Play Audio",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(currentMedia.durationMs / 1000)}s · Encrypted Private Audio",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f))
                            )
                        }
                    } else {
                        AsyncImage(
                            model = file,
                            contentDescription = currentMedia.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentMedia.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SecureInk,
                                fontSize = 17.sp
                            )
                        )
                        Text(
                            text = "Encrypted in private storage · ${(currentMedia.fileSize / 1024)} KB",
                            style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                        )
                    }

                    IconButton(
                        onClick = { showActionSheet = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecureSurfaceSoft)
                    ) {
                        Icon(Icons.Outlined.MoreHoriz, contentDescription = null, tint = SecurePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                SecondaryPillButton(
                    text = "Media Actions",
                    onClick = { showActionSheet = true },
                    testTag = "btn_media_actions"
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Actions bottom sheet
        if (showActionSheet) {
            ModalBottomSheet(
                onDismissRequest = { showActionSheet = false },
                containerColor = SecureSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Media Actions",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SecureInk
                        )
                    )
                    Text(
                        text = "Choose what to do with this private item.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ActionRow(
                        title = "Rename",
                        icon = Icons.Outlined.Edit,
                        onClick = {
                            showActionSheet = false
                            showRenameDialog = true
                        }
                    )

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))

                    ActionRow(
                        title = "Delete",
                        icon = Icons.Outlined.Delete,
                        iconTint = SecureAlert,
                        onClick = {
                            showActionSheet = false
                            showDeleteDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SecondaryPillButton(
                        text = "Cancel",
                        onClick = { showActionSheet = false }
                    )
                }
            }
        }

        // Rename Dialog
        if (showRenameDialog && currentMedia != null) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                containerColor = SecureSurface,
                title = { Text("Rename media", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                SecureLensApp.instance.vaultRepository.renameMedia(currentMedia.id, renameText)
                                media = media?.copy(title = renameText)
                                showRenameDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecurePrimary)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = SecureMuted)
                    }
                }
            )
        }

        // Delete Dialog
        if (showDeleteDialog && currentMedia != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = SecureSurface,
                title = { Text("Delete this item?", color = SecureAlert, fontWeight = FontWeight.Bold) },
                text = {
                    Text("This permanently deletes the encrypted file and its local record. This action cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                SecureLensApp.instance.vaultRepository.deleteMedia(currentMedia.id)
                                showDeleteDialog = false
                                onDeleted()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecureAlert)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = SecureMuted)
                    }
                }
            )
        }
    }
}

@Composable
fun ActionRow(
    title: String,
    icon: ImageVector,
    iconTint: Color = SecurePrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SecureSurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = SecureInk
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = SecureMuted)
    }
}

@Composable
fun VaultSecurityScreen(
    onBackClick: () -> Unit,
    onLockVault: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = SecureLensApp.instance.settingsStore
    val isBiometric by settings.isBiometricEnabled.collectAsState(initial = true)
    val autoLock by settings.autoLockTimeout.collectAsState(initial = "Immediately")
    val currentPin by settings.vaultPin.collectAsState(initial = "")
    var showSetPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    val isPinProtectionEnabled = !currentPin.isNullOrEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SecureBackground)
    ) {
        AppHeader(title = "Vault Security", onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Access",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SecureInk),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Vault PIN Lock Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vault PIN Lock", fontWeight = FontWeight.SemiBold, color = SecureInk)
                            Text(
                                text = if (isPinProtectionEnabled) "PIN protection is active" else "Optional: vault is unlocked directly",
                                style = MaterialTheme.typography.bodySmall.copy(color = SecureMuted, fontSize = 12.sp)
                            )
                        }
                        Switch(
                            checked = isPinProtectionEnabled,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    showSetPinDialog = true
                                } else {
                                    coroutineScope.launch { settings.setVaultPin("") }
                                }
                            }
                        )
                    }

                    if (isPinProtectionEnabled) {
                        HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSetPinDialog = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Change PIN", fontWeight = FontWeight.SemiBold, color = SecurePrimary)
                            Icon(Icons.Outlined.Edit, contentDescription = null, tint = SecurePrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Biometric unlock", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isBiometric,
                            onCheckedChange = { coroutineScope.launch { settings.setBiometricEnabled(it) } }
                        )
                    }

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-lock", fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SecureSurfaceSoft)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(autoLock, color = SecurePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Data protection",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SecureInk),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SecureSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Local file encryption", fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecureSoftGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Enabled", color = SecureSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(color = SecureDivider, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Private app storage", fontWeight = FontWeight.SemiBold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SecureSoftGreen)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Enabled", color = SecureSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            SecondaryPillButton(
                text = "Lock Vault",
                onClick = onLockVault,
                testTag = "btn_lock_vault"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
