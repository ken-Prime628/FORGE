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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.kennedy.forge.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPER — sign in with email + password
// ─────────────────────────────────────────────────────────────────

suspend fun loginWithEmail(
    email: String,
    password: String
): Result<String> = runCatching {
    val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()
    val firebaseUser = result.user ?: error("Sign-in failed")
    firebaseUser.uid
}

// ─────────────────────────────────────────────────────────────────
//  LOGIN SCREEN
// ─────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(navController: NavController) {

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading       by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }
    var emailError      by remember { mutableStateOf(false) }
    var passwordError   by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun validate(): Boolean {
        emailError    = email.isBlank() || !email.contains("@")
        passwordError = password.length < 6
        return when {
            emailError    -> { errorMessage = "Enter a valid email address"; false }
            passwordError -> { errorMessage = "Password must be at least 6 characters"; false }
            else          -> { errorMessage = ""; true }
        }
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

            LoginHeroHeader()

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
                                "Sign in to your account",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color         = TextSecondary,
                                    fontWeight    = FontWeight.W500,
                                    letterSpacing = 0.6.sp
                                )
                            )
                        }

                        // EMAIL
                        AuthInputField(
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

                        Spacer(Modifier.height(4.dp))
                        AuthFieldDivider()
                        Spacer(Modifier.height(4.dp))

                        // PASSWORD
                        AuthInputField(
                            label            = "Password",
                            value            = password,
                            onValueChange    = { password = it; passwordError = false; errorMessage = "" },
                            icon             = Icons.Default.Lock,
                            hint             = "At least 6 characters",
                            keyboardType     = KeyboardType.Password,
                            imeAction        = ImeAction.Done,
                            showError        = passwordError,
                            isRequired       = true,
                            isPassword       = true,
                            passwordVisible  = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )

                        // Forgot password
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                "Forgot password?",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color      = GoldPrimary,
                                    fontWeight = FontWeight.W500
                                ),
                                modifier = Modifier.clickable {
                                    navController.navigate("forgot_password")
                                }
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Error banner
                        AnimatedVisibility(
                            visible = errorMessage.isNotEmpty(),
                            enter   = fadeIn() + slideInVertically { -6 },
                            exit    = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
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
                            Spacer(Modifier.height(12.dp))
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── SIGN IN — calls Firebase ──────────────────
                        LoginButton(
                            isLoading = isLoading,
                            onClick   = {
                                if (validate()) {
                                    isLoading = true
                                    scope.launch {
                                        val result = loginWithEmail(email, password)
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            result.fold(
                                                onSuccess = {
                                                    navController.navigate("home") {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                },
                                                onFailure = { e ->
                                                    errorMessage = when {
                                                        e.message?.contains("no user record") == true ||
                                                                e.message?.contains("user-not-found") == true ->
                                                            "No account found with this email."
                                                        e.message?.contains("password is invalid") == true ||
                                                                e.message?.contains("wrong-password") == true ->
                                                            "Incorrect password. Please try again."
                                                        e.message?.contains("too many requests") == true ->
                                                            "Too many attempts. Please wait and try again."
                                                        e.message?.contains("network") == true ->
                                                            "Network error. Check your connection."
                                                        else -> e.message ?: "Sign-in failed. Try again."
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

                // Register link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    Text(
                        "Create one",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = GoldPrimary,
                            fontWeight = FontWeight.W600
                        ),
                        modifier = Modifier.clickable { navController.navigate("register") }
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO HEADER  (unchanged)
// ─────────────────────────────────────────────────────────────────
@Composable
private fun LoginHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
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
                        center = Offset(size.width * 0.88f, size.height * 0.08f),
                        radius = size.width * 0.55f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF534AB7).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.05f, size.height * 0.95f),
                        radius = size.width * 0.45f
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 28.dp, top = 56.dp, end = 28.dp, bottom = 48.dp),
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
                    "Welcome back,",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = Color(0xFF8A8A8A),
                        fontWeight = FontWeight.W400
                    )
                )
                Text(
                    "creator.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color         = TextOnDark,
                        fontWeight    = FontWeight.W600,
                        letterSpacing = (-0.4).sp
                    )
                )
                Text(
                    "Continue where you left off.",
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
//  AUTH INPUT FIELD  (unchanged)
// ─────────────────────────────────────────────────────────────────
@Composable
private fun AuthInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
                        keyboardType = keyboardType,
                        imeAction    = imeAction
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
private fun AuthFieldDivider() {
    HorizontalDivider(
        color     = BackgroundSecondary,
        thickness = 0.5.dp,
        modifier  = Modifier.padding(vertical = 6.dp)
    )
}

// ─────────────────────────────────────────────────────────────────
//  LOGIN BUTTON  (unchanged)
// ─────────────────────────────────────────────────────────────────
@Composable
private fun LoginButton(isLoading: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "login_btn_scale"
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
                onClick           = {} ,
                enabled           = !isLoading
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "login_btn_content"
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
                        "Signing in…",
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
                        "Sign in",
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
//  CANVAS HELPERS  (unchanged)
// ─────────────────────────────────────────────────────────────────
private fun DrawScope.drawDotGrid(color: Color, spacing: Float, radius: Float) {
    val cols = (size.width / spacing).toInt() + 2
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
fun LoginScreenPreview() {
    LoginScreen(rememberNavController())
}