package com.dannyk.toolbox.ui.screens.tools.text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState

data class DuplicateInfo(
    val line: String,
    val count: Int,
    val isFirstOccurrence: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveDuplicatesScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    var caseSensitive by remember { mutableStateOf(true) }
    var trimWhitespace by remember { mutableStateOf(true) }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Process and remove duplicates
    val result = remember(inputText, caseSensitive, trimWhitespace) {
        removeDuplicates(inputText, caseSensitive, trimWhitespace)
    }

    val originalLines = remember(inputText) {
        if (inputText.isBlank()) emptyList()
        else inputText.split("\n")
    }

    val originalCount = originalLines.size
    
    // Get duplicate info for display
    val duplicateInfo: List<DuplicateInfo> = remember(inputText, caseSensitive, trimWhitespace) {
        getDuplicateInfo(inputText, caseSensitive, trimWhitespace)
    }

    val duplicateCount = originalCount - result.lines.size
    val uniqueCount = result.lines.size

    Scaffold(
        topBar = { ToolTopBar("Remove Duplicates", onBackClick = { navController.navigateUp() }) }
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
                text = "Enter text (one item per line):",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 250.dp),
                placeholder = { 
                    Text("Paste your lines here...\n\nExample:\napple\nbanana\napple\norange\nbanana") 
                },
                textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Options Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Options",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Case Sensitive Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = caseSensitive,
                            onCheckedChange = { caseSensitive = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Case Sensitive", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (caseSensitive) 
                                    "'Apple' and 'apple' are different" 
                                else 
                                    "'Apple' and 'apple' are same",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Trim Whitespace Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = trimWhitespace,
                            onCheckedChange = { trimWhitespace = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Trim Whitespace", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (trimWhitespace) 
                                    "' apple' equals 'apple'" 
                                else 
                                    "Keep leading/trailing spaces",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxWidth())

            // Statistics Section
            if (inputText.isNotBlank()) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox("Original Lines", originalCount.toString(), 
                            MaterialTheme.colorScheme.outline)
                        StatBox("Unique Lines", uniqueCount.toString(), 
                            MaterialTheme.colorScheme.primary)
                        StatBox("Duplicates Removed", duplicateCount.toString(), 
                            if (duplicateCount > 0) 
                                androidx.compose.ui.graphics.Color(0xFF4CAF50) else 
                                MaterialTheme.colorScheme.surfaceVariant)
                    }
                    
                    // Progress indicator showing reduction
                    if (originalCount > 0) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { uniqueCount.toFloat() / originalCount },
                                modifier = Modifier.fillMaxWidth().height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "${((1 - uniqueCount.toFloat() / originalCount) * 100).toInt()}% duplicates removed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Result Section
            Text(
                text = "Result (Unique Lines)",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header with copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$uniqueCount unique items",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilledTonalButton(
                            onClick = { copyToClipboard(context, result.text) },
                            enabled = result.text.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Copy All")
                        }
                    }
                    
                    // Result text area
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp)
                    ) {
                        if (result.text.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                Text(
                                    text = "Enter text above to see results",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            Text(
                                text = result.text,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }
            }

            // Duplicate Details (show which were duplicates)
            if (duplicateInfo.any { it.count > 1 }) {
                Text(
                    text = "Duplicate Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(duplicateInfo.filter { it.count > 1 }.take(20)) { info ->
                            DuplicateItem(info)
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
                    onClick = { inputText = "" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                
                Button(
                    onClick = { inputText = result.text }, // Use result as new input
                    enabled = result.text.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use Result")
                }
            }
        }
    }
}

private data class DedupResult(val text: String, val lines: List<String>)

private fun removeDuplicates(text: String, caseSensitive: Boolean, trimWhitespace: Boolean): DedupResult {
    if (text.isBlank()) return DedupResult("", emptyList())
    
    val lines = text.split("\n")
    val seen = mutableSetOf<String>()
    val uniqueLines = mutableListOf<String>()
    
    for (line in lines) {
        val processedLine = when {
            trimWhitespace -> line.trim()
            else -> line
        }
        
        val comparisonKey = when {
            caseSensitive -> processedLine
            else -> processedLine.lowercase()
        }
        
        if (comparisonKey !in seen || comparisonKey.isBlank()) {
            seen.add(comparisonKey)
            uniqueLines.add(line) // Keep original formatting for output
        } else if (processedLine.isBlank()) {
            // Always keep blank lines (they're not really duplicates)
            uniqueLines.add(line)
        }
    }
    
    return DedupResult(uniqueLines.joinToString("\n"), uniqueLines.map { 
        if (trimWhitespace) it.trim() else it 
    })
}

private fun getDuplicateInfo(text: String, caseSensitive: Boolean, trimWhitespace: Boolean): List<DuplicateInfo> {
    if (text.isBlank()) return emptyList()
    
    val lines = text.split("\n")
    val countMap = mutableMapOf<String, Int>()
    val orderList = mutableListOf<String>()
    
    for (line in lines) {
        val processedLine = when {
            trimWhitespace -> line.trim()
            else -> line
        }
        
        val key = when {
            caseSensitive -> processedLine
            else -> processedLine.lowercase()
        }
        
        if (key.isNotBlank()) {
            countMap[key] = countMap.getOrDefault(key, 0) + 1
            if (!orderList.contains(key)) {
                orderList.add(key)
            }
        }
    }
    
    return orderList.mapIndexed { index, key ->
        DuplicateInfo(
            line = key,
            count = countMap[key] ?: 1,
            isFirstOccurrence = true
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DuplicateItem(info: DuplicateInfo) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = "${info.count}x",
                    style = MaterialTheme.typography.labelSmall.copy(
                        androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(Modifier.width(8.dp))
            
            Text(
                text = info.line,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("deduplicated_text", text)
    clipboard.setPrimaryClip(clip)
}
