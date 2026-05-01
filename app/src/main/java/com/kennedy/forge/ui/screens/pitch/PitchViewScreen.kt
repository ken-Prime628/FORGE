package com.kennedy.forge.ui.screens.pitch

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.ui.theme.*

///////////////////////////////////////////////////////////
// MODEL (DB READY)
///////////////////////////////////////////////////////////

data class Pitch(
    val title: String,
    val problem: String,
    val solution: String,
    val audience: String,
    val monetization: String,
    val creator: String
)

data class PitchComment(
    val user: String,
    val text: String
)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PitchViewScreen(navController: NavController) {

    ///////////////////////////////////////////////////////
    // 🔥 STATE (DYNAMIC - READY FOR DB)
    ///////////////////////////////////////////////////////
    var pitch by remember {
        mutableStateOf(
            Pitch(
                title = "Smart Study Platform",
                problem = "Students struggle to stay focused and organized.",
                solution = "A platform that tracks focus, tasks, and progress.",
                audience = "Students and learners",
                monetization = "Subscription model",
                creator = "Alex"
            )
        )
    }

    var liked by remember { mutableStateOf(false) }

    var comments by remember {
        mutableStateOf(
            listOf(
                PitchComment("Jordan", "This idea is powerful 🔥"),
                PitchComment("Sam", "I would invest in this")
            )
        )
    }

    var newComment by remember { mutableStateOf("") }

    ///////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////

    Scaffold(
        containerColor = BackgroundMain,

        /////////////////////////////////////////////
        // TOP BAR
        /////////////////////////////////////////////
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Pitch", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("pitch_live")
                    }) {
                        Icon(Icons.Default.Videocam, null, tint = GoldPrimary)
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
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(GoldPrimary, BackgroundMain)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pitch.title,
                        color = TextOnDark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            ///////////////////////////////////////////////////////
            // 👤 CREATOR
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate("public_profile")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            pitch.creator.first().toString(),
                            color = TextOnDark
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(pitch.creator, color = TextPrimary)
                }
            }

            ///////////////////////////////////////////////////////
            // 📄 PITCH DETAILS
            ///////////////////////////////////////////////////////
            item { PitchSection("Problem", pitch.problem) }
            item { PitchSection("Solution", pitch.solution) }
            item { PitchSection("Audience", pitch.audience) }
            item { PitchSection("Monetization", pitch.monetization) }

            ///////////////////////////////////////////////////////
            // 🔥 ACTIONS
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    IconButton(onClick = { liked = !liked }) {
                        Icon(
                            imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (liked) GoldPrimary else TextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            navController.navigate("collaboration")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Join Project", color = TextOnDark)
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // 💬 COMMENTS HEADER
            ///////////////////////////////////////////////////////
            item {
                Text(
                    "Comments",
                    modifier = Modifier.padding(start = 16.dp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            ///////////////////////////////////////////////////////
            // 💬 COMMENT LIST
            ///////////////////////////////////////////////////////
            items(comments) { comment ->
                CommentItem(comment)
            }

            ///////////////////////////////////////////////////////
            // ➕ ADD COMMENT
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = {
                        if (newComment.isNotBlank()) {
                            comments = comments + PitchComment("You", newComment)
                            newComment = ""
                        }
                    }) {
                        Icon(Icons.Default.Send, null, tint = GoldPrimary)
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// SECTION
///////////////////////////////////////////////////////////

@Composable
fun PitchSection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(content, color = TextSecondary)
        }
    }
}

///////////////////////////////////////////////////////////
// COMMENT ITEM
///////////////////////////////////////////////////////////

@Composable
fun CommentItem(comment: PitchComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "${comment.user}: ",
            color = GoldPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(comment.text, color = TextPrimary)
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun PreviewPitchView() {
    PitchViewScreen(rememberNavController())
}