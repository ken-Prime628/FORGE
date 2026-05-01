package com.kennedy.forge.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.kennedy.forge.navigation.ROUT_ONBOARDING2
import com.kennedy.forge.navigation.ROUT_ONBOARDING3
import com.kennedy.forge.ui.theme.*
import com.kennedy.forge.ui.screens.components.Indicator

@Composable
fun OnboardingScreen1(navController: NavController) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // 🖼️ FULL IMAGE BACKGROUND
        Image(
            painter = painterResource(id = R.drawable.onboarding1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🌫️ OVERLAY (FOR READABILITY)
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("",
                    color = Color.White
                )

                Text(
                    text = "Skip",
                    color = GoldAccent,
                    modifier = Modifier.clickable {
                        navController.navigate(ROUTE_Register)
                    }
                )
            }

            // 🧠 TEXT CONTENT (UPGRADED)
            Column {

                Text(
                    text = "Forge Your Creativity",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Turn raw ideas into refined, impactful work.",
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forge is designed for creators who want more than just inspiration. It gives you a structured path to improve your craft, refine your thinking, and build work that stands out.",
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "From idea to execution, every step is guided so you can grow with clarity, confidence, and purpose.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // 🔽 BOTTOM SECTION
            Column {

                Indicator(current = 1)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Skip",
                        color = SoftPeach,
                        modifier = Modifier.clickable {
                            navController.navigate(ROUT_ONBOARDING3)
                        }
                    )

                    Text(
                        text = "Next",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            navController.navigate(ROUT_ONBOARDING2)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen1Preview(){
    OnboardingScreen1(rememberNavController())
}