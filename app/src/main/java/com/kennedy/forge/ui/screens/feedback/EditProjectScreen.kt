package com.kennedy.forge.ui.screens.feedback

// ─────────────────────────────────────────────────────────────────
//  NAV GRAPH WIRING
//
//  composable("edit_project") {
//      EditProjectScreen(navController = navController, workId = null)
//  }
//  composable("edit_project/{workId}") { back ->
//      EditProjectScreen(
//          navController = navController,
//          workId        = back.arguments?.getString("workId")
//      )
//  }
//
//  Data path:  works/{uid}/{workId}   ← identical to SubmitWorkScreen
// ─────────────────────────────────────────────────────────────────

import android.content.Context
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY CONFIG  — same preset as SubmitWorkScreen
// ─────────────────────────────────────────────────────────────────

private const val CLOUDINARY_CLOUD_NAME    = "dv4sidtxo"
private const val CLOUDINARY_UPLOAD_PRESET = "submit_work"
private const val CLOUDINARY_UPLOAD_URL    =
    "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"

// ─────────────────────────────────────────────────────────────────
//  COLOUR TOKENS  (identical to original)
// ─────────────────────────────────────────────────────────────────

private val BackgroundMain      = Color(0xFFF5F2EC)
private val BackgroundSecondary = Color(0xFFEDE7DD)
private val CardBackground      = Color(0xFFFFFFFF)
private val DarkSurface         = Color(0xFF121212)
private val DarkCard            = Color(0xFF1C1C1C)
private val DarkDeep            = Color(0xFF1E1A15)
private val GoldPrimary         = Color(0xFFC89B3C)
private val GoldAccent          = Color(0xFFE6B85C)
private val GoldDeep            = Color(0xFFA67C2E)
private val SoftGreen           = Color(0xFF7FBF9F)
private val TextPrimary         = Color(0xFF1A1A1A)
private val TextSecondary       = Color(0xFF6F6F6F)
private val TextOnDark          = Color(0xFFFFFFFF)
private val ErrorRed            = Color(0xFFE53935)

private val GoldGradient = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
private val DarkGradient = Brush.verticalGradient(listOf(DarkDeep, DarkSurface))

// ─────────────────────────────────────────────────────────────────
//  OPTIONS
// ─────────────────────────────────────────────────────────────────

private val editCategoryOptions = listOf("Design", "Writing", "Code", "Art", "Music", "Other")

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY UPLOAD HELPER  (blocking — call from Dispatchers.IO)
//  Same implementation as SubmitWorkScreen so re-uploads work correctly
// ─────────────────────────────────────────────────────────────────

private fun uploadToCloudinaryEdit(context: Context, uri: Uri): String? = runCatching {
    val mimeType  = context.contentResolver.getType(uri) ?: "image/jpeg"
    val bytes     = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: return@runCatching null
    val extension = when {
        mimeType.contains("png")  -> "png"
        mimeType.contains("gif")  -> "gif"
        mimeType.contains("webp") -> "webp"
        else                      -> "jpg"
    }

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file", "work.$extension",
            bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        )
        .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
        .addFormDataPart("folder", "forge/works")
        .build()

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val request = Request.Builder()
        .url(CLOUDINARY_UPLOAD_URL)
        .post(requestBody)
        .build()

    val response = client.newCall(request).execute()
    val body     = response.body?.string() ?: return@runCatching null
    if (!response.isSuccessful) return@runCatching null

    JSONObject(body).optString("secure_url").takeIf { it.isNotBlank() }
}.getOrNull()

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPERS
//  All use  works/{uid}/  — identical path to SubmitWorkScreen
// ─────────────────────────────────────────────────────────────────

private fun worksRef(uid: String) =
    Firebase.database.reference.child("works").child(uid)

private suspend fun fetchWorks(uid: String): List<Work> =
    withContext(Dispatchers.IO) {
        try {
            val snap = worksRef(uid).get().await()
            snap.children
                .mapNotNull { it.getValue(Work::class.java) }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

/**
 * Full update — overwrites the entire node with the edited Work object.
 * This keeps imageUrl, fileName, status, createdAt intact alongside
 * any fields the user changed.
 */
private suspend fun saveWorkEdits(work: Work): Result<Unit> = runCatching {
    withContext(Dispatchers.IO) {
        worksRef(work.uid).child(work.id)
            .setValue(work.copy(updatedAt = System.currentTimeMillis()))
            .await()
    }
}

private suspend fun deleteWorkEntry(uid: String, workId: String): Result<Unit> = runCatching {
    withContext(Dispatchers.IO) {
        worksRef(uid).child(workId).removeValue().await()
    }
}

// ─────────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────────

@Composable
fun EditProjectScreen(
    navController: NavController,
    workId       : String? = null
) {
    val context = LocalContext.current
    val uid     = Firebase.auth.currentUser?.uid ?: ""
    val scope   = rememberCoroutineScope()

    var isLoadingList  by remember { mutableStateOf(true) }
    var allWorks       by remember { mutableStateOf<List<Work>>(emptyList()) }
    var loadError      by remember { mutableStateOf("") }
    var activeWork     by remember { mutableStateOf<Work?>(null) }

    // Image state for the edit form
    var localImageUri     by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl  by remember { mutableStateOf("") }
    var isUploadingImage  by remember { mutableStateOf(false) }
    var uploadError       by remember { mutableStateOf("") }

    var isSaving      by remember { mutableStateOf(false) }
    var saveSuccess   by remember { mutableStateOf(false) }
    var saveError     by remember { mutableStateOf("") }
    var showDiscard   by remember { mutableStateOf(false) }

    // ── Load works/{uid} — same path SubmitWorkScreen writes to ──
    LaunchedEffect(uid) {
        if (uid.isEmpty()) {
            isLoadingList = false
            loadError     = "Not signed in."
            return@LaunchedEffect
        }
        val fetched   = fetchWorks(uid)
        allWorks      = fetched
        isLoadingList = false

        if (workId != null) {
            activeWork = fetched.firstOrNull { it.id == workId }
                ?: run { loadError = "Work not found."; null }
        }
    }

    // ── When activeWork is set, pre-fill image state ──────────────
    LaunchedEffect(activeWork?.id) {
        val w = activeWork
        if (w != null) {
            uploadedImageUrl = w.imageUrl
            localImageUri    = null
            uploadError      = ""
            saveSuccess      = false
            saveError        = ""
        }
    }

    // ── Gallery launcher — auto-uploads to Cloudinary on pick ─────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            localImageUri    = it
            uploadedImageUrl = ""
            uploadError      = ""
            isUploadingImage = true
            scope.launch {
                val url = withContext(Dispatchers.IO) { uploadToCloudinaryEdit(context, it) }
                withContext(Dispatchers.Main) {
                    isUploadingImage = false
                    if (url != null) {
                        uploadedImageUrl = url
                        // Push updated imageUrl into activeWork so save picks it up
                        activeWork = activeWork?.copy(imageUrl = url)
                    } else {
                        uploadError   = "Image upload failed. Check your internet connection."
                        localImageUri = null
                    }
                }
            }
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val name = it.lastPathSegment ?: "Attached file"
            activeWork    = activeWork?.copy(fileName = name)
            localImageUri = null
            uploadError   = ""
        }
    }

    if (showDiscard) {
        DiscardDialog(
            onConfirm = {
                activeWork       = null
                localImageUri    = null
                uploadedImageUrl = ""
                saveSuccess      = false
                saveError        = ""
                showDiscard      = false
            },
            onDismiss = { showDiscard = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundMain)) {
        when {
            // ── 1. Loading ────────────────────────────────────────
            isLoadingList -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = GoldPrimary, strokeWidth = 2.5.dp)
                        Text("Loading your submissions…", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            // ── 2. Error ──────────────────────────────────────────
            loadError.isNotEmpty() && activeWork == null -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline, null,
                            tint     = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            loadError,
                            color     = ErrorRed,
                            textAlign = TextAlign.Center,
                            fontSize  = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldGradient)
                                .clickable { navController.popBackStack() }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Go Back", color = TextOnDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── 3. No submissions ─────────────────────────────────
            allWorks.isEmpty() -> {
                EmptyWorksState(onBack = { navController.popBackStack() })
            }

            // ── 4. Picker ─────────────────────────────────────────
            activeWork == null -> {
                WorkPickerScreen(
                    works    = allWorks,
                    onSelect = {
                        activeWork       = it
                        localImageUri    = null
                        uploadedImageUrl = it.imageUrl
                        saveSuccess      = false
                        saveError        = ""
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── 5. Edit form ──────────────────────────────────────
            else -> {
                val work = activeWork!!
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier       = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {

                        item {
                            EditTopBar(
                                title  = work.title.take(28).ifEmpty { "Edit Work" },
                                onBack = {
                                    activeWork       = null
                                    localImageUri    = null
                                    uploadedImageUrl = ""
                                    saveSuccess      = false
                                }
                            )
                        }

                        // ── Cover image zone ──────────────────────
                        item {
                            CoverImageZone(
                                imageUrl          = work.imageUrl,
                                localUri          = localImageUri,
                                isUploadingImage  = isUploadingImage,
                                onGalleryClick    = { galleryLauncher.launch("image/*") },
                                onFileClick       = { fileLauncher.launch("*/*") },
                                onClear           = {
                                    localImageUri    = null
                                    uploadedImageUrl = ""
                                    uploadError      = ""
                                    activeWork       = work.copy(imageUrl = "", fileName = "")
                                }
                            )
                        }

                        // Upload error
                        if (uploadError.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ErrorRed.copy(alpha = 0.08f))
                                        .padding(12.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline, null,
                                        tint     = ErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(uploadError, color = ErrorRed, fontSize = 12.sp)
                                }
                            }
                        }

                        // ── Title ─────────────────────────────────
                        item {
                            EditSectionLabel("Title", topPad = 20.dp)
                            EditTextField(
                                value         = work.title,
                                onValueChange = { activeWork = work.copy(title = it) },
                                placeholder   = "Give your work a strong title",
                                singleLine    = true,
                                maxChars      = 60,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            )
                        }

                        // ── Description ───────────────────────────
                        item {
                            EditSectionLabel("Description", topPad = 20.dp)
                            EditTextField(
                                value         = work.description,
                                onValueChange = { activeWork = work.copy(description = it) },
                                placeholder   = "Describe your work, process, or what feedback you need…",
                                singleLine    = false,
                                minLines      = 5,
                                maxChars      = 500,
                                modifier      = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            )
                        }

                        // ── Category ──────────────────────────────
                        item {
                            EditSectionLabel("Category", topPad = 20.dp)
                            WorkCategoryRow(
                                selected = work.category,
                                onSelect = { activeWork = work.copy(category = it) }
                            )
                        }

                        // ── Meta row ──────────────────────────────
                        item {
                            WorkMetaRow(work = work)
                        }

                        // ── Danger zone ───────────────────────────
                        item {
                            WorkDangerZone(onDelete = {
                                scope.launch {
                                    deleteWorkEntry(uid, work.id)
                                    allWorks   = fetchWorks(uid)
                                    activeWork = null
                                }
                            })
                        }

                        // Save error banner
                        if (saveError.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ErrorRed.copy(alpha = 0.08f))
                                        .padding(12.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ErrorOutline, null,
                                        tint     = ErrorRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(saveError, color = ErrorRed, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // ── Sticky save bar ───────────────────────────
                    SaveBar(
                        isSaving    = isSaving,
                        saveSuccess = saveSuccess,
                        onSave      = {
                            if (work.title.isBlank()) {
                                saveError = "Title cannot be empty."
                                return@SaveBar
                            }
                            if (isUploadingImage) {
                                saveError = "Please wait for the image to finish uploading."
                                return@SaveBar
                            }
                            isSaving  = true
                            saveError = ""
                            scope.launch {
                                saveWorkEdits(work).fold(
                                    onSuccess = {
                                        withContext(Dispatchers.Main) {
                                            isSaving    = false
                                            saveSuccess = true
                                            allWorks    = fetchWorks(uid)
                                        }
                                    },
                                    onFailure = { e ->
                                        withContext(Dispatchers.Main) {
                                            isSaving  = false
                                            saveError = e.message ?: "Save failed. Try again."
                                        }
                                    }
                                )
                            }
                        },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  WORK PICKER
// ─────────────────────────────────────────────────────────────────

@Composable
private fun WorkPickerScreen(
    works   : List<Work>,
    onSelect: (Work) -> Unit,
    onBack  : () -> Unit
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, null,
                            tint     = TextOnDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(
                    modifier            = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CHOOSE A SUBMISSION",
                        fontSize      = 10.sp,
                        fontWeight    = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color         = GoldAccent
                    )
                    Text(
                        "Which work would you like to edit?",
                        fontSize      = 16.sp,
                        fontWeight    = FontWeight.Black,
                        color         = TextOnDark,
                        letterSpacing = (-0.3).sp
                    )
                }
            }
        }

        item {
            Text(
                "${works.size} submission${if (works.size != 1) "s" else ""} found",
                color    = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }

        itemsIndexed(works) { _, work ->
            WorkPickerCard(work = work, onClick = { onSelect(work) })
        }
    }
}

@Composable
private fun WorkPickerCard(work: Work, onClick: () -> Unit) {
    val statusColor = when (work.status) {
        "reviewed" -> SoftGreen
        "closed"   -> Color(0xFF7DAED3)
        else       -> Color(0xFFE8A87C)
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thumbnail — imageUrl written by SubmitWorkScreen (Cloudinary URL)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundSecondary)
            ) {
                when {
                    work.imageUrl.isNotEmpty() -> {
                        AsyncImage(
                            model              = work.imageUrl,
                            contentDescription = work.title,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                    }
                    work.fileName.isNotEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AttachFile, null,
                                tint     = GoldPrimary.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    else -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Image, null,
                                tint     = GoldPrimary.copy(alpha = 0.35f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                if (work.category.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(topEnd = 8.dp))
                            .background(GoldPrimary)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            work.category,
                            color      = Color.White,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        work.title.ifEmpty { "Untitled" },
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            work.status.replaceFirstChar { it.uppercase() },
                            color      = statusColor,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (work.description.isNotEmpty()) {
                    Text(
                        work.description,
                        color      = TextSecondary,
                        fontSize   = 12.sp,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                if (work.createdAt > 0L) {
                    Text(
                        "Submitted ${formatWorkDate(work.createdAt)}",
                        color    = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                tint     = GoldPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  EMPTY STATE
// ─────────────────────────────────────────────────────────────────

@Composable
private fun EmptyWorksState(onBack: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.FolderOpen, null,
                tint     = GoldPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "No submissions yet",
            color      = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = 20.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Submit a work from the Submit Work screen first, then come back here to edit it.",
            color      = TextSecondary,
            fontSize   = 14.sp,
            textAlign  = TextAlign.Center,
            lineHeight = 21.sp
        )
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(GoldGradient)
                .clickable { onBack() }
                .padding(horizontal = 28.dp, vertical = 14.dp)
        ) {
            Text("Go Back", color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  WORK META ROW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun WorkMetaRow(work: Work) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundSecondary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WorkMetaItem(
            label = "Submitted",
            value = if (work.createdAt > 0L) formatWorkDate(work.createdAt) else "—"
        )
        Box(Modifier.width(1.dp).height(32.dp).background(Color(0xFFD8D0C4)))
        WorkMetaItem(
            label = "Last updated",
            value = if (work.updatedAt > 0L) formatWorkDate(work.updatedAt) else "—"
        )
        Box(Modifier.width(1.dp).height(32.dp).background(Color(0xFFD8D0C4)))
        WorkMetaItem(
            label = "Status",
            value = work.status.replaceFirstChar { it.uppercase() }
        )
    }
}

@Composable
private fun WorkMetaItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(3.dp))
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatWorkDate(timestamp: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(timestamp))

// ─────────────────────────────────────────────────────────────────
//  CATEGORY ROW
// ─────────────────────────────────────────────────────────────────

@Composable
fun WorkCategoryRow(selected: String, onSelect: (String) -> Unit) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(editCategoryOptions) { cat ->
            val isSelected = cat == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) GoldGradient
                        else Brush.horizontalGradient(listOf(CardBackground, CardBackground))
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color.Transparent else Color(0xFFD8D0C4),
                        RoundedCornerShape(50)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) { onSelect(if (isSelected) "" else cat) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    cat,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color      = if (isSelected) TextOnDark else TextSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  DANGER ZONE
// ─────────────────────────────────────────────────────────────────

@Composable
fun WorkDangerZone(onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .width(3.dp).height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(ErrorRed)
            )
            Spacer(Modifier.width(9.dp))
            Text("Danger Zone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ErrorRed.copy(alpha = 0.06f))
                .border(1.dp, ErrorRed.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Delete Submission",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color      = ErrorRed
                    )
                    Text("This action cannot be undone.", fontSize = 12.sp, color = TextSecondary)
                }
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(11.dp))
                        .background(ErrorRed.copy(alpha = 0.12f))
                        .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(11.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
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
                Text(
                    "Delete this submission?",
                    fontWeight = FontWeight.Black,
                    color      = TextPrimary,
                    fontSize   = 18.sp
                )
            },
            text = {
                Text(
                    "Your work and all associated data will be permanently removed.",
                    color      = TextSecondary,
                    fontSize   = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ErrorRed)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { confirmDelete = false; onDelete() }
                        .padding(horizontal = 22.dp, vertical = 11.dp)
                ) {
                    Text(
                        "Yes, delete",
                        color      = TextOnDark,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            },
            dismissButton = {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundSecondary)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null
                        ) { confirmDelete = false }
                        .padding(horizontal = 22.dp, vertical = 11.dp)
                ) {
                    Text(
                        "Cancel",
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  EDIT TOP BAR
// ─────────────────────────────────────────────────────────────────

@Composable
fun EditTopBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGradient)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, null,
                    tint     = TextOnDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "EDIT SUBMISSION",
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 3.sp,
                color         = GoldAccent
            )
            Text(
                title,
                fontSize      = 16.sp,
                fontWeight    = FontWeight.Black,
                color         = TextOnDark,
                letterSpacing = (-0.3).sp,
                maxLines      = 1,
                overflow      = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                "Draft",
                fontSize   = 11.sp,
                color      = TextOnDark.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  COVER IMAGE ZONE
//  Updated: shows upload spinner overlay, reflects Cloudinary upload
// ─────────────────────────────────────────────────────────────────

@Composable
fun CoverImageZone(
    imageUrl         : String,
    localUri         : Uri?,
    isUploadingImage : Boolean,
    onGalleryClick   : () -> Unit,
    onFileClick      : () -> Unit,
    onClear          : () -> Unit
) {
    // While uploading → show local preview; once done → show remote Cloudinary URL
    val displayModel: Any? = when {
        localUri != null && isUploadingImage -> localUri
        imageUrl.isNotEmpty()               -> imageUrl
        localUri != null                    -> localUri
        else                                -> null
    }

    Box(modifier = Modifier.fillMaxWidth().height(240.dp).background(DarkGradient)) {
        // Decorative circle
        Box(
            Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-20).dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.07f))
        )

        if (displayModel != null) {
            AsyncImage(
                model              = displayModel,
                contentDescription = "Cover image",
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
            // Gradient scrim
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, DarkSurface.copy(alpha = 0.55f))
                    )
                )
            )

            // Uploading spinner overlay
            if (isUploadingImage) {
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color       = GoldAccent,
                            modifier    = Modifier.size(32.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            "Uploading to Cloudinary…",
                            color      = TextOnDark,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Clear button
            IconButton(
                onClick  = onClear,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DarkSurface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, null, tint = TextOnDark, modifier = Modifier.size(17.dp))
                }
            }

            // Status label bottom-left
            if (!isUploadingImage) {
                Text(
                    text     = if (imageUrl.isNotEmpty()) "Image uploaded ✓" else "Image attached",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color    = GoldAccent,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                )
            }

        } else {
            // Empty state
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GoldPrimary.copy(alpha = 0.14f))
                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate, null,
                        tint     = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Add a cover image", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                Spacer(Modifier.height(4.dp))
                Text("Makes your work stand out", fontSize = 12.sp, color = TextOnDark.copy(alpha = 0.4f))
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .background(GoldGradient)
        )
    }

    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(GoldGradient)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onGalleryClick
                )
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Photo, null, tint = TextOnDark, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text("Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(13.dp))
                .background(CardBackground)
                .border(1.5.dp, Color(0xFFD8D0C4), RoundedCornerShape(13.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onFileClick
                )
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AttachFile, null, tint = TextSecondary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text("Files", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SECTION LABEL
// ─────────────────────────────────────────────────────────────────

@Composable
fun EditSectionLabel(text: String, topPad: Dp = 0.dp) {
    Row(
        modifier          = Modifier.padding(
            start  = 20.dp,
            end    = 20.dp,
            top    = topPad,
            bottom = 10.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(3.dp).height(16.dp)
                .clip(RoundedCornerShape(50))
                .background(GoldGradient)
        )
        Spacer(Modifier.width(9.dp))
        Text(
            text,
            fontSize      = 14.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextPrimary,
            letterSpacing = 0.1.sp
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  EDIT TEXT FIELD
// ─────────────────────────────────────────────────────────────────

@Composable
fun EditTextField(
    value        : String,
    onValueChange: (String) -> Unit,
    placeholder  : String,
    singleLine   : Boolean,
    modifier     : Modifier = Modifier,
    minLines     : Int      = 1,
    maxChars     : Int      = Int.MAX_VALUE
) {
    val atLimit = value.length >= maxChars
    Column(modifier = modifier) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .border(
                    1.5.dp,
                    when {
                        atLimit            -> ErrorRed.copy(alpha = 0.6f)
                        value.isNotEmpty() -> GoldPrimary.copy(alpha = 0.55f)
                        else               -> Color(0xFFE0D9CE)
                    },
                    RoundedCornerShape(16.dp)
                )
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value           = value,
                onValueChange   = { if (it.length <= maxChars) onValueChange(it) },
                singleLine      = singleLine,
                minLines        = minLines,
                textStyle       = androidx.compose.ui.text.TextStyle(
                    fontSize   = 15.sp,
                    color      = TextPrimary,
                    lineHeight = 23.sp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                decorationBox   = { inner ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                fontSize   = 15.sp,
                                color      = Color(0xFFBBB3A8),
                                lineHeight = 23.sp
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, end = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                "${value.length} / $maxChars",
                fontSize = 11.sp,
                color    = if (atLimit) ErrorRed else TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  DISCARD DIALOG
// ─────────────────────────────────────────────────────────────────

@Composable
fun DiscardDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = CardBackground,
        shape            = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Discard changes?",
                fontWeight = FontWeight.Black,
                color      = TextPrimary,
                fontSize   = 18.sp
            )
        },
        text = {
            Text(
                "You have unsaved changes. If you go back, your edits will be lost.",
                color      = TextSecondary,
                fontSize   = 14.sp,
                lineHeight = 21.sp
            )
        },
        confirmButton = {
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCard)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onConfirm
                    )
                    .padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Text("Discard", color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldGradient)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onDismiss
                    )
                    .padding(horizontal = 22.dp, vertical = 11.dp)
            ) {
                Text("Keep editing", color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────
//  SAVE BAR
// ─────────────────────────────────────────────────────────────────

@Composable
fun SaveBar(
    isSaving    : Boolean,
    saveSuccess : Boolean,
    onSave      : () -> Unit,
    modifier    : Modifier = Modifier
) {
    Surface(color = Color.Transparent, modifier = modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(BackgroundMain.copy(alpha = 0f), BackgroundMain, BackgroundMain)
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 30.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (saveSuccess) Brush.verticalGradient(
                            listOf(SoftGreen.copy(alpha = 0.2f), BackgroundMain)
                        ) else GoldGradient
                    )
                    .border(
                        1.dp,
                        if (saveSuccess) SoftGreen.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        enabled           = !isSaving
                    ) { onSave() },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState  = when {
                        isSaving    -> "saving"
                        saveSuccess -> "success"
                        else        -> "idle"
                    },
                    transitionSpec = { fadeIn(tween(250)).togetherWith(fadeOut(tween(200))) },
                    label          = "save_state"
                ) { state ->
                    when (state) {
                        "saving"  -> CircularProgressIndicator(
                            color       = TextOnDark,
                            modifier    = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        "success" -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint     = SoftGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Changes Saved!",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = SoftGreen
                            )
                        }
                        else -> Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Save, null,
                                tint     = TextOnDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Save Changes",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextOnDark
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
fun EditProjectScreenPreview() {
    EditProjectScreen(navController = rememberNavController(), workId = null)
}