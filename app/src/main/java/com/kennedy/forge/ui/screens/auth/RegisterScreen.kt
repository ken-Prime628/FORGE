package com.kennedy.forge.ui.screens.auth

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import com.kennedy.forge.navigation.ROUTE_Login
import com.kennedy.forge.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────
//  DEPENDENCIES — add to build.gradle (app)
//
//  implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
//  implementation("com.google.firebase:firebase-auth-ktx")
//  implementation("com.google.firebase:firebase-database-ktx")
//  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
//
//  build.gradle (project): id("com.google.gms.google-services") version "4.4.2" apply false
//  build.gradle (app) plugins: id("com.google.gms.google-services")
//  Place google-services.json in app/
//  Firebase Console → Auth: enable Email/Password provider only
//  Firebase Console → Realtime Database: create DB, rules → auth != null
// ─────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────
//  USER MODEL — saved to Realtime Database at /users/{uid}
// ─────────────────────────────────────────────────────────────────
data class ForgeUser(
    val uid: String     = "",
    val name: String    = "",
    val email: String   = "",
    val plan: String    = "free",
    val createdAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPERS
// ─────────────────────────────────────────────────────────────────

suspend fun saveUserToDatabase(user: ForgeUser) {
    com.google.firebase.Firebase.database.reference
        .child("users")
        .child(user.uid)
        .setValue(user)
        .await()
}

suspend fun registerWithEmail(
    name: String,
    email: String,
    password: String
): Result<String> = runCatching {
    val result = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
    val firebaseUser = result.user ?: error("User creation failed")

    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
        .setDisplayName(name)
        .build()
    firebaseUser.updateProfile(profileUpdates).await()

    saveUserToDatabase(
        ForgeUser(
            uid   = firebaseUser.uid,
            name  = name,
            email = email
        )
    )
    firebaseUser.uid
}

// ─────────────────────────────────────────────────────────────────
//  REGISTER SCREEN
// ─────────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(navController: NavController) {

    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading              by remember { mutableStateOf(false) }

    var nameError     by remember { mutableStateOf(false) }
    var emailError    by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmError  by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val passwordStrength = when {
        password.length >= 12 && password.any { it.isUpperCase() } &&
                password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 10 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 3
        password.length >= 8  && (password.any { it.isUpperCase() } || password.any { it.isDigit() }) -> 2
        password.length >= 6  -> 1
        else -> 0
    }

    fun validate(): Boolean {
        nameError     = name.isBlank()
        emailError    = email.isBlank() || !email.contains("@")
        passwordError = password.length < 6
        confirmError  = password != confirmPassword || confirmPassword.isBlank()
        errorMessage  = when {
            nameError     -> "Please enter your full name"
            emailError    -> "Enter a valid email address"
            passwordError -> "Password must be at least 6 characters"
            confirmError  -> "Passwords do not match"
            else          -> ""
        }
        return !nameError && !emailError && !passwordError && !confirmError
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {

        Canvas(Modifier.fillMaxSize()) {
            drawDotGrid(color = GoldPrimary.copy(alpha = 0.04f), spacing = 28f, radius = 1.4f)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            RegisterHeroHeader()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-28).dp)
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation    = 16.dp,
                            shape        = RoundedCornerShape(28.dp),
                            ambientColor = GoldPrimary.copy(alpha = 0.08f),
                            spotColor    = GoldPrimary.copy(alpha = 0.14f)
                        ),
                    shape     = RoundedCornerShape(28.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Box(
                                Modifier
                                    .width(3.dp)
                                    .height(16.dp)
                                    .background(GoldGradient, RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Create your Forge account",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color         = TextSecondary,
                                    fontWeight    = FontWeight.W500,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        }

                        RegInputField(
                            label         = "Full name",
                            value         = name,
                            onValueChange = { name = it; nameError = false; errorMessage = "" },
                            icon          = Icons.Default.Person,
                            hint          = "e.g. Kennedy Osei",
                            imeAction     = ImeAction.Next,
                            showError     = nameError,
                            isRequired    = true
                        )

                        RegDivider()

                        RegInputField(
                            label         = "Email address",
                            value         = email,
                            onValueChange = { email = it; emailError = false; errorMessage = "" },
                            icon          = Icons.Default.Email,
                            hint          = "you@example.com",
                            keyboardType  = KeyboardType.Email,
                            imeAction     = ImeAction.Next,
                            showError     = emailError,
                            isRequired    = true
                        )

                        RegDivider()

                        RegInputField(
                            label            = "Password",
                            value            = password,
                            onValueChange    = { password = it; passwordError = false; errorMessage = "" },
                            icon             = Icons.Default.Lock,
                            hint             = "At least 6 characters",
                            keyboardType     = KeyboardType.Password,
                            imeAction        = ImeAction.Next,
                            showError        = passwordError,
                            isRequired       = true,
                            isPassword       = true,
                            passwordVisible  = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )

                        AnimatedVisibility(
                            visible = password.isNotEmpty(),
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            PasswordStrengthMeter(strength = passwordStrength)
                        }

                        RegDivider()

                        RegInputField(
                            label            = "Confirm password",
                            value            = confirmPassword,
                            onValueChange    = { confirmPassword = it; confirmError = false; errorMessage = "" },
                            icon             = Icons.Default.LockOpen,
                            hint             = "Re-enter your password",
                            keyboardType     = KeyboardType.Password,
                            imeAction        = ImeAction.Done,
                            showError        = confirmError,
                            isRequired       = true,
                            isPassword       = true,
                            passwordVisible  = confirmPasswordVisible,
                            onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
                        )

                        AnimatedVisibility(
                            visible = errorMessage.isNotEmpty(),
                            enter   = fadeIn() + slideInVertically { -6 },
                            exit    = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Error.copy(alpha = 0.07f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline, null,
                                    tint     = Error,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    errorMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Error)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Text(
                            "By creating an account you agree to Forge's Terms of Service and Privacy Policy.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color      = TextSecondary,
                                textAlign  = TextAlign.Center,
                                lineHeight = 17.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        RegisterButton(
                            isLoading = isLoading,
                            onClick   = {
                                if (validate()) {
                                    isLoading = true
                                    scope.launch {
                                        val result = registerWithEmail(name, email, password)
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            result.fold(
                                                onSuccess = {
                                                    // ✅ Account created — send user to login screen.
                                                    // popUpTo("register") removes the register screen from
                                                    // the back stack so pressing Back on the login screen
                                                    // does NOT return to register (and does NOT close the app
                                                    // if there are screens behind register, e.g. onboarding).
                                                    navController.navigate("login") {
                                                        popUpTo("register") { inclusive = true }
                                                    }
                                                },
                                                onFailure = { e ->
                                                    errorMessage = when {
                                                        e.message?.contains("email address is already") == true ->
                                                            "This email is already registered."
                                                        e.message?.contains("badly formatted") == true ->
                                                            "Please enter a valid email address."
                                                        else -> e.message ?: "Registration failed. Try again."
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Text(
                        "Sign in",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = GoldPrimary,
                            fontWeight = FontWeight.W600
                        ),
                        modifier = Modifier.clickable { navController.navigate(ROUTE_Login) }
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO HEADER
// ─────────────────────────────────────────────────────────────────
@Composable
private fun RegisterHeroHeader() {
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
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldPrimary.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.1f),
                        radius = size.width * 0.55f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF0F6E56).copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height * 0.95f),
                        radius = size.width * 0.45f
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, top = 52.dp, end = 28.dp, bottom = 44.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(GoldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "F",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color      = DarkSurface,
                            fontWeight = FontWeight.W700
                        )
                    )
                }
                Text(
                    "Forge",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color         = TextOnDark,
                        fontWeight    = FontWeight.W600,
                        letterSpacing = (-0.3).sp
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Start building",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = Color(0xFF8A8A8A),
                        fontWeight = FontWeight.W400
                    )
                )
                Text(
                    "your edge.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color         = TextOnDark,
                        fontWeight    = FontWeight.W600,
                        letterSpacing = (-0.4).sp
                    )
                )
                Text(
                    "Join thousands of creators growing on Forge.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = Color(0xFF8A8A8A),
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PASSWORD STRENGTH METER
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PasswordStrengthMeter(strength: Int) {
    val labels = listOf("", "Weak", "Fair", "Good", "Strong")
    val colors = listOf(Color.Transparent, Error, SoftPeach, SoftBlue, SoftGreen)
    val strengthColor = colors.getOrElse(strength) { Color.Transparent }
    val strengthLabel = labels.getOrElse(strength) { "" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Password strength", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Text(
                strengthLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color      = strengthColor,
                    fontWeight = FontWeight.W600
                )
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                val filled = index < strength
                val animatedColor by animateColorAsState(
                    targetValue   = if (filled) strengthColor else BackgroundSecondary,
                    animationSpec = tween(300),
                    label         = "strength_bar_$index"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(animatedColor)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  REG INPUT FIELD
// ─────────────────────────────────────────────────────────────────
@Composable
private fun RegInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    hint: String,
    keyboardType: KeyboardType   = KeyboardType.Text,
    imeAction: ImeAction         = ImeAction.Next,
    showError: Boolean           = false,
    isRequired: Boolean          = false,
    isPassword: Boolean          = false,
    passwordVisible: Boolean     = false,
    onTogglePassword: () -> Unit = {}
) {
    val isFilled   = value.isNotEmpty()
    val iconTint   = when { showError -> Error; isFilled -> GoldPrimary; else -> Color(0xFF9E9E9E) }
    val iconBg     = when { showError -> Error.copy(alpha = 0.08f); isFilled -> GoldPrimary.copy(alpha = 0.10f); else -> BackgroundSecondary }
    val labelColor = when { showError -> Error; isFilled -> GoldPrimary; else -> TextSecondary }
    val underline: Brush = when {
        showError -> Brush.linearGradient(listOf(Error, Error.copy(alpha = 0.6f)))
        isFilled  -> GoldGradient
        else      -> Brush.linearGradient(listOf(BackgroundSecondary, BackgroundSecondary))
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
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
                            fontWeight    = FontWeight.W500,
                            letterSpacing = 0.4.sp
                        )
                    )
                    if (isRequired) {
                        Text(
                            " *",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (showError) Error else GoldPrimary
                            )
                        )
                    }
                }
                TextField(
                    value         = value,
                    onValueChange = onValueChange,
                    placeholder   = {
                        Text(
                            hint,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color    = Color(0xFFCCCCCC),
                                fontSize = 14.sp
                            )
                        )
                    },
                    singleLine      = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType   = keyboardType,
                        imeAction      = imeAction,
                        capitalization = if (!isPassword && keyboardType != KeyboardType.Email)
                            KeyboardCapitalization.Words else KeyboardCapitalization.None
                    ),
                    visualTransformation = if (isPassword && !passwordVisible)
                        PasswordVisualTransformation() else VisualTransformation.None,
                    trailingIcon = if (isPassword) {
                        {
                            IconButton(onClick = onTogglePassword, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password",
                                    tint     = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null,
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(if (showError) 1.5.dp else 1.dp)
                .background(underline)
        )
    }
}

@Composable
private fun RegDivider() {
    HorizontalDivider(
        color     = BackgroundSecondary,
        thickness = 0.5.dp,
        modifier  = Modifier.padding(vertical = 6.dp)
    )
}

// ─────────────────────────────────────────────────────────────────
//  REGISTER BUTTON
// ─────────────────────────────────────────────────────────────────
@Composable
private fun RegisterButton(isLoading: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "reg_btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(GoldGradient)
            .clickable(
                interactionSource = interactionSource,
                indication        = ripple(color = DarkSurface.copy(alpha = 0.15f)),
                onClick           = onClick,
                enabled           = !isLoading
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "reg_btn_content"
        ) { loading ->
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        color       = DarkSurface,
                        strokeWidth = 2.dp,
                        modifier    = Modifier.size(18.dp)
                    )
                    Text(
                        "Creating your account…",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color      = DarkSurface,
                            fontWeight = FontWeight.W600
                        )
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Create account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color         = DarkSurface,
                            fontWeight    = FontWeight.W700,
                            letterSpacing = 0.3.sp
                        )
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(DarkSurface.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint     = DarkSurface,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPERS
// ─────────────────────────────────────────────────────────────────
private fun DrawScope.drawDotGrid(color: Color, spacing: Float, radius: Float) {
    val cols = (size.width  / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2
    for (col in 0..cols) {
        for (row in 0..rows) {
            drawCircle(color = color, radius = radius, center = Offset(col * spacing, row * spacing))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(rememberNavController())
}