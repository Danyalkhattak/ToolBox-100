package com.dannyk.toolbox.ui.screens.tools.text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar

data class CaseConversion(
    val name: String,
    val convertedText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseConverterScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var copiedIndex by remember { mutableIntStateOf(-1) }
    
    // Generate all case conversions
    val conversions: List<CaseConversion> = remember(inputText) {
        listOf(
            CaseConversion("UPPERCASE", toUpperCase(inputText)),
            CaseConversion("lowercase", toLowerCase(inputText)),
            CaseConversion("Title Case", toTitleCase(inputText)),
            CaseConversion("Sentence case", toSentenceCase(inputText)),
            CaseConversion("camelCase", toCamelCase(inputText)),
            CaseConversion("snake_case", toSnakeCase(inputText)),
            CaseConversion("kebab-case", toKebabCase(inputText)),
            CaseConversion("CONSTANT_CASE", toConstantCase(inputText)),
            CaseConversion("tOGGLE cASE", toToggleCase(inputText))
        )
    }

    Scaffold(
        topBar = { ToolTopBar("Case Converter", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Text(
                text = "Enter text to convert:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type or paste your text here...") },
                textStyle = TextStyle(fontSize = 16.sp, fontFamily = FontFamily.Monospace),
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            HorizontalDivider()

            // Conversion Results
            if (inputText.isNotBlank()) {
                Text(
                    text = "Converted Results",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                conversions.forEachIndexed { index, conversion ->
                    ConversionCard(
                        conversion = conversion,
                        isCopied = copiedIndex == index,
                        onCopy = {
                            copyToClipboard(context, conversion.convertedText)
                            copiedIndex = index
                        }
                    )
                }
            } else {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Enter text above to see conversions",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Supports 9 different case formats",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { inputText = "" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { 
                        if (inputText.isNotEmpty()) {
                            copyToClipboard(context, inputText)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = inputText.isNotEmpty()
                ) {
                    Text("Copy Original")
                }
            }
        }
    }
}

// Conversion Functions
private fun toUpperCase(text: String): String = text.uppercase()

private fun toLowerCase(text: String): String = text.lowercase()

private fun toTitleCase(text: String): String {
    return text.split(" ").joinToString(" ") { word ->
        if (word.isEmpty()) word
        else word[0].uppercase() + word.drop(1).lowercase()
    }
}

private fun toSentenceCase(text: String): String {
    if (text.isEmpty()) return text
    var result = text.lowercase()
    // Capitalize first letter
    result = result[0].uppercase() + result.drop(1)
    // Capitalize after sentence terminators
    val terminators = listOf('.', '!', '?')
    var i = 0
    while (i < result.length - 1) {
        if (result[i] in terminators) {
            // Find next letter after whitespace
            var j = i + 1
            while (j < result.length && !result[j].isLetter()) j++
            if (j < result.length) {
                result = result.substring(0, j) + 
                         result[j].uppercase() + 
                         result.substring(j + 1)
            }
        }
        i++
    }
    return result
}

private fun toCamelCase(text: String): String {
    // Remove special characters and split by spaces/underscores/dashes
    val words = text.split(Regex("[\\s_\\-]+"))
        .filter { it.isNotEmpty() }
    
    if (words.isEmpty()) return ""
    
    return words.mapIndexed { index, word ->
        when (index) {
            0 -> word.lowercase()
            else -> word[0].uppercase() + word.drop(1).lowercase()
        }
    }.joinToString("")
}

private fun toSnakeCase(text: String): String {
    // Convert to lowercase and replace spaces/special chars with underscores
    return text.trim()
        .replace(Regex("[A-Z]")) { "_${it.value.lowercase()}" }
        .replace(Regex("[^a-zA-Z0-9]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')
        .lowercase()
}

private fun toKebabCase(text: String): String {
    // Convert to lowercase and replace spaces/special chars with dashes
    return text.trim()
        .replace(Regex("[A-Z]")) { "-${it.value.lowercase()}" }
        .replace(Regex("[^a-zA-Z0-9]"), "-")
        .replace(Regex("-+"), "-")
        .trim('-')
        .lowercase()
}

private fun toConstantCase(text: String): String {
    // Similar to snake case but all uppercase
    return toSnakeCase(text).uppercase()
}

private fun toToggleCase(text: String): String {
    return text.map { char ->
        if (char.isUpperCase()) char.lowercase()
        else if (char.isLowerCase()) char.uppercase()
        else char.toString()
    }.joinToString("")
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("converted_text", text)
    clipboard.setPrimaryClip(clip)
}

@Composable
private fun ConversionCard(
    conversion: CaseConversion,
    isCopied: Boolean,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header row with name and copy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = conversion.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                // Character count
                Text(
                    text = "${conversion.convertedText.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Spacer(Modifier.width(8.dp))
                
                // Copy button
                FilledTonalButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) 
                            androidx.compose.material.icons.Icons.Default.Check else 
                            androidx.compose.material.icons.Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isCopied) "Copied!" else "Copy")
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Converted text display
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (conversion.convertedText.isEmpty()) "(empty)" 
                          else conversion.convertedText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(12.dp),
                    maxLines = 4,
                    color = if (conversion.convertedText.isEmpty()) 
                        MaterialTheme.colorScheme.outline else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
