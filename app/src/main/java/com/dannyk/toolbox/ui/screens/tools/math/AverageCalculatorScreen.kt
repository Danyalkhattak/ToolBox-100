package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import kotlin.math.*

@Composable
fun AverageCalculatorScreen(navController: NavHostController) {
    var numberInput by remember { mutableStateOf("") }
    var numbers by remember { mutableStateOf<List<Double>>(emptyList()) }
    var statistics by remember { mutableStateOf<StatisticsResult?>(null) }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Average Calculator",
            subtitle = "Calculate mean, median, mode, range and more",
            onBack = { navController.navigateUp() }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Enter Numbers", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = numberInput,
                            onValueChange = { numberInput = it },
                            label = { Text("Enter numbers (comma-separated)") },
                            placeholder = { Text("e.g., 1, 2, 3, 4, 5") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrimaryButton(
                                text = "Add Numbers",
                                onClick = {
                                    val newNumbers = parseNumbers(numberInput)
                                    if (newNumbers.isNotEmpty()) {
                                        numbers = newNumbers
                                        statistics = calculateStatistics(numbers)
                                        numberInput = ""
                                    }
                                },
                                enabled = numberInput.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            )
                            
                            SecondaryButton(
                                text = "Clear All",
                                onClick = {
                                    numbers = emptyList()
                                    statistics = null
                                    numberInput = ""
                                },
                                enabled = numbers.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Numbers List
            item {
                if (numbers.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Numbers (${numbers.size})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(numbers.joinToString(", ")))
                                }) {
                                    Text("Copy All")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Display as chips/tags
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                numbers.forEachIndexed { index, num ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        onClick = {
                                            numbers = numbers.toMutableList().also { it.removeAt(index) }
                                            if (numbers.isEmpty()) {
                                                statistics = null
                                            } else {
                                                statistics = calculateStatistics(numbers)
                                            }
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                formatNumber(num),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Statistics Results
            item {
                statistics?.let { stats ->
                    // Main Statistics Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Statistical Measures", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Mean - Primary result
                            StatResultRow("Mean (Average)", stats.mean.formatDecimal(), isPrimary = true)
                            Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))
                            
                            // Median
                            StatResultRow("Median", stats.median.formatDecimal())
                            
                            // Mode
                            StatResultRow("Mode", 
                                if (stats.modes.isEmpty()) "No mode" 
                                else stats.modes.joinToString(", ") { it.formatDecimal() })
                            
                            Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))
                            
                            // Range
                            StatResultRow("Range", stats.range.formatDecimal())
                        }
                    }
                }
            }

            // Additional Statistics
            item {
                statistics?.let { stats ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Additional Information", style = MaterialTheme.typography.titleMedium)
                            
                            InfoRow("Count", "${stats.count}")
                            InfoRow("Sum", stats.sum.formatDecimal())
                            InfoRow("Minimum", stats.min.formatDecimal())
                            InfoRow("Maximum", stats.max.formatDecimal())
                            
                            Divider(modifier = Modifier.fillMaxWidth())
                            
                            InfoRow("Standard Deviation", stats.standardDeviation.formatDecimal())
                            InfoRow("Variance", stats.variance.formatDecimal())
                            InfoRow("Population Std Dev", stats.populationStdDev.formatDecimal())
                        }
                    }
                }
            }

            // Distribution Info
            item {
                statistics?.let { stats ->
                    if (stats.count >= 2) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Distribution Analysis", style = MaterialTheme.typography.titleSmall)
                                
                                InfoRow("Quartile Q1", stats.q1?.formatDecimal() ?: "N/A")
                                InfoRow("Quartile Q2 (Median)", stats.median.formatDecimal())
                                InfoRow("Quartile Q3", stats.q3?.formatDecimal() ?: "N/A")
                                InfoRow("Interquartile Range (IQR)", 
                                    if (stats.q1 != null && stats.q3 != null) 
                                        (stats.q3!! - stats.q1!!).formatDecimal() 
                                    else "N/A")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatResultRow(label: String, value: String, isPrimary: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (isPrimary) MaterialTheme.typography.titleMedium 
                     else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal
        )
        
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isPrimary) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                value,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Medium,
                color = if (isPrimary) MaterialTheme.colorScheme.onPrimary 
                       else MaterialTheme.colorScheme.onSurface
            )
        }
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

data class StatisticsResult(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val modes: List<Double>,
    val min: Double,
    val max: Double,
    val range: Double,
    val variance: Double,
    val standardDeviation: Double,
    val populationStdDev: Double,
    val q1: Double?,
    val q3: Double?
)

private fun parseNumbers(input: String): List<Double> {
    return input.split(",", ";", "\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .mapNotNull { it.toDoubleOrNull() }
}

private fun calculateStatistics(numbers: List<Double>): StatisticsResult {
    if (numbers.isEmpty()) throw IllegalArgumentException("Cannot calculate statistics for empty list")
    
    val sorted = numbers.sorted()
    val count = numbers.size
    val sum = numbers.sum()
    val mean = sum / count
    
    // Median
    val median = if (count % 2 == 1) {
        sorted[count / 2]
    } else {
        (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
    }
    
    // Mode(s)
    val frequencyMap = numbers.groupingBy { it }.eachCount()
    val maxFreq = frequencyMap.values.maxOrNull() ?: 0
    val modes = if (maxFreq > 1) {
        frequencyMap.filter { it.value == maxFreq }.keys.toList().sorted()
    } else emptyList()
    
    // Min, Max, Range
    val min = sorted.first()
    val max = sorted.last()
    val range = max - min
    
    // Variance and Standard Deviation (sample)
    val variance = if (count > 1) {
        numbers.sumOf { (it - mean) * (it - mean) } / (count - 1)
    } else 0.0
    val standardDeviation = sqrt(variance)
    
    // Population standard deviation
    val populationVariance = numbers.sumOf { (it - mean) * (it - mean) } / count
    val populationStdDev = sqrt(populationVariance)
    
    // Quartiles
    val q1 = calculateQuartile(sorted, 0.25)
    val q3 = calculateQuartile(sorted, 0.75)
    
    return StatisticsResult(
        count = count,
        sum = sum,
        mean = mean,
        median = median,
        modes = modes,
        min = min,
        max = max,
        range = range,
        variance = variance,
        standardDeviation = standardDeviation,
        populationStdDev = populationStdDev,
        q1 = q1,
        q3 = q3
    )
}

private fun calculateQuartile(sorted: List<Double>, percentile: Double): Double? {
    if (sorted.size < 4) return null
    
    val n = sorted.size
    val index = (percentile * (n - 1))
    val lowerIndex = index.toInt()
    val upperIndex = lowerIndex + 1
    
    return if (upperIndex < n) {
        val fraction = index - lowerIndex
        sorted[lowerIndex] + fraction * (sorted[upperIndex] - sorted[lowerIndex])
    } else {
        sorted.last()
    }
}

private fun formatNumber(num: Double): String {
    return if (num == num.toLong().toDouble()) {
        num.toLong().toString()
    } else {
        String.format("%.4f", num).removeSuffix(".0000").removeSuffix(".00").removeSuffix(".0")
    }
}

private fun Double.formatDecimal(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.6f", this).removeSuffix("000000").removeSuffix(".").let {
            if (it.isEmpty() || it == "-") "0" else it
        }
    }
}
