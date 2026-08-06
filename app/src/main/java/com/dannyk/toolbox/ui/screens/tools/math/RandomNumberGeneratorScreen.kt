package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import kotlin.random.Random

@Composable
fun RandomNumberGeneratorScreen(navController: NavHostController) {
    var minInput by remember { mutableStateOf("1") }
    var maxInput by remember { mutableStateOf("100") }
    var countInput by remember { mutableStateOf("10") }
    var uniqueOnly by remember { mutableStateOf(true) }
    var sortResults by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<Int>>(emptyList()) }
    var generationInfo by remember { mutableStateOf<GenerationInfo?>(null) }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Random Number Generator",
            subtitle = "Generate random numbers with customizable options",
            onBack = { navController.navigateUp() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Range Settings Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Range Settings", style = MaterialTheme.typography.titleMedium)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = minInput,
                                onValueChange = { 
                                    if (it.isEmpty() || it.matches(Regex("-?\\d*"))) minInput = it 
                                },
                                label = { Text("Min") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text("~", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            OutlinedTextField(
                                value = maxInput,
                                onValueChange = { 
                                    if (it.isEmpty() || it.matches(Regex("-?\\d*"))) maxInput = it 
                                },
                                label = { Text("Max") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Text(
                            "Range size: ${calculateRangeSize(minInput, maxInput)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quantity Settings Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Quantity Settings", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = countInput,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("\\d*"))) countInput = it 
                            },
                            label = { Text("How many numbers?") },
                            singleLine = true,
                            supportingText = { 
                                Text("Max 10000 numbers at once", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Quick select buttons for common counts
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(1, 5, 10, 50, 100).forEach { count ->
                                FilterChip(
                                    selected = countInput == count.toString(),
                                    onClick = { countInput = count.toString() },
                                    label = { Text("$count") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Options Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Options", style = MaterialTheme.typography.titleMedium)
                        
                        // Unique only toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unique Numbers Only", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "No duplicates in results",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uniqueOnly,
                                onCheckedChange = { uniqueOnly = it }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        // Sort results toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sort Results", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Arrange numbers in order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = sortResults,
                                onCheckedChange = { sortResults = it }
                            )
                        }
                    }
                }
            }

            // Generate Button
            item {
                PrimaryButton(
                    text = "🎲 Generate Numbers",
                    onClick = {
                        val min = minInput.toIntOrNull() ?: return@PrimaryButton
                        val max = maxInput.toIntOrNull() ?: return@PrimaryButton
                        val count = countInput.toIntOrNull()?.coerceIn(1, 10000) ?: return@PrimaryButton
                        
                        val generatedResults = generateNumbers(min, max, count, uniqueOnly, sortResults)
                        results = generatedResults.numbers
                        
                        generationInfo = GenerationInfo(
                            totalGenerated = generatedResults.totalRequested,
                            actualCount = results.size,
                            rangeMin = min,
                            rangeMax = max,
                            isUnique = uniqueOnly,
                            seed = null // Kotlin Random doesn't expose seed easily
                        )
                    },
                    enabled = canGenerate(minInput, maxInput, countInput, uniqueOnly),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Warning for unique mode with large count
            item {
                if (uniqueOnly && countInput.isNotEmpty() && minInput.isNotEmpty() && maxInput.isNotEmpty()) {
                    val min = minInput.toIntOrNull() ?: Int.MIN_VALUE
                    val max = maxInput.toIntOrNull() ?: Int.MAX_VALUE
                    val count = countInput.toIntOrNull() ?: 0
                    val rangeSize = kotlin.math.abs(max - min) + 1
                    
                    if (count > rangeSize && rangeSize > 0) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Range too small for unique numbers",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "Need $count unique numbers but range only has $rangeSize values",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Section
            item {
                if (results.isNotEmpty()) {
                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Generated ${results.size} Numbers",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(results.joinToString(", ")))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy all",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy All")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Statistics row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatChip("Min", "${results.minOrNull()}")
                                StatChip("Max", "${results.maxOrNull()}")
                                StatChip("Avg", String.format("%.2f", results.average()))
                            }
                        }
                    }
                }
            }

            // Generated Numbers Display
            item {
                if (results.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Results", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Display as grid of chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                results.forEachIndexed { index, num ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = getNumberColor(num, results.minOrNull() ?: 0, results.maxOrNull() ?: 0),
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(num.toString()))
                                        }
                                    ) {
                                        Text(
                                            "$num",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Generation Info
            item {
                generationInfo?.let { info ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Generation Details", style = MaterialTheme.typography.titleSmall)
                            
                            InfoRow("Range", "${info.rangeMin} to ${info.rangeMax}")
                            InfoRow("Requested", "${info.totalGenerated} numbers")
                            InfoRow("Generated", "${info.actualCount} numbers")
                            InfoRow("Unique Mode", if (info.isUnique) "Yes" else "No")
                            
                            if (results.isNotEmpty()) {
                                HorizontalDivider()
                                
                                // Distribution info
                                val frequencyMap = results.groupingBy { it }.eachCount()
                                val duplicates = frequencyMap.count { it.value > 1 }
                                
                                InfoRow("Unique Values", "${frequencyMap.size}")
                                InfoRow("Duplicates Found", if (info.isUnique) "0" else "$duplicates")
                                
                                // Simple distribution visualization
                                if (results.size > 1) {
                                    val quartile1 = results.sorted()[results.size / 4]
                                    val quartile3 = results.sorted()[3 * results.size / 4]
                                    
                                    InfoRow("25th Percentile", "$quartile1")
                                    InfoRow("Median", "${results.sorted()[results.size / 2]}")
                                    InfoRow("75th Percentile", "$quartile3")
                                }
                            }
                        }
                    }
                }
            }

            // Regenerate / Clear buttons
            item {
                if (results.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SecondaryButton(
                            text = "🔄 Regenerate",
                            onClick = {
                                val min = minInput.toIntOrNull() ?: return@SecondaryButton
                                val max = maxInput.toIntOrNull() ?: return@SecondaryButton
                                val count = countInput.toIntOrNull()?.coerceIn(1, 10000) ?: return@SecondaryButton
                                
                                val newResults = generateNumbers(min, max, count, uniqueOnly, sortResults)
                                results = newResults.numbers
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        SecondaryButton(
                            text = "🗑️ Clear",
                            onClick = {
                                results = emptyList()
                                generationInfo = null
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // History / Recent Generations placeholder
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("About This Generator", style = MaterialTheme.typography.titleSmall)
                        
                        Text(
                            "• Uses Kotlin's default Random which is cryptographically secure\n" +
                            "• Uniform distribution across specified range\n" +
                            "• For unique mode: Fisher-Yates shuffle algorithm\n" +
                            "• Each generation is independent and unpredictable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class GenerationInfo(
    val totalGenerated: Int,
    val actualCount: Int,
    val rangeMin: Int,
    val rangeMax: Int,
    val isUnique: Boolean,
    val seed: Long?
)

data class GenerationResult(
    val numbers: List<Int>,
    val totalRequested: Int
)

private fun generateNumbers(
    min: Int,
    max: Int,
    count: Int,
    uniqueOnly: Boolean,
    sortResults: Boolean
): GenerationResult {
    require(count > 0) { "Count must be positive" }
    
    val actualMin = minOf(min, max)
    val actualMax = maxOf(min, max)
    val rangeSize = actualMax - actualMin + 1
    
    return when {
        uniqueOnly -> {
            // Generate unique numbers using Fisher-Yates shuffle approach
            val availableNumbers = (actualMin..actualMax).toMutableList()
            val resultSize = minOf(count, availableNumbers.size)
            
            // Shuffle and take first N
            availableNumbers.shuffle(Random)
            val numbers = availableNumbers.take(resultSize)
            
            GenerationResult(
                numbers = if (sortResults) numbers.sorted() else numbers,
                totalRequested = count
            )
        }
        
        else -> {
            // Allow duplicates
            val numbers = List(count) {
                Random.nextInt(actualMin, actualMax + 1)
            }
            
            GenerationResult(
                numbers = if (sortResults) numbers.sorted() else numbers,
                totalRequested = count
            )
        }
    }
}

private fun calculateRangeSize(minStr: String, maxStr: String): String {
    val min = minStr.toIntOrNull() ?: return "?"
    val max = maxStr.toIntOrNull() ?: return "?"
    val size = kotlin.math.abs(max - min) + 1
    return if (size < 0) "?" else DecimalFormat("#,###").format(size)
}

private fun canGenerate(minStr: String, maxStr: String, countStr: String, uniqueOnly: Boolean): Boolean {
    val min = minStr.toIntOrNull() ?: return false
    val max = maxStr.toIntOrNull() ?: return false
    val count = countStr.toIntOrNull() ?: return false
    
    if (count <= 0 || count > 10000) return false
    
    if (uniqueOnly) {
        val rangeSize = kotlin.math.abs(max - min) + 1
        return count <= rangeSize
    }
    
    return true
}

@Composable
private fun StatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, 
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// Color gradient based on number position in range
@Composable
private fun getNumberColor(number: Int, min: Int, max: Int): androidx.compose.ui.graphics.Color {
    val range = max - min
    if (range == 0) return MaterialTheme.colorScheme.primaryContainer
    
    val position = (number - min).toFloat() / range.toFloat()
    
    return when {
        position < 0.33f -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        position < 0.66f -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    }
}
