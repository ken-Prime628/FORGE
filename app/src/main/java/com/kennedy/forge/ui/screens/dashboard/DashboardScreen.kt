package com.kennedy.forge.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.firebase.auth.FirebaseAuth
import com.kennedy.forge.navigation.ROUT_BlockDiagnosis
import com.kennedy.forge.navigation.ROUT_Collaboration
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_DiscoveryFeed
import com.kennedy.forge.navigation.ROUT_FeedbackDashboard
import com.kennedy.forge.navigation.ROUT_GrowthInsight
import com.kennedy.forge.navigation.ROUT_Notification
import com.kennedy.forge.navigation.ROUT_Payments
import com.kennedy.forge.navigation.ROUT_PitchView
import com.kennedy.forge.navigation.ROUT_PortfolioBuilder
import com.kennedy.forge.navigation.ROUT_Profile
import com.kennedy.forge.navigation.ROUT_ProfileSetup
import com.kennedy.forge.navigation.ROUT_Search
import com.kennedy.forge.navigation.ROUT_SubmitWork

// ─────────────────────────────────────────────────────────────────
//  DATA MODELS
// ─────────────────────────────────────────────────────────────────
data class User(val name: String, val avatarUri: String? = null)

data class Tool(
    val title: String,
    val subtitle: String,
    val image: Int,
    val route: String,
    val accentColor: Color
)

data class Activity(
    val text: String,
    val subtext: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val timeAgo: String,
    val route: String
)

// ─────────────────────────────────────────────────────────────────
//  FIREBASE USER → User MODEL HELPER
//
//  Priority order for display name:
//    1. Firebase Auth displayName  (set via Google Sign-In or updateProfile)
//    2. Email prefix before '@'    (covers email/password registrations)
//    3. Phone number               (covers phone auth)
//    4. Fallback "User"
//
//  Priority order for avatar:
//    1. Firebase Auth photoUrl     (Google/Facebook profile photo)
//    2. null → initials shown instead
// ─────────────────────────────────────────────────────────────────
private fun FirebaseUser.toUser(): User {
    val displayName = this.displayName
        ?.takeIf { it.isNotBlank() }
        ?: this.email
            ?.substringBefore('@')
            ?.takeIf { it.isNotBlank() }
        ?: this.phoneNumber
            ?.takeIf { it.isNotBlank() }
        ?: "User"

    val photoUrl = this.photoUrl?.toString()

    return User(name = displayName, avatarUri = photoUrl)
}

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────
@Composable
fun DashboardScreen(navController: NavController) {

    val isPreview = LocalInspectionMode.current

    // ── Resolve real Firebase user; fall back gracefully in Preview ──
    val firebaseUser = if (isPreview) null else Firebase.auth.currentUser

    var user by remember {
        mutableStateOf(
            firebaseUser?.toUser() ?: User(name = "User", avatarUri = null)
        )
    }

    // Re-sync whenever the Auth state changes (e.g. profile update mid-session)
    DisposableEffect(Unit) {
        // 1. Declare the listener variable outside the if/else
        var listener: FirebaseAuth.AuthStateListener? = null

        if (isPreview) {
            onDispose { /* Nothing to clean up */ }
        } else {
            // 2. Initialize it here
            listener = FirebaseAuth.AuthStateListener { auth ->
                user = auth.currentUser?.toUser() ?: User(name = "User", avatarUri = null)
            }
            Firebase.auth.addAuthStateListener(listener!!)

            // 3. The onDispose can now safely see 'listener'
            onDispose {
                listener?.let { Firebase.auth.removeAuthStateListener(it) }
            }
        }
    }

    val tools = listOf(
        Tool("FeedbackLoop",   "Get honest reviews",  R.drawable.tool_feedback,  ROUT_SubmitWork,       Color(0xFF185FA5)),
        Tool("BlockBreaker",   "Diagnose your block", R.drawable.tool_blocks,    ROUT_BlockDiagnosis,   Color(0xFF993C1D)),
        Tool("PortfolioTruth", "Optimise your work",  R.drawable.tool_portfolio, ROUT_PortfolioBuilder, Color(0xFF0F6E56)),
        Tool("PitchMirror",    "Practise your pitch", R.drawable.tool_pitch,     ROUT_PitchView,        Color(0xFF854F0B)),
    )

    val activities = listOf(
        Activity("Feedback received",    "Brand identity — NovaCo", Icons.Default.RateReview, Color(0xFF185FA5), "2m ago",    ROUT_FeedbackDashboard),
        Activity("Block diagnosed",      "Perfectionism block",     Icons.Default.Psychology, Color(0xFF993C1D), "1h ago",    ROUT_BlockDiagnosis),
        Activity("Portfolio view spike", "142 views today",         Icons.Default.BarChart,   Color(0xFF0F6E56), "3h ago",    ROUT_PortfolioBuilder),
        Activity("Pitch score improved", "+8 points this session",  Icons.Default.Mic,        Color(0xFF854F0B), "Yesterday", ROUT_PitchView),
        Activity("Source flagged",       "2 restricted references", Icons.Default.Folder,     Color(0xFF3B6D11), "2d ago",    ROUT_GrowthInsight),
    )

    val goToProfile: () -> Unit = { navController.navigate(ROUT_Profile) }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            DashboardTopBar(
                user            = user,
                onSearch        = { navController.navigate(ROUT_Search) },
                onNotifications = { navController.navigate(ROUT_Notification) },
                onAvatarClick   = goToProfile
            )
        },
        bottomBar = { ForgeBottomNavigation(navController) }
    ) { padding ->

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            item {
                HeroGreetingCard(
                    user         = user,
                    onStreakClick = { navController.navigate(ROUT_GrowthInsight) }
                )
            }

            item {
                StatsStrip(
                    onReviewsClick   = { navController.navigate(ROUT_FeedbackDashboard) },
                    onViewsClick     = { navController.navigate(ROUT_PortfolioBuilder) },
                    onPitchClick     = { navController.navigate(ROUT_PitchView) },
                    onBlockFreeClick = { navController.navigate(ROUT_BlockDiagnosis) }
                )
            }

            item { ProExposureBanner(navController) }
            item { SubmitWorkCTA(navController) }
            item { CollaborationBanner(onClick = { navController.navigate(ROUT_Collaboration) }) }

            item {
                DashSectionHeader(
                    title    = "Creative tools",
                    action   = "See all",
                    onAction = { navController.navigate(ROUT_DiscoveryFeed) }
                )
            }
            item { CreativeToolsRow(tools = tools, navController = navController) }

            item {
                DashSectionHeader(
                    title    = "Recent activity",
                    action   = "View all",
                    onAction = { navController.navigate(ROUT_Notification) }
                )
            }
            items(activities) { activity ->
                ActivityCard(
                    activity = activity,
                    onClick  = { navController.navigate(activity.route) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  COLLABORATION BANNER — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
fun CollaborationBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF1A2340), Color(0xFF0F1A30))))
            .border(
                0.5.dp,
                Brush.linearGradient(listOf(SoftBlue.copy(alpha = 0.5f), GoldPrimary.copy(alpha = 0.3f))),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                    .background(SoftBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Group, null, tint = SoftBlue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Find Collaborators",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Connect with creators, find partners & build together",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp, lineHeight = 17.sp
                    )
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(SoftBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowForward, null, tint = SoftBlue, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TOP BAR — unchanged
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    user:            User,
    onSearch:        () -> Unit,
    onNotifications: () -> Unit,
    onAvatarClick:   () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(26.dp).clip(RoundedCornerShape(7.dp)).background(GoldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text("F", style = MaterialTheme.typography.labelLarge.copy(color = DarkSurface, fontWeight = FontWeight.W700))
                }
                Text(
                    "Forge",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600, color = TextPrimary, letterSpacing = (-0.3).sp
                    )
                )
            }
        },
        navigationIcon = {
            Spacer(Modifier.width(8.dp))
            UserAvatar(user = user, onAvatarClick = onAvatarClick)
        },
        actions = {
            IconButton(onClick = onSearch) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BackgroundSecondary), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = onNotifications) {
                Box(Modifier.size(36.dp).clip(CircleShape).background(BackgroundSecondary), contentAlignment = Alignment.Center) {
                    Box(Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Notifications, null, tint = TextPrimary, modifier = Modifier.size(18.dp).align(Alignment.Center))
                        Box(Modifier.size(7.dp).align(Alignment.TopEnd).offset((-4).dp, 4.dp).clip(CircleShape).background(Error).border(1.5.dp, BackgroundSecondary, CircleShape))
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
    )
}

// ─────────────────────────────────────────────────────────────────
//  USER AVATAR
//  Shows profile photo if available, otherwise shows the first
//  letter of the resolved display name — driven by Firebase Auth.
// ─────────────────────────────────────────────────────────────────
@Composable
fun UserAvatar(user: User, onAvatarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(GoldGradient)
            .clickable { onAvatarClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!user.avatarUri.isNullOrBlank()) {
            // Google / Facebook profile photo
            AsyncImage(
                model              = user.avatarUri,
                contentDescription = "Profile",
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            // Initial derived from whatever name Firebase returned
            Text(
                text  = user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = DarkSurface,
                    fontWeight = FontWeight.W700
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO GREETING CARD — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
private fun HeroGreetingCard(user: User, onStreakClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
            .height(186.dp).clip(RoundedCornerShape(28.dp)).shadow(8.dp, RoundedCornerShape(28.dp))
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.hero_bg),
            contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.72f)))))
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldPrimary.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.15f),
                    radius = size.width * 0.5f
                )
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape  = RoundedCornerShape(20.dp),
                color  = GoldPrimary.copy(alpha = 0.20f),
                border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { onStreakClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Whatshot, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                    Text("12 day streak", style = MaterialTheme.typography.labelSmall.copy(color = GoldAccent, fontWeight = FontWeight.W500, letterSpacing = 0.3.sp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Good morning,", style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.65f)))
                Text("${user.name}.", style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.W700, letterSpacing = (-0.3).sp))
                Text("You're forging greatness today.", style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f)))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  STATS STRIP — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
private fun StatsStrip(
    onReviewsClick: () -> Unit, onViewsClick: () -> Unit,
    onPitchClick: () -> Unit, onBlockFreeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp)).background(DarkSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        StatItem("3",   "Pending\nreviews",  GoldAccent,  onClick = onReviewsClick)
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.White.copy(alpha = 0.08f))
        StatItem("142", "Portfolio\nviews",  Color.White, onClick = onViewsClick)
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.White.copy(alpha = 0.08f))
        StatItem("81",  "Pitch\nscore",      SoftGreen,   onClick = onPitchClick)
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.White.copy(alpha = 0.08f))
        StatItem("7",   "Block-free\ndays",  SoftBlue,    onClick = onBlockFreeClick)
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W700, color = color))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.45f), letterSpacing = 0.2.sp, lineHeight = 14.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  PRO EXPOSURE BANNER — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ProExposureBanner(navController: NavController) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        border    = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(GoldGradient), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Star, null, tint = DarkSurface, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pro member", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.W600))
                    Surface(shape = RoundedCornerShape(6.dp), color = GoldPrimary.copy(alpha = 0.12f)) {
                        Text("ACTIVE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.W700, fontSize = 9.sp, letterSpacing = 0.6.sp))
                    }
                }
                Text("You were spotted 12× this week — upgrade to Elite for client matching",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp))
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier.height(34.dp).clip(RoundedCornerShape(10.dp)).background(GoldGradient)
                    .clickable { navController.navigate(ROUT_Payments) }.padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Upgrade", style = MaterialTheme.typography.labelMedium.copy(color = DarkSurface, fontWeight = FontWeight.W700))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SUBMIT WORK CTA — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
fun SubmitWorkCTA(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp)
            .clip(RoundedCornerShape(16.dp)).background(DarkSurface)
            .border(0.5.dp, Brush.linearGradient(listOf(GoldAccent.copy(alpha = 0.4f), GoldPrimary.copy(alpha = 0.2f))), RoundedCornerShape(16.dp))
            .clickable { navController.navigate(ROUT_SubmitWork) },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(GoldGradient), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(14.dp))
            }
            Text("Submit new work for review", style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.W500))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SECTION HEADER — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
fun DashSectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.width(3.dp).height(16.dp).background(GoldGradient, RoundedCornerShape(2.dp)))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.W600))
        }
        Text(action, style = MaterialTheme.typography.labelMedium.copy(color = GoldPrimary, fontWeight = FontWeight.W500),
            modifier = Modifier.clickable(onClick = onAction))
    }
}

// ─────────────────────────────────────────────────────────────────
//  CREATIVE TOOLS ROW — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
fun CreativeToolsRow(tools: List<Tool>, navController: NavController) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tools) { tool -> ToolCard(tool = tool, onClick = { navController.navigate(tool.route) }) }
    }
}

@Composable
private fun ToolCard(tool: Tool, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.width(152.dp).height(178.dp).clickable(onClick = onClick),
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        border    = BorderStroke(0.5.dp, BackgroundSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(104.dp)) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = tool.image), contentDescription = tool.title,
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)))))
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(8.dp).clip(CircleShape).background(tool.accentColor))
            }
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(tool.title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.W600, fontSize = 13.sp), maxLines = 1)
                Text(tool.subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp), maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Open", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.W600, fontSize = 11.sp))
                    Icon(Icons.Default.ArrowForward, null, tint = GoldPrimary, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ACTIVITY CARD — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ActivityCard(activity: Activity, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp).clickable { onClick() },
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBackground),
        border    = BorderStroke(0.5.dp, BackgroundSecondary),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp))
                    .background(activity.iconColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(activity.icon, null, tint = activity.iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.text, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.W500))
                Text(activity.subtext, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
            }
            Text(activity.timeAgo, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  BOTTOM NAVIGATION — unchanged
// ─────────────────────────────────────────────────────────────────
@Composable
fun ForgeBottomNavigation(navController: NavController) {
    NavigationBar(containerColor = CardBackground, tonalElevation = 0.dp, modifier = Modifier.shadow(12.dp)) {
        NavigationBarItem(
            icon     = { Icon(Icons.Default.Home, null) },
            label    = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            selected = true,
            onClick  = { navController.navigate(ROUT_Dashboard) { popUpTo("dashboard") { inclusive = false }; launchSingleTop = true } }
        )
        NavigationBarItem(
            icon     = { Icon(Icons.Default.Search, null) },
            label    = { Text("Discover", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick  = { navController.navigate(ROUT_DiscoveryFeed) }
        )
        NavigationBarItem(
            icon = {
                Box(Modifier.size(44.dp).clip(CircleShape).background(GoldGradient), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(22.dp))
                }
            },
            label    = { Text("Submit", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick  = { navController.navigate(ROUT_SubmitWork) }
        )
        NavigationBarItem(
            icon     = { Icon(Icons.Default.BarChart, null) },
            label    = { Text("Growth", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick  = { navController.navigate(ROUT_GrowthInsight) }
        )
        NavigationBarItem(
            icon     = { Icon(Icons.Default.Person, null) },
            label    = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick  = { navController.navigate(ROUT_Profile) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPER — unchanged
// ─────────────────────────────────────────────────────────────────
private fun DrawScope.drawDotGrid(color: Color, spacing: Float, radius: Float) {
    val cols = (size.width / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2
    for (c in 0..cols) for (r in 0..rows)
        drawCircle(color = color, radius = radius, center = Offset(c * spacing, r * spacing))
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW — unchanged
// ─────────────────────────────────────────────────────────────────
@Preview(showSystemUi = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen(rememberNavController())
}