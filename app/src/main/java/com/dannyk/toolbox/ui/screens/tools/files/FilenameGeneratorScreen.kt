package com.dannyk.toolbox.ui.screens.tools.files

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import java.util.regex.Pattern
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.ScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilenameGeneratorScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    // Base name input
    var baseName by remember { mutableStateOf("document") }
    
    // Extension input
    var extension by remember { mutableStateOf("txt") }
    
    // Pattern options
    var includeTimestamp by remember { mutableStateOf(true) }
    var timestampFormat by remember { mutableStateOf("YYYYMMDD_HHMMSS") }
    var includeRandomString by remember { mutableStateOf(false) }
    var randomStringLength by remember { mutableStateOf("6") }
    var includeSequentialNumber by remember { mutableStateOf(false) }
    var sequentialNumber by remember { mutableStateOf("001") }
    var useCustomTemplate by remember { mutableStateOf(false) }
    var customTemplate by remember { mutableStateOf("{base}_{timestamp}.{ext}") }
    
    // Generated filenames
    var generatedFilename by remember { mutableStateOf("") }
    var bulkFilenames by remember { mutableStateOf<List<String>>(emptyList()) }
    var bulkCount by remember { mutableStateOf("5") }

    // Generate filename on state changes
    LaunchedEffect(baseName, extension, includeTimestamp, timestampFormat, includeRandomString, 
             randomStringLength, includeSequentialNumber, sequentialNumber, useCustomTemplate, customTemplate) {
        generatedFilename = generateFilename(
            baseName = baseName,
            extension = extension,
            includeTimestamp = includeTimestamp,
            timestampFormat = timestampFormat,
            includeRandomString = includeRandomString,
            randomLength = randomStringLength.toIntOrNull() ?: 6,
            includeSequentialNumber = includeSequentialNumber,
            sequenceNumber = sequentialNumber.toIntOrNull() ?: 1,
            useCustomTemplate = useCustomTemplate,
            customTemplate = customTemplate
        )
    }

    ToolScreenLayout(
        title = "Filename Generator",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Base Name Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Base Name",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = baseName,
                        onValueChange = { baseName = it },
                        label = { Text("Enter base filename") },
                        placeholder = { Text("e.g., report, image, backup") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Extension Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "File Extension",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ".",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        OutlinedTextField(
                            value = extension,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { c -> c.isLetterOrDigit() || c == '_' }) {
                                    extension = it.lowercase()
                                }
                            },
                            label = { Text("Extension") },
                            placeholder { Text("txt") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Quick extension suggestions
                    val commonExtensions = listOf("txt", "pdf", "jpg", "png", "docx", "xlsx", "csv", "json", "xml", "html")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        commonExtensions.forEach { ext ->
                            FilterChip(
                                selected = extension == ext,
                                onClick = { extension = ext },
                                label = { Text(ext, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            // Pattern Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pattern Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Timestamp Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = includeTimestamp,
                            onCheckedChange = { includeTimestamp = it }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Include Timestamp")
                            Text(
                                text = "Add date/time to filename",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (includeTimestamp) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Timestamp Format",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        val timestampFormats = listOf(
                            "YYYYMMDD_HHMMSS" to "20240115_143022",
                            "YYYY-MM-DD_HH-MM-SS" to "2024-01-15_14-30-22",
                            "YYYYMMDD" to "20240115",
                            "DDMMYYYY" to "15012024",
                            "HHMMSS" to "143022",
                            "Unix Timestamp" to "1705327822"
                        )
                        
                        timestampFormats.forEach { (format, example) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = timestampFormat == format,
                                    onClick = { timestampFormat = format }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = format, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                Text(
                                    text = example,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Random String Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = includeRandomString,
                            onCheckedChange = { includeRandomString = it }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Include Random String")
                            Text(
                                text = "Add random characters for uniqueness",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (includeRandomString) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Length: ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Slider(
                                value = randomStringLength.toFloatOrNull()?.coerceIn(4f, 16f) ?: 6f,
                                onValueChange = { randomStringLength = it.toInt().toString() },
                                valueRange = 4f..16f,
                                steps = 11,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = randomStringLength,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Sequential Number Option
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = includeSequentialNumber,
                            onCheckedChange = { includeSequentialNumber = it }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Include Sequential Number")
                            Text(
                                text = "Add incrementing number (001, 002...)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (includeSequentialNumber) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = sequentialNumber,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.length <= 6)) {
                                    sequentialNumber = newValue.padStart(3, '0').takeLast(6)
                                }
                            },
                            label = { Text("Starting Number") },
                            modifier = Modifier.width(150.dp),
                            singleLine = true,
                            prefix = { Text("#") }
                        )
                    }
                }
            }

            // Custom Template Option
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = useCustomTemplate,
                            onCheckedChange = { useCustomTemplate = it }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Use Custom Template")
                    }
                    
                    if (useCustomTemplate) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = customTemplate,
                            onValueChange = { customTemplate = it },
                            label = { Text("Custom Template") },
                            placeholder { Text("{base}_{timestamp}_{random}.{ext}") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Available placeholders:\n" +
                                   "{base} - Base name\n" +
                                   "{ext} - File extension\n" +
                                   "{timestamp} - Formatted date/time\n" +
                                   "{date} - Date only (YYYYMMDD)\n" +
                                   "{time} - Time only (HHMMSS)\n" +
                                   "{random} - Random string\n" +
                                   "{seq} - Sequential number\n" +
                                   "{year}, {month}, {day}, {hour}, {min}, {sec}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Generated Result
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated Filename",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    generatedFilename = generateFilename(
                                        baseName = baseName,
                                        extension = extension,
                                        includeTimestamp = includeTimestamp,
                                        timestampFormat = timestampFormat,
                                        includeRandomString = includeRandomString,
                                        randomLength = randomStringLength.toIntOrNull() ?: 6,
                                        includeSequentialNumber = includeSequentialNumber,
                                        sequenceNumber = sequentialNumber.toIntOrNull() ?: 1,
                                        useCustomTemplate = useCustomTemplate,
                                        customTemplate = customTemplate
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                            }
                            
                            IconButton(
                                onClick = {
                                    copyToClipboard(context, generatedFilename)
                                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = generatedFilename.ifEmpty { "Enter a base name to generate..." },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (generatedFilename.isNotEmpty()) 
                                Color.Unspecified 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            copyToClipboard(context, generatedFilename)
                            Toast.makeText(context, "Copied: $generatedFilename", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Filename")
                    }
                }
            }

            // Bulk Generation Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bulk Generation",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generate ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        OutlinedTextField(
                            value = bulkCount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull()?.let { it in 1..100 } == true)) {
                                    bulkCount = newValue
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                        
                        Text(
                            text = " filenames",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            val count = bulkCount.toIntOrNull()?.coerceIn(1, 100) ?: 5
                            val startSeq = sequentialNumber.toIntOrNull() ?: 1
                            
                            bulkFilenames = (0 until count).map { index ->
                                generateFilename(
                                    baseName = baseName,
                                    extension = extension,
                                    includeTimestamp = includeTimestamp,
                                    timestampFormat = timestampFormat,
                                    includeRandomString = includeRandomString,
                                    randomLength = randomStringLength.toIntOrNull() ?: 6,
                                    includeSequentialNumber = includeSequentialNumber,
                                    sequenceNumber = startSeq + index,
                                    useCustomTemplate = useCustomTemplate,
                                    customTemplate = customTemplate
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Multiple Filenames")
                    }
                    
                    if (bulkFilenames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp)
                            ) {
                                bulkFilenames.forEachIndexed { index, filename ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.width(36.dp)
                                        )
                                        Text(
                                            text = filename,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        IconButton(
                                            onClick = {
                                                copyToClipboard(context, filename)
                                                Toast.makeText(context, "Copied: $filename", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    
                                    if (index < bulkFilenames.lastIndex) {
                                        Divider(modifier = Modifier.fillMaxWidth(), 
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = {
                                val allNames = bulkFilenames.joinToString("\n")
                                copyToClipboard(context, allNames)
                                Toast.makeText(context, "Copied ${bulkFilenames.size} filenames!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy All (${bulkFilenames.size})")
                        }
                    }
                }
            }

            // Tips / Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tips & Best Practices",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Use descriptive base names for easy identification\n" +
                               "• Include timestamps for version control and backups\n" +
                               "• Add random strings for temporary or unique files\n" +
                               "• Use sequential numbers for ordered file series\n" +
                               "• Avoid special characters in filenames\n" +
                               "• Keep filenames under 255 characters\n" +
                               "• Use consistent naming conventions across projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun generateFilename(
    baseName: String,
    extension: String,
    includeTimestamp: Boolean,
    timestampFormat: String,
    includeRandomString: Boolean,
    randomLength: Int,
    includeSequentialNumber: Boolean,
    sequenceNumber: Int,
    useCustomTemplate: Boolean,
    customTemplate: String
): String {
    if (useCustomTemplate) {
        return applyCustomTemplate(
            template = customTemplate,
            baseName = baseName,
            extension = extension,
            timestampFormat = timestampFormat,
            randomLength = randomLength,
            sequenceNumber = sequenceNumber
        )
    }
    
    val parts = mutableListOf<String>()
    
    // Add base name (if not empty)
    if (baseName.isNotEmpty()) {
        parts.add(sanitizeFilename(baseName))
    }
    
    // Add timestamp if enabled
    if (includeTimestamp) {
        parts.add(generateTimestamp(timestampFormat))
    }
    
    // Add random string if enabled
    if (includeRandomString) {
        parts.add(generateRandomString(randomLength))
    }
    
    // Add sequential number if enabled
    if (includeSequentialNumber) {
        parts.add(String.format("%03d", sequenceNumber))
    }
    
    // If no parts were added, return a default
    if (parts.isEmpty()) {
        parts.add("file")
    }
    
    // Combine parts and add extension
    val name = parts.joinToString("_")
    return if (extension.isNotEmpty()) "$name.$extension" else name
}

private fun applyCustomTemplate(
    template: String,
    baseName: String,
    extension: String,
    timestampFormat: String,
    randomLength: Int,
    sequenceNumber: Int
): String {
    val now = Calendar.getInstance()
    
    return template
        .replace("{base}", sanitizeFilename(baseName))
        .replace("{ext}", extension)
        .replace("{timestamp}", generateTimestamp(timestampFormat))
        .replace("{date}", SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now.time))
        .replace("{time}", SimpleDateFormat("HHmmss", Locale.getDefault()).format(now.time))
        .replace("{random}", generateRandomString(randomLength))
        .replace("{seq}", String.format("%03d", sequenceNumber))
        .replace("{year}", SimpleDateFormat("yyyy", Locale.getDefault()).format(now.time))
        .replace("{month}", SimpleDateFormat("MM", Locale.getDefault()).format(now.time))
        .replace("{day}", SimpleDateFormat("dd", Locale.getDefault()).format(now.time))
        .replace("{hour}", SimpleDateFormat("HH", Locale.getDefault()).format(now.time))
        .replace("{min}", SimpleDateFormat("mm", Locale.getDefault()).format(now.time))
        .replace("{sec}", SimpleDateFormat("ss", Locale.getDefault()).format(now.time))
}

private fun sanitizeFilename(name: String): String {
    // Remove/replace invalid characters
    val invalidChars = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
    return name.filter { it !in invalidChars }.trim().ifEmpty { "file" }
}

private fun generateTimestamp(format: String): String {
    val now = Calendar.getInstance()
    
    return when (format) {
        "YYYYMMDD_HHMMSS" -> {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now.time)
        }
        "YYYY-MM-DD_HH-MM-SS" -> {
            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(now.time)
        }
        "YYYYMMDD" -> {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now.time)
        }
        "DDMMYYYY" -> {
            SimpleDateFormat("ddMMyyyy", Locale.getDefault()).format(now.time)
        }
        "HHMMSS" -> {
            SimpleDateFormat("HHmmss", Locale.getDefault()).format(now.time)
        }
        "Unix Timestamp" -> {
            now.timeInMillis.div(1000).toString()
        }
        else -> {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now.time)
        }
    }
}

private fun generateRandomString(length: Int): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("filename", text)
    clipboard.setPrimaryClip(clip)
}
