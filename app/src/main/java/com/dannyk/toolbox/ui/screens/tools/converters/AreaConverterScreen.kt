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

enum class AreaUnit(val displayName: String, val toSquareMeter: Double) {
    SQ_MILLIMETER("Square Millimeter (mm²)", 0.000001),
    SQ_CENTIMETER("Square Centimeter (cm²)", 0.0001),
    SQ_METER("Square Meter (m²)", 1.0),
    SQ_KILOMETER("Square Kilometer (km²)", 1_000_000.0),
    HECTARE("Hectare (ha)", 10_000.0),
    ACRE("Acre (ac)", 4046.8564224),
    SQ_FOOT("Square Foot (ft²)", 0.09290304),
    SQ_MILE("Square Mile (mi²)", 2_589_988.110336)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf(AreaUnit.SQ_METER) }
    var toUnit by remember { mutableStateOf(AreaUnit.SQ_CENTIMETER) }
    
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    
    // Calculate conversion
    val result = remember(inputValue, fromUnit, toUnit) {
        if (inputValue.isEmpty() || inputValue == "-" || inputValue == ".") {
            ""
        } else {
            val value = inputValue.toDoubleOrNull()
            if (value != null && value >= 0) {
                // Convert to square meters first, then to target unit
                val squareMeters = value * fromUnit.toSquareMeter
                val resultValue = squareMeters / toUnit.toSquareMeter
                
                // Format result nicely
                when {
                    resultValue == 0.0 -> "0"
                    resultValue >= 1e9 || resultValue <= 1e-6 -> String.format("%.6e", resultValue)
                    resultValue % 1.0 == 0.0 && !resultValue.isInfinite() -> String.format("%.0f", resultValue)
                    else -> String.format("%.6g", resultValue).let { 
                        if (it.contains('e')) it else it.removeSuffix(".0").removeSuffix(".00")
                    }
                }
            } else if (value != null && value < 0) {
                "Area cannot be negative"
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
            title = "Area Converter",
            subtitle = "Convert between metric and imperial area units",
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
                AreaUnit.entries.forEach { unit ->
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
                AreaUnit.entries.forEach { unit ->
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
                    clipboard.setPrimaryClip(ClipData.newPlainText("area result", result))
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
                    "1 m² = 10,000 cm²" to "1 m² ≈ 10.76 ft²",
                    "1 hectare = 10,000 m²" to "1 acre ≈ 4,047 m²",
                    "1 km² = 100 hectares" to "1 sq mile ≈ 2.59 km²",
                    "1 acre ≈ 43,560 ft²" to "1 ft² ≈ 929 cm²"
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
