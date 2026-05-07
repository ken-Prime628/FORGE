package com.kennedy.forge.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.navigation.ROUTE_Register
import com.kennedy.forge.navigation.ROUT_ONBOARDING1
import com.kennedy.forge.navigation.ROUT_ProfileSetup
import com.kennedy.forge.ui.theme.*

@Composable
fun OnboardingScreen3(navController: NavController) {

    val infiniteTransition = rememberInfiniteTransition(label = "glow3")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha3"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.onboarding3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.30f),
                            0.30f to Color.Black.copy(alpha = 0.15f),
                            0.55f to Color.Black.copy(alpha = 0.65f),
                            1.0f to Color(0xFF0A0805).copy(alpha = 0.98f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = glowAlpha),
                            GoldDeep.copy(alpha = glowAlpha * 0.4f),
                            Color.Transparent
                        )
                    )
                )
                .blur(70.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "FORGE",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        letterSpacing = 4.sp
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(1.5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GoldPrimary, GoldAccent)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "YOUR JOURNEY BEGINS",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = TextOnDark,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-1).sp
                            )
                        ) { append("Turn Talent\nInto ") }
                        withStyle(
                            SpanStyle(
                                brush = Brush.linearGradient(
                                    listOf(GoldAccent, GoldPrimary, GoldDeep)
                                ),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-1).sp
                            )
                        ) { append("Impact") }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldDeep.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Build a portfolio, receive expert-level feedback, and connect with a community that pushes you to grow.",
                    color = TextOnDark.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "From learning to mastery — this is where it happens.",
                    color = SoftOlive.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(2) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TextOnDark.copy(alpha = 0.25f))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GoldAccent, GoldPrimary)
                                        )
                                    )
                            )
                        }
                    }

                    ///////////////////////////////////////////////////////
                    // ✅ FIXED NAVIGATION BUTTON
                    ///////////////////////////////////////////////////////
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(GoldAccent, GoldPrimary, GoldDeep)
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(ROUTE_Register) {
                                    popUpTo(ROUT_ONBOARDING1) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Get Started",
                            color = DarkSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "By continuing, you agree to our Terms & Privacy Policy",
                            color = TextOnDark.copy(alpha = 0.30f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreen3Preview() {
    OnboardingScreen3(rememberNavController())
}