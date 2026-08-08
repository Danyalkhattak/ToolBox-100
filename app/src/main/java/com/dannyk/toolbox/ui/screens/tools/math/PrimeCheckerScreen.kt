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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import kotlin.math.sqrt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Check
import kotlin.math.*

@Composable
fun PrimeCheckerScreen(navController: NavHostController) {
    var numberInput by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<PrimeResult?>(null) }
    var showAllFactors by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Prime Number Checker",
            subtitle = "Check primality, find factors, and discover primes",
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
                        Text("Enter a Number", style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = numberInput,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("\\d*"))) numberInput = it 
                            },
                            label = { Text("Positive integer") },
                            placeholder = { Text("e.g., 17, 100, 997") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            supportingText = { 
                                Text("Enter a positive integer (0 - 10,000,000,000)", 
                                     color = MaterialTheme.colorScheme.onSurfaceVariant) 
                            }
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = showAllFactors,
                                onCheckedChange = { showAllFactors = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Show all factors")
                        }
                        
                        PrimaryButton(
                            text = "Analyze Number",
                            onClick = {
                                val num = numberInput.toLongOrNull()
                                if (num != null && num >= 0) {
                                    result = analyzeNumber(num)
                                }
                            },
                            enabled = numberInput.isNotEmpty() && numberInput.toLongOrNull() != null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Results Section
            item {
                result?.let { res ->
                    // Prime Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (res.isPrime) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (res.isPrime) Icons.Default.Verified else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (res.isPrime) MaterialTheme.colorScheme.primary 
                                           else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        if (res.isPrime) "PRIME NUMBER" else "NOT PRIME",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (res.isPrime) MaterialTheme.colorScheme.primary 
                                               else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "${res.number} is ${if (res.isPrime) "" else "not "}a prime number",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            if (res.number == 2L || res.number == 3L) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        "SMALLEST PRIME",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else if (res.isPrime && res.number < 100L) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        "SINGLE DIGIT/SPECIAL",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Basic Info
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Basic Information", style = MaterialTheme.typography.titleMedium)
                            
                            InfoRow("Number", res.number.toString())
                            InfoRow("Is Prime", if (res.isPrime) "Yes" else "No")
                            InfoRow("Number of Factors", "${res.factors.size}")
                            InfoRow("Is Perfect Square", if (res.isPerfectSquare) "Yes (${sqrt(res.number.toDouble()).toLong()}²)" else "No")
                            InfoRow("Is Even", if (res.number % 2L == 0L) "Yes" else "No")
                            InfoRow("Digit Sum", "${res.digitSum}")
                        }
                    }

                    // Factors Section
                    if (showAllFactors) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("All Factors (${res.factors.size})", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    res.factors.forEach { factor ->
                                        Surface(
                                            shape = MaterialTheme.shapes.small,
                                            color = when {
                                                factor == 1L || factor == res.number -> MaterialTheme.colorScheme.surfaceVariant
                                                isPrime(factor.toLong()) -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                                            }
                                        ) {
                                            Text(
                                                factor.toString(),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                fontWeight = if (factor == res.number) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Factor pairs
                                if (res.factors.size > 2) {
                                    Text("Factor Pairs:", style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    val pairs = getFactorPairs(res.number, res.factors)
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 150.dp)
                                    ) {
                                        items(pairs) { (a, b) ->
                                            Text(
                                                "$a × $b = ${a * b}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Prime Factorization
                    if (!res.isPrime && res.number > 1) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Prime Factorization", style = MaterialTheme.typography.titleMedium,
                                     color = MaterialTheme.colorScheme.primary)
                                
                                Text(
                                    "$res.number = ${res.primeFactorizationString}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                // Show expanded form
                                if (res.primeFactors.size > 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Expanded: ${res.primeFactors.joinToString(" × ")}", 
                                         style = MaterialTheme.typography.bodySmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant,
                                         fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    // Neighboring Primes
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Neighboring Primes", style = MaterialTheme.typography.titleMedium)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Previous prime
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Previous Prime", style = MaterialTheme.typography.labelSmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            "${res.previousPrime}",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        "Gap: ${res.number - res.previousPrime}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Next prime
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Next Prime", style = MaterialTheme.typography.labelSmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(
                                        shape = MaterialTheme.shapes.medium,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            "${res.nextPrime}",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        "Gap: ${res.nextPrime - res.number}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Special Properties
                    if (res.specialProperties.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Special Properties", style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                res.specialProperties.forEach { property ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(property, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PrimeResult(
    val number: Long,
    val isPrime: Boolean,
    val factors: List<Long>,
    val primeFactors: List<Long>,
    val primeFactorizationString: String,
    val previousPrime: Long,
    val nextPrime: Long,
    val digitSum: Int,
    val isPerfectSquare: Boolean,
    val specialProperties: List<String>
)

private fun analyzeNumber(number: Long): PrimeResult {
    if (number < 0) throw IllegalArgumentException("Number must be non-negative")
    
    val isPrime = isPrime(number)
    val factors = getAllFactors(number)
    val primeFactors = primeFactorization(number)
    val primeFactorizationString = formatPrimeFactorization(primeFactors)
    
    val previousPrime = findPreviousPrime(number)
    val nextPrime = findNextPrime(number)
    
    val digitSum = number.toString().map { it.digitToInt() }.sum()
    val sqrtNum = sqrt(number.toDouble())
    val isPerfectSquare = sqrtNum == sqrtNum.toLong().toDouble()
    
    val specialProperties = mutableListOf<String>()
    
    // Check for special properties
    if (number == 1L) specialProperties.add("Unit (neither prime nor composite)")
    if (isMersennePrime(number)) specialProperties.add("Mersenne prime (2^n - 1)")
    if (isFermatPrime(number)) specialProperties.add("Fermat prime (2^(2^n) + 1)")
    if (isTwinPrime(number)) specialProperties.add("Part of a twin prime pair")
    if (isPalindrome(number.toString())) specialProperties.add("Palindromic number")
    if (isEmirp(number)) specialProperties.add("Emirp (prime that forms different prime when reversed)")
    
    return PrimeResult(
        number = number,
        isPrime = isPrime,
        factors = factors,
        primeFactors = primeFactors,
        primeFactorizationString = primeFactorizationString,
        previousPrime = previousPrime,
        nextPrime = nextPrime,
        digitSum = digitSum,
        isPerfectSquare = isPerfectSquare,
        specialProperties = specialProperties
    )
}

// Check if a number is prime using optimized trial division
private fun isPrime(n: Long): Boolean {
    if (n <= 1) return false
    if (n <= 3) return true
    if (n % 2L == 0L || n % 3L == 0L) return false
    
    var i = 5L
    while (i * i <= n) {
        if (n % i == 0L || n % (i + 2) == 0L) return false
        i += 6L
    }
    return true
}

// Get all factors of a number
private fun getAllFactors(n: Long): List<Long> {
    if (n <= 0) return emptyList()
    if (n == 1L) return listOf(1L)
    
    val factors = mutableListOf<Long>()
    val sqrtN = sqrt(n.toDouble()).toLong()
    
    for (i in 1..sqrtN) {
        if (n % i == 0L) {
            factors.add(i)
            if (i != n / i) {
                factors.add(n / i)
            }
        }
    }
    
    return factors.sorted()
}

// Get factor pairs for display
private fun getFactorPairs(n: Long, factors: List<Long>): List<Pair<Long, Long>> {
    val pairs = mutableListOf<Pair<Long, Long>>()
    val used = mutableSetOf<Long>()
    
    for (factor in factors) {
        if (factor !in used && n / factor !in used) {
            pairs.add(Pair(factor, n / factor))
            used.add(factor)
            used.add(n / factor)
        }
    }
    
    return pairs.sortedBy { it.first }
}

// Prime factorization
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

// Format prime factorization with exponents
private fun formatPrimeFactorization(factors: List<Long>): String {
    if (factors.isEmpty()) return "1"
    
    val counts = factors.groupingBy { it }.eachCount()
    return counts.entries
        .sortedBy { it.key }
        .joinToString(" × ") { (prime, count) ->
            if (count > 1) "$prime$superscript(count)" else "$prime"
        }
}

private fun superscript(n: Int): String {
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

// Find previous prime
private fun findPreviousPrime(n: Long): Long {
    if (n <= 2) return 2L
    
    var candidate = n - 1
    while (candidate >= 2) {
        if (isPrime(candidate)) return candidate
        candidate--
    }
    return 2L
}

// Find next prime
private fun findNextPrime(n: Long): Long {
    if (n < 2) return 2L
    
    var candidate = n + 1
    while (true) {
        if (isPrime(candidate)) return candidate
        candidate++
    }
}

// Check for Mersenne prime (2^p - 1 where p is prime)
private fun isMersennePrime(n: Long): Boolean {
    if (!isPrime(n)) return false
    
    // Check if n+1 is a power of 2
    var m = n + 1
    if (m <= 0) return false
    if ((m and (m - 1)) != 0L) return false
    
    // Verify exponent is prime
    var exp = 0
    while (m > 1) {
        m = m shr 1
        exp++
    }
    
    return isPrime(exp.toLong())
}

// Check for Fermat prime (2^(2^n) + 1)
private fun isFermatPrime(n: Long): Boolean {
    if (!isPrime(n)) return false
    
    // Known Fermat primes: 3, 5, 17, 257, 65537
    val knownFermatPrimes = setOf(3L, 5L, 17L, 257L, 65537L)
    return n in knownFermatPrimes
}

// Check for twin prime (p and p±2 are both prime)
private fun isTwinPrime(n: Long): Boolean {
    if (!isPrime(n)) return false
    return isPrime(n - 2) || isPrime(n + 2)
}

// Check if string is palindrome
private fun isPalindrome(s: String): Boolean {
    return s == s.reversed()
}

// Check for emirp (prime whose reversal is a different prime)
private fun isEmirp(n: Long): Boolean {
    if (!isPrime(n)) return false
    val reversed = n.toString().reversed().toLongOrNull() ?: return false
    return reversed != n && isPrime(reversed)
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
