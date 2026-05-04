package com.kennedy.forge.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

// ─────────────────────────────────────────────
// COLOR PALETTE (inline since imported from theme)
// ─────────────────────────────────────────────

private val BackgroundMain      = Color(0xFFF5F2EC)
private val BackgroundSecondary = Color(0xFFEDE7DD)
private val CardBackground      = Color(0xFFFFFFFF)
private val GoldPrimary         = Color(0xFFC89B3C)
private val GoldAccent          = Color(0xFFE6B85C)
private val GoldDeep            = Color(0xFFA67C2E)
private val SoftGreen           = Color(0xFF7FBF9F)
private val SoftBlue            = Color(0xFF7DAED3)
private val SoftPeach           = Color(0xFFE8A87C)
private val SoftOlive           = Color(0xFFB5A27A)
private val TextPrimary         = Color(0xFF1A1A1A)
private val TextSecondary       = Color(0xFF6F6F6F)

// ─────────────────────────────────────────────
// SKILL OPTIONS DATA
// ─────────────────────────────────────────────

private val availableSkills = listOf(
    "UI/UX Design", "Web Development", "Mobile Dev",
    "Branding", "Motion Design", "Illustration",
    "Photography", "Copywriting", "3D Design", "Video Editing"
)

private val availableRoles = listOf(
    "Designer", "Developer", "Full-Stack", "Freelancer",
    "Student", "Creative Director", "Product Manager"
)

// ─────────────────────────────────────────────
// EDIT PROFILE SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {

    // ── Form state ──────────────────────────────
    var fullName       by remember { mutableStateOf("") }
    var username       by remember { mutableStateOf("") }
    var bio            by remember { mutableStateOf("") }
    var location       by remember { mutableStateOf("") }
    var website        by remember { mutableStateOf("") }
    var selectedRole   by remember { mutableStateOf<String?>(null) }
    var selectedSkills by remember { mutableStateOf(setOf<String>()) }
    var showSavedToast by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // ── Toast auto-dismiss ───────────────────────
    LaunchedEffect(showSavedToast) {
        if (showSavedToast) {
            kotlinx.coroutines.delay(2000)
            showSavedToast = false
        }
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
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
                actions = {
                    TextButton(onClick = { showSavedToast = true }) {
                        Text(
                            "Save",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── AVATAR SECTION ───────────────────────────
                AvatarSection()

                Spacer(Modifier.height(8.dp))

                // ── BASIC INFO ───────────────────────────────
                SectionLabel("Basic Info")
                FormCard {
                    ForgeTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        placeholder = "e.g. Kennedy Ochieng",
                        icon = Icons.Default.Person,
                        capitalization = KeyboardCapitalization.Words
                    )
                    FormDivider()
                    ForgeTextField(
                        value = username,
                        onValueChange = { username = it.lowercase().replace(" ", "") },
                        label = "Username",
                        placeholder = "@yourhandle",
                        icon = Icons.Default.AlternateEmail,
                        prefix = "@"
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── BIO ──────────────────────────────────────
                SectionLabel("About You")
                FormCard {
                    BioField(
                        value = bio,
                        onValueChange = { if (it.length <= 160) bio = it }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── LINKS & LOCATION ─────────────────────────
                SectionLabel("Links & Location")
                FormCard {
                    ForgeTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Location",
                        placeholder = "City, Country",
                        icon = Icons.Default.LocationOn
                    )
                    FormDivider()
                    ForgeTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = "Website / Portfolio",
                        placeholder = "https://yoursite.com",
                        icon = Icons.Default.Link,
                        keyboardType = KeyboardType.Uri
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── ROLE ─────────────────────────────────────
                SectionLabel("Your Role")
                RoleSelector(
                    roles = availableRoles,
                    selected = selectedRole,
                    onSelect = { selectedRole = if (selectedRole == it) null else it }
                )

                Spacer(Modifier.height(20.dp))

                // ── SKILLS ───────────────────────────────────
                SectionLabel("Skills  ·  Pick up to 5")
                SkillsGrid(
                    skills = availableSkills,
                    selected = selectedSkills,
                    onToggle = { skill ->
                        selectedSkills = if (selectedSkills.contains(skill)) {
                            selectedSkills - skill
                        } else if (selectedSkills.size < 5) {
                            selectedSkills + skill
                        } else selectedSkills
                    }
                )

                Spacer(Modifier.height(28.dp))

                // ── SAVE BUTTON ──────────────────────────────
                Button(
                    onClick = { showSavedToast = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Save Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(40.dp))
            }

            // ── TOAST ────────────────────────────────────────
            AnimatedVisibility(
                visible = showSavedToast,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SavedToast()
            }
        }
    }
}

// ─────────────────────────────────────────────
// AVATAR SECTION
// ─────────────────────────────────────────────

@Composable
fun AvatarSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GoldPrimary.copy(alpha = 0.08f), Color.Transparent)
                )
            )
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            // Avatar ring
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.linearGradient(listOf(GoldAccent, GoldDeep)),
                        shape = CircleShape
                    )
                    .background(BackgroundSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(46.dp)
                )
            }

            // Camera badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary)
                    .border(2.dp, CardBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Tap to change photo",
            color = GoldPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            "JPG or PNG · Max 5MB",
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

// ─────────────────────────────────────────────
// SECTION LABEL
// ─────────────────────────────────────────────

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
        color = SoftOlive,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

// ─────────────────────────────────────────────
// FORM CARD WRAPPER
// ─────────────────────────────────────────────

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

// ─────────────────────────────────────────────
// DIVIDER INSIDE FORM CARD
// ─────────────────────────────────────────────

@Composable
fun FormDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
        thickness = 0.5.dp,
        color = BackgroundSecondary
    )
}

// ─────────────────────────────────────────────
// FORGE TEXT FIELD
// ─────────────────────────────────────────────

@Composable
fun ForgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (prefix != null) {
                    Text(prefix, color = GoldDeep, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                BasicForgeInput(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = placeholder,
                    keyboardType = keyboardType,
                    capitalization = capitalization
                )
            }
        }
    }
}

@Composable
fun BasicForgeInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization
        ),
        singleLine = true,
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, color = TextSecondary.copy(alpha = 0.6f), fontSize = 15.sp)
            }
            inner()
        }
    )
}

// ─────────────────────────────────────────────
// BIO FIELD
// ─────────────────────────────────────────────

@Composable
fun BioField(value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EditNote, null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("Bio", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(8.dp))

        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 80.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        "Tell people what you create, what inspires you, or what you're working on…",
                        color = TextSecondary.copy(alpha = 0.55f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
                inner()
            }
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "${value.length}/160",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            color = if (value.length > 140) SoftPeach else TextSecondary,
            fontSize = 11.sp
        )
    }
}

// ─────────────────────────────────────────────
// ROLE SELECTOR
// ─────────────────────────────────────────────

@Composable
fun RoleSelector(
    roles: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Wrap rows manually for a 2-column tag layout
        val chunked = roles.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { role ->
                    val isSelected = selected == role
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) GoldPrimary
                                else CardBackground
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) GoldPrimary else BackgroundSecondary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelect(role) }
                            .padding(vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            role,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
                // Fill empty cell if odd count
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ─────────────────────────────────────────────
// SKILLS GRID
// ─────────────────────────────────────────────

@Composable
fun SkillsGrid(
    skills: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        skills.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { skill ->
                    val isSelected = selected.contains(skill)
                    val chipColor = when (skill) {
                        "UI/UX Design"     -> SoftBlue
                        "Web Development"  -> SoftGreen
                        "Mobile Dev"       -> GoldAccent
                        "Branding"         -> SoftPeach
                        "Motion Design"    -> SoftOlive
                        "Illustration"     -> Color(0xFFB39DDB)
                        else               -> GoldPrimary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) chipColor.copy(alpha = 0.15f)
                                else CardBackground
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) chipColor else BackgroundSecondary,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onToggle(skill) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            skill,
                            color = if (isSelected) chipColor else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ─────────────────────────────────────────────
// SAVED TOAST
// ─────────────────────────────────────────────

@Composable
fun SavedToast() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1C1C1C))
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = SoftGreen, modifier = Modifier.size(20.dp))
        Text("Profile saved!", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    EditProfileScreen(rememberNavController())
}