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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

enum class WeightUnit(val displayName: String, val toKilogram: Double) {
    MILLIGRAM("Milligram (mg)", 0.000001),
    GRAM("Gram (g)", 0.001),
    KILOGRAM("Kilogram (kg)", 1.0),
    METRIC_TON("Metric Ton (t)", 1000.0),
    OUNCE("Ounce (oz)", 0.028349523125),
    POUND("Pound (lb)", 0.45359237),
    STONE("Stone (st)", 6.35029318),
    US_TON("US Ton (short ton)", 907.18474)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf(WeightUnit.KILOGRAM) }
    var toUnit by remember { mutableStateOf(WeightUnit.GRAM) }
    
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    
    // Calculate conversion
    val result = remember(inputValue, fromUnit, toUnit) {
        if (inputValue.isEmpty() || inputValue == "-" || inputValue == ".") {
            ""
        } else {
            val value = inputValue.toDoubleOrNull()
            if (value != null) {
                // Convert to kilograms first, then to target unit
                val kilograms = value * fromUnit.toKilogram
                val resultValue = kilograms / toUnit.toKilogram
                
                // Format result nicely
                if (resultValue == 0.0 || resultValue >= 1e9 || resultValue <= 1e-6) {
                    String.format("%.6e", resultValue)
                } else if (resultValue % 1.0 == 0.0 && !resultValue.isInfinite()) {
                    String.format("%.0f", resultValue)
                } else {
                    String.format("%.6g", resultValue).let { 
                        if (it.contains('e')) it else it.removeSuffix(".0").removeSuffix(".00")
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
            title = "Weight Converter",
            subtitle = "Convert between metric and imperial weight units",
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
                WeightUnit.entries.forEach { unit ->
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
                if (newValue.isEmpty() || newValue == "-" || newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
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
                WeightUnit.entries.forEach { unit ->
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
                    clipboard.setPrimaryClip(ClipData.newPlainText("weight result", result))
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
                    "1 kg = 1000 g" to "1 lb = 16 oz",
                    "1 stone = 14 lbs" to "1 US ton = 2000 lbs",
                    "1 metric ton = 1000 kg" to "1 oz ≈ 28.35 g"
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
            }
        }
    }
}
