package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WiFiQRGeneratorScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedSecurity by remember { mutableStateOf(WiFiSecurity.WPA) }
    var isHiddenNetwork by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var wifiString by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    // Auto-generate when inputs change
    LaunchedEffect(ssid, password, selectedSecurity, isHiddenNetwork) {
        if (ssid.isNotBlank()) {
            val wifiStr = generateWiFiString(
                ssid = ssid,
                password = password,
                security = selectedSecurity,
                hidden = isHiddenNetwork
            )
            wifiString = wifiStr
            
            isGenerating = true
            val bitmap = generateQRCode(wifiStr)
            qrBitmap = bitmap
            isGenerating = false
        } else {
            qrBitmap = null
            wifiString = null
        }
    }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "Wi-Fi QR Generator",
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
                        text = "Wi-Fi Network Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // SSID (Network Name)
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text("Network Name (SSID)") },
                        placeholder = { Text("MyWiFiNetwork") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Wifi, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = ssid.isBlank() && qrBitmap != null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { 
                            Text(
                                if (selectedSecurity == WiFiSecurity.NONE) "Password (optional)" else "Password"
                            )
                        },
                        placeholder = { Text("••••••••") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedSecurity != WiFiSecurity.NONE
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security Type Selection
                    Text(
                        text = "Security Type",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        WiFiSecurity.values().forEach { security ->
                            FilterChip(
                                selected = selectedSecurity == security,
                                onClick = { selectedSecurity = security },
                                label = { Text(security.displayName) },
                                leadingIcon = if (selectedSecurity == security) {
                                    { Text("✓") }
                                } else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hidden Network Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hidden Network",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Switch(
                            checked = isHiddenNetwork,
                            onCheckedChange = { isHiddenNetwork = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Enable this if the network doesn't broadcast its SSID",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code Display
            if (qrBitmap != null && wifiString != null) {
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
                            contentDescription = "Wi-Fi QR Code",
                            modifier = Modifier.size(280.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Wi-Fi String Display
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = wifiString!!,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    ),
                                    maxLines = 2,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(onClick = {
                                    copyToClipboard(context, wifiString!!)
                                    showCopiedMessage = true
                                }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Wi-Fi string",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        if (showCopiedMessage) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Wi-Fi string copied!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    saveQRCodeToGallery(context, qrBitmap!!, "wifi_${ssid}") { message ->
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
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Empty State
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Enter Wi-Fi details above\nto generate QR code",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How to Use Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How to Use This QR Code",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    listOf(
                        "1. Print or display this QR code" to "Share it with guests who need Wi-Fi access",
                        "2. Open camera app on any device" to "Point it at the QR code",
                        "3. Tap the notification" to "Connect automatically without typing the password"
                    ).forEach { (step, desc) ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Supported Devices Info
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
                        text = "Supported Devices & Apps",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = """
                            • Android: Camera app (Android 8+), Google Lens
                            • iOS: Camera app (iOS 11+), Code Scanner
                            • Windows: Camera app, QR Code Reader apps
                            • macOS: Camera app (macOS Ventura+)
                            
                            Most modern smartphones can scan Wi-Fi QR codes natively.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Security Notice
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ Security Notice",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "This QR code contains your Wi-Fi password in plain text. Only share it with people you trust. Don't post it publicly online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

enum class WiFiSecurity(val displayName: String, val protocolCode: String) {
    WPA("WPA/WPA2", "WPA"),
    WEP("WEP", "WEP"),
    NONE("None", "nopass");
}

private fun generateWiFiString(
    ssid: String,
    password: String,
    security: WiFiSecurity,
    hidden: Boolean
): String {
    return buildString {
        append("WIFI:S:")
        append(escapeSpecialChars(ssid))
        append(";T:")
        append(security.protocolCode)
        
        if (security != WiFiSecurity.NONE && password.isNotBlank()) {
            append(";P:")
            append(escapeSpecialChars(password))
        }
        
        if (hidden) {
            append(";H:true")
        }
        
        append(";;")
    }
}

private fun escapeSpecialChars(input: String): String {
    return input
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace(":", "\\:")
}

private suspend fun generateQRCode(content: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val writer = QRCodeWriter()
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2
            )
            
            val size = 600
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
    val clip = android.content.ClipData.newPlainText("Wi-Fi Config", text)
    clipboardManager.setPrimaryClip(clip)
}

private fun saveQRCodeToGallery(context: Context, bitmap: Bitmap, prefix: String, onResult: (String) -> Unit) {
    try {
        val filename = "${prefix}_${System.currentTimeMillis()}.png"
        
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
        
        // Trigger media scan
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data = android.net.Uri.fromFile(file)
        context.sendBroadcast(intent)
    } catch (e: Exception) {
        onResult("Error saving: ${e.message}")
    }
}

private fun shareQRCode(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(cachePath, "shared_wifi_qrcode.png")
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
        
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
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Wi-Fi QR Code"))
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Here's my Wi-Fi QR code!")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share"))
    }
}