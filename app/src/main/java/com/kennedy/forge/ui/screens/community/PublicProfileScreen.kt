package com.kennedy.forge.ui.screens.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*

///////////////////////////////////////////////////////////
// 🔥 MODELS (DB READY)
///////////////////////////////////////////////////////////

data class PublicUser(
    val id: String,
    val name: String,
    val bio: String,
    val followers: Int,
    val following: Int,
    val isFollowing: Boolean
)

data class PublicProject(
    val id: String,
    val title: String,
    val image: Int
)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    navController: NavController,
    userId: String = "user_1" // 🔥 will come from navigation later
) {

    ///////////////////////////////////////////////////////
    // 🔥 STATE (SIMULATES BACKEND)
    ///////////////////////////////////////////////////////
    var user by remember {
        mutableStateOf(
            PublicUser(
                id = userId,
                name = "Alex Johnson",
                bio = "UI/UX Designer crafting modern experiences",
                followers = 340,
                following = 180,
                isFollowing = false
            )
        )
    }

    var projects by remember {
        mutableStateOf(
            listOf(
                PublicProject("1", "Modern UI", R.drawable.hero_bg),
                PublicProject("2", "Creative App", R.drawable.hero_bg),
                PublicProject("3", "Dashboard UX", R.drawable.hero_bg)
            )
        )
    }

    ///////////////////////////////////////////////////////
    // 🔥 ACTIONS (REALISTIC LOGIC)
    ///////////////////////////////////////////////////////
    fun toggleFollow() {
        user = user.copy(
            isFollowing = !user.isFollowing,
            followers = if (user.isFollowing)
                user.followers - 1
            else
                user.followers + 1
        )
    }

    ///////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////

    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // 🔥 Navigate to chat screen later
                        navController.navigate("chat/${user.id}")
                    }) {
                        Icon(Icons.Default.Message, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            ///////////////////////////////////////////////////////
            // 🔥 HERO HEADER
            ///////////////////////////////////////////////////////
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(GoldPrimary, BackgroundMain)
                            )
                        )
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(CardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold)

                        Text(user.bio, color = TextSecondary)

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { toggleFollow() },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isFollowing) CardBackground else GoldPrimary
                            )
                        ) {
                            Text(
                                if (user.isFollowing) "Following" else "Follow",
                                color = if (user.isFollowing) TextPrimary else TextOnDark
                            )
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // 🔥 STATS
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem("Projects", projects.size.toString())
                    StatItem("Followers", user.followers.toString())
                    StatItem("Following", user.following.toString())
                }
            }

            ///////////////////////////////////////////////////////
            // 🔥 PROJECT HEADER
            ///////////////////////////////////////////////////////
            item {
                Text(
                    "Projects",
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            ///////////////////////////////////////////////////////
            // 🔥 PROJECT LIST (DYNAMIC)
            ///////////////////////////////////////////////////////
            items(projects, key = { it.id }) { project ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate("project_detail/${project.id}")
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {

                    Column {

                        Image(
                            painter = painterResource(id = project.image),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentScale = ContentScale.Crop
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                project.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Icon(Icons.Default.ArrowForward, null, tint = GoldPrimary)
                        }
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// 🔥 STAT ITEM
///////////////////////////////////////////////////////////
@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = GoldPrimary, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary)
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun PreviewPublicProfile() {
    PublicProfileScreen(rememberNavController())
}