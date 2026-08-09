package com.dannyk.toolbox.ui.screens.tools.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect
import android.graphics.Color

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HexRGBConverterScreen(navController: NavHostController) {
    val clipboardManager = LocalClipboardManager.current
    
    // HEX to RGB mode
    var hexInput by remember { mutableStateOf("#FF5722") }
    var parsedRed by remember { mutableIntStateOf(255) }
    var parsedGreen by remember { mutableIntStateOf(87) }
    var parsedBlue by remember { mutableIntStateOf(34) }
    var hexError by remember { mutableStateOf<String?>(null) }
    
    // RGB to HEX mode
    var redInput by remember { mutableStateOf("255") }
    var greenInput by remember { mutableStateOf("87") }
    var blueInput by remember { mutableStateOf("34") }
    var generatedHex by remember { mutableStateOf("#FF5722") }
    var rgbError by remember { mutableStateOf<String?>(null) }

    // Parse HEX on change
    LaunchedEffect(hexInput) {
        parseHexColor(hexInput) { r, g, b, error ->
            parsedRed = r
            parsedGreen = g
            parsedBlue = b
            hexError = error
        }
    }

    // Generate HEX on RGB change
    LaunchedEffect(redInput, greenInput, blueInput) {
        generateHexFromRgb(redInput, greenInput, blueInput) { hex, error ->
            generatedHex = hex
            rgbError = error
        }
    }

    Scaffold(
        topBar = { ToolTopBar("HEX ↔ RGB Converter", onBackClick = { navController.navigateUp() }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ==================== HEX TO RGB SECTION ====================
            Text(
                text = "HEX → RGB",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // HEX Input Field
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { newValue ->
                            // Allow # and hex characters only
                            if (newValue.isEmpty() || 
                                newValue.matches(Regex("^#?[A-Fa-f0-9]{0,6}$"))) {
                                hexInput = newValue.uppercase()
                            } else if (newValue.matches(Regex("^#?[A-Fa-f0-9]{7,}$"))) {
                                // Truncate to 7 chars (# + 6 hex)
                                hexInput = newValue.take(7).uppercase()
                            }
                        },
                        label = { Text("HEX Color Code") },
                        placeholder = { Text("#RRGGBB or RRGGBB") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        prefix = { 
                            if (!hexInput.startsWith("#")) {
                                Text("#", fontFamily = FontFamily.Monospace)
                            }
                        },
                        isError = hexError != null,
                        supportingText = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = hexError ?: "Enter a valid 6-digit hexadecimal color code",
                                    color = if (hexError != null) MaterialTheme.colorScheme.error 
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                IconButton(
                                    onClick = { clipboardManager.setText(AnnotatedString(hexInput)) },
                                    enabled = hexError == null && hexInput.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy HEX",
                                         modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Parsed RGB Values
                    if (hexError == null && hexInput.length >= 6) {
                        // Color Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ComposeColor(parsedRed, parsedGreen, parsedBlue)),
                            contentAlignment = Alignment.Center
                        ) {
                            val luminance = (0.299 * parsedRed + 0.587 * parsedGreen + 0.114 * parsedBlue) / 255
                            Text(
                                text = hexInput.ifEmpty { "#000000" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (luminance > 0.5) ComposeColor.Black else ComposeColor.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // RGB Output Cards
                        Text(
                            text = "Parsed RGB Values:",
                            style = MaterialTheme.typography.labelLarge
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RgbValueCard("R", parsedRed.toString(), ComposeColor.Red)
                            RgbValueCard("G", parsedGreen.toString(), ComposeColor.Green)
                            RgbValueCard("B", parsedBlue.toString(), ComposeColor.Blue)
                        }

                        // Additional formats
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FormatRow("HEX", hexInput.ifEmpty { "#000000" })
                                FormatRow("RGB", "rgb($parsedRed, $parsedGreen, $parsedBlue)")
                                FormatRow("CSS", "rgb($parsedRed, $parsedGreen, $parsedBlue)")
                                FormatRow("Integer", "${Color.rgb(parsedRed, parsedGreen, parsedBlue)}")
                            }
                        }

                        // Copy all button
                        OutlinedButton(
                            onClick = {
                                val text = """
                                    |HEX: ${hexInput.ifEmpty { "#000000" }}
                                    |RGB: rgb($parsedRed, $parsedGreen, $parsedBlue)
                                    |R: $parsedRed
                                    |G: $parsedGreen
                                    |B: $parsedBlue
                                """.trimMargin()
                                clipboardManager.setText(AnnotatedString(text))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, 
                                 modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy All Values")
                        }
                    }
                }
            }

            // ==================== RGB TO HEX SECTION ====================
            Text(
                text = "RGB → HEX",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // RGB Input Fields
                    Text(
                        text = "Enter RGB values (0-255):",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RgbInputField(
                            label = "Red",
                            value = redInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..255)) {
                                    redInput = it 
                                }
                            },
                            color = ComposeColor.Red,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        RgbInputField(
                            label = "Green",
                            value = greenInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..255)) {
                                    greenInput = it 
                                }
                            },
                            color = ComposeColor.Green,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        RgbInputField(
                            label = "Blue",
                            value = blueInput,
                            onValueChange = { 
                                if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..255)) {
                                    blueInput = it 
                                }
                            },
                            color = ComposeColor.Blue,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider(modifier = Modifier.fillMaxWidth())

                    // Generated HEX Value
                    if (rgbError == null && redInput.isNotEmpty() && 
                        greenInput.isNotEmpty() && blueInput.isNotEmpty()) {
                        val r = redInput.toIntOrNull() ?: 0
                        val g = greenInput.toIntOrNull() ?: 0
                        val b = blueInput.toIntOrNull() ?: 0
                        
                        // Color Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ComposeColor(r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))),
                            contentAlignment = Alignment.Center
                        ) {
                            val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255
                            Text(
                                text = generatedHex,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (luminance > 0.5) ComposeColor.Black else ComposeColor.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // HEX Output Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Generated HEX Code",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = generatedHex.uppercase(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                IconButton(onClick = { 
                                    clipboardManager.setText(AnnotatedString(generatedHex)) 
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy HEX")
                                }
                            }
                        }

                        // Copy all button
                        OutlinedButton(
                            onClick = {
                                val text = """
                                    |HEX: $generatedHex
                                    |RGB: rgb($r, $g, $b)
                                    |R: $r
                                    |G: $g
                                    |B: $b
                                """.trimMargin()
                                clipboardManager.setText(AnnotatedString(text))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, 
                                 modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy All Values")
                        }
                    } else if (rgbError != null) {
                        Text(
                            text = rgbError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Quick Reference Colors
            Text(
                text = "Quick Reference",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Card(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("#000000", "Black", ComposeColor.Black),
                        Triple("#FFFFFF", "White", ComposeColor.White),
                        Triple("#FF0000", "Red", ComposeColor.Red),
                        Triple("#00FF00", "Lime", ComposeColor(0xFF00FF00)),
                        Triple("#0000FF", "Blue", ComposeColor.Blue),
                        Triple("#FFFF00", "Yellow", ComposeColor.Yellow),
                        Triple("#00FFFF", "Cyan", ComposeColor.Cyan),
                        Triple("#FF00FF", "Magenta", ComposeColor.Magenta),
                        Triple("#FF5722", "Deep Orange", ComposeColor(0xFFFF5722)),
                        Triple("#4CAF50", "Green", ComposeColor(0xFF4CAF50)),
                        Triple("#2196F3", "Light Blue", ComposeColor(0xFF2196F3)),
                        Triple("#9C27B0", "Purple", ComposeColor(0xFF9C27B0)),
                    ).forEach { (hex, name, color) ->
                        Surface(
                            onClick = {
                                hexInput = hex
                                val c = android.graphics.Color.parseColor(hex)
                                redInput = android.graphics.Color.red(c).toString()
                                greenInput = android.graphics.Color.green(c).toString()
                                blueInput = android.graphics.Color.blue(c).toString()
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
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Column {
                                    Text(name, style = MaterialTheme.typography.labelSmall)
                                    Text(hex, style = MaterialTheme.typography.labelSmall, 
                                         fontFamily = FontFamily.Monospace,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String, onResult: (Int, Int, Int, String?) -> Unit) {
    try {
        val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
        
        when {
            cleanHex.isEmpty() -> onResult(0, 0, 0, null)
            cleanHex.length < 6 -> onResult(0, 0, 0, null)
            cleanHex.length == 6 -> {
                val colorInt = Integer.parseInt(cleanHex, 16)
                onResult(
                    (colorInt shr 16) and 0xFF,
                    (colorInt shr 8) and 0xFF,
                    colorInt and 0xFF,
                    null
                )
            }
            else -> onResult(0, 0, 0, "Invalid HEX format")
        }
    } catch (e: Exception) {
        onResult(0, 0, 0, "Invalid HEX code")
    }
}

private fun generateHexFromRgb(rStr: String, gStr: String, bStr: String, 
                               onResult: (String, String?) -> Unit) {
    try {
        if (rStr.isEmpty() || gStr.isEmpty() || bStr.isEmpty()) {
            onResult("#000000", null)
            return
        }
        
        val r = rStr.toIntOrNull()
        val g = gStr.toIntOrNull()
        val b = bStr.toIntOrNull()
        
        if (r == null || g == null || b == null) {
            onResult("#000000", "Invalid number format")
            return
        }
        
        if (r !in 0..255 || g !in 0..255 || b !in 0..255) {
            onResult("#000000", "Values must be 0-255")
            return
        }
        
        val hex = String.format("#%02X%02X%02X", r, g, b)
        onResult(hex, null)
    } catch (e: Exception) {
        onResult("#000000", "Conversion error")
    }
}

@Composable
private fun RgbValueCard(label: String, value: String, color: ComposeColor) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelMedium, 
                         fontWeight = FontWeight.Bold, color = ComposeColor.White)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, 
                 fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text("/ 255", style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun RgbInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    color: ComposeColor,
    modifier: Modifier = Modifier
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
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
private fun FormatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, 
             color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, 
             fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}
