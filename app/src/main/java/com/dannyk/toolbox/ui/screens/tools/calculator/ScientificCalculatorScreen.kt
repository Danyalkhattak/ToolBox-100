package com.dannyk.toolbox.ui.screens.tools.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import kotlin.math.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState

@Composable
fun ScientificCalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var currentInput by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf("") }
    var shouldResetInput by remember { mutableStateOf(false) }
    var isDegreeMode by remember { mutableStateOf(true) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "Scientific Calculator",
            subtitle = "Advanced calculations with scientific functions",
            onBack = { navHostController.popBackStack() }
        )
        
        // Mode toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDegreeMode) "DEG" else "RAD",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isDegreeMode,
                onCheckedChange = { isDegreeMode = it },
                modifier = Modifier.height(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Expression display (scrollable)
                Text(
                    text = expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Current input / result display
                Text(
                    text = formatScientificDisplay(currentInput),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Scientific function buttons row 1
        ScientificButtonRow(
            buttons = listOf("sin", "cos", "tan", "ln", "log", "1/x")
        ) { func ->
            handleScientificFunction(func, currentInput, expression, isDegreeMode) { newInput, newExpr ->
                currentInput = newInput
                expression = newExpr
                shouldResetInput = true
            }
        }
        
        // Scientific function buttons row 2
        ScientificButtonRow(
            buttons = listOf("asin", "acos", "atan", "x²", "x³", "√")
        ) { func ->
            handleScientificFunction(func, currentInput, expression, isDegreeMode) { newInput, newExpr ->
                currentInput = newInput
                expression = newExpr
                shouldResetInput = true
            }
        }
        
        // Scientific function buttons row 3
        ScientificButtonRow(
            buttons = listOf("xʸ", "∛", "n!", "π", "e", "(")
        ) { func ->
            when (func) {
                "π" -> {
                    currentInput = PI.toString()
                    shouldResetInput = false
                }
                "e" -> {
                    currentInput = E.toString()
                    shouldResetInput = false
                }
                "(" -> {
                    expression += "("
                    shouldResetInput = true
                }
                else -> {
                    handleScientificFunction(func, currentInput, expression, isDegreeMode) { newInput, newExpr ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = true
                    }
                }
            }
        }
        
        // Scientific function buttons row 4 - constants and close paren
        ScientificButtonRow(
            buttons = listOf(")", "%", "⌫", "C", "±", "÷")
        ) { func ->
            when (func) {
                ")" -> {
                    val fullExpr = "$expression$currentInput)"
                    val result = evaluateScientific(fullExpr)
                    if (result != null) {
                        currentInput = result
                        expression = ""
                        shouldResetInput = true
                    } else {
                        expression += "$currentInput)"
                        shouldResetInput = true
                    }
                }
                "%" -> {
                    handleOperator("%", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
                "⌫" -> {
                    if (currentInput.length > 1) {
                        currentInput = currentInput.dropLast(1)
                    } else {
                        currentInput = "0"
                    }
                }
                "C" -> {
                    currentInput = "0"
                    expression = ""
                    shouldResetInput = false
                }
                "±" -> {
                    if (currentInput != "0") {
                        currentInput = if (currentInput.startsWith("-")) {
                            currentInput.drop(1)
                        } else {
                            "-$currentInput"
                        }
                    }
                }
                else -> {
                    handleOperator("/", currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                        currentInput = newInput
                        expression = newExpr
                        shouldResetInput = reset
                    }
                }
            }
        }
        
        // Number pad rows
        NumberPadRow(buttons = listOf("7", "8", "9", "×")) { btn ->
            handleCalculatorInput(btn, currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                currentInput = newInput
                expression = newExpr
                shouldResetInput = reset
            }
        }
        
        NumberPadRow(buttons = listOf("4", "5", "6", "-")) { btn ->
            handleCalculatorInput(btn, currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                currentInput = newInput
                expression = newExpr
                shouldResetInput = reset
            }
        }
        
        NumberPadRow(buttons = listOf("1", "2", "3", "+")) { btn ->
            handleCalculatorInput(btn, currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                currentInput = newInput
                expression = newExpr
                shouldResetInput = reset
            }
        }
        
        // Last row: 0, ., =
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SciCalcButton(text = "0", modifier = Modifier.weight(2f)) {
                handleDigit("0", currentInput, shouldResetInput) { currentInput = it; shouldResetInput = false }
            }
            SciCalcButton(text = ".") {
                if (!currentInput.contains(".")) {
                    currentInput += "."
                }
            }
            SciCalcButton(text = "=", isEquals = true) {
                val fullExpression = if (expression.isEmpty()) currentInput else "$expression$currentInput"
                val result = evaluateScientific(fullExpression)
                if (result != null) {
                    expression = "$fullExpression="
                    currentInput = result
                    shouldResetInput = true
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Copy button
        OutlinedButton(
            onClick = { copyToClipboard_ScientificCalculatorScreen(context, currentInput) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy Result")
        }
    }
}

@Composable
private fun ScientificButtonRow(
    buttons: List<String>,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        buttons.forEach { btn ->
            SciCalcButton(
                text = btn,
                isFunction = true,
                modifier = Modifier.weight(1f)
            ) {
                onClick(btn)
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun NumberPadRow(
    buttons: List<String>,
    onClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttons.forEachIndexed { index, btn ->
            if (index < buttons.size - 1) {
                SciCalcButton(text = btn, modifier = Modifier.weight(1f)) { onClick(btn) }
            } else {
                SciCalcButton(text = btn, isOperator = true, modifier = Modifier.weight(1f)) { onClick(btn) }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SciCalcButton(
    text: String,
    isFunction: Boolean = false,
    isOperator: Boolean = false,
    isEquals: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isEquals -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.secondaryContainer
        isFunction -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    
    val contentColor = when {
        isEquals -> MaterialTheme.colorScheme.onPrimary
        isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
        isFunction -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = if (text.length > 2) 13.sp else 18.sp
                ),
                color = contentColor
            )
        }
    }
}

private fun handleScientificFunction(
    function: String,
    currentInput: String,
    expression: String,
    isDegreeMode: Boolean,
    onResult: (String, String) -> Unit
) {
    try {
        val value = currentInput.toDoubleOrNull() ?: return
        
        val result = when (function) {
            "sin" -> if (isDegreeMode) sin(Math.toRadians(value)) else sin(value)
            "cos" -> if (isDegreeMode) cos(Math.toRadians(value)) else cos(value)
            "tan" -> if (isDegreeMode) tan(Math.toRadians(value)) else tan(value)
            "asin" -> if (isDegreeMode) Math.toDegrees(asin(value)) else asin(value)
            "acos" -> if (isDegreeMode) Math.toDegrees(acos(value)) else acos(value)
            "atan" -> if (isDegreeMode) Math.toDegrees(atan(value)) else atan(value)
            "ln" -> ln(value)
            "log" -> log10(value)
            "x²" -> value * value
            "x³" -> value * value * value
            "xʸ" -> {
                onResult("", "${currentInput}^")
                return
            }
            "√" -> sqrt(value)
            "∛" -> cbrt(value)
            "n!" -> factorial(value.toInt())
            "1/x" -> 1.0 / value
            else -> return
        }
        
        val formattedResult = formatResult(result)
        onResult(formattedResult, "$function($currentInput)")
    } catch (e: Exception) {
        onResult("Error", "")
    }
}

private fun factorial(n: Int): Double {
    require(n >= 0) { "Factorial not defined for negative numbers" }
    require(n <= 170) { "Factorial too large" }
    var result = 1.0
    for (i in 2..n) result *= i
    return result
}

private fun handleCalculatorInput(
    input: String,
    currentInput: String,
    expression: String,
    shouldResetInput: Boolean,
    onResult: (String, String, Boolean) -> Unit
) {
    when (input) {
        "+", "-", "*", "/" -> {
            val operator = when (input) {
                "*" -> "×"
                "/" -> "÷"
                else -> input
            }
            handleOperator(input, currentInput, expression, shouldResetInput) { newInput, newExpr, reset ->
                onResult(newInput, newExpr.replace("*", "×").replace("/", "÷"), reset)
            }
        }
        else -> {
            handleDigit(input, currentInput, shouldResetInput) { newInput ->
                onResult(newInput, expression, false)
            }
        }
    }
}

private fun handleDigit(digit: String, currentInput: String, shouldResetInput: Boolean, onResult: (String) -> Unit) {
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
        val fullExpression = "$expression$currentInput"
        val result = evaluateScientific(fullExpression)
        if (result != null) {
            onResult("0", "$result$operator", true)
        } else {
            onResult("0", "$fullExpression$operator", true)
        }
    } else {
        onResult("0", "$currentInput$operator", true)
    }
}

private fun evaluateScientific(expression: String): String? {
    return try {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("%", "/100*")
            .replace("π", PI.toString())
            .replace("e", E.toString())
        
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
                        var x = parsePower()
                        while (true) {
                            when {
                                eat('*'.code) -> x *= parsePower()
                                eat('/'.code) -> x /= parsePower()
                                else -> return x
                            }
                        }
                    }
                    
                    fun parsePower(): Double {
                        var x = parseFactor()
                        if (eat('^'.code)) {
                            val exp = parsePower()
                            x = x.pow(exp)
                        }
                        return x
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
        
        formatResult(result)
    } catch (e: Exception) {
        null
    }
}

private fun formatResult(result: Double): String {
    return if (result.isInfinite() || result.isNaN()) {
        "Error"
    } else if (result == result.toLong().toDouble()) {
        result.toLong().toString()
    } else {
        "%.10f".format(result).trimEnd('0').trimEnd('.')
    }
}

private fun formatScientificDisplay(number: String): String {
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

private fun copyToClipboard_ScientificCalculatorScreen(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Calculator Result", text)
    clipboard.setPrimaryClip(clip)
}
