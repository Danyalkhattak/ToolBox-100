package com.dannyk.toolbox.ui.screens.tools.image

import android.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import kotlin.math.roundToInt

// Material Design color palette data
private val materialColors = mapOf(
    "Red" to listOf(0xFFCDD2, 0xE57373, 0xF44336, 0xD32F2F, 0xB71C1C),
    "Pink" to listOf(0xF8BBD0, 0xF06292, 0xE91E63, 0xC2185B, 0x880E4F),
    "Purple" to listOf(0xE1BEE7, 0xBA68C8, 0x9C27B0, 0x7B1FA2, 0x4A148C),
    "Deep Purple" to listOf(0xD1C4E9, 0x9575CD, 0x673AB7, 0x512DA8, 0x311B92),
    "Indigo" to listOf(0xC5CAE9, 0x7986CB, 0x3F51B5, 0x303F9F, 0x1A237E),
    "Blue" to listOf(0xBBDEFB, 0x64B5F6, 0x2196F3, 0x1976D2, 0x0D47A1),
    "Light Blue" to listOf(0xB3E5FC, 0x4FC3F7, 0x03A9F4, 0x0288D1, 0x01579B),
    "Cyan" to listOf(0xB2EBF2, 0x4DD0E1, 0x00BCD4, 0x0097A7, 0x006064),
    "Teal" to listOf(0xB2DFDB, 0x4DB6AC, 0x009688, 0x00796B, 0x004D40),
    "Green" to listOf(0xC8E6C9, 0x81C784, 0x4CAF50, 0x388E3C, 0x1B5E20),
    "Light Green" to listOf(0xDCEDC8, 0xAED581, 0x8BC34A, 0x689F38, 0x33691E),
    "Lime" to listOf(0xF0F4C3, 0xDCE775, 0xCDDC39, 0xAFB42B, 0x827717),
    "Yellow" to listOf(0xFFF9C4, 0xFFF176, 0xFFEB3B, 0xFBC02D, 0xF57F17),
    "Amber" to listOf(0xFFECB3, 0xFFD740, 0xFFC107, 0xFFA000, 0xFF6F00),
    "Orange" to listOf(0xFFE0B2, 0xFFB74D, 0xFF9800, 0xF57C00, 0xE65100),
    "Deep Orange" to listOf(0xFFCCBC, 0xFF8A65, 0xFF5722, 0xE64A19, 0xBF360C),
    "Brown" to listOf(0xD7CCC8, 0xA1887F, 0x795548, 0x5D4037, 0x3E2723),
    "Grey" to listOf(0xF5F5F5, 0x9E9E9E, 0x757575, 0x616161, 0x212121),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerScreen(navController: NavHostController) {
    var selectedColor by remember { mutableIntStateOf(Color.RED) }
    var redValue by remember { mutableIntStateOf(Color.red(selectedColor)) }
    var greenValue by remember { mutableIntStateOf(Color.green(selectedColor)) }
    var blueValue by remember { mutableIntStateOf(Color.blue(selectedColor)) }
    var hexInput by remember { mutableStateOf("#${Integer.toHexString(selectedColor).substring(2).uppercase()}") }
    var recentColors by remember { mutableStateOf(mutableListOf<Int>()) }
    
    val clipboardManager = LocalClipboardManager.current
    
    // Update color when RGB changes
    LaunchedEffect(redValue, greenValue, blueValue) {
        selectedColor = Color.rgb(redValue.coerceIn(0, 255), 
                                   greenValue.coerceIn(0, 255), 
                                   blueValue.coerceIn(0, 255))
        hexInput = "#${Integer.toHexString(selectedColor).substring(2).uppercase()}"
    }

    Scaffold(
        topBar = { ToolTopBar("Color Picker", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Large Color Preview
            Text(
                text = "Selected Color",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Main color preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ComposeColor(selectedColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show contrasting text
                        val luminance = (0.299 * redValue + 0.587 * greenValue + 0.114 * blueValue) / 255
                        Text(
                            text = hexInput,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (luminance > 0.5) ComposeColor.Black else ComposeColor.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Color values row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ColorValueChip("HEX", hexInput) {
                            clipboardManager.setText(AnnotatedString(hexInput))
                        }
                        ColorValueChip("RGB", "($redValue, $greenValue, $blueValue)") {
                            clipboardManager.setText(AnnotatedString("($redValue, $greenValue, $blueValue)"))
                        }
                        
                        // Calculate and show HSL
                        val hsl = rgbToHsl(redValue, greenValue, blueValue)
                        ColorValueChip("HSL", String.format("(%d°, %d%%, %d%%)", 
                            hsl[0], hsl[1], hsl[2])) {
                            clipboardManager.setText(AnnotatedString(String.format(
                                "(%d, %d%%, %d%%)", hsl[0], hsl[1], hsl[2])))
                        }
                    }
                    
                    // Copy all formats button
                    OutlinedButton(
                        onClick = {
                            val hsl = rgbToHsl(redValue, greenValue, blueValue)
                            val allFormats = """
                                |HEX: $hexInput
                                |RGB: ($redValue, $greenValue, $blueValue)
                                |HSL: (${hsl[0]}°, ${hsl[1]}%, ${hsl[2]}%)
                                |Int: $selectedColor
                            """.trimMargin()
                            clipboardManager.setText(AnnotatedString(allFormats))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy All Formats")
                    }
                }
            }

            // RGB Sliders
            Text(
                text = "RGB Values",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Red slider
                    ColorSlider(
                        label = "Red (R)",
                        value = redValue,
                        onValueChange = { redValue = it },
                        color = ComposeColor.Red
                    )
                    
                    // Green slider
                    ColorSlider(
                        label = "Green (G)",
                        value = greenValue,
                        onValueChange = { greenValue = it },
                        color = ComposeColor.Green
                    )
                    
                    // Blue slider
                    ColorSlider(
                        label = "Blue (B)",
                        value = blueValue,
                        onValueChange = { blueValue = it },
                        color = ComposeColor.Blue
                    )
                }
            }

            // HEX Input
            Text(
                text = "HEX Code",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            OutlinedTextField(
                value = hexInput,
                onValueChange = { newValue ->
                    val cleanHex = if (newValue.startsWith("#")) newValue else "#$newValue"
                    if (cleanHex.matches(Regex("^#?([A-Fa-f0-9]{6})$"))) {
                        hexInput = cleanHex.uppercase()
                        try {
                            val colorInt = android.graphics.Color.parseColor(cleanHex)
                            redValue = Color.red(colorInt)
                            greenValue = Color.green(colorInt)
                            blueValue = Color.blue(colorInt)
                            selectedColor = colorInt
                        } catch (_: Exception) {}
                    } else if (newValue.isEmpty() || newValue == "#") {
                        hexInput = newValue.uppercase()
                    }
                },
                label = { Text("#RRGGBB") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                prefix = { Text("#", fontFamily = FontFamily.Monospace) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Material Color Palette
            Text(
                text = "Material Colors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    materialColors.forEach { (name, colors) ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            colors.forEach { colorInt ->
                                Surface(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clickable {
                                            redValue = Color.red(colorInt)
                                            greenValue = Color.green(colorInt)
                                            blueValue = Color.blue(colorInt)
                                            addToRecent(recentColors, colorInt)
                                        },
                                    shape = CircleShape,
                                    color = ComposeColor(colorInt or 0xFF000000.toInt())
                                ) {}
                            }
                        }
                    }
                }
            }

            // Recent Colors
            if (recentColors.isNotEmpty()) {
                Text(
                    text = "Recent Colors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentColors.reversed().take(20).forEach { colorInt ->
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        redValue = Color.red(colorInt)
                                        greenValue = Color.green(colorInt)
                                        blueValue = Color.blue(colorInt)
                                    },
                                shape = CircleShape,
                                color = ComposeColor(colorInt or 0xFF000000.toInt()),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, 
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {}
                        }
                    }
                }
            }
            
            // Add current color button
            Button(
                onClick = { addToRecent(recentColors, selectedColor) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Current Color to Recent")
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    color: ComposeColor
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("$value", style = MaterialTheme.typography.bodySmall, 
                 fontFamily = FontFamily.Monospace)
        }
        
        Box(modifier = Modifier.height(32.dp)) {
            // Background gradient
            Canvas(modifier = Modifier.matchParentSize()) {
                val gradient = Brush.horizontalGradient(
                    colors = listOf(ComposeColor.Black, color, ComposeColor.White)
                )
                drawRect(gradient)
            }
            
            // Slider overlay
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 255)) },
                valueRange = 0f..255f,
                modifier = Modifier.matchParentSize(),
                colors = SliderDefaults.colors(
                    thumbColor = ComposeColor.Transparent,
                    activeTrackColor = ComposeColor.Transparent,
                    inactiveTrackColor = ComposeColor.Transparent
                )
            )
        }
    }
}

@Composable
private fun ColorValueChip(label: String, value: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall,
                 fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

private fun addToRecent(recentColors: MutableList<Int>, color: Int) {
    if (!recentColors.contains(color)) {
        recentColors.add(color)
        // Keep only last 30 colors
        if (recentColors.size > 30) {
            recentColors.removeAt(0)
        }
    }
}

private fun rgbToHsl(r: Int, g: Int, b: Int): IntArray {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f
    
    val max = maxOf(rf, gf, bf)
    val min = minOf(rf, gf, bf)
    val delta = max - min
    
    var h = 0f
    val s: Float
    val l = (max + min) / 2f
    
    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = if (l > 0.5f) delta / (2f - max - min) else delta / (max + min)
        
        when (max) {
            rf -> h = ((gf - bf) / delta + if (gf < bf) 6f else 0f) * 60f
            gf -> h = ((bf - rf) / delta + 2f) * 60f
            bf -> h = ((rf - gf) / delta + 4f) * 60f
        }
    }
    
    return intArrayOf(h.roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt())
}
