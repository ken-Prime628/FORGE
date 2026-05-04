package com.kennedy.forge.ui.screens.security

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun ChangePasswordScreen(navController: NavController) {

    ///////////////////////////////////////////////////////
    // STATE (DB READY)
    ///////////////////////////////////////////////////////
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    ///////////////////////////////////////////////////////
    // VALIDATION
    ///////////////////////////////////////////////////////
    fun validate(): Boolean {
        return when {
            currentPassword.isBlank() -> {
                errorMessage = "Enter current password"
                false
            }
            newPassword.length < 6 -> {
                errorMessage = "New password must be at least 6 characters"
                false
            }
            newPassword != confirmPassword -> {
                errorMessage = "Passwords do not match"
                false
            }
            else -> {
                errorMessage = null
                true
            }
        }
    }

    ///////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////
    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Change Password", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            ///////////////////////////////////////////////////////
            // 🔐 HEADER
            ///////////////////////////////////////////////////////
            Text(
                "Update your password",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Make sure your new password is strong and secure.",
                color = TextSecondary
            )

            Spacer(Modifier.height(20.dp))

            ///////////////////////////////////////////////////////
            // CURRENT PASSWORD
            ///////////////////////////////////////////////////////
            PasswordField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Current Password",
                visible = showCurrent,
                onToggleVisibility = { showCurrent = !showCurrent }
            )

            Spacer(Modifier.height(12.dp))

            ///////////////////////////////////////////////////////
            // NEW PASSWORD
            ///////////////////////////////////////////////////////
            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "New Password",
                visible = showNew,
                onToggleVisibility = { showNew = !showNew }
            )

            Spacer(Modifier.height(12.dp))

            ///////////////////////////////////////////////////////
            // CONFIRM PASSWORD
            ///////////////////////////////////////////////////////
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                visible = showConfirm,
                onToggleVisibility = { showConfirm = !showConfirm }
            )

            ///////////////////////////////////////////////////////
            // ERROR MESSAGE
            ///////////////////////////////////////////////////////
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))

            ///////////////////////////////////////////////////////
            // SAVE BUTTON
            ///////////////////////////////////////////////////////
            Button(
                onClick = {
                    if (validate()) {
                        success = true
                        // TODO: Connect Firebase Auth updatePassword()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Save Changes", color = TextOnDark, fontWeight = FontWeight.Bold)
            }

            ///////////////////////////////////////////////////////
            // SUCCESS STATE
            ///////////////////////////////////////////////////////
            if (success) {
                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            SoftGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        "Password updated successfully",
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// 🔁 PASSWORD FIELD COMPONENT
///////////////////////////////////////////////////////////
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        }
    )
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showSystemUi = true)
@Composable
fun ChangePasswordPreview() {
    ChangePasswordScreen(rememberNavController())
}