package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun URLParserScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var urlInput by remember { mutableStateOf("") }
    var parsedURL by remember { mutableStateOf<ParsedURL?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    // Auto-parse when input changes and looks like a valid URL
    LaunchedEffect(urlInput) {
        if (urlInput.isNotBlank()) {
            val result = parseURL(urlInput.trim())
            if (result != null) {
                parsedURL = result
                validationError = null
            } else if (urlInput.contains(".") || urlInput.contains("://")) {
                // Only show error for URL-like inputs
                validationError = "Invalid URL format"
                parsedURL = null
            } else {
                parsedURL = null
                validationError = null
            }
        } else {
            parsedURL = null
            validationError = null
        }
    }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "URL Parser",
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
                        text = "Enter URL to Parse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("URL") },
                        placeholder = { Text("https://example.com/path?key=value#section") },
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (validationError != null) {
                            Text(
                                text = validationError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                        } else if (parsedURL != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "✓ Valid URL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(onClick = {
                            copyToClipboard(context, urlInput)
                            showCopiedMessage = true
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                        
                        if (showCopiedMessage) {
                            Text(
                                text = "Copied!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }
                    }
                }
            }

            // Parsed Results
            if (parsedURL != null) {
                val url = parsedURL!!
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Parsed Components",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Basic Components
                URLComponentCard(
                    label = "Protocol / Scheme",
                    value = url.protocol,
                    icon = "🔒",
                    context = context
                )

                URLComponentCard(
                    label = "Host / Domain",
                    value = url.host,
                    icon = "🌐",
                    context = context
                )

                if (url.port != -1 && url.port != getDefaultPort(url.protocol)) {
                    URLComponentCard(
                        label = "Port",
                        value = "${url.port}",
                        icon = "🚪",
                        context = context
                    )
                }

                if (!url.userInfo.isNullOrBlank()) {
                    URLComponentCard(
                        label = "User Info",
                        value = url.userInfo,
                        icon = "👤",
                        isSensitive = true,
                        context = context
                    )
                    
                    // Parse username/password separately
                    val parts = url.userInfo.split(":")
                    if (parts.size >= 1) {
                        URLComponentCard(
                            label = "  Username",
                            value = parts[0],
                            icon = "",
                            isSensitive = true,
                            context = context
                        )
                    }
                    if (parts.size >= 2) {
                        URLComponentCard(
                            label = "  Password",
                            value = "*".repeat(parts[1].length),
                            icon = "",
                            isSensitive = true,
                            context = context
                        )
                    }
                }

                if (url.path.isNotBlank() && url.path != "/") {
                    URLComponentCard(
                        label = "Path",
                        value = url.path,
                        icon = "📁",
                        context = context
                    )
                }

                // Query Parameters
                if (url.queryParams.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Query Parameters (${url.queryParams.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    url.queryParams.forEachIndexed { index, param ->
                        QueryParamCard(
                            key = param.key,
                            value = param.value,
                            decodedKey = param.decodedKey,
                            decodedValue = param.decodedValue,
                            index = index + 1,
                            context = context
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // Fragment
                if (!url.fragment.isNullOrBlank()) {
                    URLComponentCard(
                        label = "Fragment / Hash",
                        value = "#${url.fragment}",
                        icon = "🔗",
                        context = context
                    )
                }

                // Full URL Reconstruction
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            text = "Reconstructed URL",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = url.fullURL,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2
                                )
                                
                                IconButton(onClick = {
                                    copyToClipboard(context, url.fullURL)
                                }) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy full URL"
                                    )
                                }
                            }
                        }
                    }
                }

                // Encoded/Decoded comparison
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Encoding Info",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        DetailRow("Original", urlInput, context)
                        DetailRow("Encoded", try { URLEncoder.encode(urlInput, "UTF-8") } catch (_: Exception) { "N/A" }, context)
                    }
                }
            }

            // Empty State
            if (parsedURL == null && validationError == null && urlInput.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "URL Parser Tool",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enter a URL to parse its components including protocol, host, path, query parameters, and fragment.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            // URL Format Reference
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
                        text = "URL Structure Reference",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = """
                                scheme://[user:pass@]host[:port][/path][?query][#fragment]
                                
                                Examples:
                                https://www.example.com:443/path/to/page?key=value&foo=bar#section
                                ftp://user:pass@ftp.example.com:21/files/
                                mailto:user@example.com
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun URLComponentCard(
    label: String,
    value: String?,
    icon: String,
    isSensitive: Boolean = false,
    context: Context,
    modifier: Modifier = Modifier
) {
    if (value.isNullOrBlank()) return
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon.isNotBlank()) {
                Text(text = icon, modifier = Modifier.width(32.dp))
            }
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(130.dp)
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { copyToClipboard(context, value) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun QueryParamCard(
    key: String,
    value: String,
    decodedKey: String,
    decodedValue: String,
    index: Int,
    context: Context
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$index.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(30.dp)
                )
                
                Text(
                    text = key,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { copyToClipboard(context, "$key=$value") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy parameter",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 30.dp)
            ) {
                Text(
                    text = "=",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(20.dp)
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Show decoded values if different from encoded
            if (decodedKey != key || decodedValue != value) {
                Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Decoded:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "$decodedKey = $decodedValue",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = { copyToClipboard(context, value) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

data class ParsedURL(
    val protocol: String,
    val userInfo: String?,
    val host: String,
    val port: Int,
    val path: String,
    val queryParams: List<QueryParam>,
    val fragment: String?,
    val fullURL: String
)

data class QueryParam(
    val key: String,
    val value: String,
    val decodedKey: String,
    val decodedValue: String
)

private fun parseURL(urlString: String): ParsedURL? {
    return try {
        var processedUrl = urlString
        
        // Add protocol if missing
        if (!processedUrl.startsWith("http://") && 
            !processedUrl.startsWith("https://") &&
            !processedUrl.startsWith("ftp://") &&
            !processedUrl.startsWith("mailto:") &&
            !processedUrl.startsWith("file://")
        ) {
            // Check if it looks like a domain
            if (processedUrl.contains(".") || processedUrl.contains(":")) {
                processedUrl = "https://$processedUrl"
            }
        }
        
        val url = URL(processedUrl)
        
        // Parse query parameters
        val queryParams = mutableListOf<QueryParam>()
        url.query?.split("&")?.forEach { param ->
            val parts = param.split("=", limit = 2)
            if (parts.isNotEmpty()) {
                val rawKey = parts.getOrNull(0) ?: ""
                val rawValue = parts.getOrElse(1) { "" }
                
                val decodedKey = try { URLDecoder.decode(rawKey, "UTF-8") } catch (_: Exception) { rawKey }
                val decodedValue = try { URLDecoder.decode(rawValue, "UTF-8") } catch (_: Exception) { rawValue }
                
                queryParams.add(QueryParam(rawKey, rawValue, decodedKey, decodedValue))
            }
        }
        
        ParsedURL(
            protocol = url.protocol,
            userInfo = url.userInfo?.takeIf { it.isNotBlank() },
            host = url.host,
            port = url.port,
            path = url.path ?: "/",
            queryParams = queryParams,
            fragment = url.ref?.takeIf { it.isNotBlank() },
            fullURL = url.toString()
        )
    } catch (e: MalformedURLException) {
        null
    } catch (e: Exception) {
        null
    }
}

private fun getDefaultPort(protocol: String): Int {
    return when (protocol.lowercase()) {
        "http" -> 80
        "https" -> 443
        "ftp" -> 21
        "ftps" -> 990
        "ssh" -> 22
        "smtp" -> 25
        "pop3" -> 110
        "imap" -> 143
        else -> -1
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("URL Component", text)
    clipboardManager.setPrimaryClip(clip)
}
