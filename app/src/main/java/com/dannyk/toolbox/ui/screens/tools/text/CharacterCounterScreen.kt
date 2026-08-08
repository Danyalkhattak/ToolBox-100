package com.dannyk.toolbox.ui.screens.tools.text

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider

data class CharFrequency(val char: String, val count: Int, val percentage: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCounterScreen(navController: NavHostController) {
    var text by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Calculate character statistics
    val totalChars = remember(text) { text.length }
    
    val charsNoSpaces = remember(text) { 
        text.replace(" ", "").replace("\n", "").replace("\t", "").length 
    }

    val letterCount = remember(text) { 
        text.count { it.isLetter() }
    }

    val upperCaseCount = remember(text) { 
        text.count { it.isUpperCase() }
    }

    val lowerCaseCount = remember(text) { 
        text.count { it.isLowerCase() }
    }

    val digitCount = remember(text) { 
        text.count { it.isDigit() }
    }

    val whitespaceCount = remember(text) { 
        text.count { it.isWhitespace() }
    }

    val specialCharCount = remember(text) { 
        text.count { !it.isLetterOrDigit() && !it.isWhitespace() }
    }

    // Character frequency analysis
    val charFrequencies: List<CharFrequency> = remember(text) {
        if (text.isEmpty()) emptyList()
        else {
            val frequencyMap = mutableMapOf<String, Int>()
            text.forEach { char ->
                val key = when {
                    char == ' ' -> "Space"
                    char == '\n' -> "Newline"
                    char == '\t' -> "Tab"
                    else -> char.toString()
                }
                frequencyMap[key] = frequencyMap.getOrDefault(key, 0) + 1
            }
            frequencyMap.entries
                .sortedByDescending { it.value }
                .take(20)
                .map { (char, count) ->
                    CharFrequency(char, count, (count.toFloat() / totalChars * 100))
                }
        }
    }

    // Letter frequency (letters only)
    val letterFrequencies: List<CharFrequency> = remember(text) {
        if (text.isEmpty()) emptyList()
        else {
            val letterMap = mutableMapOf<Char, Int>()
            text.filter { it.isLetter() }.forEach { char ->
                val upperChar = char.uppercaseChar()
                letterMap[upperChar] = letterMap.getOrDefault(upperChar, 0) + 1
            }
            letterMap.entries
                .sortedByDescending { it.value }
                .map { (char, count) ->
                    CharFrequency(char.toString(), count, 
                        if (letterCount > 0) (count.toFloat() / letterCount * 100) else 0f)
                }
        }
    }

    // Unicode category breakdown
    val unicodeCategories = remember(text) {
        val categories = mutableMapOf<String, Int>()
        text.forEach { char ->
            val category = when {
                char.isLetter() -> "Letters"
                char.isDigit() -> "Digits"
                char.isWhitespace() -> "Whitespace"
                char in "..,;:!?\"'()[]{}" -> "Punctuation"
                char in "@#$%^&*+=|\\/<>" -> "Symbols"
                else -> "Other (${char.code})"
            }
            categories[category] = categories.getOrDefault(category, 0) + 1
        }
        categories.toList().sortedByDescending { it.second }
    }

    Scaffold(
        topBar = { ToolTopBar("Character Counter", navController) }
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
                text = "Enter text to analyze characters:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp),
                placeholder = { Text("Type or paste your text here...") },
                textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Divider(modifier = Modifier.fillMaxWidth())

            // Overview Stats Card
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatRowItem("Total Characters", totalChars.toString())
                    StatRowItem("Characters (no spaces)", charsNoSpaces.toString())
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(letterCount.toString(), style = MaterialTheme.typography.titleLarge, 
                                color = MaterialTheme.colorScheme.primary)
                            Text("Letters", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row {
                                ChipLabel("$upperCaseCount upper")
                                Spacer(Modifier.width(4.dp))
                                ChipLabel("$lowerCaseCount lower")
                            }
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(digitCount.toString(), style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary)
                            Text("Digits", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(specialCharCount.toString(), style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.tertiary)
                            Text("Special Chars", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    
                    StatRowItem("Whitespace", whitespaceCount.toString())
                }
            }

            // Unicode Category Breakdown
            if (unicodeCategories.isNotEmpty()) {
                Text(
                    text = "Character Categories",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        unicodeCategories.forEach { (category, count) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.width(150.dp)
                                )
                                LinearProgressIndicator(
                                    progress = { count.toFloat() / totalChars },
                                    modifier = Modifier.weight(1f).height(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "$count (${(count * 100 / totalChars)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    minWidth = 80.dp
                                )
                            }
                        }
                    }
                }
            }

            // Top Characters Frequency
            if (charFrequencies.isNotEmpty()) {
                Text(
                    text = "Top Characters (by frequency)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(charFrequencies) { freq ->
                            FrequencyRow(freq)
                        }
                    }
                }
            }

            // Letter Distribution (A-Z)
            if (letterFrequencies.isNotEmpty()) {
                Text(
                    text = "Letter Distribution (A-Z)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        letterFrequencies.forEach { freq ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "'${freq.char}'",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.width(40.dp)
                                )
                                LinearProgressIndicator(
                                    progress = { freq.percentage / 100f },
                                    modifier = Modifier.weight(1f).height(6.dp),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${freq.count}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(40.dp)
                                )
                                Text(
                                    text = String.format("%.1f%%", freq.percentage),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(50.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
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
            }
        }
    }
}

@Composable
private fun StatRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChipLabel(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun FrequencyRow(freq: CharFrequency) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Display character
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (freq.char.length > 1 && freq.char != "Space" && 
                             freq.char != "Newline" && freq.char != "Tab") "?" 
                          else if (freq.char == "Space") "␣" 
                          else if (freq.char == "Newline") "↵" 
                          else if (freq.char == "Tab") "⇥" 
                          else freq.char,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Progress bar
        LinearProgressIndicator(
            progress = { freq.percentage / 100f },
            modifier = Modifier.weight(1f).height(8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.width(12.dp))
        
        // Count and percentage
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${freq.count}x",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format("%.2f%%", freq.percentage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
