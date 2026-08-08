package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import android.content.ClipData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgentViewerScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val currentUA = remember { getDefaultUserAgent(context) }
    
    var selectedUAType by remember { mutableStateOf<String?>(null) }
    var serverResponse by remember { mutableStateOf<String?>(null) }
    var isLoadingServer by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "User-Agent Viewer",
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
            // Current User-Agent Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your Current User-Agent",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentUA,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 3,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(onClick = {
                                copyToClipboard(context, currentUA)
                                showCopiedMessage = true
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy UA",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (showCopiedMessage) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Copied to clipboard!",
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

            Spacer(modifier = Modifier.height(24.dp))

            // Parsed Components
            Text(
                text = "Parsed Components",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val parsedComponents = parseUserAgent(currentUA)
            
            parsedComponents.forEach { (label, value) ->
                ComponentCard(label = label, value = value)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test What Server Sees
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
                        text = "Test What Server Sees",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Check what your User-Agent looks like to a web server:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                testServerSees(
                                    context = context,
                                    onLoading = { isLoadingServer = it },
                                    onSuccess = { response ->
                                        serverResponse = response
                                    },
                                    onError = { error ->
                                        serverResponse = "Error: $error"
                                    }
                                )
                            }
                        },
                        enabled = !isLoadingServer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoadingServer) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Check Server Response")
                        }
                    }

                    if (serverResponse != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = serverResponse!!,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(12.dp),
                                maxLines = 10
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Common Browser User-Agents
            Text(
                text = "Common Browser User-Agents",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            commonUserAgents.forEachIndexed { index, ua ->
                val isSelected = selectedUAType == ua.name
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = { 
                                selectedUAType = if (isSelected) null else ua.name 
                            },
                            role = Role.RadioButton
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) 
                            MaterialTheme.colorScheme.secondaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = ua.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ua.userAgent,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        maxLines = 4,
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    IconButton(onClick = {
                                        copyToClipboard(context, ua.userAgent)
                                    }) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = ua.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
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
                        text = "About User-Agent Strings",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = """
                            A User-Agent string is sent by browsers to identify themselves to web servers. It typically contains:
                            
                            • Platform/OS information (e.g., Android, Windows, macOS)
                            • Device type and model
                            • Browser name and version
                            • Rendering engine details
                            
                            Servers use this information to:
                            - Serve appropriate content versions
                            - Track browser statistics
                            - Block or allow certain clients
                            - Optimize page rendering
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentCard(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private data class CommonUA(
    val name: String,
    val userAgent: String,
    val description: String
)

private val commonUserAgents = listOf(
    CommonUA(
        name = "Chrome on Windows",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        description = "Latest Google Chrome on Windows 11"
    ),
    CommonUA(
        name = "Firefox on Windows",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        description = "Latest Mozilla Firefox on Windows 11"
    ),
    CommonUA(
        name = "Safari on macOS",
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15",
        description = "Latest Safari on macOS Sonoma"
    ),
    CommonUA(
        name = "Chrome on Android",
        userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
        description = "Latest Chrome on Pixel 8 Pro"
    ),
    CommonUA(
        name = "Safari on iPhone",
        userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1",
        description = "Latest Safari on iPhone"
    ),
    CommonUA(
        name = "Edge on Windows",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
        description = "Latest Microsoft Edge on Windows 11"
    ),
    CommonUA(
        name = "Google Bot",
        userAgent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
        description = "Google's web crawler bot"
    ),
    CommonUA(
        name = "Generic Mobile",
        userAgent = "Mozilla/5.0 (Mobile; rv:40.0) Gecko/40.0 Firefox/40.0",
        description = "Generic mobile Firefox user agent"
    )
)

private fun getDefaultUserAgent(context: Context): String {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            android.webkit.WebSettings.getDefaultUserAgent(context)
        } else {
            System.getProperty("http.agent") ?: "Unknown"
        }
    } catch (_: Exception) {
        System.getProperty("http.agent") ?: "Unknown User Agent"
    }
}

private fun parseUserAgent(userAgent: String): List<Pair<String, String?>> {
    val components = mutableListOf<Pair<String, String?>>()

    // Detect platform
    when {
        userAgent.contains("Android", ignoreCase = true) -> {
            components.add("Platform" to "Android")
            
            // Try to extract Android version
            val androidVersionRegex = Regex("Android\\s*([\\d.]+)")
            androidVersionRegex.find(userAgent)?.let {
                components.add("Android Version" to it.groupValues[1])
            }
            
            // Try to extract device model
            val deviceRegex = Regex(";\\s*([^;]+?)\\s*(?:Build|;)").find(userAgent)?.groupValues?.getOrNull(1)
            deviceRegex?.let { components.add("Device" to it.trim()) }
        }
        userAgent.contains("Windows NT", ignoreCase = true) -> {
            components.add("Platform" to "Windows")
            val winVersionRegex = Regex("Windows NT\\s*([\\d.]+)").find(userAgent)?.groupValues?.getOrNull(1)
            winVersionRegex?.let { components.add("Windows Version" to it) }
        }
        userAgent.contains("Mac OS X", ignoreCase = true) || userAgent.contains("Macintosh", ignoreCase = true) -> {
            components.add("Platform" to "macOS")
            val macVersionRegex = Regex("Mac OS X\\s*([\\d_]+)").find(userAgent)?.groupValues?.getOrNull(1)
            macVersionRegex?.let { components.add("macOS Version" to it.replace("_", ".")) }
        }
        userAgent.contains("iPhone", ignoreCase = true) -> {
            components.add("Platform" to "iOS (iPhone)")
        }
        userAgent.contains("iPad", ignoreCase = true) -> {
            components.add("Platform" to "iOS (iPad)")
        }
        userAgent.contains("Linux", ignoreCase = true) && !userAgent.contains("Android") -> {
            components.add("Platform" to "Linux")
        }
        else -> {
            components.add("Platform" to "Unknown")
        }
    }

    // Detect browser
    when {
        userAgent.contains("Edg/", ignoreCase = true) -> {
            components.add("Browser" to "Microsoft Edge")
            val edgeVersion = Regex("Edg/([\\d.]+)").find(userAgent)?.groupValues?.getOrNull(1)
            edgeVersion?.let { components.add("Browser Version" to it) }
        }
        userAgent.contains("Chrome/", ignoreCase = true) && !userAgent.contains("Edg/") -> {
            components.add("Browser" to "Google Chrome")
            val chromeVersion = Regex("Chrome/([\\d.]+)").find(userAgent)?.groupValues?.getOrNull(1)
            chromeVersion?.let { components.add("Browser Version" to it) }
        }
        userAgent.contains("Firefox/", ignoreCase = true) -> {
            components.add("Browser" to "Mozilla Firefox")
            val firefoxVersion = Regex("Firefox/([\\d.]+)").find(userAgent)?.groupValues?.getOrNull(1)
            firefoxVersion?.let { components.add("Browser Version" to it) }
        }
        userAgent.contains("Safari/", ignoreCase = true) && !userAgent.contains("Chrome") -> {
            components.add("Browser" to "Apple Safari")
            val safariVersion = Regex("Version/([\\d.]+)").find(userAgent)?.groupValues?.getOrNull(1)
            safariVersion?.let { components.add("Browser Version" to it) }
        }
        userAgent.contains("Opera|OPR/", ignoreCase = true) -> {
            components.add("Browser" to "Opera")
        }
        else -> {
            components.add("Browser" to "Unknown")
        }
    }

    // Detect engine
    when {
        userAgent.contains("WebKit", ignoreCase = true) -> {
            components.add("Rendering Engine" to "WebKit")
            val webkitVersion = Regex("WebKit/([\\d.]+)").find(userAgent)?.groupValues?.getOrNull(1)
            webkitVersion?.let { components.add("Engine Version" to it) }
        }
        userAgent.contains("Gecko", ignoreCase = true) -> {
            components.add("Rendering Engine" to "Gecko")
        }
        userAgent.contains("Trident", ignoreCase = true) || userAgent.contains("MSIE", ignoreCase = true) -> {
            components.add("Rendering Engine" to "Trident (Legacy IE)")
        }
    }

    // Check for mobile
    components.add("Mobile" to if (userAgent.contains("Mobile", ignoreCase = true)) "Yes" else "No")

    return components
}

private suspend fun testServerSees(
    context: Context,
    onLoading: (Boolean) -> Unit,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            onLoading(true)
            
            val url = java.net.URL("https://httpbin.org/user-agent")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val json = org.json.JSONObject(response)
                val serverSeenUA = json.optString("user-agent", "Could not parse response")
                
                onLoading(false)
                withContext(Dispatchers.Main) {
                    onSuccess(serverSeenUA)
                }
            } else {
                onLoading(false)
                withContext(Dispatchers.Main) {
                    onError("Server returned ${connection.responseCode}")
                }
            }
            connection.disconnect()
        } catch (e: Exception) {
            onLoading(false)
            withContext(Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("User-Agent", text)
    clipboardManager.setPrimaryClip(clip)
}
