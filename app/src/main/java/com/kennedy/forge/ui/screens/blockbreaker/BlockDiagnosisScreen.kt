package com.kennedy.forge.ui.screens.blockbreaker

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val DarkSurface         = Color(0xFF121212)
private val ErrorRed            = Color(0xFFE53935)
private val WarningAmber        = Color(0xFFFFA726)

// ─────────────────────────────────────────────
// MODELS
// ─────────────────────────────────────────────

enum class BlockSeverity(val label: String, val color: Color) {
    LOW("Low", SoftGreen),
    MEDIUM("Medium", WarningAmber),
    HIGH("High", SoftPeach),
    CRITICAL("Critical", ErrorRed)
}

enum class BlockCategory(val label: String, val icon: ImageVector) {
    FOCUS("Focus & Clarity",   Icons.Default.CenterFocusStrong),
    EMOTION("Emotional Block", Icons.Default.SentimentDissatisfied),
    CREATIVE("Creative Block", Icons.Default.Brush),
    MOTIVATION("Motivation",   Icons.Default.BatterySaver),
    MINDSET("Mindset",         Icons.Default.Psychology),
    SOCIAL("Social Anxiety",   Icons.Default.People)
}

data class DiagnosisQuestion(
    val id: Int,
    val question: String,
    val options: List<String>
)

data class BlockEntry(
    val id: Int,
    val title: String,
    val category: BlockCategory,
    val severity: BlockSeverity,
    val dateLabel: String,
    val resolved: Boolean,
    val tags: List<String>
)

data class BlockResult(
    val category: BlockCategory,
    val severity: BlockSeverity,
    val score: Int,
    val summary: String,
    val rootCauses: List<String>,
    val recommendations: List<String>,
    val affirmation: String
)

// ─────────────────────────────────────────────
// DEMO DATA
// ─────────────────────────────────────────────

private val diagnosisQuestions = listOf(
    DiagnosisQuestion(1, "How would you describe your current mental state when trying to work?", listOf(
        "Calm and focused", "Slightly distracted", "Overwhelmed and scattered", "Completely frozen"
    )),
    DiagnosisQuestion(2, "How long have you been experiencing this creative/productivity block?", listOf(
        "Just today", "A few days", "1–2 weeks", "More than a month"
    )),
    DiagnosisQuestion(3, "What best describes the root feeling behind your block?", listOf(
        "Fear of failure", "Lack of inspiration", "Emotional exhaustion", "External pressure"
    )),
    DiagnosisQuestion(4, "How does the block affect your daily output?", listOf(
        "Slightly slower", "Noticeably less work", "Barely producing anything", "Completely stuck"
    )),
    DiagnosisQuestion(5, "Which area feels most impacted?", listOf(
        "Creative work", "Decision making", "Motivation to start", "Maintaining consistency"
    ))
)

private val demoBlockHistory = listOf(
    BlockEntry(1, "Post-launch creative slump",        BlockCategory.CREATIVE,    BlockSeverity.HIGH,     "3 days ago",   false, listOf("creative", "burnout")),
    BlockEntry(2, "Fear of presenting new ideas",      BlockCategory.SOCIAL,      BlockSeverity.MEDIUM,   "1 week ago",   true,  listOf("social", "fear")),
    BlockEntry(3, "Motivation crash after rejection",  BlockCategory.MOTIVATION,  BlockSeverity.CRITICAL, "2 weeks ago",  false, listOf("motivation", "rejection")),
    BlockEntry(4, "Overthinking project direction",    BlockCategory.MINDSET,     BlockSeverity.MEDIUM,   "3 weeks ago",  true,  listOf("mindset", "overthinking")),
    BlockEntry(5, "Emotional overwhelm mid-project",   BlockCategory.EMOTION,     BlockSeverity.HIGH,     "1 month ago",  true,  listOf("emotion", "stress")),
    BlockEntry(6, "Can't enter deep focus sessions",   BlockCategory.FOCUS,       BlockSeverity.LOW,      "6 weeks ago",  true,  listOf("focus", "distraction"))
)

private val demoResult = BlockResult(
    category    = BlockCategory.CREATIVE,
    severity    = BlockSeverity.HIGH,
    score       = 74,
    summary     = "You're experiencing a high-severity creative block driven primarily by fear of judgment and emotional fatigue. This is extremely common after periods of high output — your mind is asking for permission to rest and reset.",
    rootCauses  = listOf(
        "Accumulated creative fatigue from sustained high output",
        "Fear of failure reducing willingness to experiment",
        "External pressure creating perfectionist paralysis",
        "Disconnect between inner vision and external expectations"
    ),
    recommendations = listOf(
        "Take a deliberate 48-hour creative rest — consume, don't create",
        "Start a 5-minute daily free-write with zero self-editing rules",
        "Separate ideation sessions from execution sessions this week",
        "Share an imperfect work-in-progress to break the perfectionism cycle",
        "Reconnect with your original 'why' through journaling"
    ),
    affirmation = "Blocks aren't walls — they're redirections. Your next breakthrough is already forming beneath the surface."
)

// ─────────────────────────────────────────────
// TAB ENUM
// ─────────────────────────────────────────────

private enum class BlockTab(val label: String, val icon: ImageVector) {
    DIAGNOSE("Diagnose", Icons.Default.Psychology),
    HISTORY("History",   Icons.Default.History),
    RESULTS("Results",   Icons.Default.Insights)
}

// ─────────────────────────────────────────────
// ROOT SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockDiagnosisScreen(navController: NavController) {

    var activeTab        by remember { mutableStateOf(BlockTab.DIAGNOSE) }
    var answers          by remember { mutableStateOf(mapOf<Int, Int>()) }     // questionId -> selectedOptionIndex
    var currentQuestion  by remember { mutableStateOf(0) }
    var diagnosisComplete by remember { mutableStateOf(false) }
    var showToast        by remember { mutableStateOf(false) }
    var toastMessage     by remember { mutableStateOf("") }
    var historyList      by remember { mutableStateOf(demoBlockHistory.toMutableList()) }
    var filterResolved   by remember { mutableStateOf<Boolean?>(null) } // null = all

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
                            "Block Breaker",
                            color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                        )
                        Text(
                            "Identify · Understand · Overcome",
                            color = GoldPrimary, fontSize = 10.sp, letterSpacing = 0.5.sp
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
                        toastMessage = "Saved to journal"
                        showToast = true
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .navigationBarsPadding()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BlockTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Column(
                        modifier = Modifier
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
                                    if (selected) GoldPrimary.copy(alpha = 0.12f) else Color.Transparent
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Show badge on Results if diagnosis is complete
                            if (tab == BlockTab.RESULTS && diagnosisComplete) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .offset(x = 8.dp, y = (-8).dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary)
                                        .align(Alignment.TopEnd)
                                )
                            }
                            Icon(
                                tab.icon,
                                contentDescription = tab.label,
                                tint = if (selected) GoldPrimary else TextSecondary,
                                modifier = Modifier.size(20.dp)
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
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                label = "block_tab_transition"
            ) { tab ->
                when (tab) {
                    BlockTab.DIAGNOSE -> DiagnoseTab(
                        questions        = diagnosisQuestions,
                        answers          = answers,
                        currentQuestion  = currentQuestion,
                        diagnosisComplete = diagnosisComplete,
                        padding          = padding,
                        onAnswer = { qId, optionIdx ->
                            answers = answers + (qId to optionIdx)
                        },
                        onNext = {
                            if (currentQuestion < diagnosisQuestions.lastIndex) {
                                currentQuestion++
                            } else {
                                diagnosisComplete = true
                                activeTab = BlockTab.RESULTS
                                toastMessage = "Diagnosis complete! View your results."
                                showToast = true
                            }
                        },
                        onPrev = {
                            if (currentQuestion > 0) currentQuestion--
                        },
                        onRestart = {
                            answers = mapOf()
                            currentQuestion = 0
                            diagnosisComplete = false
                        }
                    )

                    BlockTab.HISTORY -> HistoryTab(
                        history       = historyList,
                        filterResolved = filterResolved,
                        padding       = padding,
                        onFilterChange = { filterResolved = it },
                        onDelete = { entry ->
                            historyList = historyList.toMutableList()
                                .also { it.remove(entry) }
                            toastMessage = "Entry removed"
                            showToast = true
                        },
                        onToggleResolved = { entry ->
                            val idx = historyList.indexOf(entry)
                            if (idx >= 0) {
                                val updated = historyList.toMutableList()
                                updated[idx] = entry.copy(resolved = !entry.resolved)
                                historyList = updated
                            }
                        }
                    )

                    BlockTab.RESULTS -> ResultsTab(
                        result            = demoResult,
                        diagnosisComplete = diagnosisComplete,
                        padding           = padding,
                        onStartDiagnosis  = { activeTab = BlockTab.DIAGNOSE },
                        onSave = {
                            toastMessage = "Result saved to history!"
                            showToast = true
                        }
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
//  TAB 1 — DIAGNOSE
// ═══════════════════════════════════════════════════════

@Composable
fun DiagnoseTab(
    questions: List<DiagnosisQuestion>,
    answers: Map<Int, Int>,
    currentQuestion: Int,
    diagnosisComplete: Boolean,
    padding: PaddingValues,
    onAnswer: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onRestart: () -> Unit
) {
    val question      = questions[currentQuestion]
    val selectedOption = answers[question.id]
    val progress      = (currentQuestion + 1).toFloat() / questions.size.toFloat()

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
                    .background(
                        Brush.linearGradient(
                            listOf(GoldDeep.copy(alpha = 0.85f), GoldAccent.copy(alpha = 0.6f), BackgroundMain)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Psychology, null, tint = GoldDeep, modifier = Modifier.size(20.dp))
                        Text(
                            "BLOCK DIAGNOSIS",
                            color       = GoldDeep,
                            fontSize    = 11.sp,
                            fontWeight  = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Let's understand\nwhat's holding you back.",
                        color      = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 22.sp,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Answer honestly — this is your safe space.",
                        color    = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── PROGRESS ─────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Question ${currentQuestion + 1} of ${questions.size}",
                        color    = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        color      = GoldPrimary,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BackgroundSecondary)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(listOf(GoldAccent, GoldPrimary))
                            )
                    )
                }
            }
        }

        // ── QUESTION CARD ────────────────────────────────
        item {
            AnimatedContent(
                targetState = currentQuestion,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                },
                label = "question_transition"
            ) { _ ->
                Card(
                    modifier  = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            question.question,
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(20.dp))

                        question.options.forEachIndexed { idx, option ->
                            val isSelected = selectedOption == idx
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) GoldPrimary.copy(alpha = 0.1f)
                                        else BackgroundSecondary.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) GoldPrimary else BackgroundSecondary,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { onAnswer(question.id, idx) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) GoldPrimary else BackgroundSecondary
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) GoldDeep else Color.Transparent,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            tint     = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Text(
                                    option,
                                    color      = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize   = 14.sp,
                                    modifier   = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── NAV BUTTONS ──────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentQuestion > 0) {
                    OutlinedButton(
                        onClick = onPrev,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("Previous", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick  = onNext,
                    enabled  = selectedOption != null,
                    modifier = Modifier.weight(if (currentQuestion > 0) 1f else 1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = GoldPrimary,
                        disabledContainerColor = BackgroundSecondary
                    )
                ) {
                    Text(
                        if (currentQuestion == diagnosisQuestions.lastIndex) "Get Results" else "Next",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        // ── RESTART ──────────────────────────────────────
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onRestart) {
                    Icon(Icons.Default.Refresh, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restart Diagnosis", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }

        // ── CATEGORIES ───────────────────────────────────
        item {
            Text(
                "Block Categories",
                color      = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        item {
            val rows = BlockCategory.entries.chunked(2)
            Column(
                modifier            = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { cat ->
                            CategoryPill(category = cat, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 2 — HISTORY
// ═══════════════════════════════════════════════════════

@Composable
fun HistoryTab(
    history: List<BlockEntry>,
    filterResolved: Boolean?,
    padding: PaddingValues,
    onFilterChange: (Boolean?) -> Unit,
    onDelete: (BlockEntry) -> Unit,
    onToggleResolved: (BlockEntry) -> Unit
) {
    val filtered = when (filterResolved) {
        true  -> history.filter { it.resolved }
        false -> history.filter { !it.resolved }
        null  -> history
    }

    val resolvedCount   = history.count { it.resolved }
    val unresolvedCount = history.count { !it.resolved }

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
                            listOf(SoftBlue.copy(alpha = 0.12f), BackgroundMain)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.History, null, tint = SoftBlue, modifier = Modifier.size(18.dp))
                        Text("BLOCK HISTORY", color = SoftBlue, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Your journey to clarity.", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))

                    // Stats mini row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStatChip("${history.size} Total",       GoldPrimary)
                        MiniStatChip("$resolvedCount Resolved",     SoftGreen)
                        MiniStatChip("$unresolvedCount Active",     SoftPeach)
                    }
                }
            }
        }

        // ── FILTER CHIPS ─────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("All",      null,  TextSecondary),
                    Triple("Active",   false, SoftPeach),
                    Triple("Resolved", true,  SoftGreen)
                ).forEach { (label, value, accent) ->
                    val isActive = filterResolved == value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isActive) accent.copy(alpha = 0.15f) else CardBackground)
                            .border(1.dp, if (isActive) accent else BackgroundSecondary, RoundedCornerShape(20.dp))
                            .clickable { onFilterChange(value) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            label,
                            color      = if (isActive) accent else TextSecondary,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = 13.sp
                        )
                    }
                }
            }
        }

        // ── EMPTY STATE ──────────────────────────────────
        if (filtered.isEmpty()) {
            item {
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
                            .background(SoftBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, null, tint = SoftBlue, modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("No entries here", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Complete a diagnosis to start building your history.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(filtered, key = { it.id }) { entry ->
                HistoryEntryCard(
                    entry            = entry,
                    onDelete         = { onDelete(entry) },
                    onToggleResolved = { onToggleResolved(entry) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
//  TAB 3 — RESULTS
// ═══════════════════════════════════════════════════════

@Composable
fun ResultsTab(
    result: BlockResult,
    diagnosisComplete: Boolean,
    padding: PaddingValues,
    onStartDiagnosis: () -> Unit,
    onSave: () -> Unit
) {
    if (!diagnosisComplete) {
        // Not yet diagnosed — prompt screen
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Insights, null, tint = GoldPrimary, modifier = Modifier.size(38.dp))
                }
                Spacer(Modifier.height(20.dp))
                Text("No results yet", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Complete a Block Diagnosis to receive your personalised breakdown, root causes, and actionable recommendations.",
                    color     = TextSecondary,
                    fontSize  = 14.sp,
                    lineHeight = 21.sp,
                    textAlign  = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick   = onStartDiagnosis,
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier  = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Psychology, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start Diagnosis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier       = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        // ── RESULT HERO ──────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                result.severity.color.copy(alpha = 0.2f),
                                BackgroundMain
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(result.category.icon, null, tint = result.severity.color, modifier = Modifier.size(18.dp))
                        Text(
                            result.category.label.uppercase(Locale.US),
                            color         = result.severity.color,
                            fontSize      = 11.sp,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Your Block Report", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Based on your diagnosis responses", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(16.dp))

                    // Severity + Score row
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SeverityBadge(result.severity)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldPrimary.copy(alpha = 0.1f))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Score: ${result.score}/100",
                                color      = GoldDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // ── SCORE VISUAL ─────────────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Block Severity Score", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundSecondary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(result.score / 100f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SoftGreen, WarningAmber, result.severity.color)
                                    )
                                )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Low", color = SoftGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${result.score}", color = result.severity.color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Critical", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── SUMMARY ──────────────────────────────────────
        item {
            ResultSection(
                title = "Summary",
                icon  = Icons.Default.Summarize,
                tint  = SoftBlue
            ) {
                Text(result.summary, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
            }
        }

        // ── ROOT CAUSES ──────────────────────────────────
        item {
            ResultSection(
                title = "Root Causes Identified",
                icon  = Icons.Default.Search,
                tint  = SoftPeach
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    result.rootCauses.forEach { cause ->
                        Row(
                            verticalAlignment     = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(SoftPeach)
                            )
                            Text(cause, color = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ── RECOMMENDATIONS ──────────────────────────────
        item {
            ResultSection(
                title = "What To Do Next",
                icon  = Icons.Default.Lightbulb,
                tint  = GoldAccent
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    result.recommendations.forEachIndexed { idx, rec ->
                        Row(
                            verticalAlignment     = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${idx + 1}",
                                    color      = GoldDeep,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize   = 12.sp
                                )
                            }
                            Text(rec, color = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ── AFFIRMATION ──────────────────────────────────
        item {
            Card(
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = GoldPrimary.copy(alpha = 0.08f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.FormatQuote, null, tint = GoldPrimary, modifier = Modifier.size(28.dp))
                    Text(
                        result.affirmation,
                        color      = GoldDeep,
                        fontSize   = 15.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle  = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // ── SAVE BUTTON ──────────────────────────────────
        item {
            Button(
                onClick   = onSave,
                modifier  = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .height(52.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save to History", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────
// SHARED COMPOSABLES
// ─────────────────────────────────────────────

@Composable
fun CategoryPill(category: BlockCategory, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBackground)
            .border(1.dp, BackgroundSecondary, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(category.icon, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
        Text(
            category.label,
            color      = TextPrimary,
            fontSize   = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HistoryEntryCard(
    entry: BlockEntry,
    onDelete: () -> Unit,
    onToggleResolved: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(entry.severity.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(entry.category.icon, null, tint = entry.severity.color, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(
                            entry.title,
                            color      = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        Text(entry.dateLabel, color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Box {
                    IconButton(
                        onClick  = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, null, tint = TextSecondary, modifier = Modifier.size(17.dp))
                    }
                    DropdownMenu(
                        expanded         = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor   = CardBackground
                    ) {
                        DropdownMenuItem(
                            text        = {
                                Text(
                                    if (entry.resolved) "Mark as Active" else "Mark as Resolved",
                                    color = TextPrimary, fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (entry.resolved) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                                    null, tint = SoftGreen, modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = { menuExpanded = false; onToggleResolved() }
                        )
                        DropdownMenuItem(
                            text        = { Text("Delete", color = ErrorRed, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            },
                            onClick = { menuExpanded = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Tags + severity row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    entry.tags.take(2).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(BackgroundSecondary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("#$tag", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                SeverityBadge(entry.severity)
            }

            // Resolved indicator
            if (entry.resolved) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(13.dp))
                    Text("Resolved", color = SoftGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun ResultSection(title: String, icon: ImageVector, tint: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                }
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
fun SeverityBadge(severity: BlockSeverity) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(severity.color.copy(alpha = 0.12f))
            .border(1.dp, severity.color.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(severity.label, color = severity.color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun MiniStatChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BlockDiagnosisScreenPreview() {
    BlockDiagnosisScreen(rememberNavController())
}