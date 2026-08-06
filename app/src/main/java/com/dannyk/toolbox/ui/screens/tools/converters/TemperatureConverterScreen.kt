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

enum class TemperatureUnit(val displayName: String) {
    CELSIUS("Celsius (°C)"),
    FAHRENHEIT("Fahrenheit (°F)"),
    KELVIN("Kelvin (K)")
}

// Convert temperature to Celsius first, then to target unit
fun convertTemperature(value: Double, from: TemperatureUnit, to: TemperatureUnit): Double {
    // Convert source to Celsius
    val celsius = when (from) {
        TemperatureUnit.CELSIUS -> value
        TemperatureUnit.FAHRENHEIT -> (value - 32) * 5 / 9
        TemperatureUnit.KELVIN -> value - 273.15
    }
    
    // Convert Celsius to target
    return when (to) {
        TemperatureUnit.CELSIUS -> celsius
        TemperatureUnit.FAHRENHEIT -> celsius * 9 / 5 + 32
        TemperatureUnit.KELVIN -> celsius + 273.15
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemperatureConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf(TemperatureUnit.CELSIUS) }
    var toUnit by remember { mutableStateOf(TemperatureUnit.FAHRENHEIT) }
    
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    
    // Calculate conversion
    val result = remember(inputValue, fromUnit, toUnit) {
        if (inputValue.isEmpty() || inputValue == "-" || inputValue == "." || inputValue == "-.") {
            ""
        } else {
            val value = inputValue.toDoubleOrNull()
            if (value != null) {
                val resultValue = convertTemperature(value, fromUnit, toUnit)
                
                // Format result nicely
                when {
                    resultValue.isNaN() || resultValue.isInfinite() -> "Invalid"
                    resultValue % 1.0 == 0.0 -> String.format("%.0f", resultValue)
                    else -> String.format("%.2f", resultValue).let { 
                        it.removeSuffix(".00").removeSuffix(".0")
                    }
                }
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
            title = "Temperature Converter",
            subtitle = "Convert between Celsius, Fahrenheit, and Kelvin",
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
                TemperatureUnit.entries.forEach { unit ->
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
                    newValue == "-." || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                    inputValue = newValue
                }
            },
            label = { Text("Enter value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
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
                TemperatureUnit.entries.forEach { unit ->
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
                    clipboard.setPrimaryClip(ClipData.newPlainText("temperature result", result))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Reference Card - Formulas
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
                    text = "Conversion Formulas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "°F = °C × 9/5 + 32",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "°C = (°F - 32) × 5/9",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "K = °C + 273.15",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Reference Points:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                listOf(
                    "Water freezes" to "0°C = 32°F = 273.15K",
                    "Room temperature" to "20°C ≈ 68°F = 293.15K",
                    "Body temperature" to "37°C ≈ 98.6°F = 310.15K",
                    "Water boils" to "100°C = 212°F = 373.15K"
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
