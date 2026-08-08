package com.dannyk.toolbox.ui.screens.tools.text

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.foundation.ScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordCounterScreen(navController: NavHostController) {
    var text by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Calculate statistics in real-time
    val wordCount = remember(text) {
        if (text.isBlank()) 0 else text.trim().split(Regex("\\s+")).size
    }

    val charCount = remember(text) { text.length }

    val charCountNoSpaces = remember(text) {
        text.replace(" ", "").replace("\n", "").replace("\t", "").length
    }

    val sentenceCount = remember(text) {
        if (text.isBlank()) 0 
        else {
            val sentences = text.split(Regex("[.!?]+"))
                .filter { it.trim().isNotEmpty() }.size
            sentences.coerceAtLeast(if (text.trim().isNotEmpty()) 1 else 0)
        }
    }

    val paragraphCount = remember(text) {
        if (text.isBlank()) 0 
        else text.split(Regex("\\n\\s*\\n"))
            .filter { it.trim().isNotEmpty() }.size.coerceAtLeast(1)
    }

    val avgWordLength = remember(text) {
        if (wordCount == 0) 0.0
        else {
            val words = text.trim().split(Regex("\\s+"))
            words.map { it.replace(Regex("[^a-zA-Z]"), "").length }
                .average()
        }
    }

    // Reading time estimate: average 200-250 words per minute for adults
    val readingTimeMinutes = remember(wordCount) {
        if (wordCount == 0) "0"
        else {
            val minutes = wordCount / 200.0
            when {
                minutes < 1 -> "< 1 min"
                minutes < 2 -> "~ 1 min"
                else -> "~ ${minutes.toInt()} min"
            }
        }
    }

    val speakingTimeMinutes = remember(wordCount) {
        if (wordCount == 0) "0"
        else {
            val minutes = wordCount / 150.0 // Average speaking rate
            when {
                minutes < 1 -> "< 1 min"
                minutes < 2 -> "~ 1 min"
                else -> "~ ${minutes.toInt()} min"
            }
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Word Counter") { navController.navigateUp() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Area
            Text(
                text = "Enter or paste your text below:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp, max = 300.dp),
                placeholder = { Text("Start typing or paste your text here...") },
                textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Divider(modifier = Modifier.fillMaxWidth())

            // Statistics Grid
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Primary Stats Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Words", wordCount.toString(), MaterialTheme.colorScheme.primary)
                    StatItem("Characters", charCount.toString(), MaterialTheme.colorScheme.secondary)
                    StatItem("Sentences", sentenceCount.toString(), MaterialTheme.colorScheme.tertiary)
                }
            }

            // Secondary Stats
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatRow("Characters (no spaces)", charCountNoSpaces.toString())
                    StatRow("Paragraphs", paragraphCount.toString())
                    StatRow("Average Word Length", String.format("%.1f chars", avgWordLength))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Time Estimates
                    Text(
                        text = "Time Estimates",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatRow("Reading Time", readingTimeMinutes)
                    StatRow("Speaking Time", speakingTimeMinutes)
                }
            }

            // Detailed Breakdown
            if (wordCount > 0) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Text Density",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        LinearProgressIndicator(
                            progress = { 
                                if (charCount > 0) charCountNoSpaces.toFloat() / charCount 
                                else 0f 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Content density: ${if (charCount > 0) ((charCountNoSpaces * 100) / charCount) else 0}% meaningful characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { text = "" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { /* Copy stats could be implemented */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Copy All")
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
