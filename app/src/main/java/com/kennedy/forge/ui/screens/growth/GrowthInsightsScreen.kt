package com.kennedy.forge.ui.screens.growth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.navigation.ROUT_FeedbackDashboard
import com.kennedy.forge.navigation.ROUT_Payments
import com.kennedy.forge.navigation.ROUT_SubmitWork
import com.kennedy.forge.ui.theme.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────────────────────────

data class GrowthStat(
    val label: String,
    val value: String,
    val delta: String,
    val isPositive: Boolean,
    val icon: ImageVector,
    val accentColor: Color
)

data class Achievement(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val progress: Float,        // 0f – 1f
    val xp: Int,
    val rarity: AchievementRarity
)

enum class AchievementRarity(val label: String, val color: Color) {
    COMMON("Common", Color(0xFF9E9E9E)),
    RARE("Rare", Color(0xFF7DAED3)),
    EPIC("Epic", Color(0xFF9C7BDB)),
    LEGENDARY("Legendary", Color(0xFFC89B3C))
}

data class WeeklyPoint(val day: String, val value: Float)  // value 0f–1f normalised

enum class GrowthTab { OVERVIEW, ACHIEVEMENTS, ACTIVITY }

// ─────────────────────────────────────────────────────────────────
//  MOCK DATA  🔁 Replace with real Firebase reads
// ─────────────────────────────────────────────────────────────────

private val mockStats = listOf(
    GrowthStat("Total XP",       "4,820",  "+320",  true,  Icons.Default.Stars,        Color(0xFFC89B3C)),
    GrowthStat("Uploads",        "47",     "+5",    true,  Icons.Default.CloudUpload,  Color(0xFF7DAED3)),
    GrowthStat("Feedback Given", "132",    "+18",   true,  Icons.Default.Forum,        Color(0xFF7FBF9F)),
    GrowthStat("Streak",         "14 days","",      true,  Icons.Default.Whatshot,    Color(0xFFE8A87C))
)

private val mockAchievements = listOf(
    Achievement("First Upload",     "Upload your first creative work",          Icons.Default.CloudUpload,   true,  1f,    50,   AchievementRarity.COMMON),
    Achievement("Feedback King",    "Give feedback on 100 works",               Icons.Default.Forum,         true,  1f,    200,  AchievementRarity.RARE),
    Achievement("On Fire",          "Maintain a 14-day streak",                 Icons.Default.Whatshot,     true,  1f,    350,  AchievementRarity.EPIC),
    Achievement("Portfolio Pro",    "Complete your full portfolio",             Icons.Default.WorkspacePremium, false, 0.65f, 500,  AchievementRarity.EPIC),
    Achievement("Mentor",           "Give 1-on-1 mentorship sessions",          Icons.Default.School,        false, 0.20f, 750,  AchievementRarity.LEGENDARY),
    Achievement("Viral Creator",    "Get 1,000 views on a single upload",       Icons.Default.Visibility,    false, 0.43f, 400,  AchievementRarity.RARE),
    Achievement("Collaborator",     "Co-create with 5 different creators",      Icons.Default.Group,         false, 0.10f, 300,  AchievementRarity.COMMON),
    Achievement("Elite Member",     "Subscribe to the Elite plan",              Icons.Default.Diamond,       true,  1f,    100,  AchievementRarity.LEGENDARY)
)

private val mockWeekly = listOf(
    WeeklyPoint("Mon", 0.40f),
    WeeklyPoint("Tue", 0.65f),
    WeeklyPoint("Wed", 0.50f),
    WeeklyPoint("Thu", 0.85f),
    WeeklyPoint("Fri", 0.70f),
    WeeklyPoint("Sat", 0.95f),
    WeeklyPoint("Sun", 0.60f)
)

private val mockRecentActivity = listOf(
    Triple(Icons.Default.CloudUpload,  "Uploaded 'Brand Identity v3'",       "2h ago"),
    Triple(Icons.Default.Forum,        "Gave feedback on 'Motion Design'",   "5h ago"),
    Triple(Icons.Default.Stars,        "Earned 'On Fire' achievement",       "1d ago"),
    Triple(Icons.Default.Visibility,   "Your upload reached 430 views",      "1d ago"),
    Triple(Icons.Default.WorkspacePremium, "Upgraded to Elite plan",         "3d ago")
)

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthInsightScreen(navController: NavController) {

    var activeTab by remember { mutableStateOf(GrowthTab.OVERVIEW) }

    // Animate XP ring on first composition
    val xpProgress by animateFloatAsState(
        targetValue    = 0.72f,   // 🔁 Replace with real XP / nextLevelXP ratio
        animationSpec  = tween(durationMillis = 1200, easing = EaseOutCubic),
        label          = "xp_ring"
    )

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Growth & Insights",
                        fontWeight    = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color         = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUT_Payments) }) {
                        Icon(Icons.Default.WorkspacePremium, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── HERO: XP RING + LEVEL ─────────────────────────────
            GrowthHeroSection(xpProgress = xpProgress)

            // ── STAT CARDS ────────────────────────────────────────
            StatCardsRow()

            // ── TAB SWITCHER ──────────────────────────────────────
            GrowthTabSwitcher(activeTab = activeTab, onTabSelected = { activeTab = it })

            // ── TAB CONTENT ───────────────────────────────────────
            AnimatedContent(
                targetState   = activeTab,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label         = "tab_content"
            ) { tab ->
                when (tab) {
                    GrowthTab.OVERVIEW     -> OverviewContent(navController)
                    GrowthTab.ACHIEVEMENTS -> AchievementsContent()
                    GrowthTab.ACTIVITY     -> ActivityContent(navController)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO SECTION — XP ring + level badge
// ─────────────────────────────────────────────────────────────────

@Composable
private fun GrowthHeroSection(xpProgress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(DarkSurface, BackgroundMain))
            )
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle dot grid on dark bg
        Canvas(Modifier.matchParentSize()) {
            val spacing = 28f
            val cols = (size.width / spacing).toInt() + 2
            val rows = (size.height / spacing).toInt() + 2
            for (c in 0..cols) for (r in 0..rows) {
                drawCircle(
                    color  = Color(0xFFC89B3C).copy(alpha = 0.06f),
                    radius = 1.5f,
                    center = Offset(c * spacing, r * spacing)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // XP Ring
            XpRing(progress = xpProgress, size = 160.dp, currentXp = 4820, nextXp = 6000)

            Spacer(Modifier.height(16.dp))

            // Level badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    "LEVEL 12  ·  FORGE CREATOR",
                    color         = DarkSurface,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 11.sp,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "1,180 XP to Level 13",
                color    = TextOnDark.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  XP RING (custom Canvas arc)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun XpRing(progress: Float, size: Dp, currentXp: Int, nextXp: Int) {
    Box(
        modifier         = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius      = (this.size.minDimension - strokeWidth) / 2f
            val center      = Offset(this.size.width / 2f, this.size.height / 2f)
            val startAngle  = -90f

            // Track ring
            drawCircle(
                color  = Color(0xFF2A2A2A),
                radius = radius,
                center = center,
                style  = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                brush      = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0.0f to GoldDeep,
                        0.5f to GoldPrimary,
                        1.0f to GoldAccent
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = 360f * progress,
                useCenter  = false,
                style      = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                size       = Size(radius * 2, radius * 2),
                topLeft    = Offset(center.x - radius, center.y - radius)
            )

            // Dot at progress tip
            val angle  = Math.toRadians((startAngle + 360f * progress).toDouble())
            val dotX   = center.x + radius * cos(angle).toFloat()
            val dotY   = center.y + radius * sin(angle).toFloat()
            drawCircle(color = GoldAccent, radius = 8.dp.toPx(), center = Offset(dotX, dotY))
            drawCircle(color = DarkSurface, radius = 4.dp.toPx(), center = Offset(dotX, dotY))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                currentXp.toString().let {
                    if (it.length > 3) it.dropLast(3) + "," + it.takeLast(3) else it
                },
                color      = TextOnDark,
                fontWeight = FontWeight.Black,
                fontSize   = 28.sp
            )
            Text("XP", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  STAT CARDS ROW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCardsRow() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
        Text(
            "YOUR NUMBERS",
            color         = TextSecondary,
            fontSize      = 10.sp,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier      = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            mockStats.take(2).forEach { stat ->
                StatCard(stat = stat, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            mockStats.drop(2).forEach { stat ->
                StatCard(stat = stat, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatCard(stat: GrowthStat, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp))
                        .background(stat.accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(stat.icon, null, tint = stat.accentColor, modifier = Modifier.size(18.dp))
                }
                if (stat.delta.isNotEmpty()) {
                    Text(
                        stat.delta,
                        color      = if (stat.isPositive) SoftGreen else Error,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(stat.value, color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
            Text(stat.label, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TAB SWITCHER
// ─────────────────────────────────────────────────────────────────

@Composable
private fun GrowthTabSwitcher(activeTab: GrowthTab, onTabSelected: (GrowthTab) -> Unit) {
    val tabs = listOf(
        GrowthTab.OVERVIEW     to "Overview",
        GrowthTab.ACHIEVEMENTS to "Achievements",
        GrowthTab.ACTIVITY     to "Activity"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundSecondary)
    ) {
        tabs.forEach { (tab, label) ->
            val isActive = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) Brush.horizontalGradient(listOf(GoldPrimary, GoldAccent))
                        else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color      = if (isActive) DarkSurface else TextSecondary,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize   = 12.sp
                )
            }
        }
    }

    Spacer(Modifier.height(20.dp))
}

// ─────────────────────────────────────────────────────────────────
//  TAB 1: OVERVIEW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun OverviewContent(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Weekly activity bar chart
        WeeklyChart()

        Spacer(Modifier.height(24.dp))

        // Quick action cards
        Text(
            "QUICK ACTIONS",
            color = TextSecondary, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                icon    = Icons.Default.CloudUpload,
                label   = "Upload Work",
                color   = GoldPrimary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(ROUT_SubmitWork) }
            )
            QuickActionCard(
                icon    = Icons.Default.Forum,
                label   = "Give Feedback",
                color   = SoftBlue,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(ROUT_FeedbackDashboard) }
            )
            QuickActionCard(
                icon    = Icons.Default.WorkspacePremium,
                label   = "Upgrade",
                color   = SoftPeach,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(ROUT_Payments) }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Top 3 unlocked achievements teaser
        Text(
            "RECENT ACHIEVEMENTS",
            color = TextSecondary, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        mockAchievements.filter { it.isUnlocked }.take(3).forEach { achievement ->
            MiniAchievementRow(achievement = achievement)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))

        // See all achievements link
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { /* handled by parent tab switch */ }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "View all achievements →",
                color      = GoldPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  WEEKLY BAR CHART (Canvas)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun WeeklyChart() {
    // Animate bars in
    val animProgress by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label         = "chart_anim"
    )

    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Text("WEEKLY ACTIVITY", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("This week", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount  = mockWeekly.size
                val barWidth  = (size.width - (barCount - 1) * 8.dp.toPx()) / barCount
                val maxHeight = size.height - 24.dp.toPx()

                mockWeekly.forEachIndexed { i, point ->
                    val barHeight = maxHeight * point.value * animProgress
                    val left      = i * (barWidth + 8.dp.toPx())
                    val top       = size.height - barHeight - 24.dp.toPx()

                    // Bar background track
                    drawRoundRect(
                        color        = Color(0xFFEDE7DD),
                        topLeft      = Offset(left, size.height - maxHeight - 24.dp.toPx()),
                        size         = Size(barWidth, maxHeight),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    // Filled bar
                    drawRoundRect(
                        brush        = Brush.verticalGradient(
                            colors    = listOf(GoldAccent, GoldDeep),
                            startY    = top,
                            endY      = top + barHeight
                        ),
                        topLeft      = Offset(left, top),
                        size         = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                }
            }

            // Day labels
            Row(
                modifier              = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                mockWeekly.forEach { point ->
                    Text(point.day, color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  QUICK ACTION CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun QuickActionCard(
    icon: ImageVector, label: String, color: Color,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Card(
        modifier  = modifier.clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier              = Modifier.padding(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier         = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  MINI ACHIEVEMENT ROW (used in overview tab)
// ─────────────────────────────────────────────────────────────────

@Composable
private fun MiniAchievementRow(achievement: Achievement) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                .background(achievement.rarity.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(achievement.icon, null, tint = achievement.rarity.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(achievement.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(achievement.description, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(GoldPrimary.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("+${achievement.xp} XP", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TAB 2: ACHIEVEMENTS
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AchievementsContent() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Unlocked / total summary
        val unlockedCount = mockAchievements.count { it.isUnlocked }
        val totalCount    = mockAchievements.size
        val overallProgress = unlockedCount.toFloat() / totalCount

        // Summary card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = DarkCard),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                Column {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "$unlockedCount / $totalCount",
                                color = TextOnDark, fontWeight = FontWeight.Black, fontSize = 28.sp
                            )
                            Text("Achievements unlocked", color = TextOnDark.copy(0.5f), fontSize = 12.sp)
                        }
                        // Mini ring
                        Box(contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.size(56.dp)) {
                                val stroke = 6.dp.toPx()
                                val r      = (size.minDimension - stroke) / 2f
                                drawCircle(color = Color(0xFF2A2A2A), radius = r, style = Stroke(stroke))
                                drawArc(
                                    color      = GoldPrimary,
                                    startAngle = -90f,
                                    sweepAngle = 360f * overallProgress,
                                    useCenter  = false,
                                    style      = Stroke(stroke, cap = StrokeCap.Round),
                                    size       = Size(r * 2, r * 2),
                                    topLeft    = Offset(center.x - r, center.y - r)
                                )
                            }
                            Text(
                                "${(overallProgress * 100).toInt()}%",
                                color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress      = { overallProgress },
                        modifier      = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color         = GoldPrimary,
                        trackColor    = Color(0xFF2A2A2A)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Unlocked section
        Text("UNLOCKED", color = SoftGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 10.dp))

        mockAchievements.filter { it.isUnlocked }.forEach { achievement ->
            AchievementCard(achievement = achievement)
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(16.dp))

        // Locked section
        Text("IN PROGRESS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 10.dp))

        mockAchievements.filter { !it.isUnlocked }.forEach { achievement ->
            AchievementCard(achievement = achievement)
            Spacer(Modifier.height(10.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ACHIEVEMENT CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun AchievementCard(achievement: Achievement) {
    val alpha = if (achievement.isUnlocked) 1f else 0.6f

    Card(
        modifier  = Modifier.fillMaxWidth().alpha(alpha),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(if (achievement.isUnlocked) 3.dp else 0.dp),
        border    = if (achievement.isUnlocked && achievement.rarity == AchievementRarity.LEGENDARY)
            BorderStroke(1.dp, Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Icon
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(
                            if (achievement.isUnlocked) achievement.rarity.color.copy(alpha = 0.15f)
                            else Color(0xFFEDE7DD)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        achievement.icon, null,
                        tint     = if (achievement.isUnlocked) achievement.rarity.color else Color(0xFFCCC5B8),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(achievement.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        // Rarity badge
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                .background(achievement.rarity.color.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(achievement.rarity.label, color = achievement.rarity.color,
                                fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                    }
                    Text(achievement.description, color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(Modifier.width(8.dp))

                // XP pill
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("+${achievement.xp} XP", color = GoldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (achievement.isUnlocked) {
                        Spacer(Modifier.height(4.dp))
                        Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Progress bar for locked achievements
            if (!achievement.isUnlocked) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress   = { achievement.progress },
                        modifier   = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)),
                        color      = achievement.rarity.color,
                        trackColor = BackgroundSecondary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${(achievement.progress * 100).toInt()}%",
                        color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TAB 3: ACTIVITY
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ActivityContent(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

        // Streak card
        StreakCard()

        Spacer(Modifier.height(20.dp))

        Text(
            "RECENT ACTIVITY",
            color = TextSecondary, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        mockRecentActivity.forEachIndexed { index, (icon, label, time) ->
            ActivityRow(icon = icon, label = label, time = time, isLast = index == mockRecentActivity.lastIndex)
        }

        Spacer(Modifier.height(20.dp))

        // CTA to upload
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(GoldDeep, GoldAccent)))
                .clickable { navController.navigate(ROUT_SubmitWork) }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudUpload, null, tint = DarkSurface, modifier = Modifier.size(20.dp))
                Text("Upload New Work", color = DarkSurface, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  STREAK CARD
// ─────────────────────────────────────────────────────────────────

@Composable
private fun StreakCard() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier.size(56.dp).clip(CircleShape)
                    .background(SoftPeach.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", fontSize = 28.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("14-Day Streak!", color = TextOnDark, fontWeight = FontWeight.Black, fontSize = 18.sp)
                Text("Keep uploading daily to maintain it", color = TextOnDark.copy(0.5f), fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("🏆", fontSize = 24.sp)
                Text("Best: 21", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ACTIVITY ROW
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ActivityRow(icon: ImageVector, label: String, time: String, isLast: Boolean) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier         = Modifier.size(36.dp).clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFFEDE7DD)))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
            Text(label, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(time, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GrowthInsightScreenPreview() {
    GrowthInsightScreen(rememberNavController())
}