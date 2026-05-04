package com.kennedy.forge.ui.screens.feedback

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage

// ─── Colour Tokens ───────────────────────────────────────────────────────────

private val BackgroundMain      = Color(0xFFF5F2EC)
private val BackgroundSecondary = Color(0xFFEDE7DD)
private val CardBackground      = Color(0xFFFFFFFF)

private val DarkSurface = Color(0xFF121212)
private val DarkCard    = Color(0xFF1C1C1C)

private val GoldPrimary = Color(0xFFC89B3C)
private val GoldAccent  = Color(0xFFE6B85C)
private val GoldDeep    = Color(0xFFA67C2E)

private val SoftGreen = Color(0xFF7FBF9F)
private val SoftBlue  = Color(0xFF7DAED3)
private val SoftPeach = Color(0xFFE8A87C)

private val TextPrimary   = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6F6F6F)
private val TextOnDark    = Color(0xFFFFFFFF)
private val TextGold      = Color(0xFFC89B3C)

private val GoldGradient = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
private val DarkGradient = Brush.verticalGradient(listOf(Color(0xFF1E1A15), DarkSurface))

// ─── Category chips ──────────────────────────────────────────────────────────

private val categories = listOf("Design", "Writing", "Code", "Art", "Music", "Other")

// ─── Screen ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitWorkScreen(navController: NavController) {

    var title           by remember { mutableStateOf("") }
    var description     by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var imageUri        by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }

    // ── Launchers ────────────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            attachedFileName = null
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            attachedFileName = it.lastPathSegment ?: "Attached file"
            imageUri = null
        }
    }

    val canSubmit = title.isNotBlank() && description.isNotBlank()

    Scaffold(
        containerColor = BackgroundMain,
        // ── Top bar ──────────────────────────────────────────────────────────
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                // Back button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextOnDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SUBMIT WORK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = GoldAccent
                    )
                    Text(
                        text = "Get Feedback",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextOnDark,
                        letterSpacing = (-0.3).sp
                    )
                }
            }
        },
        // ── Bottom nav ───────────────────────────────────────────────────────
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Media upload zone ─────────────────────────────────────────────
            item {
                MediaUploadZone(
                    imageUri = imageUri,
                    attachedFileName = attachedFileName,
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onFileClick    = { fileLauncher.launch("*/*") },
                    onClear        = { imageUri = null; attachedFileName = null }
                )
            }

            // ── Upload action row ─────────────────────────────────────────────
            item {
                UploadActionRow(
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onFileClick    = { fileLauncher.launch("*/*") }
                )
            }

            // ── Section: Details ─────────────────────────────────────────────
            item {
                SectionLabel(text = "Work Details", topPad = 24.dp)
            }

            // ── Title field ───────────────────────────────────────────────────
            item {
                StyledTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Give your work a title",
                    label = "Title",
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            // ── Description ───────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(14.dp))
                StyledTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Describe your work, your process, or what feedback you're looking for…",
                    label = "Description",
                    singleLine = false,
                    minLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

            // ── Category ─────────────────────────────────────────────────────
            item {
                SectionLabel(text = "Category", topPad = 24.dp)
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { cat ->
                        val selected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) GoldGradient
                                    else Brush.horizontalGradient(
                                        listOf(CardBackground, CardBackground)
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    if (selected) Color.Transparent else Color(0xFFD8D0C4),
                                    RoundedCornerShape(50)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedCategory = if (selected) null else cat }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) TextOnDark else TextSecondary,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }
                }
            }

            // ── Submit button ─────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(36.dp))
                SubmitButton(
                    enabled = canSubmit,
                    onClick  = { navController.navigate("feedback_dashboard") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your work is reviewed by mentors within 24 hours",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── Media Upload Zone ───────────────────────────────────────────────────────

@Composable
fun MediaUploadZone(
    imageUri: Uri?,
    attachedFileName: String?,
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(DarkGradient)
    ) {
        when {
            imageUri != null -> {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, DarkSurface.copy(alpha = 0.5f))
                            )
                        )
                )
                // Clear button
                IconButton(
                    onClick = onClear,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkSurface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
                    }
                }
                // Label bottom-left
                Text(
                    text = "Image attached ✓",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldAccent,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            attachedFileName != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(GoldPrimary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AttachFile, null, tint = GoldAccent, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = attachedFileName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextOnDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("File attached ✓", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Medium)
                }
                // Clear
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
                    }
                }
            }

            else -> {
                // Empty state — tap to open gallery
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onGalleryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .border(1.5.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload media",
                                tint = GoldAccent,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Tap to add a photo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "or use the buttons below to browse files",
                            fontSize = 12.sp,
                            color = TextOnDark.copy(alpha = 0.45f)
                        )
                    }
                }
            }
        }

        // Gold bottom rule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .background(GoldGradient)
        )
    }
}

// ─── Upload Action Row ───────────────────────────────────────────────────────

@Composable
fun UploadActionRow(
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gallery button
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(GoldGradient)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onGalleryClick
                )
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Photo, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Gallery",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnDark
            )
        }

        // Files button
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(CardBackground)
                .border(1.5.dp, Color(0xFFD8D0C4), RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onFileClick
                )
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AttachFile, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Files",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

// ─── Section Label ───────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, topPad: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp, top = topPad, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(GoldGradient)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 0.1.sp
        )
    }
}

// ─── Styled Text Field ───────────────────────────────────────────────────────

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    label: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    val isFocused = remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (value.isNotEmpty()) TextGold else TextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(
                    width = 1.5.dp,
                    color = if (value.isNotEmpty()) GoldPrimary.copy(alpha = 0.6f) else Color(0xFFE0D9CE),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                minLines = minLines,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { inner ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        if (value.isEmpty()) {
                            Text(placeholder, fontSize = 15.sp, color = Color(0xFFBBB3A8), lineHeight = 22.sp)
                        }
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── Submit Button ───────────────────────────────────────────────────────────

@Composable
fun SubmitButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) GoldGradient
                else Brush.horizontalGradient(listOf(BackgroundSecondary, BackgroundSecondary))
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Send,
                contentDescription = null,
                tint = if (enabled) TextOnDark else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Submit for Feedback",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) TextOnDark else TextSecondary,
                letterSpacing = 0.2.sp
            )
        }
    }
}

// ─── Bottom Nav ──────────────────────────────────────────────────────────────

@Composable
fun BottomNavBar(navController: NavController) {
    Surface(
        color = CardBackground,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            NavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = false,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
            // Submit (active)
            NavItem(
                icon = Icons.Default.Add,
                label = "Submit",
                selected = true,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            // Profile
            NavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = false,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("profile") }
            )
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextOnDark, modifier = Modifier.size(22.dp))
            }
        } else {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Missing import alias ─────────────────────────────────────────────────────

@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    minLines: Int,
    textStyle: androidx.compose.ui.text.TextStyle,
    keyboardOptions: KeyboardOptions,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = minLines,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        decorationBox = decorationBox,
        modifier = modifier
    )
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
fun SubmitWorkScreenPreview() {
    SubmitWorkScreen(rememberNavController())
}