package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.text.font.FontWeight
import android.content.ClipboardManager
import android.content.ClipData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HTTPStatusScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var urlInput by remember { mutableStateOf("https://google.com") }
    var isLoading by remember { mutableStateOf(false) }
    var httpResult by remember { mutableStateOf<HTTPCheckResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "HTTP Status Checker",
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
                        text = "Enter URL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("URL to check") },
                        placeholder = { Text("https://example.com") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Http, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (urlInput.isNotBlank()) {
                                scope.launch {
                                    checkHTTPStatus(
                                        urlString = urlInput.trim(),
                                        onLoading = { isLoading = it },
                                        onSuccess = { result ->
                                            httpResult = result
                                            errorMessage = null
                                        },
                                        onError = { message ->
                                            errorMessage = message
                                            httpResult = null
                                        }
                                    )
                                }
                            }
                        },
                        enabled = urlInput.isNotBlank() && !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isLoading) "Checking..." else "Check Status")
                    }
                }
            }

            // Error Message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Results Section
            if (httpResult != null) {
                val result = httpResult!!
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Status Code Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = getStatusColor(result.statusCode).copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HTTP Status Code",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${result.statusCode}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = getStatusColor(result.statusCode)
                            )
                            
                            IconButton(onClick = {
                                copyToClipboard(context, "${result.statusCode}")
                                showCopiedMessage = true
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy status code"
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.statusMessage ?: getStatusText(result.statusCode),
                            style = MaterialTheme.typography.titleMedium,
                            color = getStatusColor(result.statusCode)
                        )

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

                Spacer(modifier = Modifier.height(16.dp))

                // Response Time & Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        title = "Response Time",
                        value = "${result.responseTime}ms",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        title = "Content Length",
                        value = formatBytes(result.contentLength),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // URL Info
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Request Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        DetailRow("Final URL", result.finalUrl, context)
                        DetailRow("Protocol", result.protocol, context)
                        DetailRow("Encoding", result.encoding, context)
                        DetailRow("Content Type", result.contentType, context)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Response Headers
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Response Headers",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${result.headers.size} headers",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        result.headers.forEach { (key, value) ->
                            HeaderItem(key = key, value = value, context = context)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Empty State
            if (!isLoading && httpResult == null && errorMessage == null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Http,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "HTTP Status Checker",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enter a URL to check its HTTP status code, response time, and response headers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            // Common Status Codes Reference
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
                        text = "Common HTTP Status Codes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    listOf(
                        Triple(200, "OK", "Request succeeded"),
                        Triple(201, "Created", "Resource created"),
                        Triple(204, "No Content", "Success with no body"),
                        Triple(301, "Moved Permanently", "Permanent redirect"),
                        Triple(302, "Found", "Temporary redirect"),
                        Triple(304, "Not Modified", "Resource unchanged"),
                        Triple(400, "Bad Request", "Invalid request"),
                        Triple(401, "Unauthorized", "Authentication required"),
                        Triple(403, "Forbidden", "Access denied"),
                        Triple(404, "Not Found", "Resource not found"),
                        Triple(500, "Internal Server Error", "Server error"),
                        Triple(502, "Bad Gateway", "Invalid upstream response"),
                        Triple(503, "Service Unavailable", "Server overloaded")
                    ).forEach { (code, name, desc) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$code",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                color = getStatusColor(code),
                                modifier = Modifier.width(50.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(130.dp)
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String?,
    context: Context,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        
        val displayValue = value ?: "N/A"
        
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            if (value != null) {
                IconButton(
                    onClick = { copyToClipboard(context, value) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderItem(
    key: String,
    value: String,
    context: Context
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(120.dp)
            )
            
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { copyToClipboard(context, "$key: $value") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy header",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class HTTPCheckResult(
    val statusCode: Int,
    val statusMessage: String?,
    val finalUrl: String,
    val protocol: String,
    val encoding: String?,
    val contentType: String?,
    val contentLength: Long,
    val responseTime: Long,
    val headers: Map<String, String>
)

private suspend fun checkHTTPStatus(
    urlString: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: (HTTPCheckResult) -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            onLoading(true)
            
            // Ensure URL has protocol
            var processedUrl = urlString.trim()
            if (!processedUrl.startsWith("http://") && !processedUrl.startsWith("https://")) {
                processedUrl = "https://$processedUrl"
            }
            
            val url = URL(processedUrl)
            val startTime = System.currentTimeMillis()
            
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "HEAD" // Use HEAD to avoid downloading body
            
            try {
                val statusCode = connection.responseCode
                val endTime = System.currentTimeMillis()
                
                // Collect headers
                val headers = mutableMapOf<String, String>()
                for ((key, values) in connection.headerFields) {
                    if (key != null && values.isNotEmpty()) {
                        headers[key] = values.joinToString("; ")
                    }
                }
                
                val result = HTTPCheckResult(
                    statusCode = statusCode,
                    statusMessage = connection.responseMessage,
                    finalUrl = connection.url.toString(),
                    protocol = "HTTP/${if (url.protocol == "https") "1.1/2.0 (TLS)" else "1.1"}",
                    encoding = connection.contentEncoding,
                    contentType = connection.contentType,
                    contentLength = connection.contentLengthLong,
                    responseTime = endTime - startTime,
                    headers = headers.toMap()
                )
                
                onLoading(false)
                withContext(Dispatchers.Main) {
                    onSuccess(result)
                }
            } finally {
                connection.disconnect()
            }
            
        } catch (e: Exception) {
            onLoading(false)
            withContext(Dispatchers.Main) {
                onError("Failed to check URL: ${e.message}")
            }
        }
    }
}

private fun getStatusColor(statusCode: Int): Color {
    return when (statusCode) {
        in 200..299 -> Color(0xFF4CAF50) // Green - Success
        in 300..399 -> Color(0xFF2196F3) // Blue - Redirect
        in 400..499 -> Color(0xFFFF9800) // Orange - Client Error
        in 500..599 -> Color(0xFFF44336) // Red - Server Error
        else -> Color.Gray
    }
}

private fun getStatusText(statusCode: Int): String {
    return when (statusCode) {
        200 -> "OK"
        201 -> "Created"
        204 -> "No Content"
        301 -> "Moved Permanently"
        302 -> "Found"
        304 -> "Not Modified"
        400 -> "Bad Request"
        401 -> "Unauthorized"
        403 -> "Forbidden"
        404 -> "Not Found"
        500 -> "Internal Server Error"
        502 -> "Bad Gateway"
        503 -> "Service Unavailable"
        else -> "Unknown Status"
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 0 -> "Unknown"
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("HTTP Info", text)
    clipboardManager.setPrimaryClip(clip)
}
