package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Html
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

@Composable
fun HTMLEntityEncoderScreen(navController: NavHostController) {
    var plainText by remember { mutableStateOf("") }
    var encodedOutput by remember { mutableStateOf("") }
    var encodeSpecialOnly by remember { mutableStateOf(true) }
    var encodeAllNonAscii by remember { mutableStateOf(false) }
    var encodedEntities by remember { mutableStateOf<List<EncodedEntity>>(emptyList()) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "HTML Entity Encoder",
            subtitle = "Encode special characters to HTML entities",
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
                    Text("Text to Encode", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = plainText,
                        onValueChange = { plainText = it },
                        label = { Text("Enter text with special characters") },
                        placeholder = { 
                            Text(
                                "<div class=\"test\">Hello & Welcome!</div>", 
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

                    // Encoding options
                    Text("Encoding Options", style = MaterialTheme.typography.bodyMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = encodeSpecialOnly,
                            onCheckedChange = { encodeSpecialOnly = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Encode only 5 special chars", style = MaterialTheme.typography.bodyMedium)
                            Text("< > & \" '", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = encodeAllNonAscii,
                            onCheckedChange = { encodeAllNonAscii = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Encode all non-ASCII", style = MaterialTheme.typography.bodyMedium)
                            Text("Convert é, ñ, ©, etc. to entities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    PrimaryButton(
                        text = "Encode HTML Entities",
                        onClick = {
                            val result = encodeHtmlEntities(plainText, encodeSpecialOnly, encodeAllNonAscii)
                            encodedOutput = result.first
                            encodedEntities = result.second
                        },
                        enabled = plainText.isNotEmpty(),
                        icon = Icons.Default.Html
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            plainText = ""
                            encodedOutput = ""
                            encodedEntities = emptyList()
                        }
                    )
                }
            }

            // Output Section
            if (encodedOutput.isNotEmpty()) {
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
                            Text("Encoded Output", style = MaterialTheme.typography.titleMedium)
                            
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(encodedOutput))
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
                                text = encodedOutput,
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
                            StatItem("Original", "${plainText.length} chars")
                            StatItem("Encoded", "${encodedOutput.length} chars")
                            StatItem("Entities", "${encodedEntities.size}")
                        }
                    }
                }
            }

            // Encoded Entities Detail
            if (encodedEntities.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Encoded Entities (${encodedEntities.size})", style = MaterialTheme.typography.titleMedium)

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(encodedEntities) { entity ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            entity.original,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
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
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            entity.entity,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
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

            // Special Characters Reference
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("5 Must-Encode HTML Characters", style = MaterialTheme.typography.titleSmall)

                    Text(
                        "These characters have special meaning in HTML and must be encoded:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val mustEncodeChars = listOf(
                        EntityInfo("&", "&amp;", "Ampersand - starts entities"),
                        EntityInfo("<", "&lt;", "Less than - starts tags"),
                        EntityInfo(">", "&gt;", "Greater than - ends tags"),
                        EntityInfo("\"", "&quot;", "Double quote - attribute value"),
                        EntityInfo("'", "&apos;", "Single quote - attribute value")
                    )

                    mustEncodeChars.forEach { info ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.errorContainer) {
                                Text(info.char, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = MaterialTheme.typography.bodyLarge.fontSize)
                            }
                            
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            
                            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(info.entity, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(info.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Common Named Entities Reference
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Common Named Entities", style = MaterialTheme.typography.titleSmall)

                    val commonEntities = listOf(
                        Triple("&nbsp;", "\u00A0", "Non-breaking space"),
                        Triple("&copy;", "\u00A9", "Copyright symbol"),
                        Triple("&reg;", "\u00AE", "Registered trademark"),
                        Triple("&trade;", "\u2122", "Trademark"),
                        Triple("&euro;", "\u20AC", "Euro sign"),
                        Triple("&pound;", "\u00A3", "Pound sterling"),
                        Triple("&yen;", "\u00A5", "Yen/Yuan sign"),
                        Triple("&cent;", "\u00A2", "Cent sign"),
                        Triple("&times;", "\u00D7", "Multiplication sign"),
                        Triple("&divide;", "\u00F7", "Division sign"),
                        Triple("&plusmn;", "\u00B1", "Plus-minus sign"),
                        Triple("&laquo;", "\u00AB", "Left angle quote"),
                        Triple("&raquo;", "\u00BB", "Right angle quote"),
                        Triple("&mdash;", "\u2014", "Em dash"),
                        Triple("&ndash;", "\u2013", "En dash"),
                        Triple("&lsquo;", "\u2018", "Left single quote"),
                        Triple("&rsquo;", "\u2019", "Right single quote"),
                        Triple("&ldquo;", "\u201C", "Left double quote"),
                        Triple("&rdquo;", "\u201D", "Right double quote")
                    )

                    commonEntities.chunked(2).forEach { row ->
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
                                    Text("->$char ", style = MaterialTheme.typography.labelSmall)
                                    Text("($desc)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About HTML Entity Encoding", style = MaterialTheme.typography.titleSmall)
                    
                    Text(
                        "HTML entities are used to display reserved characters in HTML documents.\n\n" +
                        "• Named entities: &amp; &lt; etc. (more readable)\n" +
                        "• Decimal numeric: &#38; &#60; etc.\n" +
                        "• Hexadecimal numeric: &#x26; &#x3C; etc.\n\n" +
                        "Always encode user input before displaying in HTML to prevent XSS attacks!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class EncodedEntity(
    val original: String,
    val entity: String,
    val position: Int,
    val description: String
)

data class EntityInfo(
    val char: String,
    val entity: String,
    val description: String
)

// Map of special characters to their named entities
private val SPECIAL_CHAR_ENTITIES = mapOf(
    '&' to Pair("&amp;", "Ampersand"),
    '<' to Pair("&lt;", "Less than"),
    '>' to Pair("&gt;", "Greater than"),
    '"' to Pair("&quot;", "Double quote"),
    '\'' to Pair("&apos;", "Single quote/Apostrophe")
)

// Extended named entities for common non-ASCII characters
private val NAMED_ENTITIES = mapOf(
    '\u00A0' to "&nbsp;",      // Non-breaking space
    '\u00A9' to "&copy;",      // Copyright
    '\u00AE' to "&reg;",       // Registered
    '\u2122' to "&trade;",     // Trademark
    '\u20AC' to "&euro;",      // Euro
    '\u00A3' to "&pound;",     // Pound
    '\u00A5' to "&yen;",       // Yen
    '\u00A2' to "&cent;",      // Cent
    '\u00D7' to "&times;",     // Times
    '\u00F7' to "&divide;",    // Divide
    '\u00B1' to "&plusmn;",    // Plus-minus
    '\u00AB' to "&laquo;",     // Left guillemet
    '\u00BB' to "&raquo;",     // Right guillemet
    '\u2014' to "&mdash;",     // Em dash
    '\u2013' to "&ndash;",     // En dash
    '\u2018' to "&lsquo;",     // Left single quote
    '\u2019' to "&rsquo;",     // Right single quote
    '\u201C' to "&ldquo;",     // Left double quote
    '\u201D' to "&rdquo;"      // Right double quote
)

private fun encodeHtmlEntities(input: String, specialOnly: Boolean, encodeAllNonAscii: Boolean): Pair<String, List<EncodedEntity>> {
    val result = StringBuilder()
    val entities = mutableListOf<EncodedEntity>()
    
    for ((index, char) in input.withIndex()) {
        when {
            // Check for 5 special characters first
            char in SPECIAL_CHAR_ENTITIES -> {
                val (entity, description) = SPECIAL_CHAR_ENTITIES[char]!!
                result.append(entity)
                entities.add(EncodedEntity(char.toString(), entity, index, description))
            }
            // Check for named entities if encoding all non-ASCII
            !specialOnly && encodeAllNonAscii && char.code > 127 -> {
                val namedEntity = NAMED_ENTITIES[char]
                if (namedEntity != null) {
                    result.append(namedEntity)
                    entities.add(EncodedEntity(char.toString(), namedEntity, index, getCharName(char)))
                } else {
                    // Use decimal numeric entity
                    val numericEntity = "&#${char.code};"
                    result.append(numericEntity)
                    entities.add(EncodedEntity(char.toString(), numericEntity, index, getCharName(char)))
                }
            }
            // Encode all non-ASCII as numeric even without named entities option
            !specialOnly && char.code > 127 -> {
                val numericEntity = "&#${char.code};"
                result.append(numericEntity)
                entities.add(EncodedEntity(char.toString(), numericEntity, index, getCharName(char)))
            }
            else -> {
                result.append(char)
            }
        }
    }
    
    return Pair(result.toString(), entities)
}

private fun getCharName(char: Char): String {
    return when (char) {
        '\n' -> "Newline"
        '\r' -> "Carriage return"
        '\t' -> "Tab"
        ' ' -> "Space"
        in 'a'..'z', in 'A'..'Z' -> "$char (Letter)"
        in '0'..'9' -> "$char (Digit)"
        else -> {
            val unicodeCategories = listOf(
                CharCategory.UPPERCASE_LETTER to "Uppercase letter",
                CharCategory.LOWERCASE_LETTER to "Lowercase letter",
                CharCategory.TITLECASE_LETTER to "Titlecase letter",
                CharCategory.MODIFIER_LETTER to "Modifier letter",
                CharCategory.OTHER_LETTER to "Other letter",
                CharCategory.NON_SPACING_MARK to "Non-spacing mark",
                CharCategory.COMBINING_SPACING_MARK to "Combining mark",
                CharCategory.ENCLOSING_MARK to "Enclosing mark",
                CharCategory.DECIMAL_DIGIT_NUMBER to "Decimal digit",
                CharCategory.LETTER_NUMBER to "Letter number",
                CharCategory.OTHER_NUMBER to "Other number",
                CharCategory.SPACE_SEPARATOR to "Space separator",
                CharCategory.LINE_SEPARATOR to "Line separator",
                CharCategory.PARAGRAPH_SEPARATOR to "Paragraph separator",
                CharCategory.CONTROL to "Control character",
                CharCategory.FORMAT to "Format character",
                CharCategory.SURROGATE to "Surrogate",
                CharCategory.PRIVATE_USE to "Private use",
                CharCategory.CONNECTOR_PUNCTUATION to "Connector punctuation",
                CharCategory.DASH_PUNCTUATION to "Dash punctuation",
                CharCategory.START_PUNCTUATION to "Start punctuation",
                CharCategory.END_PUNCTUATION to "End punctuation",
                CharCategory.INITIAL_QUOTE to "Initial quote",
                CharCategory.FINAL_QUOTE to "Final quote",
                CharCategory.OTHER_PUNCTUATION to "Other punctuation",
                CharCategory.MATH_SYMBOL to "Math symbol",
                CharCategory.CURRENCY_SYMBOL to "Currency symbol",
                CharCategory.MODIFIER_SYMBOL to "Modifier symbol",
                CharCategory.OTHER_SYMBOL to "Other symbol"
            )
            
            val category = unicodeCategories.find { it.first == char.category }
            "${category?.second ?: "Character"} U+${String.format("%04X", char.code)}"
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
