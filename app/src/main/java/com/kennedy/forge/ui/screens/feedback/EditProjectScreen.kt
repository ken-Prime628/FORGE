package com.kennedy.forge.ui.screens.feedback

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kennedy.forge.R
import com.kennedy.forge.navigation.ROUT_ProjectDetail
import com.kennedy.forge.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProjectScreen(navController: NavController) {
    var title by remember { mutableStateOf("Modern UI Design") }
    var description by remember { mutableStateOf("A sleek modern UI design for mobile apps.") }
    var imageUrl by remember { mutableStateOf("https://via.placeholder.com/400") }

    // Simulated project data (Later, you will use this data to update via a database API)
    var updatedTitle by remember { mutableStateOf(title) }
    var updatedDescription by remember { mutableStateOf(description) }
    var updatedImageUrl by remember { mutableStateOf(imageUrl) }

    // Update Project Button
    fun onSave() {
        // Logic to save the project details
        title = updatedTitle
        description = updatedDescription
        imageUrl = updatedImageUrl
    }

    Scaffold(
        containerColor = BackgroundMain,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Project", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ROUT_ProjectDetail) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BackgroundMain)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Project Image Preview and Edit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
                    .clickable {
                        // Simulate opening image picker
                        updatedImageUrl = "https://via.placeholder.com/400" // This would be replaced with image picker logic
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_bg),
                    contentDescription = "Project Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Project Title
            OutlinedTextField(
                value = updatedTitle,
                onValueChange = { updatedTitle = it },
                label = { Text("Project Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Project Description
            OutlinedTextField(
                value = updatedDescription,
                onValueChange = { updatedDescription = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Image URL (for simplicity, or file picker could be integrated later)
            OutlinedTextField(
                value = updatedImageUrl,
                onValueChange = { updatedImageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Save Changes Button
            Button(
                onClick = {
                    onSave()
                    navController.popBackStack()  // Navigate back after save
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("Save Changes", color = TextOnDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun EditProjectScreenPreview() {
    EditProjectScreen(navController = rememberNavController())
}