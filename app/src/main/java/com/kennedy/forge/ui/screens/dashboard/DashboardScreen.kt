package com.kennedy.forge.ui.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.navigation.ROUT_ProfileSetup
import com.kennedy.forge.navigation.ROUT_SubmitWork

///////////////////////////////////////////////////////////
// DATA MODELS
///////////////////////////////////////////////////////////

data class User(
    val name: String,
    val avatarUri: String? = null
)

data class Tool(
    val title: String,
    val image: Int
)

data class Activity(val text: String)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@Composable
fun DashboardScreen(navController: NavController) {

    var user by remember {
        mutableStateOf(
            User(
                name = "Kennedy",
                avatarUri = null
            )
        )
    }

    val tools = listOf(
        Tool("Feedback", R.drawable.tool_feedback),
        Tool("Blocks", R.drawable.tool_blocks),
        Tool("Portfolio", R.drawable.tool_portfolio),
        Tool("Pitch", R.drawable.tool_pitch)
    )

    val activities = listOf(
        Activity("Uploaded a new project"),
        Activity("Received feedback"),
        Activity("Completed a challenge")
    )

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            ForgeTopBar(
                user = user,
                onAvatarChange = { uri ->
                    user = user.copy(avatarUri = uri)
                }
            )
        },
        bottomBar = { ForgeBottomNavigation(navController) }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {

            item { GreetingSection() }
            item { ProBanner() }

            // ✅ NEW CTA BUTTON


            item { SectionHeader("Creative Tools", "Explore") }
            item { CreativeToolsRow(tools) }

            item { SectionHeader("Recent Activity", "View All") }
            items(activities) {
                ActivityItem(it)
            }
        }
    }
}

///////////////////////////////////////////////////////////
// TOP BAR
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgeTopBar(
    user: User,
    onAvatarChange: (String) -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Forge",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Search, null, tint = TextPrimary)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, null, tint = TextPrimary)
            }

            UserAvatar(
                user = user,
                onAvatarChange = onAvatarChange
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = BackgroundMain
        )
    )
}

///////////////////////////////////////////////////////////
// USER AVATAR (DYNAMIC)
///////////////////////////////////////////////////////////

@Composable
fun UserAvatar(
    user: User,
    onAvatarChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable {
                onAvatarChange("https://picsum.photos/200")
            }
    ) {

        if (user.avatarUri != null) {
            AsyncImage(
                model = user.avatarUri,
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.first().toString(),
                    color = TextOnDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

///////////////////////////////////////////////////////////
// HERO SECTION
///////////////////////////////////////////////////////////

@Composable
fun GreetingSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box {

            Image(
                painter = painterResource(id = R.drawable.hero_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Text("Welcome back 👋", color = Color.White)

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "You're forging greatness today.",
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Whatshot, null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("12 Day Streak", color = GoldAccent)
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PRO BANNER
///////////////////////////////////////////////////////////

@Composable
fun ProBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(Icons.Default.Star, null, tint = GoldPrimary)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("PRO Member", color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Unlock full creative power", color = TextSecondary)
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("View", color = TextOnDark)
            }
        }
    }
}

///////////////////////////////////////////////////////////
// CTA BUTTON
///////////////////////////////////////////////////////////

@Composable
fun SubmitWorkCTA(navController: NavController) {
    Button(
        onClick = {
            navController.navigate(ROUT_SubmitWork)

        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = TextOnDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Submit New Work")
    }
}

///////////////////////////////////////////////////////////
// TOOLS
///////////////////////////////////////////////////////////

@Composable
fun CreativeToolsRow(tools: List<Tool>) {
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tools) {
            ToolCard(it)
        }
    }
}

@Composable
fun ToolCard(tool: Tool) {
    Card(
        modifier = Modifier.size(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column {

            Image(
                painter = painterResource(id = tool.image),
                contentDescription = tool.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(tool.title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("Explore", color = TextSecondary)
            }
        }
    }
}

///////////////////////////////////////////////////////////
// ACTIVITY
///////////////////////////////////////////////////////////

@Composable
fun ActivityItem(activity: Activity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.tool_feedback),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(activity.text, color = TextPrimary)
        }
    }
}

///////////////////////////////////////////////////////////
// SECTION HEADER
///////////////////////////////////////////////////////////

@Composable
fun SectionHeader(title: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(action, color = GoldPrimary)
    }
}

///////////////////////////////////////////////////////////
// BOTTOM NAVIGATION
///////////////////////////////////////////////////////////

@Composable
fun ForgeBottomNavigation(navController: NavController) {
    NavigationBar(containerColor = CardBackground) {

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, null) },
            selected = true,
            onClick = {}
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, null) },
            selected = false,
            onClick = {}
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, null, tint = GoldPrimary) },
            selected = false,
            onClick = {
                navController.navigate("submit_work")
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, null) },
            selected = false,
            onClick = {navController.navigate(ROUT_ProfileSetup)}
        )
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun PreviewDashboard() {
    DashboardScreen(rememberNavController())
}