package com.kennedy.forge.ui.screens.feedback

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_FeedbackDashboard
import com.kennedy.forge.navigation.ROUT_Profile
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
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY CONFIG  🔁 Replace with your real credentials
//  Create a free unsigned upload preset:
//  Cloudinary Dashboard → Settings → Upload → Upload presets → Add preset → Signing mode: Unsigned
// ─────────────────────────────────────────────────────────────────
private const val CLOUDINARY_CLOUD_NAME    = "dv4sidtxo"       // e.g. "dv4sidtxo"
private const val CLOUDINARY_UPLOAD_PRESET = "submit_work"    // e.g. "forge_works"
private const val CLOUDINARY_UPLOAD_URL    =
    "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"

// ─────────────────────────────────────────────────────────────────
//  DATA MODEL — saved to /works/{uid}/{workId}
// ─────────────────────────────────────────────────────────────────
data class Work(
    val id: String          = "",
    val uid: String         = "",
    val title: String       = "",
    val description: String = "",
    val category: String    = "",
    val imageUrl: String    = "",   // Cloudinary HTTPS URL
    val fileName: String    = "",   // if a non-image file was attached
    val status: String      = "pending",  // pending | reviewed | closed
    val createdAt: Long     = System.currentTimeMillis(),
    val updatedAt: Long     = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY UPLOAD HELPER  (blocking — call from Dispatchers.IO)
// ─────────────────────────────────────────────────────────────────
private fun uploadToCloudinary(context: Context, uri: Uri): String? = runCatching {
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

    if (!response.isSuccessful) {
        // Log error body for debugging
        println("Cloudinary error ${response.code}: $body")
        return@runCatching null
    }

    JSONObject(body).optString("secure_url").takeIf { it.isNotBlank() }
}.getOrElse { e -> println("Cloudinary exception: ${e.message}"); null }

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPERS
// ─────────────────────────────────────────────────────────────────

/** Create a new work entry — push() generates a unique key */
private suspend fun createWork(work: Work): Result<String> = runCatching {
    val ref = Firebase.database.reference
        .child("works")
        .child(work.uid)
        .push()
    val id  = ref.key ?: error("Failed to generate work ID")
    ref.setValue(work.copy(id = id)).await()
    id
}

/** Read all works for current user */
private suspend fun loadWorks(uid: String): List<Work> = runCatching {
    val snap = Firebase.database.reference
        .child("works").child(uid).get().await()
    snap.children
        .mapNotNull { it.getValue(Work::class.java) }
        .sortedByDescending { it.createdAt }
}.getOrElse { emptyList() }

/** Update an existing work */
private suspend fun updateWork(work: Work): Result<Unit> = runCatching {
    Firebase.database.reference
        .child("works").child(work.uid).child(work.id)
        .setValue(work.copy(updatedAt = System.currentTimeMillis()))
        .await()
}

/** Delete a work entry */
private suspend fun deleteWork(uid: String, workId: String): Result<Unit> = runCatching {
    Firebase.database.reference
        .child("works").child(uid).child(workId)
        .removeValue().await()
}

// ─────────────────────────────────────────────────────────────────
//  COLOUR TOKENS  (kept local — identical to original)
// ─────────────────────────────────────────────────────────────────
private val BackgroundMain      = Color(0xFFF5F2EC)
private val BackgroundSecondary = Color(0xFFEDE7DD)
private val CardBackground      = Color(0xFFFFFFFF)
private val DarkSurface         = Color(0xFF121212)
private val DarkCard            = Color(0xFF1C1C1C)
private val GoldPrimary         = Color(0xFFC89B3C)
private val GoldAccent          = Color(0xFFE6B85C)
private val GoldDeep            = Color(0xFFA67C2E)
private val SoftGreen           = Color(0xFF7FBF9F)
private val SoftBlue            = Color(0xFF7DAED3)
private val SoftPeach           = Color(0xFFE8A87C)
private val TextPrimary         = Color(0xFF1A1A1A)
private val TextSecondary       = Color(0xFF6F6F6F)
private val TextOnDark          = Color(0xFFFFFFFF)
private val TextGold            = Color(0xFFC89B3C)
private val ErrorColor          = Color(0xFFE53935)

private val GoldGradient = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
private val DarkGradient = Brush.verticalGradient(listOf(Color(0xFF1E1A15), DarkSurface))

private val categories = listOf("Design", "Writing", "Code", "Art", "Music", "Other")

// ─────────────────────────────────────────────────────────────────
//  SCREEN
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitWorkScreen(navController: NavController) {

    val context   = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val scope     = rememberCoroutineScope()
    val uid       = if (isPreview) "preview_uid" else Firebase.auth.currentUser?.uid ?: ""

    // ── Form state ────────────────────────────────────────────────
    var title            by remember { mutableStateOf("") }
    var description      by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var imageUri         by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }

    // ── Upload / submit state ─────────────────────────────────────
    var uploadedImageUrl  by remember { mutableStateOf("") }
    var isUploadingImage  by remember { mutableStateOf(false) }
    var isSubmitting      by remember { mutableStateOf(false) }
    var uploadError       by remember { mutableStateOf("") }
    var submitError       by remember { mutableStateOf("") }
    var submitSuccess     by remember { mutableStateOf(false) }

    // ── My Works list (loaded from DB) ────────────────────────────
    var myWorks           by remember { mutableStateOf<List<Work>>(emptyList()) }
    var isLoadingWorks    by remember { mutableStateOf(false) }
    var editingWork       by remember { mutableStateOf<Work?>(null) }  // null = create mode
    var showDeleteConfirm by remember { mutableStateOf<Work?>(null) }

    // ── Load works on first composition ──────────────────────────
    LaunchedEffect(uid) {
        if (uid.isEmpty() || isPreview) return@LaunchedEffect
        isLoadingWorks = true
        myWorks = loadWorks(uid)
        isLoadingWorks = false
    }

    // ── Reset form when editingWork changes ───────────────────────
    LaunchedEffect(editingWork) {
        val w = editingWork
        if (w != null) {
            title            = w.title
            description      = w.description
            selectedCategory = w.category.ifEmpty { null }
            uploadedImageUrl = w.imageUrl
            imageUri         = null
            attachedFileName = w.fileName.ifEmpty { null }
            uploadError      = ""
            submitError      = ""
            submitSuccess    = false
        } else {
            // New submission — clear form
            title            = ""
            description      = ""
            selectedCategory = null
            imageUri         = null
            attachedFileName = null
            uploadedImageUrl = ""
            uploadError      = ""
            submitError      = ""
            submitSuccess    = false
        }
    }

    // ── Launchers ─────────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri         = it
            attachedFileName = null
            uploadedImageUrl = ""
            uploadError      = ""

            // Auto-upload to Cloudinary immediately after pick
            isUploadingImage = true
            scope.launch {
                val url = withContext(Dispatchers.IO) { uploadToCloudinary(context, it) }
                withContext(Dispatchers.Main) {
                    isUploadingImage = false
                    if (url != null) {
                        uploadedImageUrl = url
                    } else {
                        uploadError  = "Upload failed — check your Cloudinary preset and internet connection."
                        imageUri     = null   // clear local preview on failure
                    }
                }
            }
        }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            attachedFileName = it.lastPathSegment ?: "Attached file"
            imageUri         = null
            uploadedImageUrl = ""
            uploadError      = ""
            // Note: non-image files are stored by name only (no Cloudinary upload)
            // Extend here with a storage solution for arbitrary files if needed.
        }
    }

    // canSubmit — must have title, description, and image upload must be complete (not in progress)
    val canSubmit = title.isNotBlank() && description.isNotBlank() && !isUploadingImage && !isSubmitting

    // ── Submit handler ────────────────────────────────────────────
    fun handleSubmit() {
        if (!canSubmit) return
        if (uid.isEmpty()) { submitError = "You must be signed in to submit."; return }
        isSubmitting = true
        submitError  = ""
        submitSuccess = false

        scope.launch {
            val work = Work(
                uid         = uid,
                title       = title.trim(),
                description = description.trim(),
                category    = selectedCategory ?: "",
                imageUrl    = uploadedImageUrl,
                fileName    = attachedFileName ?: "",
                status      = "pending"
            )

            val result = if (editingWork != null) {
                updateWork(work.copy(id = editingWork!!.id, createdAt = editingWork!!.createdAt))
            } else {
                createWork(work).map { }
            }

            withContext(Dispatchers.Main) {
                isSubmitting = false
                result.fold(
                    onSuccess = {
                        submitSuccess = true
                        editingWork   = null
                        // Refresh works list
                        myWorks = loadWorks(uid)
                        // Navigate to feedback dashboard after short delay
                        scope.launch {
                            kotlinx.coroutines.delay(800)
                            navController.navigate(ROUT_FeedbackDashboard) {
                                popUpTo("submit_work") { inclusive = false }
                            }
                        }
                    },
                    onFailure = { e ->
                        submitError = e.message ?: "Submission failed. Please try again."
                    }
                )
            }
        }
    }

    // ── Delete handler ────────────────────────────────────────────
    fun handleDelete(work: Work) {
        scope.launch {
            deleteWork(uid, work.id).fold(
                onSuccess = { myWorks = myWorks.filter { it.id != work.id } },
                onFailure = { e -> submitError = e.message ?: "Delete failed." }
            )
        }
    }

    // ── UI ────────────────────────────────────────────────────────
    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = TextOnDark, modifier = Modifier.size(20.dp))
                    }
                }

                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (editingWork != null) "EDITING WORK" else "SUBMIT WORK",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp, color = GoldAccent
                    )
                    Text(
                        text = if (editingWork != null) "Update your work" else "Get Feedback",
                        fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextOnDark, letterSpacing = (-0.3).sp
                    )
                }

                // Edit mode: cancel button top-right
                if (editingWork != null) {
                    IconButton(
                        onClick = { editingWork = null },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(CircleShape)
                                .background(ErrorColor.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, "Cancel edit", tint = ErrorColor, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Media upload zone ─────────────────────────────────
            item {
                MediaUploadZone(
                    imageUri          = imageUri,
                    remoteImageUrl    = uploadedImageUrl,
                    attachedFileName  = attachedFileName,
                    isUploadingImage  = isUploadingImage,
                    onGalleryClick    = { galleryLauncher.launch("image/*") },
                    onFileClick       = { fileLauncher.launch("*/*") },
                    onClear           = {
                        imageUri         = null
                        attachedFileName = null
                        uploadedImageUrl = ""
                        uploadError      = ""
                    }
                )
            }

            // Upload error banner
            if (uploadError.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp)).background(ErrorColor.copy(0.08f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(uploadError, color = ErrorColor, fontSize = 12.sp)
                    }
                }
            }

            // ── Upload action row ─────────────────────────────────
            item {
                UploadActionRow(
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onFileClick    = { fileLauncher.launch("*/*") }
                )
            }

            // ── Work Details ──────────────────────────────────────
            item { SectionLabel(text = "Work Details", topPad = 24.dp) }

            item {
                StyledTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = "Give your work a title",
                    label = "Title", singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
            }

            item {
                Spacer(Modifier.height(14.dp))
                StyledTextField(
                    value = description, onValueChange = { description = it },
                    placeholder = "Describe your work, your process, or what feedback you're looking for…",
                    label = "Description", singleLine = false, minLines = 5,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                )
            }

            // ── Category ─────────────────────────────────────────
            item { SectionLabel(text = "Category", topPad = 24.dp) }

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
                                .background(if (selected) GoldGradient else Brush.horizontalGradient(listOf(CardBackground, CardBackground)))
                                .border(1.5.dp, if (selected) Color.Transparent else Color(0xFFD8D0C4), RoundedCornerShape(50))
                                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                                    selectedCategory = if (selected) null else cat
                                }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(cat, fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) TextOnDark else TextSecondary, letterSpacing = 0.2.sp)
                        }
                    }
                }
            }

            // ── Submit error / success ────────────────────────────
            if (submitError.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp)).background(ErrorColor.copy(0.08f)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(submitError, color = ErrorColor, fontSize = 12.sp)
                    }
                }
            }

            if (submitSuccess) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp)).background(SoftGreen.copy(0.12f)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (editingWork != null) "Work updated! Redirecting…" else "Submitted! Redirecting to dashboard…",
                            color = SoftGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Submit button ─────────────────────────────────────
            item {
                Spacer(Modifier.height(36.dp))
                SubmitButton(
                    enabled     = canSubmit,
                    isSubmitting = isSubmitting,
                    isEditMode  = editingWork != null,
                    onClick     = { handleSubmit() }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (editingWork != null)
                        "Changes will be saved to your submission"
                    else
                        "Your work is reviewed by mentors within 24 hours",
                    fontSize = 11.sp, color = TextSecondary,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }

            // ── MY WORKS LIST ─────────────────────────────────────
            item { SectionLabel(text = "My Submissions", topPad = 32.dp) }

            if (isLoadingWorks) {
                item {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GoldPrimary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }
            } else if (myWorks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp)).background(CardBackground)
                            .border(1.dp, Color(0xFFE0D9CE), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No submissions yet.\nSubmit your first work above!",
                            color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    }
                }
            } else {
                items(myWorks) { work ->
                    Spacer(Modifier.height(10.dp))
                    WorkCard(
                        work      = work,
                        onEdit    = { editingWork = it },
                        onDelete  = { showDeleteConfirm = it },
                        onView    = { navController.navigate("feedback_view/${it.id}") }
                    )
                }
            }
        }

        // ── Delete confirmation dialog ─────────────────────────────
        showDeleteConfirm?.let { work ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = null },
                title   = { Text("Delete submission?", fontWeight = FontWeight.Bold) },
                text    = { Text("\"${work.title}\" will be permanently removed. This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        handleDelete(work)
                        showDeleteConfirm = null
                    }) {
                        Text("Delete", color = ErrorColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = CardBackground
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  WORK CARD  (read + edit + delete)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun WorkCard(
    work: Work,
    onEdit: (Work) -> Unit,
    onDelete: (Work) -> Unit,
    onView: (Work) -> Unit
) {
    val statusColor = when (work.status) {
        "reviewed" -> SoftGreen
        "closed"   -> SoftBlue
        else       -> SoftPeach
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp)).background(CardBackground)
            .border(1.dp, Color(0xFFE0D9CE), RoundedCornerShape(16.dp))
            .clickable { onView(work) }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {

            // Image thumbnail
            Box(
                modifier = Modifier.size(width = 90.dp, height = 90.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(DarkCard)
            ) {
                if (work.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = work.imageUrl, contentDescription = work.title,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            if (work.fileName.isNotEmpty()) Icons.Default.AttachFile else Icons.Default.Image,
                            null, tint = Color(0xFF3A3A3A), modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f).padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(work.title.ifEmpty { "Untitled" }, color = TextPrimary,
                        fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        modifier = Modifier.weight(1f))

                    // Status chip
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(0.12f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(work.status.replaceFirstChar { it.uppercase() },
                            color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (work.category.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(work.category, color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                if (work.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(work.description, color = TextSecondary, fontSize = 11.sp,
                        maxLines = 2, lineHeight = 15.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                Spacer(Modifier.height(8.dp))

                // Edit / Delete row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallActionChip(Icons.Default.Edit, "Edit", SoftBlue) { onEdit(work) }
                    SmallActionChip(Icons.Default.Delete, "Delete", ErrorColor) { onDelete(work) }
                    SmallActionChip(Icons.Default.Visibility, "View", GoldPrimary) { onView(work) }
                }
            }
        }
    }
}

@Composable
private fun SmallActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, color: Color, onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(7.dp))
            .background(color.copy(0.10f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
            Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  MEDIA UPLOAD ZONE  (updated: shows Cloudinary URL image + upload spinner)
// ─────────────────────────────────────────────────────────────────

@Composable
fun MediaUploadZone(
    imageUri: Uri?,
    remoteImageUrl: String,
    attachedFileName: String?,
    isUploadingImage: Boolean,
    onGalleryClick: () -> Unit,
    onFileClick: () -> Unit,
    onClear: () -> Unit
) {
    // What to display — local URI while uploading, remote URL once done
    val displayModel: Any? = when {
        imageUri != null && isUploadingImage -> imageUri         // local preview during upload
        remoteImageUrl.isNotEmpty()          -> remoteImageUrl  // Cloudinary URL after upload
        imageUri != null                     -> imageUri         // fallback local
        else                                 -> null
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp).background(DarkGradient)
    ) {
        when {
            displayModel != null -> {
                AsyncImage(
                    model = displayModel, contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
                // Gradient scrim
                Box(modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, DarkSurface.copy(alpha = 0.5f)))))

                // Uploading overlay spinner
                if (isUploadingImage) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.55f)),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(32.dp), strokeWidth = 2.5.dp)
                            Text("Uploading to Cloudinary…", color = TextOnDark, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Clear button
                IconButton(onClick = onClear, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                    Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(DarkSurface.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
                    }
                }

                // Label
                if (!isUploadingImage) {
                    Text(
                        text = if (remoteImageUrl.isNotEmpty()) "Image uploaded ✓" else "Image attached",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GoldAccent,
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    )
                }
            }

            attachedFileName != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                        .background(GoldPrimary.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AttachFile, null, tint = GoldAccent, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(attachedFileName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        color = TextOnDark, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                    Spacer(Modifier.height(6.dp))
                    Text("File attached ✓", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.Medium)
                }
                IconButton(onClick = onClear, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
                    Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(0.1f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
                    }
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onGalleryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(22.dp))
                                .background(GoldPrimary.copy(0.15f))
                                .border(1.5.dp, GoldPrimary.copy(0.35f), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, "Upload media",
                                tint = GoldAccent, modifier = Modifier.size(34.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text("Tap to add a photo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                        Spacer(Modifier.height(4.dp))
                        Text("or use the buttons below to browse files", fontSize = 12.sp, color = TextOnDark.copy(0.45f))
                    }
                }
            }
        }

        // Gold bottom rule
        Box(modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomCenter).background(GoldGradient))
    }
}

// ─────────────────────────────────────────────────────────────────
//  UPLOAD ACTION ROW  (unchanged UI)
// ─────────────────────────────────────────────────────────────────

@Composable
fun UploadActionRow(onGalleryClick: () -> Unit, onFileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(GoldGradient)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onGalleryClick)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Photo, null, tint = TextOnDark, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Gallery", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
        }

        Row(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(CardBackground)
                .border(1.5.dp, Color(0xFFD8D0C4), RoundedCornerShape(14.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onFileClick)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AttachFile, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Files", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SECTION LABEL  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, topPad: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = topPad, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(50)).background(GoldGradient))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = 0.1.sp)
    }
}

// ─────────────────────────────────────────────────────────────────
//  STYLED TEXT FIELD  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun StyledTextField(
    value: String, onValueChange: (String) -> Unit,
    placeholder: String, label: String,
    singleLine: Boolean, modifier: Modifier = Modifier, minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            color = if (value.isNotEmpty()) TextGold else TextSecondary,
            letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 6.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(CardBackground)
                .border(1.5.dp,
                    if (value.isNotEmpty()) GoldPrimary.copy(0.6f) else Color(0xFFE0D9CE),
                    RoundedCornerShape(16.dp))
        ) {
            BasicTextField(
                value = value, onValueChange = onValueChange,
                singleLine = singleLine, minLines = minLines,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = TextPrimary, lineHeight = 22.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { inner ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        if (value.isEmpty()) Text(placeholder, fontSize = 15.sp, color = Color(0xFFBBB3A8), lineHeight = 22.sp)
                        inner()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SUBMIT BUTTON  (updated: loading state + edit mode label)
// ─────────────────────────────────────────────────────────────────

@Composable
fun SubmitButton(enabled: Boolean, isSubmitting: Boolean, isEditMode: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled) GoldGradient
                else Brush.horizontalGradient(listOf(BackgroundSecondary, BackgroundSecondary))
            )
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = null, enabled = enabled && !isSubmitting) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(targetState = isSubmitting, label = "submit_btn") { submitting ->
            if (submitting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = TextOnDark, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(if (isEditMode) "Updating…" else "Submitting…",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isEditMode) Icons.Default.Save else Icons.Default.Send,
                        null, tint = if (enabled) TextOnDark else TextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (isEditMode) "Update Work" else "Submit for Feedback",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        color = if (enabled) TextOnDark else TextSecondary, letterSpacing = 0.2.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  BOTTOM NAV  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun BottomNavBar(navController: NavController) {
    Surface(color = CardBackground, shadowElevation = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(Icons.Default.Home,   "Home",   false, Modifier.weight(1f)) {
                navController.navigate(ROUT_Dashboard) { popUpTo("dashboard") { inclusive = true } }
            }
            NavItem(Icons.Default.Add,    "Submit", true,  Modifier.weight(1f)) {}
            NavItem(Icons.Default.Person, "Profile", false, Modifier.weight(1f)) {
                navController.navigate(ROUT_Profile)
            }
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String, selected: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxHeight()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Box(modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(GoldGradient),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = TextOnDark, modifier = Modifier.size(22.dp))
            }
        } else {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  BASIC TEXT FIELD ALIAS  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun BasicTextField(
    value: String, onValueChange: (String) -> Unit,
    singleLine: Boolean, minLines: Int,
    textStyle: androidx.compose.ui.text.TextStyle,
    keyboardOptions: KeyboardOptions,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value, onValueChange = onValueChange,
        singleLine = singleLine, minLines = minLines,
        textStyle = textStyle, keyboardOptions = keyboardOptions,
        decorationBox = decorationBox, modifier = modifier
    )
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
fun SubmitWorkScreenPreview() {
    SubmitWorkScreen(rememberNavController())
}