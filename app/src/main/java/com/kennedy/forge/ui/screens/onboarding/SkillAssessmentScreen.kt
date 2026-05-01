package com.kennedy.forge.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillAssessmentScreen(navController: NavController) {

    val levels = listOf(
        "Beginner",
        "Elementary",
        "Intermediate",
        "Upper Intermediate",
        "Advanced",
        "Proficiency"
    )

    var selectedLevel by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = BackgroundMain,

        // 🔝 TOP BAR
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Choose Your Level",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        },

        // 🔽 BOTTOM BUTTON
        bottomBar = {
            BottomBarSection(
                selectedLevel = selectedLevel,
                onConfirm = {
                    selectedLevel?.let {
                        navController.navigate("dashboard") {
                            popUpTo("skill_assessment") { inclusive = true }
                        }
                    }
                }
            )
        }

    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // 🧠 DESCRIPTION
            Text(
                text = "Select the level that best matches your current ability. This helps Forge tailor your growth journey and challenges.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 📋 LEVEL LIST
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(levels) { level ->
                    LevelCard(
                        level = level,
                        selected = selectedLevel == level,
                        onClick = { selectedLevel = level }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LevelCard(
    level: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) GoldPrimary else CardBackground,
        animationSpec = tween(300),
        label = ""
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) TextOnDark else TextPrimary,
        animationSpec = tween(300),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = level,
                color = textColor,
                style = MaterialTheme.typography.titleMedium
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = TextOnDark,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(
                            2.dp,
                            TextSecondary.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun BottomBarSection(
    selectedLevel: String?,
    onConfirm: () -> Unit
) {
    Surface(
        color = BackgroundMain,
        shadowElevation = 8.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            AnimatedVisibility(
                visible = selectedLevel != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {

                Button(
                    onClick = {

                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continue with $selectedLevel",
                        color = TextOnDark
                    )
                }
            }

            if (selectedLevel == null) {
                Text(
                    text = "Select a level to continue",
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSkillAssessment() {
    SkillAssessmentScreen(rememberNavController())
}