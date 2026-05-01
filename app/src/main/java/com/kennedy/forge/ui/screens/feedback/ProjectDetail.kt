package com.kennedy.forge.ui.screens.feedback

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

///////////////////////////////////////////////////////////
// ✅ MODEL (ONLY ONE)
///////////////////////////////////////////////////////////
data class Feedback(
    val reviewer: String,
    val comment: String,
    val rating: Int
)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(navController: NavController) {

    var description by remember {
        mutableStateOf("This project explores a modern UI concept for creative platforms.")
    }

    var feedbackList by remember {
        mutableStateOf(
            listOf(
                FeedbackItem("Alex", "Improve spacing in sections.", 4),
                FeedbackItem("Jordan", "Amazing visuals!", 5)
            )
        )
    }

    var newComment by remember { mutableStateOf("") }
    var newRating by remember { mutableStateOf(0) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    val avgRating = if (feedbackList.isNotEmpty())
        feedbackList.map { it.rating }.average()
    else 0.0

    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Project Details", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },

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
                    onClick = { navController.navigate("submit_work") }
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
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            /////////////////////////////////////////////
            // HERO IMAGE
            /////////////////////////////////////////////
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.hero_bg),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        androidx.compose.ui.graphics.Color.Transparent,
                                        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    Text(
                        "Your Project",
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            /////////////////////////////////////////////
            // DESCRIPTION
            /////////////////////////////////////////////
            item {
                SectionCard("About Project") {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            /////////////////////////////////////////////
            // RATING SUMMARY
            /////////////////////////////////////////////
            item {
                SectionCard("Overall Rating") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            String.format("%.1f", avgRating),
                            color = GoldPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        repeat(avgRating.toInt()) {
                            Icon(Icons.Default.Star, null, tint = GoldAccent)
                        }
                    }
                }
            }

            /////////////////////////////////////////////
            // CREATE / UPDATE FEEDBACK
            /////////////////////////////////////////////
            item {
                SectionCard(if (editingIndex == null) "Leave Feedback" else "Edit Feedback") {

                    Row {
                        for (i in 1..5) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = if (i <= newRating) GoldAccent else androidx.compose.ui.graphics.Color.Gray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { newRating = i }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newComment,
                        onValueChange = { newComment = it },
                        placeholder = { Text("Write feedback...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newComment.isNotBlank() && newRating > 0) {

                                if (editingIndex == null) {
                                    feedbackList = feedbackList + FeedbackItem("You", newComment, newRating)
                                } else {
                                    val updated = feedbackList.toMutableList()
                                    updated[editingIndex!!] = FeedbackItem("You", newComment, newRating)
                                    feedbackList = updated
                                    editingIndex = null
                                }

                                newComment = ""
                                newRating = 0
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text(if (editingIndex == null) "Submit Feedback" else "Update Feedback")
                    }
                }
            }

            /////////////////////////////////////////////
            // FEEDBACK LIST
            /////////////////////////////////////////////
            itemsIndexed(feedbackList) { index, item ->
                FeedbackCard(
                    item = item,
                    onDelete = {
                        feedbackList = feedbackList.toMutableList().also { it.removeAt(index) }
                    },
                    onEdit = {
                        newComment = item.comment
                        newRating = item.rating
                        editingIndex = index
                    }
                )
            }
        }
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
// FEEDBACK CARD (CRUD ACTIONS)
///////////////////////////////////////////////////////////
@Composable
fun FeedbackCard(
    item: FeedbackItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.reviewer.firstOrNull()?.toString() ?: "?",
                            color = TextOnDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(item.reviewer, color = TextPrimary)
                        Row {
                            repeat(item.rating) {
                                Icon(Icons.Default.Star, null, tint = GoldAccent)
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(item.comment, color = TextSecondary)
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun PreviewScreen() {
    ProjectDetailScreen(rememberNavController())
}