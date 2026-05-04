package com.kennedy.forge.ui.screens.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(navController: NavController) {

    var isPrivateAccount by remember { mutableStateOf(false) }
    var showActivityStatus by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Privacy & Security", color = TextPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            ///////////////////////////////////////////////////////
            // 🔐 PROFILE VISIBILITY CARD
            ///////////////////////////////////////////////////////
            PrivacyCard {
                PrivacySectionTitle("Profile Visibility")

                PrivacyToggleItem(
                    title = "Private Account",
                    subtitle = if (isPrivateAccount)
                        "Only approved users can see your work"
                    else
                        "Anyone can view your profile and projects",
                    icon = Icons.Default.VisibilityOff,
                    checked = isPrivateAccount,
                    onCheckedChange = { isPrivateAccount = it }
                )
            }

            ///////////////////////////////////////////////////////
            // 💬 MESSAGING CARD
            ///////////////////////////////////////////////////////
            PrivacyCard {
                PrivacySectionTitle("Messaging")

                PrivacySettingItem(
                    title = "Who can message you",
                    icon = Icons.Default.Message,
                    onClick = {
                        navController.navigate("message_privacy")
                    }
                )
            }

            ///////////////////////////////////////////////////////
            // 🟢 ACTIVITY CARD
            ///////////////////////////////////////////////////////
            PrivacyCard {
                PrivacySectionTitle("Activity")

                PrivacyToggleItem(
                    title = "Show Activity Status",
                    subtitle = "Let others see when you're online",
                    icon = Icons.Default.Circle,
                    checked = showActivityStatus,
                    onCheckedChange = { showActivityStatus = it }
                )
            }

            ///////////////////////////////////////////////////////
            // 🚫 SAFETY CARD
            ///////////////////////////////////////////////////////
            PrivacyCard {
                PrivacySectionTitle("Safety")

                PrivacySettingItem(
                    title = "Blocked Users",
                    icon = Icons.Default.Block,
                    onClick = {
                        navController.navigate("blocked_users")
                    }
                )

                PrivacySettingItem(
                    title = "Security",
                    icon = Icons.Default.Lock,
                    onClick = {
                        navController.navigate("security")
                    }
                )
            }

            ///////////////////////////////////////////////////////
            // 🧠 FIRST TIME HINT
            ///////////////////////////////////////////////////////
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "You’re in control. Adjust these settings anytime.",
                modifier = Modifier.padding(16.dp),
                color = TextSecondary
            )
        }
    }
}

///////////////////////////////////////////////////////////
// 🔥 PREMIUM CARD WRAPPER
///////////////////////////////////////////////////////////
@Composable
fun PrivacyCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

///////////////////////////////////////////////////////////
// SECTION TITLE
///////////////////////////////////////////////////////////
@Composable
fun PrivacySectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(12.dp),
        color = GoldPrimary,
        fontWeight = FontWeight.Bold
    )
}

///////////////////////////////////////////////////////////
// TOGGLE ITEM
///////////////////////////////////////////////////////////
@Composable
fun PrivacyToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(icon, null, tint = GoldPrimary)

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GoldPrimary,
                checkedTrackColor = GoldPrimary.copy(alpha = 0.4f)
            )
        )
    }
}

///////////////////////////////////////////////////////////
// NORMAL ITEM
///////////////////////////////////////////////////////////
@Composable
fun PrivacySettingItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(icon, null, tint = GoldPrimary)

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            modifier = Modifier.weight(1f),
            color = TextPrimary
        )

        Icon(Icons.Default.ArrowForwardIos, null, tint = TextSecondary)
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun PrivacyScreenPreview() {
    PrivacyScreen(rememberNavController())
}