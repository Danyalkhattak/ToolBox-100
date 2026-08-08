package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import androidx.compose.ui.text.font.FontWeight
import android.content.ClipboardManager
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.foundation.ScrollState

@Composable
fun RegexTesterScreen(navController: NavHostController) {
    var patternInput by remember { mutableStateOf("") }
    var testString by remember { mutableStateOf("") }
    var replaceString by remember { mutableStateOf("") }
    var showReplace by remember { mutableStateOf(false) }
    var regexError by remember { mutableStateOf<String?>(null) }
    var matchResults by remember { mutableStateOf<RegexMatchResult?>(null) }
    var caseInsensitive by remember { mutableStateOf(false) }
    var multiline by remember { mutableStateOf(false) }
    var dotAll by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Regex Tester",
            subtitle = "Test and debug regular expressions",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pattern Input Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Regular Expression Pattern", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = patternInput,
                        onValueChange = { 
                            patternInput = it
                            regexError = null
                        },
                        label = { Text("Enter regex pattern") },
                        placeholder = { 
                            Text(
                                "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", 
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Flags section
                    Text("Pattern Flags", style = MaterialTheme.typography.bodyMedium)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = caseInsensitive,
                            onClick = { caseInsensitive = !caseInsensitive },
                            label = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("CASE_INSENSITIVE")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("(?i)", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                            }}
                        )
                        
                        FilterChip(
                            selected = multiline,
                            onClick = { multiline = !multiline },
                            label = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("MULTILINE")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("(?m)", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                            }}
                        )
                    }

                    FilterChip(
                        selected = dotAll,
                        onClick = { dotAll = !dotAll },
                        label = { Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("DOTALL")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("(?s)", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                        }}
                    )
                }
            }

            // Test String Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Test String", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = testString,
                        onValueChange = { testString = it },
                        label = { Text("Enter text to search") },
                        placeholder = { 
                            Text(
                                "Enter your test text here...", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Replace option toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = showReplace,
                            onCheckedChange = { showReplace = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Find & Replace Mode", style = MaterialTheme.typography.bodyMedium)
                            if (showReplace) {
                                OutlinedTextField(
                                    value = replaceString,
                                    onValueChange = { replaceString = it },
                                    label = { Text("Replacement string") },
                                    placeholder = { Text("$1-$2", fontFamily = FontFamily.Monospace) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                        }
                    }

                    PrimaryButton(
                        text = if (showReplace) "Test Replace" else "Test Match",
                        onClick = {
                            try {
                                val flags = buildFlags(caseInsensitive, multiline, dotAll)
                                matchResults = testRegex(patternInput, testString, flags, showReplace, replaceString)
                                regexError = null
                            } catch (e: PatternSyntaxException) {
                                regexError = "Invalid regex: ${e.message}"
                                matchResults = null
                            } catch (e: Exception) {
                                regexError = e.message ?: "Unknown error"
                                matchResults = null
                            }
                        },
                        enabled = patternInput.isNotEmpty() && testString.isNotEmpty(),
                        icon = Icons.Default.FindReplace
                    )

                    SecondaryButton(
                        text = "Clear All",
                        onClick = {
                            patternInput = ""
                            testString = ""
                            replaceString = ""
                            matchResults = null
                            regexError = null
                        }
                    )
                }
            }

            // Error Display
            regexError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Regex Error", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Results Section
            matchResults?.let { results ->
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Match Summary", style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Matches", "${results.matches.size}")
                            StatItem("Groups", "${results.groupCount}")
                            StatItem("Position", if (results.matches.isNotEmpty()) "0-${results.matches.last().end}" else "N/A")
                        }
                    }
                }

                // Highlighted Matches
                if (results.matches.isNotEmpty() || results.replacedText != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (showReplace && results.replacedText != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Replaced Result", style = MaterialTheme.typography.titleMedium)
                                    
                                    IconButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(results.replacedText!!))
                                    }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    }
                                }

                                Surface(
                                    color = Color(0xFF1E1E1E),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = results.replacedText,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                                        modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Highlighted Matches", style = MaterialTheme.typography.titleMedium)
                                    
                                    TextButton(onClick = {
                                        clipboardManager.setText(AnnotatedString(testString))
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Original")
                                    }
                                }

                                Surface(
                                    color = Color(0xFF1E1E1E),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = buildHighlightedText(testString, results.matches),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                                        modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                                    )
                                }
                            }
                        }
                    }
                } else if (!showReplace) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            "No matches found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Individual Matches Detail
                if (results.matches.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Match Details (${results.matches.size})", style = MaterialTheme.typography.titleMedium)

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                items(results.matches.withIndex().toList()) { (index, match) ->
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Match header
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary) {
                                                    Text(
                                                        "Match #${index + 1}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                                
                                                Text(
                                                    "Position ${match.start}-${match.end}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            // Match value
                                            Surface(color = Color.Black.copy(alpha = 0.9f), shape = MaterialTheme.shapes.small) {
                                                Text(
                                                    match.value,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color.Green,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }

                                            // Groups
                                            if (match.groups.isNotEmpty()) {
                                                Text("Groups:", style = MaterialTheme.typography.labelSmall)
                                                match.groups.forEachIndexed { groupIndex, group ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                                    ) {
                                                        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.secondaryContainer) {
                                                            Text(
                                                                "$groupIndex",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            "\"$group\"",
                                                            fontFamily = FontFamily.Monospace,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Common Patterns Library
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Common Patterns Library", style = MaterialTheme.typography.titleSmall)

                    Text(
                        "Click to use a pre-built pattern:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val commonPatterns = listOf(
                        RegexPattern("Email", "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", "user@example.com"),
                        RegexPattern("URL", "https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*", "https://example.com/path"),
                        RegexPattern("IP Address (v4)", "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b", "192.168.1.1"),
                        RegexPattern("Phone (US)", "\\+?1?[-.]?\\(?[0-9]{3}\\)?[-.]?[0-9]{3}[-.]?[0-9]{4}", "(555) 123-4567"),
                        RegexPattern("Date (YYYY-MM-DD)", "\\d{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])", "2024-01-15"),
                        RegexPattern("Time (HH:MM:SS)", "(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d", "14:30:00"),
                        RegexPattern("Hex Color", "#(?:[0-9a-fA-F]{3}){1,2}\\b", "#FF5733 or #F53"),
                        RegexPattern("Username (3-20 chars)", "^[a-zA-Z][a-zA-Z0-9_-]{2,19}$", "john_doe123"),
                        RegexPattern("Password (strong)", "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", "Str0ng@Pass!"),
                        RegexPattern("Credit Card", "\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b", "1234-5678-9012-3456"),
                        RegexPattern("HTML Tag", "<([a-z][a-z0-9]*)[^>]*>(.*?)</\\1>", "<div>content</div>"),
                        RegexPattern("Whitespace", "\\s+", "spaces, tabs, newlines"),
                        RegexPattern("Number (integer)", "-?\\d+(?=\\D|$)", "42 or -7"),
                        RegexPattern("Number (float)", "-?\\d*\\.\\d+(?=\\D|$)", "3.14159 or -0.5"),
                        RegexPattern("Word boundary", "\\b\\w+\\b", "any word")
                    )

                    commonPatterns.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { pattern ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    onClick = {
                                        patternInput = pattern.pattern
                                        if (pattern.example != null) {
                                            testString = pattern.example
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(pattern.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                                        Text(pattern.pattern, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (pattern.example != null) {
                                            Text("e.g.: ${pattern.example}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Reference
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Quick Reference", style = MaterialTheme.typography.titleSmall)

                    val referenceItems = listOf(
                        "." to "Any character except newline",
                        "\\d" to "Digit [0-9]",
                        "\\D" to "Non-digit [^0-9]",
                        "\\w" to "Word char [a-zA-Z0-9_]",
                        "\\W" to "Non-word char",
                        "\\s" to "Whitespace [ \\t\\n\\r]",
                        "\\S" to "Non-whitespace",
                        "^" to "Start of line/string",
                        "$" to "End of line/string",
                        "*" to "Zero or more (greedy)",
                        "+" to "One or more (greedy)",
                        "?" to "Optional (zero or one)",
                        "{n}" to "Exactly n times",
                        "{n,m}" to "n to m times",
                        "[abc]" to "Character class",
                        "[^abc]" to "Negated class",
                        "(...)" to "Capturing group",
                        "(?:...)" to "Non-capturing group",
                        "| " to "Alternation (OR)"
                    )

                    referenceItems.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (syntax, desc) ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(syntax, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RegexMatchResult(
    val matches: List<MatchDetail>,
    val groupCount: Int,
    val replacedText: String?
)

data class MatchDetail(
    val value: String,
    val start: Int,
    val end: Int,
    val groups: List<String>
)

data class RegexPattern(
    val name: String,
    val pattern: String,
    val example: String? = null
)

private fun buildFlags(caseInsensitive: Boolean, multiline: Boolean, dotAll: Boolean): Int {
    var flags = 0
    if (caseInsensitive) flags = flags or Pattern.CASE_INSENSITIVE
    if (multiline) flags = flags or Pattern.MULTILINE
    if (dotAll) flags = flags or Pattern.DOTALL
    return flags
}

private fun testRegex(
    pattern: String,
    input: String,
    flags: Int,
    doReplace: Boolean,
    replacement: String
): RegexMatchResult {
    val compiledPattern = Pattern.compile(pattern, flags)
    
    return if (doReplace) {
        val matcher = compiledPattern.matcher(input)
        val replaced = matcher.replaceAll(replacement)
        
        // Also find matches for display
        val resetMatcher = compiledPattern.matcher(input)
        val matches = mutableListOf<MatchDetail>()
        
        while (resetMatcher.find()) {
            val groups = mutableListOf<String>()
            for (i in 0..resetMatcher.groupCount()) {
                groups.add(resetMatcher.group(i) ?: "")
            }
            matches.add(MatchDetail(resetMatcher.group(), resetMatcher.start(), resetMatcher.end(), groups))
        }
        
        RegexMatchResult(matches, compiledPattern.matcher("").groupCount() + 1, replaced)
    } else {
        val matcher = compiledPattern.matcher(input)
        val matches = mutableListOf<MatchDetail>()
        
        while (matcher.find()) {
            val groups = mutableListOf<String>()
            for (i in 0..matcher.groupCount()) {
                groups.add(matcher.group(i) ?: "")
            }
            matches.add(MatchDetail(matcher.group(), matcher.start(), matcher.end(), groups))
        }
        
        RegexMatchResult(matches, compiledPattern.matcher("").groupCount() + 1, null)
    }
}

@Composable
private fun buildHighlightedText(text: String, matches: List<MatchDetail>): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        
        for (match in matches.sortedBy { it.start }) {
            // Add non-matching text before this match
            if (lastIndex < match.start) {
                append(text.substring(lastIndex, match.start))
            }
            
            // Add highlighted match
            withStyle(style = SpanStyle(backgroundColor = Color(0xFF4CAF50), color = Color.White)) {
                append(match.value)
            }
            
            lastIndex = match.end
        }
        
        // Add remaining text after last match
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
