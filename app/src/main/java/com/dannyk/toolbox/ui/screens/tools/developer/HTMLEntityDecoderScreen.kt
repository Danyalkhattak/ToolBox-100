package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.foundation.ScrollState
import androidx.core.text.HtmlCompat

@Composable
fun HTMLEntityDecoderScreen(navController: NavHostController) {
    var entityInput by remember { mutableStateOf("") }
    var decodedOutput by remember { mutableStateOf("") }
    var decodeError by remember { mutableStateOf<String?>(null) }
    var decodedEntities by remember { mutableStateOf<List<DecodedEntity>>(emptyList()) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "HTML Entity Decoder",
            subtitle = "Decode HTML entities back to plain text",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("HTML Entity Input", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = entityInput,
                        onValueChange = { 
                            entityInput = it
                            decodeError = null
                        },
                        label = { Text("Enter text with HTML entities") },
                        placeholder = { 
                            Text(
                                "&lt;div class=&quot;test&quot;&gt;Hello &amp; Welcome!&lt;/div&gt;", 
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp, max = 150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    PrimaryButton(
                        text = "Decode HTML Entities",
                        onClick = {
                            try {
                                val result = decodeHtmlEntities(entityInput)
                                decodedOutput = result.first
                                decodedEntities = result.second
                                decodeError = null
                            } catch (e: Exception) {
                                decodedOutput = ""
                                decodedEntities = emptyList()
                                decodeError = e.message ?: "Failed to decode entities"
                            }
                        },
                        enabled = entityInput.isNotEmpty(),
                        icon = Icons.Default.Code
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            entityInput = ""
                            decodedOutput = ""
                            decodedEntities = emptyList()
                            decodeError = null
                        }
                    )

                    // Quick examples
                    Text("Quick Examples:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    
                    listOf(
                        "&amp;" to "Ampersand",
                        "&lt;&gt;" to "Less/Greater than",
                        "&#169;" to "Copyright symbol",
                        "&#x2603;" to "Snowman"
                    ).forEach { (example, desc) ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { entityInput = example }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(example, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("($desc)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Error Display
            decodeError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Decoding Error", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Output Section
            if (decodedOutput.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Decoded Output", style = MaterialTheme.typography.titleMedium)
                            
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(decodedOutput))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy to clipboard"
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 60.dp, max = 200.dp),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = decodedOutput,
                                fontFamily = FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Encoded", "${entityInput.length} chars")
                            StatItem("Decoded", "${decodedOutput.length} chars")
                            StatItem("Entities", "${decodedEntities.size}")
                        }
                    }
                }
            }

            // Decoded Entities Detail
            if (decodedEntities.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Decoded Entities (${decodedEntities.size})", style = MaterialTheme.typography.titleMedium)

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(decodedEntities) { entity ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            entity.entity,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            entity.decoded,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    Text(
                                        entity.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Common Entity Reference Table
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Common Entity Reference Table", style = MaterialTheme.typography.titleSmall)

                    // Named entities section
                    Text("Named Entities:", style = MaterialTheme.typography.labelMedium)
                    
                    val namedEntities = listOf(
                        Triple("&amp;", "&", "Ampersand"),
                        Triple("&lt;", "<", "Less than"),
                        Triple("&gt;", ">", "Greater than"),
                        Triple("&quot;", "\"", "Double quote"),
                        Triple("&apos;", "'", "Single quote"),
                        Triple("&nbsp;", "\u00A0", "Non-breaking space"),
                        Triple("&copy;", "\u00A9", "Copyright"),
                        Triple("&reg;", "\u00AE", "Registered"),
                        Triple("&trade;", "\u2122", "Trademark"),
                        Triple("&euro;", "\u20AC", "Euro sign"),
                        Triple("&pound;", "\u00A3", "Pound sign"),
                        Triple("&yen;", "\u00A5", "Yen sign"),
                        Triple("&cent;", "\u00A2", "Cent sign"),
                        Triple("&times;", "\u00D7", "Multiplication"),
                        Triple("&divide;", "\u00F7", "Division"),
                        Triple("&plusmn;", "\u00B1", "Plus-minus"),
                        Triple("&mdash;", "\u2014", "Em dash"),
                        Triple("&ndash;", "\u2013", "En dash"),
                        Triple("&lsquo;", "\u2018", "Left single quote"),
                        Triple("&rsquo;", "\u2019", "Right single quote"),
                        Triple("&ldquo;", "\u201C", "Left double quote"),
                        Triple("&rdquo;", "\u201D", "Right double quote"),
                        Triple("&laquo;", "\u00AB", "Left guillemet"),
                        Triple("&raquo;", "\u00BB", "Right guillemet"),
                        Triple("&hellip;", "\u2026", "Horizontal ellipsis"),
                        Triple("&bull;", "\u2022", "Bullet"),
                        Triple("&middot;", "\u00B7", "Middle dot"),
                        Triple("&para;", "\u00B6", "Pilcrow/Paragraph"),
                        Triple("&sect;", "\u00A7", "Section sign"),
                        Triple("&deg;", "\u00B0", "Degree sign")
                    )

                    namedEntities.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (entity, char, desc) ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(entity, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                    Text("→$char ", style = MaterialTheme.typography.labelSmall)
                                    Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Numeric entities examples
                    Text("Numeric Entity Examples:", style = MaterialTheme.typography.labelMedium)
                    
                    val numericExamples = listOf(
                        Triple("&#38;", "&", "Decimal ampersand"),
                        Triple("&#60;", "<", "Decimal less than"),
                        Triple("&#x26;", "&", "Hex ampersand"),
                        Triple("&#x3C;", "<", "Hex less than"),
                        Triple("&#169;", "\u00A9", "Decimal copyright"),
                        Triple("&#xA9;", "\u00A9", "Hex copyright"),
                        Triple("&#8364;", "\u20AC", "Decimal euro"),
                        Triple("&#x20AC;", "\u20AC", "Hex euro"),
                        Triple("&#9733;", "\u2603", "Decimal snowman ☃"),
                        Triple("&#x2603;", "\u2603", "Hex snowman ☃"),
                        Triple("&#128512;", "\uD83D\uDE00", "Grinning face 😀"),
                        Triple("&#x1F600;", "\uD83D\uDE00", "Hex grinning face 😀")
                    )

                    numericExamples.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { (entity, char, desc) ->
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.tertiaryContainer) {
                                        Text(entity, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                    Text("→$char ", style = MaterialTheme.typography.labelSmall)
                                    Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About HTML Entity Decoding", style = MaterialTheme.typography.titleSmall)
                    
                    Text(
                        "HTML entities are decoded by converting them back to their character representations.\n\n" +
                        "• Named: &name; → character (e.g., &amp; → &)\n" +
                        "• Decimal: &#NNN; → Unicode code point NNN\n" +
                        "• Hexadecimal: &#xHHH; → Unicode code point HHH\n\n" +
                        "All three formats produce the same output for the same character.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class DecodedEntity(
    val entity: String,
    val decoded: String,
    val position: Int,
    val description: String
)

// Named entity lookup table (reverse of encoding map)
private val NAMED_ENTITY_TO_CHAR = mapOf(
    "amp" to '&',
    "lt" to '<',
    "gt" to '>',
    "quot" to '"',
    "apos" to '\'',
    "nbsp" to '\u00A0',
    "copy" to '\u00A9',
    "reg" to '\u00AE',
    "trade" to '\u2122',
    "euro" to '\u20AC',
    "pound" to '\u00A3',
    "yen" to '\u00A5',
    "cent" to '\u00A2',
    "times" to '\u00D7',
    "divide" to '\u00F7',
    "plusmn" to '\u00B1',
    "laquo" to '\u00AB',
    "raquo" to '\u00BB',
    "mdash" to '\u2014',
    "ndash" to '\u2013',
    "lsquo" to '\u2018',
    "rsquo" to '\u2019',
    "ldquo" to '\u201C',
    "rdquo" to '\u201D',
    "hellip" to '\u2026',
    "bull" to '\u2022',
    "middot" to '\u00B7',
    "para" to '\u00B6',
    "sect" to '\u00A7',
    "deg" to '\u00B0'
)

private fun decodeHtmlEntities(input: String): Pair<String, List<DecodedEntity>> {
    val result = StringBuilder()
    val entities = mutableListOf<DecodedEntity>()
    var i = 0
    
    while (i < input.length) {
        if (input[i] == '&' && i + 1 < input.length && input[i + 1] != ' ') {
            // Try to find an entity
            val semicolonIndex = input.indexOf(';', i + 1)
            
            if (semicolonIndex != -1 && semicolonIndex - i <= 12) { // Max reasonable entity length
                val entityContent = input.substring(i + 1, semicolonIndex)
                
                when {
                    // Named entity
                    entityContent in NAMED_ENTITY_TO_CHAR -> {
                        val char = NAMED_ENTITY_TO_CHAR[entityContent]!!
                        result.append(char)
                        entities.add(DecodedEntity(
                            entity = input.substring(i, semicolonIndex + 1),
                            decoded = char.toString(),
                            position = i,
                            description = getCharDescription(char)
                        ))
                        i = semicolonIndex + 1
                    }
                    // Decimal numeric entity &#NNN;
                    entityContent.startsWith("#") && !entityContent.startsWith("#x") && !entityContent.startsWith("#X") -> {
                        try {
                            val codePoint = entityContent.substring(1).toInt()
                            if (codePoint in 0..0x10FFFF) {
                                val char = Char(codePoint)
                                result.append(char)
                                entities.add(DecodedEntity(
                                    entity = input.substring(i, semicolonIndex + 1),
                                    decoded = char.toString(),
                                    position = i,
                                    description = getCharDescription(char)
                                ))
                                i = semicolonIndex + 1
                            } else {
                                result.append(input[i])
                                i++
                            }
                        } catch (e: NumberFormatException) {
                            result.append(input[i])
                            i++
                        }
                    }
                    // Hexadecimal numeric entity &#xHHH;
                    entityContent.startsWith("#x") || entityContent.startsWith("#X") -> {
                        try {
                            val hexPart = entityContent.substring(2)
                            val codePoint = hexPart.toInt(16)
                            if (codePoint in 0..0x10FFFF) {
                                val char = Char(codePoint)
                                result.append(char)
                                entities.add(DecodedEntity(
                                    entity = input.substring(i, semicolonIndex + 1),
                                    decoded = char.toString(),
                                    position = i,
                                    description = getCharDescription(char)
                                ))
                                i = semicolonIndex + 1
                            } else {
                                result.append(input[i])
                                i++
                            }
                        } catch (e: NumberFormatException) {
                            result.append(input[i])
                            i++
                        }
                    }
                    else -> {
                        // Not a valid entity, just append the &
                        result.append(input[i])
                        i++
                    }
                }
            } else {
                result.append(input[i])
                i++
            }
        } else {
            result.append(input[i])
            i++
        }
    }
    
    return Pair(result.toString(), entities)
}

private fun getCharDescription(char: Char): String {
    return when (char) {
        '&' -> "Ampersand"
        '<' -> "Less than"
        '>' -> "Greater than"
        '"' -> "Double quote"
        '\'' -> "Single quote/Apostrophe"
        '\u00A0' -> "Non-breaking space"
        '\u00A9' -> "Copyright symbol"
        '\u00AE' -> "Registered trademark"
        '\u2122' -> "Trademark"
        '\u20AC' -> "Euro sign"
        '\u00A3' -> "Pound sterling"
        '\u00A5' -> "Yen/Yuan sign"
        '\u00A2' -> "Cent sign"
        '\u00D7' -> "Multiplication sign"
        '\u00F7' -> "Division sign"
        '\u00B1' -> "Plus-minus sign"
        '\u00AB' -> "Left guillemet"
        '\u00BB' -> "Right guillemet"
        '\u2014' -> "Em dash"
        '\u2013' -> "En dash"
        '\u2018' -> "Left single quotation mark"
        '\u2019' -> "Right single quotation mark"
        '\u201C' -> "Left double quotation mark"
        '\u201D' -> "Right double quotation mark"
        '\u2026' -> "Horizontal ellipsis"
        '\u2022' -> "Bullet"
        '\u00B7' -> "Middle dot"
        '\u00B6' -> "Pilcrow/Paragraph sign"
        '\u00A7' -> "Section sign"
        '\u00B0' -> "Degree sign"
        '\n' -> "Newline"
        '\r' -> "Carriage return"
        '\t' -> "Tab"
        ' ' -> "Space"
        in 'a'..'z', in 'A'..'Z' -> "$char (Letter)"
        in '0'..'9' -> "$char (Digit)"
        else -> "U+${String.format("%04X", char.code)}"
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
