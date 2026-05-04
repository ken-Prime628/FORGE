package com.kennedy.forge.ui.screens.feedback

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

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

private val GoldGradient = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
private val DarkGradient = Brush.verticalGradient(listOf(DarkDeep, DarkSurface))

// ─── Realistic Data Models ───────────────────────────────────────────────────

public final data class FeedbackItem(
    public final val reviewer: String,
    public final val role: String,
    public final val comment: String,
    public final val rating: Int,
    public final val timeAgo: String,
    public final val avatarColor: Color,
    public final val helpful: Int
)

data class WorkStat(
    val label: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color
)

// ─── Seed Data ───────────────────────────────────────────────────────────────

private val feedbackData = listOf(
    FeedbackItem(
        reviewer    = "Alex Monroe",
        role        = "Senior Designer",
        comment     = "Excellent concept execution. The visual hierarchy is strong but I'd tighten the spacing on mobile breakpoints — 8dp instead of 12dp between elements. Overall a really polished piece of work.",
        rating      = 5,
        timeAgo     = "2h ago",
        avatarColor = SoftBlue,
        helpful     = 14
    ),
    FeedbackItem(
        reviewer    = "Jordan Kim",
        role        = "UX Mentor",
        comment     = "Very strong visual direction! The colour palette feels intentional. Consider adding more micro-interactions on the CTAs — they'd elevate this from great to exceptional.",
        rating      = 5,
        timeAgo     = "5h ago",
        avatarColor = SoftGreen,
        helpful     = 9
    ),
    FeedbackItem(
        reviewer    = "Priya Sharma",
        role        = "Product Lead",
        comment     = "Love the ambition here. The layout breathes well. I'd revisit the typography scale — your heading and body sizes are close; push them further apart for contrast.",
        rating      = 4,
        timeAgo     = "1d ago",
        avatarColor = SoftPeach,
        helpful     = 21
    ),
    FeedbackItem(
        reviewer    = "Marcus Webb",
        role        = "Creative Director",
        comment     = "Solid foundations. The dark-and-gold brand direction has real character. Keep iterating — this shows genuine potential.",
        rating      = 4,
        timeAgo     = "2d ago",
        avatarColor = SoftOlive,
        helpful     = 6
    ),
)

private val workStats = listOf(
    WorkStat("Views",      "1.2k",  Icons.Default.Visibility,    SoftBlue),
    WorkStat("Feedback",   "4",     Icons.Default.ChatBubble,    SoftGreen),
    WorkStat("Avg Rating", "4.5★",  Icons.Default.Star,          GoldAccent),
    WorkStat("Helpful",    "50",    Icons.Default.ThumbUp,       SoftPeach),
)

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun FeedbackDashboardScreen(navController: NavController) {

    val helpfulStates = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Hero project card ──────────────────────────────────────────
            item { ProjectHeroCard() }

            // ── Stats row ─────────────────────────────────────────────────
            item { StatsRow() }

            // ── About section ─────────────────────────────────────────────
            item { AboutSection() }

            // ── Feedback header ───────────────────────────────────────────
            item { FeedbackSectionHeader(count = feedbackData.size) }

            // ── Feedback cards ────────────────────────────────────────────
            items(feedbackData) { feedback ->
                FeedbackCard(
                    item = feedback,
                    isHelpful = helpfulStates[feedback.reviewer] == true,
                    onHelpful = {
                        helpfulStates[feedback.reviewer] =
                            !(helpfulStates[feedback.reviewer] ?: false)
                    }
                )
            }

            // ── Invite banner ─────────────────────────────────────────────
            item { InviteBanner() }
        }

        // ── Floating bottom nav ────────────────────────────────────────────
        FeedbackBottomNav(
            navController = navController,
            modifier      = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Project Hero Card ───────────────────────────────────────────────────────

@Composable
fun ProjectHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(DarkGradient)
    ) {
        // Decorative geometric blobs
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 20.dp)
                .clip(CircleShape)
                .background(SoftBlue.copy(alpha = 0.08f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: label + status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(GoldPrimary.copy(alpha = 0.18f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        "FEEDBACK DASHBOARD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = GoldAccent
                    )
                }
                // Live badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SoftGreen.copy(alpha = 0.15f))
                        .border(1.dp, SoftGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SoftGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open for review", fontSize = 10.sp, color = SoftGreen, fontWeight = FontWeight.SemiBold)
                }
            }

            // Project title block
            Column {
                Text(
                    text = "Forge\nMobile App",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 48.sp,
                    letterSpacing = (-1.2).sp,
                    color = TextOnDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(50))
                        .background(GoldGradient)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "UI / UX Design  ·  Jetpack Compose  ·  2025",
                    fontSize = 12.sp,
                    color = TextOnDark.copy(alpha = 0.45f),
                    letterSpacing = 0.3.sp
                )
            }
        }

        // Bottom gradient fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, BackgroundMain.copy(alpha = 0.5f))
                    )
                )
        )
    }
}

// ─── Stats Row ───────────────────────────────────────────────────────────────

@Composable
fun StatsRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(workStats) { stat ->
            StatChip(stat)
        }
    }
}

@Composable
fun StatChip(stat: WorkStat) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(stat.tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(stat.icon, null, tint = stat.tint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                stat.value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = (-0.3).sp
            )
            Text(
                stat.label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── About Section ───────────────────────────────────────────────────────────

@Composable
fun AboutSection() {
    var expanded by remember { mutableStateOf(false) }

    val fullText = "Forge is a skill-building mobile app built with Jetpack Compose. The goal was to create a premium onboarding experience with rich dark-and-gold brand language, fluid animations, and a multi-select level assessment flow. Submitted for UX and visual design feedback."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        DashSectionLabel("About This Work")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBackground)
                .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = fullText,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = TextSecondary,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (expanded) "Show less ↑" else "Read more ↓",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = !expanded }
                )
            }
        }
    }
}

// ─── Feedback Section Header ──────────────────────────────────────────────────

@Composable
fun FeedbackSectionHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(GoldGradient)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Community Feedback",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(DarkCard)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                "$count reviews",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldAccent
            )
        }
    }
}

// ─── Feedback Card ───────────────────────────────────────────────────────────

@Composable
fun FeedbackCard(
    item: FeedbackItem,
    isHelpful: Boolean,
    onHelpful: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .border(1.dp, Color(0xFFECE4D8), RoundedCornerShape(24.dp))
    ) {
        // Left accent strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(
                    Brush.verticalGradient(listOf(item.avatarColor, GoldDeep))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        ) {
            // Reviewer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(item.avatarColor.copy(alpha = 0.2f))
                            .border(2.dp, item.avatarColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.reviewer.first().toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = item.avatarColor
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            item.reviewer,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            item.role,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Time
                Text(
                    item.timeAgo,
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Star rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        imageVector = if (i < item.rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (i < item.rating) GoldAccent else Color(0xFFD8D0C4),
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "${item.rating}.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comment
            Text(
                text = item.comment,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = TextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (item.comment.length > 80) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (expanded) "Show less" else "Read more",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextGold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = !expanded }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF0EAE0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Footer: helpful
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHelpful
                        )
                        .background(
                            if (isHelpful) GoldPrimary.copy(alpha = 0.1f)
                            else BackgroundSecondary
                        )
                        .border(
                            1.dp,
                            if (isHelpful) GoldPrimary.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = if (isHelpful) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Helpful",
                        tint = if (isHelpful) GoldPrimary else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Helpful · ${if (isHelpful) item.helpful + 1 else item.helpful}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isHelpful) GoldPrimary else TextSecondary
                    )
                }

                // Reply chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(DarkCard)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Default.Reply, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Reply", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─── Invite Banner ───────────────────────────────────────────────────────────

@Composable
fun InviteBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DarkGradient)
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Want more eyes on your work?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = TextOnDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Share your submission link and invite peers to leave feedback.",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = TextOnDark.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldGradient)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, null, tint = TextOnDark, modifier = Modifier.size(17.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Share Submission",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextOnDark
                    )
                }
            }
        }
    }
}

// ─── Section Label ───────────────────────────────────────────────────────────

@Composable
fun DashSectionLabel(text: String) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(GoldGradient)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

// ─── Bottom Nav ──────────────────────────────────────────────────────────────

@Composable
fun FeedbackBottomNav(navController: NavController, modifier: Modifier = Modifier) {
    Surface(
        color = CardBackground,
        shadowElevation = 20.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedNavItem(
                icon     = Icons.Default.Home,
                label    = "Home",
                selected = false,
                modifier = Modifier.weight(1f),
                onClick  = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
            FeedNavItem(
                icon     = Icons.Default.Add,
                label    = "Submit",
                selected = false,
                modifier = Modifier.weight(1f),
                onClick  = { navController.navigate("submit_work") }
            )
            FeedNavItem(
                icon     = Icons.Default.ChatBubble,
                label    = "Feedback",
                selected = true,
                modifier = Modifier.weight(1f),
                onClick  = {}
            )
            FeedNavItem(
                icon     = Icons.Default.Person,
                label    = "Profile",
                selected = false,
                modifier = Modifier.weight(1f),
                onClick  = { navController.navigate("profile") }
            )
        }
    }
}

@Composable
fun FeedNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(GoldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextOnDark, modifier = Modifier.size(21.dp))
            }
        } else {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
fun FeedbackDashboardScreenPreview() {
    FeedbackDashboardScreen(rememberNavController())
}