package com.dannyk.toolbox.ui.screens.tools.converters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.ui.draw.clip
import java.util.regex.Pattern

enum class NumberSystem(val displayName: String, val base: Int, val prefix: String) {
    BINARY("Binary (Base 2)", 2, "0b"),
    OCTAL("Octal (Base 8)", 8, "0o"),
    DECIMAL("Decimal (Base 10)", 10, ""),
    HEXADECIMAL("Hexadecimal (Base 16)", 16, "0x")
}

data class ConversionResult(
    val binary: String,
    val octal: String,
    val decimal: String,
    val hexadecimal: String,
    val isValid: Boolean = true,
    val errorMessage: String? = null
)

fun convertNumberSystem(input: String, sourceBase: Int): ConversionResult {
    if (input.isEmpty()) {
        return ConversionResult("", "", "", "", false)
    }
    
    // Clean input - remove prefixes
    val cleanInput = when {
        input.startsWith("0b") || input.startsWith("0B") -> input.substring(2)
        input.startsWith("0o") || input.startsWith("0O") -> input.substring(2)
        input.startsWith("0x") || input.startsWith("0X") -> input.substring(2)
        else -> input
    }
    
    if (cleanInput.isEmpty()) {
        return ConversionResult("", "", "", "", false)
    }
    
    // Validate and parse based on source base
    val validChars = when (sourceBase) {
        2 -> Regex("^[01]+$")
        8 -> Regex("^[0-7]+$")
        10 -> Regex("^-?\\d+$")
        16 -> Regex("^[0-9a-fA-F]+$")
        else -> null
    }
    
    // For auto-detect, try to determine the base
    val actualInput = cleanInput.trim()
    val actualBase: Int
    val valueToConvert: Long
    
    if (sourceBase == 0) { // Auto-detect mode
        when {
            actualInput.matches(Regex("^[01]+$")) -> actualBase = 2
            actualInput.matches(Regex("^[0-7]+$")) && !actualInput.matches(Regex("^[01]+$")) -> actualBase = 8
            actualInput.matches(Regex("^[0-9a-fA-F]+$")) && 
                !actualInput.matches(Regex("^[0-9]+$")) -> actualBase = 16
            actualInput.matches(Regex("^-?\\d+$")) -> actualBase = 10
            else -> return ConversionResult("", "", "", "", false, "Invalid number format")
        }
        
        valueToConvert = try {
            actualInput.toLong(actualBase)
        } catch (e: Exception) {
            return ConversionResult("", "", "", "", false, e.message ?: "Invalid number")
        }
    } else {
        actualBase = sourceBase
        
        if (!actualInput.matches(validChars!!)) {
            return ConversionResult("", "", "", "", false, "Invalid characters for selected base")
        }
        
        valueToConvert = try {
            actualInput.toLong(actualBase)
        } catch (e: Exception) {
            return ConversionResult("", "", "", "", false, e.message ?: "Invalid number")
        }
    }
    
    // Convert to all bases
    return ConversionResult(
        binary = valueToConvert.toString(2),
        octal = valueToConvert.toString(8),
        decimal = valueToConvert.toString(),
        hexadecimal = valueToConvert.toString(16).uppercase()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberSystemConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var inputValue by remember { mutableStateOf("") }
    var sourceSystem by remember { mutableStateOf(NumberSystem.DECIMAL) }
    var expandedSource by remember { mutableStateOf(false) }
    
    // Calculate conversion
    val result = remember(inputValue, sourceSystem) {
        convertNumberSystem(inputValue, sourceSystem.base)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        ToolHeader(
            title = "Number System Converter",
            subtitle = "Convert between Binary, Octal, Decimal, and Hexadecimal",
            onBack = { navController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Source System Selection
        Text(
            text = "Source Number System",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ExposedDropdownMenuBox(
            expanded = expandedSource,
            onExpandedChange = { expandedSource = it }
        ) {
            OutlinedTextField(
                value = sourceSystem.displayName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSource) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            
            DropdownMenu(
                expanded = expandedSource,
                onDismissRequest = { expandedSource = false }
            ) {
                NumberSystem.entries.forEach { system ->
                    DropdownMenuItem(
                        text = { Text(system.displayName) },
                        onClick = {
                            sourceSystem = system
                            expandedSource = false
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
                // Allow valid characters for the selected system
                val allowedPattern = when (sourceSystem) {
                    NumberSystem.BINARY -> Regex("^[0-9a-fA-F]*$") // Allow hex prefix chars too
                    NumberSystem.OCTAL -> Regex("^[0-9a-fA-F]*$")
                    NumberSystem.DECIMAL -> Regex("^[0-9a-fA-F]*$")
                    NumberSystem.HEXADECIMAL -> Regex("^[0-9a-fA-F]*$")
                }
                
                // Allow prefixes
                val cleanedNew = newValue.lowercase().removePrefix("0b").removePrefix("0o").removePrefix("0x")
                if (newValue.isEmpty() || cleanedNew.isEmpty() || cleanedNew.matches(allowedPattern)) {
                    inputValue = newValue
                }
            },
            label = { Text("Enter ${sourceSystem.displayName} number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            supportingText = { 
                Text(
                    text = when (sourceSystem) {
                        NumberSystem.BINARY -> "Enter only 0s and 1s"
                        NumberSystem.OCTAL -> "Enter digits 0-7"
                        NumberSystem.DECIMAL -> "Enter digits 0-9"
                        NumberSystem.HEXADECIMAL -> "Enter digits 0-9 and letters A-F"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
        
        // Error message
        result.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        if (result.isValid && result.decimal.isNotEmpty()) {
            Text(
                text = "Conversion Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Binary Result
                ResultCardWithCopy(
                    title = "Binary (Base 2)",
                    result = result.binary,
                    prefix = "0b",
                    context = context
                )
                
                // Octal Result
                ResultCardWithCopy(
                    title = "Octal (Base 8)",
                    result = result.octal,
                    prefix = "0o",
                    context = context
                )
                
                // Decimal Result
                ResultCardWithCopy(
                    title = "Decimal (Base 10)",
                    result = result.decimal,
                    prefix = "",
                    context = context
                )
                
                // Hexadecimal Result
                ResultCardWithCopy(
                    title = "Hexadecimal (Base 16)",
                    result = result.hexadecimal,
                    prefix = "0x",
                    context = context
                )
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
                
                listOf(
                    Triple("Binary", "0", "1"),
                    Triple("Octal", "0-7", "8 digits"),
                    Triple("Decimal", "0-9", "10 digits"),
                    Triple("Hexadecimal", "0-9, A-F", "16 digits")
                ).forEach { (name, range, note) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(range, style = MaterialTheme.typography.bodySmall)
                        Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Divider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Common Values:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                listOf(
                    "255" to "FF (hex) = 377 (oct) = 11111111 (bin)",
                    "256" to "100 (hex) = 400 (oct) = 100000000 (bin)",
                    "1024" to "400 (hex) = 2000 (oct) = 10000000000 (bin)"
                ).forEach { (dec, conversions) ->
                    Text(
                        text = "$dec → $conversions",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultCardWithCopy(
    title: String,
    result: String,
    prefix: String,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$prefix$result",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
            
            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("$title result", "$prefix$result"))
                    Toast.makeText(context, "Copied $prefix$result", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
