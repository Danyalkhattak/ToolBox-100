package com.dannyk.toolbox.ui.screens.tools.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat

@Composable
fun SplitBillScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var totalBill by remember { mutableStateOf("") }
    var tipPercentage by remember { mutableStateOf("15") }
    var numberOfPeople by remember { mutableStateOf("2") }
    var useCustomAmounts by remember { mutableStateOf(false) }
    
    // Custom amounts for each person
    var customAmounts by remember {
        mutableStateOf(mutableMapOf<Int, String>())
    }
    
    // Update custom amounts when number of people changes
    LaunchedEffect(numberOfPeople) {
        val num = numberOfPeople.toIntOrNull() ?: 2
        val currentKeys = customAmounts.keys.toSet()
        val newKeys = (0 until num).toSet()
        
        // Remove excess entries
        customAmounts = customAmounts.filterKeys { it in newKeys }.toMutableMap()
        
        // Add new entries with default values
        for (i in newKeys - currentKeys) {
            if (!customAmounts.containsKey(i)) {
                customAmounts[i] = ""
            }
        }
    }
    
    val result = remember(totalBill, tipPercentage, numberOfPeople, useCustomAmounts, customAmounts) {
        calculateSplit(
            totalBill = totalBill,
            tipPercent = tipPercentage,
            peopleCount = numberOfPeople,
            useCustomAmounts = useCustomAmounts,
            customAmounts = customAmounts
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Split Bill",
            subtitle = "Divide bills fairly among friends",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Total Bill Input
        OutlinedTextField(
            value = totalBill,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    totalBill = it 
                }
            },
            label = { Text("Total Bill Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            trailingIcon = {
                if (totalBill.isNotEmpty()) {
                    IconButton(onClick = { totalBill = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tip Percentage
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tip:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(60.dp)
            )
            
            listOf("0", "10", "15", "18", "20", "25").forEach { pct ->
                FilterChip(
                    selected = tipPercentage == pct,
                    onClick = { tipPercentage = pct },
                    label = { Text("$pct%") },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Number of People Card
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
                        text = "Number of People",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Custom amounts toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Custom amounts",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = useCustomAmounts,
                            onCheckedChange = { useCustomAmounts = it }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // People counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            val current = numberOfPeople.toIntOrNull() ?: 2
                            if (current > 1) numberOfPeople = (current - 1).toString()
                        },
                        enabled = (numberOfPeople.toIntOrNull() ?: 2) > 1
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    
                    OutlinedTextField(
                        value = numberOfPeople,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d{0,3}$")) && (it.isEmpty() || it.toIntOrNull()?.let { n -> n in 1..50 } == true)) {
                                numberOfPeople = it.ifEmpty { "1" }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    
                    IconButton(
                        onClick = { 
                            val current = numberOfPeople.toIntOrNull() ?: 2
                            if (current < 50) numberOfPeople = (current + 1).toString()
                        },
                        enabled = (numberOfPeople.toIntOrNull() ?: 2) < 50
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }
        
        // Custom Amounts Section
        if (useCustomAmounts) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter each person's share of the bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val numPeople = numberOfPeople.toIntOrNull() ?: 2
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(numPeople) { index ->
                            OutlinedTextField(
                                value = customAmounts[index] ?: "",
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                        customAmounts = customAmounts.toMutableMap().apply {
                                            this[index] = newValue
                                        }
                                    }
                                },
                                label = { Text("Person ${index + 1}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                trailingIcon = { Text("${index + 1}") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    
                    // Auto-fill equal button
                    OutlinedButton(
                        onClick = {
                            val bill = totalBill.toDoubleOrNull() ?: return@OutlinedButton
                            val num = numberOfPeople.toIntOrNull() ?: return@OutlinedButton
                            val perPerson = bill / num
                            val formatter = DecimalFormat("#,##0.00")
                            
                            customAmounts = mutableMapOf()
                            repeat(num) { i ->
                                customAmounts[i] = formatter.format(perPerson)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Equalizer, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split Equally")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        result?.let { res ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Summary Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ResultRowItem(label = "Total Bill", value = formatCurrency(res.totalBill))
                        ResultRowItem(label = "Tip (${res.tipPercentage}%)", value = formatCurrency(res.tipAmount))
                        
                        HorizontalDivider()
                        
                        ResultRowItem(
                            label = "Total with Tip",
                            value = formatCurrency(res.totalWithTip),
                            isBold = true
                        )
                        
                        if (res.people.size > 1) {
                            HorizontalDivider()
                            ResultRowItem(
                                label = "Each person pays",
                                value = formatCurrency(res.perPersonAverage),
                                valueColor = MaterialTheme.colorScheme.primary,
                                isBold = true
                            )
                        }
                    }
                }
                
                // Individual Breakdown
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
                                text = "Individual Shares",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            TextButton(onClick = { copyAllShares(context, res.people) }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy All")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        res.people.forEachIndexed { index, person ->
                            PersonShareRow(
                                personNumber = index + 1,
                                name = person.name,
                                billShare = person.billShare,
                                tipShare = person.tipShare,
                                totalShare = person.totalShare,
                                isCustom = useCustomAmounts
                            )
                            
                            if (index < res.people.lastIndex) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
                
                // Verification card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (res.isBalanced) 
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (res.isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (res.isBalanced) 
                                MaterialTheme.colorScheme.secondary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (res.isBalanced) "Balanced ✓" else "Unbalanced ⚠",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (res.isBalanced) 
                                    MaterialTheme.colorScheme.onSecondaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (res.isBalanced) 
                                    "All shares add up correctly"
                                else 
                                    "Difference: ${formatCurrency(res.difference)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (res.isBalanced) 
                                    MaterialTheme.colorScheme.onSecondaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
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
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter the total bill to split",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonShareRow(
    personNumber: Int,
    name: String,
    billShare: Double,
    tipShare: Double,
    totalShare: Double,
    isCustom: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Person icon/number
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$personNumber",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            if (isCustom) {
                Text(
                    text = "Bill: ${formatCurrency(billShare)} | Tip: ${formatCurrency(tipShare)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = formatCurrency(totalShare),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResultRowItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
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
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
            ),
            color = valueColor
        )
    }
}

// Data classes
data class SplitResult(
    val totalBill: Double,
    val tipAmount: Double,
    val totalWithTip: Double,
    val tipPercentage: String,
    val people: List<PersonShare>,
    val perPersonAverage: Double,
    val isBalanced: Boolean,
    val difference: Double
)

data class PersonShare(
    val name: String,
    val billShare: Double,
    val tipShare: Double,
    val totalShare: Double
)

private fun calculateSplit(
    totalBill: String,
    tipPercent: String,
    peopleCount: String,
    useCustomAmounts: Boolean,
    customAmounts: Map<Int, String>
): SplitResult? {
    return try {
        val bill = totalBill.toDoubleOrNull() ?: return null
        val tipPct = tipPercent.toDoubleOrNull() ?: return null
        val numPeople = peopleCount.toIntOrNull() ?: return null
        
        if (bill <= 0 || tipPct < 0 || numPeople < 1) return null
        
        val tipAmount = bill * (tipPct / 100)
        val totalWithTip = bill + tipAmount
        
        val people = if (useCustomAmounts && customAmounts.isNotEmpty()) {
            // Calculate based on custom amounts
            val totalCustom = customAmounts.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
            
            if (totalCustom <= 0) {
                // Fall back to equal split
                createEqualSplit(bill, tipAmount, numPeople)
            } else {
                // Proportional split based on custom amounts
                customAmounts.mapNotNull { (index, amountStr) ->
                    val amount = amountStr.toDoubleOrNull() ?: return@mapNotNull null
                    val ratio = amount / totalCustom
                    PersonShare(
                        name = "Person ${index + 1}",
                        billShare = bill * ratio,
                        tipShare = tipAmount * ratio,
                        totalShare = totalWithTip * ratio
                    )
                }.sortedBy { it.name.extractNumber() }
            }
        } else {
            createEqualSplit(bill, tipAmount, numPeople)
        }
        
        val totalOfShares = people.sumOf { it.totalShare }
        val difference = kotlin.math.abs(totalWithTip - totalOfShares)
        val perPersonAvg = totalWithTip / numPeople
        
        SplitResult(
            totalBill = bill,
            tipAmount = tipAmount,
            totalWithTip = totalWithTip,
            tipPercentage = DecimalFormat("#,##0.#").format(tipPct),
            people = people,
            perPersonAverage = perPersonAvg,
            isBalanced = difference < 0.02, // Allow small rounding differences
            difference = difference
        )
    } catch (e: Exception) {
        null
    }
}

private fun createEqualSplit(bill: Double, tipAmount: Double, numPeople: Int): List<PersonShare> {
    val billPerPerson = bill / numPeople
    val tipPerPerson = tipAmount / numPeople
    
    return (0 until numPeople).map { index ->
        PersonShare(
            name = "Person ${index + 1}",
            billShare = billPerPerson,
            tipShare = tipPerPerson,
            totalShare = billPerPerson + tipPerPerson
        )
    }
}

private fun String.extractNumber(): Int {
    return Regex("\\d+").find(this)?.value?.toIntOrNull() ?: 0
}

private fun formatCurrency(amount: Double): String {
    return try {
        DecimalFormat("$#,##0.00").format(amount)
    } catch (e: Exception) {
        "$0.00"
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Split Bill Result", text)
    clipboard.setPrimaryClip(clip)
}

private fun copyAllShares(context: Context, shares: List<PersonShare>) {
    val text = shares.joinToString("\n") { "${it.name}: ${formatCurrency(it.totalShare)}" }
    copyToClipboard(context, text)
}
