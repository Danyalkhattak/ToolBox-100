package com.dannyk.toolbox.ui.screens.tools.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import android.content.ClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.ScrollState
import kotlin.math.*
import androidx.compose.runtime.LaunchedEffect
import android.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RGBHSLConverterScreen(navController: NavHostController) {
    val clipboardManager = LocalClipboardManager.current
    
    // RGB State (input mode 1)
    var redInput by remember { mutableStateOf("128") }
    var greenInput by remember { mutableStateOf("128") }
    var blueInput by remember { mutableStateOf("128") }
    
    // HSL State (input mode 2)
    var hueInput by remember { mutableStateOf("0") }
    var saturationInput by remember { mutableStateOf("50") }
    var lightnessInput by remember { mutableStateOf("50") }
    
    // Output values
    var outputHue by remember { mutableIntStateOf(0) }
    var outputSaturation by remember { mutableIntStateOf(0) }
    var outputLightness by remember { mutableIntStateOf(50) }
    
    var outputRed by remember { mutableIntStateOf(128) }
    var outputGreen by remember { mutableIntStateOf(128) }
    var outputBlue by remember { mutableIntStateOf(128) }
    
    var currentColor by remember { mutableIntStateOf(Color.GRAY) }
    var rgbError by remember { mutableStateOf<String?>(null) }
    var hslError by remember { mutableStateOf<String?>(null) }

    // Convert RGB -> HSL when RGB changes
    LaunchedEffect(redInput, greenInput, blueInput) {
        convertRgbToHsl(redInput, greenInput, blueInput) { h, s, l, r, g, b, error ->
            outputHue = h
            outputSaturation = s
            outputLightness = l
            outputRed = r
            outputGreen = g
            outputBlue = b
            if (error == null) {
                currentColor = Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
            }
            rgbError = error
        }
    }

    // Convert HSL -> RGB when HSL changes
    LaunchedEffect(hueInput, saturationInput, lightnessInput) {
        convertHslToRgb(hueInput, saturationInput, lightnessInput) { h, s, l, r, g, b, error ->
            outputHue = h
            outputSaturation = s
            outputLightness = l
            outputRed = r
            outputGreen = g
            outputBlue = b
            if (error == null) {
                currentColor = Color.rgb(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
            }
            hslError = error
        }
    }

    Scaffold(
        topBar = { ToolTopBar("RGB ↔ HSL Converter", onBackClick = { navController.navigateUp() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==================== COLOR PREVIEW ====================
            Text(
                text = "Color Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ComposeColor(currentColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        val luminance = (0.299 * outputRed + 0.587 * outputGreen + 0.114 * outputBlue) / 255
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "#${String.format("%02X%02X%02X", 
                                    outputRed.coerceIn(0, 255), 
                                    outputGreen.coerceIn(0, 255), 
                                    outputBlue.coerceIn(0, 255))}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (luminance > 0.5) ComposeColor.Black else ComposeColor.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FormatChip("HEX", String.format("#%02X%02X%02X", 
                            outputRed.coerceIn(0, 255), outputGreen.coerceIn(0, 255), outputBlue.coerceIn(0, 255)))
                        FormatChip("RGB", "($outputRed, $outputGreen, $outputBlue)")
                        FormatChip("HSL", "($outputHue°, $outputSaturation%, $outputLightness%)")
                    }
                }
            }

            // ==================== RGB INPUT SECTION ====================
            Text(
                text = "RGB Input → HSL Output",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter RGB values (0-255):",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorChannelInput(
                            label = "R",
                            value = redInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..255)) redInput = it 
                            },
                            color = ComposeColor.Red,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        ColorChannelInput(
                            label = "G",
                            value = greenInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..255)) greenInput = it 
                            },
                            color = ComposeColor.Green,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        ColorChannelInput(
                            label = "B",
                            value = blueInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..255)) blueInput = it 
                            },
                            color = ComposeColor.Blue,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // HSL Output from RGB
                    Text(
                        text = "Converted HSL Values:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HslOutputCard("H", "$outputHue°", "0-360", ComposeColor(0xFFFF5722))
                        HslOutputCard("S", "$outputSaturation%", "0-100%", ComposeColor(0xFF4CAF50))
                        HslOutputCard("L", "$outputLightness%", "0-100%", ComposeColor(0xFF2196F3))
                    }

                    if (rgbError != null) {
                        Text(rgbError!!, color = MaterialTheme.colorScheme.error, 
                             style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ==================== HSL INPUT SECTION ====================
            Text(
                text = "HSL Input → RGB Output",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter HSL values:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Hue input (0-360)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hue (H)", style = MaterialTheme.typography.labelMedium, 
                                 color = ComposeColor(0xFFFF5722))
                            Text("$hueInput° / 360°", style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Slider(
                            value = (hueInput.toFloatOrNull() ?: 0f).coerceIn(0f, 360f),
                            onValueChange = { hueInput = it.toInt().toString() },
                            valueRange = 0f..360f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = hueInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..360)) 
                                    hueInput = it 
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Saturation input (0-100)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Saturation (S)", style = MaterialTheme.typography.labelMedium, 
                                 color = ComposeColor(0xFF4CAF50))
                            Text("$saturationInput% / 100%", style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Slider(
                            value = (saturationInput.toFloatOrNull() ?: 50f).coerceIn(0f, 100f),
                            onValueChange = { saturationInput = it.toInt().toString() },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = saturationInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..100)) 
                                    saturationInput = it 
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Lightness input (0-100)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Lightness (L)", style = MaterialTheme.typography.labelMedium, 
                                 color = ComposeColor(0xFF2196F3))
                            Text("$lightnessInput% / 100%", style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Slider(
                            value = (lightnessInput.toFloatOrNull() ?: 50f).coerceIn(0f, 100f),
                            onValueChange = { lightnessInput = it.toInt().toString() },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = lightnessInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..100)) 
                                    lightnessInput = it 
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // RGB Output from HSL
                    Text(
                        text = "Converted RGB Values:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RgbOutputCard("R", "$outputRed", "/ 255", ComposeColor.Red)
                        RgbOutputCard("G", "$outputGreen", "/ 255", ComposeColor.Green)
                        RgbOutputCard("B", "$outputBlue", "/ 255", ComposeColor.Blue)
                    }

                    if (hslError != null) {
                        Text(hslError!!, color = MaterialTheme.colorScheme.error, 
                             style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // ==================== FORMULAS REFERENCE ====================
            Text(
                text = "Conversion Formulas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RGB → HSL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    FormulaText("""
                        R' = R/255, G' = G/255, B' = B/255
                        Cmax = max(R', G', B')
                        Cmin = min(R', G', B')
                        Δ = Cmax - Cmin
                        
                        L = (Cmax + Cmin) / 2
                        
                        If Δ = 0:
                          H = 0, S = 0
                        Else:
                          S = Δ / (1 - |2L - 1|)
                          
                          When Cmax = R':
                            H = 60 × ((G' - B')/Δ mod 6)
                          When Cmax = G':
                            H = 60 × ((B' - R')/Δ + 2)
                          When Cmax = B':
                            H = 60 × ((R' - G')/Δ + 4)
                    """.trimIndent())
                    
                    Divider()
                    
                    Text(
                        text = "HSL → RGB",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    FormulaText("""
                        C = (1 - |2L - 1|) × S
                        X = C × (1 - |(H/60 mod 2) - 1|)
                        m = L - C/2
                        
                        If 0 ≤ H < 60:
                          (R', G', B') = (C, X, 0)
                        If 60 ≤ H < 120:
                          (R', G', B') = (X, C, 0)
                        If 120 ≤ H < 180:
                          (R', G', B') = (0, C, X)
                        If 180 ≤ H < 240:
                          (R', G', B') = (0, X, C)
                        If 240 ≤ H < 300:
                          (R', G', B') = (X, 0, C)
                        If 300 ≤ H < 360:
                          (R', G', B') = (C, 0, X)
                          
                        R = round((R' + m) × 255)
                        G = round((G' + m) × 255)
                        B = round((B' + m) × 255)
                    """.trimIndent())
                }
            }

            // Copy All Button
            Button(
                onClick = {
                    val hex = String.format("#%02X%02X%02X", 
                        outputRed.coerceIn(0, 255), outputGreen.coerceIn(0, 255), outputBlue.coerceIn(0, 255))
                    val text = """
                        |HEX: $hex
                        |RGB: ($outputRed, $outputGreen, $outputBlue)
                        |HSL: ($outputHue°, $outputSaturation%, $outputLightness%)
                    """.trimMargin()
                    clipboardManager.setText(AnnotatedString(text))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy All Values")
            }
        }
    }
}

@Composable
private fun FormatChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall,
                 fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ColorChannelInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: ComposeColor,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, 
             color = color, fontWeight = FontWeight.Medium)
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("0-255") },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            ),
            modifier = modifier,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun HslOutputCard(label: String, value: String, range: String, color: ComposeColor) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelSmall, 
                         fontWeight = FontWeight.Bold, color = ComposeColor.White)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, 
                 fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(range, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RgbOutputCard(label: String, value: String, range: String, color: ComposeColor) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelSmall, 
                         fontWeight = FontWeight.Bold, color = ComposeColor.White)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, 
                 fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(range, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FormulaText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 14.sp
    )
}

// Conversion functions
private fun convertRgbToHsl(
    rStr: String, gStr: String, bStr: String,
    onResult: (Int, Int, Int, Int, Int, Int, String?) -> Unit
) {
    try {
        val r = rStr.toIntOrNull() ?: return onResult(0, 0, 50, 0, 0, 0, null)
        val g = gStr.toIntOrNull() ?: return onResult(0, 0, 50, 0, 0, 0, null)
        val b = bStr.toIntOrNull() ?: return onResult(0, 0, 50, 0, 0, 0, null)
        
        if (r !in 0..255 || g !in 0..255 || b !in 0..255) {
            return onResult(0, 0, 50, r, g, b, "Values must be 0-255")
        }
        
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
        
        onResult(h.roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt(), r, g, b, null)
    } catch (e: Exception) {
        onResult(0, 0, 50, 0, 0, 0, "Conversion error")
    }
}

private fun convertHslToRgb(
    hStr: String, sStr: String, lStr: String,
    onResult: (Int, Int, Int, Int, Int, Int, String?) -> Unit
) {
    try {
        val h = hStr.toFloatOrNull() ?: return onResult(0, 0, 50, 0, 0, 0, null)
        val s = (sStr.toFloatOrNull() ?: 0f) / 100f
        val l = (lStr.toFloatOrNull() ?: 50f) / 100f
        
        if (h !in 0f..360f) return onResult(0, 0, 50, 0, 0, 0, "Hue must be 0-360")
        if (s !in 0f..1f) return onResult(0, 0, 50, 0, 0, 0, "Saturation must be 0-100")
        if (l !in 0f..1f) return onResult(0, 0, 50, 0, 0, 0, "Lightness must be 0-100")
        
        val c = (1 - kotlin.math.abs(2 * l - 1)) * s
        val x = c * (1 - kotlin.math.abs((h / 60 % 2) - 1))
        val m = l - c / 2
        
        var rf = 0f
        var gf = 0f
        var bf = 0f
        
        when {
            h < 60 -> { rf = c; gf = x; bf = 0f }
            h < 120 -> { rf = x; gf = c; bf = 0f }
            h < 180 -> { rf = 0f; gf = c; bf = x }
            h < 240 -> { rf = 0f; gf = x; bf = c }
            h < 300 -> { rf = x; gf = 0f; bf = c }
            else -> { rf = c; gf = 0f; bf = x }
        }
        
        val r = ((rf + m) * 255).roundToInt().coerceIn(0, 255)
        val g = ((gf + m) * 255).roundToInt().coerceIn(0, 255)
        val b = ((bf + m) * 255).roundToInt().coerceIn(0, 255)
        
        onResult(h.roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt(), r, g, b, null)
    } catch (e: Exception) {
        onResult(0, 0, 50, 0, 0, 0, "Conversion error")
    }
}
