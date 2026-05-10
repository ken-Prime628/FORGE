package com.kennedy.forge.ui.screens.feedback

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_Profile
import com.kennedy.forge.navigation.ROUT_SubmitWork
import com.kennedy.forge.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────
//  SHARED WORK STATE
//  SubmitWorkScreen sets WorkDetailState.selectedWork = work
//  before calling navController.navigate(ROUT_ProjectDetail)
//  so this screen can read it without navigation args.
// ─────────────────────────────────────────────────────────────────

object WorkDetailState {
    var selectedWork: Work? = null
}

// ─────────────────────────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────────────────────────

enum class FeedbackCategory(val label: String, val emoji: String) {
    CLARITY("Clarity",         "🎯"),
    ORIGINALITY("Originality", "✨"),
    EXECUTION("Execution",     "⚙️"),
    IMPACT("Impact",           "🔥"),
    PACING("Pacing",           "⏱️"),
}

enum class ReviewerTier(val label: String, val color: Color) {
    VERIFIED("Verified Pro", Color(0xFF0F6E56)),
    PRO("Pro",               Color(0xFF534AB7)),
    PEER("Peer",             Color(0xFF854F0B)),
}

data class FeedbackReview(
    val id: String,
    val reviewer: String,
    val tier: ReviewerTier,
    val avatarColor: Color,
    val overallScore: Int,           // 0–100
    val categoryScores: Map<FeedbackCategory, Int>,
    val whatWorked: String,
    val whatToImprove: String,
    val oneAction: String,
    val timeAgo: String,
    val isActioned: Boolean = false,
    val helpfulVotes: Int   = 0,
)

// ─────────────────────────────────────────────────────────────────
//  HELPERS
// ─────────────────────────────────────────────────────────────────

private fun formatDate(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    return when {
        diff < 60_000L         -> "Just now"
        diff < 3_600_000L      -> "${diff / 60_000}m ago"
        diff < 86_400_000L     -> "${diff / 3_600_000}h ago"
        diff < 7 * 86_400_000L -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(millis))
    }
}

private fun statusColor(status: String): Color = when (status) {
    "reviewed" -> SoftGreen
    "closed"   -> SoftBlue
    else       -> SoftPeach  // pending
}

// ─────────────────────────────────────────────────────────────────
//  SEEDED REVIEWS — shown for every project so the screen is never empty
// ─────────────────────────────────────────────────────────────────

private val seededReviews = listOf(
    FeedbackReview(
        id = "1", reviewer = "Amara Jide", tier = ReviewerTier.VERIFIED,
        avatarColor = Color(0xFF534AB7), overallScore = 87,
        categoryScores = mapOf(
            FeedbackCategory.CLARITY     to 90, FeedbackCategory.ORIGINALITY to 85,
            FeedbackCategory.EXECUTION   to 88, FeedbackCategory.IMPACT       to 82,
            FeedbackCategory.PACING      to 90
        ),
        whatWorked    = "The visual hierarchy is immediately clear — your headline does real work and the spacing feels intentional. The colour choices are confident.",
        whatToImprove = "The middle section loses momentum. One idea doesn't connect back to the opening premise.",
        oneAction     = "Cut paragraph 2 entirely or move it after the conclusion.",
        timeAgo = "2 hours ago", helpfulVotes = 12
    ),
    FeedbackReview(
        id = "2", reviewer = "Marcus Teller", tier = ReviewerTier.PRO,
        avatarColor = Color(0xFF185FA5), overallScore = 74,
        categoryScores = mapOf(
            FeedbackCategory.CLARITY     to 78, FeedbackCategory.ORIGINALITY to 82,
            FeedbackCategory.EXECUTION   to 65, FeedbackCategory.IMPACT       to 70,
            FeedbackCategory.PACING      to 75
        ),
        whatWorked    = "Strong conceptual foundation. The central idea is genuinely original.",
        whatToImprove = "Execution needs work. Several transitions feel abrupt.",
        oneAction     = "Add one transitional sentence between sections 3 and 4.",
        timeAgo = "5 hours ago", isActioned = true, helpfulVotes = 8
    ),
    FeedbackReview(
        id = "3", reviewer = "Leo Kwame", tier = ReviewerTier.PEER,
        avatarColor = Color(0xFF0F6E56), overallScore = 91,
        categoryScores = mapOf(
            FeedbackCategory.CLARITY     to 93, FeedbackCategory.ORIGINALITY to 88,
            FeedbackCategory.EXECUTION   to 95, FeedbackCategory.IMPACT       to 90,
            FeedbackCategory.PACING      to 89
        ),
        whatWorked    = "This is genuinely excellent. The execution is clean, the pacing is exactly right.",
        whatToImprove = "The opening could be bolder. You bury your strongest idea in sentence three.",
        oneAction     = "Move sentence 3 to position 1. Lead with the stakes.",
        timeAgo = "1 day ago", helpfulVotes = 21
    )
)

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(navController: NavController) {

    // Read the work that was set by SubmitWorkScreen before navigating here
    val work = WorkDetailState.selectedWork

    val reviews       = remember { mutableStateListOf(*seededReviews.toTypedArray()) }
    var showAddFeedback by remember { mutableStateOf(false) }
    val avgScore        = if (reviews.isNotEmpty()) reviews.map { it.overallScore }.average() else 0.0
    val actionedCount   = reviews.count { it.isActioned }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text  = work?.title?.let { if (it.length > 22) "${it.take(22)}…" else it } ?: "Project Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary, fontWeight = FontWeight.W600
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Box(
                            Modifier.size(36.dp).clip(CircleShape).background(BackgroundSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                actions = {
                    // Live status chip from the real Work object
                    if (work != null) {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(statusColor(work.status).copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                work.status.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = statusColor(work.status), fontWeight = FontWeight.W700, fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        },
        bottomBar = { DetailBottomNavBar(navController) }
    ) { padding ->

        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Hero — uses actual Cloudinary image ───────────────
            item { ProjectHeroBlock(work = work) }

            // ── Description + meta chips from the real Work ───────
            if (work != null) {
                item { WorkMetaCard(work = work) }
            }

            // ── Score summary strip ───────────────────────────────
            item {
                ScoreSummaryStrip(avgScore = avgScore, reviewCount = reviews.size, actionedCount = actionedCount)
            }

            // ── Category breakdown bars ───────────────────────────
            item { CategoryBreakdownCard(reviews) }

            // ── Reviews header ────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reviewer feedback", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600, color = TextPrimary))
                        Text("${reviews.size} structured reviews", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    }
                    Box(
                        modifier = Modifier.height(36.dp).clip(RoundedCornerShape(18.dp))
                            .background(GoldGradient).clickable { showAddFeedback = !showAddFeedback }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(14.dp))
                            Text("Add review", style = MaterialTheme.typography.labelMedium.copy(color = DarkSurface, fontWeight = FontWeight.W600))
                        }
                    }
                }
            }

            // ── Add feedback panel ────────────────────────────────
            item {
                AnimatedVisibility(visible = showAddFeedback, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    AddFeedbackPanel(
                        onSubmit  = { newReview -> reviews.add(0, newReview); showAddFeedback = false },
                        onDismiss = { showAddFeedback = false }
                    )
                }
            }

            // ── Review cards ──────────────────────────────────────
            items(reviews, key = { it.id }) { review ->
                var actioned by remember { mutableStateOf(review.isActioned) }
                StructuredReviewCard(
                    review         = review.copy(isActioned = actioned),
                    onMarkActioned = { actioned = !actioned }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO BLOCK — real Work image + real title
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ProjectHeroBlock(work: Work?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(220.dp)
            .clip(RoundedCornerShape(28.dp))
    ) {
        // Use Cloudinary URL when available, canvas fallback otherwise
        if (work != null && work.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = work.imageUrl, contentDescription = work.title,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(DarkSurface)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRadialGlow(Color(0xFFC89B3C).copy(alpha = 0.25f), Offset(size.width * 0.8f, size.height * 0.2f), size.width * 0.6f)
                    drawRadialGlow(Color(0xFF534AB7).copy(alpha = 0.14f), Offset(size.width * 0.1f, size.height * 0.9f), size.width * 0.5f)
                }
            }
        }

        // Dark scrim so text always reads
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)))))

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            // Category pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GoldPrimary.copy(alpha = 0.2f),
                border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    work?.category?.takeIf { it.isNotEmpty() } ?: "Creative Work",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent, letterSpacing = 0.4.sp)
                )
            }
            Spacer(Modifier.height(8.dp))
            // Real title
            Text(
                work?.title?.ifBlank { "Untitled Work" } ?: "Project Preview",
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.W700)
            )
            // Real submission date
            Text(
                if (work != null) "Submitted ${formatDate(work.createdAt)}"
                else              "Tap a submission to view its details",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.65f))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  WORK META CARD — description + chips, all from real Work
// ─────────────────────────────────────────────────────────────────

@Composable
private fun WorkMetaCard(work: Work) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(0.5.dp, BackgroundSecondary)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (work.description.isNotBlank()) {
                Text(
                    "About this work",
                    style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.W600, letterSpacing = 0.5.sp)
                )
                Spacer(Modifier.height(6.dp))
                Text(work.description, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 22.sp))
                Spacer(Modifier.height(14.dp))
            }
            // Meta chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (work.category.isNotEmpty()) MetaChip(Icons.Default.Category, work.category, GoldPrimary)
                MetaChip(Icons.Default.AccessTime, formatDate(work.createdAt), SoftBlue)
                if (work.fileName.isNotEmpty()) MetaChip(Icons.Default.AttachFile, "File attached", SoftGreen)
            }
        }
    }
}

@Composable
private fun MetaChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(tint.copy(alpha = 0.10f)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = tint, fontWeight = FontWeight.W500))
    }
}

// ─────────────────────────────────────────────────────────────────
//  SCORE SUMMARY STRIP
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ScoreSummaryStrip(avgScore: Double, reviewCount: Int, actionedCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)).background(DarkSurface).padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ScoreStat(String.format(Locale.US, "%.0f", avgScore), "Avg score", GoldAccent)
        VerticalDivider(modifier = Modifier.height(40.dp), color = Color.White.copy(alpha = 0.1f))
        ScoreStat("$reviewCount", "Reviews",  Color.White)
        VerticalDivider(modifier = Modifier.height(40.dp), color = Color.White.copy(alpha = 0.1f))
        ScoreStat("$actionedCount", "Actioned", SoftGreen)
    }
}

@Composable
private fun ScoreStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.W600, color = color))
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.5f), letterSpacing = 0.4.sp))
    }
}

// ─────────────────────────────────────────────────────────────────
//  CATEGORY BREAKDOWN
// ─────────────────────────────────────────────────────────────────

@Composable
private fun CategoryBreakdownCard(reviews: List<FeedbackReview>) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(0.5.dp, BackgroundSecondary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Score breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600, color = TextPrimary))
            Spacer(Modifier.height(16.dp))
            FeedbackCategory.entries.forEach { cat ->
                val avg = if (reviews.isNotEmpty()) reviews.mapNotNull { it.categoryScores[cat] }.average() else 0.0
                CategoryBar(cat.emoji, cat.label, avg.toInt(), (avg / 100f).toFloat())
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryBar(emoji: String, label: String, score: Int, percent: Float) {
    val animatedPercent by animateFloatAsState(targetValue = percent, animationSpec = tween(800, easing = EaseOut), label = "cat_bar")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 16.sp, modifier = Modifier.width(26.dp))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary), modifier = Modifier.width(80.dp))
        Box(modifier = Modifier.weight(1f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(BackgroundSecondary)) {
            Box(
                modifier = Modifier.fillMaxWidth(animatedPercent).fillMaxHeight().clip(RoundedCornerShape(4.dp))
                    .background(when { score >= 85 -> Brush.linearGradient(listOf(GoldAccent, GoldPrimary)); score >= 70 -> Brush.linearGradient(listOf(SoftBlue, Color(0xFF185FA5))); else -> Brush.linearGradient(listOf(SoftPeach, Color(0xFFE8A87C))) })
            )
        }
        Spacer(Modifier.width(10.dp))
        Text("$score", style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary, fontWeight = FontWeight.W600), modifier = Modifier.width(26.dp), textAlign = TextAlign.End)
    }
}

// ─────────────────────────────────────────────────────────────────
//  STRUCTURED REVIEW CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StructuredReviewCard(review: FeedbackReview, onMarkActioned: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(if (expanded) 8.dp else 2.dp, RoundedCornerShape(24.dp), ambientColor = GoldPrimary.copy(0.06f)),
        shape  = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (review.isActioned) BackgroundSecondary else CardBackground),
        border = if (review.isActioned) BorderStroke(0.5.dp, SoftGreen.copy(0.4f)) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Reviewer header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(46.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(review.avatarColor, review.avatarColor.copy(0.7f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(review.reviewer.first().toString(), style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.W600))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(review.reviewer, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.W600))
                        Surface(shape = RoundedCornerShape(6.dp), color = review.tier.color.copy(0.12f)) {
                            Text(review.tier.label, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = review.tier.color, fontWeight = FontWeight.W500, fontSize = 10.sp))
                        }
                    }
                    Text(review.timeAgo, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                }
                // Score circle
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(
                        when { review.overallScore >= 85 -> GoldPrimary.copy(0.12f); review.overallScore >= 70 -> Color(0xFF185FA5).copy(0.10f); else -> Color(0xFF993C1D).copy(0.10f) }
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("${review.overallScore}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W700,
                        color = when { review.overallScore >= 85 -> GoldPrimary; review.overallScore >= 70 -> Color(0xFF185FA5); else -> Color(0xFF993C1D) }))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Star rating row (score 0-100 → 1-5 stars)
            val stars = ((review.overallScore / 100f) * 5).coerceIn(1f, 5f).toInt()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..5) {
                    Icon(
                        if (i <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                        null, tint = if (i <= stars) GoldAccent else BackgroundSecondary, modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text("${review.overallScore}/100", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            }

            Spacer(Modifier.height(12.dp))

            // Category score chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(review.categoryScores.entries.toList()) { (cat, score) ->
                    Surface(shape = RoundedCornerShape(20.dp), color = BackgroundSecondary, border = BorderStroke(0.5.dp, BackgroundSecondary)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(cat.emoji, fontSize = 12.sp)
                            Text("$score", style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary, fontWeight = FontWeight.W600))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Expandable body
            AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeedbackBlock(SoftGreen,  Icons.Default.CheckCircle, "What worked",     review.whatWorked)
                    FeedbackBlock(SoftPeach,  Icons.Default.Edit,         "What to improve", review.whatToImprove)
                    FeedbackBlock(GoldAccent, Icons.Default.ArrowForward, "One action",      review.oneAction)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.height(34.dp).clip(RoundedCornerShape(17.dp))
                                .background(if (review.isActioned) Brush.linearGradient(listOf(SoftGreen.copy(0.2f), SoftGreen.copy(0.1f))) else Brush.linearGradient(listOf(BackgroundSecondary, BackgroundSecondary)))
                                .border(0.5.dp, if (review.isActioned) SoftGreen.copy(0.6f) else BackgroundSecondary, RoundedCornerShape(17.dp))
                                .clickable(onClick = onMarkActioned).padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(if (review.isActioned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null,
                                    tint = if (review.isActioned) SoftGreen else TextSecondary, modifier = Modifier.size(14.dp))
                                Text(if (review.isActioned) "Actioned" else "Mark actioned",
                                    style = MaterialTheme.typography.labelSmall.copy(color = if (review.isActioned) SoftGreen else TextSecondary, fontWeight = FontWeight.W500))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ThumbUp, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Text("${review.helpfulVotes} found this helpful", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Expand / collapse toggle
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(BackgroundSecondary.copy(0.6f)).clickable { expanded = !expanded }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (expanded) "Collapse review" else "Read full review",
                        style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary, fontWeight = FontWeight.W500))
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  FEEDBACK BLOCK
// ─────────────────────────────────────────────────────────────────

@Composable
private fun FeedbackBlock(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color.copy(0.07f)).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(color))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Text(label, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.W600, letterSpacing = 0.3.sp))
            }
            Text(text, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, lineHeight = 18.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ADD FEEDBACK PANEL
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AddFeedbackPanel(onSubmit: (FeedbackReview) -> Unit, onDismiss: () -> Unit) {
    var whatWorked    by remember { mutableStateOf("") }
    var whatToImprove by remember { mutableStateOf("") }
    var oneAction     by remember { mutableStateOf("") }
    var starPick      by remember { mutableIntStateOf(3) }   // 1-5 stars
    val overallScore  = (starPick * 20).coerceIn(10, 100)    // maps to 0-100

    val canSubmit = whatWorked.isNotBlank() && whatToImprove.isNotBlank() && oneAction.isNotBlank()

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, GoldPrimary.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Write your review", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600, color = TextPrimary))
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Be specific so the creator can act on it.", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            Spacer(Modifier.height(16.dp))

            // Star rating picker — matches SubmitWorkScreen star UI
            Text("Your rating", style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary, fontWeight = FontWeight.W500))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..5) {
                    Icon(
                        if (i <= starPick) Icons.Default.Star else Icons.Default.StarBorder,
                        "$i stars",
                        tint     = if (i <= starPick) GoldAccent else BackgroundSecondary,
                        modifier = Modifier.size(32.dp).clickable { starPick = i }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Score: $overallScore / 100", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Spacer(Modifier.height(16.dp))

            AddFeedbackField("✅  What worked well?",       whatWorked,    "Be specific — what landed?")           { whatWorked = it }
            Spacer(Modifier.height(10.dp))
            AddFeedbackField("⚠️  What needs improvement?", whatToImprove, "Focus on craft, not preference.")       { whatToImprove = it }
            Spacer(Modifier.height(10.dp))
            AddFeedbackField("🎯  One specific action",     oneAction,     "The single most important thing to change?") { oneAction = it }
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (canSubmit) GoldGradient else Brush.linearGradient(listOf(Color(0xFFD8D0C4), Color(0xFFCCC5B8))))
                    .clickable(enabled = canSubmit) {
                        onSubmit(FeedbackReview(
                            id = System.currentTimeMillis().toString(), reviewer = "You",
                            tier = ReviewerTier.PEER, avatarColor = Color(0xFF993556),
                            overallScore = overallScore,
                            categoryScores = FeedbackCategory.entries.associateWith { overallScore },
                            whatWorked = whatWorked, whatToImprove = whatToImprove, oneAction = oneAction, timeAgo = "Just now"
                        ))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("Submit review", style = MaterialTheme.typography.titleSmall.copy(
                    color = if (canSubmit) DarkSurface else Color(0xFF9A9A9A), fontWeight = FontWeight.W600))
            }
        }
    }
}

@Composable
private fun AddFeedbackField(label: String, value: String, hint: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.W500))
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(hint, style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFBBBBBB))) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), minLines = 2,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary, unfocusedBorderColor = BackgroundSecondary,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = GoldPrimary
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  BOTTOM NAV BAR
// ─────────────────────────────────────────────────────────────────

@Composable
private fun DetailBottomNavBar(navController: NavController) {
    NavigationBar(containerColor = CardBackground, tonalElevation = 0.dp, modifier = Modifier.shadow(8.dp)) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick = { navController.navigate(ROUT_Dashboard) { popUpTo(ROUT_Dashboard) { inclusive = false }; launchSingleTop = true } }
        )
        NavigationBarItem(
            icon = {
                Box(Modifier.size(40.dp).clip(CircleShape).background(GoldGradient), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(22.dp))
                }
            },
            label = { Text("Submit", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick = { navController.navigate(ROUT_SubmitWork) { launchSingleTop = true } }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick = { navController.navigate(ROUT_Profile) { launchSingleTop = true } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPER
// ─────────────────────────────────────────────────────────────────

private fun DrawScope.drawRadialGlow(color: Color, center: Offset, radius: Float) {
    drawCircle(brush = Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius))
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun ProjectDetailScreenPreview() {
    WorkDetailState.selectedWork = Work(
        id = "preview", uid = "uid",
        title = "Forge Design System v2",
        description = "A comprehensive design system for modern creative platforms. Built with care and precision.",
        category = "Design", imageUrl = "", status = "pending",
        createdAt = System.currentTimeMillis() - 3_600_000
    )
    ProjectDetailScreen(rememberNavController())
}