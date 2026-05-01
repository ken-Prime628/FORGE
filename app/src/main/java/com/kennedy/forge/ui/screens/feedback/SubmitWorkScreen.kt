package com.kennedy.forge.ui.screens.feedback

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.kennedy.forge.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitWorkScreen(navController: NavController){

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    ///////////////////////////////////////////////////////
    // 🔥 FILE PICKERS
    ///////////////////////////////////////////////////////

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Scaffold(
        containerColor = BackgroundMain,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Submit Work", color = TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundMain
                )
            )
        },

        ///////////////////////////////////////////////////////
        // 🔥 BOTTOM NAV (with Home working)
        ///////////////////////////////////////////////////////
        bottomBar = {
            NavigationBar(containerColor = CardBackground) {

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = false,
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, null, tint = GoldPrimary) },
                    selected = true,
                    onClick = {}
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    selected = false,
                    onClick = {}
                )
            }
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ///////////////////////////////////////////////////////
            // IMAGE PREVIEW
            ///////////////////////////////////////////////////////
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { galleryLauncher.launch("image/*") },
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {

                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, null, tint = GoldPrimary)
                                Text("Tap to select image", color = TextPrimary)
                            }
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // UPLOAD OPTIONS
            ///////////////////////////////////////////////////////
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.Photo, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery")
                    }

                    Button(
                        onClick = { fileLauncher.launch("*/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                    ) {
                        Icon(Icons.Default.AttachFile, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Files", color = TextPrimary)
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // TITLE FIELD
            ///////////////////////////////////////////////////////
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Title of your work") },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            ///////////////////////////////////////////////////////
            // DESCRIPTION
            ///////////////////////////////////////////////////////
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Describe your work...") },
                    shape = RoundedCornerShape(16.dp),
                    minLines = 4
                )
            }

            ///////////////////////////////////////////////////////
            // SUBMIT BUTTON
            ///////////////////////////////////////////////////////
            item {
                Button(
                    onClick = {
                        navController.navigate("feedback_dashboard")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = TextOnDark
                    )
                ) {
                    Text("Submit for Feedback")
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun SubmitWorkScreenPreview(){
    SubmitWorkScreen(rememberNavController())
}