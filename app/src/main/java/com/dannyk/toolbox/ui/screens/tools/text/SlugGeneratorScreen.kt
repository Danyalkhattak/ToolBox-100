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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState

enum class Separator {
    DASH, UNDERSCORE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlugGeneratorScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    
    var lowercaseOnly by remember { mutableStateOf(true) }
    var separator by remember { mutableStateOf(Separator.DASH) }
    var removeSpecialChars by remember { mutableStateOf(true) }
    var removeStopWords by remember { mutableStateOf(false) }
    var maxLength by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var copiedSlug by remember { mutableStateOf(false) }

    // Generate slug based on options
    val generatedSlug: String = remember(
        inputText, lowercaseOnly, separator, 
        removeSpecialChars, removeStopWords, maxLength
    ) {
        generateSlug(
            inputText,
            lowercaseOnly,
            separator,
            removeSpecialChars,
            removeStopWords,
            maxLength.toIntOrNull()
        )
    }

    // Stop words list
    val stopWords = setOf(
        "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "by", "from", "as", "is", "was", "are", "been", "be",
        "have", "has", "had", "do", "does", "did", "will", "would", "could",
        "should", "may", "might", "must", "shall", "can", "need", "dare",
        "it", "its", "this", "that", "these", "those", "i", "you", "he",
        "she", "we", "they", "what", "which", "who", "whom", "how", "not",
        "no", "nor", "not", "only", "own", "same", "so", "than", "too",
        "very", "just", "also", "now", "here", "there", "then", "once",
        "if", "about", "above", "after", "again", "all", "am", "any",
        "because", "before", "being", "below", "between", "both", "each",
        "every", "few", "more", "most", "other", "some", "such", "up",
        "upon", "out", "over", "under", "until", "while", "your", "my"
    )

    Scaffold(
        topBar = { ToolTopBar("URL Slug Generator", navController) }
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
                text = "Enter title or text to convert to URL-friendly slug:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder { 
                    Text("Example: How to Create Amazing Android Apps in 2024!") 
                },
                textStyle = TextStyle(fontSize = 16.sp),
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Options Card
            Text(
                text = "Slug Options",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Lowercase option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = lowercaseOnly,
                            onCheckedChange = { lowercaseOnly = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Lowercase only", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (lowercaseOnly) "Convert to: my-url-slug" else "Keep: My-URL-Slug",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Separator option
                    Text(
                        text = "Word Separator",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = separator == Separator.DASH,
                            onClick = { separator = Separator.DASH },
                            label = { Text("Dash (-)") },
                            leadingIcon = if (separator == Separator.DASH) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else null
                        )
                        
                        FilterChip(
                            selected = separator == Separator.UNDERSCORE,
                            onClick = { separator = Separator.UNDERSCORE },
                            label = { Text("Underscore (_)") },
                            leadingIcon = if (separator == Separator.UNDERSCORE) {
                                { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    // Remove special characters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = removeSpecialChars,
                            onCheckedChange = { removeSpecialChars = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Remove special characters", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (removeSpecialChars) 
                                    "'Hello!' → 'hello'" else 
                                    "Keep symbols as-is",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Remove stop words
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = removeStopWords,
                            onCheckedChange = { removeStopWords = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Remove stop words", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = "Remove common words (a, an, the, and, etc.)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Max length
                    OutlinedTextField(
                        value = maxLength,
                        onValueChange = { 
                            if (it.isEmpty() || (it.all { c -> c.isDigit() } && it.length <= 4)) {
                                maxLength = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label { Text("Maximum Length (optional)") },
                        placeholder { Text("e.g., 50 for max 50 chars") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Straighten, contentDescription = null)
                        },
                        suffix = { Text("chars") }
                    )
                    
                    // Show stop words info when enabled
                    if (removeStopWords) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Stop words that will be removed:\n${stopWords.take(20).joinToString(", ")}...",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxWidth())

            // Generated Slug Result
            Text(
                text = "Generated Slug",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your URL-ready slug:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Length indicator
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${generatedSlug.length} chars",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        Spacer(Modifier.width(8.dp))
                        
                        FilledTonalButton(
                            onClick = {
                                copyToClipboard(context, generatedSlug)
                                copiedSlug = true
                            },
                            enabled = generatedSlug.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (copiedSlug) 
                                    Icons.Default.Check else 
                                    Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(if (copiedSlug) "Copied!" else "Copy")
                        }
                    }
                    
                    // Slug display
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Show full URL preview
                            Text(
                                text = "https://example.com/",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            Text(
                                text = if (generatedSlug.isEmpty()) "(enter text above)" 
                                      else generatedSlug,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = if (generatedSlug.isEmpty()) 
                                        MaterialTheme.colorScheme.outline else 
                                        MaterialTheme.colorScheme.primary
                                ),
                                wordBreak = androidx.compose.ui.text.style.WordBreak.BreakAll
                            )
                        }
                    }
                    
                    // Slug analysis
                    if (generatedSlug.isNotEmpty()) {
                        val wordCount = generatedSlug.split(
                            if (separator == Separator.DASH) "-" else "_"
                        ).filter { it.isNotEmpty() }.size
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SlugStat("Words", wordCount.toString())
                            SlugStat("Characters", generatedSlug.length.toString())
                            SlugStat("Separator", if (separator == Separator.DASH) "-" else "_")
                        }
                    }
                }
            }

            // Quick Templates
            Text(
                text = "Quick Templates",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { inputText = "How to Build a Modern Android App with Jetpack Compose" },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text("Blog Post", fontSize = 12.sp)
                }
                
                FilledTonalButton(
                    onClick = { inputText = "Product Name: Super Cool Widget Pro - Best Seller!" },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text("Product", fontSize = 12.sp)
                }
                
                FilledTonalButton(
                    onClick = { inputText = "What's New in Version 2.0? (Updated Features & Bug Fixes)" },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text("Article", fontSize = 12.sp)
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
                        copiedSlug = false
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                
                Button(
                    onClick = {
                        // Reset to defaults
                        lowercaseOnly = true
                        separator = Separator.DASH
                        removeSpecialChars = true
                        removeStopWords = false
                        maxLength = ""
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Options")
                }
            }
        }
    }
}

private fun generateSlug(
    text: String,
    lowercaseOnly: Boolean,
    separator: Separator,
    removeSpecialChars: Boolean,
    removeStopWords: Boolean,
    maxLen: Int?
): String {
    if (text.isBlank()) return ""
    
    var result = text
    
    // Convert to lowercase if needed
    if (lowercaseOnly) {
        result = result.lowercase()
    }
    
    // Remove special characters (keep alphanumeric, spaces, dashes, underscores)
    if (removeSpecialChars) {
        result = result.replace(Regex("[^a-zA-Z0-9\\s\\-_]"), "")
    }
    
    // Remove stop words if enabled
    if (removeStopWords) {
        val stopWords = setOf(
            "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "from", "as", "is", "was", "are", "been", "be",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "must", "shall", "can", "need", "dare"
        )
        
        val words = result.split(Regex("\\s+"))
        result = words.filter { 
            it.trim().lowercase() !in stopWords || 
            it.matches(Regex("\\d+")) // Keep numbers
        }.joinToString(" ")
    }
    
    // Replace whitespace and multiple separators with chosen separator
    val sepChar = when (separator) {
        Separator.DASH -> "-"
        Separator.UNDERSCORE -> "_"
    }
    
    // Replace any existing separators and whitespace with our chosen one
    result = result.replace(Regex("[\\s\\-_]+"), sepChar)
    
    // Remove leading/trailing separators
    result = result.trim(sepChar)
    
    // Apply max length if specified (try to cut at word boundary)
    if (maxLen != null && maxLen > 0 && result.length > maxLen) {
        result = result.substring(0, maxLen)
        // Don't end with partial word or separator
        result = result.trimEnd(sepChar).trimEnd()
    }
    
    return result
}

@Composable
private fun SlugStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, 
            color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("url_slug", text)
    clipboard.setPrimaryClip(clip)
}
