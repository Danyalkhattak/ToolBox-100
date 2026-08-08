package com.dannyk.toolbox.ui.screens.tools.files

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToPDFScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Text input
    var textInput by remember { mutableStateOf(TextFieldValue("")) }
    
    // Font options
    var fontSize by remember { mutableStateOf("12") }
    var fontFamily by remember { mutableStateOf("Default") }
    var wordWrapEnabled by remember { mutableStateOf(true) }
    
    // Page setup
    var pageSize by remember { mutableStateOf("A4") }
    var marginSize by remember { mutableStateOf("Medium") }
    
    // Processing state
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }

    ToolScreenLayout(
        title = "Text to PDF",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enter Text",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${textInput.text.length} characters | ${textInput.text.lines().size} lines",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { 
                            Text("Enter or paste your text here...\n\nThis will be converted to a PDF document with your selected formatting options.") 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 150.dp, max = 300.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = androidx.compose.ui.unit.sp(14)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { textInput = TextFieldValue("") }
                        ) {
                            Text("Clear")
                        }
                        
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("text", textInput.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Text copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Paste from Clipboard")
                        }
                    }
                }
            }

            // Font Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Font Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Font Size
                    Text(
                        text = "Font Size (pt)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = fontSize.toFloatOrNull() ?: 12f,
                            onValueChange = { fontSize = it.toInt().toString() },
                            valueRange = 8f..24f,
                            steps = 15,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        OutlinedTextField(
                            value = fontSize,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull()?.let { it in 6..72 } == true)) {
                                    fontSize = newValue
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.width(70.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Font Family
                    Text(
                        text = "Font Family",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val fontFamilies = listOf("Default", "Monospace", "Sans Serif", "Serif")
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        fontFamilies.forEachIndexed { index, family ->
                            SegmentedButton(
                                selected = fontFamily == family,
                                onClick = { fontFamily = family },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = fontFamilies.size),
                                icon = {}
                            ) {
                                Text(family)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Word Wrap Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = wordWrapEnabled,
                            onCheckedChange = { wordWrapEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Enable Word Wrap")
                    }
                }
            }

            // Page Setup
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Page Setup",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Page Size
                    Text(
                        text = "Page Size",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("A4", "Letter", "Legal").forEach { size ->
                            FilterChip(
                                selected = pageSize == size,
                                onClick = { pageSize = size },
                                label = { Text(size) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Margins
                    Text(
                        text = "Margins",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Small", "Medium", "Large").forEach { margin ->
                            FilterChip(
                                selected = marginSize == margin,
                                onClick = { marginSize = margin },
                                label = { Text(margin) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    if (textInput.text.isBlank()) {
                        Toast.makeText(context, "Please enter some text first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isProcessing = true
                    
                    scope.launch {
                        try {
                            val pdfFile = generateTextPdf(
                                text = textInput.text,
                                fontSize = fontSize.toIntOrNull() ?: 12,
                                fontFamily = fontFamily,
                                wordWrap = wordWrapEnabled,
                                pageSize = pageSize,
                                marginSize = marginSize
                            )
                            
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                showSuccessMessage = "PDF saved: ${pdfFile.name}"
                                
                                Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = textInput.text.isNotBlank() && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isProcessing) "Generating..." else "Generate PDF")
            }

            // Success Message
            showSuccessMessage?.let { message ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showSuccessMessage = null }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How to use:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Enter or paste the text you want to convert\n" +
                               "• Choose font size and family\n" +
                               "• Enable/disable word wrap\n" +
                               "• Select page size and margins\n" +
                               "• Tap 'Generate PDF' to create document\n" +
                               "• The PDF will be saved in Downloads folder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private suspend fun generateTextPdf(
    text: String,
    fontSize: Int,
    fontFamily: String,
    wordWrap: Boolean,
    pageSize: String,
    marginSize: String
): File = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()
    
    // Page dimensions in points (1/72 inch)
    val pageInfo = when (pageSize.uppercase()) {
        "LETTER" -> Pair(612, 792) // 8.5 x 11 inches
        "LEGAL" -> Pair(612, 1008) // 8.5 x 14 inches
        else -> Pair(595, 842) // A4: 210 x 297 mm
    }
    
    // Margin sizes in points
    val margins = when (marginSize.lowercase()) {
        "none" -> 0
        "small" -> 18
        "large" -> 72
        else -> 36 // Medium
    }
    
    val pageWidth = pageInfo.first
    val pageHeight = pageInfo.second
    
    // Content area
    val contentWidth = pageWidth - (margins * 2)
    val contentHeight = pageHeight - (margins * 2)
    
    // Paint for text
    val paint = Paint().apply {
        this.fontSize = fontSize.toFloat()
        color = Color.BLACK
        isAntiAlias = true
        
        typeface = when (fontFamily.lowercase()) {
            "monospace" -> Typeface.MONOSPACE
                            "serif" -> Typeface.SERIF
                            "sans serif" -> Typeface.SANS_SERIF
                            else -> Typeface.DEFAULT
        }
    }
    
    // Calculate line height
    val lineHeight = (fontSize * 1.4).toFloat()
    
    // Process text into lines
    val lines = if (wordWrap) {
        wrapText(text, paint, contentWidth)
    } else {
        text.split("\n")
    }
    
    // Create pages and draw text
    var currentLineIndex = 0
    var pageNumber = 0
    
    while (currentLineIndex < lines.size) {
        val pageInfoObj = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = pdfDocument.startPage(pageInfoObj)
        val canvas = page.canvas
        
        var yPosition = margins + fontSize.toFloat()
        
        while (currentLineIndex < lines.size && yPosition + lineHeight <= pageHeight - margins) {
            canvas.drawText(lines[currentLineIndex], margins.toFloat(), yPosition, paint)
            yPosition += lineHeight
            currentLineIndex++
        }
        
        pdfDocument.finishPage(page)
        pageNumber++
    }
    
    // Save PDF file
    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
    val outputFile = File(downloadsDir, "text_${System.currentTimeMillis()}.pdf")
    
    FileOutputStream(outputFile).use { outputStream ->
        pdfDocument.writeTo(outputStream)
    }
    
    pdfDocument.close()
    
    outputFile
}

private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
    val lines = mutableListOf<String>()
    val paragraphs = text.split("\n")
    
    for (paragraph in paragraphs) {
        if (paragraph.isEmpty()) {
            lines.add("")
            continue
        }
        
        var currentLine = StringBuilder()
        var currentWord = StringBuilder()
        
        for (char in paragraph) {
            if (char == ' ') {
                val testLine = if (currentLine.isEmpty()) currentWord.toString() 
                                else "$currentLine $currentWord"
                
                if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(currentWord.toString())
                } else {
                    currentLine = StringBuilder(testLine)
                }
                currentWord = StringBuilder()
            } else {
                currentWord.append(char)
            }
        }
        
        // Add last word
        if (currentWord.isNotEmpty()) {
            val testLine = if (currentLine.isEmpty()) currentWord.toString() 
                            else "$currentLine $currentWord"
            
            if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                lines.add(currentWord.toString())
            } else {
                lines.add(testLine)
            }
        }
    }
    
    return lines.ifEmpty { listOf("") }
}
