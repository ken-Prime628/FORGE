package com.kennedy.forge.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

///////////////////////////////////////////////////////////
// MODELS (DB READY)
///////////////////////////////////////////////////////////

data class SearchUser(val name: String)
data class SearchProject(val title: String)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {

    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("All") }

    ///////////////////////////////////////////////////////
    // 🔥 REALISTIC (EMPTY FIRST TIME)
    ///////////////////////////////////////////////////////
    var recentSearches by remember {
        mutableStateOf(listOf<String>())
    }

    ///////////////////////////////////////////////////////
    // TEMP DATA (REPLACE WITH DB LATER)
    ///////////////////////////////////////////////////////
    val users = listOf(
        SearchUser("Alex Johnson"),
        SearchUser("Jordan Smith"),
        SearchUser("Samuel Lee")
    )

    val projects = listOf(
        SearchProject("Modern UI Design"),
        SearchProject("Creative Dashboard"),
        SearchProject("Mobile App Concept")
    )

    ///////////////////////////////////////////////////////
    // FILTER LOGIC
    ///////////////////////////////////////////////////////
    val filteredUsers = users.filter {
        it.name.contains(query, ignoreCase = true)
    }

    val filteredProjects = projects.filter {
        it.title.contains(query, ignoreCase = true)
    }

    ///////////////////////////////////////////////////////
    // SAVE SEARCH (REALISTIC)
    ///////////////////////////////////////////////////////
    fun saveSearch(text: String) {
        if (text.isNotBlank() && !recentSearches.contains(text)) {
            recentSearches = listOf(text) + recentSearches.take(4)
        }
    }

    ///////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////

    Scaffold(
        containerColor = BackgroundMain,

        /////////////////////////////////////////////
        // TOP BAR (SEARCH + FILTERS)
        /////////////////////////////////////////////
        topBar = {

            Column {

                ///////////////////////////////////////////////////////
                // 🔥 PREMIUM SEARCH BAR
                ///////////////////////////////////////////////////////
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(Icons.Default.Search, null, tint = GoldPrimary)

                        Spacer(Modifier.width(8.dp))

                        TextField(
                            value = query,
                            onValueChange = {
                                query = it
                            },
                            placeholder = {
                                Text("Search projects or users...", color = TextSecondary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CardBackground,
                                unfocusedContainerColor = CardBackground,
                                disabledContainerColor = CardBackground,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }

                ///////////////////////////////////////////////////////
                // FILTER CHIPS
                ///////////////////////////////////////////////////////
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    listOf("All", "Projects", "Users").forEach { tab ->
                        FilterChip(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    tab,
                                    color = if (selectedTab == tab) TextOnDark else TextPrimary
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                containerColor = CardBackground
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {

            ///////////////////////////////////////////////////////
            // 🔥 FIRST TIME STATE OR RECENT SEARCHES
            ///////////////////////////////////////////////////////
            if (query.isEmpty()) {

                if (recentSearches.isEmpty()) {

                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(48.dp)
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    "Start exploring",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "Search for projects or creators",
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                } else {

                    item { SearchSectionTitle("Recent Searches") }

                    items(recentSearches) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { query = item }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, null, tint = TextSecondary)

                            Spacer(Modifier.width(8.dp))

                            Text(item, color = TextSecondary)

                            Spacer(Modifier.weight(1f))

                            IconButton(onClick = {
                                recentSearches = recentSearches.filter { it != item }
                            }) {
                                Icon(Icons.Default.Close, null, tint = TextSecondary)
                            }
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // PROJECT RESULTS
            ///////////////////////////////////////////////////////
            if ((selectedTab == "All" || selectedTab == "Projects") && query.isNotEmpty()) {

                item { SearchSectionTitle("Projects") }

                items(filteredProjects) { project ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable {
                                saveSearch(query)
                                navController.navigate("project_detail")
                            },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Work, null, tint = GoldPrimary)
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(project.title, color = TextPrimary)
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // USER RESULTS
            ///////////////////////////////////////////////////////
            if ((selectedTab == "All" || selectedTab == "Users") && query.isNotEmpty()) {

                item { SearchSectionTitle("Users") }

                items(filteredUsers) { user ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                saveSearch(query)
                                navController.navigate("public_profile")
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.name.first().toString(),
                                color = TextOnDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(user.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Creative • Designer", color = TextSecondary)
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // EMPTY STATE
            ///////////////////////////////////////////////////////
            if (query.isNotEmpty() &&
                filteredUsers.isEmpty() &&
                filteredProjects.isEmpty()
            ) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found", color = TextSecondary)
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// SECTION TITLE
///////////////////////////////////////////////////////////
@Composable
fun SearchSectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(16.dp),
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun SearchScreenPreview() {
    SearchScreen(rememberNavController())
}