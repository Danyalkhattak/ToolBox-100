package com.dannyk.toolbox.ui.screens.tools.text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.ui.draw.clip
import java.util.regex.Pattern
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.LaunchedEffect

data class MatchInfo(
    val start: Int,
    val end: Int,
    val text: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindReplaceScreen(navController: NavHostController) {
    var sourceText by remember { mutableStateOf("") }
    var findText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    
    var matchCase by remember { mutableStateOf(false) }
    var wholeWord by remember { mutableStateOf(false) }
    var useRegex by remember { mutableStateOf(false) }
    
    var resultText by remember { mutableStateOf("") }
    var replacementCount by remember { mutableIntStateOf(0) }
    var showPreview by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Find matches for preview
    val matches: List<MatchInfo> = remember(sourceText, findText, matchCase, wholeWord, useRegex) {
        if (findText.isEmpty() || sourceText.isEmpty()) {
            emptyList()
        } else {
            findAllMatches(sourceText, findText, matchCase, wholeWord, useRegex)
        }
    }

    // Auto-update preview when inputs change
    LaunchedEffect(sourceText, findText, replaceText, matchCase, wholeWord, useRegex) {
        if (showPreview && findText.isNotEmpty() && sourceText.isNotEmpty()) {
            val result = performReplace(sourceText, findText, replaceText, matchCase, wholeWord, useRegex)
            resultText = result.first
            replacementCount = result.second
            hasError = result.third
        } else {
            resultText = sourceText
            replacementCount = 0
            hasError = null
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Find & Replace", onBackClick = { navController.navigateUp() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source Text Section
            Text(
                text = "Source Text",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = sourceText,
                onValueChange = { 
                    sourceText = it
                    if (!showPreview) {
                        resultText = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp),
                placeholder = { Text("Enter or paste your text here...") },
                textStyle = TextStyle(fontSize = 14.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Find and Replace Fields
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Find field
                    OutlinedTextField(
                        value = findText,
                        onValueChange = { findText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Find") },
                        placeholder = { Text("Enter text to find...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Find")
                        },
                        trailingIcon = {
                            if (findText.isNotEmpty()) {
                                IconButton(onClick = { findText = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        }
                    )

                    // Replace field
                    OutlinedTextField(
                        value = replaceText,
                        onValueChange = { replaceText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Replace with") },
                        placeholder = { Text("Enter replacement text...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Replace")
                        }
                    )

                    // Options Row
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Match Case
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = matchCase,
                                onClick = { matchCase = !matchCase },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Aa")
                                        Spacer(Modifier.width(4.dp))
                                        Text("Match Case")
                                    }
                                }
                            )
                            
                            Spacer(Modifier.width(8.dp))
                            
                            // Whole Word
                            FilterChip(
                                selected = wholeWord,
                                onClick = { wholeWord = !wholeWord },
                                label = { Text("Whole Word") }
                            )
                            
                            Spacer(Modifier.width(8.dp))
                            
                            // Regex Mode
                            FilterChip(
                                selected = useRegex,
                                onClick = { useRegex = !useRegex },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(".*")
                                        Spacer(Modifier.width(4.dp))
                                        Text("Regex")
                                    }
                                }
                            )
                        }

                        // Error message for regex
                        if (hasError != null) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = hasError!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                        
                        // Preview toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = showPreview,
                                onCheckedChange = { showPreview = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Live Preview", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Match Statistics
            if (findText.isNotEmpty() && sourceText.isNotEmpty() && hasError == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            matches.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItemCol("Matches Found", matches.size.toString(), 
                            if (matches.isNotEmpty()) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline)
                        StatItemCol("Replacements", replacementCount.toString(), 
                            MaterialTheme.colorScheme.primary)
                    }
                    
                    if (matches.isNotEmpty()) {
                        // Show first few match positions
                        Text(
                            text = "Found at positions: ${matches.take(5).joinToString(", ") { "[${it.start}-${it.end}]" }}" +
                                  if (matches.size > 5) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        )
                    }
                }
            }

            // Result/Preview Section
            Text(
                text = if (showPreview) "Preview" else "Result",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header with copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${resultText.length} characters",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilledTonalButton(
                            onClick = { copyToClipboard(context, resultText) },
                            enabled = resultText.isNotEmpty(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Copy Result")
                        }
                    }
                    
                    // Result display with highlighting
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 250.dp)
                    ) {
                        if (resultText.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            ) {
                                Text(
                                    text = "Enter text to see results",
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        } else {
                            SelectionContainer {
                                Text(
                                    text = buildHighlightedText(resultText, findText, matches),
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
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        sourceText = ""
                        findText = ""
                        replaceText = ""
                        resultText = ""
                        replacementCount = 0
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                
                Button(
                    onClick = { 
                        // Apply replacement to source
                        sourceText = resultText
                        replacementCount = 0
                    },
                    enabled = replacementCount > 0 && hasError == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply ($replacementCount)")
                }
            }
        }
    }
}

private fun findAllMatches(
    text: String,
    pattern: String,
    caseSensitive: Boolean,
    wholeWord: Boolean,
    regexMode: Boolean
): List<MatchInfo> {
    if (pattern.isEmpty()) return emptyList()
    
    return try {
        if (regexMode) {
            val options = if (caseSensitive) setOf() else setOf(RegexOption.IGNORE_CASE)
            val regex = Regex(pattern, options)
            regex.findAll(text).map { match ->
                MatchInfo(match.range.first, match.range.last + 1, match.value)
            }.toList()
        } else {
            val searchPattern = if (caseSensitive) pattern else pattern.lowercase()
            val searchIn = if (caseSensitive) text else text.lowercase()
            
            val results = mutableListOf<MatchInfo>()
            var startIndex = 0
            
            while (startIndex <= searchIn.length) {
                val index = searchIn.indexOf(searchPattern, startIndex)
                if (index == -1) break
                
                // Check whole word constraint
                if (wholeWord) {
                    val beforeOk = index == 0 || !searchIn[index - 1].isLetterOrDigit()
                    val afterOk = index + searchPattern.length >= searchIn.length || 
                                   !searchIn[index + searchPattern.length].isLetterOrDigit()
                    
                    if (beforeOk && afterOk) {
                        results.add(MatchInfo(index, index + searchPattern.length, 
                            text.substring(index, index + searchPattern.length)))
                    }
                } else {
                    results.add(MatchInfo(index, index + searchPattern.length, 
                        text.substring(index, index + searchPattern.length)))
                }
                
                startIndex = index + 1
            }
            
            results
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun performReplace(
    text: String,
    pattern: String,
    replacement: String,
    caseSensitive: Boolean,
    wholeWord: Boolean,
    regexMode: Boolean
): Triple<String, Int, String?> {
    if (pattern.isEmpty() || text.isEmpty()) return Triple(text, 0, null)
    
    return try {
        if (regexMode) {
            val options = if (caseSensitive) setOf() else setOf(RegexOption.IGNORE_CASE)
            val regex = Regex(pattern, options)
            val result = regex.replace(text, replacement)
            val count = regex.findAll(text).count()
            Triple(result, count, null)
        } else {
            var count = 0
            var result = text
            val searchPattern = if (caseSensitive) pattern else pattern.lowercase()
            
            if (wholeWord) {
                // Build word boundary regex
                val wordBoundary = "\\b"
                val flags = if (caseSensitive) "" else "(?i)"
                val regex = Regex("$flags$wordBoundary${Regex.escape(pattern)}$wordBoundary")
                count = regex.findAll(text).count()
                result = regex.replace(text, replacement)
            } else {
                if (caseSensitive) {
                    count = result.split(pattern).size - 1
                    result = result.replace(pattern, replacement)
                } else {
                    val lowerResult = result.lowercase()
                    val lowerPattern = pattern.lowercase()
                    var lastIndex = 0
                    val sb = StringBuilder()
                    var searchFrom = 0
                    
                    while (searchFrom <= lowerResult.length) {
                        val idx = lowerResult.indexOf(lowerPattern, searchFrom)
                        if (idx == -1) break
                        
                        sb.append(result.substring(searchFrom, idx))
                        sb.append(replacement)
                        count++
                        searchFrom = idx + lowerPattern.length
                    }
                    sb.append(result.substring(searchFrom))
                    result = sb.toString()
                }
            }
            
            Triple(result, count, null)
        }
    } catch (e: Exception) {
        Triple(text, 0, "Invalid regex: ${e.message}")
    }
}

@Composable
private fun buildHighlightedText(
    text: String,
    pattern: String,
    matches: List<MatchInfo>
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var lastEnd = 0
        
        for (match in matches) {
            // Add text before this match
            if (match.start > lastEnd) {
                append(text.substring(lastEnd, match.start))
            }
            
            // Add highlighted match
            withStyle(style = SpanStyle(
                background = Color(0xFFFFEB3B),
                color = Color.Black
            )) {
                append(text.substring(match.start, match.end))
            }
            
            lastEnd = match.end
        }
        
        // Add remaining text
        if (lastEnd < text.length) {
            append(text.substring(lastEnd))
        }
    }
}

@Composable
private fun StatItemCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("replaced_text", text)
    clipboard.setPrimaryClip(clip)
}
