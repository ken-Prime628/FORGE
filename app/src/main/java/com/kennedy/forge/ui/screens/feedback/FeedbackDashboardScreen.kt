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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_EditProject
import com.kennedy.forge.navigation.ROUT_Profile
import com.kennedy.forge.navigation.ROUT_ProjectDetail
import com.kennedy.forge.navigation.ROUT_SubmitWork

// ─────────────────────────────────────────────────────────────────
//  NAVIGATION ROUTE CONSTANTS
//  Add these to your navigation graph / Routes file if not already there:
//
//  const val ROUT_ProjectDetails  = "project_details/{projectId}"
//  const val ROUT_FeedbackDetails = "feedback_details/{feedbackId}"
//  const val ROUT_EditProject     = "edit_project/{projectId}"
//
//  In NavHost:
//  composable("project_details/{projectId}")  { ProjectDetailsScreen(navController, it.arguments?.getString("projectId")) }
//  composable("feedback_details/{feedbackId}") { FeedbackDetailsScreen(navController, it.arguments?.getString("feedbackId")) }
//  composable("edit_project/{projectId}")     { EditProjectScreen(navController, it.arguments?.getString("projectId")) }
// ─────────────────────────────────────────────────────────────────

// ─── Colour Tokens ───────────────────────────────────────────────────────────

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
private val SoftBlue            = Color(0xFF7DAED3)
private val SoftPeach           = Color(0xFFE8A87C)
private val SoftOlive           = Color(0xFFB5A27A)
private val TextPrimary         = Color(0xFF1A1A1A)
private val TextSecondary       = Color(0xFF6F6F6F)
private val TextOnDark          = Color(0xFFFFFFFF)
private val TextGold            = Color(0xFFC89B3C)
private val GoldGradient        = Brush.linearGradient(listOf(GoldAccent, GoldPrimary))
private val DarkGradient        = Brush.verticalGradient(listOf(DarkDeep, DarkSurface))

// ─── Mock project ID (replace with real data from DB / nav args) ──────────────
private const val MOCK_PROJECT_ID = "forge_mobile_2025"

// ─── Data Models ─────────────────────────────────────────────────────────────

data class FeedbackItem(
    val id: String,
    val reviewer: String,
    val role: String,
    val comment: String,
    val rating: Int,
    val timeAgo: String,
    val avatarColor: Color,
    val helpful: Int
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
        id          = "fb_001",
        reviewer    = "Alex Monroe",
        role        = "Senior Designer",
        comment     = "Excellent concept execution. The visual hierarchy is strong but I'd tighten the spacing on mobile breakpoints — 8dp instead of 12dp between elements. Overall a really polished piece of work.",
        rating      = 5,
        timeAgo     = "2h ago",
        avatarColor = SoftBlue,
        helpful     = 14
    ),
    FeedbackItem(
        id          = "fb_002",
        reviewer    = "Jordan Kim",
        role        = "UX Mentor",
        comment     = "Very strong visual direction! The colour palette feels intentional. Consider adding more micro-interactions on the CTAs — they'd elevate this from great to exceptional.",
        rating      = 5,
        timeAgo     = "5h ago",
        avatarColor = SoftGreen,
        helpful     = 9
    ),
    FeedbackItem(
        id          = "fb_003",
        reviewer    = "Priya Sharma",
        role        = "Product Lead",
        comment     = "Love the ambition here. The layout breathes well. I'd revisit the typography scale — your heading and body sizes are close; push them further apart for contrast.",
        rating      = 4,
        timeAgo     = "1d ago",
        avatarColor = SoftPeach,
        helpful     = 21
    ),
    FeedbackItem(
        id          = "fb_004",
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
    WorkStat("Views",      "1.2k",  Icons.Default.Visibility,  SoftBlue),
    WorkStat("Feedback",   "4",     Icons.Default.ChatBubble,  SoftGreen),
    WorkStat("Avg Rating", "4.5★",  Icons.Default.Star,        GoldAccent),
    WorkStat("Helpful",    "50",    Icons.Default.ThumbUp,     SoftPeach),
)

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────

@Composable
fun FeedbackDashboardScreen(navController: NavController) {

    val helpfulStates = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundMain)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            // ── Hero project card ──────────────────────────────────
            item {
                ProjectHeroCard(
                    onViewDetails = {
                        navController.navigate(ROUT_ProjectDetail)
                    },
                    onEditProject = {
                        navController.navigate(ROUT_EditProject)
                    }
                )
            }

            // ── Stats row ─────────────────────────────────────────
            item { StatsRow() }

            // ── About section ─────────────────────────────────────
            item {
                AboutSection(
                    onViewFull = {
                        navController.navigate("project_details/$MOCK_PROJECT_ID")
                    }
                )
            }

            // ── Feedback header ───────────────────────────────────
            item {
                FeedbackSectionHeader(
                    count = feedbackData.size,
                    onViewAll = {
                        // Navigate to feedback list for this project
                        navController.navigate("feedback_details/$MOCK_PROJECT_ID")
                    }
                )
            }

            // ── Feedback cards ────────────────────────────────────
            items(feedbackData) { feedback ->
                FeedbackCard(
                    item      = feedback,
                    isHelpful = helpfulStates[feedback.id] == true,
                    onHelpful = {
                        helpfulStates[feedback.id] = !(helpfulStates[feedback.id] ?: false)
                    },
                    onViewDetail = {
                        // Navigate to full feedback thread for this specific feedback item
                        navController.navigate("feedback_details/${feedback.id}")
                    }
                )
            }

            // ── Invite banner ─────────────────────────────────────
            item { InviteBanner() }
        }

        // ── Floating bottom nav ────────────────────────────────────
        FeedbackBottomNav(
            navController = navController,
            modifier      = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  PROJECT HERO CARD
//  ➕ Two icon buttons added (top-right area):
//     ✏️ Edit  →  edit_project/{projectId}
//     👁 View  →  project_details/{projectId}
// ─────────────────────────────────────────────────────────────────

@Composable
fun ProjectHeroCard(
    onViewDetails: () -> Unit,
    onEditProject: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().height(320.dp).background(DarkGradient)
    ) {
        // Decorative blobs (unchanged)
        Box(modifier = Modifier.size(220.dp).offset(x = (-40).dp, y = (-40).dp)
            .clip(CircleShape).background(GoldPrimary.copy(alpha = 0.07f)))
        Box(modifier = Modifier.size(160.dp).align(Alignment.TopEnd).offset(x = 40.dp, y = 20.dp)
            .clip(CircleShape).background(SoftBlue.copy(alpha = 0.08f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: label + status + action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold pill label
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50))
                        .background(GoldPrimary.copy(alpha = 0.18f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text("FEEDBACK DASHBOARD", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp, color = GoldAccent)
                }

                // Status badge + action icon buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Live badge (unchanged)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .background(SoftGreen.copy(alpha = 0.15f))
                            .border(1.dp, SoftGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SoftGreen))
                        Spacer(Modifier.width(6.dp))
                        Text("Open for review", fontSize = 10.sp, color = SoftGreen, fontWeight = FontWeight.SemiBold)
                    }

                    // ✏️ Edit project icon button
                    Box(
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(0.10f))
                            .border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onEditProject),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, "Edit project", tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }

                    // 👁 View project details icon button
                    Box(
                        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                            .background(GoldPrimary.copy(0.20f))
                            .border(1.dp, GoldPrimary.copy(0.35f), RoundedCornerShape(10.dp))
                            .clickable(onClick = onViewDetails),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Visibility, "View details", tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Project title block (unchanged)
            Column {
                Text(
                    text = "Forge\nMobile App",
                    fontSize = 44.sp, fontWeight = FontWeight.Black,
                    lineHeight = 48.sp, letterSpacing = (-1.2).sp, color = TextOnDark
                )
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.width(48.dp).height(3.dp).clip(RoundedCornerShape(50)).background(GoldGradient))
                Spacer(Modifier.height(10.dp))
                Text("UI / UX Design  ·  Jetpack Compose  ·  2025",
                    fontSize = 12.sp, color = TextOnDark.copy(alpha = 0.45f), letterSpacing = 0.3.sp)

                Spacer(Modifier.height(14.dp))

                // ── View Full Project button (text link style) ────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary.copy(0.12f))
                        .border(1.dp, GoldPrimary.copy(0.3f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onViewDetails)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("View Full Project", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Bottom gradient fade (unchanged)
        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundMain.copy(alpha = 0.5f))))
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  STATS ROW  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun StatsRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(workStats) { stat -> StatChip(stat) }
    }
}

@Composable
fun StatChip(stat: WorkStat) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(CardBackground)
            .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
            .background(stat.tint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(stat.icon, null, tint = stat.tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(stat.value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.3).sp)
            Text(stat.label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ABOUT SECTION
//  ➕ "View full project →" tappable link at the bottom
// ─────────────────────────────────────────────────────────────────

@Composable
fun AboutSection(onViewFull: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val fullText = "Forge is a skill-building mobile app built with Jetpack Compose. The goal was to create a premium onboarding experience with rich dark-and-gold brand language, fluid animations, and a multi-select level assessment flow. Submitted for UX and visual design feedback."

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        DashSectionLabel("About This Work")

        Box(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(CardBackground)
                .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(20.dp)).padding(20.dp)
        ) {
            Column {
                Text(
                    text = fullText, fontSize = 14.sp, lineHeight = 22.sp, color = TextSecondary,
                    maxLines = if (expanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Read more / less toggle (unchanged)
                    Text(
                        text = if (expanded) "Show less ↑" else "Read more ↓",
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextGold,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null
                        ) { expanded = !expanded }
                    )

                    // ── View full project details button ─────────
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(0.08f))
                            .border(1.dp, GoldPrimary.copy(0.2f), RoundedCornerShape(8.dp))
                            .clickable(onClick = onViewFull)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, "View project", tint = GoldPrimary, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Full Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GoldPrimary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  FEEDBACK SECTION HEADER
//  ➕ "View all →" button navigates to full feedback list
// ─────────────────────────────────────────────────────────────────

@Composable
fun FeedbackSectionHeader(count: Int, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title (unchanged)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(3.dp).height(20.dp).clip(RoundedCornerShape(50)).background(GoldGradient))
            Spacer(Modifier.width(10.dp))
            Text("Community Feedback", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Reviews count badge (unchanged)
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(DarkCard)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text("$count reviews", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = GoldAccent)
            }

            // ── View All button ───────────────────────────────────
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(0.10f))
                    .border(1.dp, GoldPrimary.copy(0.25f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onViewAll)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.FormatListBulleted, "View all", tint = GoldPrimary, modifier = Modifier.size(13.dp))
                    Text("View All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  FEEDBACK CARD
//  ➕ Tapping the card body → feedback_details/{feedback.id}
//  ➕ "Reply" chip → feedback_details/{feedback.id}  (opens thread)
// ─────────────────────────────────────────────────────────────────

@Composable
fun FeedbackCard(
    item: FeedbackItem,
    isHelpful: Boolean,
    onHelpful: () -> Unit,
    onViewDetail: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp)).background(CardBackground)
            .border(1.dp, Color(0xFFECE4D8), RoundedCornerShape(24.dp))
            // ── Whole card is tappable → FeedbackDetails ─────────
            .clickable(onClick = onViewDetail)
    ) {
        // Left accent strip (unchanged)
        Box(
            modifier = Modifier.width(4.dp).fillMaxHeight().align(Alignment.CenterStart)
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(Brush.verticalGradient(listOf(item.avatarColor, GoldDeep)))
        )

        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 16.dp)
        ) {
            // Reviewer row (unchanged)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape)
                            .background(item.avatarColor.copy(alpha = 0.2f))
                            .border(2.dp, item.avatarColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.reviewer.first().toString(), fontSize = 18.sp,
                            fontWeight = FontWeight.Black, color = item.avatarColor)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(item.reviewer, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(item.role, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(item.timeAgo, fontSize = 11.sp, color = TextSecondary.copy(alpha = 0.6f))
                    // ── Chevron hint — card is tappable ──────────
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "View feedback detail",
                        tint = Color(0xFFD8D0C4), modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            // Stars (unchanged)
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(
                        imageVector = if (i < item.rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (i < item.rating) GoldAccent else Color(0xFFD8D0C4),
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text("${item.rating}.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextGold)
            }

            Spacer(Modifier.height(10.dp))

            // Comment (unchanged)
            Text(
                text = item.comment, fontSize = 14.sp, lineHeight = 22.sp, color = TextSecondary,
                maxLines = if (expanded) Int.MAX_VALUE else 2, overflow = TextOverflow.Ellipsis
            )

            if (item.comment.length > 80) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (expanded) "Show less" else "Read more",
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextGold,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() }, indication = null
                    ) { expanded = !expanded }
                )
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = Color(0xFFF0EAE0), thickness = 1.dp)
            Spacer(Modifier.height(12.dp))

            // Footer row (unchanged layout)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Helpful toggle (unchanged)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHelpful
                        )
                        .background(if (isHelpful) GoldPrimary.copy(alpha = 0.1f) else BackgroundSecondary)
                        .border(1.dp, if (isHelpful) GoldPrimary.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = if (isHelpful) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Helpful",
                        tint = if (isHelpful) GoldPrimary else TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Helpful · ${if (isHelpful) item.helpful + 1 else item.helpful}",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = if (isHelpful) GoldPrimary else TextSecondary
                    )
                }

                // ── Reply chip → opens FeedbackDetails thread ────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(DarkCard)
                        .clickable(onClick = onViewDetail)
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Icon(Icons.Default.Reply, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Reply", fontSize = 12.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  INVITE BANNER  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun InviteBanner() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(24.dp)).background(DarkGradient)
            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp)).padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Want more eyes on your work?", fontSize = 18.sp, fontWeight = FontWeight.Black,
                color = TextOnDark, textAlign = TextAlign.Center, letterSpacing = (-0.3).sp)
            Spacer(Modifier.height(8.dp))
            Text("Share your submission link and invite peers to leave feedback.",
                fontSize = 13.sp, lineHeight = 20.sp, color = TextOnDark.copy(alpha = 0.5f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(GoldGradient)
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, null, tint = TextOnDark, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share Submission", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextOnDark)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SECTION LABEL  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun DashSectionLabel(text: String) {
    Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(50)).background(GoldGradient))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────────
//  BOTTOM NAV  (unchanged)
// ─────────────────────────────────────────────────────────────────

@Composable
fun FeedbackBottomNav(navController: NavController, modifier: Modifier = Modifier) {
    Surface(color = CardBackground, shadowElevation = 20.dp, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(68.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedNavItem(Icons.Default.Home,        "Home",     false, Modifier.weight(1f)) {
                navController.navigate(ROUT_Dashboard) { popUpTo("dashboard") { inclusive = true } }
            }
            FeedNavItem(Icons.Default.Add,         "Submit",   false, Modifier.weight(1f)) {
                navController.navigate(ROUT_SubmitWork)
            }
            FeedNavItem(Icons.Default.ChatBubble,  "Feedback", true,  Modifier.weight(1f)) {}
            FeedNavItem(Icons.Default.Person,      "Profile",  false, Modifier.weight(1f)) {
                navController.navigate(ROUT_Profile)
            }
        }
    }
}

@Composable
fun FeedNavItem(
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
            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(GoldGradient),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = TextOnDark, modifier = Modifier.size(21.dp))
            }
        } else {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true, backgroundColor = 0xFFF5F2EC)
@Composable
fun FeedbackDashboardScreenPreview() {
    FeedbackDashboardScreen(rememberNavController())
}