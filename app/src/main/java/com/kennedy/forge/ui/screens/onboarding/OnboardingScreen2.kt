package com.kennedy.forge.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.navigation.ROUTE_Register
import com.kennedy.forge.navigation.ROUT_ONBOARDING3
import com.kennedy.forge.ui.screens.components.Indicator
import com.kennedy.forge.ui.theme.*

@Composable
fun OnboardingScreen2(navController: NavController){

    Box(modifier = Modifier.fillMaxSize()) {

        // 🖼️ BACKGROUND IMAGE
        Image(
            painter = painterResource(id = R.drawable.onboarding2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🌫️ OVERLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // 📄 CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 🔼 TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Skip",
                    color = GoldAccent,
                    modifier = Modifier.clickable {
                        navController.navigate(ROUTE_Register)
                    }
                )
            }

            // 🧠 TEXT CONTENT (RICH + STRUCTURED)
            Column {

                Text(
                    text = "Get Real Feedback",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Clarity beats guessing.",
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forge connects you with meaningful, structured feedback designed to help you understand what works — and what doesn’t.",
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "No more uncertainty. Improve with direction, refine your skills faster, and build confidence in every piece you create.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // 🔽 BOTTOM
            Column {

                Indicator(current = 2)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Back",
                        color = SoftPeach,
                        modifier = Modifier.clickable {
                            navController.popBackStack()
                        }
                    )

                    Text(
                        text = "Next",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(ROUT_ONBOARDING3) // ✅ FIXED
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen2Preview() {
    OnboardingScreen2(rememberNavController())
}