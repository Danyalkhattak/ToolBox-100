package com.dannyk.toolbox.ui.screens.tools.converters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import com.dannyk.toolbox.ui.components.ResultCard
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.math.BigDecimal
import java.math.RoundingMode

enum class TimeUnit(val displayName: String, val toSecond: BigDecimal) {
    NANOSECOND("Nanosecond (ns)", BigDecimal("0.000000001")),
    MICROSECOND("Microsecond (μs)", BigDecimal("0.000001")),
    MILLISECOND("Millisecond (ms)", BigDecimal("0.001")),
    SECOND("Second (s)", BigDecimal("1")),
    MINUTE("Minute (min)", BigDecimal("60")),
    HOUR("Hour (hr)", BigDecimal("3600")),
    DAY("Day", BigDecimal("86400")),
    WEEK("Week", BigDecimal("604800")),
    MONTH("Month (30 days)", BigDecimal("2592000")),
    YEAR("Year (365 days)", BigDecimal("31536000"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf(TimeUnit.SECOND) }
    var toUnit by remember { mutableStateOf(TimeUnit.MINUTE) }
    
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    
    // Calculate conversion using BigDecimal for precision
    val result = remember(inputValue, fromUnit, toUnit) {
        if (inputValue.isEmpty() || inputValue == "-" || inputValue == ".") {
            ""
        } else {
            val value = inputValue.toBigDecimalOrNull()
            if (value != null && value >= BigDecimal.ZERO) {
                // Convert to seconds first, then to target unit
                val seconds = value * fromUnit.toSecond
                val resultValue = seconds.divide(toUnit.toSecond, 10, RoundingMode.HALF_UP)
                
                // Format result nicely
                when {
                    resultValue.compareTo(BigDecimal.ZERO) == 0 -> "0"
                    resultValue >= BigDecimal("1e12") || resultValue <= BigDecimal("1e-6") -> resultValue.toPlainString()
                    resultValue.stripTrailingZeros().scale() <= 0 -> resultValue.setScale(0, RoundingMode.DOWN).toPlainString()
                    else -> resultValue.stripTrailingZeros().toPlainString()
                }
            } else if (value != null && value < BigDecimal.ZERO) {
                "Time cannot be negative"
            } else {
                "Invalid input"
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ToolHeader(
            title = "Time Converter",
            subtitle = "Convert between different time units",
            onBack = { navController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // From Unit Section
        Text(
            text = "From",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expandedFrom,
            onExpandedChange = { expandedFrom = it }
        ) {
            OutlinedTextField(
                value = fromUnit.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            
            DropdownMenu(
                expanded = expandedFrom,
                onDismissRequest = { expandedFrom = false }
            ) {
                TimeUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.displayName) },
                        onClick = {
                            fromUnit = unit
                            expandedFrom = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input Field
        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue == "-" || newValue == "." ||
                    newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                    inputValue = newValue
                }
            },
            label = { Text("Enter value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            supportingText = { Text("Enter a non-negative value", style = MaterialTheme.typography.bodySmall) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Swap Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalIconButton(
                onClick = {
                    val tempUnit = fromUnit
                    fromUnit = toUnit
                    toUnit = tempUnit
                }
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap units"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // To Unit Section
        Text(
            text = "To",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expandedTo,
            onExpandedChange = { expandedTo = it }
        ) {
            OutlinedTextField(
                value = toUnit.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            
            DropdownMenu(
                expanded = expandedTo,
                onDismissRequest = { expandedTo = false }
            ) {
                TimeUnit.entries.forEach { unit ->
                    DropdownMenuItem(
                        text = { Text(unit.displayName) },
                        onClick = {
                            toUnit = unit
                            expandedTo = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Result Card
        if (result.isNotEmpty()) {
            ResultCard(
                title = "Result",
                result = "$result ${toUnit.displayName}",
                onCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("time result", result))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Reference",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                listOf(
                    "1 minute = 60 seconds" to "1 hour = 60 minutes",
                    "1 day = 24 hours" to "1 week = 7 days",
                    "1 month ≈ 30 days" to "1 year = 365 days",
                    "1 ms = 1000 μs" to "1 s = 1000 ms"
                ).forEach { (left, right) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(left, style = MaterialTheme.typography.bodySmall)
                        Text(right, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Divider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Interesting Facts:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                listOf(
                    "1 year in hours" to "~8,760 hrs",
                    "1 year in minutes" to "~525,600 min",
                    "1 century in days" to "~36,500 days",
                    "1 billion seconds" to "~31.69 years"
                ).forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, style = MaterialTheme.typography.bodySmall)
                        Text(value, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
