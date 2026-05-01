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
import com.kennedy.forge.navigation.ROUT_ProfileSetup
import com.kennedy.forge.ui.screens.components.Indicator
import com.kennedy.forge.ui.theme.*

@Composable
fun OnboardingScreen3(navController: NavController){

    Box(modifier = Modifier.fillMaxSize()) {

        // 🖼️ BACKGROUND IMAGE
        Image(
            painter = painterResource(id = R.drawable.onboarding3), // 🔥 Add your image
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 🌫️ DARK OVERLAY FOR READABILITY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
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

            // 🔼 TOP (EMPTY / CLEAN)
            Spacer(modifier = Modifier.height(10.dp))

            // 🧠 MAIN TEXT (POWERFUL CLOSE)
            Column {

                Text(
                    text = "Turn Your Talent Into Impact",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forge empowers you to refine your skills, showcase your work, and connect with a community that pushes you to grow. Build a portfolio, receive expert-level feedback, and transform your creativity into real-world success.\n\nThis is where your journey evolves from learning to mastery.",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            // 🔽 BOTTOM SECTION
            Column {

                // 🔘 Indicator (3rd screen)
                Indicator(current = 3)

                Spacer(modifier = Modifier.height(20.dp))

                // 🚀 GET STARTED BUTTON (MAIN ACTION)
                Button(
                    onClick = {
                        navController.navigate(ROUT_ProfileSetup)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                ) {
                    Text(
                        text = "Get Started",
                        color = TextOnDark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen3Preview() {
    OnboardingScreen3(rememberNavController())
}