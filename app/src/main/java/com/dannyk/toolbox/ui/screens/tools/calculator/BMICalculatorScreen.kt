package com.dannyk.toolbox.ui.screens.tools.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import kotlin.math.pow
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState
import kotlin.math.*

@Composable
fun BMICalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    // Unit system
    var isMetric by remember { mutableStateOf(true) }
    
    // Metric inputs (cm, kg)
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    
    // Imperial inputs (ft, in, lbs)
    var heightFt by remember { mutableStateOf("") }
    var heightIn by remember { mutableStateOf("") }
    var weightLbs by remember { mutableStateOf("") }
    
    val result = remember(heightCm, weightKg, heightFt, heightIn, weightLbs, isMetric) {
        calculateBMI(
            isMetric = isMetric,
            heightCm = heightCm,
            weightKg = weightKg,
            heightFt = heightFt,
            heightIn = heightIn,
            weightLbs = weightLbs
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "BMI Calculator",
            subtitle = "Calculate your Body Mass Index",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Unit System Toggle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(4.dp)
            ) {
                // Metric option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isMetric) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isMetric = true }
                ) {
                    Text(
                        text = "Metric (cm/kg)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isMetric) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                
                // Imperial option
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (!isMetric) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { isMetric = false }
                ) {
                    Text(
                        text = "Imperial (ft/lb)",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (!isMetric) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (!isMetric) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isMetric) {
            // Metric Inputs
            OutlinedTextField(
                value = heightCm,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        heightCm = it 
                    }
                },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) },
                trailingIcon = {
                    if (heightCm.isNotEmpty()) {
                        IconButton(onClick = { heightCm = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Range: 50 - 300 cm") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = weightKg,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        weightKg = it 
                    }
                },
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                trailingIcon = {
                    if (weightKg.isNotEmpty()) {
                        IconButton(onClick = { weightKg = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Range: 10 - 500 kg") }
            )
        } else {
            // Imperial Inputs - Height in feet and inches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = heightFt,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*$"))) {
                            heightFt = it 
                        }
                    },
                    label = { Text("Feet") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    supportingText = { Text("1-8 ft") }
                )
                
                OutlinedTextField(
                    value = heightIn,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*$")) && (it.isEmpty() || it.toIntOrNull()?.let { i -> i in 0..11 } == true)) {
                            heightIn = it 
                        }
                    },
                    label = { Text("Inches") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    supportingText = { Text("0-11 in") }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = weightLbs,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        weightLbs = it 
                    }
                },
                label = { Text("Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                trailingIcon = {
                    if (weightLbs.isNotEmpty()) {
                        IconButton(onClick = { weightLbs = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Range: 22 - 1100 lbs") }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        result?.let { bmiResult ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Main BMI Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = bmiResult.categoryColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your BMI",
                            style = MaterialTheme.typography.labelLarge,
                            color = bmiResult.categoryColor,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = String.format("%.1f", bmiResult.bmi),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = bmiResult.categoryColor
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = bmiResult.categoryColor,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = bmiResult.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextButton(onClick = { copyToClipboard(context, String.format("%.1f", bmiResult.bmi)) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy BMI")
                        }
                    }
                }
                
                // BMI Scale Visual
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BMI Categories",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // BMI scale bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        ) {
                            // Background gradient bar
                            Row(modifier = Modifier.fillMaxSize()) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = Color(0xFF34A853) // Green - Underweight
                                ) {}
                                Surface(
                                    modifier = Modifier.weight(3f),
                                    color = Color(0xFF4285F4) // Blue - Normal
                                ) {}
                                Surface(
                                    modifier = Modifier.weight(2f),
                                    color = Color(0xFFFBBC05) // Yellow - Overweight
                                ) {}
                                Surface(
                                    modifier = Modifier.weight(2f),
                                    color = Color(0xFFEA4335) // Red - Obese
                                ) {}
                            }
                            
                            // Indicator
                            val indicatorPosition = when {
                                bmiResult.bmi < 15 -> 0f
                                bmiResult.bmi > 40 -> 1f
                                else -> ((bmiResult.bmi - 15) / 25).toFloat().coerceIn(0f, 1f)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(indicatorPosition)
                                    .align(Alignment.CenterStart)
                                    .height(40.dp)
                                    .offset(x = (-20).dp), // Center the indicator
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                ) {}
                            }
                        }
                        
                        // Labels
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("15", style = MaterialTheme.typography.labelSmall)
                            Text("18.5", style = MaterialTheme.typography.labelSmall)
                            Text("25", style = MaterialTheme.typography.labelSmall)
                            Text("30", style = MaterialTheme.typography.labelSmall)
                            Text("40", style = MaterialTheme.typography.labelSmall)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Category list with indicators
                        CategoryItem(
                            category = "Underweight",
                            range = "< 18.5",
                            color = Color(0xFF34A853),
                            isSelected = bmiResult.category == "Underweight"
                        )
                        CategoryItem(
                            category = "Normal Weight",
                            range = "18.5 - 24.9",
                            color = Color(0xFF4285F4),
                            isSelected = bmiResult.category == "Normal"
                        )
                        CategoryItem(
                            category = "Overweight",
                            range = "25.0 - 29.9",
                            color = Color(0xFFFBBC05),
                            isSelected = bmiResult.category == "Overweight"
                        )
                        CategoryItem(
                            category = "Obese",
                            range = "≥ 30.0",
                            color = Color(0xFFEA4335),
                            isSelected = bmiResult.category == "Obese"
                        )
                    }
                }
                
                // Healthy Weight Range
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Healthy Weight Range",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isMetric) {
                            InfoRow("For your height (${heightCm} cm):", "")
                            InfoRow("Minimum healthy weight", "${bmiResult.minHealthyWeightKg} kg")
                            InfoRow("Maximum healthy weight", "${bmiResult.maxHealthyWeightKg} kg")
                        } else {
                            val totalHeightInches = (heightFt.toIntOrNull() ?: 0) * 12 + (heightIn.toIntOrNull() ?: 0)
                            InfoRow("For your height ($totalHeightInches in):", "")
                            InfoRow("Minimum healthy weight", "${bmiResult.minHealthyWeightLbs} lbs")
                            InfoRow("Maximum healthy weight", "${bmiResult.maxHealthyWeightLbs} lbs")
                        }
                    }
                }
                
                // Additional Information
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About BMI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Body Mass Index (BMI) is a person's weight in kilograms divided by the square of height in meters. While BMI can be a useful screening tool, it doesn't diagnose body fatness or health. Factors like muscle mass, age, sex, and ethnicity can affect the interpretation of your BMI.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Note: For athletes or individuals with high muscle mass, BMI may overestimate body fat. Consult a healthcare provider for personalized health advice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Ponderal Index (optional additional metric)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Ponderal Index",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format("%.2f", bmiResult.ponderalIndex),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "kg/m³ (more accurate for very tall/short people)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    modifier = Modifier.padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.MonitorWeight,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter your height and weight to calculate BMI",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryItem(
    category: String,
    range: String,
    color: Color,
    isSelected: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = color,
            modifier = Modifier.size(16.dp)
        ) {
            if (isSelected) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = category,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = range,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// Data class for results
data class BMIResult(
    val bmi: Double,
    val category: String,
    val categoryColor: Color,
    val minHealthyWeightKg: Double,
    val maxHealthyWeightKg: Double,
    val minHealthyWeightLbs: Double,
    val maxHealthyWeightLbs: Double,
    val ponderalIndex: Double
)

private fun calculateBMI(
    isMetric: Boolean,
    heightCm: String,
    weightKg: String,
    heightFt: String,
    heightIn: String,
    weightLbs: String
): BMIResult? {
    return try {
        val heightMeters: Double
        val weightKgVal: Double
        
        if (isMetric) {
            val hCm = heightCm.toDoubleOrNull() ?: return null
            val wKg = weightKg.toDoubleOrNull() ?: return null
            
            // Validate ranges
            if (hCm < 50 || hCm > 300) return null
            if (wKg < 10 || wKg > 500) return null
            
            heightMeters = hCm / 100
            weightKgVal = wKg
        } else {
            val ft = heightFt.toIntOrNull() ?: return null
            val inches = heightIn.toIntOrNull() ?: 0
            val lbs = weightLbs.toDoubleOrNull() ?: return null
            
            // Validate ranges
            if (ft < 1 || ft > 8) return null
            if (inches < 0 || inches > 11) return null
            if (lbs < 22 || lbs > 1100) return null
            
            val totalInches = ft * 12 + inches
            heightMeters = totalInches * 0.0254
            weightKgVal = lbs * 0.453592
        }
        
        if (heightMeters <= 0 || weightKgVal <= 0) return null
        
        // Calculate BMI: kg/m²
        val bmi = weightKgVal / (heightMeters.pow(2))
        
        // Determine category
        val (category, categoryColor) = when {
            bmi < 18.5 -> "Underweight" to Color(0xFF34A853)
            bmi < 25 -> "Normal" to Color(0xFF4285F4)
            bmi < 30 -> "Overweight" to Color(0xFFFBBC05)
            else -> "Obese" to Color(0xFFEA4335)
        }
        
        // Calculate healthy weight range (BMI 18.5-24.9)
        val minHealthyWeightKg = 18.5 * heightMeters.pow(2)
        val maxHealthyWeightKg = 24.9 * heightMeters.pow(2)
        val minHealthyWeightLbs = minHealthyWeightKg * 2.20462
        val maxHealthyWeightLbs = maxHealthyWeightKg * 2.20462
        
        // Calculate Ponderal Index (kg/m³)
        val ponderalIndex = weightKgVal / (heightMeters.pow(3))
        
        BMIResult(
            bmi = bmi,
            category = category,
            categoryColor = categoryColor,
            minHealthyWeightKg = minHealthyWeightKg.roundToInt().toDouble(),
            maxHealthyWeightKg = maxHealthyWeightKg.roundToInt().toDouble(),
            minHealthyWeightLbs = minHealthyWeightLbs.roundToInt().toDouble(),
            maxHealthyWeightLbs = maxHealthyWeightLbs.roundToInt().toDouble(),
            ponderalIndex = ponderalIndex
        )
    } catch (e: Exception) {
        null
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("BMI Result", text)
    clipboard.setPrimaryClip(clip)
}
