package com.dannyk.toolbox.ui.screens.tools.converters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import com.dannyk.toolbox.ui.components.ResultCard
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import java.util.regex.Pattern

// Roman numeral symbols and their values (sorted by value descending)
private val ROMAN_VALUES = listOf(
    1000 to "M",
    900 to "CM",
    500 to "D",
    400 to "CD",
    100 to "C",
    90 to "XC",
    50 to "L",
    40 to "XL",
    10 to "X",
    9 to "IX",
    5 to "V",
    4 to "IV",
    1 to "I"
)

// Valid Roman numeral characters
private val VALID_ROMAN_CHARS = setOf('I', 'V', 'X', 'L', 'C', 'D', 'M')

enum class ConversionMode(val displayName: String) {
    NUMBER_TO_ROMAN("Number → Roman Numeral"),
    ROMAN_TO_NUMBER("Roman Numeral → Number")
}

/**
 * Convert an integer to a Roman numeral.
 * Range: 1-3999 (standard Roman numeral range)
 */
fun numberToRoman(num: Int): String? {
    if (num < 1 || num > 3999) return null
    
    var remaining = num
    val result = StringBuilder()
    
    for ((value, symbol) in ROMAN_VALUES) {
        while (remaining >= value) {
            result.append(symbol)
            remaining -= value
        }
    }
    
    return result.toString()
}

/**
 * Convert a Roman numeral string to an integer.
 * Returns null if the input is invalid.
 */
fun romanToNumber(roman: String): Int? {
    val upperRoman = roman.uppercase().trim()
    
    // Check for empty input
    if (upperRoman.isEmpty()) return null
    
    // Validate all characters are valid Roman numerals
    if (!upperRoman.all { it in VALID_ROMAN_CHARS }) return null
    
    var result = 0
    var i = 0
    
    while (i < upperRoman.length) {
        val currentVal = getRomanValue(upperRoman[i])
        
        // Look ahead for subtractive notation
        if (i + 1 < upperRoman.length) {
            val nextVal = getRomanValue(upperRoman[i + 1])
            
            if (currentVal < nextVal) {
                // Subtractive notation (e.g., IV = 4)
                result += nextVal - currentVal
                i += 2
                continue
            }
        }
        
        result += currentVal
        i++
    }
    
    // Verify the conversion is correct by converting back
    val verification = numberToRoman(result)
    
    // Additional validation: check that the original format was valid
    // This catches cases like "IIII" which would parse as 4 but isn't standard form
    if (verification != null && !isStandardRomanForm(upperRoman)) {
        // Still allow non-standard forms but note they're not canonical
        // For strict mode, you could return null here
    }
    
    return result
}

private fun getRomanValue(char: Char): Int {
    return when (char) {
        'I' -> 1
        'V' -> 5
        'X' -> 10
        'L' -> 50
        'C' -> 100
        'D' -> 500
        'M' -> 1000
        else -> 0
    }
}

/**
 * Check if a Roman numeral follows standard formatting rules.
 * This validates against common errors like:
 * - More than 3 identical symbols in a row (except M)
 * - Invalid subtractive combinations
 * - V, L, D cannot be repeated
 * - Certain symbols can only be subtracted from specific others
 */
fun validateRomanNumeral(roman: String): Pair<Boolean, String?> {
    val upperRoman = roman.uppercase().trim()
    
    if (upperRoman.isEmpty()) {
        return false to "Empty input"
    }
    
    // Check for invalid characters
    val invalidChars = upperRoman.filter { it !in VALID_ROMAN_CHARS }
    if (invalidChars.isNotEmpty()) {
        return false to "Invalid character(s): ${invalidChars.joinToString(", ")}"
    }
    
    // Check for invalid repetitions
    // I, X, C, M can repeat up to 3 times
    // V, L, D cannot repeat
    val noRepeat = setOf('V', 'L', 'D')
    for (char in noRepeat) {
        if (Regex("$char{2,}").containsMatchIn(upperRoman)) {
            return false to "'$char' cannot be repeated"
        }
    }
    
    val canRepeat = setOf('I', 'X', 'C')
    for (char in canRepeat) {
        if (Regex("$char{4,}").containsMatchIn(upperRoman)) {
            return false to "'$char' can appear at most 3 times consecutively"
        }
    }
    
    // Check for invalid subtractive patterns
    // Only these subtractive pairs are valid: IV, IX, XL, XC, CD, CM
    val validSubtractivePairs = setOf("IV", "IX", "XL", "XC", "CD", "CM")
    
    // Find all potential subtractive patterns and validate them
    val invalidSubtractivePatterns = Regex("[IVXLC][DM]|.[VLD]").findAll(upperRoman)
        .map { it.value }
        .filter { it !in validSubtractivePairs && 
                   it[0] != it[1] && 
                   getRomanValue(it[0]) < getRomanValue(it[1]) }
        .toList()
    
    if (invalidSubtractivePatterns.isNotEmpty()) {
        return false to "Invalid subtractive pattern: ${invalidSubtractivePatterns.first()}"
    }
    
    // More specific rules about what can be subtracted from what
    // I can only precede V or X
    // X can only precede L or C
    // C can only precede D or M
    val subtractionRules = mapOf(
        'I' to setOf('V', 'X'),
        'X' to setOf('L', 'C'),
        'C' to setOf('D', 'M')
    )
    
    for ((smaller, allowedLarger) in subtractionRules) {
        val pattern = Regex("$smaller([${VALID_ROMAN_CHARS.joinToString("")}])")
        pattern.findAll(upperRoman).forEach { match ->
            val largerChar = match.groupValues[1][0]
            if (getRomanValue(smaller) < getRomanValue(largerChar) && largerChar !in allowedLarger) {
                return false to "'$smaller' cannot be placed before '$largerChar'"
            }
        }
    }
    
    // Parse and verify
    val parsedValue = romanToNumber(upperRoman)
    if (parsedValue == null || parsedValue == 0) {
        return false to "Invalid Roman numeral format"
    }
    
    if (parsedValue > 3999) {
        return false to "Value exceeds maximum (3999)"
    }
    
    return true to null
}

/**
 * Check if the Roman numeral is in standard/canonical form
 */
private fun isStandardRomanForm(roman: String): Boolean {
    val canonical = numberToRoman(romanToNumber(roman) ?: return false)
    return canonical?.uppercase() == roman.uppercase()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RomanNumeralConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var conversionMode by remember { mutableStateOf(ConversionMode.NUMBER_TO_ROMAN) }
    
    // Calculate conversion based on mode
    val result = remember(inputValue, conversionMode) {
        when (conversionMode) {
            ConversionMode.NUMBER_TO_ROMAN -> {
                if (inputValue.isEmpty()) {
                    Triple("", true, null as String?)
                } else {
                    val num = inputValue.toIntOrNull()
                    when {
                        num == null -> Triple("", false, "Please enter a valid integer")
                        num < 1 -> Triple("", false, "Minimum value is 1")
                        num > 3999 -> Triple("", false, "Maximum value is 3999")
                        else -> Triple(numberToRoman(num) ?: "", true, null)
                    }
                }
            }
            ConversionMode.ROMAN_TO_NUMBER -> {
                if (inputValue.isEmpty()) {
                    Triple("", true, null as String?)
                } else {
                    val (isValid, error) = validateRomanNumeral(inputValue)
                    if (!isValid) {
                        Triple("", false, error)
                    } else {
                        val num = romanToNumber(inputValue)
                        Triple(num?.toString() ?: "", true, null)
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ToolHeader(
            title = "Roman Numeral Converter",
            subtitle = "Convert between numbers and Roman numerals (1-3999)",
            onBack = { navController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Conversion Mode Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ConversionMode.entries.forEach { mode ->
                    FilterChip(
                        selected = conversionMode == mode,
                        onClick = { 
                            conversionMode = mode
                            inputValue = "" // Clear input on mode change
                        },
                        label = { Text(mode.displayName) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Input Field
        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                when (conversionMode) {
                    ConversionMode.NUMBER_TO_ROMAN -> {
                        // Allow only positive integers
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*$"))) {
                            inputValue = newValue
                        }
                    }
                    ConversionMode.ROMAN_TO_NUMBER -> {
                        // Allow only valid Roman numeral characters (case insensitive)
                        val filtered = newValue.uppercase().filter { it in VALID_ROMAN_CHARS || it.isWhitespace() }
                        if (newValue.isEmpty() || newValue.uppercase() == filtered ||
                            newValue.all { it.uppercaseChar() in VALID_ROMAN_CHARS }) {
                            inputValue = newValue
                        }
                    }
                }
            },
            label = { Text(when (conversionMode) {
                ConversionMode.NUMBER_TO_ROMAN -> "Enter a number (1-3999)"
                ConversionMode.ROMAN_TO_NUMBER -> "Enter a Roman numeral"
            }) },
            keyboardOptions = KeyboardOptions(keyboardType = when (conversionMode) {
                ConversionMode.NUMBER_TO_ROMAN -> KeyboardType.Number
                ConversionMode.ROMAN_TO_NUMBER -> KeyboardType.Text
            }),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            supportingText = { 
                Text(
                    text = when (conversionMode) {
                        ConversionMode.NUMBER_TO_ROMAN -> "Range: 1 to 3999"
                        ConversionMode.ROMAN_TO_NUMBER -> "Valid letters: I, V, X, L, C, D, M"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
        
        // Error message
        if (!result.second && result.third != null) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.third!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Result Section
        if (result.first.isNotEmpty() && result.second) {
            ResultCard(
                title = when (conversionMode) {
                    ConversionMode.NUMBER_TO_ROMAN -> "Roman Numeral"
                    ConversionMode.ROMAN_TO_NUMBER -> "Number"
                },
                result = result.first,
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("roman numeral result", result.first))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
            
            // Show both representations
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Both Representations:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    when (conversionMode) {
                        ConversionMode.NUMBER_TO_ROMAN -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Number:", style = MaterialTheme.typography.bodyMedium)
                                Text(inputValue, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Roman:", style = MaterialTheme.typography.bodyMedium)
                                Text(result.first, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Serif)
                            }
                        }
                        ConversionMode.ROMAN_TO_NUMBER -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Roman:", style = MaterialTheme.typography.bodyMedium)
                                Text(inputValue.uppercase(), style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Serif)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Number:", style = MaterialTheme.typography.bodyMedium)
                                Text(result.first, style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Reference Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Roman Numeral Reference",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Basic Symbols
                Text(
                    text = "Basic Symbols:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                listOf(
                    "I" to "1",
                    "V" to "5",
                    "X" to "10",
                    "L" to "50",
                    "C" to "100",
                    "D" to "500",
                    "M" to "1000"
                ).chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { (symbol, value) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = symbol,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontFamily = FontFamily.Serif
                                )
                                Text(
                                    text = "= $value",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        // Fill empty slots
                        repeat(4 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Divider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Subtractive Notation
                Text(
                    text = "Subtractive Notation:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                listOf(
                    "IV" to "4 (5 - 1)",
                    "IX" to "9 (10 - 1)",
                    "XL" to "40 (50 - 10)",
                    "XC" to "90 (100 - 10)",
                    "CD" to "400 (500 - 100)",
                    "CM" to "900 (1000 - 100)"
                ).forEach { (symbol, meaning) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Serif
                        )
                        Text(meaning, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Divider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Examples
                Text(
                    text = "Examples:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                listOf(
                    "2024" to "MMXXIV",
                    "1994" to "MCMXCIV",
                    "1776" to "MDCCLXXVI",
                    "888" to "DCCCLXXXVIII",
                    "444" to "CDXLIV"
                ).forEach { (number, roman) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(number, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                        Text(roman, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Serif)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Divider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Note: Standard Roman numerals support values 1-3999.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
