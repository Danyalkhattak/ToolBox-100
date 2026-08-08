package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material.icons.filled.Check

data class Fraction(val numerator: Long, val denominator: Long)

@Composable
fun FractionCalculatorScreen(navController: NavHostController) {
    var num1 by remember { mutableStateOf("") }
    var den1 by remember { mutableStateOf("") }
    var num2 by remember { mutableStateOf("") }
    var den2 by remember { mutableStateOf("") }
    var selectedOperation by remember { mutableStateOf(0) } // 0=+, 1=-, 2=*, 3=/
    var result by remember { mutableStateOf<FractionResult?>(null) }
    var showSteps by remember { mutableStateOf(false) }

    val operations = listOf("+", "-", "×", "÷")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Fraction Calculator",
            subtitle = "Add, subtract, multiply, or divide fractions",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First Fraction Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("First Fraction", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = num1,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*"))) num1 = it 
                            },
                            label = { Text("Numerator") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Divider(modifier = Modifier.fillMaxWidth(), 
                            modifier = Modifier.width(80.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = den1,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*"))) den1 = it 
                            },
                            label = { Text("Denominator") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }

            // Operation Selector
            SegmentedButtonContainer {
                operations.forEachIndexed { index, op ->
                    FilterChip(
                        selected = selectedOperation == index,
                        onClick = { selectedOperation = index },
                        label = { 
                            Text(op, style = MaterialTheme.typography.titleLarge) 
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            // Second Fraction Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Second Fraction", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = num2,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*"))) num2 = it 
                            },
                            label = { Text("Numerator") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Divider(modifier = Modifier.fillMaxWidth(), 
                            modifier = Modifier.width(80.dp),
                            thickness = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = den2,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("-?\\d*"))) den2 = it 
                            },
                            label = { Text("Denominator") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }

            // Calculate Button
            PrimaryButton(
                text = "Calculate",
                onClick = {
                    val n1 = num1.toLongOrNull() ?: 0L
                    val d1 = den1.toLongOrNull() ?: 1L
                    val n2 = num2.toLongOrNull() ?: 0L
                    val d2 = den2.toLongOrNull() ?: 1L
                    
                    if (d1 == 0L || d2 == 0L) return@PrimaryButton
                    
                    val fraction1 = Fraction(n1, d1)
                    val fraction2 = Fraction(n2, d2)
                    
                    result = calculateFractions(fraction1, fraction2, selectedOperation)
                },
                enabled = num1.isNotEmpty() && den1.isNotEmpty() && 
                          num2.isNotEmpty() && den2.isNotEmpty() &&
                          den1 != "0" && den2 != "0"
            )

            // Show Steps Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = showSteps,
                    onCheckedChange = { showSteps = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show step-by-step solution")
            }

            // Result Display
            result?.let { res ->
                ResultCard(
                    title = "Result",
                    result = formatFractionResult(res)
                )

                if (showSteps) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                "Step-by-Step Solution",
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
                }

                // Additional Info
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Additional Information", style = MaterialTheme.typography.titleSmall)
                        
                        InfoRow("Simplified Form", res.simplifiedFraction)
                        if (res.mixedNumber != null) {
                            InfoRow("Mixed Number", res.mixedNumber!!)
                        }
                        InfoRow("Decimal Value", String.format("%.6f", res.decimalValue))
                    }
                }
            }
        }
    }
}

@Composable
private fun SegmentedButtonContainer(content: @Composable RowScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            content = content
        )
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

data class FractionResult(
    val simplifiedFraction: String,
    val mixedNumber: String?,
    val decimalValue: Double,
    val steps: List<String>
)

private fun calculateFractions(f1: Fraction, f2: Fraction, operation: Int): FractionResult {
    val steps = mutableListOf<String>()
    
    // Normalize fractions (move negative to numerator)
    val normF1 = Fraction(
        if (f1.denominator < 0) -f1.numerator else f1.numerator,
        kotlin.math.abs(f1.denominator)
    )
    val normF2 = Fraction(
        if (f2.denominator < 0) -f2.numerator else f2.numerator,
        kotlin.math.abs(f2.denominator)
    )
    
    val operationSymbol = listOf("+", "-", "×", "÷")[operation]
    steps.add("Problem: ${formatFraction(normF1)} $operationSymbol ${formatFraction(normF2)}")
    
    var resultNum: Long
    var resultDen: Long
    
    when (operation) {
        0 -> { // Addition
            val lcd = lcm(kotlin.math.abs(normF1.denominator), kotlin.math.abs(normF2.denominator))
            val mult1 = lcd / kotlin.math.abs(normF1.denominator)
            val mult2 = lcd / kotlin.math.abs(normF2.denominator)
            
            steps.add("Find LCD of ${normF1.denominator} and ${normF2.denominator}: $lcd")
            steps.add("Convert: (${normF1.numerator} × $mult1)/$lcd + (${normF2.numerator} × $mult2)/$lcd")
            
            resultNum = normF1.numerator * mult1 + normF2.numerator * mult2
            resultDen = lcd
            
            steps.add("Add numerators: $resultNum/$resultDen")
        }
        1 -> { // Subtraction
            val lcd = lcm(kotlin.math.abs(normF1.denominator), kotlin.math.abs(normF2.denominator))
            val mult1 = lcd / kotlin.math.abs(normF1.denominator)
            val mult2 = lcd / kotlin.math.abs(normF2.denominator)
            
            steps.add("Find LCD of ${normF1.denominator} and ${normF2.denominator}: $lcd")
            steps.add("Convert: (${normF1.numerator} × $mult1)/$lcd − (${normF2.numerator} × $mult2)/$lcd")
            
            resultNum = normF1.numerator * mult1 - normF2.numerator * mult2
            resultDen = lcd
            
            steps.add("Subtract numerators: $resultNum/$resultDen")
        }
        2 -> { // Multiplication
            resultNum = normF1.numerator * normF2.numerator
            resultDen = normF1.denominator * normF2.denominator
            
            steps.add("Multiply numerators: ${normF1.numerator} × ${normF2.numerator} = $resultNum")
            steps.add("Multiply denominators: ${normF1.denominator} × ${normF2.denominator} = $resultDen")
            steps.add("Result before simplifying: $resultNum/$resultDen")
        }
        else -> { // Division
            // a/b ÷ c/d = a/b × d/c
            resultNum = normF1.numerator * normF2.denominator
            resultDen = normF1.denominator * normF2.numerator
            
            steps.add("Convert division to multiplication:")
            steps.add("${formatFraction(normF1)} ÷ ${formatFraction(normF2)} = ${formatFraction(normF1)} × ${formatFraction(Fraction(normF2.denominator, normF2.numerator))}")
            steps.add("Multiply numerators: ${normF1.numerator} × ${normF2.denominator} = $resultNum")
            steps.add("Multiply denominators: ${normF1.denominator} × ${normF2.numerator} = $resultDen")
            steps.add("Result before simplifying: $resultNum/$resultDen")
        }
    }
    
    // Handle negative denominator
    if (resultDen < 0) {
        resultNum = -resultNum
        resultDen = -resultDen
    }
    
    // Simplify
    val gcdVal = gcd(kotlin.math.abs(resultNum), kotlin.math.abs(resultDen))
    val simpNum = resultNum / gcdVal
    val simpDen = resultDen / gcdVal
    
    if (gcdVal > 1) {
        steps.add("Simplify: GCD($resultNum, $resultDen) = $gcdVal")
        steps.add("Divide both by $gcdVal: $simpNum/$simpDen")
    }
    
    val simplifiedStr = if (simpDen == 1L) "$simpNum" else "$simpNum/$simpDen"
    
    // Mixed number for improper fractions
    val mixedNumber = if (kotlin.math.abs(simpNum) >= simpDen && simpDen != 1L) {
        val wholePart = simpNum / simpDen
        val remainder = kotlin.math.abs(simpNum) % simpDen
        if (remainder == 0L) "$wholePart" else "$wholePart $remainder/$simpDen"
    } else null
    
    val decimalValue = simpNum.toDouble() / simpDen.toDouble()
    
    return FractionResult(simplifiedStr, mixedNumber, decimalValue, steps)
}

private fun formatFraction(f: Fraction): String {
    return if (f.denominator == 1L) "${f.numerator}" else "${f.numerator}/${f.denominator}"
}

private fun formatFractionResult(res: FractionResult): String {
    return buildString {
        append(res.simplifiedFraction)
        res.mixedNumber?.let {
            append(" = ")
            append(it)
        }
        append(" ≈ ")
        append(String.format("%.6f", res.decimalValue))
    }
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
    return x
}

// LCM using GCD formula: LCM(a,b) = |a*b| / GCD(a,b)
private fun lcm(a: Long, b: Long): Long {
    return if (a == 0L || b == 0L) 0L else kotlin.math.abs(a * b) / gcd(a, b)
}
