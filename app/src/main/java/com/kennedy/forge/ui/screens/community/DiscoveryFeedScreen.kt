package com.kennedy.forge.ui.screens.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.tasks.await
import com.kennedy.forge.navigation.ROUT_SubmitWork
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────
//  MODELS
//  FeedEntry combines a published PortfolioItem with its owner's
//  public identity so each card can show real user info.
// ─────────────────────────────────────────────────────────────────

data class FeedProject(
    val id: Int,
    val title: String,
    val creator: String,
    val image: Int,
    val rating: Int,
    val liked: Boolean = false
)

data class FeedEntry(
    // Portfolio item fields
    val itemId: String       = "",
    val ownerUid: String     = "",
    val ownerName: String    = "",
    val ownerPhotoUrl: String = "",
    val title: String        = "",
    val category: String     = "",
    val description: String  = "",
    val imageUrl: String     = "",
    val tags: List<String>   = emptyList(),
    val featured: Boolean    = false,
    val createdAt: Long      = 0L,
    // Interaction state
    val liked: Boolean       = false,
    val likeCount: Int       = 0
)

// ─────────────────────────────────────────────────────────────────
//  FIREBASE HELPERS
// ─────────────────────────────────────────────────────────────────

/**
 * Loads all *published* portfolios then flattens their items into a
 * unified feed, enriched with the owner's name from workspace_presence.
 *
 * Firebase path layout relied upon:
 *   users/{uid}/portfolio/meta        → isPublished, headline
 *   users/{uid}/portfolio/items/{id}  → PortfolioItem fields
 *   workspace_presence/{uid}          → name, role
 *   feed_likes/{ownerUid}_{itemId}/{myUid} → exists() = liked by me
 */
private suspend fun loadFeedEntries(myUid: String): List<FeedEntry> {
    val db = Firebase.database.reference

    // 1. Fetch all user nodes that have a published portfolio
    val usersSnap = runCatching { db.child("users").get().await() }
        .getOrNull() ?: return emptyList()

    val entries = mutableListOf<FeedEntry>()

    for (userNode in usersSnap.children) {
        val uid = userNode.key ?: continue

        // Check portfolio is published
        val isPublished = userNode.child("portfolio").child("meta")
            .child("isPublished").getValue(Boolean::class.java) ?: false
        if (!isPublished) continue

        // Fetch owner name from workspace_presence (most up-to-date)
        val presenceSnap = runCatching {
            db.child("workspace_presence").child(uid).get().await()
        }.getOrNull()
        val ownerName     = presenceSnap?.child("name")?.getValue(String::class.java)
            ?: userNode.child("portfolio").child("meta")
                .child("uid").getValue(String::class.java) ?: "Creator"
        val ownerPhotoUrl = presenceSnap?.child("photoUrl")?.getValue(String::class.java) ?: ""

        // Iterate portfolio items
        val itemsNode = userNode.child("portfolio").child("items")
        for (itemSnap in itemsNode.children) {
            val itemId      = itemSnap.key ?: continue
            val title       = itemSnap.child("title").getValue(String::class.java)       ?: continue
            val category    = itemSnap.child("category").getValue(String::class.java)    ?: ""
            val description = itemSnap.child("description").getValue(String::class.java) ?: ""
            val imageUrl    = itemSnap.child("imageUrl").getValue(String::class.java)    ?: ""
            val featured    = itemSnap.child("featured").getValue(Boolean::class.java)   ?: false
            val createdAt   = itemSnap.child("createdAt").getValue(Long::class.java)     ?: 0L
            @Suppress("UNCHECKED_CAST")
            val tags        = (itemSnap.child("tags").value as? List<String>) ?: emptyList()

            // Like state for this viewer
            val likeKey   = "${uid}_${itemId}"
            val likeSnap  = runCatching {
                db.child("feed_likes").child(likeKey).get().await()
            }.getOrNull()
            val likeCount = likeSnap?.childrenCount?.toInt() ?: 0
            val liked     = if (myUid.isNotEmpty()) likeSnap?.child(myUid)?.exists() ?: false else false

            entries += FeedEntry(
                itemId        = itemId,
                ownerUid      = uid,
                ownerName     = ownerName,
                ownerPhotoUrl = ownerPhotoUrl,
                title         = title,
                category      = category,
                description   = description,
                imageUrl      = imageUrl,
                tags          = tags,
                featured      = featured,
                createdAt     = createdAt,
                liked         = liked,
                likeCount     = likeCount
            )
        }
    }

    return entries.sortedByDescending { it.createdAt }
}

/**
 * Toggle like: writes/removes feed_likes/{ownerUid}_{itemId}/{myUid}
 */
private suspend fun toggleFeedLike(
    myUid: String,
    ownerUid: String,
    itemId: String,
    currentlyLiked: Boolean
) {
    if (myUid.isEmpty()) return
    val likeKey = "${ownerUid}_${itemId}"
    val ref = Firebase.database.reference.child("feed_likes").child(likeKey).child(myUid)
    if (currentlyLiked) ref.removeValue().await()
    else ref.setValue(true).await()
}

// ─────────────────────────────────────────────────────────────────
//  MAIN SCREEN
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryFeedScreen(navController: NavController) {

    val isPreview = LocalInspectionMode.current
    val myUid     = if (isPreview) "" else Firebase.auth.currentUser?.uid ?: ""
    val scope     = rememberCoroutineScope()

    var entries   by remember {
        mutableStateOf(
            if (isPreview) listOf(
                FeedEntry("1", "uid1", "Alex",   "", "Modern UI Design",    "UI/UX",   "Full design system",   "", listOf("ui","design"), true,  0L, false, 12),
                FeedEntry("2", "uid2", "Jordan", "", "Mobile App Concept",  "Mobile",  "Concept app design",   "", listOf("mobile"),       false, 0L, true,  5),
                FeedEntry("3", "uid3", "Sam",    "", "Dashboard Analytics", "Data",    "Real-time dashboard",  "", listOf("data","ux"),    false, 0L, false, 3)
            ) else emptyList()
        )
    }
    var isLoading by remember { mutableStateOf(!isPreview) }

    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        isLoading = true
        entries   = loadFeedEntries(myUid)
        isLoading = false
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title   = { Text("Discover", color = TextPrimary) },
                actions = {
                    IconButton(onClick = { navController.navigate(ROUT_SubmitWork) }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
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
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            if (entries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Explore, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No published projects yet", color = TextSecondary)
                            Text("Be the first to publish your portfolio!", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            items(entries, key = { "${it.ownerUid}_${it.itemId}" }) { entry ->
                FeedEntryCard(
                    entry          = entry,
                    onLike         = {
                        // Optimistic update
                        entries = entries.map {
                            if (it.itemId == entry.itemId && it.ownerUid == entry.ownerUid) {
                                it.copy(
                                    liked     = !it.liked,
                                    likeCount = if (it.liked) it.likeCount - 1 else it.likeCount + 1
                                )
                            } else it
                        }
                        if (!isPreview) {
                            scope.launch {
                                toggleFeedLike(myUid, entry.ownerUid, entry.itemId, entry.liked)
                            }
                        }
                    },
                    onProjectClick = {
                        navController.navigate("project_detail/${entry.itemId}")
                    },
                    // ✅ Navigate to public profile screen with real uid
                    onUserClick    = {
                        navController.navigate("public_profile/${entry.ownerUid}")
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  FEED ENTRY CARD  (UI unchanged from original FeedCard)
// ─────────────────────────────────────────────────────────────────

@Composable
fun FeedEntryCard(
    entry: FeedEntry,
    onLike: () -> Unit,
    onProjectClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column {

            // ── Project image ──────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(DarkCard)
                    .clickable { onProjectClick() }
            ) {
                if (entry.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model              = entry.imageUrl,
                        contentDescription = entry.title,
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
                if (entry.category.isNotBlank()) {
                    Box(
                        modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                            .clip(RoundedCornerShape(8.dp)).background(DarkSurface.copy(0.85f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(entry.category, color = GoldAccent, fontSize = 10.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }

                // Featured badge
                if (entry.featured) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                            .clip(CircleShape).background(GoldPrimary).padding(6.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = DarkSurface, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {

                // ── Title ──────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        entry.title,
                        color      = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                }

                if (entry.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(entry.description, color = TextSecondary, fontSize = 12.sp,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }

                Spacer(Modifier.height(8.dp))

                // ── Owner row — clicking navigates to public profile ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.clickable { onUserClick() }   // ✅ public profile nav
                ) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(GoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (entry.ownerPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model              = entry.ownerPhotoUrl,
                                contentDescription = entry.ownerName,
                                modifier           = Modifier.fillMaxSize(),
                                contentScale       = ContentScale.Crop
                            )
                        } else {
                            Text(
                                entry.ownerName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color      = TextOnDark,
                                fontSize   = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(entry.ownerName, color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    // ✅ Explicit icon button navigating to public profile
                    IconButton(
                        onClick  = { onUserClick() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "View ${entry.ownerName}'s profile",
                            tint     = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Tags ───────────────────────────────────────
                if (entry.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        entry.tags.take(3).forEach { tag ->
                            Box(
                                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                    .background(GoldPrimary.copy(0.08f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("#$tag", color = GoldPrimary, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // ── Like row ───────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Like button + count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onLike) {
                            Icon(
                                imageVector = if (entry.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (entry.liked) GoldPrimary else TextSecondary
                            )
                        }
                        if (entry.likeCount > 0) {
                            Text(entry.likeCount.toString(), color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    // View project arrow
                    IconButton(onClick = onProjectClick) {
                        Icon(Icons.Default.ArrowForward, null, tint = GoldPrimary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  PREVIEW
// ─────────────────────────────────────────────────────────────────
@Preview(showSystemUi = true)
@Composable
fun PreviewDiscovery() {
    DiscoveryFeedScreen(rememberNavController())
}