package com.kennedy.forge.ui.screens.portfolio

// ─────────────────────────────────────────────────────────────────
//  DEPENDENCIES REQUIRED IN build.gradle.kts (app)
//  implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
//  implementation("com.google.firebase:firebase-auth-ktx")
//  implementation("com.google.firebase:firebase-database-ktx")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
//  implementation("io.coil-kt:coil-compose:2.6.0")
//  implementation("com.squareup.okhttp3:okhttp:4.12.0")
//
//  AndroidManifest.xml — inside <manifest> (NOT inside <application>):
//  <uses-permission android:name="android.permission.INTERNET" />
//  <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />   ← Android 13+
//  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
//      android:maxSdkVersion="32" />                                          ← Android ≤12
// ─────────────────────────────────────────────────────────────────

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.kennedy.forge.navigation.ROUT_PitchView
import com.kennedy.forge.ui.theme.*
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
//  CLOUDINARY CONFIG
//  1. Go to https://console.cloudinary.com → Dashboard → copy "Cloud name"
//  2. Go to Settings → Upload → "Upload presets" → Add unsigned preset
//     Set "Signing Mode" = Unsigned, note the preset name
//  Replace the two constants below with your real values.
// ─────────────────────────────────────────────────────────────────

private const val CLOUDINARY_CLOUD_NAME    = "dv4sidtxo"   // ← your cloud name
private const val CLOUDINARY_UPLOAD_PRESET = "forge_portfolio"       // ← your unsigned preset name
private const val TAG                      = "PortfolioUpload"

// Built automatically — do NOT edit
private val CLOUDINARY_UPLOAD_URL =
    "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY UPLOAD  — the fixed version
//
//  ROOT CAUSE OF THE ORIGINAL FAILURE:
//  The previous implementation called context.contentResolver.openInputStream(uri)
//  but on many Android devices a content:// URI from the photo picker requires
//  the stream to be opened on the CALLING thread that holds the URI grant.
//  When the coroutine switched to Dispatchers.IO the grant was sometimes lost,
//  returning null.  The fix:
//    1. Read the bytes FIRST on Dispatchers.Main (where the URI grant is valid)
//       using a tiny withContext(Dispatchers.Main) block.
//    2. Then switch to Dispatchers.IO for the network call with those bytes.
//  Also added:
//    • Correct content-type passed to Cloudinary (was previously forced to jpeg)
//    • Explicit "resource_type=image" form field
//    • Larger timeouts (90 s write, 60 s read) for slow connections
//    • Full JSON error logging so the exact Cloudinary error message surfaces
// ─────────────────────────────────────────────────────────────────

suspend fun uploadToCloudinary(context: Context, uri: Uri): String? {

    // ── Step 1: resolve MIME and read bytes on Main (URI grant is valid there) ──
    val mimeType = withContext(Dispatchers.Main) {
        context.contentResolver.getType(uri) ?: "image/jpeg"
    }
    val extension = when {
        mimeType.contains("png",  ignoreCase = true) -> "png"
        mimeType.contains("webp", ignoreCase = true) -> "webp"
        else                                          -> "jpg"
    }

    // Read bytes on Main so the ContentResolver URI grant is never lost
    val bytes: ByteArray? = withContext(Dispatchers.Main) {
        try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            Log.e(TAG, "openInputStream failed: ${e.message}", e)
            null
        }
    }

    if (bytes == null || bytes.isEmpty()) {
        Log.e(TAG, "Could not read bytes from URI: $uri")
        return null
    }

    Log.d(TAG, "Read ${bytes.size} bytes, mimeType=$mimeType, uploading to $CLOUDINARY_UPLOAD_URL")

    // ── Step 2: upload on IO thread ──────────────────────────────────────────
    return withContext(Dispatchers.IO) {
        try {
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

            val multipart = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",          "upload.$extension", requestBody)
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("folder",        "forge/portfolio")
                .addFormDataPart("resource_type", "image")
                .build()

            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(90,  TimeUnit.SECONDS)
                .readTimeout(60,   TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(CLOUDINARY_UPLOAD_URL)
                .post(multipart)
                .build()

            val response = client.newCall(request).execute()
            val bodyStr  = response.body?.string() ?: ""

            Log.d(TAG, "Cloudinary HTTP ${response.code}: $bodyStr")

            if (!response.isSuccessful) {
                // Try to extract Cloudinary's own error message
                val errMsg = runCatching {
                    JSONObject(bodyStr).optJSONObject("error")?.optString("message")
                }.getOrNull() ?: "HTTP ${response.code}"
                Log.e(TAG, "Upload failed — $errMsg")
                return@withContext null
            }

            val json   = JSONObject(bodyStr)
            val errObj = json.optJSONObject("error")
            if (errObj != null) {
                Log.e(TAG, "Cloudinary error: ${errObj.optString("message")}")
                return@withContext null
            }

            val url = json.optString("secure_url", "")
            if (url.isBlank()) {
                Log.e(TAG, "secure_url missing in response: $bodyStr")
                return@withContext null
            }

            Log.d(TAG, "Upload success → $url")
            url

        } catch (e: Exception) {
            Log.e(TAG, "uploadToCloudinary exception: ${e.message}", e)
            null
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────────────────────────

data class PortfolioItem(
    val id          : String       = "",
    val title       : String       = "",
    val category    : String       = "",
    val description : String       = "",
    val imageUrl    : String       = "",
    val tags        : List<String> = emptyList(),
    val featured    : Boolean      = false,
    val createdAt   : Long         = System.currentTimeMillis()
)

data class PortfolioMeta(
    val uid           : String  = "",
    val headline      : String  = "",
    val tagline       : String  = "",
    val theme         : String  = "dark",
    val isPublished   : Boolean = false,
    val autoGenerated : Boolean = false,
    val updatedAt     : Long    = System.currentTimeMillis()
)

data class AutoGenForm(
    val name       : String = "",
    val profession : String = "",
    val bio        : String = "",
    val skills     : String = "",
    val style      : String = "bold"
)

enum class PortfolioTab { WORKS, BUILDER, PREVIEW }

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPERS
// ─────────────────────────────────────────────────────────────────

private fun portfolioRef(uid: String) =
    Firebase.database.reference.child("users").child(uid).child("portfolio")

private suspend fun loadMeta(uid: String): PortfolioMeta? = try {
    portfolioRef(uid).child("meta").get().await()
        .getValue(PortfolioMeta::class.java)
} catch (e: Exception) { Log.e(TAG, "loadMeta: ${e.message}"); null }

private suspend fun loadItems(uid: String): List<PortfolioItem> = try {
    portfolioRef(uid).child("items").get().await()
        .children.mapNotNull { it.getValue(PortfolioItem::class.java) }
        .sortedByDescending { it.createdAt }
} catch (e: Exception) { Log.e(TAG, "loadItems: ${e.message}"); emptyList() }

private suspend fun saveMeta(uid: String, meta: PortfolioMeta): Result<Unit> =
    runCatching {
        portfolioRef(uid).child("meta")
            .setValue(meta.copy(updatedAt = System.currentTimeMillis())).await()
    }

private suspend fun upsertItem(uid: String, item: PortfolioItem): Result<PortfolioItem> =
    runCatching {
        val itemsRef = portfolioRef(uid).child("items")
        val ref      = if (item.id.isEmpty()) itemsRef.push() else itemsRef.child(item.id)
        val saved    = item.copy(id = ref.key ?: item.id)
        ref.setValue(saved).await()
        saved
    }

private suspend fun deleteItem(uid: String, itemId: String): Result<Unit> =
    runCatching {
        portfolioRef(uid).child("items").child(itemId).removeValue().await()
    }

private suspend fun setFeatured(uid: String, itemId: String, featured: Boolean): Result<Unit> =
    runCatching {
        portfolioRef(uid).child("items").child(itemId)
            .child("featured").setValue(featured).await()
    }

// ─────────────────────────────────────────────────────────────────
//  AUTO-GENERATE
// ─────────────────────────────────────────────────────────────────

private suspend fun autoGenerate(uid: String, form: AutoGenForm): Result<PortfolioMeta> =
    runCatching {
        val name       = form.name.trim().ifEmpty { "Creative" }
        val profession = form.profession.trim().ifEmpty { "Designer" }
        val bio        = form.bio.trim()
        val skills     = form.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val headline = when (form.style) {
            "minimal" -> "$profession · $name"
            "elegant" -> "$name — $profession & Visual Architect"
            else      -> "$profession · Creative Force · Visual Storyteller"
        }
        val baseLine = bio.ifEmpty {
            when (form.style) {
                "minimal" -> "Design with purpose. Craft with precision."
                "elegant" -> "Merging artistry and strategy to create experiences that endure."
                else      -> "I don't just design — I shape how people feel, think, and connect with ideas."
            }
        }
        val skillSuffix = if (skills.isNotEmpty())
            " Specialising in ${skills.take(3).joinToString(" · ")}." else ""
        val tagline     = "${baseLine.take(150)}$skillSuffix".trim()

        val meta = PortfolioMeta(
            uid = uid, headline = headline, tagline = tagline,
            theme = "dark", isPublished = false, autoGenerated = true,
            updatedAt = System.currentTimeMillis()
        )
        saveMeta(uid, meta).getOrThrow()
        meta
    }

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN  — unchanged from original except showAutoGenForm wiring
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioBuilderScreen(navController: NavController) {

    val isPreview = LocalInspectionMode.current
    val scope     = rememberCoroutineScope()
    val uid       = if (isPreview) "preview_uid"
    else Firebase.auth.currentUser?.uid ?: ""

    var activeTab       by remember { mutableStateOf(PortfolioTab.BUILDER) }
    var meta            by remember { mutableStateOf(PortfolioMeta()) }
    var items           by remember { mutableStateOf<List<PortfolioItem>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(!isPreview) }
    var isSaving        by remember { mutableStateOf(false) }
    var isGenerating    by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }
    var showAddDialog   by remember { mutableStateOf(false) }
    var editingItem     by remember { mutableStateOf<PortfolioItem?>(null) }
    var showDeleteId    by remember { mutableStateOf("") }
    var showAutoGenForm by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        if (isPreview) {
            meta = PortfolioMeta(uid = "preview",
                headline    = "Creative Designer & Visual Storyteller",
                tagline     = "Crafting bold ideas into unforgettable experiences.",
                isPublished = true)
            items = listOf(
                PortfolioItem("1", "Brand Identity – Forge", "Branding",
                    "Full visual identity system", featured = true),
                PortfolioItem("2", "Motion UI Kit", "Motion", "Micro-interaction library"),
                PortfolioItem("3", "Editorial Layout", "Print",
                    "Magazine spread design", featured = true)
            )
            isLoading = false; return@LaunchedEffect
        }
        if (uid.isEmpty()) { isLoading = false; return@LaunchedEffect }
        meta  = loadMeta(uid)  ?: PortfolioMeta(uid = uid)
        items = loadItems(uid)
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Portfolio Builder",
                        fontWeight = FontWeight.Black, letterSpacing = 0.5.sp, color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    AnimatedContent(targetState = meta.isPublished, label = "pub") { published ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (published) SoftGreen.copy(0.15f) else BackgroundSecondary)
                                .clickable {
                                    val updated = meta.copy(isPublished = !published)
                                    meta = updated
                                    if (uid.isNotEmpty()) scope.launch { saveMeta(uid, updated) }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(if (published) "Published" else "Draft",
                                color      = if (published) SoftGreen else TextSecondary,
                                fontSize   = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
            return@Scaffold
        }

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PortfolioTabSwitcher(activeTab = activeTab, onSelect = { activeTab = it })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                AnimatedContent(
                    targetState   = activeTab,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label         = "tab_content"
                ) { tab ->
                    when (tab) {
                        PortfolioTab.WORKS -> WorksTabContent(
                            items          = items,
                            onAddClick     = { editingItem = null; showAddDialog = true },
                            onEditClick    = { editingItem = it; showAddDialog = true },
                            onDeleteClick  = { showDeleteId = it.id },
                            onFeatureClick = { item ->
                                val newFeatured = !item.featured
                                items = items.map {
                                    if (it.id == item.id) it.copy(featured = newFeatured) else it
                                }
                                if (uid.isNotEmpty()) scope.launch {
                                    setFeatured(uid, item.id, newFeatured)
                                }
                            }
                        )
                        PortfolioTab.BUILDER -> BuilderTabContent(
                            meta          = meta,
                            isGenerating  = isGenerating,
                            isSaving      = isSaving,
                            errorMsg      = errorMsg,
                            onMetaChange  = { meta = it },
                            onSave        = {
                                isSaving = true; errorMsg = ""
                                scope.launch {
                                    saveMeta(uid, meta).fold(
                                        onSuccess = { withContext(Dispatchers.Main) { isSaving = false } },
                                        onFailure = { e -> withContext(Dispatchers.Main) {
                                            isSaving = false; errorMsg = e.message ?: "Save failed" }
                                        }
                                    )
                                }
                            },
                            onAutoGenerate = { showAutoGenForm = true }
                        )
                        PortfolioTab.PREVIEW -> PreviewTabContent(
                            meta         = meta,
                            items        = items,
                            onPitchClick = { navController.navigate(ROUT_PitchView) }
                        )
                    }
                }
            }
        }

        // ── ADD / EDIT DIALOG ──────────────────────────────────────────
        if (showAddDialog) {
            AddEditItemDialog(
                initial   = editingItem,
                uid       = uid,
                onDismiss = { showAddDialog = false; editingItem = null },
                onSave    = { item ->
                    showAddDialog = false; editingItem = null
                    scope.launch {
                        upsertItem(uid, item).fold(
                            onSuccess = { saved ->
                                withContext(Dispatchers.Main) {
                                    items = if (items.any { it.id == saved.id })
                                        items.map { if (it.id == saved.id) saved else it }
                                    else listOf(saved) + items
                                }
                            },
                            onFailure = { e ->
                                withContext(Dispatchers.Main) { errorMsg = e.message ?: "Save failed" }
                            }
                        )
                    }
                }
            )
        }

        // ── AUTO-GEN FORM ──────────────────────────────────────────────
        if (showAutoGenForm) {
            AutoGenFormDialog(
                onDismiss  = { showAutoGenForm = false },
                onGenerate = { form ->
                    showAutoGenForm = false
                    isGenerating    = true; errorMsg = ""
                    scope.launch {
                        autoGenerate(uid, form).fold(
                            onSuccess = { generated ->
                                withContext(Dispatchers.Main) {
                                    meta         = generated
                                    isGenerating = false
                                    activeTab    = PortfolioTab.PREVIEW
                                }
                            },
                            onFailure = { e ->
                                withContext(Dispatchers.Main) {
                                    isGenerating = false; errorMsg = e.message ?: "Generation failed"
                                }
                            }
                        )
                    }
                }
            )
        }

        // ── DELETE CONFIRMATION ────────────────────────────────────────
        if (showDeleteId.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showDeleteId = "" },
                title   = { Text("Remove work?", fontWeight = FontWeight.Bold) },
                text    = { Text("This will permanently remove this item from your portfolio.") },
                confirmButton = {
                    TextButton(onClick = {
                        val id = showDeleteId; showDeleteId = ""
                        items = items.filter { it.id != id }
                        scope.launch { deleteItem(uid, id) }
                    }) { Text("Remove", color = Error, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteId = "" }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = CardBackground
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TAB SWITCHER — unchanged
// ─────────────────────────────────────────────────────────────────

@Composable
private fun PortfolioTabSwitcher(activeTab: PortfolioTab, onSelect: (PortfolioTab) -> Unit) {
    val tabs = listOf(
        PortfolioTab.WORKS   to "Works",
        PortfolioTab.BUILDER to "Builder",
        PortfolioTab.PREVIEW to "Preview"
    )
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundSecondary)
    ) {
        tabs.forEach { (tab, label) ->
            val active = activeTab == tab
            Box(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (active) Brush.horizontalGradient(listOf(GoldPrimary, GoldAccent))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onSelect(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label,
                    color      = if (active) DarkSurface else TextSecondary,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    fontSize   = 13.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  WORKS TAB — unchanged
// ─────────────────────────────────────────────────────────────────

@Composable
private fun WorksTabContent(
    items          : List<PortfolioItem>,
    onAddClick     : () -> Unit,
    onEditClick    : (PortfolioItem) -> Unit,
    onDeleteClick  : (PortfolioItem) -> Unit,
    onFeatureClick : (PortfolioItem) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text("Your Works", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                Text("${items.size} piece${if (items.size != 1) "s" else ""} in your collection",
                    color = TextSecondary, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(16.dp))
                    Text("Add Work", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        if (items.isEmpty()) {
            EmptyWorksPlaceholder(onAddClick = onAddClick)
        } else {
            val featured = items.filter { it.featured }
            val regular  = items.filter { !it.featured }
            if (featured.isNotEmpty()) {
                SectionLabel("⭐  FEATURED"); Spacer(Modifier.height(10.dp))
                featured.forEach { item ->
                    PortfolioItemCard(item, onEditClick, onDeleteClick, onFeatureClick)
                    Spacer(Modifier.height(12.dp))
                }
            }
            if (regular.isNotEmpty()) {
                if (featured.isNotEmpty()) SectionLabel("ALL WORKS")
                Spacer(Modifier.height(10.dp))
                regular.forEach { item ->
                    PortfolioItemCard(item, onEditClick, onDeleteClick, onFeatureClick)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyWorksPlaceholder(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp)
            .clip(RoundedCornerShape(20.dp)).background(CardBackground)
            .border(1.dp, GoldPrimary.copy(0.15f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(GoldPrimary.copy(0.1f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PhotoLibrary, null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
            }
            Text("No works yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Add your first creative piece\nto start building your portfolio",
                color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                    .clickable(onClick = onAddClick).padding(horizontal = 20.dp, vertical = 10.dp)
            ) { Text("Add First Work", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        }
    }
}

@Composable private fun SectionLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
}

@Composable
private fun PortfolioItemCard(
    item      : PortfolioItem,
    onEdit    : (PortfolioItem) -> Unit,
    onDelete  : (PortfolioItem) -> Unit,
    onFeature : (PortfolioItem) -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(if (item.featured) 4.dp else 1.dp),
        border    = if (item.featured)
            BorderStroke(1.dp, Brush.horizontalGradient(listOf(GoldDeep, GoldAccent))) else null
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .background(DarkCard)
            ) {
                if (item.imageUrl.isNotEmpty()) {
                    AsyncImage(model = item.imageUrl, contentDescription = item.title,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize()
                        .background(Brush.linearGradient(listOf(DarkCard, DarkSurface))),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = Color(0xFF3A3A3A), modifier = Modifier.size(40.dp))
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                    .clip(RoundedCornerShape(8.dp)).background(DarkSurface.copy(0.85f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(item.category.ifEmpty { "Uncategorized" }, color = GoldAccent,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                if (item.featured) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                        .clip(CircleShape).background(GoldPrimary).padding(6.dp)) {
                        Icon(Icons.Default.Star, null, tint = DarkSurface, modifier = Modifier.size(12.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(item.title.ifEmpty { "Untitled" }, color = TextPrimary,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(item.description, color = TextSecondary, fontSize = 12.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (item.tags.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.tags.take(3).forEach { tag ->
                            Box(Modifier.clip(RoundedCornerShape(6.dp))
                                .background(GoldPrimary.copy(0.08f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                Text("#$tag", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = BackgroundSecondary, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip(
                        icon  = if (item.featured) Icons.Default.StarBorder else Icons.Default.Star,
                        label = if (item.featured) "Unfeature" else "Feature",
                        color = GoldPrimary, onClick = { onFeature(item) })
                    ActionChip(Icons.Default.Edit,   "Edit",   SoftBlue, { onEdit(item) })
                    ActionChip(Icons.Default.Delete, "Remove", Error,    { onDelete(item) })
                }
            }
        }
    }
}

@Composable
private fun ActionChip(
    icon    : androidx.compose.ui.graphics.vector.ImageVector,
    label   : String,
    color   : Color,
    onClick : () -> Unit
) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(color.copy(0.10f)).clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(13.dp))
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  BUILDER TAB — unchanged
// ─────────────────────────────────────────────────────────────────

@Composable
private fun BuilderTabContent(
    meta          : PortfolioMeta,
    isGenerating  : Boolean,
    isSaving      : Boolean,
    errorMsg      : String,
    onMetaChange  : (PortfolioMeta) -> Unit,
    onSave        : () -> Unit,
    onAutoGenerate: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        AutoGenerateCard(isGenerating = isGenerating, onGenerate = onAutoGenerate)
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 14.dp)) {
            Box(Modifier.width(3.dp).height(16.dp).background(GoldGradient, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(10.dp))
            Text("CUSTOMISE YOUR PORTFOLIO", color = TextSecondary, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(2.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BuilderField("Professional Headline", meta.headline,
                    "e.g. Designer · Visual Storyteller · Problem Solver") {
                    onMetaChange(meta.copy(headline = it)) }
                HorizontalDivider(color = BackgroundSecondary, thickness = 0.5.dp)
                BuilderField("Tagline / Pitch Summary", meta.tagline,
                    "1–2 sentences that capture who you are and what you create",
                    singleLine = false, minLines = 3) { onMetaChange(meta.copy(tagline = it)) }
                HorizontalDivider(color = BackgroundSecondary, thickness = 0.5.dp)
                Column {
                    Text("Portfolio Theme", color = TextSecondary, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("dark" to "Dark", "light" to "Light", "gold" to "Gold").forEach { (v, lbl) ->
                            ThemeChip(lbl, meta.theme == v,
                                when (v) { "dark" -> DarkSurface; "light" -> BackgroundMain; else -> GoldPrimary }
                            ) { onMetaChange(meta.copy(theme = v)) }
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = errorMsg.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Error.copy(0.08f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(errorMsg, style = MaterialTheme.typography.bodySmall.copy(color = Error))
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                .clickable(enabled = !isSaving, onClick = onSave),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(targetState = isSaving, label = "save") { saving ->
                if (saving) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(color = DarkSurface, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        Text("Saving…", color = DarkSurface, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Save, null, tint = DarkSurface, modifier = Modifier.size(18.dp))
                        Text("Save Portfolio", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoGenerateCard(isGenerating: Boolean, onGenerate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(DarkCard, DarkSurface)))
            .border(1.dp, GoldPrimary.copy(0.35f), RoundedCornerShape(20.dp))
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(brush = Brush.radialGradient(
                listOf(GoldPrimary.copy(0.12f), Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * 0.2f),
                radius = size.width * 0.5f))
        }
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(GoldPrimary.copy(0.15f)),
                    contentAlignment = Alignment.Center) { Text("✨", fontSize = 20.sp) }
                Column {
                    Text("Auto-Generate Portfolio", color = TextOnDark, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("Answer a few quick questions first", color = TextOnDark.copy(0.5f), fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Fill in a short form about yourself and let Forge craft a polished, professional portfolio identity — tailored to your voice and style.",
                color = TextOnDark.copy(0.7f), fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                    .clickable(enabled = !isGenerating, onClick = onGenerate),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(targetState = isGenerating, label = "gen") { gen ->
                    if (gen) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = DarkSurface, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Text("Generating…", color = DarkSurface, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✨", fontSize = 16.sp)
                            Text("Auto-Generate Now", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuilderField(
    label: String, value: String, hint: String,
    singleLine: Boolean = true, minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    val isFilled = value.isNotEmpty()
    Column {
        Text(label, color = if (isFilled) GoldPrimary else TextSecondary,
            fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        TextField(value = value, onValueChange = onValueChange,
            placeholder = { Text(hint, style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFBBBBBB), fontSize = 14.sp)) },
            singleLine = singleLine, minLines = minLines,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = GoldPrimary),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth())
        Box(modifier = Modifier.fillMaxWidth().height(if (isFilled) 1.5.dp else 1.dp)
            .background(if (isFilled) GoldGradient
            else Brush.linearGradient(listOf(BackgroundSecondary, BackgroundSecondary))))
    }
}

@Composable
private fun ThemeChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(10.dp))
        .background(if (isSelected) GoldPrimary.copy(0.12f) else BackgroundSecondary)
        .border(if (isSelected) 1.5.dp else 0.dp,
            if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(10.dp))
        .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(color)
                .border(1.dp, GoldPrimary.copy(0.3f), CircleShape))
            Text(label, color = if (isSelected) GoldPrimary else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW TAB — unchanged
// ─────────────────────────────────────────────────────────────────

@Composable
private fun PreviewTabContent(
    meta        : PortfolioMeta,
    items       : List<PortfolioItem>,
    onPitchClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (meta.theme == "dark") DarkCard else CardBackground),
            elevation = CardDefaults.cardElevation(4.dp)) {
            Column {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(when (meta.theme) {
                        "dark"  -> Brush.linearGradient(listOf(DarkSurface, DarkCard))
                        "gold"  -> Brush.linearGradient(listOf(GoldDeep, GoldAccent))
                        else    -> Brush.linearGradient(listOf(BackgroundSecondary, BackgroundMain))
                    })) {
                    Canvas(Modifier.matchParentSize()) {
                        val sp = 28f; val c = (size.width / sp).toInt() + 2; val r = (size.height / sp).toInt() + 2
                        for (ci in 0..c) for (ri in 0..r)
                            drawCircle(GoldPrimary.copy(0.06f), 1.4f, Offset(ci * sp, ri * sp))
                    }
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        if (meta.headline.isNotEmpty()) {
                            Text(meta.headline,
                                color = if (meta.theme == "gold") DarkSurface else TextOnDark,
                                fontWeight = FontWeight.Black, fontSize = 14.sp,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (meta.tagline.isNotEmpty()) {
                    Text(meta.tagline,
                        color = if (meta.theme == "dark") TextOnDark.copy(0.75f) else TextSecondary,
                        fontSize = 13.sp, lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
                }
                HorizontalDivider(color = BackgroundSecondary.copy(0.4f))
                val featured = items.filter { it.featured }.take(3)
                if (featured.isNotEmpty()) {
                    Text("Featured Works",
                        color = if (meta.theme == "dark") GoldAccent else GoldPrimary,
                        fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 20.dp, top = 14.dp))
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        featured.forEach { item ->
                            Box(Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(DarkSurface)) {
                                if (item.imageUrl.isNotEmpty()) {
                                    AsyncImage(model = item.imageUrl, contentDescription = item.title,
                                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Box(Modifier.fillMaxSize().background(
                                        Brush.linearGradient(listOf(DarkCard, Color(0xFF2A2A2A)))),
                                        contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Image, null, tint = Color(0xFF444444), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("${items.size}" to "Works",
                        "${items.count { it.featured }}" to "Featured",
                        (if (meta.isPublished) "Live" else "Draft") to "Status"
                    ).forEach { (v, lbl) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(v, color = if (meta.theme == "dark") TextOnDark else TextPrimary,
                                fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(lbl, color = if (meta.theme == "dark") TextOnDark.copy(0.4f) else TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        CompletenessCard(meta = meta, itemCount = items.size)
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
            .clickable(onClick = onPitchClick), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Mic, null, tint = DarkSurface, modifier = Modifier.size(20.dp))
                Text("Go to Pitch View", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 0.3.sp)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DarkSurface, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Your portfolio will be attached to your pitch deck automatically",
            color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun CompletenessCard(meta: PortfolioMeta, itemCount: Int) {
    val checks = listOf(
        "Headline added"      to meta.headline.isNotBlank(),
        "Tagline written"     to meta.tagline.isNotBlank(),
        "Works uploaded"      to (itemCount > 0),
        "Featured work set"   to (itemCount > 0),
        "Portfolio published" to meta.isPublished
    )
    val score = checks.count { it.second }.toFloat() / checks.size
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Portfolio Completeness", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${(score * 100).toInt()}%", color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { score },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = GoldPrimary, trackColor = BackgroundSecondary)
            Spacer(Modifier.height(12.dp))
            checks.forEach { (label, done) ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Icon(if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null,
                        tint = if (done) SoftGreen else Color(0xFFCCC5B8), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(label, color = if (done) TextPrimary else TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ADD / EDIT DIALOG  — fixed: uid passed in, bytes read on Main
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AddEditItemDialog(
    initial  : PortfolioItem?,
    uid      : String,
    onDismiss: () -> Unit,
    onSave   : (PortfolioItem) -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var title       by remember { mutableStateOf(initial?.title       ?: "") }
    var category    by remember { mutableStateOf(initial?.category    ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var tagsRaw     by remember { mutableStateOf(initial?.tags?.joinToString(", ") ?: "") }
    var showError   by remember { mutableStateOf(false) }

    var imageUrl         by remember { mutableStateOf(initial?.imageUrl ?: "") }
    var localPreviewUri  by remember { mutableStateOf<Uri?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadError      by remember { mutableStateOf("") }

    // ── Gallery launcher ──────────────────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        localPreviewUri  = uri       // show local preview immediately
        isUploadingImage = true
        uploadError      = ""
        imageUrl         = ""        // clear previous URL

        scope.launch {
            // uploadToCloudinary reads bytes on Main internally — safe here
            val cloudUrl = uploadToCloudinary(context, uri)
            // Back on Main (launch runs on Main by default)
            if (cloudUrl != null) {
                imageUrl        = cloudUrl
                localPreviewUri = null   // replace preview with hosted URL
            } else {
                uploadError     = "Upload failed — check your internet and try again."
                localPreviewUri = null
            }
            isUploadingImage = false
        }
    }

    val displayModel: Any? = when {
        localPreviewUri != null -> localPreviewUri
        imageUrl.isNotEmpty()   -> imageUrl
        else                    -> null
    }

    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f))
                .clickable(interactionSource = remember { MutableInteractionSource() },
                    indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CardBackground)
                    .clickable(interactionSource = remember { MutableInteractionSource() },
                        indication = null) {}
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(Modifier.align(Alignment.CenterHorizontally)
                    .width(40.dp).height(4.dp).clip(CircleShape).background(Color(0xFFDDD8CE)))
                Spacer(Modifier.height(16.dp))

                Text(if (initial == null) "Add Work" else "Edit Work",
                    color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text("Fill in the details for this portfolio piece",
                    color = TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(20.dp))

                DialogField("Title *", title, "e.g. Brand Identity – Forge",
                    showError && title.isBlank()) { title = it }
                Spacer(Modifier.height(14.dp))
                DialogField("Category", category, "e.g. Branding, Motion, Photography") { category = it }
                Spacer(Modifier.height(14.dp))
                DialogField("Description", description, "What is this project about?",
                    singleLine = false, minLines = 3) { description = it }
                Spacer(Modifier.height(14.dp))
                DialogField("Tags (comma separated)", tagsRaw, "e.g. ui, branding, print") { tagsRaw = it }

                Spacer(Modifier.height(18.dp))

                // ── Image picker ──────────────────────────────────────────
                Text("Work image",
                    color = when {
                        uploadError.isNotEmpty() -> Error
                        imageUrl.isNotEmpty()    -> GoldPrimary
                        else                     -> TextSecondary
                    },
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BackgroundSecondary)
                        .border(1.dp,
                            when {
                                uploadError.isNotEmpty() -> Error.copy(0.5f)
                                imageUrl.isNotEmpty()    -> GoldPrimary.copy(0.4f)
                                else                     -> Color(0xFFCCC5B8)
                            }, RoundedCornerShape(14.dp))
                        .clickable(enabled = !isUploadingImage) { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (displayModel != null) {
                        AsyncImage(model = displayModel, contentDescription = "Work image",
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())

                        // Uploading overlay
                        if (isUploadingImage) {
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.55f)),
                                contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(color = GoldPrimary,
                                        modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                                    Text("Uploading to Cloudinary…",
                                        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else {
                            // "Tap to change" hint
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.28f)),
                                contentAlignment = Alignment.Center) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("Tap to change", color = Color.White,
                                        fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    } else {
                        // Empty state
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(52.dp).clip(CircleShape).background(GoldPrimary.copy(0.1f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PhotoLibrary, null,
                                    tint = GoldPrimary, modifier = Modifier.size(26.dp))
                            }
                            Text("Tap to pick from gallery",
                                color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Supports JPG, PNG, WEBP",
                                color = TextSecondary.copy(0.6f), fontSize = 11.sp)
                        }
                    }
                }

                // Upload error
                if (uploadError.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(13.dp))
                        Text(uploadError, color = Error, fontSize = 12.sp)
                    }
                }
                if (showError && title.isBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Title is required", color = Error, fontSize = 12.sp)
                }

                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp)) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier.weight(1f).height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (!isUploadingImage)
                                    Brush.horizontalGradient(listOf(GoldDeep, GoldAccent))
                                else
                                    Brush.horizontalGradient(listOf(GoldDeep.copy(0.5f), GoldAccent.copy(0.5f)))
                            )
                            .clickable(enabled = !isUploadingImage) {
                                if (title.isBlank()) { showError = true; return@clickable }
                                val tags = tagsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                onSave((initial ?: PortfolioItem()).copy(
                                    title       = title.trim(),
                                    category    = category.trim(),
                                    description = description.trim(),
                                    imageUrl    = imageUrl,
                                    tags        = tags
                                ))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploadingImage) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CircularProgressIndicator(color = DarkSurface, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                                Text("Uploading…", color = DarkSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        } else {
                            Text(if (initial == null) "Add to Portfolio" else "Save Changes",
                                color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  AUTO-GEN FORM DIALOG — unchanged
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AutoGenFormDialog(
    onDismiss : () -> Unit,
    onGenerate: (AutoGenForm) -> Unit
) {
    var name       by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var bio        by remember { mutableStateOf("") }
    var skills     by remember { mutableStateOf("") }
    var style      by remember { mutableStateOf("bold") }
    var showError  by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.7f))
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter) {
            Column(modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(CardBackground)
                .clickable(interactionSource = remember { MutableInteractionSource() },
                    indication = null) {}
                .padding(24.dp).verticalScroll(rememberScrollState())) {

                Box(Modifier.align(Alignment.CenterHorizontally)
                    .width(40.dp).height(4.dp).clip(CircleShape).background(Color(0xFFDDD8CE)))
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(GoldPrimary.copy(0.15f)),
                        contentAlignment = Alignment.Center) { Text("✨", fontSize = 20.sp) }
                    Column {
                        Text("Build My Portfolio", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("Tell us about yourself", color = TextSecondary, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))
                DialogField("Your Name *", name, "e.g. Jordan Lee", showError && name.isBlank()) { name = it }
                Spacer(Modifier.height(14.dp))
                DialogField("Profession / Role *", profession, "e.g. Graphic Designer, Motion Artist",
                    showError && profession.isBlank()) { profession = it }
                Spacer(Modifier.height(14.dp))
                DialogField("Bio / About You", bio, "What drives you? What kind of work do you create?",
                    singleLine = false, minLines = 3) { bio = it }
                Spacer(Modifier.height(14.dp))
                DialogField("Skills (comma separated)", skills, "e.g. Branding, UI Design, Photography") { skills = it }

                Spacer(Modifier.height(18.dp))
                Text("Portfolio Voice", color = TextSecondary, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("bold" to "🔥 Bold", "minimal" to "◻ Minimal", "elegant" to "✦ Elegant")
                        .forEach { (value, label) ->
                            val selected = style == value
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (selected) GoldPrimary.copy(0.12f) else BackgroundSecondary)
                                .border(if (selected) 1.5.dp else 0.dp,
                                    if (selected) GoldPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { style = value }.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center) {
                                Text(label, color = if (selected) GoldPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center)
                            }
                        }
                }

                if (showError && (name.isBlank() || profession.isBlank())) {
                    Spacer(Modifier.height(10.dp))
                    Text("Please fill in your name and profession", color = Error, fontSize = 12.sp)
                }

                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Box(modifier = Modifier.weight(2f).height(50.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                        .clickable {
                            if (name.isBlank() || profession.isBlank()) { showError = true; return@clickable }
                            onGenerate(AutoGenForm(name, profession, bio, skills, style))
                        }, contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("✨", fontSize = 16.sp)
                            Text("Generate Portfolio", color = DarkSurface,
                                fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SHARED DIALOG FIELD — unchanged
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DialogField(
    label        : String,
    value        : String,
    hint         : String,
    showError    : Boolean = false,
    singleLine   : Boolean = true,
    minLines     : Int     = 1,
    onValueChange: (String) -> Unit
) {
    val isFilled = value.isNotEmpty()
    Column {
        Text(label,
            color = when { showError -> Error; isFilled -> GoldPrimary; else -> TextSecondary },
            fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
        TextField(value = value, onValueChange = onValueChange,
            placeholder = { Text(hint, style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFFBBBBBB), fontSize = 13.sp)) },
            singleLine = singleLine, minLines = minLines,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = GoldPrimary),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth())
        Box(modifier = Modifier.fillMaxWidth().height(if (showError) 1.5.dp else 1.dp)
            .background(when {
                showError -> Brush.linearGradient(listOf(Error, Error.copy(0.6f)))
                isFilled  -> GoldGradient
                else      -> Brush.linearGradient(listOf(BackgroundSecondary, BackgroundSecondary))
            }))
    }
}

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPER — unchanged
// ─────────────────────────────────────────────────────────────────

private fun DrawScope.drawDots(color: Color, spacing: Float) {
    val cols = (size.width  / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2
    for (c in 0..cols) for (r in 0..rows)
        drawCircle(color, 1.4f, Offset(c * spacing, r * spacing))
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PortfolioBuilderScreenPreview() {
    PortfolioBuilderScreen(rememberNavController())
}