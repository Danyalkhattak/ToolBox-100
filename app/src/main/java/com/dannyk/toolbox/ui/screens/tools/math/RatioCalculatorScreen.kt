package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.SegmentedButton
import kotlin.math.*
import androidx.compose.foundation.layout.RowScope

@Composable
fun RatioCalculatorScreen(navController: NavHostController) {
    var valueA by remember { mutableStateOf("") }
    var valueB by remember { mutableStateOf("") }
    var scaleValue by remember { mutableStateOf("") }
    var scaleTarget by remember { mutableStateOf("A") } // A or B to scale to
    
    var simplifiedRatio by remember { mutableStateOf<RatioResult?>(null) }
    var equivalentRatios by remember { mutableStateOf<List<RatioPair>>(emptyList()) }
    var scaledRatio by remember { mutableStateOf<RatioPair?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Ratio Calculator",
            subtitle = "Simplify, find equivalents, and scale ratios",
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Enter Ratio A:B", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedTextField(
                                value = valueA,
                                onValueChange = { 
                                    if (it.isEmpty() || it.matches(Regex("-?\\d*"))) valueA = it 
                                },
                                label = { Text("A") },
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                ":",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            OutlinedTextField(
                                value = valueB,
                                onValueChange = { 
                                    if (it.isEmpty() || it.matches(Regex("-?\\d*"))) valueB = it 
                                },
                                label = { Text("B") },
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }

            // Action Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrimaryButton(
                        text = "Simplify",
                        onClick = {
                            val a = valueA.toLongOrNull() ?: return@PrimaryButton
                            val b = valueB.toLongOrNull() ?: return@PrimaryButton
                            if (a == 0L && b == 0L) return@PrimaryButton
                            
                            simplifiedRatio = simplifyRatio(a, b)
                            equivalentRatios = generateEquivalentRatios(simplifiedRatio!!.a, simplifiedRatio!!.b)
                        },
                        enabled = valueA.isNotEmpty() && valueB.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                    
                    SecondaryButton(
                        text = "Find Equivalents",
                        onClick = {
                            val a = valueA.toLongOrNull() ?: return@SecondaryButton
                            val b = valueB.toLongOrNull() ?: return@SecondaryButton
                            if (a == 0L && b == 0L) return@SecondaryButton
                            
                            simplifiedRatio = simplifyRatio(a, b)
                            equivalentRatios = generateEquivalentRatios(simplifiedRatio!!.a, simplifiedRatio!!.b)
                        },
                        enabled = valueA.isNotEmpty() && valueB.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Scale Section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Scale Ratio", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = scaleValue,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*"))) scaleValue = it 
                            },
                            label = { Text("Target Value") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // Target selector
                        SegmentedButtonContainer {
                            FilterChip(
                                selected = scaleTarget == "A",
                                onClick = { scaleTarget = "A" },
                                label = { Text("Scale A to this value") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = scaleTarget == "B",
                                onClick = { scaleTarget = "B" },
                                label = { Text("Scale B to this value") }
                            )
                        }
                        
                        Button(
                            onClick = {
                                val targetVal = scaleValue.toDoubleOrNull() ?: return@Button
                                val a = valueA.toDoubleOrNull() ?: return@Button
                                val b = valueB.toDoubleOrNull() ?: return@Button
                                
                                if (scaleTarget == "A") {
                                    val factor = targetVal / a
                                    scaledRatio = RatioPair(targetVal, b * factor)
                                } else {
                                    val factor = targetVal / b
                                    scaledRatio = RatioPair(a * factor, targetVal)
                                }
                            },
                            enabled = scaleValue.isNotEmpty() && valueA.isNotEmpty() && valueB.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calculate Scaled Ratio")
                        }
                    }
                }
            }

            // Simplified Result
            item {
                simplifiedRatio?.let { result ->
                    ResultCard(
                        title = "Simplified Ratio",
                        result = "${result.a} : ${result.b}"
                    )
                    
                    if (result.gcd > 1) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Simplification Steps", style = MaterialTheme.typography.titleSmall)
                                InfoRow("Original Ratio", "${result.originalA} : ${result.originalB}")
                                InfoRow("GCD of values", "${result.gcd}")
                                InfoRow("Divide A by GCD", "${result.originalA} ÷ ${result.gcd} = ${result.a}")
                                InfoRow("Divide B by GCD", "${result.originalB} ÷ ${result.gcd} = ${result.b}")
                            }
                        }
                    }
                }
            }

            // Equivalent Ratios
            item {
                if (equivalentRatios.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Equivalent Ratios",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            equivalentRatios.forEach { ratio ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            "${ratio.a.formatToIntIfWhole()} : ${ratio.b.formatToIntIfWhole()}",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Text(
                                        "= ${(ratio.a / ratio.b).formatDecimal()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Scaled Result
            item {
                scaledRatio?.let { ratio ->
                    ResultCard(
                        title = "Scaled Ratio",
                        result = "${ratio.a.formatToIntIfWhole()} : ${ratio.b.formatToIntIfWhole()}"
                    )
                    
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Scaling Details", style = MaterialTheme.typography.titleSmall)
                            val originalA = valueA.toDoubleOrNull() ?: 0.0
                            val originalB = valueB.toDoubleOrNull() ?: 0.0
                            val targetVal = scaleValue.toDoubleOrNull() ?: 0.0
                            
                            if (scaleTarget == "A") {
                                val factor = targetVal / originalA
                                InfoRow("Scaling Factor", factor.formatDecimal())
                                InfoRow("New A value", "$targetVal")
                                InfoRow("New B value", (originalB * factor).formatToIntIfWhole())
                            } else {
                                val factor = targetVal / originalB
                                InfoRow("Scaling Factor", factor.formatDecimal())
                                InfoRow("New A value", (originalA * factor).formatToIntIfWhole())
                                InfoRow("New B value", "$targetVal")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RatioResult(
    val originalA: Long,
    val originalB: Long,
    val a: Long,
    val b: Long,
    val gcd: Long
)

data class RatioPair(val a: Double, val b: Double)

private fun simplifyRatio(a: Long, b: Long): RatioResult {
    val gcdVal = gcd(kotlin.math.abs(a), kotlin.math.abs(b))
    
    // Handle case where both are zero or one is zero
    return when {
        a == 0L && b == 0L -> RatioResult(0, 0, 0, 0, 1)
        b == 0L -> RatioResult(a, b, 1, 0, kotlin.math.abs(a))
        a == 0L -> RatioResult(a, b, 0, 1, kotlin.math.abs(b))
        else -> {
            val signA = if ((a < 0) xor (b < 0)) -1 else 1
            RatioResult(
                originalA = a,
                originalB = b,
                a = signA * kotlin.math.abs(a) / gcdVal,
                b = kotlin.math.abs(b) / gcdVal,
                gcd = gcdVal
            )
        }
    }
}

private fun generateEquivalentRatios(a: Long, b: Long): List<RatioPair> {
    val ratios = mutableListOf<RatioPair>()
    for (i in 2..10) {
        ratios.add(RatioPair((a * i).toDouble(), (b * i).toDouble()))
    }
    return ratios
}

// GCD using Euclidean algorithm
private fun gcd(a: Long, b: Long): Long {
    var x = a
    var y = b
    while (y != 0L) {
        val temp = y
        y = x % y
        x = temp
    }
    return kotlin.math.max(x, 1)
}

@Composable
private fun SegmentedButtonContainer(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        content = content
    )
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

private fun Double.formatToIntIfWhole(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        String.format("%.4f", this).removeSuffix(".0000")
    }
}

private fun Double.formatDecimal(): String {
    return String.format("%.6f", this).removeSuffix(".000000").let {
        if (it.isEmpty()) "0" else it
    }
}
