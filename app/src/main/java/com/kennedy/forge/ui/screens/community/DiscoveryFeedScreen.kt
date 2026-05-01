package com.kennedy.forge.ui.screens.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.navigation.ROUT_SubmitWork

///////////////////////////////////////////////////////////
// ✅ MODEL
///////////////////////////////////////////////////////////
data class FeedProject(
    val id: Int,
    val title: String,
    val creator: String,
    val image: Int,
    val rating: Int,
    val liked: Boolean = false
)

///////////////////////////////////////////////////////////
// ✅ FAKE REPOSITORY (SIMULATES REAL BACKEND)
///////////////////////////////////////////////////////////
object ProjectRepository {

    private val _projects = mutableStateListOf(
        FeedProject(1, "Modern UI Design", "Alex", R.drawable.img, 5),
        FeedProject(2, "Mobile App Concept", "Jordan", R.drawable.img_1, 4)
    )

    val projects: List<FeedProject> get() = _projects

    fun addProject(project: FeedProject) {
        _projects.add(project)
    }

    fun toggleLike(id: Int) {
        val index = _projects.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = _projects[index]
            _projects[index] = item.copy(liked = !item.liked)
        }
    }

    fun deleteProject(id: Int) {
        _projects.removeAll { it.id == id }
    }
}

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryFeedScreen(navController: NavController) {

    val projects = ProjectRepository.projects

    fun addProject() {
        val newId = (projects.maxOfOrNull { it.id } ?: 0) + 1
        ProjectRepository.addProject(
            FeedProject(
                id = newId,
                title = "New Project $newId",
                creator = "You",
                image = R.drawable.hero_bg,
                rating = (3..5).random()
            )
        )
    }

    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Discover", color = TextPrimary) },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUT_SubmitWork) }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
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

            if (projects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No projects yet", color = TextSecondary)
                    }
                }
            }

            items(projects, key = { it.id }) { project ->

                FeedCard(
                    project = project,

                    onLike = {
                        ProjectRepository.toggleLike(project.id)
                    },

                    onProjectClick = {
                        navController.navigate("project_detail")
                    },

                    onUserClick = {
                        navController.navigate("profile")
                    },

                    onDelete = {
                        ProjectRepository.deleteProject(project.id)
                    }
                )
            }
        }
    }
}

///////////////////////////////////////////////////////////
// FEED CARD
///////////////////////////////////////////////////////////
@Composable
fun FeedCard(
    project: FeedProject,
    onLike: () -> Unit,
    onProjectClick: () -> Unit,
    onUserClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {

        Column {

            Image(
                painter = painterResource(id = project.image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onProjectClick() },
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        project.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = TextSecondary)
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onUserClick() }
                ) {

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            project.creator.firstOrNull()?.toString() ?: "?",
                            color = TextOnDark
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(project.creator, color = TextSecondary)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row {
                        repeat(project.rating) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = if (project.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (project.liked) GoldPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun PreviewDiscovery() {
    DiscoveryFeedScreen(rememberNavController())
}