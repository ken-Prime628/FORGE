package com.kennedy.forge.ui.screens.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.kennedy.forge.navigation.ROUT_ProfileSetup
import com.kennedy.forge.navigation.ROUT_SkillAssessment
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

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY CONFIG  🔁 Replace with your real credentials
// ─────────────────────────────────────────────────────────────────
private object CloudinaryConfig {
    const val CLOUD_NAME    = "YOUR_CLOUD_NAME"
    const val UPLOAD_PRESET = "YOUR_UPLOAD_PRESET"
    val UPLOAD_URL get() = "https://api.\n" +
            "dv4sidtxo.com/v1_1/$CLOUD_NAME/image/upload"
}

// ─────────────────────────────────────────────────────────────────
//  PROFILE MODEL — saved to /users/{uid}/profile
// ─────────────────────────────────────────────────────────────────
data class UserProfile(
    val uid: String        = "",
    val name: String       = "",
    val profession: String = "",
    val category: String   = "",
    val bio: String        = "",
    val avatarUrl: String  = "",
    val updatedAt: Long    = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────
//  CLOUDINARY UPLOAD
// ─────────────────────────────────────────────────────────────────
suspend fun uploadAvatarToCloudinary(
    context: Context,
    imageUri: Uri
): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val tempFile = File(context.cacheDir, "upload_avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(tempFile).use { output -> input.copyTo(output) }
        } ?: error("Could not open image stream")

        val client = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", tempFile.name, tempFile.asRequestBody("image/*".toMediaType()))
            .addFormDataPart("upload_preset", CloudinaryConfig.UPLOAD_PRESET)
            .addFormDataPart("folder", "forge/avatars")
            .build()

        val request = Request.Builder()
            .url(CloudinaryConfig.UPLOAD_URL)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: error("Empty response from Cloudinary")
        if (!response.isSuccessful) error("Cloudinary upload failed (${response.code}): $body")

        JSONObject(body).getString("secure_url")
    }
}

// ─────────────────────────────────────────────────────────────────
//  FIREBASE — save profile under /users/{uid}/profile
// ─────────────────────────────────────────────────────────────────
suspend fun saveProfileToDatabase(profile: UserProfile): Result<Unit> = runCatching {
    Firebase.database.reference
        .child("users")
        .child(profile.uid)
        .child("profile")
        .setValue(profile)
        .await()
}

// ─────────────────────────────────────────────────────────────────
//  PROFILE SETUP SCREEN
// ─────────────────────────────────────────────────────────────────
@Composable
fun ProfileSetupScreen(navController: NavController) {

    val context   = LocalContext.current
    val scope     = rememberCoroutineScope()
    val isPreview = LocalInspectionMode.current   // ✅ true when rendering in Android Studio preview

    var name               by remember { mutableStateOf("") }
    var profession         by remember { mutableStateOf("") }
    var category           by remember { mutableStateOf("") }
    var bio                by remember { mutableStateOf("") }
    var avatarUri          by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPicker    by remember { mutableStateOf(false) }
    var showValidationHint by remember { mutableStateOf(false) }
    var isSaving           by remember { mutableStateOf(false) }
    var saveError          by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && profession.isNotBlank()

    // ✅ FileProvider.getUriForFile() crashes the preview renderer because there is no
    //    real application context at design time. We return Uri.EMPTY as a safe dummy
    //    when running inside the Compose preview, and use the real FileProvider on device.
    val cameraUri: Uri = if (isPreview) {
        Uri.EMPTY
    } else {
        remember {
            val file = File(context.cacheDir, "forge_avatar_${System.currentTimeMillis()}.jpg")
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) avatarUri = cameraUri }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { avatarUri = it } }

    val shakeOffset by animateFloatAsState(
        targetValue = if (showValidationHint) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0; (-8f) at 50; 8f at 100
            (-6f) at 150; 6f at 200; (-4f) at 250; 0f at 300
        },
        label = "shake"
    )

    // ── Save handler ─────────────────────────────────────────────
    fun handleContinue() {
        if (!isFormValid) { showValidationHint = true; return }

        isSaving  = true
        saveError = ""

        scope.launch {
            val uid = Firebase.auth.currentUser?.uid
            if (uid == null) {
                withContext(Dispatchers.Main) {
                    isSaving  = false
                    saveError = "Session expired. Please sign in again."
                }
                return@launch
            }

            val avatarUrl: String = if (avatarUri != null) {
                val uploadResult = uploadAvatarToCloudinary(context, avatarUri!!)
                uploadResult.getOrElse { e ->
                    withContext(Dispatchers.Main) {
                        isSaving  = false
                        saveError = "Photo upload failed: ${e.message}"
                    }
                    return@launch
                }
            } else ""

            val profile = UserProfile(
                uid        = uid,
                name       = name.trim(),
                profession = profession.trim(),
                category   = category.trim(),
                bio        = bio.trim(),
                avatarUrl  = avatarUrl
            )

            val saveResult = saveProfileToDatabase(profile)
            withContext(Dispatchers.Main) {
                isSaving = false
                saveResult.fold(
                    onSuccess = {
                        navController.navigate(ROUT_SkillAssessment) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onFailure = { e ->
                        saveError = e.message ?: "Failed to save profile. Try again."
                    }
                )
            }
        }
    }

    // ── UI ────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDotPattern(
                color     = Color(0xFFC89B3C).copy(alpha = 0.05f),
                spacing   = 32f,
                dotRadius = 1.5f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp)
        ) {

            ProfileHeroHeader()

            Box(
                modifier         = Modifier.fillMaxWidth().offset(y = (-48).dp),
                contentAlignment = Alignment.Center
            ) {
                PremiumAvatarPicker(avatarUri = avatarUri, onPickPhoto = { showPhotoPicker = true })
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-28).dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .background(GoldGradient, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "YOUR CREATIVE IDENTITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color         = TextSecondary,
                            letterSpacing = 2.sp,
                            fontWeight    = FontWeight.W600,
                            fontSize      = 10.sp
                        )
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation    = 16.dp,
                            shape        = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFFC89B3C).copy(alpha = 0.08f),
                            spotColor    = Color(0xFFC89B3C).copy(alpha = 0.14f)
                        ),
                    colors    = CardDefaults.cardColors(containerColor = CardBackground),
                    shape     = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        ForgeInputField(
                            label         = "Full Name",
                            value         = name,
                            onValueChange = { name = it; showValidationHint = false; saveError = "" },
                            icon          = Icons.Outlined.Person,
                            hint          = "e.g. Kennedy Osei",
                            isRequired    = true,
                            showError     = showValidationHint && name.isBlank(),
                            imeAction     = ImeAction.Next
                        )

                        ForgeFieldDivider()

                        ForgeInputField(
                            label         = "Profession",
                            value         = profession,
                            onValueChange = { profession = it; showValidationHint = false; saveError = "" },
                            icon          = Icons.Outlined.Work,
                            hint          = "e.g. Designer, Filmmaker, Writer",
                            isRequired    = true,
                            showError     = showValidationHint && profession.isBlank(),
                            imeAction     = ImeAction.Next
                        )

                        ForgeFieldDivider()

                        ForgeInputField(
                            label         = "Creative Category",
                            value         = category,
                            onValueChange = { category = it },
                            icon          = Icons.Outlined.Category,
                            hint          = "e.g. UI/UX, Music Production, Fiction",
                            imeAction     = ImeAction.Next
                        )

                        ForgeFieldDivider()

                        ForgeInputField(
                            label         = "Short Bio",
                            value         = bio,
                            onValueChange = { bio = it },
                            icon          = Icons.Outlined.Edit,
                            hint          = "What drives your creative work?",
                            singleLine    = false,
                            imeAction     = ImeAction.Done,
                            minLines      = 3
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                StepProgress(currentStep = 1, totalSteps = 3)

                Spacer(Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = showValidationHint,
                    enter   = fadeIn() + slideInVertically { -8 },
                    exit    = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = shakeOffset.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Error.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = Error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Name and Profession are required to continue",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color      = Error,
                                fontWeight = FontWeight.W500
                            )
                        )
                    }
                }

                AnimatedVisibility(
                    visible = saveError.isNotEmpty(),
                    enter   = fadeIn() + slideInVertically { -8 },
                    exit    = fadeOut()
                ) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Error.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = Error, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(saveError, style = MaterialTheme.typography.bodySmall.copy(color = Error))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Category and bio can be updated anytime from your profile",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color     = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize  = 12.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        FixedContinueBar(
            isFormValid = isFormValid,
            isSaving    = isSaving,
            hasAvatar   = avatarUri != null,
            modifier    = Modifier.align(Alignment.BottomCenter),
            onContinue  = { handleContinue() }
        )

        if (showPhotoPicker) {
            PhotoPickerSheet(
                onDismiss = { showPhotoPicker = false },
                onCamera  = { showPhotoPicker = false; cameraLauncher.launch(cameraUri) },
                onGallery = { showPhotoPicker = false; galleryLauncher.launch("image/*") },
                onRemove  = if (avatarUri != null) {
                    { avatarUri = null; showPhotoPicker = false }
                } else null
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PHOTO PICKER SHEET
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PhotoPickerSheet(
    onDismiss: () -> Unit,
    onCamera:  () -> Unit,
    onGallery: () -> Unit,
    onRemove:  (() -> Unit)?
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CardBackground)
                    .padding(bottom = 32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null
                    ) {}
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDD8CE))
                )
                Text(
                    "Profile Photo",
                    style    = MaterialTheme.typography.titleMedium.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.W700,
                        fontSize   = 17.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                )
                Text(
                    "Choose how to set your avatar",
                    style    = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 13.sp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                )
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
private fun PickerOption(
    icon: ImageVector, label: String, subtitle: String,
    iconBg: Color, iconTint: Color, onClick: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label,    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary,   fontWeight = FontWeight.W600, fontSize = 15.sp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall.copy( color = TextSecondary, fontSize   = 12.sp))
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFCCC5B8), modifier = Modifier.size(18.dp))
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO HEADER
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(DarkSurface)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC89B3C).copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.08f),
                        radius = size.width * 0.58f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE6B85C).copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.92f),
                        radius = size.width * 0.42f
                    )
                )
            }

            Column(
                modifier            = Modifier.fillMaxSize().padding(start = 28.dp, top = 52.dp, end = 28.dp, bottom = 60.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = GoldPrimary.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.size(5.dp).clip(CircleShape).background(GoldAccent))
                        Text(
                            "Step 1 of 3  ·  Profile Setup",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = GoldAccent,
                                letterSpacing = 0.6.sp,
                                fontWeight    = FontWeight.W500
                            )
                        )
                    }
                }

                Column {
                    Text("Build your",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color      = TextOnDark.copy(alpha = 0.40f),
                            fontWeight = FontWeight.W300,
                            fontSize   = 22.sp
                        )
                    )
                    Text("creative identity.",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color         = TextOnDark,
                            fontWeight    = FontWeight.W700,
                            letterSpacing = (-0.5).sp,
                            fontSize      = 30.sp
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "This profile represents you across feedback,\nportfolio, pitches, and collaborations.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color      = TextOnDark.copy(alpha = 0.45f),
                            lineHeight = 18.sp,
                            fontSize   = 12.sp
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  AVATAR PICKER
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PremiumAvatarPicker(avatarUri: Uri?, onPickPhoto: () -> Unit) {
    val scaleAnim by animateFloatAsState(
        targetValue   = if (avatarUri != null) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "avatar_scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(scaleAnim)) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(
                    if (avatarUri != null) GoldGradient
                    else Brush.linearGradient(listOf(Color(0xFFDDD8CE), Color(0xFFCCC5B8)))
                )
        )
        Box(modifier = Modifier.size(97.dp).clip(CircleShape).background(BackgroundMain))
        Box(
            modifier         = Modifier.size(88.dp).clip(CircleShape).background(BackgroundSecondary).clickable(onClick = onPickPhoto),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUri != null) {
                AsyncImage(
                    model              = avatarUri,
                    contentDescription = "Profile photo",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CameraAlt, null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Add photo", style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary, fontSize = 10.sp, letterSpacing = 0.3.sp))
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = (-2).dp)
                .size(26.dp)
                .clip(CircleShape)
                .background(GoldGradient)
                .border(2.dp, BackgroundMain, CircleShape)
                .clickable(onClick = onPickPhoto),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (avatarUri != null) Icons.Default.Edit else Icons.Default.Add,
                null, tint = DarkSurface, modifier = Modifier.size(12.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  INPUT FIELD
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ForgeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    hint: String,
    singleLine: Boolean  = true,
    imeAction: ImeAction = ImeAction.Next,
    minLines: Int        = 1,
    isRequired: Boolean  = false,
    showError: Boolean   = false
) {
    val isFilled   = value.isNotEmpty()
    val iconTint   = when { showError -> Error; isFilled -> GoldPrimary; else -> Color(0xFF9E9E9E) }
    val iconBg     = when { showError -> Error.copy(alpha = 0.08f); isFilled -> GoldPrimary.copy(alpha = 0.10f); else -> BackgroundMain }
    val labelColor = when { showError -> Error; isFilled -> GoldPrimary; else -> TextSecondary }

    Column {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier         = Modifier.padding(top = 4.dp).size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color         = labelColor,
                            fontWeight    = FontWeight.W600,
                            letterSpacing = 0.4.sp,
                            fontSize      = 11.sp
                        )
                    )
                    if (isRequired) Text(
                        " *",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color    = if (showError) Error else GoldPrimary,
                            fontSize = 11.sp
                        )
                    )
                }
                TextField(
                    value           = value,
                    onValueChange   = onValueChange,
                    placeholder     = { Text(hint, style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFBBBBBB), fontSize = 14.sp)) },
                    singleLine      = singleLine,
                    minLines        = minLines,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = imeAction
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary,
                        cursorColor             = GoldPrimary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color      = TextPrimary,
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.W400
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(if (showError) 1.5.dp else 1.dp)
                .background(
                    when {
                        showError -> Brush.linearGradient(listOf(Error, Error.copy(alpha = 0.6f)))
                        isFilled  -> GoldGradient
                        else      -> Brush.linearGradient(listOf(Color(0xFFEDE7DD), Color(0xFFEDE7DD)))
                    }
                )
        )
    }
}

@Composable
private fun ForgeFieldDivider() {
    HorizontalDivider(
        color     = Color(0xFFEDE7DD),
        thickness = 0.5.dp,
        modifier  = Modifier.padding(vertical = 10.dp)
    )
}

// ─────────────────────────────────────────────────────────────────
//  STEP PROGRESS
// ─────────────────────────────────────────────────────────────────
@Suppress("SameParameterValue")
@Composable
private fun StepProgress(currentStep: Int, totalSteps: Int) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Profile setup", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.W600, fontSize = 11.sp))
            Text("$currentStep / $totalSteps", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp))
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(totalSteps) { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (i < currentStep) GoldGradient
                            else Brush.linearGradient(listOf(Color(0xFFEDE7DD), Color(0xFFEDE7DD)))
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  FIXED BOTTOM CTA
// ─────────────────────────────────────────────────────────────────
@Composable
private fun FixedContinueBar(
    isFormValid: Boolean,
    isSaving:    Boolean,
    hasAvatar:   Boolean,
    modifier:    Modifier = Modifier,
    onContinue:  () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label         = "btn_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundMain.copy(alpha = 0f), BackgroundMain.copy(alpha = 0.95f), BackgroundMain)
                )
            )
            .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 36.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = Triple(isFormValid, isSaving, hasAvatar),
                    label       = "valid_hint"
                ) { (valid, saving, avatar) ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                Text("Looking good — ready to continue", style = MaterialTheme.typography.labelSmall.copy(color = Success, fontWeight = FontWeight.W500, fontSize = 12.sp))
                            }
                            else -> {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFD8D0C4)))
                                Text("Fill in Name and Profession to continue", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 12.sp))
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .scale(buttonScale)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (isFormValid && !isSaving) GoldGradient
                        else Brush.linearGradient(listOf(Color(0xFFD8D0C4), Color(0xFFCCC5B8)))
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication        = ripple(color = if (isFormValid) DarkSurface.copy(alpha = 0.15f) else Color.Transparent),
                        enabled           = !isSaving,
                        onClick           = onContinue
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState    = isSaving,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label          = "btn_content"
                ) { saving ->
                    if (saving) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = DarkSurface, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Text(
                                if (hasAvatar) "Uploading & saving…" else "Saving profile…",
                                style = MaterialTheme.typography.titleSmall.copy(color = DarkSurface, fontWeight = FontWeight.W600)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Continue to Assessment",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color      = if (isFormValid) DarkSurface else Color(0xFF9A9A9A),
                                    fontWeight = FontWeight.W700,
                                    fontSize   = 15.sp
                                )
                            )
                            if (isFormValid) {
                                Spacer(Modifier.width(10.dp))
                                Box(
                                    modifier         = Modifier.size(28.dp).clip(CircleShape).background(DarkSurface.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
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

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPERS
// ─────────────────────────────────────────────────────────────────
private fun DrawScope.drawDotPattern(color: Color, spacing: Float, dotRadius: Float) {
    val cols = (size.width / spacing).toInt() + 1
    val rows = (size.height / spacing).toInt() + 1
    for (col in 0..cols) for (row in 0..rows) {
        drawCircle(color = color, radius = dotRadius, center = Offset(col * spacing, row * spacing))
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileSetupScreenPreview() {
    ProfileSetupScreen(rememberNavController())
}