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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState
import kotlin.math.*

@Composable
fun PowerCalculatorScreen(navController: NavHostController) {
    var baseInput by remember { mutableStateOf("") }
    var exponentInput by remember { mutableStateOf("") }
    var showSteps by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<PowerResult?>(null) }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Power Calculator",
            subtitle = "Calculate base^exponent with step-by-step multiplication",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Formula Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "base^exponent",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Supports positive, negative, and decimal exponents",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter Values", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = baseInput,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) baseInput = it 
                        },
                        label = { Text("Base") },
                        placeholder = { Text("e.g., 2, -3, 1.5") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    OutlinedTextField(
                        value = exponentInput,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) exponentInput = it 
                        },
                        label = { Text("Exponent") },
                        placeholder = { Text("e.g., 10, -2, 0.5, 2.5") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { 
                            Text("For integer exponents: -10000 to 10000 | For decimal: limited precision", 
                                 color = MaterialTheme.colorScheme.onSurfaceVariant) 
                        }
                    )
                    
                    // Quick examples
                    Text("Common Powers:", style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Pair("2^10", Pair("2", "10")),
                            Pair("2^20", Pair("2", "20")),
                            Pair("10^6", Pair("10", "6")),
                            Pair("3^7", Pair("3", "7"))
                        ).forEach { (label, values) ->
                            FilterChip(
                                selected = baseInput == values.first && exponentInput == values.second,
                                onClick = { 
                                    baseInput = values.first
                                    exponentInput = values.second
                                },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = showSteps,
                            onCheckedChange = { showSteps = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show calculation steps")
                    }
                    
                    PrimaryButton(
                        text = "Calculate Power",
                        onClick = {
                            val base = baseInput.toDoubleOrNull()
                            val exp = exponentInput.toDoubleOrNull()
                            
                            if (base != null && exp != null) {
                                result = calculatePower(base, exp, showSteps)
                            }
                        },
                        enabled = baseInput.isNotEmpty() && exponentInput.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Results Section
            result?.let { res ->
                // Main Result
                ResultCard(
                    title = "${res.base}^${res.exponent} = ",
                    result = res.resultDisplay,
                    onCopy = { clipboardManager.setText(AnnotatedString(res.resultDisplay)) }
                )

                // Exact vs Approximate info
                if (!res.isExact) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Result is approximate due to decimal exponent or large value",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Additional Representations
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Number Properties", style = MaterialTheme.typography.titleMedium)
                        
                        InfoRow("Scientific Notation", res.scientificNotation)
                        
                        if (res.naturalLog != null) {
                            InfoRow("Natural Log (ln)", res.naturalLog!!)
                        }
                        
                        if (res.log10 != null) {
                            InfoRow("Log Base 10", res.log10!!)
                        }
                        
                        if (res.digitCount != null) {
                            InfoRow("Total Digits", "${res.digitCount}")
                        }
                        
                        if (res.isInteger) {
                            val intVal = if (res.result == res.result.toLong().toDouble()) res.result.toLong() else null
                            if (intVal != null) {
                                InfoRow("Is Even", if (intVal % 2L == 0L) "Yes" else "No")
                                InfoRow("Is Perfect Square", isPerfectSquare(intVal).let { if (it) "Yes (${sqrtLong(intVal)}²)" else "No" })
                            }
                        }
                    }
                }

                // Step-by-step calculation
                if (showSteps && res.steps.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Calculation Steps", style = MaterialTheme.typography.titleMedium,
                                 color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            when {
                                // Integer exponent steps
                                res.exponent == res.exponent.toLong().toDouble() && 
                                res.exponent >= 0 && 
                                res.exponent <= 20 -> {
                                    res.steps.forEachIndexed { index, step ->
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = if (index == res.steps.lastIndex) 
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else 
                                                MaterialTheme.colorScheme.surface,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                step,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                fontFamily = FontFamily.Monospace,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                                
                                // Negative integer exponent
                                res.exponent < 0 && res.exponent == res.exponent.toLong().toDouble() -> {
                                    Text("Negative exponent means reciprocal:", style = MaterialTheme.typography.bodyMedium)
                                    Text("${res.base}^(${res.exponent.toInt()}) = 1 / ${res.base}^${kotlin.math.abs(res.exponent.toInt())}", 
                                         style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    res.steps.forEach { step ->
                                        Text(step, style = MaterialTheme.typography.bodySmall, 
                                             fontFamily = FontFamily.Monospace)
                                    }
                                }
                                
                                // Decimal/fractional exponent
                                else -> {
                                    res.steps.forEach { step ->
                                        Text(step, style = MaterialTheme.typography.bodyMedium,
                                             fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                // Special cases explanation
                if (res.specialNote != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Special Case", style = MaterialTheme.typography.titleSmall,
                                     color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(res.specialNote!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Related calculations
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Related Calculations", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val expInt = if (res.exponent == res.exponent.toLong().toDouble()) res.exponent.toInt() else null
                        if (expInt != null) {
                            RelatedCalcRow("${res.base}^${expInt + 1}", 
                                calculateQuickPower(res.base, expInt + 1))
                            RelatedCalcRow("${res.base}^${expInt - 1}", 
                                if (expInt > 1) calculateQuickPower(res.base, expInt - 1) else "N/A")
                            
                            if (res.base != 0.0) {
                                RelatedCalcRow("${res.base}^${-expInt}", 
                                    calculateQuickPower(res.base, -expInt))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PowerResult(
    val base: Double,
    val exponent: Double,
    val result: Double,
    val resultDisplay: String,
    val scientificNotation: String,
    val naturalLog: String?,
    val log10: String?,
    val digitCount: Int?,
    val isExact: Boolean,
    val isInteger: Boolean,
    val steps: List<String>,
    val specialNote: String?
)

private fun calculatePower(base: Double, exponent: Double, includeSteps: Boolean): PowerResult {
    val result = base.pow(exponent)
    
    // Check for special cases
    val specialNote = getSpecialCase(base, exponent)
    
    // Determine if result is exact
    val isExact = isExactPower(base, exponent)
    val isInteger = result == result.toLong().toDouble() && 
                   !result.isInfinite() && 
                   !result.isNaN()
    
    // Format result display
    val resultDisplay = formatResult(result, isInteger)
    
    // Scientific notation
    val scientificNotation = formatScientific(result)
    
    // Logarithms (only for positive results)
    val naturalLog = if (result > 0) String.format("%.10f", kotlin.math.ln(result)).removeSuffix("000000000").removeSuffix(".") else null
    val log10 = if (result > 0) String.format("%.10f", kotlin.math.log10(result)).removeSuffix("000000000").removeSuffix(".") else null
    
    // Digit count for integers
    val digitCount = if (isInteger) result.toLong().toString().length else null
    
    // Generate steps
    val steps = generateSteps(base, exponent, result, includeSteps)
    
    return PowerResult(
        base = base,
        exponent = exponent,
        result = result,
        resultDisplay = resultDisplay,
        scientificNotation = scientificNotation,
        naturalLog = naturalLog,
        log10 = log10,
        digitCount = digitCount,
        isExact = isExact,
        isInteger = isInteger,
        steps = steps,
        specialNote = specialNote
    )
}

private fun formatResult(result: Double, isInteger: Boolean): String {
    return when {
        result.isInfinite() -> if (result > 0) "∞ (Infinity)" else "-∞ (Negative Infinity)"
        result.isNaN() -> "Undefined"
        isInteger -> DecimalFormat("#,###").format(result.toLong())
        kotlin.math.abs(result) >= 1e15 || (kotlin.math.abs(result) < 0.001 && result != 0.0) -> 
            String.format("%.6e", result)
        else -> {
            val formatted = String.format("%.10f", result).removeSuffix("0000000000").removeSuffix(".0000000000")
            if (formatted.endsWith(".")) formatted.dropLast(1) else formatted
        }
    }
}

private fun formatScientific(value: Double): String {
    return when {
        value.isInfinite() -> if (value > 0) "+∞" else "-∞"
        value.isNaN() -> "NaN"
        value == 0.0 -> "0 × 10⁰"
        else -> String.format("%.4e", value).replace("e+0", " × 10^").replace("e-", " × 10^-")
    }
}

private fun getSpecialCase(base: Double, exponent: Double): String? {
    return when {
        base == 0.0 && exponent > 0 -> "Any non-zero number to power 0 equals 1"
        base == 0.0 && exponent < 0 -> "Division by zero! Undefined."
        base == 0.0 && exponent == 0.0 -> "Indeterminate form (0^0)"
        base == 1.0 -> "1 raised to any power always equals 1"
        exponent == 0.0 && base != 0.0 -> "Any non-zero number to power 0 equals 1"
        exponent == 1.0 -> "Any number to power 1 equals itself"
        exponent == 0.5 -> "Exponent of 0.5 (√) means square root"
        exponent == (1.0/3.0) -> "Exponent of 1/3 (∛) means cube root"
        base == -1.0 && exponent % 2 == 0.0 -> "-1 to even power equals 1"
        base == -1.0 && exponent % 2 != 0.0 -> "-1 to odd power equals -1"
        else -> null
    }
}

private fun isExactPower(base: Double, exponent: Double): Boolean {
    // Check if we can compute exactly
    if (exponent < 0 || exponent != exponent.toLong().toDouble()) return false
    if (exponent > 20) return false // Too large for exact display
    
    val expInt = exponent.toInt()
    var result = 1.0
    repeat(expInt) { result *= base }
    
    return result == base.pow(exponent)
}

private fun generateSteps(base: Double, exponent: Double, result: Double, includeSteps: Boolean): List<String> {
    if (!includeSteps) return emptyList()
    
    val steps = mutableListOf<String>()
    
    when {
        // Positive integer exponent
        exponent == exponent.toLong().toDouble() && exponent >= 0 && exponent <= 20 -> {
            val expInt = exponent.toInt()
            
            if (expInt == 0) {
                steps.add("Any number^0 = 1")
                steps.add("$base^0 = 1")
            } else if (expInt == 1) {
                steps.add("Any number^1 = itself")
                steps.add("$base^1 = $base")
            } else {
                steps.add("Multiply $base by itself $expInt times:")
                
                var current = base
                steps.add("Start: $current")
                
                for (i in 2..expInt) {
                    current *= base
                    val operation = "$base × ${String.format("%.4f", current / base)}"
                    steps.add("Step $i: $operation = ${formatStepValue(current)}")
                }
                
                steps.add("")
                steps.add("$base^$expInt = ${formatFinalValue(result)}")
            }
        }
        
        // Negative integer exponent
        exponent < 0 && exponent == exponent.toLong().toDouble() -> {
            val absExp = kotlin.math.abs(exponent.toInt())
            steps.add("Negative exponent rule: a^(-n) = 1 / a^n")
            steps.add("")
            steps.add("$base^($exponent) = 1 / ($base^$absExp)")
            
            // Calculate positive power
            var posResult = 1.0
            repeat(absExp) { posResult *= base }
            steps.add("$base^$absExp = ${formatStepValue(posResult)}")
            steps.add("")
            steps.add("$base^($exponent) = 1 / ${formatStepValue(posResult)} = ${formatFinalValue(result)}")
        }
        
        // Fractional exponent (roots)
        exponent > 0 && exponent < 1 -> {
            val denominator = getDenominator(exponent)
            val rootName = when (denominator) {
                2 -> "square root"
                3 -> "cube root"
                else -> "$denominator-th root"
            }
            steps.add("Fractional exponent represents a $rootName:")
            steps.add("$base^$exponent = √[$denominator]($base^${(exponent * denominator).toInt()})")
            steps.add("")
            steps.add("Using calculator: $base^$exponent ≈ ${formatFinalValue(result)}")
        }
        
        // Other decimal exponents
        else -> {
            steps.add("Calculating $base^$exponent:")
            steps.add("Using exponential function: e^($exponent × ln($base))")
            steps.add("")
            steps.add("Result: ${formatFinalValue(result)}")
        }
    }
    
    return steps
}

private fun formatStepValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.6f", value).removeSuffix("000000").removeSuffix(".")
    }
}

private fun formatFinalValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        DecimalFormat("#,###").format(value.toLong())
    } else if (kotlin.math.abs(value) >= 1e10 || (kotlin.math.abs(value) < 0.001 && value != 0.0)) {
        String.format("%.4e", value)
    } else {
        String.format("%.10f", value).removeSuffix("0000000000").removeSuffix(".0000000000")
    }
}

// Get denominator of fraction approximation
private fun getDenominator(d: Double): Int {
    val fractions = listOf(
        0.5 to 2, 0.333333 to 3, 0.25 to 4, 0.2 to 5,
        0.166667 to 6, 0.142857 to 7, 0.125 to 8, 0.111111 to 9,
        0.1 to 10, 0.75 to 4, 0.666667 to 3
    )
    
    for ((frac, denom) in fractions) {
        if (kotlin.math.abs(d - frac) < 0.01) return denom
    }
    
    return 2 // Default to square root
}

private fun calculateQuickPower(base: Double, exponent: Int): String {
    return try {
        val result = base.pow(exponent.toDouble())
        formatFinalValue(result)
    } catch (e: Exception) {
        "Error"
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

@Composable
private fun RelatedCalcRow(expression: String, result: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(expression, style = MaterialTheme.typography.bodyMedium, 
             fontFamily = FontFamily.Monospace)
        Text("= $result", style = MaterialTheme.typography.bodyMedium,
             fontWeight = FontWeight.Medium)
    }
}

private fun isPerfectSquare(n: Long): Boolean {
    if (n < 0) return false
    val sqrt = sqrtLong(n)
    return sqrt * sqrt == n
}

private fun sqrtLong(n: Long): Long {
    if (n < 0) throw IllegalArgumentException("Cannot compute square root of negative number")
    if (n < 2L) return n
    
    var x = n
    var y = (x + 1L) / 2L
    
    while (y < x) {
        x = y
        y = (x + n / x) / 2L
    }
    
    return x
}
