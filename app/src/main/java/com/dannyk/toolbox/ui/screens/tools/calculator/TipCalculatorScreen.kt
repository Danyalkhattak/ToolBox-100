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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import kotlin.math.*

@Composable
fun TipCalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var billAmount by remember { mutableStateOf("") }
    var tipPercentage by remember { mutableStateOf("15") }
    var customTipPercentage by remember { mutableStateOf("") }
    var isCustomTip by remember { mutableStateOf(false) }
    var numberOfPeople by remember { mutableStateOf("1") }
    var roundUp by remember { mutableStateOf(false) }
    
    val result = remember(billAmount, tipPercentage, customTipPercentage, isCustomTip, numberOfPeople, roundUp) {
        calculateTip(
            billAmount = billAmount,
            tipPercent = if (isCustomTip) customTipPercentage else tipPercentage,
            people = numberOfPeople,
            roundUp = roundUp
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Tip Calculator",
            subtitle = "Calculate tips and split bills easily",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bill Amount Input
        OutlinedTextField(
            value = billAmount,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    billAmount = it 
                }
            },
            label = { Text("Bill Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
            trailingIcon = {
                if (billAmount.isNotEmpty()) {
                    IconButton(onClick = { billAmount = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tip Percentage Section
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tip Percentage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Custom tip toggle
                    FilterChip(
                        selected = isCustomTip,
                        onClick = { 
                            isCustomTip = !isCustomTip
                            if (!isCustomTip) customTipPercentage = ""
                        },
                        label = { Text("Custom") }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (!isCustomTip) {
                    // Preset tip buttons
                    listOf(10 to "10%", 15 to "15%", 18 to "18%", 20 to "20%", 25 to "25%").forEach { (value, label) ->
                        FilterChip(
                            selected = tipPercentage == value.toString(),
                            onClick = { tipPercentage = value.toString() },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(label)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${formatCurrency_TipCalculatorScreen(calculateTipOnly(billAmount, value.toString()))})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                        )
                        
                        if (value != 25) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                    
                    // Horizontal layout for chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10, 15, 18, 20, 25).forEach { value ->
                            FilterChip(
                                selected = tipPercentage == value.toString(),
                                onClick = { tipPercentage = value.toString() },
                                label = { Text("$value%") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customTipPercentage,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                customTipPercentage = it 
                            }
                        },
                        label = { Text("Custom Tip %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Current tip amount preview
                if (billAmount.isNotEmpty()) {
                    val currentTipPct = if (isCustomTip) customTipPercentage else tipPercentage
                    if (currentTipPct.isNotEmpty()) {
                        Text(
                            text = "Tip Amount: ${formatCurrency_TipCalculatorScreen(calculateTipOnly(billAmount, currentTipPct))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Split Options
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Split Between",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Number of people selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decrease button
                    IconButton(
                        onClick = { 
                            val current = numberOfPeople.toIntOrNull() ?: 1
                            if (current > 1) numberOfPeople = (current - 1).toString()
                        },
                        enabled = (numberOfPeople.toIntOrNull() ?: 1) > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    
                    // Number display
                    OutlinedTextField(
                        value = numberOfPeople,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d{0,3}$")) && (it.isEmpty() || it.toIntOrNull()?.let { n -> n in 1..999 } == true)) {
                                numberOfPeople = it.ifEmpty { "1" }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    
                    // Increase button
                    IconButton(
                        onClick = { 
                            val current = numberOfPeople.toIntOrNull() ?: 1
                            if (current < 99) numberOfPeople = (current + 1).toString()
                        },
                        enabled = (numberOfPeople.toIntOrNull() ?: 1) < 99
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Round up option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Round up total",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = roundUp,
                        onCheckedChange = { roundUp = it }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        result?.let { res ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Total Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Amount",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = { copyToClipboard_TipCalculatorScreen(context, res.totalPerPerson) }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = formatCurrency_TipCalculatorScreen(res.total),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        if (res.peopleCount > 1) {
                            Text(
                                text = "${formatCurrency_TipCalculatorScreen(res.totalPerPerson)} per person",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Breakdown Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ResultRowItem(label = "Bill Amount", value = formatCurrency_TipCalculatorScreen(res.billAmount))
                        ResultRowItem(
                            label = "Tip (${res.tipPercentage}%)", 
                            value = formatCurrency_TipCalculatorScreen(res.tipAmount),
                            valueColor = MaterialTheme.colorScheme.primary
                        )
                        
                        Divider(modifier = Modifier.fillMaxWidth())
                        
                        ResultRowItem(
                            label = "Total", 
                            value = formatCurrency_TipCalculatorScreen(res.total),
                            isBold = true
                        )
                        
                        if (res.peopleCount > 1) {
                            Divider(modifier = Modifier.fillMaxWidth())
                            
                            Text(
                                text = "Split ${res.peopleCount} ways:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            
                            ResultRowItem(
                                label = "Each person pays:", 
                                value = formatCurrency_TipCalculatorScreen(res.totalPerPerson),
                                valueStyle = MaterialTheme.typography.titleMedium,
                                valueColor = MaterialTheme.colorScheme.primary,
                                isBold = true
                            )
                            
                            ResultRowItem(
                                label = "  - Bill portion", 
                                value = formatCurrency_TipCalculatorScreen(res.billPerPerson)
                            )
                            ResultRowItem(
                                label = "  - Tip portion", 
                                value = formatCurrency_TipCalculatorScreen(res.tipPerPerson)
                            )
                        }
                    }
                }
                
                // Quick summary for multiple people
                if (res.peopleCount > 2) {
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
                                Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Everyone pays",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatCurrency_TipCalculatorScreen(res.totalPerPerson),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { copyToClipboard_TipCalculatorScreen(context, res.totalPerPerson) }) {
                                Text("Share")
                            }
                        }
                    }
                }
            }
        } ?: run {
            // Empty state
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter your bill amount to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultRowItem(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = valueStyle.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal),
            color = valueColor
        )
    }
}

// Data class for results
data class TipResult(
    val billAmount: Double,
    val tipAmount: Double,
    val total: Double,
    val tipPercentage: String,
    val peopleCount: Int,
    val totalPerPerson: Double,
    val billPerPerson: Double,
    val tipPerPerson: Double
)

private fun calculateTipOnly(billAmount: String, tipPercent: String): String {
    return try {
        val bill = billAmount.toDoubleOrNull() ?: return "0.00"
        val tip = tipPercent.toDoubleOrNull() ?: return "0.00"
        DecimalFormat("#,##0.00").format(bill * (tip / 100))
    } catch (e: Exception) {
        "0.00"
    }
}

private fun calculateTip(
    billAmount: String,
    tipPercent: String,
    people: String,
    roundUp: Boolean
): TipResult? {
    return try {
        val bill = billAmount.toDoubleOrNull() ?: return null
        val tipPct = tipPercent.toDoubleOrNull() ?: return null
        val numPeople = people.toIntOrNull() ?: return null
        
        if (bill <= 0 || tipPct < 0 || numPeople < 1) return null
        
        var tipAmount = bill * (tipPct / 100)
        var total = bill + tipAmount
        
        if (roundUp && numPeople == 1) {
            total = kotlin.math.ceil(total)
            tipAmount = total - bill
        }
        
        val totalPerPerson = total / numPeople
        val roundedTotalPerPerson = if (roundUp) kotlin.math.ceil(totalPerPerson) else totalPerPerson
        val finalTotal = if (roundUp) roundedTotalPerPerson * numPeople else total
        val finalTip = finalTotal - bill
        
        TipResult(
            billAmount = bill,
            tipAmount = finalTip,
            total = finalTotal,
            tipPercentage = DecimalFormat("#,##0.#").format(tipPct),
            peopleCount = numPeople,
            totalPerPerson = roundedTotalPerPerson,
            billPerPerson = bill / numPeople,
            tipPerPerson = finalTip / numPeople
        )
    } catch (e: Exception) {
        null
    }
}

private fun formatCurrency_TipCalculatorScreen(amount: Double): String {
    return try {
        DecimalFormat("$#,##0.00").format(amount)
    } catch (e: Exception) {
        "$0.00"
    }
}

private fun formatCurrency_TipCalculatorScreen(amount: String): String {
    return try {
        val num = amount.toDoubleOrNull() ?: return "$0.00"
        DecimalFormat("$#,##0.00").format(num)
    } catch (e: Exception) {
        "$0.00"
    }
}

private fun copyToClipboard_TipCalculatorScreen(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Tip Result", text)
    clipboard.setPrimaryClip(clip)
}
