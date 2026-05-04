package com.kennedy.forge.ui.screens.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {

    ///////////////////////////////////////////////////////
    // 🔥 FIRST-TIME USER STATE (DB READY)
    ///////////////////////////////////////////////////////
    var hasProfile by remember { mutableStateOf(false) } // will come from DB
    var userName by remember { mutableStateOf("") }

    var notificationsEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    ///////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////
    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
            // 👤 PROFILE SECTION (REALISTIC FIRST TIME)
            ///////////////////////////////////////////////////////
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (hasProfile) {
                            navController.navigate("public_profile")
                        } else {
                            navController.navigate("edit_profile") // onboarding
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (hasProfile) userName.first().toString() else "?",
                        color = TextOnDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {

                    if (hasProfile) {
                        Text(userName, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("View Profile", color = TextSecondary)
                    } else {
                        Text("Set up your profile", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Add name, photo and bio", color = TextSecondary)
                    }
                }

                Icon(Icons.Default.ArrowForwardIos, null, tint = TextSecondary)
            }

            Divider(color = CardBackground)

            ///////////////////////////////////////////////////////
            // 🔔 NOTIFICATIONS
            ///////////////////////////////////////////////////////
            SettingToggleItem(
                title = "Notifications",
                subtitle = "Get feedback and updates",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            ///////////////////////////////////////////////////////
            // 🌙 DARK MODE
            ///////////////////////////////////////////////////////
            SettingToggleItem(
                title = "Dark Mode",
                subtitle = "Switch appearance",
                icon = Icons.Default.DarkMode,
                checked = darkModeEnabled,
                onCheckedChange = { darkModeEnabled = it }
            )

            ///////////////////////////////////////////////////////
            // 🔐 PRIVACY
            ///////////////////////////////////////////////////////
            SettingItem(
                title = "Privacy & Security",
                icon = Icons.Default.Lock,
                onClick = {
                    navController.navigate("privacy")
                }
            )

            ///////////////////////////////////////////////////////
            // 💳 SUBSCRIPTION
            ///////////////////////////////////////////////////////
            SettingItem(
                title = "Subscription",
                icon = Icons.Default.Star,
                onClick = {
                    navController.navigate("payment")
                }
            )

            ///////////////////////////////////////////////////////
            // ❓ HELP (REALISTIC ADDITION)
            ///////////////////////////////////////////////////////
            SettingItem(
                title = "Help & Support",
                icon = Icons.Default.Info,
                onClick = {
                    // future screen
                }
            )

            ///////////////////////////////////////////////////////
            // 🚪 LOGOUT (ONLY IF USER EXISTS)
            ///////////////////////////////////////////////////////
            Spacer(modifier = Modifier.weight(1f))

            if (hasProfile) {
                Button(
                    onClick = {
                        hasProfile = false
                        userName = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Logout", color = TextOnDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// 🔁 TOGGLE ITEM
///////////////////////////////////////////////////////////
@Composable
fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary)
        )
    }
}

///////////////////////////////////////////////////////////
// 🔁 NORMAL ITEM
///////////////////////////////////////////////////////////
@Composable
fun SettingItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
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
fun SettingsScreenPreview() {
    SettingsScreen(rememberNavController())
}