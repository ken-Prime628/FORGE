package com.kennedy.forge.ui.screens.pitch

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
private val TextOnDark          = Color(0xFFFFFFFF)
private val DarkSurface         = Color(0xFF121212)
private val DarkCard            = Color(0xFF1C1C1C)
private val ErrorRed            = Color(0xFFE53935)

// ─────────────────────────────────────────────
// MODELS
// ─────────────────────────────────────────────

data class Pitch(
    val title: String,
    val tagline: String,
    val problem: String,
    val solution: String,
    val audience: String,
    val monetization: String,
    val stage: String,
    val creator: String,
    val creatorRole: String,
    val avatarColor: Color,
    val likes: Int,
    val views: Int,
    val category: String
)

data class PitchComment(
    val user: String,
    val text: String,
    val avatarColor: Color,
    val timeAgo: String,
    val likes: Int
)

data class LiveViewer(
    val name: String,
    val avatarColor: Color
)

data class InsightMetric(
    val label: String,
    val value: String,
    val change: String,
    val positive: Boolean,
    val icon: ImageVector
)

// ─────────────────────────────────────────────
// DEMO DATA
// ─────────────────────────────────────────────

private val demoPitch = Pitch(
    title        = "Smart Study Platform",
    tagline      = "Turn chaos into clarity — the AI-powered way to study.",
    problem      = "Students struggle to stay focused, track their progress, and organize their study materials effectively. Distraction is the #1 reason academic performance suffers.",
    solution     = "A platform that combines AI-powered focus tracking, smart task management, and adaptive learning paths — so every study session counts.",
    audience     = "University students, self-learners, and professionals upskilling in a fast-paced world.",
    monetization = "Freemium subscription — free tier with core features, Pro at \$9.99/mo for unlimited sessions, analytics, and integrations.",
    stage        = "MVP",
    creator      = "Alex Rivera",
    creatorRole  = "Founder & Product Lead",
    avatarColor  = SoftBlue,
    likes        = 142,
    views        = 2400,
    category     = "EdTech"
)

private val demoComments = listOf(
    PitchComment("Jordan Lee",  "This concept is genuinely powerful. The focus tracking angle is underexplored — huge opportunity here 🔥", SoftGreen,  "2h ago", 18),
    PitchComment("Priya Nair",  "I'd love to see how the adaptive learning paths work. Would this integrate with platforms like Notion or Obsidian?", SoftPeach,  "4h ago", 9),
    PitchComment("Marcus Webb", "The freemium model makes sense. Just make sure the free tier is generous enough to hook users before upselling.", GoldAccent, "6h ago", 24),
    PitchComment("Sofia Chang", "As a student, this is exactly what I need. The problem is 100% real. Please build this.", SoftOlive,  "1d ago", 41)
)

private val demoLiveViewers = listOf(
    LiveViewer("Jordan", SoftGreen),
    LiveViewer("Priya",  SoftPeach),
    LiveViewer("Marcus", SoftBlue),
    LiveViewer("Sofia",  GoldAccent),
    LiveViewer("Tomas",  SoftOlive)
)

private val demoInsights = listOf(
    InsightMetric("Total Views",     "2,418",  "+34% this week",  true,  Icons.Default.Visibility),
    InsightMetric("Unique Visitors", "1,092",  "+18% this week",  true,  Icons.Default.People),
    InsightMetric("Avg. Time Spent", "3m 42s", "+12s from last",  true,  Icons.Default.AccessTime),
    InsightMetric("Likes",           "142",    "+27 this week",   true,  Icons.Default.Favorite),
    InsightMetric("Comments",        "28",     "+6 this week",    true,  Icons.Default.ChatBubble),
    InsightMetric("Collaboration Requests", "7", "+3 this week",  true,  Icons.Default.Handshake)
)

private val weeklyData = listOf(120, 240, 180, 380, 310, 460, 520)
private val weekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

// ─────────────────────────────────────────────
// TAB ENUM
// ─────────────────────────────────────────────

private enum class PitchTab(val label: String, val icon: ImageVector) {
    VIEW("Pitch",    Icons.Default.Description),
    LIVE("Live",     Icons.Default.Videocam),
    INSIGHT("Insight", Icons.Default.BarChart)
}

// ─────────────────────────────────────────────
// ROOT SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitchViewScreen(navController: NavController) {

    var activeTab    by remember { mutableStateOf(PitchTab.VIEW) }
    var pitch        by remember { mutableStateOf(demoPitch) }
    var liked        by remember { mutableStateOf(false) }
    var likeCount    by remember { mutableStateOf(demoPitch.likes) }
    var comments     by remember { mutableStateOf(demoComments.toMutableList()) }
    var newComment   by remember { mutableStateOf("") }
    var isLive       by remember { mutableStateOf(false) }
    var viewers      by remember { mutableStateOf(demoLiveViewers.toMutableList()) }
    var liveMessages by remember { mutableStateOf(
        mutableListOf(
            Triple("Jordan",  SoftGreen,  "Just joined 👋"),
            Triple("Priya",   SoftPeach,  "This is exciting, can't wait!"),
            Triple("Marcus",  SoftBlue,   "Let's gooo 🚀")
        )
    ) }
    var liveInput    by remember { mutableStateOf("") }
    var showToast    by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    LaunchedEffect(showToast) {
        if (showToast) {
            kotlinx.coroutines.delay(2200L)
            showToast = false
        }
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            pitch.title,
                            color      = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 15.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(
                            pitch.category,
                            color    = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        toastMessage = "Link copied!"
                        showToast    = true
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        },
        bottomBar = {
            // ── TAB BAR ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PitchTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Column(
                        modifier            = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) GoldPrimary.copy(alpha = 0.12f)
                                    else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Live tab shows blinking red dot when live
                            if (tab == PitchTab.LIVE && isLive) {
                                PulsingLiveDot()
                            }
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint               = if (selected) GoldPrimary else TextSecondary,
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            tab.label,
                            color      = if (selected) GoldPrimary else TextSecondary,
                            fontSize   = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180))
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    PitchTab.VIEW -> PitchViewTab(
                        pitch      = pitch,
                        liked      = liked,
                        likeCount  = likeCount,
                        comments   = comments,
                        newComment = newComment,
                        padding    = padding,
                        onLike     = {
                            liked     = !liked
                            likeCount = if (liked) likeCount + 1 else likeCount - 1
                        },
                        onCommentChange = { newComment = it },
                        onCommentSubmit = {
                            if (newComment.isNotBlank()) {
                                comments = (comments + PitchComment(
                                    user        = "You",
                                    text        = newComment,
                                    avatarColor = GoldPrimary,
                                    timeAgo     = "Just now",
                                    likes       = 0
                                )).toMutableList()
                                newComment   = ""
                                toastMessage = "Comment posted!"
                                showToast    = true
                            }
                        },
                        onCollaborate = {
                            toastMessage = "Collaboration request sent!"
                            showToast    = true
                        }
                    )

                    PitchTab.LIVE -> PitchLiveTab(
                        pitch        = pitch,
                        isLive       = isLive,
                        viewers      = viewers,
                        liveMessages = liveMessages,
                        liveInput    = liveInput,
                        padding      = padding,
                        onToggleLive = { isLive = !isLive },
                        onInputChange = { liveInput = it },
                        onSendMessage = {
                            if (liveInput.isNotBlank()) {
                                liveMessages = (liveMessages + Triple("You", GoldPrimary, liveInput)).toMutableList()
                                liveInput = ""
                            }
                        }
                    )

                    PitchTab.INSIGHT -> PitchInsightTab(
                        pitch   = pitch,
                        metrics = demoInsights,
                        padding = padding
                    )
                }
            }

            // ── TOAST ─────────────────────────────────────────
            AnimatedVisibility(
                visible  = showToast,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit  = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(18.dp))
                    Text(toastMessage, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 1 — PITCH VIEW
// ═══════════════════════════════════════════════════════

@Composable
fun PitchViewTab(
    pitch: Pitch,
    liked: Boolean,
    likeCount: Int,
    comments: List<PitchComment>,
    newComment: String,
    padding: PaddingValues,
    onLike: () -> Unit,
    onCommentChange: (String) -> Unit,
    onCommentSubmit: () -> Unit,
    onCollaborate: () -> Unit
) {
    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        // ── HERO ─────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GoldDeep, GoldPrimary, GoldAccent)
                        )
                    )
            ) {
                // Decorative circles
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = 140.dp, y = (-60).dp)
                        .background(
                            brush  = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.12f), Color.Transparent)),
                            shape  = CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .offset(x = (-30).dp, y = 100.dp)
                        .background(
                            brush  = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)),
                            shape  = CircleShape
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // Stage badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(pitch.stage, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        pitch.title,
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 26.sp,
                        lineHeight = 30.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        pitch.tagline,
                        color    = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── CREATOR ROW ─────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(pitch.avatarColor.copy(alpha = 0.2f))
                            .border(2.dp, pitch.avatarColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            pitch.creator.first().toString().uppercase(Locale.US),
                            color      = pitch.avatarColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 18.sp
                        )
                    }
                    Column {
                        Text(pitch.creator, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(pitch.creatorRole, color = TextSecondary, fontSize = 12.sp)
                    }
                }
                // View count
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text("${pitch.views / 1000.0}k", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        // ── PITCH SECTIONS ───────────────────────────────
        item { PitchSectionCard(label = "The Problem",      icon = Icons.Default.Warning,         tint = SoftPeach, content = pitch.problem) }
        item { PitchSectionCard(label = "Our Solution",     icon = Icons.Default.Lightbulb,       tint = GoldAccent, content = pitch.solution) }
        item { PitchSectionCard(label = "Target Audience",  icon = Icons.Default.People,          tint = SoftBlue, content = pitch.audience) }
        item { PitchSectionCard(label = "Monetization",     icon = Icons.Default.AttachMoney,     tint = SoftGreen, content = pitch.monetization) }

        // ── ACTIONS ──────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Like button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (liked) GoldPrimary.copy(alpha = 0.12f)
                            else CardBackground
                        )
                        .border(
                            1.dp,
                            if (liked) GoldPrimary.copy(alpha = 0.4f) else BackgroundSecondary,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onLike() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null,
                            tint     = if (liked) GoldPrimary else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "$likeCount",
                            color      = if (liked) GoldPrimary else TextSecondary,
                            fontWeight = if (liked) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = 14.sp
                        )
                    }
                }

                // Collaborate CTA
                Button(
                    onClick   = onCollaborate,
                    modifier  = Modifier.weight(2f).height(50.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Icon(Icons.Default.Handshake, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Join Project", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        // ── COMMENTS HEADER ─────────────────────────────
        item {
            Row(
                modifier              = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Discussion", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GoldPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("${comments.size}", color = GoldDeep, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // ── COMMENT LIST ─────────────────────────────────
        items(comments) { comment -> PitchCommentCard(comment) }

        // ── COMMENT INPUT ────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardBackground)
                        .border(1.dp, BackgroundSecondary, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value         = newComment,
                        onValueChange = { if (it.length <= 280) onCommentChange(it) },
                        modifier      = Modifier.fillMaxWidth(),
                        textStyle     = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        decorationBox = { inner ->
                            if (newComment.isEmpty()) Text("Share your thoughts…", color = TextSecondary.copy(alpha = 0.55f), fontSize = 14.sp)
                            inner()
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (newComment.isNotBlank()) GoldPrimary else BackgroundSecondary)
                        .clickable(enabled = newComment.isNotBlank()) { onCommentSubmit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 2 — PITCH LIVE
// ═══════════════════════════════════════════════════════

@Composable
fun PitchLiveTab(
    pitch: Pitch,
    isLive: Boolean,
    viewers: List<LiveViewer>,
    liveMessages: List<Triple<String, Color, String>>,
    liveInput: String,
    padding: PaddingValues,
    onToggleLive: () -> Unit,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(liveMessages.size) {
        if (liveMessages.isNotEmpty()) {
            listState.animateScrollToItem(liveMessages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {

        // ── LIVE STAGE ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkCard, DarkSurface)
                    )
                )
        ) {
            // Decorative glow
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .align(Alignment.Center)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(GoldPrimary.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )

            // Presenter avatar
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(pitch.avatarColor.copy(alpha = 0.2f))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(listOf(GoldAccent, GoldPrimary)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pitch.creator.first().toString().uppercase(Locale.US),
                        color      = pitch.avatarColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 28.sp
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(pitch.creator, color = TextOnDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(pitch.title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            // Live badge / Go Live button
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                if (isLive) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(ErrorRed)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingLiveDot(tint = Color.White)
                        Text("LIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("OFF AIR", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Viewers count bottom-left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.People, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                Text("${viewers.size} watching", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        // ── GO LIVE / END LIVE BUTTON ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Viewer avatars
            Row(modifier = Modifier.weight(1f)) {
                viewers.take(4).forEachIndexed { idx, v ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .offset(x = (-idx * 10).dp)
                            .clip(CircleShape)
                            .border(2.dp, CardBackground, CircleShape)
                            .background(v.avatarColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(v.name.first().toString(), color = v.avatarColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                if (viewers.size > 4) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .offset(x = (-40).dp)
                            .clip(CircleShape)
                            .background(BackgroundSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+${viewers.size - 4}", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick   = onToggleLive,
                shape     = RoundedCornerShape(12.dp),
                colors    = ButtonDefaults.buttonColors(
                    containerColor = if (isLive) ErrorRed else GoldPrimary
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    if (isLive) Icons.Default.Stop else Icons.Default.Videocam,
                    null, tint = Color.White, modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (isLive) "End Live" else "Go Live",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
            }
        }

        HorizontalDivider(color = BackgroundSecondary)

        // ── LIVE CHAT ────────────────────────────────────
        Text(
            "Live Chat",
            color      = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp,
            modifier   = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        LazyColumn(
            state          = listState,
            modifier       = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(liveMessages) { (user, color, msg) ->
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.first().toString().uppercase(Locale.US),
                            color      = color,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 11.sp
                        )
                    }
                    Column {
                        Text(user, color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(msg,  color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }

        // ── CHAT INPUT ───────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BackgroundSecondary)
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                BasicTextField(
                    value         = liveInput,
                    onValueChange = { if (it.length <= 200) onInputChange(it) },
                    modifier      = Modifier.fillMaxWidth(),
                    textStyle     = TextStyle(color = TextPrimary, fontSize = 14.sp),
                    decorationBox = { inner ->
                        if (liveInput.isEmpty()) Text("Say something…", color = TextSecondary.copy(alpha = 0.55f), fontSize = 14.sp)
                        inner()
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (liveInput.isNotBlank()) GoldPrimary else BackgroundSecondary)
                    .clickable(enabled = liveInput.isNotBlank()) { onSendMessage() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 3 — PITCH INSIGHT
// ═══════════════════════════════════════════════════════

@Composable
fun PitchInsightTab(
    pitch: Pitch,
    metrics: List<InsightMetric>,
    padding: PaddingValues
) {
    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        // ── HEADER ───────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(GoldPrimary.copy(alpha = 0.1f), BackgroundMain)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column {
                    Text("Pitch Analytics", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = """Performance overview for "${pitch.title}"""",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )                }
            }
        }

        // ── METRICS GRID (2 columns) ──────────────────────
        item {
            val chunked = metrics.chunked(2)
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chunked.forEach { row ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { metric ->
                            MetricCard(metric = metric, modifier = Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }



        // ── WEEKLY CHART ─────────────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
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
                        Text("Weekly Views", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("This week", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(16.dp))

                    // Bar chart
                    val maxVal = weeklyData.maxOrNull() ?: 1
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        weeklyData.forEachIndexed { idx, value ->
                            val fraction = value.toFloat() / maxVal.toFloat()
                            val isLast   = idx == weeklyData.lastIndex
                            Column(
                                modifier            = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                if (isLast) {
                                    Text(
                                        "$value",
                                        color      = GoldPrimary,
                                        fontSize   = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(fraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            if (isLast)
                                                Brush.verticalGradient(listOf(GoldAccent, GoldPrimary))
                                            else
                                                Brush.verticalGradient(listOf(GoldPrimary.copy(alpha = 0.35f), GoldPrimary.copy(alpha = 0.15f)))
                                        )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Day labels
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        weekLabels.forEach { label ->
                            Text(
                                label,
                                modifier   = Modifier.weight(1f),
                                textAlign  = TextAlign.Center,
                                color      = TextSecondary,
                                fontSize   = 10.sp
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }



        // ── AUDIENCE BREAKDOWN ───────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Audience Breakdown", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(14.dp))
                    AudienceBar("Students",        72, SoftBlue)
                    Spacer(Modifier.height(10.dp))
                    AudienceBar("Professionals",   18, GoldPrimary)
                    Spacer(Modifier.height(10.dp))
                    AudienceBar("Investors",       7,  SoftGreen)
                    Spacer(Modifier.height(10.dp))
                    AudienceBar("Other",           3,  SoftPeach)
                }
            }
            Spacer(Modifier.height(20.dp))
        }



        // ── TOP FEEDBACK SUMMARY ─────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Sentiment Overview", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SentimentDot("Positive", 82, SoftGreen)
                        SentimentDot("Neutral",  13, GoldAccent)
                        SentimentDot("Critical",  5, SoftPeach)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// SHARED COMPOSABLES
// ─────────────────────────────────────────────

@Composable
fun PitchSectionCard(label: String, icon: ImageVector, tint: Color, content: String) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                }
                Text(label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(content, color = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}

@Composable
fun PitchCommentCard(comment: PitchComment) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(comment.avatarColor.copy(alpha = 0.18f))
                        .border(1.dp, comment.avatarColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        comment.user.first().toString().uppercase(Locale.US),
                        color      = comment.avatarColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 14.sp
                    )
                }
                Column {
                    Text(comment.user, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(comment.timeAgo, color = TextSecondary, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.ThumbUp, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                    Text("${comment.likes}", color = TextSecondary, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(comment.text, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun MetricCard(metric: InsightMetric, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(metric.icon, null, tint = GoldPrimary, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(metric.value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
            Text(metric.label, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    if (metric.positive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null,
                    tint     = if (metric.positive) SoftGreen else ErrorRed,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    metric.change,
                    color    = if (metric.positive) SoftGreen else ErrorRed,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun AudienceBar(label: String, percent: Int, color: Color) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(90.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(BackgroundSecondary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Text("$percent%", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(34.dp))
    }
}

@Composable
fun SentimentDot(label: String, percent: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f))
                .border(2.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$percent%", color = color, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
fun PulsingLiveDot(tint: Color = ErrorRed) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue   = 0.4f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = pulse))
    )
}

// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
fun PreviewPitchViewScreen() {
    PitchViewScreen(rememberNavController())
}