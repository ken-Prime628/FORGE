package com.kennedy.forge.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.navigation.ROUT_Dashboard

// ─── Colour Tokens ───────────────────────────────────────────────────────────

val BackgroundMain      = Color(0xFFF5F2EC)
val BackgroundSecondary = Color(0xFFEDE7DD)
val CardBackground      = Color(0xFFFFFFFF)

val DarkSurface = Color(0xFF121212)
val DarkCard    = Color(0xFF1C1C1C)

val GoldPrimary = Color(0xFFC89B3C)
val GoldAccent  = Color(0xFFE6B85C)
val GoldDeep    = Color(0xFFA67C2E)

val SoftGreen = Color(0xFF7FBF9F)
val SoftBlue  = Color(0xFF7DAED3)
val SoftPeach = Color(0xFFE8A87C)
val SoftOlive = Color(0xFFB5A27A)

val TextPrimary   = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF6F6F6F)
val TextOnDark    = Color(0xFFFFFFFF)
val TextGold      = Color(0xFFC89B3C)

val GoldGradient = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
val DarkGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2A2520), DarkSurface),
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

// ─── Level Data ──────────────────────────────────────────────────────────────

data class LevelInfo(
    val title: String,
    val subtitle: String,
    val description: String,
    val emoji: String,
    val accentColor: Color,
    val lightTint: Color
)

val allLevels = listOf(
    LevelInfo(
        title = "Beginner",
        subtitle = "Starting the journey",
        description = "New to the craft, building foundations",
        emoji = "🌱",
        accentColor = SoftBlue,
        lightTint = Color(0xFFEAF4FA)
    ),
    LevelInfo(
        title = "Elementary",
        subtitle = "Grasping the basics",
        description = "Core concepts are becoming familiar",
        emoji = "📖",
        accentColor = SoftGreen,
        lightTint = Color(0xFFEAF5EF)
    ),
    LevelInfo(
        title = "Intermediate",
        subtitle = "Building momentum",
        description = "Applying knowledge with growing confidence",
        emoji = "⚡",
        accentColor = SoftOlive,
        lightTint = Color(0xFFF3F0E7)
    ),
    LevelInfo(
        title = "Upper Intermediate",
        subtitle = "Gaining fluency",
        description = "Handling complex challenges with ease",
        emoji = "🔥",
        accentColor = SoftPeach,
        lightTint = Color(0xFFFDF2EA)
    ),
    LevelInfo(
        title = "Advanced",
        subtitle = "Near the summit",
        description = "Operating at a high level consistently",
        emoji = "🎯",
        accentColor = GoldAccent,
        lightTint = Color(0xFFFDF6E3)
    ),
    LevelInfo(
        title = "Proficiency",
        subtitle = "Mastery achieved",
        description = "Peak performance, leading the way",
        emoji = "👑",
        accentColor = GoldPrimary,
        lightTint = Color(0xFFFDF4E0)
    ),
)

private const val MAX_SELECTIONS = 3

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun SkillAssessmentScreen(navController: NavController) {

    val selectedLevels = remember { mutableStateListOf<String>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {

            item { HeroHeader(selectedCount = selectedLevels.size) }

            itemsIndexed(allLevels) { index, info ->
                val isSelected  = selectedLevels.contains(info.title)
                val isDisabled  = !isSelected && selectedLevels.size >= MAX_SELECTIONS

                LevelCard(
                    info       = info,
                    isSelected = isSelected,
                    isDisabled = isDisabled,
                    onClick    = {
                        if (isSelected) {
                            selectedLevels.remove(info.title)
                        } else if (selectedLevels.size < MAX_SELECTIONS) {
                            selectedLevels.add(info.title)
                        }
                    }
                )
            }
        }

        StickyBottomBar(
            selectedLevels = selectedLevels.toList(),
            onConfirm = {
                if (selectedLevels.size == MAX_SELECTIONS) {
                    navController.navigate(ROUT_Dashboard) {
                        popUpTo("skill_assessment") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Hero Header ─────────────────────────────────────────────────────────────

@Composable
fun HeroHeader(selectedCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1E1A15), Color(0xFF121212))
                )
            )
            .padding(start = 28.dp, end = 28.dp, top = 56.dp, bottom = 36.dp)
    ) {
        Column {

            // Pill label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(GoldPrimary.copy(alpha = 0.18f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "SKILL ASSESSMENT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp,
                    color = GoldAccent
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Choose\nyour\nlevels.",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 56.sp,
                letterSpacing = (-1.5).sp,
                color = TextOnDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(GoldGradient)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pick exactly 3 levels that represent your experience across different areas. Forge crafts your path from here.",
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = TextOnDark.copy(alpha = 0.6f),
                letterSpacing = 0.1.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Progress pips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(MAX_SELECTIONS) { i ->
                    val filled = i < selectedCount
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (filled) GoldGradient
                                else Brush.horizontalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.12f),
                                        Color.White.copy(alpha = 0.12f)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = when {
                    selectedCount == 0              -> "Select 3 levels to continue"
                    selectedCount < MAX_SELECTIONS  -> "${MAX_SELECTIONS - selectedCount} more to go…"
                    else                            -> "Perfect — ready to go! ✦"
                },
                fontSize = 12.sp,
                color = if (selectedCount == MAX_SELECTIONS) GoldAccent else TextOnDark.copy(alpha = 0.45f),
                fontWeight = if (selectedCount == MAX_SELECTIONS) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ─── Level Card ──────────────────────────────────────────────────────────────

@Composable
fun LevelCard(
    info: LevelInfo,
    isSelected: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> DarkCard
            isDisabled -> BackgroundSecondary.copy(alpha = 0.55f)
            else       -> CardBackground
        },
        animationSpec = tween(260),
        label = "card_bg"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isDisabled && !isSelected) 0.38f else 1f,
        animationSpec = tween(260),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.975f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(24.dp))
                .background(bgColor)
                .then(
                    if (isSelected) Modifier.border(1.5.dp, GoldGradient, RoundedCornerShape(24.dp))
                    else Modifier
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isDisabled || isSelected
                ) { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (isSelected) info.accentColor.copy(alpha = 0.2f)
                            else info.lightTint
                        )
                        .graphicsLayer { alpha = contentAlpha },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = info.emoji, fontSize = 30.sp)
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = contentAlpha }
                ) {
                    Text(
                        text = info.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        color = if (isSelected) TextOnDark else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = info.subtitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) GoldAccent else info.accentColor
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = info.description,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = if (isSelected) TextOnDark.copy(alpha = 0.55f) else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Tick / empty circle
                AnimatedContent(
                    targetState = isSelected,
                    transitionSpec = {
                        (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(tween(200))).togetherWith(
                            scaleOut(tween(150)) + fadeOut(tween(100))
                        )
                    },
                    label = "tick"
                ) { selected ->
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(50))
                                .background(GoldGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✓", color = TextOnDark, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(50))
                                .background(BackgroundSecondary)
                                .border(1.5.dp, Color(0xFFD5CCBF), RoundedCornerShape(50))
                        )
                    }
                }
            }

            // Left accent strip when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                        .background(
                            Brush.verticalGradient(listOf(info.accentColor, GoldDeep))
                        )
                )
            }
        }
    }
}

// ─── Sticky Bottom CTA ───────────────────────────────────────────────────────

@Composable
fun StickyBottomBar(
    selectedLevels: List<String>,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ready = selectedLevels.size == MAX_SELECTIONS

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundMain.copy(alpha = 0f), BackgroundMain, BackgroundMain)
                )
            )
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Selected level chips
            AnimatedVisibility(
                visible = selectedLevels.isNotEmpty(),
                enter = fadeIn(tween(250)) + slideInVertically { it / 2 },
                exit  = fadeOut(tween(200))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    selectedLevels.forEach { lvl ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(DarkCard)
                                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(50))
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = lvl,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldAccent,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }

            // CTA button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (ready) GoldGradient
                        else Brush.horizontalGradient(
                            listOf(BackgroundSecondary, BackgroundSecondary)
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = ready
                    ) { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (ready) "Start My Journey"
                        else "Pick ${MAX_SELECTIONS - selectedLevels.size} more level${if (MAX_SELECTIONS - selectedLevels.size != 1) "s" else ""}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp,
                        color = if (ready) TextOnDark else TextSecondary
                    )
                    if (ready) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("→", color = TextOnDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFF5F2EC, showSystemUi = true)
@Composable
fun PreviewSkillAssessment() {
    SkillAssessmentScreen(rememberNavController())
}