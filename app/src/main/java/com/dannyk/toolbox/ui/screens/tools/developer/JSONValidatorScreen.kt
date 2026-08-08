package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.Context
import androidx.compose.material3.Divider
import java.util.regex.Pattern

@Composable
fun JSONValidatorScreen(navController: NavHostController) {
    var jsonInput by remember { mutableStateOf("") }
    var validationResult by remember { mutableStateOf<JsonValidationResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "JSON Validator",
            subtitle = "Validate JSON structure and analyze data",
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
                    Text("JSON Input", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { 
                            jsonInput = it
                            // Auto-validate as user types (debounced would be better but keeping it simple)
                        },
                        label = { Text("Paste or type your JSON here") },
                        placeholder = { 
                            Text(
                                "{\"key\": \"value\", \"array\": [1, 2, 3]}", 
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 250.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    PrimaryButton(
                        text = "Validate JSON",
                        onClick = {
                            validationResult = validateJson(jsonInput)
                        },
                        enabled = jsonInput.isNotBlank(),
                        icon = Icons.Default.Verified
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            jsonInput = ""
                            validationResult = null
                        }
                    )
                }
            }

            // Validation Result
            validationResult?.let { result ->
                if (result.isValid) {
                    // Valid JSON Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Valid JSON!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Your JSON is well-formed and valid", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Structure Summary
                    result.structureSummary?.let { summary ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Structure Analysis", style = MaterialTheme.typography.titleMedium)

                                InfoRow("Type", summary.type)
                                InfoRow("Depth", "${summary.maxDepth} levels")
                                InfoRow("Total Keys", "${summary.keyCount}")
                                if (summary.arrayLengths.isNotEmpty()) {
                                    InfoRow("Array Lengths", summary.arrayLengths.entries.joinToString(", ") { "[${it.key}]: ${it.value}" })
                                }
                                if (summary.topLevelKeys.isNotEmpty()) {
                                    InfoRow("Top-level Keys", summary.topLevelKeys.take(10).joinToString(", ") + if (summary.topLevelKeys.size > 10) "..." else "")
                                }
                                InfoRow("Character Count", "${jsonInput.length}")
                                InfoRow("Estimated Size", formatFileSize(jsonInput.toByteArray(Charsets.UTF_8).size.toLong()))
                            }
                        }
                    }
                } else {
                    // Invalid JSON Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Invalid JSON", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }

                            Divider()

                            // Error details
                            result.errorMessage?.let { error ->
                                Text("Error Message", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                                Surface(
                                    color = Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        error,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            result.errorPosition?.let { pos ->
                                InfoRow("Error Position", "Character $pos")
                                
                                // Show context around error
                                val startIdx = maxOf(0, pos - 30)
                                val endIdx = minOf(jsonInput.length, pos + 30)
                                val context = jsonInput.substring(startIdx, endIdx)
                                
                                Text("Context around error:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Surface(
                                    color = Color.Black.copy(alpha = 0.9f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        buildString {
                                            append(context.substring(0, minOf(pos - startIdx, context.length)))
                                            append("▌") // Cursor indicator
                                            append(context.substring(minOf(pos - startIdx, context.length)))
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            // Suggested fix
                            result.suggestedFix?.let { fix ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Suggested Fix", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(fix, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Common JSON Errors Reference
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Common JSON Errors", style = MaterialTheme.typography.titleSmall)

                    listOf(
                        "Trailing comma" to "Remove comma after last item in object/array",
                        "Unquoted keys" to "All string keys must be in double quotes",
                        "Single quotes" to "Use double quotes (\") not single quotes (')",
                        "Missing colon" to "Key-value pairs must be separated by :",
                        "Missing bracket/brace" to "Ensure all { } and [ ] are paired",
                        "Comments not allowed" to "JSON does not support /* */ or // comments"
                    ).forEach { (error, solution) ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• $error: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(solution, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

data class JsonValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val errorPosition: Int? = null,
    val suggestedFix: String? = null,
    val structureSummary: JsonStructureSummary? = null
)

data class JsonStructureSummary(
    val type: String,
    val maxDepth: Int,
    val keyCount: Int,
    val arrayLengths: Map<Int, Int>,
    val topLevelKeys: List<String>
)

private fun validateJson(json: String): JsonValidationResult {
    val trimmed = json.trim()
    
    if (trimmed.isEmpty()) {
        return JsonValidationResult(
            isValid = false,
            errorMessage = "Empty input",
            errorPosition = 0,
            suggestedFix = "Please enter some JSON to validate"
        )
    }

    try {
        when {
            trimmed.startsWith("{") -> {
                val obj = JSONObject(trimmed)
                val summary = analyzeJsonObject(obj)
                return JsonValidationResult(isValid = true, structureSummary = summary)
            }
            trimmed.startsWith("[") -> {
                val arr = JSONArray(trimmed)
                val summary = analyzeJsonArray(arr)
                return JsonValidationResult(isValid = true, structureSummary = summary)
            }
            else -> {
                // Try to parse as a primitive value
                when {
                    trimmed == "true" || trimmed == "false" -> return JsonValidationResult(isValid = true, structureSummary = JsonStructureSummary("Boolean", 1, 0, emptyMap(), emptyList()))
                    trimmed == "null" -> return JsonValidationResult(isValid = true, structureSummary = JsonStructureSummary("Null", 1, 0, emptyMap(), emptyList()))
                    trimmed.toDoubleOrNull() != null || trimmed.matches(Regex("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) -> return JsonValidationResult(isValid = true, structureSummary = JsonStructureSummary("Number", 1, 0, emptyMap(), emptyList()))
                    trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2 -> return JsonValidationResult(isValid = true, structureSummary = JsonStructureSummary("String", 1, 0, emptyMap(), emptyList()))
                    else -> throw JSONException("JSON must be an object, array, or valid primitive value")
                }
            }
        }
    } catch (e: JSONException) {
        val errorMsg = e.message ?: "Invalid JSON syntax"
        
        // Try to extract position from error message
        val position = extractErrorPosition(errorMsg)
        
        // Generate suggested fix based on common errors
        val suggestedFix = generateSuggestedFix(errorMsg, trimmed)
        
        return JsonValidationResult(
            isValid = false,
            errorMessage = errorMsg,
            errorPosition = position,
            suggestedFix = suggestedFix
        )
    }
}

private fun analyzeJsonObject(obj: JSONObject, depth: Int = 1): JsonStructureSummary {
    var maxDepth = depth
    var keyCount = obj.length()
    val topLevelKeys = mutableListOf<String>()
    val arrayLengths = mutableMapOf<Int, Int>()
    
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        if (depth == 1) topLevelKeys.add(key)
        
        val value = optValue(obj, key)
        when (value) {
            is JSONObject -> {
                val nested = analyzeJsonObject(value, depth + 1)
                maxDepth = maxOf(maxDepth, nested.maxDepth)
                keyCount += nested.keyCount
                arrayLengths.putAll(nested.arrayLengths)
            }
            is JSONArray -> {
                val nested = analyzeJsonArray(value, depth + 1)
                maxDepth = maxOf(maxDepth, nested.maxDepth)
                keyCount += nested.keyCount
                arrayLengths.putAll(nested.arrayLengths)
                arrayLengths[value.length()] = (arrayLengths[value.length()] ?: 0) + 1
            }
        }
    }
    
    return JsonStructureSummary("Object", maxDepth, keyCount, arrayLengths, topLevelKeys)
}

private fun analyzeJsonArray(arr: JSONArray, depth: Int = 1): JsonStructureSummary {
    var maxDepth = depth
    var keyCount = 0
    val arrayLengths = mutableMapOf<Int, Int>()
    
    for (i in 0 until arr.length()) {
        val value = optArrayElement(arr, i)
        when (value) {
            is JSONObject -> {
                val nested = analyzeJsonObject(value, depth + 1)
                maxDepth = maxOf(maxDepth, nested.maxDepth)
                keyCount += nested.keyCount
                arrayLengths.putAll(nested.arrayLengths)
            }
            is JSONArray -> {
                val nested = analyzeJsonArray(value, depth + 1)
                maxDepth = maxOf(maxDepth, nested.maxDepth)
                keyCount += nested.keyCount
                arrayLengths.putAll(nested.arrayLengths)
                arrayLengths[value.length()] = (arrayLengths[value.length()] ?: 0) + 1
            }
        }
    }
    
    arrayLengths[arr.length()] = (arrayLengths[arr.length()] ?: 0) + 1
    
    return JsonStructureSummary("Array", maxDepth, keyCount, arrayLengths, emptyList())
}

// Helper to safely get value from JSONObject
private fun optValue(obj: JSONObject, key: String): Any? {
    return try {
        when (obj.get(key)) {
            is JSONObject -> obj.getJSONObject(key)
            is JSONArray -> obj.getJSONArray(key)
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

// Helper to safely get element from JSONArray
private fun optArrayElement(arr: JSONArray, index: Int): Any? {
    return try {
        when (arr.get(index)) {
            is JSONObject -> arr.getJSONObject(index)
            is JSONArray -> arr.getJSONArray(index)
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

// Extract position from error message
private fun extractErrorPosition(errorMessage: String): Int? {
    // Pattern like "at character X of" or "at position X"
    val patterns = listOf(
        Regex("character\\s*(\\d+)"),
        Regex("position\\s*(\\d+)"),
        Regex("\\[.*?\\]\\s*(\\d+)")
    )
    
    for (pattern in patterns) {
        val match = pattern.find(errorMessage)
        if (match != null) {
            return match.groupValues[1].toIntOrNull()
        }
    }
    
    return null
}

// Generate suggested fix based on error message
private fun generateSuggestedFix(errorMessage: String, input: String): String {
    return when {
        errorMessage.contains("trailing", ignoreCase = true) -> 
            "Remove the trailing comma before the closing bracket or brace"
        errorMessage.contains("quote", ignoreCase = true) || errorMessage.contains("unquoted", ignoreCase = true) -> 
            "Ensure all strings are enclosed in double quotes (\")"
        errorMessage.contains("expected", ignoreCase = true) -> {
            when {
                errorMessage.contains("}", ignoreCase = true) -> "Add a closing brace } at the end of your object"
                errorMessage.contains("]", ignoreCase = true) -> "Add a closing bracket ] at the end of your array"
                errorMessage.contains("{", ignoreCase = true) -> "Add an opening brace { where expected"
                errorMessage.contains("[", ignoreCase = true) -> "Add an opening bracket [ where expected"
                errorMessage.contains(":", ignoreCase = true) -> "Add a colon : between key and value"
                else -> "Check for missing or extra punctuation marks"
            }
        }
        errorMessage.contains("literal", ignoreCase = true) -> 
            "Replace single quotes with double quotes or escape special characters properly"
        input.trim().startsWith("//") || input.trim().startsWith("/*") -> 
            "JSON does not support comments. Remove all comment lines"
        input.contains("'", ignoreCase = true) && !input.contains("\"") -> 
            "Replace all single quotes (') with double quotes (\")"
        else -> "Review your JSON syntax carefully against the JSON specification"
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
