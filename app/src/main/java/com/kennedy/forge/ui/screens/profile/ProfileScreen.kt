package com.kennedy.forge.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_SubmitWork
import com.kennedy.forge.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview

// ─────────────────────────────────────────────
//  CLOUDINARY CONFIG
// ─────────────────────────────────────────────

private object CloudinaryConfig {
    const val CLOUD_NAME    = "dv4sidtxo"
    const val UPLOAD_PRESET = "forge_portfolio"
    val UPLOAD_URL get() = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
}

// ─────────────────────────────────────────────
//  MODELS
// ─────────────────────────────────────────────

// ── Work model — mirrors exactly what SubmitWorkScreen saves to /works/{uid}/{id}
data class Work(
    val id: String          = "",
    val uid: String         = "",
    val title: String       = "",
    val description: String = "",
    val category: String    = "",
    val imageUrl: String    = "",
    val fileName: String    = "",
    val status: String      = "pending",
    val createdAt: Long     = System.currentTimeMillis(),
    val updatedAt: Long     = System.currentTimeMillis()
)

data class Achievement(
    val icon: ImageVector,
    val label: String,
    val color: Color
)

data class UserProfile(
    @JvmField val uid:        String = "",
    @JvmField val name:       String = "",
    @JvmField val profession: String = "",
    @JvmField val category:   String = "",
    @JvmField val bio:        String = "",
    @JvmField val avatarUrl:  String = "",
    @JvmField val updatedAt:  Long   = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
//  DEMO ACHIEVEMENTS  (static — not from DB)
// ─────────────────────────────────────────────

val demoAchievements = listOf(
    Achievement(Icons.Default.Star,        "Top Creator", GoldPrimary),
    Achievement(Icons.Default.Favorite,    "100+ Likes",  SoftPeach),
    Achievement(Icons.Default.CheckCircle, "Verified",    SoftGreen)
)

// ─────────────────────────────────────────────
//  FIREBASE HELPERS
// ─────────────────────────────────────────────

/** Reads all works for [uid] from /works/{uid} — same node SubmitWorkScreen writes to */
private suspend fun loadWorksFromDatabase(uid: String): List<Work> = runCatching {
    val snap = Firebase.database.reference
        .child("works").child(uid).get().await()
    snap.children
        .mapNotNull { it.getValue(Work::class.java) }
        .sortedByDescending { it.createdAt }
}.getOrElse { emptyList() }

suspend fun saveProfileToDatabase(profile: UserProfile): Result<Unit> = runCatching {
    val fresh = profile.copy(updatedAt = System.currentTimeMillis())
    Firebase.database.reference
        .child("users").child(fresh.uid).child("profile")
        .setValue(fresh).await()
}

suspend fun loadProfileFromDatabase(uid: String): Result<UserProfile> = runCatching {
    val snap = Firebase.database.reference
        .child("users").child(uid).child("profile")
        .get().await()
    snap.getValue(UserProfile::class.java) ?: UserProfile(uid = uid)
}

// ─────────────────────────────────────────────
//  NETWORK HELPERS
// ─────────────────────────────────────────────

suspend fun uploadAvatarToCloudinary(context: Context, imageUri: Uri): Result<String> =
    withContext(Dispatchers.IO) {
        runCatching {
            val tempFile = File(context.cacheDir, "upload_avatar_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            } ?: error("Could not open image stream")

            val client      = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, tempFile.asRequestBody("image/*".toMediaType()))
                .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
                .addFormDataPart("folder", "forge/avatars")
                .build()

            val request  = Request.Builder().url(CloudinaryConfig.UPLOAD_URL).post(requestBody).build()
            val response = client.newCall(request).execute()
            val body     = response.body?.string() ?: error("Empty response from Cloudinary")
            if (!response.isSuccessful) error("Cloudinary upload failed (${response.code}): $body")
            JSONObject(body).getString("secure_url")
        }
    }

// ─────────────────────────────────────────────
//  PROFILE SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // ── Profile state ─────────────────────────────────────────────
    var profile   by remember { mutableStateOf(UserProfile()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf("") }
    var avatarRevision by remember { mutableIntStateOf(0) }

    // ── Works state — loaded live from Firebase ───────────────────
    var userWorks      by remember { mutableStateOf<List<Work>>(emptyList()) }
    var isLoadingWorks by remember { mutableStateOf(true) }

    // ── Edit-sheet state ──────────────────────────────────────────
    var showUpdateSheet    by remember { mutableStateOf(false) }
    var editName           by remember { mutableStateOf("") }
    var editProfession     by remember { mutableStateOf("") }
    var editCategory       by remember { mutableStateOf("") }
    var editBio            by remember { mutableStateOf("") }
    var editAvatarUri      by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPicker    by remember { mutableStateOf(false) }
    var isSaving           by remember { mutableStateOf(false) }
    var saveError          by remember { mutableStateOf("") }
    var showValidationHint by remember { mutableStateOf(false) }

    val isFormValid = editName.isNotBlank() && editProfession.isNotBlank()

    // Computed stats from live works
    val totalLikes = userWorks.size * 7   // placeholder multiplier; replace with real likes field if added

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) editAvatarUri = cameraUri }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { editAvatarUri = it } }

    fun launchCamera() {
        val file = File(context.cacheDir, "forge_avatar_${System.currentTimeMillis()}.jpg")
        val uri  = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    // ── Load profile + works on first composition ─────────────────
    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
            ?: run {
                kotlinx.coroutines.delay(500)
                Firebase.auth.currentUser?.uid
            }

        if (uid == null) {
            navController.navigate(ROUT_Dashboard) {
                popUpTo(ROUT_Dashboard) { inclusive = false }
                launchSingleTop = true
            }
            isLoading      = false
            isLoadingWorks = false
            return@LaunchedEffect
        }

        // Load profile
        loadProfileFromDatabase(uid).fold(
            onSuccess = { loaded -> profile = loaded },
            onFailure = { e -> loadError = e.message ?: "Failed to load profile" }
        )
        isLoading = false

        // Load works from the same /works/{uid} path SubmitWorkScreen uses
        userWorks      = loadWorksFromDatabase(uid)
        isLoadingWorks = false
    }

    // ── Refresh works when returning to this screen ───────────────
    // This ensures new submissions from SubmitWorkScreen appear immediately
    LaunchedEffect(navController) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        // Re-fetch every time this composable enters composition after navigation
        userWorks = loadWorksFromDatabase(uid)
        isLoadingWorks = false
    }

    fun openUpdateSheet() {
        editName           = profile.name
        editProfession     = profile.profession
        editCategory       = profile.category
        editBio            = profile.bio
        editAvatarUri      = null
        saveError          = ""
        showValidationHint = false
        showUpdateSheet    = true
    }

    fun handleSave() {
        if (!isFormValid) { showValidationHint = true; return }
        isSaving  = true
        saveError = ""

        scope.launch {
            val uid = Firebase.auth.currentUser?.uid
            if (uid == null) {
                withContext(Dispatchers.Main) { isSaving = false; saveError = "Session expired." }
                return@launch
            }

            val avatarUrl: String = when {
                editAvatarUri != null -> {
                    uploadAvatarToCloudinary(context, editAvatarUri!!).getOrElse { e ->
                        withContext(Dispatchers.Main) {
                            isSaving  = false
                            saveError = "Photo upload failed: ${e.message}"
                        }
                        return@launch
                    }
                }
                else -> profile.avatarUrl
            }

            val updated = UserProfile(
                uid        = uid,
                name       = editName.trim(),
                profession = editProfession.trim(),
                category   = editCategory.trim(),
                bio        = editBio.trim(),
                avatarUrl  = avatarUrl
            )

            saveProfileToDatabase(updated).fold(
                onSuccess = {
                    withContext(Dispatchers.Main) {
                        profile         = updated
                        avatarRevision += 1
                        isSaving        = false
                        showUpdateSheet = false
                    }
                },
                onFailure = { e ->
                    withContext(Dispatchers.Main) {
                        isSaving  = false
                        saveError = e.message ?: "Failed to save. Try again."
                    }
                }
            )
        }
    }

    // ─────────────────────────────────────────
    //  SCAFFOLD
    // ─────────────────────────────────────────
    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("My Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary)
                            .clickable { navController.navigate(ROUT_SubmitWork) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Add Work", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp, color = BackgroundMain, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 32.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon    = Icons.Default.Home,
                        label   = "Home",
                        tint    = TextSecondary,
                        onClick = {
                            navController.navigate(ROUT_Dashboard) {
                                popUpTo(ROUT_Dashboard) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    BottomNavItem(
                        icon    = Icons.Default.Add,
                        label   = "Submit",
                        tint    = TextSecondary,
                        onClick = { navController.navigate(ROUT_SubmitWork) }
                    )
                    BottomNavItem(
                        icon    = Icons.Default.ManageAccounts,
                        label   = "Edit Profile",
                        tint    = GoldPrimary,
                        onClick = { openUpdateSheet() },
                        badge   = true
                    )
                }
            }
        }
    ) { padding ->

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldPrimary)
                }
            }
            loadError.isNotEmpty() -> {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(loadError, color = TextSecondary, textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { HeroSection(profile = profile, avatarRevision = avatarRevision) }

                    // Stats driven by live works count
                    item {
                        StatsRow(
                            projects = userWorks.size,
                            likes    = totalLikes,
                            reviews  = userWorks.count { it.status == "reviewed" }
                        )
                    }

                    item { AchievementsRow(achievements = demoAchievements) }

                    item {
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Your Work", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)

                            if (isLoadingWorks) {
                                CircularProgressIndicator(
                                    color       = GoldPrimary,
                                    strokeWidth = 2.dp,
                                    modifier    = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    "${userWorks.size} submission${if (userWorks.size != 1) "s" else ""}",
                                    color    = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    if (isLoadingWorks) {
                        item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = GoldPrimary)
                            }
                        }
                    } else if (userWorks.isEmpty()) {
                        item {
                            EmptyProjectsPlaceholder(onAdd = { navController.navigate(ROUT_SubmitWork) })
                        }
                    } else {
                        // ── Live Work cards from Firebase ─────────
                        items(userWorks, key = { it.id.ifEmpty { it.createdAt.toString() } }) { work ->
                            WorkCard(
                                work     = work,
                                onEdit   = { navController.navigate(ROUT_SubmitWork) },  // navigate to submit screen in edit mode
                                onView   = { navController.navigate("feedback_dashboard") }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Bottom sheets ─────────────────────────────────────────────
    if (showUpdateSheet) {
        UpdateProfileSheet(
            name               = editName,
            profession         = editProfession,
            category           = editCategory,
            bio                = editBio,
            avatarUri          = editAvatarUri,
            currentAvatarUrl   = profile.avatarUrl,
            avatarRevision     = avatarRevision,
            isSaving           = isSaving,
            saveError          = saveError,
            showValidationHint = showValidationHint,
            isFormValid        = isFormValid,
            onNameChange       = { editName = it; showValidationHint = false; saveError = "" },
            onProfChange       = { editProfession = it; showValidationHint = false; saveError = "" },
            onCatChange        = { editCategory = it },
            onBioChange        = { editBio = it },
            onPickPhoto        = { showPhotoPicker = true },
            onSave             = { handleSave() },
            onDismiss          = { showUpdateSheet = false }
        )
    }

    if (showPhotoPicker) {
        PhotoPickerSheet(
            onDismiss = { showPhotoPicker = false },
            onCamera  = { showPhotoPicker = false; launchCamera() },
            onGallery = { showPhotoPicker = false; galleryLauncher.launch("image/*") },
            onRemove  = if (editAvatarUri != null) {
                { editAvatarUri = null; showPhotoPicker = false }
            } else null
        )
    }
}

// ─────────────────────────────────────────────
//  LIVE WORK CARD  (replaces hardcoded ProjectCard)
//  Reads directly from the Work model saved by SubmitWorkScreen
// ─────────────────────────────────────────────

@Composable
fun WorkCard(
    work: Work,
    onEdit: () -> Unit,
    onView: () -> Unit
) {
    val statusColor = when (work.status) {
        "reviewed" -> SoftGreen
        "closed"   -> SoftBlue
        else       -> SoftPeach
    }

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable(onClick = onView),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // ── Image area ────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                if (work.imageUrl.isNotEmpty()) {
                    // Cloudinary image loaded via Coil
                    AsyncImage(
                        model              = work.imageUrl,
                        contentDescription = work.title,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)))
                        )
                    )
                } else {
                    // No image — gradient placeholder
                    Box(
                        modifier         = Modifier.fillMaxSize().background(
                            Brush.linearGradient(listOf(Color(0xFF1C1C1C), Color(0xFF121212)))
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (work.fileName.isNotEmpty()) Icons.Default.AttachFile else Icons.Default.Image,
                                null,
                                tint     = Color(0xFF3A3A3A),
                                modifier = Modifier.size(36.dp)
                            )
                            if (work.fileName.isNotEmpty()) {
                                Text(
                                    work.fileName,
                                    color    = Color(0xFF5A5A5A),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }

                // Category chip
                if (work.category.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart).padding(12.dp)
                            .clip(RoundedCornerShape(8.dp)).background(GoldPrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(work.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd).padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(0.85f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        work.status.replaceFirstChar { it.uppercase() },
                        color      = Color.White,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ── Text content ──────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    work.title.ifEmpty { "Untitled" },
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    work.description.ifEmpty { "No description added." },
                    color      = TextSecondary,
                    fontSize   = 13.sp,
                    lineHeight = 19.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Submitted date
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Text(
                            formatRelativeTime(work.createdAt),
                            color    = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Action chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Edit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftBlue.copy(0.10f))
                                .clickable(onClick = onEdit)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, tint = SoftBlue, modifier = Modifier.size(12.dp))
                                Text("Edit", color = SoftBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        // View Feedback
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary.copy(0.10f))
                                .clickable(onClick = onView)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.ChatBubble, null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                                Text("Feedback", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Simple relative time formatter */
private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins  = diff / 60_000
    val hours = diff / 3_600_000
    val days  = diff / 86_400_000
    return when {
        mins  < 1   -> "just now"
        mins  < 60  -> "${mins}m ago"
        hours < 24  -> "${hours}h ago"
        days  < 7   -> "${days}d ago"
        else        -> "${days / 7}w ago"
    }
}

// ─────────────────────────────────────────────
//  BOTTOM NAV ITEM  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun BottomNavItem(
    icon: ImageVector, label: String, tint: Color,
    onClick: () -> Unit, badge: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
            if (badge) {
                Box(modifier = Modifier.size(8.dp).align(Alignment.TopEnd).clip(CircleShape).background(GoldPrimary))
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────
//  HERO SECTION  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun HeroSection(profile: UserProfile, avatarRevision: Int = 0) {
    Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(colors = listOf(GoldPrimary.copy(alpha = 0.15f), BackgroundMain))
        ))
        Box(
            modifier = Modifier.size(180.dp).offset(x = 80.dp, y = (-40).dp).align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(colors = listOf(GoldAccent.copy(alpha = 0.18f), Color.Transparent)),
                    shape = CircleShape
                )
        )
        Column(
            modifier              = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(88.dp).shadow(12.dp, CircleShape).clip(CircleShape)
                    .border(2.dp, GoldPrimary, CircleShape).background(BackgroundSecondary),
                contentAlignment = Alignment.Center
            ) {
                if (profile.avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profile.avatarUrl)
                            .memoryCacheKey("${profile.avatarUrl}_$avatarRevision")
                            .diskCacheKey("${profile.avatarUrl}_$avatarRevision")
                            .crossfade(true).build(),
                        contentDescription = "Avatar",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Icon(Icons.Default.Person, null, tint = GoldPrimary, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(profile.name.ifBlank { "Your Name" }, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            val roleText = buildString {
                append(profile.profession.ifBlank { "Creative" })
                if (profile.category.isNotBlank()) append(" · ${profile.category}")
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(GoldPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(roleText, color = GoldDeep, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                profile.bio.ifBlank { "No bio yet — tap Edit Profile to add one." },
                color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────
//  STATS ROW  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun StatsRow(projects: Int, likes: Int, reviews: Int) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("$projects", "Submissions", Icons.Default.GridView,  GoldPrimary, Modifier.weight(1f))
        StatCard("$likes",    "Likes",       Icons.Default.Favorite,  SoftPeach,   Modifier.weight(1f))
        StatCard("$reviews",  "Reviewed",    Icons.Default.Star,      SoftGreen,   Modifier.weight(1f))
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────
//  ACHIEVEMENTS ROW  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun AchievementsRow(achievements: List<Achievement>) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text("Achievements", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            achievements.forEach { a ->
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                        .background(a.color.copy(alpha = 0.1f))
                        .border(1.dp, a.color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(a.icon, null, tint = a.color, modifier = Modifier.size(14.dp))
                    Text(a.label, color = a.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  EMPTY STATE  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun EmptyProjectsPlaceholder(onAdd: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("No submissions yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Start building your portfolio by submitting your first work.",
            color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onAdd, shape = RoundedCornerShape(12.dp),
            colors  = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Submit First Work", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
//  UPDATE PROFILE SHEET  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun UpdateProfileSheet(
    name: String, profession: String, category: String, bio: String,
    avatarUri: Uri?, currentAvatarUrl: String, avatarRevision: Int = 0,
    isSaving: Boolean, saveError: String, showValidationHint: Boolean, isFormValid: Boolean,
    onNameChange: (String) -> Unit, onProfChange: (String) -> Unit,
    onCatChange:  (String) -> Unit, onBioChange:  (String) -> Unit,
    onPickPhoto: () -> Unit, onSave: () -> Unit, onDismiss: () -> Unit
) {
    val shakeOffset by animateFloatAsState(
        targetValue   = if (showValidationHint) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0; (-8f) at 50; 8f at 100; (-6f) at 150; 6f at 200; (-4f) at 250; 0f at 300
        },
        label = "shake"
    )

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(CardBackground)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            ) {
                Box(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).align(Alignment.CenterHorizontally)
                    .width(40.dp).height(4.dp).clip(CircleShape).background(Color(0xFFDDD8CE)))
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.ManageAccounts, null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Text("Update Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = TextSecondary) }
                }
                HorizontalDivider(color = Color(0xFFEDE7DD), thickness = 0.5.dp)
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SheetAvatarPicker(
                            avatarUri        = avatarUri,
                            currentAvatarUrl = currentAvatarUrl,
                            avatarRevision   = avatarRevision,
                            onPickPhoto      = onPickPhoto
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                    Card(
                        modifier  = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        colors    = CardDefaults.cardColors(containerColor = BackgroundMain),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            SheetInputField("Full Name",        name,       onNameChange, Icons.Outlined.Person,   "e.g. Kennedy Osei",                    isRequired = true, showError = showValidationHint && name.isBlank(),       imeAction = ImeAction.Next)
                            SheetFieldDivider()
                            SheetInputField("Profession",       profession, onProfChange, Icons.Outlined.Work,     "e.g. Designer, Filmmaker, Writer",     isRequired = true, showError = showValidationHint && profession.isBlank(), imeAction = ImeAction.Next)
                            SheetFieldDivider()
                            SheetInputField("Creative Category",category,  onCatChange,  Icons.Outlined.Category, "e.g. UI/UX, Music Production, Fiction",                                                                              imeAction = ImeAction.Next)
                            SheetFieldDivider()
                            SheetInputField("Short Bio",        bio,        onBioChange,  Icons.Outlined.Edit,     "What drives your creative work?",      singleLine = false, imeAction = ImeAction.Done, minLines = 3)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(visible = showValidationHint, enter = fadeIn() + slideInVertically { -8 }, exit = fadeOut()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().offset(x = shakeOffset.dp)
                                .clip(RoundedCornerShape(10.dp)).background(Error.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Info, null, tint = Error, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Name and Profession are required",
                                style = MaterialTheme.typography.bodySmall.copy(color = Error, fontWeight = FontWeight.W500))
                        }
                    }
                    AnimatedVisibility(visible = saveError.isNotEmpty(), enter = fadeIn() + slideInVertically { -8 }, exit = fadeOut()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                .clip(RoundedCornerShape(10.dp)).background(Error.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(saveError, style = MaterialTheme.typography.bodySmall.copy(color = Error))
                        }
                    }
                }
                SheetSaveBar(isFormValid = isFormValid, isSaving = isSaving, hasAvatar = avatarUri != null, onSave = onSave)
            }
        }
    }
}

// ─────────────────────────────────────────────
//  SHEET AVATAR PICKER  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun SheetAvatarPicker(
    avatarUri: Uri?, currentAvatarUrl: String, avatarRevision: Int = 0, onPickPhoto: () -> Unit
) {
    val scaleAnim by animateFloatAsState(
        targetValue   = if (avatarUri != null) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "avatar_scale"
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(scaleAnim)) {
        Box(modifier = Modifier.size(104.dp).clip(CircleShape)
            .background(if (avatarUri != null || currentAvatarUrl.isNotBlank()) GoldGradient
            else Brush.linearGradient(listOf(Color(0xFFDDD8CE), Color(0xFFCCC5B8)))))
        Box(modifier = Modifier.size(97.dp).clip(CircleShape).background(CardBackground))
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(BackgroundSecondary).clickable(onClick = onPickPhoto),
            contentAlignment = Alignment.Center
        ) {
            when {
                avatarUri != null -> AsyncImage(model = avatarUri, contentDescription = "New photo",
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                currentAvatarUrl.isNotBlank() -> AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(currentAvatarUrl)
                        .memoryCacheKey("${currentAvatarUrl}_$avatarRevision")
                        .diskCacheKey("${currentAvatarUrl}_$avatarRevision")
                        .crossfade(true).build(),
                    contentDescription = "Current photo", contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Add photo", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp, letterSpacing = 0.3.sp))
                }
            }
        }
        Box(
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-2).dp, y = (-2).dp)
                .size(26.dp).clip(CircleShape).background(GoldGradient)
                .border(2.dp, CardBackground, CircleShape).clickable(onClick = onPickPhoto),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (avatarUri != null || currentAvatarUrl.isNotBlank()) Icons.Default.Edit else Icons.Default.Add,
                null, tint = DarkSurface, modifier = Modifier.size(12.dp))
        }
    }
}

// ─────────────────────────────────────────────
//  SHEET INPUT FIELD  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun SheetInputField(
    label: String, value: String, onValueChange: (String) -> Unit,
    icon: ImageVector, hint: String,
    singleLine: Boolean = true, imeAction: ImeAction = ImeAction.Next,
    minLines: Int = 1, isRequired: Boolean = false, showError: Boolean = false
) {
    val isFilled   = value.isNotEmpty()
    val iconTint   = when { showError -> Error; isFilled -> GoldPrimary; else -> Color(0xFF9E9E9E) }
    val iconBg     = when { showError -> Error.copy(alpha = 0.08f); isFilled -> GoldPrimary.copy(alpha = 0.10f); else -> BackgroundSecondary }
    val labelColor = when { showError -> Error; isFilled -> GoldPrimary; else -> TextSecondary }

    Column {
        Row(verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.padding(top = 4.dp).size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(color = labelColor, fontWeight = FontWeight.W600, letterSpacing = 0.4.sp, fontSize = 11.sp))
                    if (isRequired) Text(" *", style = MaterialTheme.typography.labelSmall.copy(color = if (showError) Error else GoldPrimary, fontSize = 11.sp))
                }
                TextField(
                    value = value, onValueChange = onValueChange,
                    placeholder = { Text(hint, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBBBBBB), fontSize = 14.sp)) },
                    singleLine = singleLine, minLines = minLines,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = imeAction),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = GoldPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.W400),
                    modifier  = Modifier.fillMaxWidth().padding(top = 2.dp)
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            .height(if (showError) 1.5.dp else 1.dp)
            .background(when { showError -> Brush.linearGradient(listOf(Error, Error.copy(alpha = 0.6f))); isFilled -> GoldGradient; else -> Brush.linearGradient(listOf(Color(0xFFEDE7DD), Color(0xFFEDE7DD))) }))
    }
}

@Composable
fun SheetFieldDivider() {
    HorizontalDivider(color = Color(0xFFEDE7DD), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 10.dp))
}

// ─────────────────────────────────────────────
//  SHEET SAVE BAR  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun SheetSaveBar(isFormValid: Boolean, isSaving: Boolean, hasAvatar: Boolean, onSave: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed   by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label         = "btn_scale"
    )
    Surface(tonalElevation = 4.dp, color = CardBackground, shadowElevation = 8.dp) {
        Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    AnimatedContent(targetState = Triple(isFormValid, isSaving, hasAvatar), label = "save_hint") { (valid, saving, avatar) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            when {
                                saving && avatar -> {
                                    CircularProgressIndicator(color = GoldPrimary, strokeWidth = 1.5.dp, modifier = Modifier.size(10.dp))
                                    Text("Uploading photo…", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontSize = 12.sp))
                                }
                                saving -> {
                                    CircularProgressIndicator(color = GoldPrimary, strokeWidth = 1.5.dp, modifier = Modifier.size(10.dp))
                                    Text("Saving profile…", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontSize = 12.sp))
                                }
                                valid -> {
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(Success))
                                    Text("Ready to save", style = MaterialTheme.typography.labelSmall.copy(color = Success, fontWeight = FontWeight.W500, fontSize = 12.sp))
                                }
                                else -> {
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD8D0C4)))
                                    Text("Fill in Name and Profession", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 12.sp))
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth().height(58.dp).scale(buttonScale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isFormValid && !isSaving) GoldGradient else Brush.linearGradient(listOf(Color(0xFFD8D0C4), Color(0xFFCCC5B8))))
                        .clickable(interactionSource = interactionSource,
                            indication = ripple(color = if (isFormValid) DarkSurface.copy(alpha = 0.15f) else Color.Transparent),
                            enabled = !isSaving, onClick = onSave),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(targetState = isSaving,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }, label = "btn_content") { saving ->
                        if (saving) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(color = DarkSurface, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                                Text(if (hasAvatar) "Uploading & saving…" else "Saving profile…",
                                    style = MaterialTheme.typography.titleSmall.copy(color = DarkSurface, fontWeight = FontWeight.W600))
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text("Save Changes", style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isFormValid) DarkSurface else Color(0xFF9A9A9A),
                                    fontWeight = FontWeight.W700, fontSize = 15.sp))
                                if (isFormValid) {
                                    Spacer(Modifier.width(10.dp))
                                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(DarkSurface.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DarkSurface, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
//  PHOTO PICKER SHEET  (unchanged)
// ─────────────────────────────────────────────

@Composable
fun PhotoPickerSheet(onDismiss: () -> Unit, onCamera: () -> Unit, onGallery: () -> Unit, onRemove: (() -> Unit)?) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CardBackground).padding(bottom = 32.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            ) {
                Box(modifier = Modifier.padding(top = 12.dp, bottom = 20.dp).align(Alignment.CenterHorizontally)
                    .width(40.dp).height(4.dp).clip(CircleShape).background(Color(0xFFDDD8CE)))
                Text("Profile Photo", style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary, fontWeight = FontWeight.W700, fontSize = 17.sp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                Text("Choose how to set your avatar", style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary, fontSize = 13.sp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp))
                HorizontalDivider(color = Color(0xFFEDE7DD), thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))
                PickerOption(Icons.Default.CameraAlt,    "Take a photo",        "Open your camera",   GoldPrimary.copy(alpha = 0.10f), GoldPrimary, onCamera)
                PickerOption(Icons.Default.PhotoLibrary, "Choose from gallery", "Browse your photos", SoftBlue.copy(alpha = 0.12f),   SoftBlue,    onGallery)
                if (onRemove != null) {
                    HorizontalDivider(color = Color(0xFFEDE7DD), thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                    PickerOption(Icons.Default.DeleteOutline, "Remove photo", "Revert to initials", Error.copy(alpha = 0.08f), Error, onRemove)
                }
            }
        }
    }
}

@Composable
private fun PickerOption(icon: ImageVector, label: String, subtitle: String, iconBg: Color, iconTint: Color, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label,    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary,   fontWeight = FontWeight.W600, fontSize = 15.sp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color  = TextSecondary, fontSize   = 12.sp))
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCCC5B8), modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}