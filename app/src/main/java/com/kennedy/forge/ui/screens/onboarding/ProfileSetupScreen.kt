package com.kennedy.forge.ui.screens.onboarding

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.theme.*

// ─────────────────────────────────────────────────────────────────
//  ProfileSetupScreen — Premium "Forge Identity" build screen
// ─────────────────────────────────────────────────────────────────
@Composable
fun ProfileSetupScreen(navController: NavController) {

    var name       by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var category   by remember { mutableStateOf("") }
    var bio        by remember { mutableStateOf("") }

    val isFormValid = name.isNotBlank() && profession.isNotBlank()

    // Staggered entrance animation
    val enterAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(700, easing = EaseOut),
        label = "enter"
    )

    // Validation shake animation
    var showValidationHint by remember { mutableStateOf(false) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (showValidationHint) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f at 0
            (-8f) at 50
            8f at 100
            (-6f) at 150
            6f at 200
            (-4f) at 250
            0f at 300
        },
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {
        // ── Subtle dot pattern background ────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawDotPattern(color = Color(0xFFC89B3C).copy(alpha = 0.05f), spacing = 32f, dotRadius = 1.5f)
        }

        // ── Scrollable body ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // Extra bottom padding so content clears the fixed footer
                .padding(bottom = 110.dp)
        ) {

            // ── Hero header ──────────────────────────────────────
            HeroHeader()

            // ── Avatar ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-44).dp),
                contentAlignment = Alignment.Center
            ) {
                PremiumAvatarPicker()
            }

            // ── Form section ─────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-24).dp)
            ) {
                // Section label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(18.dp)
                            .background(GoldGradient, RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Your creative identity",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = TextSecondary,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.W500
                        )
                    )
                }

                // ── Input card ───────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color(0xFFC89B3C).copy(alpha = 0.08f),
                            spotColor = Color(0xFFC89B3C).copy(alpha = 0.12f)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        ForgeInputField(
                            label = "Full Name",
                            value = name,
                            onValueChange = { name = it; showValidationHint = false },
                            icon = Icons.Default.Person,
                            hint = "e.g. Kennedy Osei",
                            isRequired = true,
                            showError = showValidationHint && name.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        ForgeFieldDivider()

                        ForgeInputField(
                            label = "Profession",
                            value = profession,
                            onValueChange = { profession = it; showValidationHint = false },
                            icon = Icons.Default.Work,
                            hint = "e.g. Designer, Filmmaker, Writer",
                            isRequired = true,
                            showError = showValidationHint && profession.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        ForgeFieldDivider()

                        ForgeInputField(
                            label = "Creative category",
                            value = category,
                            onValueChange = { category = it },
                            icon = Icons.Default.Category,
                            hint = "e.g. UI/UX, Music Production, Fiction",
                            imeAction = ImeAction.Next
                        )

                        ForgeFieldDivider()

                        ForgeInputField(
                            label = "Short bio",
                            value = bio,
                            onValueChange = { bio = it },
                            icon = Icons.Default.Edit,
                            hint = "What drives your creative work?",
                            singleLine = false,
                            imeAction = ImeAction.Done,
                            minLines = 3
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Progress indicator ───────────────────────────
                StepProgress(currentStep = 1, totalSteps = 3)

                Spacer(Modifier.height(8.dp))

                // ── Required fields hint ─────────────────────────
                AnimatedVisibility(
                    visible = showValidationHint,
                    enter = fadeIn() + slideInVertically { -8 },
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = shakeOffset.dp)
                            .padding(top = 8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Error)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Please fill in your Name and Profession to continue",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Error,
                                fontWeight = FontWeight.W500
                            )
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Skip hint ────────────────────────────────────
                Text(
                    "You can update category and bio anytime from your profile",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Fixed bottom CTA bar ─────────────────────────────────
        FixedContinueBar(
            isFormValid = isFormValid,
            modifier = Modifier.align(Alignment.BottomCenter),
            onContinue = {
                if (isFormValid) {
                    // Navigate and clear the back-stack so pressing Back
                    // from SkillAssessment does not return here
                    navController.navigate("skill_assessment") {
                        popUpTo("profile_setup") { inclusive = false }
                        launchSingleTop = true
                    }
                } else {
                    showValidationHint = true
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  Hero Header
// ─────────────────────────────────────────────────────────────────
@Composable
private fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        // Dark base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                .background(DarkSurface)
        ) {
            // Decorative gold radial glow top-right
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFC89B3C).copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.1f),
                        radius = size.width * 0.55f
                    )
                )
                // Bottom-left subtle glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFE6B85C).copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.9f),
                        radius = size.width * 0.4f
                    )
                )
            }

            // Step badge + text
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp, top = 52.dp, end = 28.dp, bottom = 56.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Step badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFC89B3C).copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, Color(0xFFC89B3C).copy(alpha = 0.4f))
                ) {
                    Text(
                        "Step 1 of 3  ·  Profile setup",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFE6B85C),
                            letterSpacing = 0.6.sp
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "Build your",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFF6F6F6F),
                            fontWeight = FontWeight.W400,
                            letterSpacing = 0.2.sp
                        )
                    )
                    Text(
                        "creative identity.",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = TextOnDark,
                            fontWeight = FontWeight.W600,
                            letterSpacing = (-0.3).sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This profile represents you across feedback,\nportfolio, pitches, and collaborations.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF8A8A8A),
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Premium Avatar Picker
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PremiumAvatarPicker() {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "avatar_scale"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer ring — gold gradient
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(GoldGradient)
        )
        // White spacer ring
        Box(
            modifier = Modifier
                .size(94.dp)
                .clip(CircleShape)
                .background(BackgroundMain)
        )
        // Inner avatar circle
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(BackgroundSecondary)
                .clickable { pressed = !pressed },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add photo",
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Add photo",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                )
            }
        }

        // Gold dot badge at bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(GoldGradient)
                .border(2.dp, BackgroundMain, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Forge Input Field — premium styled
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ForgeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    hint: String,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
    minLines: Int = 1,
    isRequired: Boolean = false,
    showError: Boolean = false
) {
    val isFilled     = value.isNotEmpty()
    val iconTint     = when { showError -> Error; isFilled -> GoldPrimary; else -> Color(0xFF9E9E9E) }
    val iconBg       = when { showError -> Error.copy(alpha = 0.08f); isFilled -> GoldPrimary.copy(alpha = 0.10f); else -> Color(0xFFF5F2EC) }
    val labelColor   = when { showError -> Error; isFilled -> GoldPrimary; else -> TextSecondary }
    val underlineBrush: Brush = when {
        showError -> Brush.linearGradient(listOf(Error, Error.copy(alpha = 0.6f)))
        isFilled  -> GoldGradient
        else      -> Brush.linearGradient(listOf(Color(0xFFEDE7DD), Color(0xFFEDE7DD)))
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = labelColor,
                            fontWeight = FontWeight.W500,
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
                BasicForgeTextField(
                    value = value,
                    onValueChange = onValueChange,
                    hint = hint,
                    singleLine = singleLine,
                    imeAction = imeAction,
                    minLines = minLines
                )
            }
        }

        // Animated underline — thicker + red when error
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .height(if (showError) 1.5.dp else 1.dp)
                .background(underlineBrush)
        )
    }
}

@Composable
private fun BasicForgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    singleLine: Boolean,
    imeAction: ImeAction,
    minLines: Int
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                hint,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFFBBBBBB),
                    fontSize = 14.sp
                )
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = imeAction
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = GoldPrimary
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.W400
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
    )
}

@Composable
private fun ForgeFieldDivider() {
    Spacer(Modifier.height(4.dp))
    HorizontalDivider(
        color = Color(0xFFEDE7DD),
        thickness = 0.5.dp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// ─────────────────────────────────────────────────────────────────
//  Step progress bar
// ─────────────────────────────────────────────────────────────────
@Composable
private fun StepProgress(currentStep: Int, totalSteps: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Profile setup",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = GoldPrimary,
                    fontWeight = FontWeight.W500
                )
            )
            Text(
                "$currentStep / $totalSteps",
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(totalSteps) { i ->
                val isActive = i < currentStep
                val fraction = if (isActive) 1f else 0f
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (isActive) GoldGradient
                            else Brush.linearGradient(listOf(Color(0xFFEDE7DD), Color(0xFFEDE7DD)))
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Premium CTA button
// ─────────────────────────────────────────────────────────────────
@Composable
private fun PremiumButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.97f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) GoldGradient
                else Brush.linearGradient(listOf(Color(0xFFD8D0C4), Color(0xFFCCC5B8)))
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = if (enabled) Color(0xFF1A1A1A) else Color(0xFF9A9A9A),
                    fontWeight = FontWeight.W600,
                    letterSpacing = 0.3.sp
                )
            )
            if (enabled) {
                Spacer(Modifier.width(10.dp))
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Canvas helpers
// ─────────────────────────────────────────────────────────────────
private fun DrawScope.drawDotPattern(color: Color, spacing: Float, dotRadius: Float) {
    val cols = (size.width / spacing).toInt() + 1
    val rows = (size.height / spacing).toInt() + 1
    for (col in 0..cols) {
        for (row in 0..rows) {
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(col * spacing, row * spacing)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Fixed bottom CTA bar — always visible, always tappable
// ─────────────────────────────────────────────────────────────────
@Composable
private fun FixedContinueBar(
    isFormValid: Boolean,
    modifier: Modifier = Modifier,
    onContinue: () -> Unit
) {
    // Subtle top fade so content scrolls under gracefully
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Frosted glass backing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BackgroundMain.copy(alpha = 0f),
                            BackgroundMain.copy(alpha = 0.96f),
                            BackgroundMain
                        )
                    )
                )
                .padding(top = 16.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
        ) {
            Column {
                // Readiness indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFormValid) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Success)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Looking good — ready to continue",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Success,
                                fontWeight = FontWeight.W500
                            )
                        )
                    } else {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD8D0C4))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Fill in Name and Profession to continue",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary
                            )
                        )
                    }
                }

                // The button itself
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    ),
                    label = "btn_press_scale"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .scale(buttonScale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isFormValid) GoldGradient
                            else Brush.linearGradient(listOf(Color(0xFFD8D0C4), Color(0xFFCCC5B8)))
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(
                                color = if (isFormValid) DarkSurface.copy(alpha = 0.15f)
                                else Color.Transparent
                            ),
                            onClick = onContinue
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Continue to assessment",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isFormValid) DarkSurface else Color(0xFF9A9A9A),
                                fontWeight = FontWeight.W600,
                                letterSpacing = 0.3.sp
                            )
                        )
                        if (isFormValid) {
                            Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = "Navigate to assessment",
                                    tint = DarkSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Preview
// ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileSetupScreenPreview() {
    ProfileSetupScreen(rememberNavController())
}