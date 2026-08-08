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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import kotlin.random.Random
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState

enum class SortOrder {
    ASCENDING, DESCENDING
}

enum class SortType {
    ALPHABETICAL, NUMERICAL, LENGTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortLinesScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.ASCENDING) }
    var sortType by remember { mutableStateOf(SortType.ALPHABETICAL) }
    var removeBlankLines by remember { mutableStateOf(false) }
    var removeDuplicates by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Process sorting
    val result: String = remember(inputText, sortOrder, sortType, removeBlankLines, removeDuplicates) {
        sortLines(inputText, sortOrder, sortType, removeBlankLines, removeDuplicates)
    }

    val originalLineCount = if (inputText.isBlank()) 0 else inputText.split("\n").size
    val resultLineCount = if (result.isBlank()) 0 else result.split("\n").size

    Scaffold(
        topBar = { ToolTopBar("Sort Lines") { navController.navigateUp() } }
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
                text = "Enter text lines to sort:",
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
                    Text("Enter one item per line...\n\nExample:\nbanana\napple\ncherry\ndate") 
                },
                textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Options Card
            Text(
                text = "Sort Options",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sort Type Selection
                    Text(
                        text = "Sort By",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SortType.entries.forEach { type ->
                            Surface(
                                onClick = { sortType = type },
                                shape = MaterialTheme.shapes.small,
                                color = if (sortType == type) 
                                    MaterialTheme.colorScheme.primaryContainer else 
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = sortType == type,
                                        onClick = { sortType = type }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                            Text(getSortTypeName(type), style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.weight(1f))
                                        Text(
                                            text = getSortTypeDescription(type),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }

                    // Shuffle Button (special case)
                    FilledTonalButton(
                        onClick = { /* Handled separately */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("🎲 Random Shuffle")
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Sort Order
                    Text(
                        text = "Sort Order",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortOrder == SortOrder.ASCENDING,
                            onClick = { sortOrder = SortOrder.ASCENDING },
                            label = { Text("A → Z / 1 → 9") },
                            leadingIcon = if (sortOrder == SortOrder.ASCENDING) {
                                { Icon(Icons.Default.ArrowUpward, null, Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = sortOrder == SortOrder.DESCENDING,
                            onClick = { sortOrder = SortOrder.DESCENDING },
                            label = { Text("Z → A / 9 → 1") },
                            leadingIcon = if (sortOrder == SortOrder.DESCENDING) {
                                { Icon(Icons.Default.ArrowDownward, null, Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Additional Options
                    Text(
                        text = "Additional Options",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = removeBlankLines,
                            onCheckedChange = { removeBlankLines = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Remove blank lines", style = MaterialTheme.typography.bodyLarge)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = removeDuplicates,
                            onCheckedChange = { removeDuplicates = it }
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Remove duplicates", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Divider(modifier = Modifier.fillMaxWidth())

            // Result Section
            Text(
                text = "Sorted Result",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header with stats and copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$resultLineCount lines",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        
                        if (originalLineCount != resultLineCount) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "was $originalLineCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        FilledTonalButton(
                            onClick = { copyToClipboard(context, result) },
                            enabled = result.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Copy")
                        }
                    }
                    
                    // Result display
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 300.dp)
                    ) {
                        if (result.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                Text(
                                    text = "Enter text above to see sorted results",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            Text(
                                text = result,
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
                    onClick = { inputText = result }, // Use sorted result as new input
                    enabled = result.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use Result")
                }
            }
        }
    }
}

private fun sortLines(
    text: String,
    order: SortOrder,
    type: SortType,
    removeBlanks: Boolean,
    removeDups: Boolean
): String {
    if (text.isBlank()) return ""
    
    var lines = text.split("\n").toMutableList()
    
    // Remove blank lines if option is enabled
    if (removeBlanks) {
        lines = lines.filter { it.trim().isNotEmpty() }.toMutableList()
    }
    
    // Remove duplicates if option is enabled
    if (removeDups) {
        val seen = mutableSetOf<String>()
        lines = lines.filter { line ->
            if (seen.contains(line)) false
            else { seen.add(line); true }
        }.toMutableList()
    }
    
    // Perform sorting based on type
    val sortedLines = when (type) {
        SortType.ALPHABETICAL -> {
            when (order) {
                SortOrder.ASCENDING -> lines.sortedWith(compareBy(String::lowercase))
                SortOrder.DESCENDING -> lines.sortedWith(compareByDescending(String::lowercase))
            }
        }
        
        SortType.NUMERICAL -> {
            when (order) {
                SortOrder.ASCENDING -> lines.sortedWith(compareBy<String> { line ->
                    extractNumber(line) ?: Double.MAX_VALUE
                }.thenBy(String::lowercase))
                
                SortOrder.DESCENDING -> lines.sortedWith(compareByDescending<String> { line ->
                    extractNumber(line) ?: Double.MIN_VALUE
                }.thenByDescending(String::lowercase))
            }
        }
        
        SortType.LENGTH -> {
            when (order) {
                SortOrder.ASCENDING -> lines.sortedBy { it.length }
                SortOrder.DESCENDING -> lines.sortedByDescending { it.length }
            }
        }
    }
    
    return sortedLines.joinToString("\n")
}

private fun extractNumber(text: String): Double? {
    // Try to find a number at the start of the string
    val match = Regex("^([+-]?(?:\\d+\\.?\\d*|\\.\\d+))").find(text)
    return match?.value?.toDoubleOrNull()
}

private fun getSortTypeName(type: SortType): String {
    return when (type) {
        SortType.ALPHABETICAL -> "Alphabetical (A-Z)"
        SortType.NUMERICAL -> "Numerical (1-2-3)"
        SortType.LENGTH -> "By Length"
    }
}

private fun getSortTypeDescription(type: SortType): String {
    return when (type) {
        SortType.ALPHABETICAL -> "a, b, c..."
        SortType.NUMERICAL -> "1, 2, 3..."
        SortType.LENGTH -> "Short → Long"
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("sorted_text", text)
    clipboard.setPrimaryClip(clip)
}
