package com.dannyk.toolbox.ui.screens.tools.converters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

enum class DataUnit(val displayName: String, val toByteBinary: BigDecimal, val toByteDecimal: BigDecimal) {
    BIT("Bit (b)", BigDecimal("0.125"), BigDecimal("0.125")),
    BYTE("Byte (B)", BigDecimal("1"), BigDecimal("1")),
    KILOBYTE_BINARY("Kilobyte (KB)", BigDecimal("1024"), BigDecimal("1000")),
    MEGABYTE_BINARY("Megabyte (MB)", BigDecimal("1048576"), BigDecimal("1000000")),
    GIGABYTE_BINARY("Gigabyte (GB)", BigDecimal("1073741824"), BigDecimal("1000000000")),
    TERABYTE_BINARY("Terabyte (TB)", BigDecimal("1099511627776"), BigDecimal("1000000000000")),
    PETABYTE_BINARY("Petabyte (PB)", BigDecimal("1125899906842624"), BigDecimal("1000000000000000"))
}

enum class ConversionType(val displayName: String) {
    BINARY("Binary (1024) - IEC/JEDEC"),
    DECIMAL("Decimal (1000) - SI")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf(DataUnit.BYTE) }
    var toUnit by remember { mutableStateOf(DataUnit.MEGABYTE_BINARY) }
    var conversionType by remember { mutableStateOf(ConversionType.BINARY) }
    
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    
    // Calculate conversion
    val result = remember(inputValue, fromUnit, toUnit, conversionType) {
        if (inputValue.isEmpty() || inputValue == "-" || inputValue == ".") {
            ""
        } else {
            val value = inputValue.toBigDecimalOrNull()
            if (value != null && value >= BigDecimal.ZERO) {
                // Use appropriate conversion factor based on type
                val fromFactor = if (conversionType == ConversionType.BINARY) fromUnit.toByteBinary else fromUnit.toByteDecimal
                val toFactor = if (conversionType == ConversionType.BINARY) toUnit.toByteBinary else toUnit.toByteDecimal
                
                // Convert to bytes first, then to target unit
                val bytes = value * fromFactor
                val resultValue = bytes.divide(toFactor, 12, RoundingMode.HALF_UP)
                
                // Format result nicely
                when {
                    resultValue.compareTo(BigDecimal.ZERO) == 0 -> "0"
                    resultValue >= BigDecimal("1e15") || resultValue <= BigDecimal("1e-6") -> 
                        resultValue.setScale(6, RoundingMode.HALF_UP).toEngineeringString()
                    resultValue.stripTrailingZeros().scale() <= 0 -> 
                        resultValue.setScale(0, RoundingMode.DOWN).toPlainString()
                    else -> resultValue.stripTrailingZeros().toPlainString()
                }
            } else if (value != null && value < BigDecimal.ZERO) {
                "Data size cannot be negative"
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
            title = "Data Storage Converter",
            subtitle = "Convert between data storage units (binary & decimal)",
            onBack = { navController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Conversion Type Selector
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
                ConversionType.entries.forEach { type ->
                    FilterChip(
                        selected = conversionType == type,
                        onClick = { conversionType = type },
                        label = { Text(type.displayName) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
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
                DataUnit.entries.forEach { unit ->
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
                DataUnit.entries.forEach { unit ->
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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ResultCard(
                    title = "Result (${conversionType.displayName})",
                    result = "$result ${toUnit.displayName}",
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("data storage result", result))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
                
                // Show both conversions for comparison
                if (inputValue.isNotEmpty() && inputValue.toDoubleOrNull() != null && 
                    inputValue.toDoubleOrNull()!! >= 0) {
                    val otherType = if (conversionType == ConversionType.BINARY) ConversionType.DECIMAL else ConversionType.BINARY
                    val otherFromFactor = if (otherType == ConversionType.BINARY) fromUnit.toByteBinary else fromUnit.toByteDecimal
                    val otherToFactor = if (otherType == ConversionType.BINARY) toUnit.toByteBinary else toUnit.toByteDecimal
                    val bytes = inputValue.toBigDecimal() * otherFromFactor
                    val otherResult = bytes.divide(otherToFactor, 12, RoundingMode.HALF_UP).stripTrailingZeros()
                    
                    ResultCard(
                        title = "Result (${otherType.displayName})",
                        result = "$otherResult ${toUnit.displayName}",
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("data storage result", otherResult.toString()))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Reference",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Binary (Base 2):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                listOf(
                    "1 KB = 1,024 Bytes" to "1 MB = 1,024 KB",
                    "1 GB = 1,024 MB" to "1 TB = 1,024 GB"
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
                    text = "Decimal (Base 10):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                listOf(
                    "1 KB = 1,000 Bytes" to "1 MB = 1,000 KB",
                    "1 GB = 1,000 MB" to "1 TB = 1,000 GB"
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
                    text = "Note: Binary is commonly used for RAM/storage capacity. Decimal is used for disk drive marketing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
