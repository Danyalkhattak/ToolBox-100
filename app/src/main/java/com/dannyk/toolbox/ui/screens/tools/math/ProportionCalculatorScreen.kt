package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.ScrollState
import kotlin.math.*

@Composable
fun ProportionCalculatorScreen(navController: NavHostController) {
    var valueA by remember { mutableStateOf("") }
    var valueB by remember { mutableStateOf("") }
    var valueC by remember { mutableStateOf("") }
    var valueD by remember { mutableStateOf("") }
    var solveFor by remember { mutableStateOf("D") } // Which value to solve for
    
    var result by remember { mutableStateOf<ProportionResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Proportion Calculator",
            subtitle = "Solve A:B = C:D proportions using cross-multiplication",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Proportion Visual Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "A : B = C : D",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cross-multiplication: A × D = B × C",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Fields - Row format showing proportion visually
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Enter Values (leave one empty to solve)", style = MaterialTheme.typography.titleMedium)
                    
                    // First row: A : B
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProportionInputField(
                            label = "A",
                            value = valueA,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) valueA = it 
                            },
                            isHighlighted = solveFor == "A"
                        )
                        
                        Text(":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        
                        ProportionInputField(
                            label = "B",
                            value = valueB,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) valueB = it 
                            },
                            isHighlighted = solveFor == "B"
                        )
                    }
                    
                    // Equals sign
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("=", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    
                    // Second row: C : D
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProportionInputField(
                            label = "C",
                            value = valueC,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) valueC = it 
                            },
                            isHighlighted = solveFor == "C"
                        )
                        
                        Text(":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        
                        ProportionInputField(
                            label = "D",
                            value = valueD,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) valueD = it 
                            },
                            isHighlighted = solveFor == "D"
                        )
                    }
                    
                    // Solve For Selector
                    Text("Solve for:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("A", "B", "C", "D").forEach { option ->
                            FilterChip(
                                selected = solveFor == option,
                                onClick = { solveFor = option },
                                label = { Text(option) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Calculate Button
            PrimaryButton(
                text = "Calculate Missing Value",
                onClick = {
                    val a = valueA.toDoubleOrNull()
                    val b = valueB.toDoubleOrNull()
                    val c = valueC.toDoubleOrNull()
                    val d = valueD.toDoubleOrNull()
                    
                    result = solveProportion(a, b, c, d, solveFor)
                },
                enabled = canCalculate(valueA, valueB, valueC, valueD, solveFor)
            )

            // Result Display
            result?.let { res ->
                Spacer(modifier = Modifier.height(8.dp))
                
                if (res.isSuccess) {
                    ResultCard(
                        title = "${res.solveFor} = ",
                        result = res.resultValue.formatResult()
                    )
                    
                    // Step-by-step explanation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Solution Steps",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            res.steps.forEachIndexed { index, step ->
                                Text(
                                    "${index + 1}. $step",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                    
                    // Verification
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Verification", style = MaterialTheme.typography.titleSmall)
                            
                            val finalValues = getFinalValues(res)
                            InfoRow("A × D", "${finalValues.a} × ${finalValues.d} = ${(finalValues.a * finalValues.d).formatResult()}")
                            InfoRow("B × C", "${finalValues.b} × ${finalValues.c} = ${(finalValues.b * finalValues.c).formatResult()}")
                            
                            val ad = finalValues.a * finalValues.d
                            val bc = finalValues.b * finalValues.c
                            val isEqual = kotlin.math.abs(ad - bc) < 0.0001
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isEqual) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isEqual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isEqual) "Cross-products are equal" else "Values don't match",
                                    color = if (isEqual) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    ErrorCard(res.errorMessage ?: "Unknown error occurred")
                }
            }

            // Quick Examples Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Quick Examples", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExampleRow("If 2:3 = 4:x", "x = 6") { 
                        valueA = "2"; valueB = "3"; valueC = "4"; valueD = ""; solveFor = "D" 
                    }
                    ExampleRow("If x:5 = 6:10", "x = 3") { 
                        valueA = ""; valueB = "5"; valueC = "6"; valueD = "10"; solveFor = "A" 
                    }
                    ExampleRow("If 3:x = 9:15", "x = 5") { 
                        valueA = "3"; valueB = ""; valueC = "9"; valueD = "15"; solveFor = "B" 
                    }
                    ExampleRow("If 4:7 = x:21", "x = 12") { 
                        valueA = "4"; valueB = "7"; valueC = ""; valueD = "21"; solveFor = "C" 
                    }
                }
            }
        }
    }
}

@Composable
private fun ProportionInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isHighlighted: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.width(110.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isHighlighted) MaterialTheme.colorScheme.primary 
                                 else MaterialTheme.colorScheme.outline,
            focusedLabelColor = if (isHighlighted) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun ExampleRow(description: String, answer: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(description, style = MaterialTheme.typography.bodyMedium)
                Text(answer, style = MaterialTheme.typography.labelSmall, 
                     color = MaterialTheme.colorScheme.primary)
            }
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Try example",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
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
            Text(message, color = MaterialTheme.colorScheme.error)
        }
    }
}

data class ProportionResult(
    val isSuccess: Boolean,
    val solveFor: String,
    val resultValue: Double,
    val steps: List<String>,
    val errorMessage: String? = null,
    val a: Double = 0.0,
    val b: Double = 0.0,
    val c: Double = 0.0,
    val d: Double = 0.0
)

private data class FinalValues(val a: Double, val b: Double, val c: Double, val d: Double)

private fun getFinalValues(result: ProportionResult): FinalValues {
    return when (result.solveFor) {
        "A" -> FinalValues(result.resultValue, result.b, result.c, result.d)
        "B" -> FinalValues(result.a, result.resultValue, result.c, result.d)
        "C" -> FinalValues(result.a, result.b, result.resultValue, result.d)
        "D" -> FinalValues(result.a, result.b, result.c, result.resultValue)
        else -> FinalValues(result.a, result.b, result.c, result.d)
    }
}

private fun canCalculate(a: String, b: String, c: String, d: String, solveFor: String): Boolean {
    val values = listOf(a, b, c, d)
    val emptyCount = values.count { it.isEmpty() }
    
    // Need exactly 3 values filled and the empty one should be what we're solving for
    if (emptyCount != 1) return false
    
    return when (solveFor) {
        "A" -> a.isEmpty() && b.isNotEmpty() && c.isNotEmpty() && d.isNotEmpty()
        "B" -> b.isEmpty() && a.isNotEmpty() && c.isNotEmpty() && d.isNotEmpty()
        "C" -> c.isEmpty() && a.isNotEmpty() && b.isNotEmpty() && d.isNotEmpty()
        "D" -> d.isEmpty() && a.isNotEmpty() && b.isNotEmpty() && c.isNotEmpty()
        else -> false
    }.also { valid ->
        if (!valid && emptyCount == 1) {
            // Auto-detect which field is empty
            when {
                a.isEmpty() -> {} // Would need auto-switch logic
            }
        }
    }
}

private fun solveProportion(
    a: Double?,
    b: Double?,
    c: Double?,
    d: Double?,
    solveFor: String
): ProportionResult {
    val steps = mutableListOf<String>()
    
    // Validate inputs
    val hasEmpty = listOf(a, b, c, d).any { it == null }
    if (hasEmpty && solveFor !in listOf("A", "B", "C", "D")) {
        return ProportionResult(false, solveFor, 0.0, emptyList(), "Please enter exactly 3 values")
    }
    
    // Check for zeros in wrong places
    when (solveFor) {
        "A" -> {
            if (b == 0.0 || d == 0.0) return ProportionResult(false, solveFor, 0.0, emptyList(), "Cannot divide by zero (B or D cannot be 0)")
        }
        "B" -> {
            if (a == 0.0 || c == 0.0) return ProportionResult(false, solveFor, 0.0, emptyList(), "Cannot divide by zero (A or C cannot be 0)")
        }
        "C" -> {
            if (a == 0.0 || d == 0.0) return ProportionResult(false, solveFor, 0.0, emptyList(), "Cannot divide by zero (A or D cannot be 0)")
        }
        "D" -> {
            if (b == 0.0 || c == 0.0) return ProportionResult(false, solveFor, 0.0, emptyList(), "Cannot divide by zero (B or C cannot be 0)")
        }
    }
    
    val resultValue: Double
    val finalA = a ?: 0.0
    val finalB = b ?: 0.0
    val finalC = c ?: 0.0
    val finalD = d ?: 0.0
    
    steps.add("Given proportion: ${formatInProportion(finalA, finalB, finalC, finalD)}")
    steps.add("")
    steps.add("Using cross-multiplication: A × D = B × C")
    
    when (solveFor) {
        "A" -> {
            // A × D = B × C => A = (B × C) / D
            val bc = finalB!! * finalC!!
            resultValue = bc / finalD!!
            steps.add("")
            steps.add("Substitute known values:")
            steps.add("${solveFor} × ${finalD} = ${finalB} × ${finalC}")
            steps.add("${solveFor} × ${finalD} = $bc")
            steps.add("")
            steps.add("Isolate ${solveFor}:")
            steps.add("${solveFor} = $bc ÷ ${finalD}")
            steps.add("${solveFor} = $resultValue")
        }
        "B" -> {
            // A × D = B × C => B = (A × D) / C
            val ad = finalA!! * finalD!!
            resultValue = ad / finalC!!
            steps.add("")
            steps.add("Substitute known values:")
            steps.add("${finalA} × ${finalD} = ${solveFor} × ${finalC}")
            steps.add("$ad = ${solveFor} × ${finalC}")
            steps.add("")
            steps.add("Isolate ${solveFor}:")
            steps.add("${solveFor} = $ad ÷ ${finalC}")
            steps.add("${solveFor} = $resultValue")
        }
        "C" -> {
            // A × D = B × C => C = (A × D) / B
            val ad = finalA!! * finalD!!
            resultValue = ad / finalB!!
            steps.add("")
            steps.add("Substitute known values:")
            steps.add("${finalA} × ${finalD} = ${finalB} × ${solveFor}")
            steps.add("$ad = ${finalB} × ${solveFor}")
            steps.add("")
            steps.add("Isolate ${solveFor}:")
            steps.add("${solveFor} = $ad ÷ ${finalB}")
            steps.add("${solveFor} = $resultValue")
        }
        "D" -> {
            // A × D = B × C => D = (B × C) / A
            val bc = finalB!! * finalC!!
            resultValue = bc / finalA!!
            steps.add("")
            steps.add("Substitute known values:")
            steps.add("${finalA} × ${solveFor} = ${finalB} × ${finalC}")
            steps.add("${finalA} × ${solveFor} = $bc")
            steps.add("")
            steps.add("Isolate ${solveFor}:")
            steps.add("${solveFor} = $bc ÷ ${finalA}")
            steps.add("${solveFor} = $resultValue")
        }
        else -> return ProportionResult(false, solveFor, 0.0, emptyList(), "Invalid variable to solve for")
    }
    
    return ProportionResult(true, solveFor, resultValue, steps, 
        a = finalA, b = finalB, c = finalC, d = finalD)
}

private fun formatInProportion(a: Double?, b: Double?, c: Double?, d: Double?): String {
    val fa = a?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "?"
    val fb = b?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "?"
    val fc = c?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "?"
    val fd = d?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() } ?: "?"
    return "$fa : $fb = $fc : $fd"
}

private fun Double.formatResult(): String {
    return if (this == this.toLong().toDouble()) {
        this.toLong().toString()
    } else {
        // Check if it's a simple fraction
        val decimalStr = this.toString()
        if (decimalStr.contains(".") && decimalStr.split(".")[1].length <= 6) {
            String.format("%.6f", this).removeSuffix("000000").removeSuffix(".").let {
                if (it.endsWith(".")) it.dropLast(1) else it
            }
        } else {
            String.format("%.10f", this)
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
