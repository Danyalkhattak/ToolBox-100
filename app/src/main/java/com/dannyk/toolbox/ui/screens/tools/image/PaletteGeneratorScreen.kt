package com.dannyk.toolbox.ui.screens.tools.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import android.content.ClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.ScrollState
import kotlin.math.*
import androidx.compose.runtime.LaunchedEffect
import android.graphics.Color

enum class PaletteMode(val displayName: String) {
    COMPLEMENTARY("Complementary"),
    ANALOGOUS("Analogous"),
    TRIADIC("Triadic"),
    SPLIT_COMPLEMENTARY("Split-Complementary"),
    MONOCHROMATIC("Monochromatic")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PaletteGeneratorScreen(navController: NavHostController) {
    val clipboardManager = LocalClipboardManager.current
    
    var baseColor by remember { mutableIntStateOf(Color.parseColor("#4CAF50")) }
    var baseRed by remember { mutableIntStateOf(Color.red(baseColor)) }
    var baseGreen by remember { mutableIntStateOf(Color.green(baseColor)) }
    var baseBlue by remember { mutableIntStateOf(Color.blue(baseColor)) }
    
    var selectedMode by remember { mutableStateOf(PaletteMode.COMPLEMENTARY) }
    var generatedPalette by remember { mutableStateOf(generatePalette(baseColor, selectedMode)) }

    // Update palette when color or mode changes
    LaunchedEffect(baseColor, selectedMode) {
        generatedPalette = generatePalette(baseColor, selectedMode)
    }

    Scaffold(
        topBar = { ToolTopBar("Palette Generator", onBackClick = { navController.navigateUp() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==================== BASE COLOR PICKER ====================
            Text(
                text = "Base Color",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Base color preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ComposeColor(baseColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        val luminance = (0.299 * baseRed + 0.587 * baseGreen + 0.114 * baseBlue) / 255
                        Text(
                            text = "#${String.format("%02X%02X%02X", baseRed, baseGreen, baseBlue)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (luminance > 0.5) ComposeColor.Black else ComposeColor.White
                        )
                    }

                    // Quick color presets
                    Text("Quick Presets:", style = MaterialTheme.typography.labelLarge)
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "#F44336" to "Red",
                            "#E91E63" to "Pink",
                            "#9C27B0" to "Purple",
                            "#673AB7" to "Deep Purple",
                            "#3F51B5" to "Indigo",
                            "#2196F3" to "Blue",
                            "#00BCD4" to "Cyan",
                            "#009688" to "Teal",
                            "#4CAF50" to "Green",
                            "#8BC34A" to "Light Green",
                            "#CDDC39" to "Lime",
                            "#FFEB3B" to "Yellow",
                            "#FFC107" to "Amber",
                            "#FF9800" to "Orange",
                            "#FF5722" to "Deep Orange",
                            "#795548" to "Brown",
                            "#607D8B" to "Blue Grey",
                        ).forEach { (hex, name) ->
                            Surface(
                                onClick = {
                                    baseColor = Color.parseColor(hex)
                                    baseRed = Color.red(baseColor)
                                    baseGreen = Color.green(baseColor)
                                    baseBlue = Color.blue(baseColor)
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(ComposeColor(Color.parseColor(hex)))
                                )
                            }
                        }
                    }

                    // Custom RGB input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RgbInputCompact("R", baseRed) { 
                            baseColor = Color.rgb(it.coerceIn(0, 255), baseGreen, baseBlue)
                            baseRed = it.coerceIn(0, 255)
                        }
                        RgbInputCompact("G", baseGreen) { 
                            baseColor = Color.rgb(baseRed, it.coerceIn(0, 255), baseBlue)
                            baseGreen = it.coerceIn(0, 255)
                        }
                        RgbInputCompact("B", baseBlue) { 
                            baseColor = Color.rgb(baseRed, baseGreen, it.coerceIn(0, 255))
                            baseBlue = it.coerceIn(0, 255)
                        }
                    }
                }
            }

            // ==================== PALETTE MODE SELECTION ====================
            Text(
                text = "Generation Mode",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PaletteMode.values().forEach { mode ->
                        Surface(
                            onClick = { selectedMode = mode },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedMode == mode) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMode == mode,
                                    onClick = { selectedMode = mode }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(mode.displayName, style = MaterialTheme.typography.bodyMedium,
                                         fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Normal)
                                    Text(getModeDescription(mode), style = MaterialTheme.typography.bodySmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // ==================== GENERATED PALETTE ====================
            Text(
                text = "Generated Palette (${selectedMode.displayName})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Color swatches row
                    generatedPalette.forEachIndexed { index, colorInt ->
                        PaletteColorRow(index + 1, colorInt)
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Export options
                    Text("Export Options:", style = MaterialTheme.typography.labelLarge)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val hexList = generatedPalette.joinToString(", ") { 
                                    "\"#${String.format("%06X", it and 0xFFFFFF)}\"" 
                                }
                                clipboardManager.setText(AnnotatedString("[$hexList]"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, 
                                 modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("HEX Array")
                        }
                        
                        OutlinedButton(
                            onClick = {
                                val rgbList = generatedPalette.joinToString(", ") { 
                                    val r = Color.red(it)
                                    val g = Color.green(it)
                                    val b = Color.blue(it)
                                    "($r, $g, $b)" 
                                }
                                clipboardManager.setText(AnnotatedString("[$rgbList]"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, 
                                 modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RGB Array")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val cssList = generatedPalette.joinToString("\n") { 
                                "#${String.format("%02X%02X%02X", 
                                    Color.red(it), Color.green(it), Color.blue(it))}" 
                            }
                            clipboardManager.setText(AnnotatedString(cssList))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, 
                             modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CSS Variables Format")
                    }

                    OutlinedButton(
                        onClick = {
                            val kotlinList = generatedPalette.joinToString(",\n    ") { 
                                "0xFF${String.format("%02X%02X%02X", 
                                    Color.red(it), Color.green(it), Color.blue(it))}" 
                            }
                            clipboardManager.setText(AnnotatedString("listOf(\n    $kotlinList\n)"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, 
                             modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kotlin Color List")
                    }
                }
            }
        }
    }
}

@Composable
private fun PaletteColorRow(number: Int, color: Int) {
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    val hex = String.format("#%02X%02X%02X", r, g, b)
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { clipboardManager.setText(AnnotatedString(hex)) },
        colors = CardDefaults.cardColors(containerColor = ComposeColor.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number badge
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$number", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Color swatch
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ComposeColor(color or 0xFF000000.toInt()))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Color info
            Column(modifier = Modifier.weight(1f)) {
                Text(hex, style = MaterialTheme.typography.bodyMedium, 
                     fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                Text("RGB($r, $g, $b)", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // Copy icon
            IconButton(onClick = { clipboardManager.setText(AnnotatedString(hex)) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", 
                     modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RgbInputCompact(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var inputValue by remember { mutableStateOf(value.toString()) }
    
    OutlinedTextField(
        value = inputValue,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || (newValue.toIntOrNull() != null && newValue.toInt() in 0..255)) {
                inputValue = newValue
                if (newValue.isNotEmpty()) {
                    onValueChange(newValue.toInt())
                }
            }
        },
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.weight(1f)
    )
}

private fun getModeDescription(mode: PaletteMode): String {
    return when (mode) {
        PaletteMode.COMPLEMENTARY -> "Colors opposite on the color wheel"
        PaletteMode.ANALOGOUS -> "Adjacent colors on the wheel"
        PaletteMode.TRIADIC -> "Three evenly spaced colors"
        PaletteMode.SPLIT_COMPLEMENTARY -> "Base + two adjacent to complement"
        PaletteMode.MONOCHROMATIC -> "Shades and tints of base color"
    }
}

private fun generatePalette(baseColor: Int, mode: PaletteMode): List<Int> {
    return when (mode) {
        PaletteMode.COMPLEMENTARY -> generateComplementary(baseColor)
        PaletteMode.ANALOGOUS -> generateAnalogous(baseColor)
        PaletteMode.TRIADIC -> generateTriadic(baseColor)
        PaletteMode.SPLIT_COMPLEMENTARY -> generateSplitComplementary(baseColor)
        PaletteMode.MONOCHROMATIC -> generateMonochromatic(baseColor)
    }
}

private fun rgbToHsl(r: Int, g: Int, b: Int): FloatArray {
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
        h = 0f; s = 0f
    } else {
        s = if (l > 0.5f) delta / (2f - max - min) else delta / (max + min)
        
        when (max) {
            rf -> h = ((gf - bf) / delta + if (gf < bf) 6f else 0f) * 60f
            gf -> h = ((bf - rf) / delta + 2f) * 60f
            bf -> h = ((rf - gf) / delta + 4f) * 60f
        }
    }
    
    return floatArrayOf(h, s, l)
}

private fun hslToRgb(h: Float, s: Float, l: Float): Int {
    val c = (1 - kotlin.math.abs(2 * l - 1)) * s
    val x = c * (1 - kotlin.math.abs((h / 60 % 2) - 1))
    val m = l - c / 2
    
    var rf = 0f; var gf = 0f; var bf = 0f
    
    when {
        h < 60 -> { rf = c; gf = x; bf = 0f }
        h < 120 -> { rf = x; gf = c; bf = 0f }
        h < 180 -> { rf = 0f; gf = c; bf = x }
        h < 240 -> { rf = 0f; gf = x; bf = c }
        h < 300 -> { rf = x; gf = 0f; bf = c }
        else -> { rf = c; gf = 0f; bf = x }
    }
    
    return Color.rgb(
        ((rf + m) * 255).roundToInt().coerceIn(0, 255),
        ((gf + m) * 255).roundToInt().coerceIn(0, 255),
        ((bf + m) * 255).roundToInt().coerceIn(0, 255)
    )
}

private fun generateComplementary(baseColor: Int): List<Int> {
    val hsl = rgbToHsl(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    val baseHue = hsl[0]
    val sat = hsl[1]
    val light = hsl[2]
    
    // Base color, complement, plus variations
    return listOf(
        baseColor,
        hslToRgb((baseHue + 180) % 360, sat, light),  // Direct complement
        hslToRgb(baseHue, sat * 0.7f, light * 0.85f),   // Desaturated base
        hslToRgb((baseHue + 180) % 360, sat * 0.7f, light * 0.85f),  // Desaturated complement
        hslToRgb((baseHue + 90) % 360, sat * 0.5f, light)  // Midpoint
    )
}

private fun generateAnalogous(baseColor: Int): List<Int> {
    val hsl = rgbToHsl(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    val baseHue = hsl[0]
    val sat = hsl[1]
    val light = hsl[2]
    
    return listOf(
        hslToRgb((baseHue - 30 + 360) % 360, sat, light),  // -30 degrees
        hslToRgb((baseHue - 15 + 360) % 360, sat, light),  // -15 degrees
        baseColor,                                           // Base
        hslToRgb((baseHue + 15) % 360, sat, light),         // +15 degrees
        hslToRgb((baseHue + 30) % 360, sat, light)          // +30 degrees
    )
}

private fun generateTriadic(baseColor: Int): List<Int> {
    val hsl = rgbToHsl(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    val baseHue = hsl[0]
    val sat = hsl[1]
    val light = hsl[2]
    
    return listOf(
        baseColor,
        hslToRgb((baseHue + 120) % 360, sat, light),  // +120 degrees
        hslToRgb((baseHue + 240) % 360, sat, light),  // +240 degrees
        hslToRgb(baseHue, sat * 0.7f, light * 0.85f),  // Desaturated base
        hslToRgb((baseHue + 120) % 360, sat * 0.7f, light * 0.85f)  // Desaturated second
    )
}

private fun generateSplitComplementary(baseColor: Int): List<Int> {
    val hsl = rgbToHsl(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    val baseHue = hsl[0]
    val sat = hsl[1]
    val light = hsl[2]
    
    return listOf(
        baseColor,
        hslToRgb((baseHue + 150) % 360, sat, light),  // +150 degrees
        hslToRgb((baseHue + 210) % 360, sat, light),  // +210 degrees
        hslToRgb(baseHue, sat * 0.8f, light * 0.9f),   // Lighter base
        hslToRgb((baseHue + 180) % 360, sat * 0.5f, light)  // Muted complement
    )
}

private fun generateMonochromatic(baseColor: Int): List<Int> {
    val hsl = rgbToHsl(Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
    val hue = hsl[0]
    val sat = hsl[1]
    val light = hsl[2]
    
    return listOf(
        hslToRgb(hue, sat, (light * 0.25f).coerceAtLeast(0.05f)),  // Very dark
        hslToRgb(hue, sat, (light * 0.55f).coerceAtLeast(0.1f)),   // Dark
        baseColor,                                                    // Base
        hslToRgb(hue, sat, (light * 1.35f).coerceAtMost(0.95f)),     // Light
        hslToRgb(hue, sat * 0.3f, 0.95f)                              // Very light tint
    )
}
