package com.kennedy.forge.ui.screens.feedback

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import java.util.Locale

// ─────────────────────────────────────────────
// COLOR PALETTE
// ─────────────────────────────────────────────

private val BackgroundMain      = Color(0xFFF5F2EC)
private val BackgroundSecondary = Color(0xFFEDE7DD)
private val CardBackground      = Color(0xFFFFFFFF)
private val GoldPrimary         = Color(0xFFC89B3C)
private val GoldAccent          = Color(0xFFE6B85C)
private val GoldDeep            = Color(0xFFA67C2E)
private val SoftGreen           = Color(0xFF7FBF9F)
private val SoftBlue            = Color(0xFF7DAED3)
private val SoftPeach           = Color(0xFFE8A87C)
private val SoftOlive           = Color(0xFFB5A27A)
private val TextPrimary         = Color(0xFF1A1A1A)
private val TextSecondary       = Color(0xFF6F6F6F)
private val DarkSurface         = Color(0xFF121212)
private val ErrorRed            = Color(0xFFE53935)

// ─────────────────────────────────────────────
// MODEL
// Fix 1: Removed invalid "public final" modifiers — Kotlin data classes are
//        final by default; "public final" is a Java idiom and causes a compile error.
// ─────────────────────────────────────────────

data class ProjectDetail(
    val reviewer: String,
    val avatarColor: Color,
    val comment: String,
    val rating: Int,
    val timeAgo: String
)

// ─────────────────────────────────────────────
// DEMO DATA
// ─────────────────────────────────────────────

private val demoFeedback = listOf(
    ProjectDetail(
        reviewer    = "Alex Rivera",
        avatarColor = SoftBlue,
        comment     = "Love the overall direction. The spacing feels intentional and the hierarchy is really clear. Would push the typography contrast a bit more.",
        rating      = 5,
        timeAgo     = "2h ago"
    ),
    ProjectDetail(
        reviewer    = "Jordan Lee",
        avatarColor = SoftGreen,
        comment     = "Great concept! The hero section is stunning. Maybe experiment with a bolder CTA button to drive more action.",
        rating      = 4,
        timeAgo     = "5h ago"
    ),
    ProjectDetail(
        reviewer = "Priya Nair",
        avatarColor = SoftPeach,
        comment = "Really solid execution. The color palette feels premium. The only thing I'd revisit is the card border radius — slightly too round for the editorial feel.",
        rating = 4,
        timeAgo = "1d ago"
    )
)

// ─────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(navController: NavController) {

    var feedbackList by remember { mutableStateOf(demoFeedback.toMutableList()) }
    var newComment   by remember { mutableStateOf("") }
    var newRating    by remember { mutableStateOf(0) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var showToast    by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    val avgRating = if (feedbackList.isNotEmpty())
        feedbackList.map { it.rating }.average()
    else 0.0

    val ratingCounts = (5 downTo 1).map { star ->
        star to feedbackList.count { it.rating == star }
    }

    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(2200L)
            showToast = false
        }
    }

    fun submitFeedback() {
        if (newComment.isBlank() || newRating == 0) return
        // Fix 2: capture editingIndex in a local val to avoid smart-cast issues
        val idx = editingIndex
        if (idx == null) {
            feedbackList = (feedbackList + ProjectDetail(
                reviewer    = "You",
                avatarColor = GoldPrimary,
                comment     = newComment,
                rating      = newRating,
                timeAgo     = "Just now"
            )).toMutableList()
            toastMessage = "Review submitted!"
        } else {
            val updated = feedbackList.toMutableList()
            updated[idx] = updated[idx].copy(comment = newComment, rating = newRating)
            feedbackList = updated
            editingIndex = null
            toastMessage = "Review updated!"
        }
        newComment = ""
        newRating  = 0
        showToast  = true
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Project Details",
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            LazyColumn(
                modifier       = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {

                item { HeroSection() }

                item { MetaRow() }

                item {
                    RatingSummaryCard(
                        avgRating    = avgRating,
                        ratingCounts = ratingCounts,
                        totalCount   = feedbackList.size
                    )
                }

                item {
                    FeedbackFormCard(
                        isEditing       = editingIndex != null,
                        comment         = newComment,
                        rating          = newRating,
                        onCommentChange = { newComment = it },
                        onRatingChange  = { newRating = it },
                        onCancel        = { editingIndex = null; newComment = ""; newRating = 0 },
                        onSubmit        = ::submitFeedback
                    )
                }

                item {
                    Row(
                        modifier              = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Reviews",
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "${feedbackList.size}",
                                color      = GoldDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 13.sp
                            )
                        }
                    }
                }

                if (feedbackList.isEmpty()) {
                    item { EmptyFeedbackState() }
                } else {
                    itemsIndexed(feedbackList) { index, item ->
                        FeedbackCard(
                            item     = item,
                            onEdit   = {
                                newComment   = item.comment
                                newRating    = item.rating
                                editingIndex = index
                            },
                            onDelete = {
                                feedbackList = feedbackList.toMutableList()
                                    .also { list -> list.removeAt(index) }
                                toastMessage = "Review removed"
                                showToast    = true
                            }
                        )
                    }
                }
            }

            // Toast overlay
            AnimatedVisibility(
                visible  = showToast,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 20.dp, vertical = 13.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(20.dp))
                    Text(toastMessage, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// HERO SECTION
// ─────────────────────────────────────────────

@Composable
fun HeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter            = painterResource(id = R.drawable.hero_bg),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        startY = 100f
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(GoldPrimary)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text("UI/UX Design", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .clickable {},
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FavoriteBorder, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                "Forge Design System v2",
                color      = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 20.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "A comprehensive design system for modern creative platforms",
                color    = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────
// META ROW
// ─────────────────────────────────────────────

@Composable
fun MetaRow() {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetaChip(icon = Icons.Default.Visibility, label = "2.4k views", tint = SoftBlue)
        MetaChip(icon = Icons.Default.Favorite,   label = "142 likes",  tint = SoftPeach)
        MetaChip(icon = Icons.Default.AccessTime, label = "3 days ago", tint = SoftOlive)
    }
}

@Composable
fun MetaChip(icon: ImageVector, label: String, tint: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(14.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────
// RATING SUMMARY CARD
// Fix 3: HorizontalDivider cannot be used as a vertical divider.
//        Replaced with a plain Box(width=1.dp, height=80.dp).
// Fix 4: String.format needs an explicit Locale to avoid lint warning.
// ─────────────────────────────────────────────

@Composable
fun RatingSummaryCard(
    avgRating: Double,
    ratingCounts: List<Pair<Int, Int>>,
    totalCount: Int
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier          = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large average score
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.width(80.dp)
            ) {
                Text(
                    text       = String.format(Locale.US, "%.1f", avgRating),
                    color      = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 38.sp
                )
                StarRow(rating = avgRating.toInt(), size = 14)
                Spacer(Modifier.height(4.dp))
                Text("$totalCount reviews", color = TextSecondary, fontSize = 11.sp)
            }

            Spacer(Modifier.width(20.dp))

            // Vertical divider — Box instead of HorizontalDivider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(80.dp)
                    .background(BackgroundSecondary)
            )

            Spacer(Modifier.width(20.dp))

            // Per-star bar breakdown
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ratingCounts.forEach { (star, count) ->
                    val fraction = if (totalCount > 0) count.toFloat() / totalCount else 0f
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "$star",
                            color    = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.width(10.dp)
                        )
                        Icon(Icons.Default.Star, null, tint = GoldAccent, modifier = Modifier.size(11.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BackgroundSecondary)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(GoldAccent, GoldPrimary))
                                    )
                            )
                        }
                        Text(
                            "$count",
                            color    = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.width(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// FEEDBACK FORM CARD
// ─────────────────────────────────────────────

@Composable
fun FeedbackFormCard(
    isEditing: Boolean,
    comment: String,
    rating: Int,
    onCommentChange: (String) -> Unit,
    onRatingChange: (Int) -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditing) "Edit Review" else "Leave a Review",
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
                if (isEditing) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Your Rating", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 1..5) {
                    Icon(
                        imageVector        = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$i stars",
                        tint               = if (i <= rating) GoldAccent else BackgroundSecondary,
                        modifier           = Modifier
                            .size(34.dp)
                            .clickable { onRatingChange(i) }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text("Your Comment", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundSecondary.copy(alpha = 0.5f))
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value         = comment,
                    onValueChange = { if (it.length <= 300) onCommentChange(it) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 72.dp),
                    textStyle     = TextStyle(
                        color      = TextPrimary,
                        fontSize   = 14.sp,
                        lineHeight = 21.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (comment.isEmpty()) {
                            Text(
                                "Share your thoughts on this project…",
                                color    = TextSecondary.copy(alpha = 0.55f),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick  = onSubmit,
                enabled  = comment.isNotBlank() && rating > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = GoldPrimary,
                    disabledContainerColor = BackgroundSecondary
                )
            ) {
                Icon(
                    imageVector        = if (isEditing) Icons.Default.Check else Icons.Default.Send,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditing) "Update Review" else "Submit Review",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 15.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// FEEDBACK CARD
// Fix 5: Char.uppercase() returns String, not Char — use toString().uppercase(Locale.US)
// Fix 6: Renamed expanded → menuExpanded to avoid shadowing conflicts
// ─────────────────────────────────────────────

@Composable
fun FeedbackCard(
    item: ProjectDetail,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    // Safe: firstOrNull returns Char?, toString() gives "?" fallback, uppercase needs Locale
    val initial = item.reviewer.firstOrNull()?.toString()?.uppercase(Locale.US) ?: "?"

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(item.avatarColor.copy(alpha = 0.2f))
                            .border(1.5.dp, item.avatarColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = initial,
                            color      = item.avatarColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 17.sp
                        )
                    }

                    Column {
                        Text(
                            item.reviewer,
                            color      = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp
                        )
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            StarRow(rating = item.rating, size = 12)
                            Text("·", color = TextSecondary, fontSize = 11.sp)
                            Text(item.timeAgo, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                // Only show actions for the current user's own review
                if (item.reviewer == "You") {
                    Box {
                        IconButton(
                            onClick  = { menuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint               = TextSecondary,
                                modifier           = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor   = CardBackground
                        ) {
                            DropdownMenuItem(
                                text        = { Text("Edit", color = TextPrimary, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint               = GoldPrimary,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                },
                                onClick = { menuExpanded = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text        = { Text("Delete", color = ErrorRed, fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint               = ErrorRed,
                                        modifier           = Modifier.size(16.dp)
                                    )
                                },
                                onClick = { menuExpanded = false; onDelete() }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                item.comment,
                color      = TextSecondary,
                fontSize   = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

// ─────────────────────────────────────────────
// EMPTY STATE
// ─────────────────────────────────────────────

@Composable
fun EmptyFeedbackState() {
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ChatBubbleOutline, null, tint = GoldPrimary, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text("No reviews yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Be the first to share your thoughts on this project.",
            color    = TextSecondary,
            fontSize = 13.sp
        )
    }
}

// ─────────────────────────────────────────────
// STAR ROW HELPER
// ─────────────────────────────────────────────

@Composable
fun StarRow(rating: Int, size: Int) {
    Row {
        for (i in 1..5) {
            Icon(
                imageVector        = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint               = if (i <= rating) GoldAccent else BackgroundSecondary,
                modifier           = Modifier.size(size.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
fun PreviewScreen() {
    ProjectDetailScreen(rememberNavController())
}