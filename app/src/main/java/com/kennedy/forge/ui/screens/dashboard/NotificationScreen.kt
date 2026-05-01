package com.kennedy.forge.ui.screens.dashboard

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
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.ui.theme.*

///////////////////////////////////////////////////////////
// 🔥 MODEL (DB READY)
///////////////////////////////////////////////////////////

data class AppNotification(
    val id: String,
    val userName: String,
    val message: String,
    val type: String, // like, follow, comment
    val time: String,
    var isRead: Boolean = false
)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {

    ///////////////////////////////////////////////////////
    // 🔥 STATE (Replace later with DB)
    ///////////////////////////////////////////////////////
    var notifications by remember {
        mutableStateOf(
            listOf<AppNotification>() // 🔥 EMPTY for first-time user
        )
    }

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
                title = { Text("Notifications", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUT_Dashboard) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // simulate incoming notifications
                        notifications = listOf(
                            AppNotification("1", "Alex", "liked your project", "like", "2m"),
                            AppNotification("2", "Jordan", "started following you", "follow", "10m"),
                            AppNotification("3", "Sam", "commented on your project", "comment", "1h")
                        )
                    }) {
                        Icon(Icons.Default.Refresh, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        }
    ) { padding ->

        ///////////////////////////////////////////////////////
        // 🔥 EMPTY STATE (FIRST TIME USER)
        ///////////////////////////////////////////////////////
        if (notifications.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(GoldPrimary, CardBackground)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, null, tint = TextOnDark)
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "No Notifications Yet",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "When people interact with your work,\nyou’ll see it here.",
                        color = TextSecondary
                    )
                }
            }

        } else {

            ///////////////////////////////////////////////////////
            // 🔥 NOTIFICATION LIST
            ///////////////////////////////////////////////////////
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {

                items(notifications, key = { it.id }) { notification ->

                    NotificationCard(
                        notification = notification,

                        onClick = {
                            // 🔥 Navigate based on type
                            when (notification.type) {
                                "follow" -> navController.navigate("public_profile")
                                else -> navController.navigate("project_detail")
                            }
                        },

                        onDelete = {
                            notifications =
                                notifications.filter { it.id != notification.id }
                        },

                        onMarkRead = {
                            notifications = notifications.map {
                                if (it.id == notification.id)
                                    it.copy(isRead = true)
                                else it
                            }
                        }
                    )
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// 🔥 NOTIFICATION CARD
///////////////////////////////////////////////////////////

@Composable
fun NotificationCard(
    notification: AppNotification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkRead: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                onClick()
                onMarkRead()
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                CardBackground
            else GoldPrimary.copy(alpha = 0.15f)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            ///////////////////////////////////////////////////////
            // AVATAR
            ///////////////////////////////////////////////////////
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    notification.userName.first().toString(),
                    color = TextOnDark,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            ///////////////////////////////////////////////////////
            // TEXT CONTENT
            ///////////////////////////////////////////////////////
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    "${notification.userName} ${notification.message}",
                    color = TextPrimary,
                    fontWeight = if (notification.isRead)
                        FontWeight.Normal
                    else FontWeight.SemiBold
                )

                Text(
                    notification.time,
                    color = TextSecondary
                )
            }

            ///////////////////////////////////////////////////////
            // DELETE BUTTON
            ///////////////////////////////////////////////////////
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Close, null, tint = TextSecondary)
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun NotificationPreview() {
    NotificationScreen(rememberNavController())
}