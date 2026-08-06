package com.dannyk.toolbox.ui.screens.tools.math

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun SquareRootCalculatorScreen(navController: NavHostController) {
    var numberInput by remember { mutableStateOf("") }
    var rootType by remember { mutableStateOf(2) } // 2=sqrt, 3=cbrt, n=nth root
    var nthRootValue by remember { mutableStateOf("4") }
    var precision by remember { mutableStateOf(10) }
    var result by remember { mutableStateOf<RootResult?>(null) }
    
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Root Calculator",
            subtitle = "Calculate square, cube, and nth roots with precision",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Root Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Select Root Type", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = rootType == 2,
                            onClick = { rootType = 2 },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("√ Square Root")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilterChip(
                            selected = rootType == 3,
                            onClick = { rootType = 3 },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("∛ Cube Root")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        FilterChip(
                            selected = rootType == 0, // Custom n-th root
                            onClick = { rootType = 0 },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("ⁿ√ Nth Root")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (rootType == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = nthRootValue,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("[2-9]\\d*"))) nthRootValue = it 
                            },
                            label = { Text("Root degree (n)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Show current formula
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        when (rootType) {
                            2 -> "Calculating: √x"
                            3 -> "Calculating: ∛x"
                            else -> "Calculating: $nthRootValue√x"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Input Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Enter Number", style = MaterialTheme.typography.titleMedium)
                    
                    OutlinedTextField(
                        value = numberInput,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("-?\\d*\\.?\\d*"))) numberInput = it 
                        },
                        label = { Text("Number (x)") },
                        placeholder = { Text("e.g., 16, 27, 144, 0.25") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Precision slider
                    Text("Decimal Precision: $precision", style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Slider(
                        value = precision.toFloat(),
                        onValueChange = { precision = it.roundToInt() },
                        valueRange = 1f..15f,
                        steps = 14
                    )
                    
                    // Quick examples
                    Text("Perfect Powers:", style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (rootType) {
                            2 -> {
                                listOf(4 to "2", 9 to "3", 16 to "4", 25 to "5", 36 to "6", 
                                      49 to "7", 64 to "8", 81 to "9", 100 to "10", 144 to "12").forEach { (num, _) ->
                                    FilterChip(
                                        selected = numberInput == num.toString(),
                                        onClick = { numberInput = num.toString() },
                                        label = { Text("$num") }
                                    )
                                }
                            }
                            3 -> {
                                listOf(1 to "1", 8 to "2", 27 to "3", 64 to "4", 125 to "5",
                                      216 to "6", 343 to "7", 512 to "8", 729 to "9", 1000 to "10").forEach { (num, _) ->
                                    FilterChip(
                                        selected = numberInput == num.toString(),
                                        onClick = { numberInput = num.toString() },
                                        label = { Text("$num") }
                                    )
                                }
                            }
                            else -> {
                                listOf(16 to "2", 32 to "2", 81 to "3", 243 to "3").forEach { (num, _) ->
                                    FilterChip(
                                        selected = numberInput == num.toString(),
                                        onClick = { numberInput = num.toString() },
                                        label = { Text("$num") }
                                    )
                                }
                            }
                        }
                    }
                    
                    PrimaryButton(
                        text = "Calculate Root",
                        onClick = {
                            val num = numberInput.toDoubleOrNull() ?: return@PrimaryButton
                            val rootN = if (rootType == 0) nthRootValue.toIntOrNull() ?: return@PrimaryButton else rootType
                            
                            result = calculateRoot(num, rootN, precision)
                        },
                        enabled = numberInput.isNotEmpty() && 
                                  (rootType != 0 || (nthRootValue.toIntOrNull()?.let { it >= 2 } == true)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Results Section
            result?.let { res ->
                // Main Result
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            res.formula,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    res.result,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(res.result))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy result"
                                    )
                                }
                            }
                        }
                        
                        if (res.isExact) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "Exact Value!",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    "Approximate (±${"0." + "1".padEnd(precision - 1, '0')})",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                // Perfect Power Detection
                if (res.isPerfectPower) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "${res.perfectPowerName}!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    "${res.number} = ${res.exactRoot}${getExponentSymbol(res.rootDegree)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace
                                )
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
                        Text("Properties", style = MaterialTheme.typography.titleMedium)
                        
                        InfoRow("Input Number", formatNumber(res.number))
                        InfoRow("Root Degree", "${res.rootDegree}")
                        InfoRow("Result", res.result)
                        InfoRow("Is Exact", if (res.isExact) "Yes" else "No")
                        
                        HorizontalDivider()
                        
                        InfoRow("Result²", formatNumber(res.result.toDouble().pow(2)))
                        if (res.rootDegree == 3) {
                            InfoRow("Result³", formatNumber(res.result.toDouble().pow(3)))
                        }
                        
                        // Verification
                        val verification = res.result.toDouble().pow(res.rootDegree)
                        InfoRow("Verification (${result}^${res.rootDegree})", formatNumber(verification))
                        
                        val error = abs(verification - res.number)
                        InfoRow("Error", String.format("%.${precision}f", error))
                    }
                }

                // Calculation Method
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calculation Method", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Using Newton's method (Newton-Raphson iteration)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Formula: x_{n+1} = ((n-1) × x_n + a / x_n^{n-1}) / n",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (res.iterations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Iteration Steps:", style = MaterialTheme.typography.titleSmall)
                            
                            res.iterations.forEachIndexed { index, iter ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Step ${index + 1}:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        iter,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Related Roots
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Related Calculations", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        RelatedRootRow("Square Root (√)", calculateQuickRoot(res.number, 2, 6))
                        RelatedRootRow("Cube Root (∛)", calculateQuickRoot(res.number, 3, 6))
                        RelatedRootRow("Fourth Root (∜)", calculateQuickRoot(res.number, 4, 6))
                        
                        if (res.rootDegree != 2) {
                            RelatedRootRow("${res.rootDegree}th Root", res.result)
                        }
                    }
                }

                // Number Properties
                if (res.number >= 0 && res.number == res.number.toLong().toDouble()) {
                    val longNum = res.number.toLong()
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Number Properties", style = MaterialTheme.typography.titleSmall)
                            
                            InfoRow("Is Integer", "Yes")
                            InfoRow("Is Positive", if (longNum > 0) "Yes" else "No")
                            InfoRow("Is Perfect Square", isPerfectSquare(longNum).let { 
                                if (it) "Yes (${sqrtLong(longNum)}²)" else "No" 
                            })
                            InfoRow("Is Perfect Cube", isPerfectCube(longNum).let { 
                                if (it) "Yes (${cbrtLong(longNum)}³)" else "No" 
                            })
                            InfoRow("Digits", "${longNum.toString().length}")
                        }
                    }
                }
            }
        }
    }
}

data class RootResult(
    val number: Double,
    val rootDegree: Int,
    val result: String,
    val formula: String,
    val isExact: Boolean,
    val isPerfectPower: Boolean,
    val perfectPowerName: String?,
    val exactRoot: Double,
    val iterations: List<String>
)

private fun calculateRoot(number: Double, rootDegree: Int, precision: Int): RootResult {
    require(rootDegree >= 2) { "Root degree must be at least 2" }
    
    val absNumber = abs(number)
    val isNegative = number < 0
    
    // Handle special cases
    if (number == 0.0) {
        return RootResult(
            number = number,
            rootDegree = rootDegree,
            result = "0",
            formula = getFormula(rootDegree, number),
            isExact = true,
            isPerfectPower = true,
            perfectPowerName = "Zero",
            exactRoot = 0.0,
            iterations = emptyList()
        )
    }
    
    if (number == 1.0 || number == -1.0) {
        return RootResult(
            number = number,
            rootDegree = rootDegree,
            result = if (number > 0) "1" else "-1",
            formula = getFormula(rootDegree, number),
            isExact = true,
            isPerfectPower = true,
            perfectPowerName = "Unity",
            exactRoot = number,
            iterations = emptyList()
        )
    }
    
    // Check for even root of negative number
    if (isNegative && rootDegree % 2 == 0) {
        return RootResult(
            number = number,
            rootDegree = rootDegree,
            result = "Undefined (imaginary)",
            formula = getFormula(rootDegree, number),
            isExact = false,
            isPerfectPower = false,
            perfectPowerName = null,
            exactRoot = Double.NaN,
            iterations = emptyList()
        )
    }
    
    // Calculate using Newton's method
    val iterations = mutableListOf<String>()
    var x = initialGuess(absNumber, rootDegree)
    val epsilon = 10.0.pow(-precision - 1)
    
    var iterationCount = 0
    val maxIterations = 100
    
    while (iterationCount < maxIterations) {
        val xPowNm1 = x.pow(rootDegree - 1)
        val newX = ((rootDegree - 1) * x + absNumber / xPowNm1) / rootDegree
        
        iterations.add(String.format("%.${minOf(precision, 10)}f", x))
        
        if (abs(newX - x) < epsilon * max(abs(newX), 1.0)) break
        
        x = newX
        iterationCount++
    }
    
    val finalX = if (isNegative && rootDegree % 2 == 1) -x else x
    
    // Format result
    val formattedResult = formatRootResult(finalX, precision)
    
    // Check if perfect power
    val isPerfectPower = checkPerfectPower(absNumber, rootDegree)
    val perfectPowerName = when {
        !isPerfectPower -> null
        rootDegree == 2 -> "Perfect Square"
        rootDegree == 3 -> "Perfect Cube"
        else -> "Perfect ${ordinal(rootDegree)} Power"
    }
    
    // Get exact root if perfect power
    val exactRoot = if (isPerfectPower) {
        roundToLong(finalX).toDouble()
    } else finalX
    
    // Check if result is exact integer
    val isExact = isPerfectPower || (abs(finalX - finalX.roundToInt()) < 10.0.pow(-precision))
    
    return RootResult(
        number = number,
        rootDegree = rootDegree,
        result = formattedResult,
        formula = getFormula(rootDegree, number),
        isExact = isExact,
        isPerfectPower = isPerfectPower,
        perfectPowerName = perfectPowerName,
        exactRoot = exactRoot,
        iterations = iterations.takeLast(minOf(iterations.size, 10))
    )
}

private fun getFormula(degree: Int, number: Double): String {
    val rootSymbol = when (degree) {
        2 -> "√"
        3 -> "∛"
        4 -> "∜"
        else -> "$degree√"
    }
    return "$rootSymbol$number = ?"
}

private fun initialGuess(number: Double, degree: Int): Double {
    // Use logarithm-based initial guess for better convergence
    return 10.0.pow(kotlin.math.log10(number) / degree)
}

private fun formatRootResult(value: Double, precision: Int): String {
    if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
    if (value.isNaN()) return "Undefined"
    
    val rounded = value.toBigDecimal().setScale(precision, java.math.RoundingMode.HALF_UP)
    
    // Check if it's essentially an integer
    if (abs(value - value.roundToInt()) < 10.0.pow(-precision)) {
        return value.roundToInt().toString()
    }
    
    return rounded.stripTrailingZeros().toPlainString()
}

private fun checkPerfectPower(number: Double, degree: Int): Boolean {
    if (number <= 0) return false
    if (number != number.toLong().toDouble()) return false
    
    val longNum = number.toLong()
    val root = nthRootLong(longNum, degree)
    
    return root.pow(degree) == longNum
}

private fun nthRootLong(n: Long, degree: Int): Long {
    if (n < 0) throw IllegalArgumentException("Cannot compute root of negative number")
    if (n < 2) return n
    if (degree == 1) return n
    if (degree == 2) return sqrtLong(n)
    
    var x = initialGuess(n.toDouble(), degree).toLong()
    
    repeat(50) {
        val xPowNm1 = powLong(x, degree - 1)
        if (xPowNm1 == 0L) return@repeat
        val newX = ((degree - 1) * x + n / xPowNm1) / degree
        if (newX == x || newX < 1) return@repeat
        x = newX
    }
    
    return x
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

private fun cbrtLong(n: Long): Long {
    return nthRootLong(n, 3)
}

private fun isPerfectSquare(n: Long): Boolean {
    if (n < 0) return false
    val sqrt = sqrtLong(n)
    return sqrt * sqrt == n
}

private fun isPerfectCube(n: Long): Boolean {
    if (n < 0) return false
    val cbrt = cbrtLong(n)
    return cbrt * cbrt * cbrt == n
}

private fun powLong(base: Long, exponent: Int): Long {
    var result = 1L
    repeat(exponent) { result *= base }
    return result
}

private fun ordinal(n: Int): String {
    return when {
        n in 11..13 -> "${n}th"
        n % 10 == 1 -> "${n}st"
        n % 10 == 2 -> "${n}nd"
        n % 10 == 3 -> "${n}rd"
        else -> "${n}th"
    }
}

private fun getExponentSymbol(n: Int): String {
    return when (n) {
        2 -> "²"
        3 -> "³"
        4 -> "⁴"
        5 -> "⁵"
        else -> superscript(n)
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

private fun calculateQuickRoot(number: Double, degree: Int, precision: Int): String {
    return try {
        val result = calculateRoot(number, degree, precision)
        result.result
    } catch (e: Exception) {
        "Error"
    }
}

private fun formatNumber(num: Double): String {
    return if (num == num.toLong().toDouble()) {
        DecimalFormat("#,###").format(num.toLong())
    } else {
        String.format("%.6f", num).removeSuffix("000000").removeSuffix(".")
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
private fun RelatedRootRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("= $value", style = MaterialTheme.typography.bodyMedium, 
             fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}
