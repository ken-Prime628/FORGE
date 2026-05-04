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
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.navigation.ROUT_ProfileSetup
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
    val timeAgo: String
)

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────
@Composable
fun DashboardScreen(navController: NavController) {

    var user by remember {
        mutableStateOf(User(name = "Kennedy", avatarUri = null))
    }

    val tools = listOf(
        Tool("FeedbackLoop",    "Get honest reviews",    R.drawable.tool_feedback,  "submit_work",          Color(0xFF185FA5)),
        Tool("BlockBreaker",    "Diagnose your block",   R.drawable.tool_blocks,    "block_diagnosis",      Color(0xFF993C1D)),
        Tool("PortfolioTruth",  "Optimise your work",    R.drawable.tool_portfolio, "portfolio_builder",    Color(0xFF0F6E56)),
        Tool("PitchMirror",     "Practise your pitch",   R.drawable.tool_pitch,     "scenario_selector",    Color(0xFF854F0B)),
    )

    val activities = listOf(
        Activity("Feedback received",        "Brand identity — NovaCo",   Icons.Default.RateReview,  Color(0xFF185FA5), "2m ago"),
        Activity("Block diagnosed",          "Perfectionism block",        Icons.Default.Psychology,  Color(0xFF993C1D), "1h ago"),
        Activity("Portfolio view spike",     "142 views today",            Icons.Default.BarChart,    Color(0xFF0F6E56), "3h ago"),
        Activity("Pitch score improved",     "+8 points this session",     Icons.Default.Mic,         Color(0xFF854F0B), "Yesterday"),
        Activity("Source flagged",           "2 restricted references",    Icons.Default.Folder,      Color(0xFF3B6D11), "2d ago"),
    )

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            DashboardTopBar(
                user          = user,
                onAvatarChange = { uri -> user = user.copy(avatarUri = uri) },
                onSearch      = { navController.navigate("global_search") },
                onNotifications = { navController.navigate("notifications") }
            )
        },
        bottomBar = { ForgeBottomNavigation(navController) }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Hero greeting ──────────────────────────────────────
            item { HeroGreetingCard(user = user) }

            // ── Stats strip ────────────────────────────────────────
            item { StatsStrip() }

            // ── Pro / exposure banner ──────────────────────────────
            item { ProExposureBanner(navController) }

            // ── Submit work CTA ────────────────────────────────────
            item { SubmitWorkCTA(navController) }

            // ── Creative tools ─────────────────────────────────────
            item {
                DashSectionHeader(
                    title  = "Creative tools",
                    action = "See all",
                    onAction = {}
                )
            }
            item { CreativeToolsRow(tools = tools, navController = navController) }

            // ── Recent activity ────────────────────────────────────
            item {
                DashSectionHeader(
                    title  = "Recent activity",
                    action = "View all",
                    onAction = { navController.navigate("notifications") }
                )
            }
            items(activities) { activity ->
                ActivityCard(activity)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TOP BAR
// ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    user: User,
    onAvatarChange: (String) -> Unit,
    onSearch: () -> Unit,
    onNotifications: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(GoldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "F",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color      = DarkSurface,
                            fontWeight = FontWeight.W700
                        )
                    )
                }
                Text(
                    "Forge",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight    = FontWeight.W600,
                        color         = TextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                )
            }
        },
        navigationIcon = {
            Spacer(Modifier.width(8.dp))
            UserAvatar(user = user, onAvatarChange = onAvatarChange)
        },
        actions = {
            IconButton(onClick = onSearch) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BackgroundSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Search, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                }
            }
            IconButton(onClick = onNotifications) {
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BackgroundSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    // Notification dot
                    Box(Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Notifications, null,
                            tint     = TextPrimary,
                            modifier = Modifier.size(18.dp).align(Alignment.Center)
                        )
                        Box(
                            Modifier
                                .size(7.dp)
                                .align(Alignment.TopEnd)
                                .offset((-4).dp, 4.dp)
                                .clip(CircleShape)
                                .background(Error)
                                .border(1.5.dp, BackgroundSecondary, CircleShape)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = BackgroundMain
        )
    )
}

// ─────────────────────────────────────────────────────────────────
//  USER AVATAR
// ─────────────────────────────────────────────────────────────────
@Composable
fun UserAvatar(user: User, onAvatarChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(GoldGradient)
            .clickable { onAvatarChange("https://picsum.photos/200") },
        contentAlignment = Alignment.Center
    ) {
        if (user.avatarUri != null) {
            AsyncImage(
                model              = user.avatarUri,
                contentDescription = "Profile",
                modifier           = Modifier.fillMaxSize(),
                contentScale       = ContentScale.Crop
            )
        } else {
            Text(
                user.name.first().toString(),
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = DarkSurface,
                    fontWeight = FontWeight.W700
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HERO GREETING CARD — uses your hero_bg drawable
// ─────────────────────────────────────────────────────────────────
@Composable
private fun HeroGreetingCard(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .height(186.dp)
            .clip(RoundedCornerShape(28.dp))
            .shadow(8.dp, RoundedCornerShape(28.dp))
    ) {
        // Your existing hero_bg image
        androidx.compose.foundation.Image(
            painter            = painterResource(id = R.drawable.hero_bg),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop
        )

        // Dark scrim so text is always readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        // Decorative gold glow
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
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Streak badge
            Surface(
                shape  = RoundedCornerShape(20.dp),
                color  = GoldPrimary.copy(alpha = 0.20f),
                border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        Icons.Default.Whatshot, null,
                        tint     = GoldAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "12 day streak",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color         = GoldAccent,
                            fontWeight    = FontWeight.W500,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }

            // Greeting text
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    "Good morning,",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.65f)
                    )
                )
                Text(
                    "${user.name}.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.W700,
                        letterSpacing = (-0.3).sp
                    )
                )
                Text(
                    "You're forging greatness today.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  STATS STRIP
// ─────────────────────────────────────────────────────────────────
@Composable
private fun StatsStrip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem("3",    "Pending\nreviews", GoldAccent)
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.White.copy(alpha = 0.08f))
        StatItem("142",  "Portfolio\nviews",  Color.White)
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.White.copy(alpha = 0.08f))
        StatItem("81",   "Pitch\nscore",      SoftGreen)
        VerticalDivider(modifier = Modifier.height(36.dp), color = Color.White.copy(alpha = 0.08f))
        StatItem("7",    "Block-free\ndays",  SoftBlue)
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.W700,
                color      = color
            )
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color      = Color.White.copy(alpha = 0.45f),
                letterSpacing = 0.2.sp,
                lineHeight = 14.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  PRO EXPOSURE BANNER
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ProExposureBanner(navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(0.5.dp, GoldPrimary.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gold star badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star, null,
                    tint     = DarkSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Pro member",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color      = TextPrimary,
                            fontWeight = FontWeight.W600
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "ACTIVE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color         = GoldPrimary,
                                fontWeight    = FontWeight.W700,
                                fontSize      = 9.sp,
                                letterSpacing = 0.6.sp
                            )
                        )
                    }
                }
                Text(
                    "You were spotted 12× this week — upgrade to Elite for client matching",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = TextSecondary,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldGradient)
                    .clickable { navController.navigate("subscription") }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Upgrade",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color      = DarkSurface,
                        fontWeight = FontWeight.W700
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SUBMIT WORK CTA
// ─────────────────────────────────────────────────────────────────
@Composable
fun SubmitWorkCTA(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(
                0.5.dp,
                Brush.linearGradient(listOf(GoldAccent.copy(alpha = 0.4f), GoldPrimary.copy(alpha = 0.2f))),
                RoundedCornerShape(16.dp)
            )
            .clickable { navController.navigate(ROUT_SubmitWork) },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(GoldGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = DarkSurface, modifier = Modifier.size(14.dp))
            }
            Text(
                "Submit new work for review",
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = Color.White,
                    fontWeight = FontWeight.W500
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SECTION HEADER
// ─────────────────────────────────────────────────────────────────
@Composable
fun DashSectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(GoldGradient, RoundedCornerShape(2.dp))
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color      = TextPrimary,
                    fontWeight = FontWeight.W600
                )
            )
        }
        Text(
            action,
            style = MaterialTheme.typography.labelMedium.copy(
                color      = GoldPrimary,
                fontWeight = FontWeight.W500
            ),
            modifier = Modifier.clickable(onClick = onAction)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  CREATIVE TOOLS ROW — uses your four tool drawables
// ─────────────────────────────────────────────────────────────────
@Composable
fun CreativeToolsRow(tools: List<Tool>, navController: NavController) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tools) { tool ->
            ToolCard(tool = tool, onClick = { navController.navigate(tool.route) })
        }
    }
}

@Composable
private fun ToolCard(tool: Tool, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(152.dp)
            .height(178.dp)
            .clickable(onClick = onClick),
        shape  = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(0.5.dp, BackgroundSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Image area — your existing drawables
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter            = painterResource(id = tool.image),
                    contentDescription = tool.title,
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
                // Dark gradient for readability
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                            )
                        )
                )
                // Accent dot top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tool.accentColor)
                )
            }

            // Text area
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    tool.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.W600,
                        fontSize   = 13.sp
                    ),
                    maxLines = 1
                )
                Text(
                    tool.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color    = TextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        "Open",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color      = GoldPrimary,
                            fontWeight = FontWeight.W600,
                            fontSize   = 11.sp
                        )
                    )
                    Icon(
                        Icons.Default.ArrowForward, null,
                        tint     = GoldPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  ACTIVITY CARD — uses Icons instead of images (no extra drawable needed)
// ─────────────────────────────────────────────────────────────────
@Composable
private fun ActivityCard(activity: Activity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape  = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(0.5.dp, BackgroundSecondary),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(activity.iconColor.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    activity.icon, null,
                    tint     = activity.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.text,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color      = TextPrimary,
                        fontWeight = FontWeight.W500
                    )
                )
                Text(
                    activity.subtext,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Text(
                activity.timeAgo,
                style = MaterialTheme.typography.labelSmall.copy(
                    color    = TextSecondary,
                    fontSize = 10.sp
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  BOTTOM NAVIGATION
// ─────────────────────────────────────────────────────────────────
@Composable
fun ForgeBottomNavigation(navController: NavController) {
    NavigationBar(
        containerColor = CardBackground,
        tonalElevation = 0.dp,
        modifier       = Modifier.shadow(12.dp)
    ) {
        NavigationBarItem(
            icon     = { Icon(Icons.Default.Home, null) },
            label    = { Text("Home", style = MaterialTheme.typography.labelSmall) },
            selected = true,
            onClick  = {}
        )
        NavigationBarItem(
            icon     = { Icon(Icons.Default.Search, null) },
            label    = { Text("Discover", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick  = { navController.navigate("discovery_feed") }
        )
        // Gold centre add button
        NavigationBarItem(
            icon = {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add, null,
                        tint     = DarkSurface,
                        modifier = Modifier.size(22.dp)
                    )
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
            onClick  = { navController.navigate("growth_insights") }
        )
        NavigationBarItem(
            icon     = { Icon(Icons.Default.Person, null) },
            label    = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
            selected = false,
            onClick  = { navController.navigate(ROUT_ProfileSetup) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  CANVAS HELPER
// ─────────────────────────────────────────────────────────────────
private fun DrawScope.drawDotGrid(color: Color, spacing: Float, radius: Float) {
    val cols = (size.width  / spacing).toInt() + 2
    val rows = (size.height / spacing).toInt() + 2
    for (c in 0..cols) for (r in 0..rows)
        drawCircle(color = color, radius = radius, center = Offset(c * spacing, r * spacing))
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────
@Preview(showSystemUi = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen(rememberNavController())
}