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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.text.DecimalFormat

@Composable
fun FactorialCalculatorScreen(navController: NavHostController) {
    var numberInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<FactorialResult?>(null) }
    var showCalculation by remember { mutableStateOf(false) }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Factorial Calculator",
            subtitle = "Calculate n! for any non-negative integer (0-10,000)",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter a Number", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("\\d*"))) numberInput = it 
                        },
                        label = { Text("n (non-negative integer)") },
                        placeholder = { Text("e.g., 5, 20, 100") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { 
                            Text("Range: 0 to 10,000 (practical limit: 170 for decimal display)", 
                                 color = MaterialTheme.colorScheme.onSurfaceVariant) 
                        }
                    )
                    
                    // Quick select buttons
                    Text("Quick Select:", style = MaterialTheme.typography.bodySmall, 
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(0, 1, 5, 10, 20, 50, 100).forEach { num ->
                            FilterChip(
                                selected = numberInput == num.toString(),
                                onClick = { numberInput = num.toString() },
                                label = { Text("$num!") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = showCalculation,
                            onCheckedChange = { showCalculation = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show calculation steps")
                    }
                    
                    PrimaryButton(
                        text = "Calculate Factorial",
                        onClick = {
                            val n = numberInput.toIntOrNull()
                            if (n != null && n >= 0 && n <= 10000) {
                                result = calculateFactorial(n)
                            }
                        },
                        enabled = numberInput.isNotEmpty() && 
                                  numberInput.toIntOrNull()?.let { it >= 0 && it <= 10000 } == true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Result Display
            result?.let { res ->
                // Main Result Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${res.n}! = ",
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Display full result with copy option
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    res.fullResult,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(res.fullResult))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy result"
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            "Digits: ${res.digitCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scientific Notation
                if (res.scientificNotation != null) {
                    ResultCard(
                        title = "Scientific Notation",
                        result = res.scientificNotation!!,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(res.scientificNotation!!))
                        }
                    )
                }

                // Approximate Value (for large numbers)
                if (res.approximateValue != null) {
                    ResultCard(
                        title = "Approximate Value",
                        result = res.approximateValue!!
                    )
                }

                // Stirling's Approximation
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Stirling's Approximation", style = MaterialTheme.typography.titleMedium)
                        Text("n! ≈ √(2πn) × (n/e)^n", style = MaterialTheme.typography.bodyMedium,
                             fontFamily = FontFamily.Monospace)
                        
                        InfoRow("Approximation", res.stirlingApproximation)
                        InfoRow("Error", "${String.format("%.6f", res.stirlingErrorPercent)}%")
                    }
                }

                // Calculation Steps
                if (showCalculation && res.calculationSteps.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Calculation Steps", style = MaterialTheme.typography.titleMedium,
                                 color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (res.n <= 20) {
                                // Show full multiplication for small numbers
                                res.calculationSteps.forEachIndexed { index, step ->
                                    Text(
                                        step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            } else {
                                // Show abbreviated steps for large numbers
                                Text(
                                    "${res.n}! = ${res.n} × ${res.n - 1} × ${res.n - 2} × ... × 2 × 1",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text("First few multiplications:", style = MaterialTheme.typography.bodySmall,
                                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                res.calculationSteps.take(5).forEach { step ->
                                    Text(
                                        step,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                                
                                if (res.calculationSteps.size > 5) {
                                    Text(
                                        "... (${res.calculationSteps.size - 5} more steps)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    Text(
                                        res.calculationSteps.last(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Additional Information
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Additional Information", style = MaterialTheme.typography.titleSmall)
                        
                        InfoRow("Input (n)", "${res.n}")
                        InfoRow("Exact Digits", "${res.digitCount}")
                        
                        if (res.trailingZeros > 0) {
                            InfoRow("Trailing Zeros", "${res.trailingZeros}")
                        }
                        
                        InfoRow("Is Even", if (res.n >= 2) "Yes" else "No")
                        InfoRow("Divisible by 10", if (res.n >= 5) "Yes" else "No")
                        
                        // Time complexity note
                        HorizontalDivider()
                        Text(
                            "Note: Factorials grow extremely fast. 70! is already larger than a googol (10^100).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interesting Facts
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Interesting Facts", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        when (res.n) {
                            0 -> FactItem("0! = 1 by definition (empty product)")
                            1 -> FactItem("1! = 1")
                            2 -> FactItem("2! = 2 (smallest prime factorial)")
                            6 -> FactItem("6! = 720 (contains digits 0-6 in some order)")
                            24 -> FactItem("24! has 24 digits!")
                            170 -> FactItem("170! ≈ 7.26 × 10^306 (near Double.MAX_VALUE)")
                            else -> {
                                if (res.n in listOf(1, 2, 6, 24, 120, 720, 5040, 40320)) {
                                    FactItem("${res.n}! = ${res.fullResult} (a highly composite number)")
                                } else {
                                    val fact = getInterestingFact(res.n)
                                    if (fact != null) FactItem(fact)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FactorialResult(
    val n: Int,
    val fullResult: String,
    val digitCount: Int,
    val scientificNotation: String?,
    val approximateValue: String?,
    val calculationSteps: List<String>,
    val stirlingApproximation: String,
    val stirlingErrorPercent: Double,
    val trailingZeros: Int
)

private fun calculateFactorial(n: Int): FactorialResult {
    require(n >= 0) { "n must be non-negative" }
    
    // Calculate factorial using BigInteger for exact results
    var factorial = BigInteger.ONE
    val steps = mutableListOf<String>()
    
    for (i in 2..n) {
        factorial = factorial.multiply(BigInteger.valueOf(i.toLong()))
        
        // Store steps only for reasonable sizes
        if (n <= 100 || i <= 10 || i == n) {
            steps.add("$i! = $factorial")
        }
    }
    
    val fullResult = factorial.toString()
    val digitCount = fullResult.length
    
    // Scientific notation for large numbers
    val scientificNotation = if (digitCount > 15) {
        formatScientific(factorial, digitCount)
    } else null
    
    // Approximate value using Double
    val approximateValue = if (n <= 170) {
        calculateDoubleFactorial(n)?.let { DecimalFormat("#,###.##############").format(it) }
    } else null
    
    // Stirling's approximation
    val (stirlingApprox, errorPercent) = stirlingApproximation(n)
    
    // Count trailing zeros
    val trailingZeros = countTrailingZeros(n)
    
    return FactorialResult(
        n = n,
        fullResult = fullResult,
        digitCount = digitCount,
        scientificNotation = scientificNotation,
        approximateValue = approximateValue,
        calculationSteps = steps,
        stirlingApproximation = stirlingApprox,
        stirlingErrorPercent = errorPercent,
        trailingZeros = trailingZeros
    )
}

// Calculate factorial as Double (for n <= 170 to avoid overflow)
private fun calculateDoubleFactorial(n: Int): Double? {
    if (n > 170) return null // Would overflow Double
    
    var result = 1.0
    for (i in 2..n) {
        result *= i.toDouble()
    }
    return result
}

// Format large BigInteger in scientific notation
private fun formatScientific(value: BigInteger, digitCount: Int): String {
    val str = value.toString()
    
    if (digitCount <= 15) return str
    
    // Get first few significant digits
    val firstPart = str.take(15)
    val exponent = digitCount - 1
    
    return "$firstPart... × 10^$exponent"
}

// Stirling's approximation: n! ≈ √(2πn) * (n/e)^n
private fun stirlingApproximation(n: Int): Pair<String, Double> {
    if (n == 0 || n == 1) return Pair("1.0", 0.0)
    
    val pi = kotlin.math.PI
    val e = kotlin.math.E
    
    val sqrtPart = kotlin.math.sqrt(2.0 * pi * n)
    val powerPart = kotlin.math.pow(n.toDouble() / e, n.toDouble())
    val approximation = sqrtPart * powerPart
    
    // Calculate actual for comparison (using log for large numbers)
    val actualLog = logFactorial(n)
    val approxLog = kotlin.math.log(approximation)
    val errorPercent = kotlin.math.abs((approxLog - actualLog) / actualLog) * 100
    
    val formatted = when {
        approximation < 1000 -> String.format("%.4f", approximation)
        approximation < 1e10 -> String.format("%.2e", approximation)
        else -> String.format("%.4e", approximation)
    }
    
    return Pair(formatted, errorPercent)
}

// Calculate ln(n!) using sum of logarithms
private fun logFactorial(n: Double): Double {
    var sum = 0.0
    for (i in 1..n.toInt()) {
        sum += kotlin.math.log(i.toDouble())
    }
    return sum
}

// Count trailing zeros in n!
// Formula: floor(n/5) + floor(n/25) + floor(n/125) + ...
private fun countTrailingZeros(n: Int): Int {
    var count = 0
    var divisor = 5
    while (divisor <= n) {
        count += n / divisor
        divisor *= 5
    }
    return count
}

@Composable
private fun FactItem(fact: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(fact, style = MaterialTheme.typography.bodyMedium)
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

private fun getInterestingFact(n: Int): String? {
    return when {
        n == 69 -> "69! - nice!"
        n == 420 -> "420! - nice!"
        n % 100 == 0 -> "${n}! ends with ${countTrailingZeros(n)} zeros"
        isPrime(n.toLong()) -> "$n is prime, so n! is divisible by all numbers from 1 to $n"
        n == 52 -> "52! = number of ways to shuffle a deck of cards"
        n == 64 -> "64! exceeds the number of atoms in the observable universe (~10^80)"
        else -> null
    }
}
