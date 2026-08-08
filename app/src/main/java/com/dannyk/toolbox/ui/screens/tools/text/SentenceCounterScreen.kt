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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState
import kotlin.math.*

data class SentenceInfo(
    val text: String,
    val wordCount: Int,
    val charCount: Int,
    val type: SentenceType
)

enum class SentenceType {
    SIMPLE,      // One clause
    COMPOUND,    // Multiple independent clauses (and, but, or)
    COMPLEX,     // Independent + dependent clauses (because, although, if)
    COMPOUND_COMPLEX // Multiple of both
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentenceCounterScreen(navController: NavHostController) {
    var text by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Parse sentences
    val sentences: List<SentenceInfo> = remember(text) {
        parseSentences(text)
    }

    val sentenceCount = sentences.size
    
    val totalWords = remember(sentences) {
        sentences.sumOf { it.wordCount }
    }

    val avgWordsPerSentence = remember(sentenceCount, totalWords) {
        if (sentenceCount == 0) 0.0 else totalWords.toDouble() / sentenceCount
    }

    val longestSentence = remember(sentences) {
        sentences.maxByOrNull { it.wordCount }
    }

    val shortestSentence = remember(sentences) {
        sentences.minByOrNull { it.wordCount }
    }

    // Sentence type distribution
    val simpleCount = remember(sentences) { sentences.count { it.type == SentenceType.SIMPLE } }
    val compoundCount = remember(sentences) { sentences.count { it.type == SentenceType.COMPOUND } }
    val complexCount = remember(sentences) { sentences.count { it.type == SentenceType.COMPLEX } }
    val compoundComplexCount = remember(sentences) { sentences.count { it.type == SentenceType.COMPOUND_COMPLEX } }

    // Readability score (simplified Flesch-Kincaid approximation)
    val readabilityScore = remember(avgWordsPerSentence) {
        when {
            avgWordsPerSentence <= 10 -> "Easy to read"
            avgWordsPerSentence <= 15 -> "Moderate difficulty"
            avgWordsPerSentence <= 20 -> "Difficult"
            else -> "Very difficult"
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Sentence Counter", navController) }
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
                text = "Enter text to analyze sentences:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 250.dp),
                placeholder = { 
                    Text("Paste your text here...\n\nSentences are detected by . ! ? terminators.") 
                },
                textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Divider(modifier = Modifier.fillMaxWidth())

            // Main Statistics Card
            Text(
                text = "Sentence Statistics",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Primary stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("Total Sentences", sentenceCount.toString(), 
                            MaterialTheme.colorScheme.primary)
                        StatColumn("Total Words", totalWords.toString(), 
                            MaterialTheme.colorScheme.secondary)
                        StatColumn("Avg Words/Sentence", String.format("%.1f", avgWordsPerSentence), 
                            MaterialTheme.colorScheme.tertiary)
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Longest and Shortest
                    if (longestSentence != null && shortestSentence != null) {
                        Text(
                            text = "Longest Sentence (${longestSentence.wordCount} words)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = longestSentence.text.trim().take(150) + 
                                    if (longestSentence.text.length > 150) "..." else "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Shortest Sentence (${shortestSentence.wordCount} words)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = shortestSentence.text.trim().take(150) + 
                                    if (shortestSentence.text.length > 150) "..." else "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Readability indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Readability:",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = when (readabilityScore) {
                                "Easy to read" -> Color(0xFF4CAF50)
                                "Moderate difficulty" -> Color(0xFFFF9800)
                                "Difficult" -> Color(0xFFFF5722)
                                else -> Color(0xFFF44336)
                            }
                        ) {
                            Text(
                                text = readabilityScore,
                                style = MaterialTheme.typography.labelMedium.copy(Color.White),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Sentence Type Distribution
            if (sentenceCount > 0) {
                Text(
                    text = "Sentence Complexity Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ComplexityRow("Simple Sentences", simpleCount, sentenceCount, 
                            Color(0xFF4CAF50))
                        ComplexityRow("Compound Sentences", compoundCount, sentenceCount, 
                            Color(0xFF2196F3))
                        ComplexityRow("Complex Sentences", complexCount, sentenceCount, 
                            Color(0xFFFF9800))
                        ComplexityRow("Compound-Complex", compoundComplexCount, sentenceCount, 
                            Color(0xFF9C27B0))
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Text(
                            text = "• Simple: Single independent clause\n" +
                                  "• Compound: Two+ independent clauses\n" +
                                  "• Complex: Independent + dependent clause(s)\n" +
                                  "• Compound-Complex: Multiple of both types",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Individual Sentence List
            if (sentences.isNotEmpty()) {
                Text(
                    text = "All Sentences ($sentenceCount)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(sentences) { index, sentence ->
                            SentenceItem(index + 1, sentence)
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

private fun parseSentences(text: String): List<SentenceInfo> {
    if (text.isBlank()) return emptyList()

    // Split by sentence terminators (. ! ?) but handle abbreviations
    val rawSentences = text.split(Regex("(?<=[.!?])\\s+(?=[A-Z])"))
    
    return rawSentences.filter { it.trim().isNotEmpty() }.mapIndexed { index, rawText ->
        val trimmed = rawText.trim()
        val wordCount = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
        val charCount = trimmed.length
        val type = classifySentence(trimmed)
        
        SentenceInfo(trimmed, wordCount, charCount, type)
    }
}

private fun classifySentence(sentence: String): SentenceType {
    val lowerSentence = sentence.lowercase()
    
    // Coordinating conjunctions for compound sentences
    val coordinatingConjunctions = listOf(" and ", " but ", " or ", " nor ", " for ", " so ", " yet ")
    
    // Subordinating conjunctions for complex sentences
    val subordinatingConjunctions = listOf(
        " because ", " although ", " though ", " while ", " since ", " unless ",
        " until ", " once ", " if ", " when ", " where ", " whether ", " that ",
        " which ", " who ", " whom ", " whose ", " after ", " before ", " as ",
        " even if ", " even though ", " in case ", " provided that ", " as if "
    )
    
    val hasCoordinating = coordinatingConjunctions.any { lowerSentence.contains(it) }
    val hasSubordinating = subordinatingConjunctions.any { lowerSentence.contains(it) }
    
    // Check for multiple clauses by counting verbs/conjugations approximately
    val verbIndicators = listOf(" is ", " are ", " was ", " were ", " am ", " be ", " been ",
        " have ", " has ", " had ", " do ", " does ", " did ", " will ", " would ", " could ",
        " should ", " may ", " might ", " can ", " shall ")
    val verbCount = verbIndicators.count { lowerSentence.contains(it) }
    
    return when {
        hasCoordinating && hasSubordinating -> SentenceType.COMPOUND_COMPLEX
        hasCoordinating || (verbCount >= 2 && !hasSubordinating) -> SentenceType.COMPOUND
        hasSubordinating -> SentenceType.COMPLEX
        else -> SentenceType.SIMPLE
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ComplexityRow(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count * 100 / total) else 0
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(140.dp)
        )
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.weight(1f).height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$count ($percentage%)",
            style = MaterialTheme.typography.bodyMedium,
            minWidth = 70.dp
        )
    }
}

@Composable
private fun SentenceItem(index: Int, sentence: SentenceInfo) {
    val typeColor = when (sentence.type) {
        SentenceType.SIMPLE -> Color(0xFF4CAF50)
        SentenceType.COMPOUND -> Color(0xFF2196F3)
        SentenceType.COMPLEX -> Color(0xFFFF9800)
        SentenceType.COMPOUND_COMPLEX -> Color(0xFF9C27B0)
    }
    
    val typeLabel = when (sentence.type) {
        SentenceType.SIMPLE -> "Simple"
        SentenceType.COMPOUND -> "Compound"
        SentenceType.COMPLEX -> "Complex"
        SentenceType.COMPOUND_COMPLEX -> "C-C"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Index badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                
                Spacer(Modifier.width(8.dp))
                
                // Type badge
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = typeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                // Word count
                Text(
                    text = "${sentence.wordCount} words",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(6.dp))
            
            Text(
                text = sentence.text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
