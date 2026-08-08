package com.dannyk.toolbox.ui.screens.tools.files

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import java.text.DecimalFormat
import java.util.Locale
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSizeConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    // Input state
    var inputValue by remember { mutableStateOf("") }
    var sourceUnit by remember { mutableStateOf("MB") }
    var targetUnit by remember { mutableStateOf("GB") }
    
    // Conversion mode: Binary (1024) or Decimal (1000)
    var isBinaryMode by remember { mutableStateOf(true) }
    
    // Results
    val conversionResult = remember(inputValue, sourceUnit, targetUnit, isBinaryMode) {
        convertFileSize(inputValue, sourceUnit, targetUnit, isBinaryMode)
    }
    
    val allConversions = remember(inputValue, sourceUnit, isBinaryMode) {
        if (inputValue.isNotEmpty() && inputValue.toDoubleOrNull() != null) {
            getAllConversions(inputValue.toDoubleOrNull() ?: 0.0, sourceUnit, isBinaryMode)
        } else {
            emptyMap()
        }
    }

    ToolScreenLayout(
        title = "File Size Converter",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Selection - Binary vs Decimal
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Conversion Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isBinaryMode,
                            onClick = { isBinaryMode = true },
                            label = { 
                                Text("Binary (1024)") 
                            },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isBinaryMode,
                            onClick = { isBinaryMode = false },
                            label = { 
                                Text("Decimal (1000)") 
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (isBinaryMode) 
                            "Using binary prefixes: 1 KB = 1024 Bytes (commonly used in computing)" 
                        else 
                            "Using decimal prefixes: 1 KB = 1000 Bytes (SI standard)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter Value",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { newValue ->
                            // Allow numbers, decimal point, and negative sign at start
                            if (newValue.isEmpty() || 
                                newValue.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                                inputValue = newValue
                            }
                        },
                        label = { Text("Enter file size") },
                        placeholder = { Text("e.g., 1024 or 1.5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Text(
                                text = "#",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Source Unit Selection
                    Text(
                        text = "From:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val units = listOf("Bytes", "KB", "MB", "GB", "TB", "PB")
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        units.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = sourceUnit == unit,
                                onClick = { sourceUnit = unit },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size),
                                icon = {}
                            ) {
                                Text(unit)
                            }
                        }
                    }
                }
            }

            // Swap Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FilledTonalButton(
                    onClick = {
                        val temp = sourceUnit
                        sourceUnit = targetUnit
                        targetUnit = temp
                    }
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    text = "Swap Units"
                }
            }

            // Target Unit Selection & Result
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Convert To:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val targetUnits = listOf("Bytes", "KB", "MB", "GB", "TB", "PB")
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        targetUnits.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = targetUnit == unit,
                                onClick = { targetUnit = unit },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = targetUnits.size),
                                icon = {}
                            ) {
                                Text(unit)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Result Display
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Result",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = conversionResult.first,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = targetUnit,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (conversionResult.second.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "≈ ${conversionResult.second}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Copy Button
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("file size result", "${conversionResult.first} ${targetUnit}")
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Result")
                    }
                }
            }

            // All Conversions Table
            if (allConversions.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "All Conversions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        allConversions.forEach { (unit, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = value,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            if (unit != "PB") {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Reference
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Reference",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isBinaryMode) {
                        Text(
                            text = "• 1 Byte = 8 bits\n" +
                                   "• 1 KB (Kilobyte) = 1,024 Bytes\n" +
                                   "• 1 MB (Megabyte) = 1,024 KB = 1,048,576 Bytes\n" +
                                   "• 1 GB (Gigabyte) = 1,024 MB = 1,073,741,824 Bytes\n" +
                                   "• 1 TB (Terabyte) = 1,024 GB\n" +
                                   "• 1 PB (Petabyte) = 1,024 TB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "• 1 Byte = 8 bits\n" +
                                   "• 1 KB (Kilobyte) = 1,000 Bytes\n" +
                                   "• 1 MB (Megabyte) = 1,000 KB = 1,000,000 Bytes\n" +
                                   "• 1 GB (Gigabyte) = 1,000 MB = 1,000,000,000 Bytes\n" +
                                   "• 1 TB (Terabyte) = 1,000 GB\n" +
                                   "• 1 PB (Petabyte) = 1,000 TB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun convertFileSize(
    input: String,
    fromUnit: String,
    toUnit: String,
    isBinary: Boolean
): Pair<String, String> {
    val value = input.toDoubleOrNull() ?: return Pair("-", "")
    
    val base = if (isBinary) 1024.0 else 1000.0
    
    // Convert to bytes first
    val bytesValue = when (fromUnit) {
        "Bytes" -> value
        "KB" -> value * base
        "MB" -> value * base * base
        "GB" -> value * base * base * base
        "TB" -> value * base * base * base * base
        "PB" -> value * base * base * base * base * base
        else -> value
    }
    
    // Convert from bytes to target unit
    val result = when (toUnit) {
        "Bytes" -> bytesValue
        "KB" -> bytesValue / base
        "MB" -> bytesValue / (base * base)
        "GB" -> bytesValue / (base * base * base)
        "TB" -> bytesValue / (base * base * base * base)
        "PB" -> bytesValue / (base * base * base * base * base)
        else -> bytesValue
    }
    
    // Format the result
    val df = DecimalFormat("#,##0.########")
    val formattedResult = df.format(result)
    
    // Human-readable version for very large/small numbers
    val humanReadable = formatHumanReadable(bytesValue, isBinary)
    
    return Pair(formattedResult, humanReadable)
}

private fun getAllConversions(
    value: Double,
    fromUnit: String,
    isBinary: Boolean
): Map<String, String> {
    val base = if (isBinary) 1024.0 else 1000.0
    
    // Convert to bytes first
    val bytesValue = when (fromUnit) {
        "Bytes" -> value
        "KB" -> value * base
        "MB" -> value * base * base
        "GB" -> value * base * base * base
        "TB" -> value * base * base * base * base
        "PB" -> value * base * base * base * base * base
        else -> value
    }
    
    val df = DecimalFormat("#,##0.##")
    
    return mapOf(
        "Bytes" to df.format(bytesValue),
        "KB" to df.format(bytesValue / base),
        "MB" to df.format(bytesValue / (base * base)),
        "GB" to df.format(bytesValue / (base * base * base)),
        "TB" to df.format(bytesValue / (base * base * base * base)),
        "PB" to df.format(bytesValue / (base * base * base * base * base))
    )
}

private fun formatHumanReadable(bytes: Double, isBinary: Boolean): String {
    val base = if (isBinary) 1024.0 else 1000.0
    val df = DecimalFormat("0.##")
    
    return when {
        bytes < base -> "${df.format(bytes)} B"
        bytes < base * base -> "${df.format(bytes / base)} KB"
        bytes < base * base * base -> "${df.format(bytes / (base * base))} MB"
        bytes < base * base * base * base -> "${df.format(bytes / (base * base * base))} GB"
        bytes < base * base * base * base * base -> "${df.format(bytes / (base * base * base * base))} TB"
        else -> "${df.format(bytes / (base * base * base * base * base))} PB"
    }
}
