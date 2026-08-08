package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Check
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GCDLCMScreen(navController: NavHostController) {
    var numberInput by remember { mutableStateOf("") }
    var numbers by remember { mutableStateOf<List<Long>>(emptyList()) }
    var result by remember { mutableStateOf<GCDLCMResult?>(null) }
    var showFactorization by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "GCD & LCM Calculator",
            subtitle = "Find Greatest Common Divisor and Least Common Multiple",
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
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("[0-9,\\s]+"))) numberInput = it 
                            },
                            label = { Text("Enter integers (comma-separated)") },
                            placeholder = { Text("e.g., 12, 18, 24") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrimaryButton(
                                text = "Calculate GCD & LCM",
                                onClick = {
                                    val parsedNumbers = parseNumbers(numberInput)
                                    if (parsedNumbers.size >= 2 && parsedNumbers.all { it > 0 }) {
                                        numbers = parsedNumbers
                                        result = calculateGCDLCM(numbers)
                                    }
                                },
                                enabled = canParseNumbers(numberInput),
                                modifier = Modifier.weight(1f)
                            )
                            
                            SecondaryButton(
                                text = "Clear",
                                onClick = {
                                    numbers = emptyList()
                                    result = null
                                    numberInput = ""
                                },
                                enabled = numbers.isNotEmpty(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = showFactorization,
                                onCheckedChange = { showFactorization = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Show prime factorization")
                        }
                    }
                }
            }

            // Numbers Display
            item {
                if (numbers.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Input Numbers (${numbers.size})", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                numbers.forEachIndexed { index, num ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            num.toString(),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Main Results
            item {
                result?.let { res ->
                    // GCD Result
                    ResultCard(
                        title = "GCD (Greatest Common Divisor)",
                        result = "${res.gcd}"
                    )
                    
                    // LCM Result
                    ResultCard(
                        title = "LCM (Least Common Multiple)",
                        result = "${res.lcm}"
                    )

                    // Formula explanation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Formula Used", style = MaterialTheme.typography.titleSmall, 
                                 color = MaterialTheme.colorScheme.primary)
                            Text("GCD(a, b) × LCM(a, b) = |a × b|", style = MaterialTheme.typography.bodyMedium,
                                 fontFamily = FontFamily.Monospace)
                            Text("Verification: ${res.gcd} × ${res.lcm} = ${res.gcd * res.lcm}", 
                                 style = MaterialTheme.typography.bodySmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Euclidean Algorithm Steps
                    if (res.gcdSteps.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Euclidean Algorithm Steps", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                res.gcdSteps.forEachIndexed { index, step ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = if (index == res.gcdSteps.lastIndex) 
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
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
                                    if (index < res.gcdSteps.lastIndex) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Prime Factorization
                    if (showFactorization && res.factorizations.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Prime Factorization", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                res.factorizations.forEach { (number, factors) ->
                                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                        Text("$number =", style = MaterialTheme.typography.bodyMedium)
                                        
                                        if (factors.isEmpty()) {
                                            Text("1 (no prime factors)", style = MaterialTheme.typography.bodySmall,
                                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                factors.forEachIndexed { index, factor ->
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Surface(
                                                            shape = MaterialTheme.shapes.small,
                                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                                        ) {
                                                            Text(
                                                                "$factor",
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }
                                                        if (index < factors.lastIndex) {
                                                            Text(" × ", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // Show exponent form
                                            val exponentForm = formatExponentForm(factors)
                                            Text("= $exponentForm", 
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = MaterialTheme.colorScheme.primary,
                                                 fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                    
                                    if (number != res.factorizations.keys.last()) {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                }
                                
                                // Show how to get GCD/LCM from factorization
                                res.gcdFromFactors?.let { gcdFactors ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(modifier = Modifier.fillMaxWidth())
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text("GCD from Factors:", style = MaterialTheme.typography.titleSmall)
                                    Text(gcdFactors, style = MaterialTheme.typography.bodyMedium,
                                         fontFamily = FontFamily.Monospace)
                                }
                                
                                res.lcmFromFactors?.let { lcmFactors ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("LCM from Factors:", style = MaterialTheme.typography.titleSmall)
                                    Text(lcmFactors, style = MaterialTheme.typography.bodyMedium,
                                         fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GCDLCMResult(
    val gcd: Long,
    val lcm: Long,
    val gcdSteps: List<String>,
    val factorizations: Map<Long, List<Long>>,
    val gcdFromFactors: String?,
    val lcmFromFactors: String?
)

private fun parseNumbers(input: String): List<Long> {
    return input.split(",", ";", " ")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .mapNotNull { it.toLongOrNull() }
        .filter { it > 0 }
}

private fun canParseNumbers(input: String): Boolean {
    val numbers = parseNumbers(input)
    return numbers.size >= 2
}

private fun calculateGCDLCM(numbers: List<Long>): GCDLCMResult {
    require(numbers.size >= 2) { "Need at least 2 numbers" }
    require(numbers.all { it > 0 }) { "All numbers must be positive" }
    
    // Calculate GCD using Euclidean algorithm for all numbers
    var currentGcd = numbers[0]
    val allGcdSteps = mutableListOf<String>()
    
    for (i in 1 until numbers.size) {
        val steps = euclideanAlgorithmSteps(currentGcd, numbers[i])
        allGcdSteps.addAll(steps)
        currentGcd = gcd(currentGcd, numbers[i])
    }
    
    // Calculate LCM using formula: LCM(a,b) = |a*b| / GCD(a,b)
    var currentLcm = numbers[0]
    for (i in 1 until numbers.size) {
        currentLcm = lcm(currentLcm, numbers[i])
    }
    
    // Calculate prime factorization for each number
    val factorizations = numbers.associateWith { primeFactorization(it) }
    
    // Generate GCD/LCM explanation from factors
    val gcdFromFactors = explainGCDFromFactors(factorizations)
    val lcmFromFactors = explainLCMFromFactors(factorizations)
    
    return GCDLCMResult(
        gcd = currentGcd,
        lcm = currentLcm,
        gcdSteps = allGcdSteps,
        factorizations = factorizations,
        gcdFromFactors = gcdFromFactors,
        lcmFromFactors = lcmFromFactors
    )
}

// Euclidean Algorithm - returns list of steps
private fun euclideanAlgorithmSteps(a: Long, b: Long): List<String> {
    val steps = mutableListOf<String>()
    var x = kotlin.math.max(a, b)
    var y = kotlin.math.min(a, b)
    
    steps.add("Finding GCD($a, $b)")
    steps.add("")
    
    while (y != 0L) {
        val quotient = x / y
        val remainder = x % y
        steps.add("$x = $y × $quotient + $remainder")
        x = y
        y = remainder
    }
    
    steps.add("")
    steps.add("GCD($a, $b) = $x")
    
    return steps
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
    return if (a == 0L || b == 0L) 0L else a / gcd(a, b) * b
}

// Prime Factorization - returns list of prime factors with repetition
private fun primeFactorization(n: Long): List<Long> {
    if (n <= 1) return emptyList()
    
    val factors = mutableListOf<Long>()
    var num = n
    var divisor = 2L
    
    while (divisor * divisor <= num) {
        while (num % divisor == 0L) {
            factors.add(divisor)
            num /= divisor
        }
        divisor++
    }
    
    if (num > 1) {
        factors.add(num)
    }
    
    return factors
}

// Format factors as exponents (e.g., [2,2,3] -> 2² × 3¹)
private fun formatExponentForm(factors: List<Long>): String {
    if (factors.isEmpty()) return "1"
    
    val counts = factors.groupingBy { it }.eachCount()
    return counts.entries
        .sortedBy { it.key }
        .joinToString(" × ") { (prime, count) ->
            if (count > 1) "${prime}${superscriptGCD(count)}" else "$prime"
        }
}

private fun superscriptGCD(n: Int): String {
    return n.toString().map {
        when (it) {
            '0' -> '⁰'
            '1' -> '¹'
            '2' -> '²'
            '3' -> '³'
            '4' -> '⁴'
            '5' -> '⁵'
            '6' -> '⁶'
            '7' -> '⁷'
            '8' -> '⁸'
            '9' -> '⁹'
            else -> it
        }
    }.joinToString("")
}

// Explain GCD calculation from prime factors
private fun explainGCDFromFactors(factorizations: Map<Long, List<Long>>): String? {
    if (factorizations.isEmpty()) return null
    
    // Get common prime factors with minimum exponents
    val allPrimes = factorizations.values.flatten().toSet().sorted()
    val commonFactors = mutableListOf<Pair<Long, Int>>()
    
    for (prime in allPrimes) {
        val minCount = factorizations.values.map { factors -> 
            factors.count { it == prime } 
        }.minOrNull() ?: 0
        
        if (minCount > 0) {
            commonFactors.add(Pair(prime, minCount))
        }
    }
    
    if (commonFactors.isEmpty()) return "No common prime factors → GCD = 1"
    
    val factorString = commonFactors.joinToString(" × ") { (prime, count) ->
        if (count > 1) "$prime$superscript(count)" else "$prime"
    }
    
    return "Take minimum power of each common prime: $factorString"
}

// Explain LCM calculation from prime factors
private fun explainLCMFromFactors(factorizations: Map<Long, List<Long>>): String? {
    if (factorizations.isEmpty()) return null
    
    // Get all primes with maximum exponents
    val allPrimes = factorizations.values.flatten().toSet().sorted()
    val maxFactors = mutableListOf<Pair<Long, Int>>()
    
    for (prime in allPrimes) {
        val maxCount = factorizations.values.map { factors -> 
            factors.count { it == prime } 
        }.maxOrNull() ?: 0
        
        maxFactors.add(Pair(prime, maxCount))
    }
    
    val factorString = maxFactors.joinToString(" × ") { (prime, count) ->
        if (count > 1) "$prime$superscript(count)" else "$prime"
    }
    
    return "Take maximum power of each prime: $factorString"
}
