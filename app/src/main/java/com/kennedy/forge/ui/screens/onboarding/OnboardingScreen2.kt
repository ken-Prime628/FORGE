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
import com.kennedy.forge.navigation.ROUT_ONBOARDING3
import com.kennedy.forge.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Design Direction: Same luxury editorial language as Screen 1.
// Ambient glow shifts to the RIGHT this time for visual variety across the flow.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen2(navController: NavController) {

    val infiniteTransition = rememberInfiniteTransition(label = "glow2")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.34f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha2"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── LAYER 1: Background image ─────────────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.onboarding2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── LAYER 2: Deep vignette ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.20f),
                            0.38f to Color.Black.copy(alpha = 0.10f),
                            0.60f to Color.Black.copy(alpha = 0.55f),
                            1.0f to Color(0xFF0A0805).copy(alpha = 0.97f)
                        )
                    )
                )
        )

        // ── LAYER 3: Ambient gold glow — bottom-RIGHT for variety ─────────────
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 140.dp, y = 500.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
                .blur(56.dp)
        )

        // ── LAYER 4: UI ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── TOP BAR ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { navController.navigate(ROUTE_Register) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Skip",
                        color = TextOnDark.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W400,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // ── MAIN CONTENT ──────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {

                // Eyebrow
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
                        text = "FEEDBACK & CLARITY",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Headline
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = TextOnDark,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-1).sp
                            )
                        ) { append("Get Real\n") }
                        withStyle(
                            SpanStyle(
                                brush = Brush.linearGradient(
                                    listOf(GoldAccent, GoldPrimary, GoldDeep)
                                ),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-1).sp
                            )
                        ) { append("Feedback") }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Decorative rule
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

                // Accent subheading
                Text(
                    text = "Clarity beats guessing.",
                    color = GoldAccent.copy(alpha = 0.95f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forge connects you with meaningful, structured feedback designed to help you understand what works — and what doesn't.",
                    color = TextOnDark.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 22.sp,
                    letterSpacing = 0.1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Improve with direction. Build confidence in every piece you create.",
                    color = SoftOlive.copy(alpha = 0.80f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W300,
                    lineHeight = 20.sp,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── BOTTOM CONTROLS ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Dot indicators — second dot active
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TextOnDark.copy(alpha = 0.25f))
                        )
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
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TextOnDark.copy(alpha = 0.25f))
                        )
                    }

                    // Back + Next controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back — ghost pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { navController.popBackStack() }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "←",
                                    color = TextOnDark.copy(alpha = 0.65f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.W600
                                )
                                Text(
                                    text = "Back",
                                    color = TextOnDark.copy(alpha = 0.65f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.W500,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }

                        // Next — gold pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoldAccent, GoldPrimary)
                                    )
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { navController.navigate(ROUT_ONBOARDING3) }
                                .padding(horizontal = 28.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Next",
                                    color = DarkSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W700,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "→",
                                    color = DarkSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.W700
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingScreen2Preview() {
    OnboardingScreen2(rememberNavController())
}