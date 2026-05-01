package com.kennedy.forge.ui.screens.feedback

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

///////////////////////////////////////////////////////////
// DATA
///////////////////////////////////////////////////////////

data class FeedbackItem(
    val reviewer: String,
    val comment: String,
    val rating: Int
)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDashboardScreen(navController: NavController) {

    var feedbackList by remember {
        mutableStateOf(
            listOf(
                FeedbackItem("Alex", "Great concept, improve spacing.", 4),
                FeedbackItem("Jordan", "Very strong visuals!", 5)
            )
        )
    }

    // 🔥 Project description (instead of self-feedback)
    var projectDescription by remember {
        mutableStateOf("Describe your work here...")
    }

    Scaffold(
        containerColor = BackgroundMain,

        ///////////////////////////////////////////////////////
        // TOP BAR
        ///////////////////////////////////////////////////////
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Feedback Dashboard", color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        },

        ///////////////////////////////////////////////////////
        // BOTTOM NAVIGATION
        ///////////////////////////////////////////////////////
        bottomBar = {
            NavigationBar(containerColor = CardBackground) {

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = false,
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
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
                    onClick = {}
                )
            }
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            ///////////////////////////////////////////////////////
            // 🔥 PROJECT IMAGE (RESTORED)
            ///////////////////////////////////////////////////////
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_bg), // replace later with uploaded image
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            ///////////////////////////////////////////////////////
            // 🔥 PROJECT DESCRIPTION (NEW PURPOSE)
            ///////////////////////////////////////////////////////
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            "About Your Work",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = projectDescription,
                            onValueChange = { projectDescription = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Explain your idea, goals, or concept...")
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // FEEDBACK HEADER
            ///////////////////////////////////////////////////////
            item {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Community Feedback",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            ///////////////////////////////////////////////////////
            // FEEDBACK LIST (OTHERS REVIEW YOU)
            ///////////////////////////////////////////////////////
            items(feedbackList) { feedback ->
                FeedbackCard(feedback)
            }
        }
    }
}

///////////////////////////////////////////////////////////
// FEEDBACK CARD
///////////////////////////////////////////////////////////

@Composable
fun FeedbackCard(item: FeedbackItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.reviewer.first().toString(),
                        color = TextOnDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(item.reviewer, color = TextPrimary)

                    Row {
                        repeat(item.rating) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(item.comment, color = TextSecondary)
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun FeedbackDashboardScreenPreview(){
    FeedbackDashboardScreen(rememberNavController())
}