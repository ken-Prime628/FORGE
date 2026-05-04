package com.kennedy.forge.ui.screens.growth

import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

///////////////////////////////////////////////////////////
// MODEL
///////////////////////////////////////////////////////////

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val priceSubtext: String,
    val badge: String?,          // e.g. "POPULAR"
    val ussdCode: String,
    val features: List<String>,
    val accentColor: Color
)

enum class PaymentTab { PLANS, SUBSCRIPTION }

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavController) {

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(PaymentTab.PLANS) }

    val plans = listOf(
        SubscriptionPlan(
            id = "starter",
            name = "Starter",
            price = "KES 500",
            priceSubtext = "per month",
            badge = null,
            ussdCode = "*234*1#",   // 🔁 Replace with real USSD
            features = listOf(
                "Basic creative access",
                "Up to 5 uploads / month",
                "Community feedback",
                "Standard support"
            ),
            accentColor = SoftBlue
        ),
        SubscriptionPlan(
            id = "pro",
            name = "Pro",
            price = "KES 1,500",
            priceSubtext = "per month",
            badge = "POPULAR",
            ussdCode = "*234*2#",   // 🔁 Replace with real USSD
            features = listOf(
                "Unlimited uploads",
                "Priority feedback",
                "Advanced analytics",
                "Pro badge on profile",
                "Priority support"
            ),
            accentColor = GoldPrimary
        ),
        SubscriptionPlan(
            id = "elite",
            name = "Elite",
            price = "KES 2,900",
            priceSubtext = "per month",
            badge = "BEST VALUE",
            ussdCode = "*234*3#",   // 🔁 Replace with real USSD
            features = listOf(
                "Everything in Pro",
                "1-on-1 mentorship sessions",
                "Early feature access",
                "Elite badge on profile",
                "Dedicated account manager"
            ),
            accentColor = SoftPeach
        )
    )

    var selectedPlan by remember { mutableStateOf(plans[1]) }

    // Current active subscription mock data
    val currentPlan = plans[0]      // 🔁 Replace with real DB lookup
    val renewalDate = "June 4, 2025"  // 🔁 Replace with real date

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Forge Premium",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
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

            ///////////////////////////////////////////////////
            // HERO STRIP — editorial gold bar
            ///////////////////////////////////////////////////
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(GoldDeep, GoldAccent, GoldPrimary)
                        )
                    )
                    .padding(vertical = 20.dp, horizontal = 24.dp)
            ) {
                Column {
                    Text(
                        "UNLOCK YOUR FULL POTENTIAL",
                        color = TextOnDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Premium plans built for creators who mean business.",
                        color = TextOnDark.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            ///////////////////////////////////////////////////
            // TAB SWITCHER
            ///////////////////////////////////////////////////
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundSecondary),
            ) {
                listOf(PaymentTab.PLANS to "Choose Plan", PaymentTab.SUBSCRIPTION to "My Subscription")
                    .forEach { (tab, label) ->
                        val isActive = activeTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isActive)
                                        Brush.horizontalGradient(listOf(GoldPrimary, GoldAccent))
                                    else
                                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                                )
                                .clickable { activeTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = if (isActive) TextOnDark else TextSecondary,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
            }

            ///////////////////////////////////////////////////
            // TAB CONTENT
            ///////////////////////////////////////////////////
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(220))
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {

                    //////////////////////////////////////////////
                    // ── TAB 1: CHOOSE A PLAN ──
                    //////////////////////////////////////////////
                    PaymentTab.PLANS -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                plans.forEach { plan ->
                                    PlanCard(
                                        plan = plan,
                                        isSelected = selectedPlan == plan,
                                        onSelect = { selectedPlan = plan }
                                    )
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Divider label
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = SoftOlive.copy(alpha = 0.3f)
                                )
                                Text(
                                    "  PAYMENT METHOD  ",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = SoftOlive.copy(alpha = 0.3f)
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // M-Pesa / STK badge
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardBackground)
                                    .border(
                                        1.dp,
                                        GoldPrimary.copy(alpha = 0.3f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SoftGreen.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📱", fontSize = 22.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Mobile Money (STK Push)",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "M-Pesa · Airtel Money · T-Kash",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SoftGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "ACTIVE",
                                        color = SoftGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            // PAY NOW BUTTON — triggers SIM Toolkit / USSD
                            Button(
                                onClick = {
                                    // Launches the SIM Toolkit via tel: intent with USSD code
                                    // This triggers the STK (SIM Application Toolkit) on the device
                                    val rawCode = selectedPlan.ussdCode
                                    try {
                                        val encoded = URLEncoder.encode(rawCode, "UTF-8")
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:$encoded")
                                        )
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(GoldDeep, GoldPrimary, GoldAccent)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("📲", fontSize = 18.sp)
                                        Text(
                                            "Pay ${selectedPlan.price} — ${selectedPlan.name}",
                                            color = TextOnDark,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                "STK push will appear on your phone · Code: ${selectedPlan.ussdCode}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 24.dp),
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }

                    //////////////////////////////////////////////
                    // ── TAB 2: MY SUBSCRIPTION ──
                    //////////////////////////////////////////////
                    PaymentTab.SUBSCRIPTION -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(Modifier.height(4.dp))

                            // Active plan card — dark editorial style
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(DarkCard, DarkSurface)
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        GoldPrimary.copy(alpha = 0.5f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(24.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                "CURRENT PLAN",
                                                color = GoldPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                currentPlan.name,
                                                color = TextOnDark,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 26.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                "● ACTIVE",
                                                color = SoftGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(20.dp))

                                    // Usage bar
                                    Text(
                                        "Uploads used this month",
                                        color = TextOnDark.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { 0.6f }, // 🔁 Replace with real value
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = GoldAccent,
                                        trackColor = TextOnDark.copy(alpha = 0.1f)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "3 of 5 uploads used",  // 🔁 Replace with real value
                                        color = TextOnDark.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )

                                    Spacer(Modifier.height(20.dp))

                                    HorizontalDivider(color = TextOnDark.copy(alpha = 0.1f))

                                    Spacer(Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Billing", color = TextOnDark.copy(0.5f), fontSize = 11.sp)
                                            Text(currentPlan.price, color = GoldAccent, fontWeight = FontWeight.Bold)
                                            Text("per month", color = TextOnDark.copy(0.4f), fontSize = 10.sp)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Renews", color = TextOnDark.copy(0.5f), fontSize = 11.sp)
                                            Text(renewalDate, color = TextOnDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            Text(
                                "INCLUDED FEATURES",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )

                            Spacer(Modifier.height(12.dp))

                            currentPlan.features.forEach { feature ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(GoldPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Text(feature, color = TextPrimary, fontSize = 14.sp)
                                }
                            }

                            Spacer(Modifier.height(28.dp))

                            // Upgrade nudge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(GoldPrimary.copy(0.08f), GoldAccent.copy(0.12f))
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        GoldPrimary.copy(alpha = 0.25f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(18.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Upgrade to Pro",
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Unlock unlimited uploads and mentorship.",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                    TextButton(
                                        onClick = { activeTab = PaymentTab.PLANS }
                                    ) {
                                        Text(
                                            "See Plans",
                                            color = GoldPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(24.dp))

                            // Cancel button
                            OutlinedButton(
                                onClick = { /* 🔁 Handle cancellation */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Error
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = Brush.horizontalGradient(listOf(Error.copy(0.5f), Error.copy(0.5f)))
                                )
                            ) {
                                Text(
                                    "Cancel Subscription",
                                    color = Error,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PLAN CARD
///////////////////////////////////////////////////////////

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) plan.accentColor else Color.Transparent
    val bgBrush = if (isSelected)
        Brush.linearGradient(listOf(plan.accentColor.copy(0.08f), plan.accentColor.copy(0.03f)))
    else
        Brush.linearGradient(listOf(CardBackground, CardBackground))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) borderColor else SoftOlive.copy(alpha = 0.2f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onSelect)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Selection dot
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) plan.accentColor
                                else BackgroundSecondary
                            )
                            .border(
                                2.dp,
                                if (isSelected) plan.accentColor else SoftOlive.copy(0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = TextOnDark,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            plan.name.uppercase(),
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                        plan.badge?.let {
                            Text(
                                it,
                                color = plan.accentColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        plan.price,
                        color = if (isSelected) plan.accentColor else TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp
                    )
                    Text(
                        plan.priceSubtext,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            if (isSelected) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = plan.accentColor.copy(alpha = 0.2f))
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    plan.features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(plan.accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = plan.accentColor,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Text(feature, color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(rememberNavController())
}