package com.dannyk.toolbox.ui.screens.tools.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.foundation.ScrollState

enum class DiffType {
    UNCHANGED, ADDED, REMOVED, MODIFIED
}

data class DiffLine(
    val type: DiffType,
    val content: String,
    val lineNumberOld: Int?,
    val lineNumberNew: Int?
)

data class DiffResult(
    val lines: List<DiffLine>,
    val additions: Int,
    val deletions: Int,
    val unchanged: Int,
    val modifications: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDiffScreen(navController: NavHostController) {
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    
    var showLineNumbers by remember { mutableStateOf(true) }
    var ignoreWhitespace by remember { mutableStateOf(false) }
    var caseSensitive by remember { mutableStateOf(true) }
    
    var diffResult by remember { mutableStateOf<DiffResult?>(null) }
    
    val scrollState = rememberScrollState()

    // Compute diff when texts change or compare button pressed
    fun computeDiff() {
        diffResult = computeTextDiff(text1, text2, ignoreWhitespace, caseSensitive)
    }

    Scaffold(
        topBar = { ToolTopBar("Text Diff") { navController.navigateUp() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Compare two texts and find differences",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Two text inputs side by side (or stacked on small screens)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Original Text
                    Text(
                        text = "Original Text",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = text1,
                        onValueChange = { text1 = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 150.dp),
                        placeholder = { Text("Paste original text...") },
                        textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Modified Text
                    Text(
                        text = "Modified Text",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    OutlinedTextField(
                        value = text2,
                        onValueChange = { text2 = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 150.dp),
                        placeholder = { Text("Paste modified text...") },
                        textStyle = TextStyle(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.secondary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Options Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = ignoreWhitespace,
                    onClick = { ignoreWhitespace = !ignoreWhitespace },
                    label = { Text("Ignore Whitespace") }
                )
                
                FilterChip(
                    selected = caseSensitive,
                    onClick = { caseSensitive = !caseSensitive },
                    label = { Text("Case Sensitive") }
                )
                
                Spacer(Modifier.weight(1f))
                
                Button(
                    onClick = { computeDiff() },
                    enabled = text1.isNotBlank() || text2.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.CompareArrows,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Compare")
                }
            }

            // Statistics Section
            if (diffResult != null) {
                Text(
                    text = "Difference Statistics",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            DiffStatItem("Unchanged", diffResult!!.unchanged.toString(), 
                                Color(0xFFE0E0E0))
                            DiffStatItem("Added", diffResult!!.additions.toString(), 
                                Color(0xFFC8E6C9))
                            DiffStatItem("Removed", diffResult!!.deletions.toString(), 
                                Color(0xFFFFCDD2))
                            DiffStatItem("Modified", diffResult!!.modifications.toString(), 
                                Color(0xFFFFF9C4))
                        }
                        
                        // Visual bar showing proportions
                        if (diffResult!!.lines.isNotEmpty()) {
                            val total = diffResult!!.lines.size.toFloat()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                            ) {
                                if (diffResult!!.unchanged > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(diffResult!!.unchanged / total)
                                            .fillMaxHeight()
                                            .background(Color(0xFFBDBDBD))
                                    )
                                }
                                if (diffResult!!.additions > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(diffResult!!.additions / total)
                                            .fillMaxHeight()
                                            .background(Color(0xFF4CAF50))
                                    )
                                }
                                if (diffResult!!.deletions > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(diffResult!!.deletions / total)
                                            .fillMaxHeight()
                                            .background(Color(0xFFF44336))
                                    )
                                }
                                if (diffResult!!.modifications > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(diffResult!!.modifications / total)
                                            .fillMaxHeight()
                                            .background(Color(0xFFFFEB3B))
                                    )
                                }
                            }
                            
                            Text(
                                text = "Total lines compared: ${diffResult!!.lines.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Diff Results
                Text(
                    text = "Detailed Differences",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem("Added", Color(0xFF4CAF50))
                    LegendItem("Removed", Color(0xFFF44336))
                    LegendItem("Modified", Color(0xFFFFEB3B))
                    LegendItem("Unchanged", Color.Transparent)
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(diffResult!!.lines) { index, line ->
                            DiffLineItem(line, index + 1, showLineNumbers)
                        }
                    }
                }
            } else {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Enter two texts to compare",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Differences will be highlighted with colors",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
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
                        text1 = ""
                        text2 = ""
                        diffResult = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                
                FilledTonalButton(
                    onClick = { /* Swap texts could go here */ },
                    enabled = text1.isNotEmpty() && text2.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Swap")
                }
            }
        }
    }
}

// Simple diff algorithm implementation
private fun computeTextDiff(
    text1: String,
    text2: String,
    ignoreWhitespace: Boolean,
    caseSensitive: Boolean
): DiffResult {
    val lines1 = processLines(text1.split("\n"), ignoreWhitespace, caseSensitive)
    val lines2 = processLines(text2.split("\n"), ignoreWhitespace, caseSensitive)
    
    // Use LCS-based diff algorithm
    val lcsMatrix = computeLCSMatrix(lines1.map { it.first }, lines2.map { it.first })
    val diffLines = mutableListOf<DiffLine>()
    
    var i = lines1.size
    var j = lines2.size
    var additions = 0
    var deletions = 0
    var unchanged = 0
    var modifications = 0
    
    // Backtrack through LCS matrix
    while (i > 0 || j > 0) {
        when {
            i > 0 && j > 0 && lines1[i - 1].first == lines2[j - 1].first -> {
                diffLines.add(0, DiffLine(
                    type = DiffType.UNCHANGED,
                    content = lines1[i - 1].second,
                    lineNumberOld = i,
                    lineNumberNew = j
                ))
                unchanged++
                i--
                j--
            }
            j > 0 && (i == 0 || (lcsMatrix[i][j - 1] >= lcsMatrix[i - 1][j])) -> {
                diffLines.add(0, DiffLine(
                    type = DiffType.ADDED,
                    content = lines2[j - 1].second,
                    lineNumberOld = null,
                    lineNumberNew = j
                ))
                additions++
                j--
            }
            else -> {
                diffLines.add(0, DiffLine(
                    type = DiffType.REMOVED,
                    content = lines1[i - 1].second,
                    lineNumberOld = i,
                    lineNumberNew = null
                ))
                deletions++
                i--
            }
        }
    }
    
    // Detect modifications (adjacent removed + added that might be related)
    val finalLines = mutableListOf<DiffLine>()
    var k = 0
    while (k < diffLines.size) {
        if (k < diffLines.size - 1 &&
            diffLines[k].type == DiffType.REMOVED &&
            diffLines[k + 1].type == DiffType.ADDED) {
            // Check if these could be a modification
            finalLines.add(DiffLine(
                type = DiffType.MODIFIED,
                content = "${diffLines[k].content} → ${diffLines[k + 1].content}",
                lineNumberOld = diffLines[k].lineNumberOld,
                lineNumberNew = diffLines[k + 1].lineNumberNew
            ))
            modifications++
            k += 2
        } else {
            finalLines.add(diffLines[k])
            k++
        }
    }
    
    return DiffResult(finalLines, additions, deletions, unchanged, modifications)
}

private fun processLines(
    lines: List<String>,
    ignoreWhitespace: Boolean,
    caseSensitive: Boolean
): List<Pair<String, String>> {
    return lines.map { line ->
        val processed = when {
            ignoreWhitespace -> line.trim().replace(Regex("\\s+"), " ")
            else -> line
        }
        val comparisonKey = when {
            caseSensitive -> processed
            else -> processed.lowercase()
        }
        Pair(comparisonKey, line)
    }
}

private fun computeLCSMatrix(list1: List<String>, list2: List<String>): Array<IntArray> {
    val m = list1.size
    val n = list2.size
    val dp = Array(m + 1) { IntArray(n + 1) }
    
    for (i in 1..m) {
        for (j in 1..n) {
            dp[i][j] = when {
                list1[i - 1] == list2[j - 1] -> dp[i - 1][j - 1] + 1
                else -> maxOf(dp[i - 1][j], dp[i][j - 1])
            }
        }
    }
    
    return dp
}

@Composable
private fun DiffStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.3f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DiffLineItem(line: DiffLine, displayIndex: Int, showNumbers: Boolean) {
    val backgroundColor = when (line.type) {
        DiffType.ADDED -> Color(0xFFC8E6C9)
        DiffType.REMOVED -> Color(0xFFFFCDD2)
        DiffType.MODIFIED -> Color(0xFFFFF9C4)
        DiffType.UNCHANGED -> Color.Transparent
    }
    
    val prefix = when (line.type) {
        DiffType.ADDED -> "+ "
        DiffType.REMOVED -> "- "
        DiffType.MODIFIED -> "~ "
        DiffType.UNCHANGED -> "  "
    }
    
    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (showNumbers) {
                Text(
                    text = buildString {
                        append(line.lineNumberOld?.toString() ?: "")
                        if (line.lineNumberNew != null) {
                            append(":")
                            append(line.lineNumberNew)
                        }
                        while (length < 10) append(' ')
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.widthIn(min = 60.dp)
                )
            }
            
            Text(
                text = prefix,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = when (line.type) {
                        DiffType.ADDED -> Color(0xFF2E7D32)
                        DiffType.REMOVED -> Color(0xFFC62828)
                        DiffType.MODIFIED -> Color(0xFFF57F17)
                        DiffType.UNCHANGED -> MaterialTheme.colorScheme.onSurface
                    }
                ),
                modifier = Modifier.width(16.dp)
            )
            
            Text(
                text = line.content,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
