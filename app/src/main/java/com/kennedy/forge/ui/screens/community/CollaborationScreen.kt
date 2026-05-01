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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.kennedy.forge.ui.theme.*

///////////////////////////////////////////////////////////
// MODELS
///////////////////////////////////////////////////////////

data class Collaborator(
    val name: String
)

data class Task(
    val id: Int,
    val title: String,
    var completed: Boolean = false
)

data class Message(
    val sender: String,
    val text: String
)

///////////////////////////////////////////////////////////
// MAIN SCREEN
///////////////////////////////////////////////////////////

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaborationScreen(navController: NavController) {

    ///////////////////////////////////////////////////////
    // STATE (DYNAMIC — DB READY)
    ///////////////////////////////////////////////////////
    var collaborators by remember {
        mutableStateOf(
            listOf(
                Collaborator("Alex"),
                Collaborator("Jordan"),
                Collaborator("You")
            )
        )
    }

    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Design UI screens"),
                Task(2, "Implement navigation"),
                Task(3, "Connect database")
            )
        )
    }

    var messages by remember {
        mutableStateOf(
            listOf(
                Message("Alex", "This UI looks clean 🔥"),
                Message("Jordan", "We should improve animations")
            )
        )
    }

    var newTask by remember { mutableStateOf("") }
    var newMessage by remember { mutableStateOf("") }

    ///////////////////////////////////////////////////////
    // UI
    ///////////////////////////////////////////////////////

    Scaffold(
        containerColor = BackgroundMain,

        /////////////////////////////////////////////
        // TOP BAR
        /////////////////////////////////////////////
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Collaboration", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
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
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            ///////////////////////////////////////////////////////
            // 🔥 HEADER (TEAM SPACE FEEL)
            ///////////////////////////////////////////////////////
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(GoldPrimary, BackgroundMain)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Team Workspace",
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ///////////////////////////////////////////////////////
            // 👥 COLLABORATORS
            ///////////////////////////////////////////////////////
            item {
                SectionTitle("Team Members")
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    collaborators.forEach {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    it.name.first().toString(),
                                    color = TextOnDark
                                )
                            }

                            Text(it.name, color = TextSecondary)
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // ✅ TASKS
            ///////////////////////////////////////////////////////
            item {
                SectionTitle("Tasks")
            }

            items(tasks, key = { it.id }) { task ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Checkbox(
                            checked = task.completed,
                            onCheckedChange = {
                                tasks = tasks.map {
                                    if (it.id == task.id)
                                        it.copy(completed = !it.completed)
                                    else it
                                }
                            }
                        )

                        Text(
                            task.title,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(onClick = {
                            tasks = tasks.filter { it.id != task.id }
                        }) {
                            Icon(Icons.Default.Delete, null, tint = TextSecondary)
                        }
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // ➕ ADD TASK
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    OutlinedTextField(
                        value = newTask,
                        onValueChange = { newTask = it },
                        placeholder = { Text("Add new task...") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = {
                        if (newTask.isNotBlank()) {
                            tasks = tasks + Task(tasks.size + 1, newTask)
                            newTask = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, null, tint = GoldPrimary)
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // 💬 CHAT
            ///////////////////////////////////////////////////////
            item {
                SectionTitle("Team Chat")
            }

            items(messages) { msg ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = if (msg.sender == "You")
                        Arrangement.End else Arrangement.Start
                ) {

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (msg.sender == "You") GoldPrimary else CardBackground
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            msg.text,
                            color = if (msg.sender == "You") TextOnDark else TextPrimary
                        )
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // ➕ SEND MESSAGE
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        placeholder = { Text("Send message...") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    IconButton(onClick = {
                        if (newMessage.isNotBlank()) {
                            messages = messages + Message("You", newMessage)
                            newMessage = ""
                        }
                    }) {
                        Icon(Icons.Default.Send, null, tint = GoldPrimary)
                    }
                }
            }

            ///////////////////////////////////////////////////////
            // VIDEO CALL BUTTON (NEW)
            ///////////////////////////////////////////////////////
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            // Navigate to video call screen (integration to come)
                        },
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.VideoCall, null, tint = TextOnDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Video Call", color = TextOnDark)
                    }
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////
// REUSABLE TITLE
///////////////////////////////////////////////////////////
@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        color = TextPrimary,
        fontWeight = FontWeight.Bold
    )
}

///////////////////////////////////////////////////////////
// PREVIEW
///////////////////////////////////////////////////////////
@Preview(showSystemUi = true)
@Composable
fun PreviewCollab() {
    CollaborationScreen(rememberNavController())
}