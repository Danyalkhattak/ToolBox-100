package com.dannyk.toolbox.ui.screens.tools.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import com.dannyk.toolbox.ui.components.ResultCard
import java.text.DecimalFormat
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import kotlin.math.*

@Composable
fun PercentageCalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var selectedMode by remember { mutableIntStateOf(0) }
    
    // Mode 1: X% of Y
    var percentage1 by remember { mutableStateOf("") }
    var valueY by remember { mutableStateOf("") }
    
    // Mode 2: Percentage change ((Y-X)/X * 100)
    var originalValue by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    
    // Mode 3: Percentage increase/decrease
    var baseValue3 by remember { mutableStateOf("") }
    var percentChange3 by remember { mutableStateOf("") }
    var isIncrease by remember { mutableStateOf(true) }
    
    // Mode 4: X is what % of Y
    var partValue by remember { mutableStateOf("") }
    var wholeValue by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Percentage Calculator",
            subtitle = "Calculate percentages in various ways",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Mode selector
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                listOf(
                    "X% of Y",
                    "Percentage Change",
                    "Increase/Decrease",
                    "X is What % of Y"
                ).forEachIndexed { index, mode ->
                    Surface(
                        color = if (selectedMode == index) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else Color.Transparent,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMode = index }
                    ) {
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selectedMode == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMode == index) 
                                MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected mode
        when (selectedMode) {
            0 -> BasicPercentageMode(
                percentage = percentage1,
                onPercentageChange = { percentage1 = it },
                value = valueY,
                onValueChange = { valueY = it },
                context = context
            )
            1 -> PercentageChangeMode(
                originalValue = originalValue,
                onOriginalValueChange = { originalValue = it },
                newValue = newValue,
                onNewValueChange = { newValue = it },
                context = context
            )
            2 -> IncreaseDecreaseMode(
                baseValue = baseValue3,
                onBaseValueChange = { baseValue3 = it },
                percentChange = percentChange3,
                onPercentChange = { percentChange3 = it },
                isIncrease = isIncrease,
                onIsIncreaseChange = { isIncrease = it },
                context = context
            )
            3 -> PartOfWholeMode(
                partValue = partValue,
                onPartValueChange = { partValue = it },
                wholeValue = wholeValue,
                onWholeValueChange = { wholeValue = it },
                context = context
            )
        }
    }
}

@Composable
private fun BasicPercentageMode(
    percentage: String,
    onPercentageChange: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    context: Context
) {
    val result = remember(percentage, value) {
        calculateBasicPercentage(percentage, value)
    }
    
    OutlinedTextField(
        value = percentage,
        onValueChange = onPercentageChange,
        label = { Text("Percentage (%)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Of Value") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    result?.let {
        ResultCard(
            title = "Result",
            result = it,
            onCopy = { copyToClipboard(context, it) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Show formula explanation
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "Formula: $percentage% × $value = $it",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PercentageChangeMode(
    originalValue: String,
    onOriginalValueChange: (String) -> Unit,
    newValue: String,
    onNewValueChange: (String) -> Unit,
    context: Context
) {
    val result = remember(originalValue, newValue) {
        calculatePercentageChange(originalValue, newValue)
    }
    
    OutlinedTextField(
        value = originalValue,
        onValueChange = onOriginalValueChange,
        label = { Text("Original Value (X)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = newValue,
        onValueChange = onNewValueChange,
        label = { Text("New Value (Y)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    result?.let { (percent, direction) ->
        val displayResult = "$percent% ($direction)"
        val color = if (direction == "Increase") 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.error
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Percentage Change",
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayResult,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { copyToClipboard(context, percent) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Formula: ((${newValue} - $originalValue) / |$originalValue|) × 100",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IncreaseDecreaseMode(
    baseValue: String,
    onBaseValueChange: (String) -> Unit,
    percentChange: String,
    onPercentChange: (String) -> Unit,
    isIncrease: Boolean,
    onIsIncreaseChange: (Boolean) -> Unit,
    context: Context
) {
    val result = remember(baseValue, percentChange, isIncrease) {
        calculateIncreaseDecrease(baseValue, percentChange, isIncrease)
    }
    
    OutlinedTextField(
        value = baseValue,
        onValueChange = onBaseValueChange,
        label = { Text("Base Value") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = percentChange,
        onValueChange = onPercentChange,
        label = { Text("Percentage") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
        trailingIcon = {
            Icon(
                imageVector = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = "Toggle increase/decrease",
                tint = if (isIncrease) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable { onIsIncreaseChange(!isIncrease) }
            )
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    // Toggle button for increase/decrease
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = isIncrease,
            onClick = { onIsIncreaseChange(true) },
            label = { Text("Increase") },
            leadingIcon = if (isIncrease) {{ Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = !isIncrease,
            onClick = { onIsIncreaseChange(false) },
            label = { Text("Decrease") },
            leadingIcon = if (!isIncrease) {{ Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp)) }} else null,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    result?.let { (newVal, difference) ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultCard(
                title = "New Value",
                result = newVal,
                onCopy = { copyToClipboard(context, newVal) }
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncrease) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${if (isIncrease) "Increase" else "Decrease"} Amount",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = difference,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartOfWholeMode(
    partValue: String,
    onPartValueChange: (String) -> Unit,
    wholeValue: String,
    onWholeValueChange: (String) -> Unit,
    context: Context
) {
    val result = remember(partValue, wholeValue) {
        calculatePartOfWhole(partValue, wholeValue)
    }
    
    OutlinedTextField(
        value = partValue,
        onValueChange = onPartValueChange,
        label = { Text("Part Value (X)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    
    OutlinedTextField(
        value = wholeValue,
        onValueChange = onWholeValueChange,
        label = { Text("Whole Value (Y)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        leadingIcon = { Icon(Icons.Default.DataArray, contentDescription = null) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    
    Spacer(modifier = Modifier.height(16.dp))
    
    result?.let {
        ResultCard(
            title = "Result",
            result = "$it%",
            onCopy = { copyToClipboard(context, it) }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = "Formula: ($partValue ÷ $wholeValue) × 100 = $it%",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Calculation functions
private fun calculateBasicPercentage(percentage: String, value: String): String? {
    return try {
        val pct = percentage.toDoubleOrNull() ?: return null
        val val_ = value.toDoubleOrNull() ?: return null
        val result = (pct / 100) * val_
        DecimalFormat("#,##0.##").format(result)
    } catch (e: Exception) {
        null
    }
}

private fun calculatePercentageChange(original: String, new: String): Pair<String, String>? {
    return try {
        val orig = original.toDoubleOrNull() ?: return null
        val new_ = new.toDoubleOrNull() ?: return null
        
        if (orig == 0.0) return "N/A" to "Cannot divide by zero"
        
        val change = ((new_ - orig) / kotlin.math.abs(orig)) * 100
        val direction = if (change >= 0) "Increase" else "Decrease"
        val formattedChange = DecimalFormat("#,##0.##").format(kotlin.math.abs(change))
        
        formattedChange to direction
    } catch (e: Exception) {
        null
    }
}

private fun calculateIncreaseDecrease(base: String, percent: String, isIncrease: Boolean): Pair<String, String>? {
    return try {
        val baseVal = base.toDoubleOrNull() ?: return null
        val pct = percent.toDoubleOrNull() ?: return null
        
        val changeAmount = baseVal * (pct / 100)
        val newBase = if (isIncrease) baseVal + changeAmount else baseVal - changeAmount
        
        val formatter = DecimalFormat("#,##0.##")
        formatter.format(newBase) to formatter.format(kotlin.math.abs(changeAmount))
    } catch (e: Exception) {
        null
    }
}

private fun calculatePartOfWhole(part: String, whole: String): String? {
    return try {
        val partVal = part.toDoubleOrNull() ?: return null
        val wholeVal = whole.toDoubleOrNull() ?: return null
        
        if (wholeVal == 0.0) return null
        
        val result = (partVal / wholeVal) * 100
        DecimalFormat("#,##0.##").format(result)
    } catch (e: Exception) {
        null
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Percentage Result", text)
    clipboard.setPrimaryClip(clip)
}
