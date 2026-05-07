package com.kennedy.forge.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.navigation.ROUTE_Register
import com.kennedy.forge.navigation.ROUTE_SPLASH
import com.kennedy.forge.navigation.ROUT_ONBOARDING1
import com.kennedy.forge.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    var startAnimation by remember { mutableStateOf(false) }

    // ✨ Smooth entrance
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(900),
        label = ""
    )

    // 🌿 Soft floating effect (very subtle)
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // ⏱️ Navigation
    LaunchedEffect(true) {
        startAnimation = true
        delay(2200)

        navController.navigate(ROUT_ONBOARDING1) {
            popUpTo(ROUTE_SPLASH) { inclusive = true }
        }
    }

    // 🌈 BRIGHT LUXURY BACKGROUND
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundMain,       // soft cream
                        BackgroundSecondary   // warm beige
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // 🔥 CLEAN CENTER LOGO (NO HEAVY EFFECTS)
        Icon(
            painter = painterResource(id = R.drawable.ic_forge_logo),
            contentDescription = "Forge Logo",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(500.dp)
                .scale(scale)
                .alpha(alpha)
                .offset(y = floatY.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(rememberNavController())
}