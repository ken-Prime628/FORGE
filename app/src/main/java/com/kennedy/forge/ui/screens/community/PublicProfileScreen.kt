package com.kennedy.forge.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import com.kennedy.forge.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────────
//  MODELS
//  PublicUser is now fully populated from Firebase:
//    - name/bio/photo  → Firebase Auth + workspace_presence
//    - headline/tagline → users/{uid}/portfolio/meta
//    - followers/following → users/{uid}/social (written on follow/unfollow)
//    - isFollowing       → users/{myUid}/following/{uid}
// ─────────────────────────────────────────────────────────────────

data class PublicUser(
    val id: String        = "",
    val name: String      = "",
    val bio: String       = "",
    val photoUrl: String  = "",
    val headline: String  = "",
    val tagline: String   = "",
    val followers: Int    = 0,
    val following: Int    = 0,
    val isFollowing: Boolean = false
)

// Reuse PortfolioItem from PortfolioBuilderScreen (same package path)
// If in a different module, define a local copy:
data class PublicPortfolioItem(
    val id: String        = "",
    val title: String     = "",
    val category: String  = "",
    val description: String = "",
    val imageUrl: String  = "",
    val tags: List<String> = emptyList(),
    val featured: Boolean = false,
    val createdAt: Long   = 0L
)

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPERS
// ─────────────────────────────────────────────────────────────────

private suspend fun fetchPublicUser(uid: String, myUid: String): PublicUser {
    val db = Firebase.database.reference

    // 1. Portfolio meta → headline, tagline
    val metaSnap = runCatching {
        db.child("users").child(uid).child("portfolio").child("meta").get().await()
    }.getOrNull()
    val headline = metaSnap?.child("headline")?.getValue(String::class.java) ?: ""
    val tagline  = metaSnap?.child("tagline")?.getValue(String::class.java)  ?: ""

    // 2. Workspace presence → name, role (bio fallback)
    val presenceSnap = runCatching {
        db.child("workspace_presence").child(uid).get().await()
    }.getOrNull()
    val presenceName = presenceSnap?.child("name")?.getValue(String::class.java) ?: ""
    val presenceRole = presenceSnap?.child("role")?.getValue(String::class.java) ?: ""

    // 3. Social counts → followers, following
    val followersCount = runCatching {
        db.child("users").child(uid).child("social").child("followersCount")
            .get().await().getValue(Int::class.java) ?: 0
    }.getOrElse { 0 }
    val followingCount = runCatching {
        db.child("users").child(uid).child("social").child("followingCount")
            .get().await().getValue(Int::class.java) ?: 0
    }.getOrElse { 0 }

    // 4. Am I following this user?
    val isFollowing = if (myUid.isNotEmpty() && myUid != uid) {
        runCatching {
            db.child("users").child(myUid).child("following").child(uid)
                .get().await().exists()
        }.getOrElse { false }
    } else false

    // 5. Firebase Auth display name / photo for this uid via presence fallback
    //    (Auth user object is only available client-side for the signed-in user,
    //     so for other users we use presence name + tagline as bio)
    val name    = presenceName.ifBlank { "Creator" }
    val bio     = tagline.ifBlank { presenceRole.ifBlank { headline } }

    return PublicUser(
        id          = uid,
        name        = name,
        bio         = bio,
        headline    = headline,
        tagline     = tagline,
        followers   = followersCount,
        following   = followingCount,
        isFollowing = isFollowing
    )
}

private suspend fun fetchPortfolioItems(uid: String): List<PublicPortfolioItem> {
    return runCatching {
        val snap = Firebase.database.reference
            .child("users").child(uid).child("portfolio").child("items").get().await()
        snap.children.mapNotNull { child ->
            val id          = child.key ?: return@mapNotNull null
            val title       = child.child("title").getValue(String::class.java)       ?: ""
            val category    = child.child("category").getValue(String::class.java)    ?: ""
            val description = child.child("description").getValue(String::class.java) ?: ""
            val imageUrl    = child.child("imageUrl").getValue(String::class.java)    ?: ""
            val featured    = child.child("featured").getValue(Boolean::class.java)   ?: false
            val createdAt   = child.child("createdAt").getValue(Long::class.java)     ?: 0L
            @Suppress("UNCHECKED_CAST")
            val tags = (child.child("tags").value as? List<String>) ?: emptyList()
            PublicPortfolioItem(id, title, category, description, imageUrl, tags, featured, createdAt)
        }.sortedByDescending { it.createdAt }
    }.getOrElse { emptyList() }
}

/**
 * Toggle follow/unfollow:
 *  - writes/removes users/{myUid}/following/{uid}
 *  - increments/decrements users/{uid}/social/followersCount
 *  - increments/decrements users/{myUid}/social/followingCount
 */
private suspend fun toggleFollowInDb(myUid: String, targetUid: String, currentlyFollowing: Boolean) {
    val db = Firebase.database.reference
    val followRef      = db.child("users").child(myUid).child("following").child(targetUid)
    val targetCountRef = db.child("users").child(targetUid).child("social").child("followersCount")
    val myCountRef     = db.child("users").child(myUid).child("social").child("followingCount")

    if (currentlyFollowing) {
        followRef.removeValue().await()
        targetCountRef.setValue(ServerValue.increment(-1))
        myCountRef.setValue(ServerValue.increment(-1))
    } else {
        followRef.setValue(true).await()
        targetCountRef.setValue(ServerValue.increment(1))
        myCountRef.setValue(ServerValue.increment(1))
    }
}

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicProfileScreen(
    navController: NavController,
    userId: String = "user_1"
) {
    val isPreview = LocalInspectionMode.current
    val myUid     = if (isPreview) "" else Firebase.auth.currentUser?.uid ?: ""
    val scope     = rememberCoroutineScope()

    var user by remember {
        mutableStateOf(
            if (isPreview) PublicUser(
                id          = "preview",
                name        = "Alex Johnson",
                bio         = "UI/UX Designer crafting modern experiences",
                headline    = "Designer · Creative Force · Visual Storyteller",
                tagline     = "Crafting bold ideas into unforgettable experiences.",
                followers   = 340,
                following   = 180,
                isFollowing = false
            ) else PublicUser()
        )
    }

    var items by remember {
        mutableStateOf(
            if (isPreview) listOf(
                PublicPortfolioItem("1", "Modern UI",     "UI/UX",    "Full design system",    featured = true),
                PublicPortfolioItem("2", "Creative App",  "Mobile",   "Concept app design",    featured = false),
                PublicPortfolioItem("3", "Dashboard UX",  "Dashboard","Analytics dashboard",   featured = true)
            ) else emptyList()
        )
    }

    var isLoading    by remember { mutableStateOf(!isPreview) }
    var isFollowLoading by remember { mutableStateOf(false) }

    // Load from Firebase
    LaunchedEffect(userId) {
        if (isPreview) return@LaunchedEffect
        isLoading = true
        user  = fetchPublicUser(userId, myUid)
        items = fetchPortfolioItems(userId)
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("chat/${user.id}") }) {
                        Icon(Icons.Default.Message, null, tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GoldPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            // ── HERO HEADER ──────────────────────────────────────
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp)
                        .background(Brush.verticalGradient(listOf(GoldPrimary, BackgroundMain)))
                ) {
                    Column(
                        modifier            = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Avatar: photo from Firebase Auth (stored in presence) or initial
                        Box(
                            modifier = Modifier.size(100.dp).clip(CircleShape).background(CardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model              = user.photoUrl,
                                    contentDescription = user.name,
                                    modifier           = Modifier.fillMaxSize(),
                                    contentScale       = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person, null,
                                    tint     = GoldPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        // Headline from portfolio meta
                        if (user.headline.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                user.headline,
                                color    = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Bio / tagline
                        if (user.bio.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                user.bio,
                                color    = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Follow button — hidden when viewing own profile
                        if (userId != myUid) {
                            Button(
                                onClick = {
                                    if (isPreview) {
                                        user = user.copy(
                                            isFollowing = !user.isFollowing,
                                            followers   = if (user.isFollowing) user.followers - 1 else user.followers + 1
                                        )
                                        return@Button
                                    }
                                    isFollowLoading = true
                                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        toggleFollowInDb(myUid, userId, user.isFollowing)
                                        val updated = fetchPublicUser(userId, myUid)
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            user = updated
                                            isFollowLoading = false
                                        }
                                    }
                                },
                                enabled = !isFollowLoading,
                                shape  = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isFollowing) CardBackground else GoldPrimary
                                )
                            ) {
                                if (isFollowLoading) {
                                    CircularProgressIndicator(color = DarkSurface, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        if (user.isFollowing) "Following" else "Follow",
                                        color = if (user.isFollowing) TextPrimary else TextOnDark
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── STATS ────────────────────────────────────────────
            item {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileStatItem("Projects",  items.size.toString())
                    ProfileStatItem("Followers", user.followers.toString())
                    ProfileStatItem("Following", user.following.toString())
                }
            }

            // ── TAGLINE CARD ─────────────────────────────────────
            if (user.tagline.isNotBlank() && user.tagline != user.bio) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier          = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FormatQuote, null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(user.tagline, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
                        }
                    }
                }
            }

            // ── PORTFOLIO HEADER ─────────────────────────────────
            item {
                Text(
                    if (items.isEmpty()) "No portfolio yet" else "Portfolio",
                    modifier   = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                    color      = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── PORTFOLIO ITEMS (from DB) ─────────────────────────
            items(items, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { navController.navigate("project_detail/${item.id}") },
                    shape  = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column {
                        // Image from Cloudinary URL or placeholder
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .background(DarkCard)
                        ) {
                            if (item.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model              = item.imageUrl,
                                    contentDescription = item.title,
                                    modifier           = Modifier.fillMaxSize(),
                                    contentScale       = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                        .background(Brush.linearGradient(listOf(DarkCard, DarkSurface))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, null, tint = Color(0xFF3A3A3A), modifier = Modifier.size(40.dp))
                                }
                            }

                            // Category badge
                            if (item.category.isNotBlank()) {
                                Box(
                                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                                        .clip(RoundedCornerShape(8.dp)).background(DarkSurface.copy(0.85f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(item.category, color = GoldAccent, fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                            }

                            // Featured star
                            if (item.featured) {
                                Box(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                                        .clip(CircleShape).background(GoldPrimary).padding(6.dp)
                                ) {
                                    Icon(Icons.Default.Star, null, tint = DarkSurface, modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title.ifEmpty { "Untitled" }, color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (item.description.isNotBlank()) {
                                    Text(item.description, color = TextSecondary, fontSize = 12.sp,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Icon(Icons.Default.ArrowForward, null, tint = GoldPrimary)
                        }

                        // Tags
                        if (item.tags.isNotEmpty()) {
                            Row(
                                modifier              = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item.tags.take(3).forEach { tag ->
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                            .background(GoldPrimary.copy(0.08f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("#$tag", color = GoldPrimary, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  STAT ITEM
// ─────────────────────────────────────────────────────────────────
@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────
@Preview(showSystemUi = true)
@Composable
fun PreviewPublicProfile() {
    PublicProfileScreen(rememberNavController())
}