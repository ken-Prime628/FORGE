package com.kennedy.forge.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_SubmitWork

///////////////////////////////////////////////////////////
// ✅ PROJECT MODEL (REAL DATA)
///////////////////////////////////////////////////////////
data class Project(
    val title: String,
    val image: Int
)

///////////////////////////////////////////////////////////
// MAIN PROFILE SCREEN
///////////////////////////////////////////////////////////
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    var name by remember { mutableStateOf("Kennedy") }
    var bio by remember { mutableStateOf("Creative developer building modern UI experiences.") }

    ///////////////////////////////////////////////////////
    // ✅ DYNAMIC PROJECT LIST (REAL STATE)
    ///////////////////////////////////////////////////////
    var userProjects by remember {
        mutableStateOf(
            mutableListOf<Project>()
        )
    }

    ///////////////////////////////////////////////////////
    // DEMO: ADD PROJECT BUTTON (simulate submit screen)
    ///////////////////////////////////////////////////////
    fun addProject() {
        userProjects = (userProjects + Project(
            title = "New Project ${userProjects.size + 1}",
            image = R.drawable.hero_bg
        )).toMutableList()
    }

    val avgProjects = userProjects.size

    Scaffold(
        containerColor = BackgroundMain,

        /////////////////////////////////////////////
        // TOP BAR
        /////////////////////////////////////////////
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUT_Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUT_SubmitWork) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Project", tint = GoldPrimary)
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
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            /////////////////////////////////////////////
            // PROFILE HEADER
            /////////////////////////////////////////////
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(GoldPrimary, BackgroundMain)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(CardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(bio, color = TextSecondary)
                    }
                }
            }

            /////////////////////////////////////////////
            // EDIT PROFILE
            /////////////////////////////////////////////
            item {
                SectionCard("Edit Profile") {

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            /////////////////////////////////////////////
            // STATS
            /////////////////////////////////////////////
            item {
                SectionCard("Stats") {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem("Projects", avgProjects.toString())
                        StatItem("Likes", "0")
                        StatItem("Reviews", "0")
                    }
                }
            }

            /////////////////////////////////////////////
            // PROJECT HEADER
            /////////////////////////////////////////////
            item {
                Text(
                    "Your Projects",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            /////////////////////////////////////////////
            // PROJECT LIST
            /////////////////////////////////////////////
            if (userProjects.isEmpty()) {
                item {
                    Text(
                        "No projects yet. Tap + to add one.",
                        modifier = Modifier.padding(16.dp),
                        color = TextSecondary
                    )
                }
            } else {
                items(userProjects) { project ->
                    ProjectCard(project)
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PROJECT CARD
///////////////////////////////////////////////////////////
@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {

        Column {

            Image(
                painter = painterResource(id = project.image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Text(
                project.title,
                modifier = Modifier.padding(12.dp),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

///////////////////////////////////////////////////////////
// STATS ITEM
///////////////////////////////////////////////////////////
@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = GoldPrimary, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary)
    }
}

///////////////////////////////////////////////////////////
// SECTION CARD
///////////////////////////////////////////////////////////
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}