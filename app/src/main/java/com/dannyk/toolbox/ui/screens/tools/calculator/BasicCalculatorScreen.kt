package com.dannyk.toolbox.ui.screens.tools.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import java.util.*
import androidx.compose.ui.draw.clip

@Composable
fun BasicCalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var currentInput by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var lastResult by remember { mutableStateOf<String?>(null) }
    var shouldResetInput by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "Basic Calculator",
            subtitle = "Perform basic arithmetic calculations",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Expression display
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Current input / result display
                Text(
                    text = formatDisplayNumber(currentInput),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calculator buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: C, ⌫, %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "C",
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    currentInput = "0"
                    expression = ""
                    shouldResetInput = false
                }
                CalculatorButton(
                    text = "⌫",
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    if (currentInput.length > 1) {
                        currentInput = currentInput.dropLast(1)
                    } else {
                        currentInput = "0"
                    }
                }
                CalculatorButton(
                    text = "%",
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    handleOperator("%", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
                CalculatorButton(
                    text = "÷",
                    isOperator = true,
                    modifier = Modifier.weight(1f)
                ) {
                    handleOperator("/", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
            }
            
            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "7", modifier = Modifier.weight(1f)) {
                    handleDigit("7", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(text = "8", modifier = Modifier.weight(1f)) {
                    handleDigit("8", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(text = "9", modifier = Modifier.weight(1f)) {
                    handleDigit("9", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(
                    text = "×",
                    isOperator = true,
                    modifier = Modifier.weight(1f)
                ) {
                    handleOperator("*", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
            }
            
            // Row 3: 4, 5, 6, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "4", modifier = Modifier.weight(1f)) {
                    handleDigit("4", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(text = "5", modifier = Modifier.weight(1f)) {
                    handleDigit("5", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(text = "6", modifier = Modifier.weight(1f)) {
                    handleDigit("6", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(
                    text = "-",
                    isOperator = true,
                    modifier = Modifier.weight(1f)
                ) {
                    handleOperator("-", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
            }
            
            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(text = "1", modifier = Modifier.weight(1f)) {
                    handleDigit("1", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(text = "2", modifier = Modifier.weight(1f)) {
                    handleDigit("2", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(text = "3", modifier = Modifier.weight(1f)) {
                    handleDigit("3", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(
                    text = "+",
                    isOperator = true,
                    modifier = Modifier.weight(1f)
                ) {
                    handleOperator("+", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
            }
            
            // Row 5: ±, 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatorButton(
                    text = "±",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    if (currentInput != "0") {
                        currentInput = if (currentInput.startsWith("-")) {
                            currentInput.drop(1)
                        } else {
                            "-$currentInput"
                        }
                    }
                }
                CalculatorButton(text = "0", modifier = Modifier.weight(1f)) {
                    handleDigit("0", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
                }
                CalculatorButton(
                    text = ".",
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    if (!currentInput.contains(".")) {
                        currentInput += "."
                    }
                }
                CalculatorButton(
                    text = "=",
                    isEquals = true,
                    modifier = Modifier.weight(1f)
                ) {
                    val fullExpression = if (expression.isEmpty()) {
                        currentInput
                    } else {
                        "$expression$currentInput"
                    }
                    
                    val result = evaluateExpression(fullExpression)
                    if (result != null) {
                        lastResult = result
                        expression = "$fullExpression="
                        currentInput = result
                        shouldResetInput = true
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Copy result button
        if (lastResult != null || currentInput != "0") {
            OutlinedButton(
                onClick = {
                    copyToClipboard(context, currentInput)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Result")
            }
        }
    }
}

@Composable
private fun CalculatorButton(
    text: String,
    isOperator: Boolean = false,
    isEquals: Boolean = false,
    backgroundColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val actualBackgroundColor = when {
        backgroundColor != Color.Unspecified -> backgroundColor
        isEquals -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    
    val actualContentColor = when {
        contentColor != Color.Unspecified -> contentColor
        isEquals -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = actualBackgroundColor,
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (isOperator || isEquals) FontWeight.Bold else FontWeight.Medium
                ),
                color = actualContentColor
            )
        }
    }
}

private fun handleDigit(
    digit: String,
    currentInput: String,
    shouldResetInput: Boolean,
    onResult: (String) -> Unit
) {
    onResult(if (shouldResetInput) digit else run {
        val current = currentInput
        if (digit == "0" && (current == "0" || current == "-0")) current else "$current$digit"
    })
}

private fun handleOperator(
    operator: String,
    currentInput: String,
    expression: String,
    shouldResetInput: Boolean,
    onResult: (String, String, Boolean) -> Unit
) {
    if (expression.isNotEmpty() && !shouldResetInput) {
        // Evaluate the existing expression first
        val fullExpression = "$expression$currentInput"
        val result = evaluateExpression(fullExpression)
        if (result != null) {
            onResult("0", "$result$operator", true)
        } else {
            onResult("0", "$fullExpression$operator", true)
        }
    } else {
        onResult("0", "$currentInput$operator", true)
    }
}

private fun evaluateExpression(expression: String): String? {
    return try {
        // Replace display operators with actual operators
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("%", "/100*")
        
        // Use a safe evaluation approach with proper precedence
        val result = object : Any() {
            fun eval(expr: String): Double {
                return object : Any() {
                    var pos = -1
                    var ch = 0
                    
                    fun nextChar() {
                        ch = if (++pos < expr.length) expr[pos].code else -1
                    }
                    
                    fun eat(charToEat: Int): Boolean {
                        while (ch == ' '.code) nextChar()
                        if (ch == charToEat) {
                            nextChar()
                            return true
                        }
                        return false
                    }
                    
                    fun parse(): Double {
                        nextChar()
                        val x = parseExpression()
                        if (pos < expr.length) throw RuntimeException("Unexpected: $ch.toChar()")
                        return x
                    }
                    
                    fun parseExpression(): Double {
                        var x = parseTerm()
                        while (true) {
                            when {
                                eat('+'.code) -> x += parseTerm()
                                eat('-'.code) -> x -= parseTerm()
                                else -> return x
                            }
                        }
                    }
                    
                    fun parseTerm(): Double {
                        var x = parseFactor()
                        while (true) {
                            when {
                                eat('*'.code) -> x *= parseFactor()
                                eat('/'.code) -> x /= parseFactor()
                                else -> return x
                            }
                        }
                    }
                    
                    fun parseFactor(): Double {
                        if (eat('+'.code)) return parseFactor()
                        if (eat('-'.code)) return -parseFactor()
                        
                        var x: Double
                        val startPos = this.pos
                        
                        if (eat('('.code)) {
                            x = parseExpression()
                            eat(')'.code)
                        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                            x = expr.substring(startPos, this.pos).toDouble()
                        } else {
                            throw RuntimeException("Unexpected: $ch.toChar()")
                        }
                        
                        return x
                    }
                }.parse()
            }
        }.eval(sanitized)
        
        // Format result
        if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            "%.10f".format(result).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) {
        null
    }
}

private fun formatDisplayNumber(number: String): String {
    return try {
        val num = number.toDoubleOrNull() ?: return number
        if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            number
        }
    } catch (e: Exception) {
        number
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Calculator Result", text)
    clipboard.setPrimaryClip(clip)
}
