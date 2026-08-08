package com.dannyk.toolbox.ui.screens.tools.image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
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
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import android.content.ClipboardManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Divider
import androidx.compose.foundation.ScrollState
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContrastCheckerScreen(navController: NavHostController) {
    val clipboardManager = LocalClipboardManager.current
    
    // Foreground color state
    var foregroundColor by remember { mutableIntStateOf(Color.BLACK) }
    var fgRed by remember { mutableIntStateOf(0) }
    var fgGreen by remember { mutableIntStateOf(0) }
    var fgBlue by remember { mutableIntStateOf(0) }
    
    // Background color state
    var backgroundColor by remember { mutableIntStateOf(Color.WHITE) }
    var bgRed by remember { mutableIntStateOf(255) }
    var bgGreen by remember { mutableIntStateOf(255) }
    var bgBlue by remember { mutableIntStateOf(255) }

    // Calculate contrast ratio whenever colors change
    val contrastRatio = remember(fgRed, fgGreen, fgBlue, bgRed, bgGreen, bgBlue) {
        calculateContrastRatio(
            fgRed.coerceIn(0, 255), fgGreen.coerceIn(0, 255), fgBlue.coerceIn(0, 255),
            bgRed.coerceIn(0, 255), bgGreen.coerceIn(0, 255), bgBlue.coerceIn(0, 255)
        )
    }

    Scaffold(
        topBar = { ToolTopBar("WCAG Contrast Checker", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==================== COLOR PICKERS ====================
            Text(
                text = "Select Colors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Foreground Color Picker Card
                ColorPickerCard(
                    title = "Foreground\n(Text)",
                    red = fgRed,
                    green = fgGreen,
                    blue = fgBlue,
                    onRedChange = { 
                        fgRed = it.coerceIn(0, 255)
                        foregroundColor = Color.rgb(fgRed, fgGreen, fgBlue)
                    },
                    onGreenChange = { 
                        fgGreen = it.coerceIn(0, 255)
                        foregroundColor = Color.rgb(fgRed, fgGreen, fgBlue)
                    },
                    onBlueChange = { 
                        fgBlue = it.coerceIn(0, 255)
                        foregroundColor = Color.rgb(fgRed, fgGreen, fgBlue)
                    },
                    modifier = Modifier.weight(1f)
                )

                // Background Color Picker Card
                ColorPickerCard(
                    title = "Background",
                    red = bgRed,
                    green = bgGreen,
                    blue = bgBlue,
                    onRedChange = { 
                        bgRed = it.coerceIn(0, 255)
                        backgroundColor = Color.rgb(bgRed, bgGreen, bgBlue)
                    },
                    onGreenChange = { 
                        bgGreen = it.coerceIn(0, 255)
                        backgroundColor = Color.rgb(bgRed, bgGreen, bgBlue)
                    },
                    onBlueChange = { 
                        bgBlue = it.coerceIn(0, 255)
                        backgroundColor = Color.rgb(bgRed, bgGreen, bgBlue)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // ==================== CONTRAST RATIO RESULT ====================
            Text(
                text = "Contrast Ratio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        contrastRatio >= 7f -> MaterialTheme.colorScheme.primaryContainer
                        contrastRatio >= 4.5f -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.2f:1", contrastRatio),
                        style = MaterialTheme.typography.displaySmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Visual indicator bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((contrastRatio / 21f).coerceAtMost(1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        contrastRatio >= 7f -> MaterialTheme.colorScheme.primary
                                        contrastRatio >= 4.5f -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                 )
                        )
                    }
                    
                    Text(
                        text = "Maximum: 21:1",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ==================== WCAG COMPLIANCE CHECKS ====================
            Text(
                text = "WCAG 2.1 Compliance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Level AA Normal Text
                ComplianceCard(
                    level = "AA",
                    textType = "Normal Text (<18px or <14px bold)",
                    requiredRatio = "4.5:1",
                    actualRatio = contrastRatio,
                    passes = contrastRatio >= 4.5f
                )

                // Level AA Large Text
                ComplianceCard(
                    level = "AA",
                    textType = "Large Text (≥18px or ≥14px bold)",
                    requiredRatio = "3:1",
                    actualRatio = contrastRatio,
                    passes = contrastRatio >= 3f
                )

                // Level AAA Normal Text
                ComplianceCard(
                    level = "AAA",
                    textType = "Normal Text (<18px or <14px bold)",
                    requiredRatio = "7:1",
                    actualRatio = contrastRatio,
                    passes = contrastRatio >= 7f
                )

                // Level AAA Large Text
                ComplianceCard(
                    level = "AAA",
                    textType = "Large Text (≥18px or ≥14px bold)",
                    requiredRatio = "4.5:1",
                    actualRatio = contrastRatio,
                    passes = contrastRatio >= 4.5f
                )
            }

            // ==================== TEXT PREVIEW ====================
            Text(
                text = "Text Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview with selected colors
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ComposeColor(backgroundColor))
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Normal Text Sample (16px)",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Normal,
                                color = ComposeColor(foregroundColor)
                            )
                            Text(
                                text = "Bold Text Sample (16px)",
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                fontWeight = FontWeight.Bold,
                                color = ComposeColor(foregroundColor)
                            )
                            Text(
                                text = "Large Text Sample (24px)",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                fontWeight = FontWeight.Normal,
                                color = ComposeColor(foregroundColor)
                            )
                            Text(
                                text = "Large Bold Text Sample (24px)",
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                fontWeight = FontWeight.Bold,
                                color = ComposeColor(foregroundColor)
                            )
                        }
                    }

                    Divider()

                    // Swap button
                    OutlinedButton(
                        onClick = {
                            val tempR = fgRed; val tempG = fgGreen; val tempB = fgBlue
                            fgRed = bgRed; fgGreen = bgGreen; fgBlue = bgBlue
                            bgRed = tempR; bgGreen = tempG; bgBlue = tempB
                            foregroundColor = Color.rgb(fgRed, fgGreen, fgBlue)
                            backgroundColor = Color.rgb(bgRed, bgGreen, bgBlue)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = null, 
                             modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Swap Colors")
                    }
                }
            }

            // ==================== PRESET COMBINATIONS ====================
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Black/White", Color.BLACK, Color.WHITE),
                    Triple("Dark Gray/Light", Color.DKGRAY, Color.LTGRAY),
                    Triple("Navy/White", Color.parseColor("#1a237e"), Color.WHITE),
                    Triple("White/Navy", Color.WHITE, Color.parseColor("#1a237e")),
                    Triple("Blue/White", Color.BLUE, Color.WHITE),
                    Triple("Red/White", Color.RED, Color.WHITE),
                    Triple("Green/White", Color.GREEN, Color.WHITE),
                    Triple("Orange/White", Color.parseColor("#E65100"), Color.WHITE),
                    Triple("Teal/Dark", Color.TEAL, Color.parseColor("#263238")),
                    Triple("Amber/Black", Color.parseColor("#FFC107"), Color.BLACK),
                ).forEach { (name, fg, bg) ->
                    Surface(
                        onClick = {
                            foregroundColor = fg
                            backgroundColor = bg
                            fgRed = Color.red(fg); fgGreen = Color.green(fg); fgBlue = Color.blue(fg)
                            bgRed = Color.red(bg); bgGreen = Color.green(bg); bgBlue = Color.blue(bg)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(ComposeColor(fg))
                            )
                            Text("/", style = MaterialTheme.typography.labelSmall,
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(ComposeColor(bg))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(name, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // Copy Results Button
            Button(
                onClick = {
                    val fgHex = String.format("#%02X%02X%02X", fgRed, fgGreen, fgBlue)
                    val bgHex = String.format("#%02X%02X%02X", bgRed, bgGreen, bgBlue)
                    val aaNormal = if (contrastRatio >= 4.5f) "PASS" else "FAIL"
                    val aaLarge = if (contrastRatio >= 3f) "PASS" else "FAIL"
                    val aaaNormal = if (contrastRatio >= 7f) "PASS" else "FAIL"
                    val aaaLarge = if (contrastRatio >= 4.5f) "PASS" else "FAIL"
                    
                    val result = """
                        |WCAG 2.1 Contrast Check Results
                        |================================
                        |Foreground: $fgHex ($fgRed, $fgGreen, $fgBlue)
                        |Background: $bgHex ($bgRed, $bgGreen, $bgBlue)
                        |
                        |Contrast Ratio: ${String.format("%.2f:1", contrastRatio)}
                        |
                        |Compliance:
                        |- AA Normal Text: $aaNormal (requires 4.5:1)
                        |- AA Large Text: $aaLarge (requires 3:1)
                        |- AAA Normal Text: $aaaNormal (requires 7:1)
                        |- AAA Large Text: $aaaLarge (requires 4.5:1)
                    """.trimMargin()
                    clipboardManager.setText(AnnotatedString(result))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy Full Report")
            }
        }
    }
}

@Composable
private fun ColorPickerCard(
    title: String,
    red: Int,
    green: Int,
    blue: Int,
    onRedChange: (Int) -> Unit,
    onGreenChange: (Int) -> Unit,
    onBlueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, 
                 fontWeight = FontWeight.Medium)
            
            // Color preview swatch
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ComposeColor(red, green, blue))
            )
            
            // RGB inputs
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RgbMiniInput("R", red, ComposeColor.Red, onRedChange)
                RgbMiniInput("G", green, ComposeColor.Green, onGreenChange)
                RgbMiniInput("B", blue, ComposeColor.Blue, onBlueChange)
            }
            
            // HEX display
            Text(
                text = String.format("#%02X%02X%02X", red, green, blue),
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RgbMiniInput(
    label: String,
    value: Int,
    color: ComposeColor,
    onValueChange: (Int) -> Unit
) {
    var inputValue by remember { mutableStateOf(value.toString()) }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, 
             color = color, modifier = Modifier.width(16.dp))
        
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
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.style.TextAlign.Center
            ),
            modifier = Modifier.width(70.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun ComplianceCard(
    level: String,
    textType: String,
    requiredRatio: String,
    actualRatio: Float,
    passes: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (passes) MaterialTheme.colorScheme.primaryContainer 
                           else MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pass/Fail icon
            if (passes) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Pass",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Fail",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (passes) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "WCAG $level",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(textType, style = MaterialTheme.typography.bodyMedium)
                }
                
                Text(
                    text = "Required: $requiredRatio • Actual: ${String.format("%.2f:1", actualRatio)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Status badge
            Surface(
                shape = CircleShape,
                color = if (passes) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.error
            ) {
                Text(
                    text = if (passes) "PASS" else "FAIL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

private fun calculateContrastRatio(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Float {
    // Get relative luminance for each color
    val l1 = getRelativeLuminance(r1, g1, b1)
    val l2 = getRelativeLuminance(r2, g2, b2)
    
    // Calculate contrast ratio
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    
    return ((lighter + 0.05) / (darker + 0.05))
}

private fun getRelativeLuminance(r: Int, g: Int, b: Int): Double {
    // Convert 8-bit sRGB to linear RGB
    val rsRGB = r / 255.0
    val gsRGB = g / 255.0
    val bsRGB = b / 255.0
    
    val rLinear = if (rsRGB <= 0.03928) rsRGB / 12.92 else ((rsRGB + 0.055) / 1.055).pow(2.4)
    val gLinear = if (gsRGB <= 0.03928) gsRGB / 12.92 else ((gsRGB + 0.055) / 1.055).pow(2.4)
    val bLinear = if (bsRGB <= 0.03928) bsRGB / 12.92 else ((bsRGB + 0.055) / 1.055).pow(2.4)
    
    return 0.2126 * rLinear + 0.7152 * gLinear + 0.0722 * bLinear
}
