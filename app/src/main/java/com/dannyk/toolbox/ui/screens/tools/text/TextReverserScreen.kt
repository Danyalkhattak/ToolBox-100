package com.dannyk.toolbox.ui.screens.tools.text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider

enum class ReverseMode {
    REVERSE_CHARS,      // Reverse all characters
    REVERSE_WORDS,      // Reverse word order
    REVERSE_EACH_WORD,  // Reverse characters in each word
    REVERSE_LINES       // Reverse line order
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextReverserScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(ReverseMode.REVERSE_CHARS) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var copiedResult by remember { mutableStateOf(false) }

    // Calculate result based on selected mode
    val result: String = remember(inputText, selectedMode) {
        when (selectedMode) {
            ReverseMode.REVERSE_CHARS -> reverseCharacters(inputText)
            ReverseMode.REVERSE_WORDS -> reverseWordOrder(inputText)
            ReverseMode.REVERSE_EACH_WORD -> reverseEachWord(inputText)
            ReverseMode.REVERSE_LINES -> reverseLines(inputText)
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Text Reverser", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Text(
                text = "Enter text to reverse:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { 
                    Text("Type or paste your text here...\n\nExample: Hello World") 
                },
                textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace),
                minLines = 4,
                maxLines = 8,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            HorizontalDivider()

            // Mode Selection
            Text(
                text = "Reverse Mode",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            // Mode selection chips/segmented button
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReverseMode.entries.forEach { mode ->
                    Surface(
                        onClick = { selectedMode = mode },
                        shape = MaterialTheme.shapes.medium,
                        color = if (selectedMode == mode) 
                            MaterialTheme.colorScheme.primaryContainer else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getModeTitle(mode),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = getModeDescription(mode),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Preview of what it does (small example)
                            if (inputText.isNotEmpty()) {
                                Text(
                                    text = when (mode) {
                                        ReverseMode.REVERSE_CHARS -> "dlroW olleH"
                                        ReverseMode.REVERSE_WORDS -> "World Hello"
                                        ReverseMode.REVERSE_EACH_WORD -> "olleH dlroW"
                                        ReverseMode.REVERSE_LINES -> "↕️ Lines"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // Result Section
            Text(
                text = "Result",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Result header with copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${getModeTitle(mode = selectedMode)} Output",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "${result.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        
                        Spacer(Modifier.width(8.dp))
                        
                        FilledTonalButton(
                            onClick = {
                                copyToClipboard(context, result)
                                copiedResult = true
                            },
                            enabled = result.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedResult) 
                                    androidx.compose.material.icons.Icons.Default.Check else 
                                    androidx.compose.material.icons.Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (copiedResult) "Copied!" else "Copy")
                        }
                    }
                    
                    // Result display
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (result.isEmpty()) "(no output)" else result,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.padding(14.dp),
                            color = if (result.isEmpty()) 
                                MaterialTheme.colorScheme.outline else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Statistics about transformation
                    if (inputText.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatChip("Original", "${inputText.length} chars")
                            StatChip("Result", "${result.length} chars")
                            if (inputText.contains("\n")) {
                                val lineCount = inputText.split("\n").size
                                StatChip("Lines", "$lineCount")
                            }
                        }
                    }
                }
            }

            // All Results Preview (showing all modes at once)
            if (inputText.isNotBlank() && inputText.length <= 100) {
                HorizontalDivider()
                
                Text(
                    text = "All Modes Preview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ReverseMode.entries.forEach { mode ->
                            val modeResult = when (mode) {
                                ReverseMode.REVERSE_CHARS -> reverseCharacters(inputText)
                                ReverseMode.REVERSE_WORDS -> reverseWordOrder(inputText)
                                ReverseMode.REVERSE_EACH_WORD -> reverseEachWord(inputText)
                                ReverseMode.REVERSE_LINES -> reverseLines(inputText)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (selectedMode == mode) 
                                        MaterialTheme.colorScheme.primary else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = getModeShortName(mode),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedMode == mode) 
                                            MaterialTheme.colorScheme.onPrimary else 
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                
                                Text(
                                    text = modeResult.take(50) + 
                                        if (modeResult.length > 50) "..." else "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
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
                    onClick = { 
                        inputText = "" 
                        copiedResult = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                
                Button(
                    onClick = {
                        inputText = result // Use result as new input
                        copiedResult = false
                    },
                    enabled = result.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use Result")
                }
            }
        }
    }
}

// Reversal Functions
private fun reverseCharacters(text: String): String {
    return text.reversed()
}

private fun reverseWordOrder(text: String): String {
    val words = text.split(Regex("\\s+"))
    return words.reversed().joinToString(" ")
}

private fun reverseEachWord(text: String): String {
    return text.split(Regex("(\\s+)")).map { part ->
        if (part.matches(Regex("\\s+"))) part
        else part.reversed()
    }.joinToString("")
}

private fun reverseLines(text: String): String {
    val lines = text.split("\n")
    return lines.reversed().joinToString("\n")
}

private fun getModeTitle(mode: ReverseMode): String {
    return when (mode) {
        ReverseMode.REVERSE_CHARS -> "Reverse Characters"
        ReverseMode.REVERSE_WORDS -> "Reverse Word Order"
        ReverseMode.REVERSE_EACH_WORD -> "Reverse Each Word"
        ReverseMode.REVERSE_LINES -> "Reverse Lines"
    }
}

private fun getModeDescription(mode: ReverseMode): String {
    return when (mode) {
        ReverseMode.REVERSE_CHARS -> "Reverses all characters in the text"
        ReverseMode.REVERSE_WORDS -> "Keeps words intact but reverses their order"
        ReverseMode.REVERSE_EACH_WORD -> "Reverses characters within each word"
        ReverseMode.REVERSE_LINES -> "Reverses the order of lines"
    }
}

private fun getModeShortName(mode: ReverseMode): String {
    return when (mode) {
        ReverseMode.REVERSE_CHARS -> "Chars"
        ReverseMode.REVERSE_WORDS -> "Words"
        ReverseMode.REVERSE_EACH_WORD -> "Each"
        ReverseMode.REVERSE_LINES -> "Lines"
    }
}

@Composable
private fun StatChip(label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("reversed_text", text)
    clipboard.setPrimaryClip(clip)
}
