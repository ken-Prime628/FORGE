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

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(navController: NavController) {

    ///////////////////////////////////////////////////////
    // STATE (FIRST-TIME SAFE DEFAULTS)
    ///////////////////////////////////////////////////////
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var emailVerified by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundMain,

        ///////////////////////////////////////////////////////
        // TOP BAR
        ///////////////////////////////////////////////////////
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Security", color = TextPrimary, fontWeight = FontWeight.Bold)
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
            // 🔑 ACCOUNT SECURITY
            ///////////////////////////////////////////////////////
            SecurityCard {
                SectionTitle("Account Security")

                SecurityActionItem(
                    title = "Change Password",
                    subtitle = "Update your account password",
                    icon = Icons.Default.Lock,
                    onClick = {
                        navController.navigate("change_password")
                    }
                )

                SecurityActionItem(
                    title = "Email Verification",
                    subtitle = if (emailVerified)
                        "Your email is verified"
                    else
                        "Verify your email for better security",
                    icon = Icons.Default.Email,
                    onClick = {
                        emailVerified = true // simulate verification
                    },
                    highlight = !emailVerified
                )
            }

            ///////////////////////////////////////////////////////
            // 🔐 TWO FACTOR AUTH
            ///////////////////////////////////////////////////////
            SecurityCard {
                SectionTitle("Two-Factor Authentication")

                SecurityToggleItem(
                    title = "Enable 2FA",
                    subtitle = if (twoFactorEnabled)
                        "Extra security enabled"
                    else
                        "Add an extra layer of protection",
                    icon = Icons.Default.Security,
                    checked = twoFactorEnabled,
                    onCheckedChange = { twoFactorEnabled = it }
                )
            }

            ///////////////////////////////////////////////////////
            // 📱 LOGIN ACTIVITY
            ///////////////////////////////////////////////////////
            SecurityCard {
                SectionTitle("Login Activity")

                SecurityInfoItem(
                    title = "Last Login",
                    value = "Just now (This device)"
                )

                SecurityInfoItem(
                    title = "Device",
                    value = "Android • Chrome"
                )

                SecurityActionItem(
                    title = "View All Sessions",
                    subtitle = "Manage logged-in devices",
                    icon = Icons.Default.Devices,
                    onClick = {
                        navController.navigate("sessions")
                    }
                )
            }

            ///////////////////////////////////////////////////////
            // ⚠️ DANGER ZONE
            ///////////////////////////////////////////////////////
            SecurityCard {
                SectionTitle("Danger Zone")

                SecurityActionItem(
                    title = "Logout from all devices",
                    subtitle = "Secure your account instantly",
                    icon = Icons.Default.Logout,
                    onClick = {
                        // future logout all devices
                    },
                    danger = true
                )
            }

            ///////////////////////////////////////////////////////
            // 🧠 FIRST-TIME UX TEXT
            ///////////////////////////////////////////////////////
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Keep your account secure. You can update these anytime.",
                modifier = Modifier.padding(16.dp),
                color = TextSecondary
            )
        }
    }
}

///////////////////////////////////////////////////////////
// 🔥 CARD WRAPPER (PREMIUM LOOK)
///////////////////////////////////////////////////////////
@Composable
fun SecurityCard(content: @Composable ColumnScope.() -> Unit) {
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
// 🔁 SECTION TITLE
///////////////////////////////////////////////////////////
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(12.dp),
        color = GoldPrimary,
        fontWeight = FontWeight.Bold
    )
}

///////////////////////////////////////////////////////////
// 🔁 ACTION ITEM
///////////////////////////////////////////////////////////
@Composable
fun SecurityActionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    highlight: Boolean = false,
    danger: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            icon,
            null,
            tint = when {
                danger -> MaterialTheme.colorScheme.error
                highlight -> GoldPrimary
                else -> GoldPrimary
            }
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (danger) MaterialTheme.colorScheme.error else TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(subtitle, color = TextSecondary)
        }

        Icon(Icons.Default.ArrowForwardIos, null, tint = TextSecondary)
    }
}

///////////////////////////////////////////////////////////
// 🔁 TOGGLE ITEM
///////////////////////////////////////////////////////////
@Composable
fun SecurityToggleItem(
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
// 🔁 INFO ITEM
///////////////////////////////////////////////////////////
@Composable
fun SecurityInfoItem(title: String, value: String) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(title, color = TextSecondary)
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun SecurityScreenPreview() {
    SecurityScreen(rememberNavController())
}