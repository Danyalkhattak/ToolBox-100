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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import java.text.DecimalFormat
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip

@Composable
fun DiscountCalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var originalPrice by remember { mutableStateOf("") }
    var discountPercent by remember { mutableStateOf("") }
    var additionalDiscount by remember { mutableStateOf("") }
    var hasAdditionalDiscount by remember { mutableStateOf(false) }
    var taxPercent by remember { mutableStateOf("") }
    var includeTax by remember { mutableStateOf(false) }
    
    val calculationResult = remember(originalPrice, discountPercent, additionalDiscount, hasAdditionalDiscount, taxPercent, includeTax) {
        calculateDiscount(
            originalPrice = originalPrice,
            discountPercent = discountPercent,
            additionalDiscount = if (hasAdditionalDiscount) additionalDiscount else null,
            taxPercent = if (includeTax) taxPercent else null
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Discount Calculator",
            subtitle = "Calculate discounted prices and savings",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Original Price Input
        OutlinedTextField(
            value = originalPrice,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    originalPrice = it 
                }
            },
            label = { Text("Original Price") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
            trailingIcon = {
                if (originalPrice.isNotEmpty()) {
                    IconButton(onClick = { originalPrice = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Discount Percentage Input
        OutlinedTextField(
            value = discountPercent,
            onValueChange = { 
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    discountPercent = it 
                }
            },
            label = { Text("Discount (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
            trailingIcon = {
                if (discountPercent.isNotEmpty()) {
                    IconButton(onClick = { discountPercent = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Quick discount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("5%", "10%", "15%", "20%", "25%", "50%").forEach { pct ->
                FilterChip(
                    selected = discountPercent == pct.dropLast(1),
                    onClick = { discountPercent = pct.dropLast(1) },
                    label = { Text(pct) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Additional Discount Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Additional Discount",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = hasAdditionalDiscount,
                onCheckedChange = { hasAdditionalDiscount = it }
            )
        }
        
        if (hasAdditionalDiscount) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = additionalDiscount,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        additionalDiscount = it 
                    }
                },
                label = { Text("Additional Discount (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tax Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Tax",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = includeTax,
                onCheckedChange = { includeTax = it }
            )
        }
        
        if (includeTax) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = taxPercent,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        taxPercent = it 
                    }
                },
                label = { Text("Tax Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Quick tax buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("5%", "7%", "8.5%", "10%", "12%", "15%").forEach { tax ->
                    FilterChip(
                        selected = taxPercent == tax.dropLast(1),
                        onClick = { taxPercent = tax.dropLast(1) },
                        label = { Text(tax) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        calculationResult?.let { result ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header with copy button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Final Price",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(onClick = { copyToClipboard(context, result.finalPrice) }) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy price",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatCurrency(result.finalPrice),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (result.originalPrice.isNotEmpty()) {
                        Text(
                            text = "was ${formatCurrency(result.originalPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Savings and breakdown
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
                    // Total Savings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Total Savings",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = formatCurrency(result.totalSavings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth())
                    
                    // Breakdown items
                    ResultRow(label = "Original Price", value = formatCurrency(result.originalPrice))
                    ResultRow(
                        label = "First Discount (${result.discountPercentage}%)", 
                        value = "-${formatCurrency(result.firstDiscount)}",
                        valueColor = MaterialTheme.colorScheme.error
                    )
                    
                    if (result.additionalDiscountAmount > 0) {
                        ResultRow(
                            label = "Additional Discount (${result.additionalDiscountPercentage}%)",
                            value = "-${formatCurrency(result.additionalDiscountAmount)}",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    if (result.taxAmount > 0) {
                        ResultRow(
                            label = "Tax (${result.taxRate}%)",
                            value = "+${formatCurrency(result.taxAmount)}",
                            valueColor = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth())
                    
                    ResultRow(
                        label = "You Save",
                        value = formatCurrency(result.totalSavings),
                        valueStyle = MaterialTheme.typography.titleMedium,
                        valueColor = MaterialTheme.colorScheme.error,
                        isBold = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Savings percentage indicator
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
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "You're saving",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${result.effectiveDiscount}% off original price",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } ?: run {
            // Empty state or placeholder
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
                        Icons.Default.LocalOffer,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter price and discount to calculate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = valueStyle.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal),
            color = valueColor,
            textAlign = TextAlign.End
        )
    }
}

// Data class for results
data class DiscountResult(
    val originalPrice: String,
    val finalPrice: String,
    val totalSavings: String,
    val discountPercentage: String,
    val firstDiscount: String,
    val additionalDiscountPercentage: String = "",
    val additionalDiscountAmount: Double = 0.0,
    val taxRate: String = "",
    val taxAmount: Double = 0.0,
    val effectiveDiscount: String
)

private fun calculateDiscount(
    originalPrice: String,
    discountPercent: String,
    additionalDiscount: String? = null,
    taxPercent: String? = null
): DiscountResult? {
    return try {
        val price = originalPrice.toDoubleOrNull() ?: return null
        val discount = discountPercent.toDoubleOrNull() ?: return null
        
        if (price < 0 || discount < 0 || discount > 100) return null
        
        var currentPrice = price
        
        // Apply first discount
        val firstDiscountAmount = currentPrice * (discount / 100)
        currentPrice -= firstDiscountAmount
        
        // Apply additional discount if provided
        var addDiscountPct = ""
        var addDiscountAmt = 0.0
        if (!additionalDiscount.isNullOrEmpty()) {
            val addDiscount = additionalDiscount.toDoubleOrNull()
            if (addDiscount != null && addDiscount >= 0 && addDiscount <= 100) {
                addDiscountPct = additionalDiscount
                addDiscountAmt = currentPrice * (addDiscount / 100)
                currentPrice -= addDiscountAmt
            }
        }
        
        // Calculate final savings before tax
        val totalSavingsBeforeTax = price - currentPrice
        
        // Apply tax if provided
        var taxPct = ""
        var taxAmt = 0.0
        if (!taxPercent.isNullOrEmpty()) {
            val tax = taxPercent.toDoubleOrNull()
            if (tax != null && tax >= 0) {
                taxPct = taxPercent
                taxAmt = currentPrice * (tax / 100)
                currentPrice += taxAmt
            }
        }
        
        val formatter = DecimalFormat("#,##0.00")
        val effectiveDiscount = DecimalFormat("#,##0.#").format((totalSavingsBeforeTax / price) * 100)
        
        DiscountResult(
            originalPrice = formatter.format(price),
            finalPrice = formatter.format(currentPrice),
            totalSavings = formatter.format(totalSavingsBeforeTax),
            discountPercentage = DecimalFormat("#,##0.#").format(discount),
            firstDiscount = formatter.format(firstDiscountAmount),
            additionalDiscountPercentage = addDiscountPct,
            additionalDiscountAmount = addDiscountAmt,
            taxRate = taxPct,
            taxAmount = taxAmt,
            effectiveDiscount = effectiveDiscount
        )
    } catch (e: Exception) {
        null
    }
}

private fun formatCurrency(amount: String): String {
    return try {
        val num = amount.toDoubleOrNull() ?: return "$0.00"
        val formatter = DecimalFormat("$#,##0.00")
        formatter.format(num)
    } catch (e: Exception) {
        "$0.00"
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Discount Result", text)
    clipboard.setPrimaryClip(clip)
}
