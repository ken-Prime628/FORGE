package com.kennedy.forge.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.navigation.ROUT_Dashboard
import com.kennedy.forge.navigation.ROUT_SubmitWork

// ─────────────────────────────────────────────
// MODELS
// ─────────────────────────────────────────────

data class Project(
    val title: String,
    val description: String,
    val category: String,
    val likes: Int,
    val image: Int,
    val isNew: Boolean = false
)

data class Achievement(
    val icon: ImageVector,
    val label: String,
    val color: Color
)

// ─────────────────────────────────────────────
// REALISTIC DEMO DATA
// ─────────────────────────────────────────────

val demoProjects = listOf(
    Project(
        title = "Forge Landing Page",
        description = "A sleek, animated landing page built for the Forge platform launch.",
        category = "Web Design",
        likes = 142,
        image = R.drawable.hero_bg,
        isNew = true
    ),
    Project(
        title = "Artisan UI Kit",
        description = "A premium component library for crafting refined digital experiences.",
        category = "UI/UX",
        likes = 89,
        image = R.drawable.hero_bg
    ),
    Project(
        title = "Portfolio 2025",
        description = "My personal portfolio showcasing motion design and development work.",
        category = "Portfolio",
        likes = 203,
        image = R.drawable.hero_bg
    )
)

val demoAchievements = listOf(
    Achievement(Icons.Default.Star, "Top Creator", GoldPrimary),
    Achievement(Icons.Default.Favorite, "100+ Likes", SoftPeach),
    Achievement(Icons.Default.CheckCircle, "Verified", SoftGreen)
)

// ─────────────────────────────────────────────
// PROFILE SCREEN
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {

    var name by remember { mutableStateOf("Kennedy Ochieng") }
    var bio by remember { mutableStateOf("Creative developer crafting modern UI experiences. Passionate about design systems & motion.") }
    var isEditingProfile by remember { mutableStateOf(false) }

    var userProjects by remember { mutableStateOf(demoProjects.toMutableList()) }
    val totalLikes = userProjects.sumOf { it.likes }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Profile",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUT_Dashboard) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldPrimary)
                            .clickable { navController.navigate(ROUT_SubmitWork) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Add Work", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── HERO SECTION ──────────────────────────────────
            item {
                HeroSection(name = name, bio = bio)
            }

            // ── STATS ROW ────────────────────────────────────
            item {
                StatsRow(
                    projects = userProjects.size,
                    likes = totalLikes,
                    reviews = 18
                )
            }

            // ── ACHIEVEMENTS ─────────────────────────────────
            item {
                AchievementsRow(achievements = demoAchievements)
            }

            // ── EDIT PROFILE TOGGLE ──────────────────────────
            item {
                EditProfileSection(
                    name = name,
                    bio = bio,
                    isEditing = isEditingProfile,
                    onToggle = { isEditingProfile = !isEditingProfile },
                    onNameChange = { name = it },
                    onBioChange = { bio = it }
                )
            }

            // ── PROJECTS HEADER ──────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Work",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        "${userProjects.size} projects",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // ── PROJECT LIST ─────────────────────────────────
            if (userProjects.isEmpty()) {
                item {
                    EmptyProjectsPlaceholder(
                        onAdd = { navController.navigate(ROUT_SubmitWork) }
                    )
                }
            } else {
                items(userProjects, key = { it.title }) { project ->
                    ProjectCard(project = project)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// HERO SECTION
// ─────────────────────────────────────────────

@Composable
fun HeroSection(name: String, bio: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // Gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GoldPrimary.copy(alpha = 0.15f),
                            BackgroundMain
                        )
                    )
                )
        )

        // Decorative circle top right
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 80.dp, y = (-40).dp)
                .align(Alignment.TopEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.18f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .border(2.dp, GoldPrimary, CircleShape)
                    .background(BackgroundSecondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                name,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(4.dp))

            // Role pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GoldPrimary.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    "UI/UX Developer · Open to work",
                    color = GoldDeep,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                bio,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────
// STATS ROW
// ─────────────────────────────────────────────

@Composable
fun StatsRow(projects: Int, likes: Int, reviews: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = projects.toString(),
            label = "Projects",
            icon = Icons.Default.GridView,
            accent = GoldPrimary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$likes",
            label = "Likes",
            icon = Icons.Default.Favorite,
            accent = SoftPeach,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = "$reviews",
            label = "Reviews",
            icon = Icons.Default.Star,
            accent = SoftGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────
// ACHIEVEMENTS ROW
// ─────────────────────────────────────────────

@Composable
fun AchievementsRow(achievements: List<Achievement>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            "Achievements",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            achievements.forEach { achievement ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(achievement.color.copy(alpha = 0.1f))
                        .border(1.dp, achievement.color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(achievement.icon, null, tint = achievement.color, modifier = Modifier.size(14.dp))
                    Text(achievement.label, color = achievement.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// EDIT PROFILE SECTION
// ─────────────────────────────────────────────

@Composable
fun EditProfileSection(
    name: String,
    bio: String,
    isEditing: Boolean,
    onToggle: () -> Unit,
    onNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                    Text("Edit Profile", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                TextButton(onClick = onToggle) {
                    Text(if (isEditing) "Done" else "Edit", color = GoldPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            AnimatedVisibility(
                visible = isEditing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = GoldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BackgroundSecondary,
                            focusedLabelColor = GoldPrimary
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = onBioChange,
                        label = { Text("Bio") },
                        leadingIcon = { Icon(Icons.Default.Info, null, tint = GoldPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BackgroundSecondary,
                            focusedLabelColor = GoldPrimary
                        )
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onToggle,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PROJECT CARD
// ─────────────────────────────────────────────

@Composable
fun ProjectCard(project: Project) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image with overlaid category badge + NEW tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Image(
                    painter = painterResource(id = project.image),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient scrim
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )

                // Category badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(project.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // NEW badge
                if (project.isNew) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftGreen)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("NEW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    project.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    project.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))

                // Footer row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Favorite, null, tint = SoftPeach, modifier = Modifier.size(16.dp))
                        Text("${project.likes} likes", color = TextSecondary, fontSize = 12.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldPrimary.copy(alpha = 0.1f))
                            .clickable { }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("View", color = GoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// EMPTY STATE
// ─────────────────────────────────────────────

@Composable
fun EmptyProjectsPlaceholder(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("No projects yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            "Start building your portfolio by adding your first project.",
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
        ) {
            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Add First Project", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────

@Preview(showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(rememberNavController())
}