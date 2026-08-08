package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.core.content.FileProvider
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeGeneratorScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    
    var textInput by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf(QRSize.MEDIUM) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    // Auto-generate when input changes (with debounce)
    LaunchedEffect(textInput, selectedSize) {
        if (textInput.isNotBlank()) {
            isGenerating = true
            val bitmap = generateQRCode(textInput, selectedSize.size)
            qrBitmap = bitmap
            isGenerating = false
        } else {
            qrBitmap = null
        }
    }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "QR Code Generator",
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Enter Content",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Text, URL, or any content") },
                        placeholder = { Text("https://example.com") },
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Size Selection
                    Text(
                        text = "QR Code Size",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QRSize.values().forEach { size ->
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                label = { 
                                    Text(size.displayName) 
                                },
                                leadingIcon = if (selectedSize == size) {
                                    { Text("✓") }
                                } else null
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code Display
            if (qrBitmap != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.size(selectedSize.size.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, textInput)
                                    showCopiedMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Text")
                            }

                            OutlinedButton(
                                onClick = {
                                    saveQRCodeToGallery(context, qrBitmap!!) { message ->
                                        saveMessage = message
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }

                            OutlinedButton(
                                onClick = {
                                    shareQRCode(context, qrBitmap!!)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }

                        if (showCopiedMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Text copied to clipboard!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }

                        if (saveMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = saveMessage!!,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(3000)
                                saveMessage = null
                            }
                        }
                    }
                }
            } else if (isGenerating) {
                Box(
                    modifier = Modifier
                        .size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Empty State
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Enter text above\nto generate QR code",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Templates
            Text(
                text = "Quick Templates",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                "https://google.com" to "Google URL",
                "tel:+1234567890" to "Phone Number",
                "mailto:example@email.com" to "Email Address",
                "WIFI:T:WPA;S:NetworkName;P:Password;;" to "Wi-Fi Config"
            ).forEach { (template, label) ->
                OutlinedButton(
                    onClick = { textInput = template },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(label, modifier = Modifier.weight(1f))
                    Text(template.take(25) + if (template.length > 25) "..." else "", 
                         style = MaterialTheme.typography.bodySmall,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Info Section
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "About QR Codes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = """
                            QR Codes can store various types of data:
                            
                            • URLs - Opens in browser when scanned
                            • Phone Numbers - Initiates call
                            • Email Addresses - Creates new email
                            • Wi-Fi Credentials - Connects to network
                            • Plain Text - Displays the text
                            • vCard - Contact information
                            
                            The generated QR code uses error correction level H (High), which allows recovery even if up to 30% of the code is damaged.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class QRSize(val displayName: String, val size: Int) {
    SMALL("Small", 256),
    MEDIUM("Medium", 400),
    LARGE("Large", 600)
}

private suspend fun generateQRCode(content: String, size: Int): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val writer = QRCodeWriter()
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2
            )
            
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("QR Content", text)
    clipboardManager.setPrimaryClip(clip)
}

private fun saveQRCodeToGallery(context: Context, bitmap: Bitmap, onResult: (String) -> Unit) {
    try {
        val filename = "qrcode_${System.currentTimeMillis()}.png"
        
        // Save to app's external files directory
        val directory = File(context.getExternalFilesDir(null), "QRCodes")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        
        val file = File(directory, filename)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.flush()
        fos.close()
        
        onResult("Saved to: ${file.absolutePath}")
        
        // Also trigger media scan so it appears in gallery
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = android.net.Uri.fromFile(file)
        context.sendBroadcast(intent)
    } catch (e: Exception) {
        onResult("Error saving: ${e.message}")
    }
}

private fun shareQRCode(context: Context, bitmap: Bitmap) {
    try {
        // Save to cache first
        val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(cachePath, "shared_qrcode.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        
        // Get URI using FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        // Fallback - just share as text
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this QR code!")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share"))
    }
}