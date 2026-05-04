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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val DarkDeep    = Color(0xFF1E1A15)

private val GoldPrimary = Color(0xFFC89B3C)
private val GoldAccent  = Color(0xFFE6B85C)
private val GoldDeep    = Color(0xFFA67C2E)

private val SoftGreen = Color(0xFF7FBF9F)
private val SoftBlue  = Color(0xFF7DAED3)
private val SoftPeach = Color(0xFFE8A87C)
private val SoftOlive = Color(0xFFB5A27A)

private val TextPrimary   = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6F6F6F)
private val TextOnDark    = Color(0xFFFFFFFF)
private val TextGold      = Color(0xFFC89B3C)
private val ErrorRed      = Color(0xFFE53935)

private val GoldGradient = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
private val DarkGradient = Brush.verticalGradient(listOf(DarkDeep, DarkSurface))

// ─── Tag options ─────────────────────────────────────────────────────────────

private val availableTags = listOf(
    "UI Design", "UX Research", "Branding", "Mobile", "Web",
    "Typography", "Illustration", "Motion", "Code", "3D"
)

// ─── Data model (database-ready) ─────────────────────────────────────────────

data class ProjectDraft(
    val title: String          = "",
    val description: String    = "",
    val category: String       = "",
    val tags: List<String>     = emptyList(),
    val imageUri: Uri?         = null,
    val isPublic: Boolean      = true
)

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun EditProjectScreen(navController: NavController) {

    // ── State (swap these with ViewModel + database calls later) ──────────
    var draft by remember {
        mutableStateOf(
            ProjectDraft(
                title       = "Forge Mobile App",
                description = "A skill-building mobile app built with Jetpack Compose. Focused on premium onboarding UX and rich dark-and-gold brand language.",
                category    = "Mobile",
                tags        = listOf("UI Design", "Mobile"),
                isPublic    = true
            )
        )
    }
    var imageUri     by remember { mutableStateOf<Uri?>(null) }
    var isSaving     by remember { mutableStateOf(false) }
    var saveSuccess  by remember { mutableStateOf(false) }
    var showDiscard  by remember { mutableStateOf(false) }

    val hasChanges = true // TODO: diff against original loaded from DB

    // ── Launchers ─────────────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = it } }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { imageUri = it } }

    // ── Discard dialog ────────────────────────────────────────────────────
    if (showDiscard) {
        DiscardDialog(
            onConfirm = { navController.popBackStack() },
            onDismiss = { showDiscard = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundMain)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            // ── Dark top bar ───────────────────────────────────────────────
            item {
                EditTopBar(
                    onBack = {
                        if (hasChanges) showDiscard = true
                        else navController.popBackStack()
                    }
                )
            }

            // ── Cover image zone ───────────────────────────────────────────
            item {
                CoverImageZone(
                    imageUri       = imageUri,
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onFileClick    = { fileLauncher.launch("*/*") },
                    onClear        = { imageUri = null }
                )
            }

            // ── Visibility toggle ──────────────────────────────────────────
            item {
                VisibilityRow(
                    isPublic  = draft.isPublic,
                    onToggle  = { draft = draft.copy(isPublic = it) }
                )
            }

            // ── Project title ──────────────────────────────────────────────
            item {
                EditSectionLabel("Project Title", topPad = 20.dp)
                EditTextField(
                    value         = draft.title,
                    onValueChange = { draft = draft.copy(title = it) },
                    placeholder   = "Give your project a strong title",
                    singleLine    = true,
                    maxChars      = 60,
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
            }

            // ── Description ────────────────────────────────────────────────
            item {
                EditSectionLabel("Description", topPad = 20.dp)
                EditTextField(
                    value         = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    placeholder   = "What's this project about? What were your goals?",
                    singleLine    = false,
                    minLines      = 5,
                    maxChars      = 500,
                    modifier      = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
            }

            // ── Category ──────────────────────────────────────────────────
            item {
                EditSectionLabel("Category", topPad = 20.dp)
                CategoryRow(
                    selected   = draft.category,
                    onSelect   = { draft = draft.copy(category = it) }
                )
            }

            // ── Tags ───────────────────────────────────────────────────────
            item {
                EditSectionLabel("Tags", topPad = 20.dp)
                TagsRow(
                    selected  = draft.tags,
                    onToggle  = { tag ->
                        val updated = if (draft.tags.contains(tag))
                            draft.tags - tag
                        else if (draft.tags.size < 5)
                            draft.tags + tag
                        else draft.tags
                        draft = draft.copy(tags = updated)
                    }
                )
                if (draft.tags.size >= 5) {
                    Text(
                        "Maximum 5 tags selected",
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        modifier = Modifier.padding(start = 20.dp, top = 6.dp)
                    )
                }
            }

            // ── Danger zone ────────────────────────────────────────────────
            item { DangerZone(onDelete = { /* TODO: call DB delete */ navController.popBackStack() }) }
        }

        // ── Sticky save bar ────────────────────────────────────────────────
        SaveBar(
            isSaving    = isSaving,
            saveSuccess = saveSuccess,
            onSave      = {
                isSaving = true
                // TODO: replace with actual DB/API call e.g. viewModel.updateProject(draft)
                // Simulated save:
                isSaving    = false
                saveSuccess = true
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
fun EditTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGradient)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        IconButton(
            onClick = onBack,
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
                    Icons.AutoMirrored.Filled.ArrowBack,
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
                "EDIT PROJECT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = GoldAccent
            )
            Text(
                "Refine Your Work",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextOnDark,
                letterSpacing = (-0.3).sp
            )
        }

        // Draft indicator top-right
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Draft", fontSize = 11.sp, color = TextOnDark.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Cover Image Zone ────────────────────────────────────────────────────────

@Composable
fun CoverImageZone(
    imageUri: Uri?,
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(DarkGradient)
    ) {
        // Decorative blob
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.07f))
        )

        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Cover image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, DarkSurface.copy(alpha = 0.55f))))
            )
            // Clear button
            IconButton(
                onClick = onClear,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(DarkSurface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, null, tint = TextOnDark, modifier = Modifier.size(17.dp))
                }
            }
            Text(
                "Cover image ✓",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldAccent,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            )
        } else {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GoldPrimary.copy(alpha = 0.14f))
                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Add a cover image", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Makes your project stand out", fontSize = 12.sp, color = TextOnDark.copy(alpha = 0.4f))
            }
        }

        // Gold bottom bar
        Box(
            modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomCenter).background(GoldGradient)
        )
    }

    // Action row below hero
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(GoldGradient)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onGalleryClick)
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Photo, null, tint = TextOnDark, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text("Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(CardBackground)
                .border(1.5.dp, Color(0xFFD8D0C4), RoundedCornerShape(13.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onFileClick)
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AttachFile, null, tint = TextSecondary, modifier = Modifier.size(17.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text("Files", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

// ─── Visibility Toggle ───────────────────────────────────────────────────────

@Composable
fun VisibilityRow(isPublic: Boolean, onToggle: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isPublic) SoftGreen.copy(alpha = 0.15f) else BackgroundSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPublic) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isPublic) SoftGreen else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        if (isPublic) "Public" else "Private",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        if (isPublic) "Visible to the community" else "Only you can see this",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Switch(
                checked = isPublic,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor       = TextOnDark,
                    checkedTrackColor       = GoldPrimary,
                    uncheckedThumbColor     = TextSecondary,
                    uncheckedTrackColor     = BackgroundSecondary,
                    uncheckedBorderColor    = Color(0xFFD8D0C4)
                )
            )
        }
    }
}

// ─── Section Label ───────────────────────────────────────────────────────────

@Composable
fun EditSectionLabel(text: String, topPad: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = topPad, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp).height(16.dp)
                .clip(RoundedCornerShape(50))
                .background(GoldGradient)
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = 0.1.sp)
    }
}

// ─── Edit Text Field ─────────────────────────────────────────────────────────

@Composable
fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxChars: Int = Int.MAX_VALUE
) {
    val atLimit = value.length >= maxChars

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(
                    1.5.dp,
                    if (atLimit) ErrorRed.copy(alpha = 0.6f)
                    else if (value.isNotEmpty()) GoldPrimary.copy(alpha = 0.55f)
                    else Color(0xFFE0D9CE),
                    RoundedCornerShape(16.dp)
                )
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = { if (it.length <= maxChars) onValueChange(it) },
                singleLine = singleLine,
                minLines = minLines,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 23.sp
                ),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { inner ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        if (value.isEmpty()) {
                            Text(placeholder, fontSize = 15.sp, color = Color(0xFFBBB3A8), lineHeight = 23.sp)
                        }
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp, end = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "${value.length} / $maxChars",
                fontSize = 11.sp,
                color = if (atLimit) ErrorRed else TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

// ─── Category Row ─────────────────────────────────────────────────────────────

private val categoryOptions = listOf("Mobile", "Web", "Branding", "Print", "Motion", "3D")

@Composable
fun CategoryRow(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categoryOptions) { cat ->
            val isSelected = cat == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) GoldGradient
                        else Brush.horizontalGradient(listOf(CardBackground, CardBackground))
                    )
                    .border(1.5.dp, if (isSelected) Color.Transparent else Color(0xFFD8D0C4), RoundedCornerShape(50))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onSelect(if (isSelected) "" else cat)
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    cat,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextOnDark else TextSecondary
                )
            }
        }
    }
}

// ─── Tags Row ────────────────────────────────────────────────────────────────

@Composable
fun TagsRow(selected: List<String>, onToggle: (String) -> Unit) {
    // Two wrapped rows of tags
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        availableTags.chunked(5).forEach { rowTags ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowTags.forEach { tag ->
                    val isSelected = selected.contains(tag)
                    val isDisabled = !isSelected && selected.size >= 5
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isSelected) GoldGradient
                                else Brush.horizontalGradient(listOf(CardBackground, CardBackground))
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent
                                else if (isDisabled) Color(0xFFE8E0D6)
                                else Color(0xFFD8D0C4),
                                RoundedCornerShape(50)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = !isDisabled || isSelected
                            ) { onToggle(tag) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            tag,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextOnDark
                            else if (isDisabled) TextSecondary.copy(alpha = 0.4f)
                            else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ─── Danger Zone ─────────────────────────────────────────────────────────────

@Composable
fun DangerZone(onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.width(3.dp).height(16.dp)
                    .clip(RoundedCornerShape(50)).background(ErrorRed)
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text("Danger Zone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ErrorRed.copy(alpha = 0.06f))
                .border(1.dp, ErrorRed.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Delete Project",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Text(
                        "This action cannot be undone.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(ErrorRed.copy(alpha = 0.12f))
                        .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { confirmDelete = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text("Delete", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor   = CardBackground,
            shape            = RoundedCornerShape(24.dp),
            title = {
                Text("Delete this project?", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 18.sp)
            },
            text = {
                Text(
                    "All feedback and data associated with this project will be permanently removed. This cannot be undone.",
                    color = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ErrorRed)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            confirmDelete = false
                            onDelete()
                        }
                        .padding(horizontal = 22.dp, vertical = 11.dp)
                ) {
                    Text("Yes, delete", color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundSecondary)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { confirmDelete = false }
                        .padding(horizontal = 22.dp, vertical = 11.dp)
                ) {
                    Text("Cancel", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        )
    }
}

// ─── Discard Dialog ──────────────────────────────────────────────────────────

@Composable
fun DiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBackground,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Text("Discard changes?", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 18.sp)
        },
        text = {
            Text(
                "You have unsaved changes. If you go back now, your edits will be lost.",
                color = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onConfirm)
                    .padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Text("Discard", color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldGradient)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                    .padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Text("Keep editing", color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    )
}

// ─── Save Bar ────────────────────────────────────────────────────────────────

@Composable
fun SaveBar(
    isSaving: Boolean,
    saveSuccess: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(BackgroundMain.copy(alpha = 0f), BackgroundMain, BackgroundMain))
                )
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 30.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                    brush = if (saveSuccess)
                        Brush.verticalGradient(
                            listOf(
                                SoftGreen.copy(alpha = 0.2f),
                                BackgroundMain
                            )
                        )
                    else GoldGradient
                )
                    .border(
                        1.dp,
                        if (saveSuccess) SoftGreen.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isSaving
                    ) { onSave() },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = when {
                        isSaving    -> "saving"
                        saveSuccess -> "success"
                        else        -> "idle"
                    },
                    transitionSpec = {
                        fadeIn(tween(250)).togetherWith(fadeOut(tween(200)))
                    },
                    label = "save_state"
                ) { state ->
                    when (state) {
                        "saving"  -> CircularProgressIndicator(
                            color = TextOnDark,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        "success" -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Changes Saved!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SoftGreen)
                        }
                        else      -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                        }
                    }
                }
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
fun EditProjectScreenPreview() {
    EditProjectScreen(navController = rememberNavController())
}