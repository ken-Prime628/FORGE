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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.navigation.ROUTE_Register
import com.kennedy.forge.navigation.ROUT_ONBOARDING2
import com.kennedy.forge.navigation.ROUT_ONBOARDING3
import com.kennedy.forge.ui.theme.*
import com.kennedy.forge.ui.screens.components.Indicator

// ─────────────────────────────────────────────────────────────────────────────
// Design Direction: LUXURY EDITORIAL — Dark backdrop, molten-gold typography,
// vignette image treatment, oversized display text with a refined accent line.
// The screen feels like a high-end magazine cover meets a craftsman's workshop.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen1(navController: NavController) {

    // Subtle pulsing glow animation for the gold orb accent
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── LAYER 1: Full-bleed background image ──────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.onboarding1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── LAYER 2: Deep dark vignette — heavier at bottom for text legibility
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

        // ── LAYER 3: Ambient gold glow bloom (bottom-left) ────────────────────
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-60).dp, y = 480.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GoldAccent.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
                .blur(60.dp)
        )

        // ── LAYER 4: Foreground UI ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // ── TOP BAR ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wordmark / logo pill
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

                // Skip pill
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

            // ── MAIN CONTENT BLOCK ───────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {

                // Eyebrow label
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
                        text = "CREATIVE GROWTH",
                        color = GoldAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── HEADLINE — oversized editorial display ────────────────────
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = TextOnDark,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-1).sp
                            )
                        ) { append("Forge Your\n") }
                        withStyle(
                            SpanStyle(
                                brush = Brush.linearGradient(
                                    listOf(GoldAccent, GoldPrimary, GoldDeep)
                                ),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-1).sp
                            )
                        ) { append("Creativity") }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Decorative horizontal rule
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

                // ── BODY COPY ─────────────────────────────────────────────────
                Text(
                    text = "Turn raw ideas into refined, impactful work.",
                    color = GoldAccent.copy(alpha = 0.95f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Forge is designed for creators who want more than inspiration — a structured path to improve your craft, refine your thinking, and build work that stands out.",
                    color = TextOnDark.copy(alpha = 0.72f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 22.sp,
                    letterSpacing = 0.1.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Every step guided. Every idea purposeful.",
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

                    // Dot indicators (replaced Indicator composable for design fidelity)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active dot
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
                        // Inactive dots
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TextOnDark.copy(alpha = 0.25f))
                            )
                        }
                    }

                    // Next CTA button — gold gradient pill
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
                            ) { navController.navigate(ROUT_ONBOARDING2) }
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
                            // Arrow icon substitute using Unicode
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingScreen1Preview() {
    OnboardingScreen1(rememberNavController())
}