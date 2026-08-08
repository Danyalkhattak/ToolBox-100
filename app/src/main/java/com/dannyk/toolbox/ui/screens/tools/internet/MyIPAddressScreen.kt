package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyIPAddressScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var ipAddress by remember { mutableStateOf<String?>(null) }
    var ispInfo by remember { mutableStateOf<ISPInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    // Auto-fetch on first load
    LaunchedEffect(Unit) {
        fetchIPInfo(
            context = context,
            onLoading = { isLoading = it },
            onSuccess = { ip, isp ->
                ipAddress = ip
                ispInfo = isp
                errorMessage = null
            },
            onError = { message ->
                errorMessage = message
            }
        )
    }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "My IP Address",
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
            // Main IP Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Public IP Address",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading && ipAddress == null) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else if (errorMessage != null && ipAddress == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (ipAddress != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = ipAddress!!,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            IconButton(
                                onClick = {
                                    copyToClipboard(context, ipAddress!!, showCopiedMessage = true)
                                    showCopiedMessage = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy IP",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        if (showCopiedMessage) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Copied!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Refresh Button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        fetchIPInfo(
                            context = context,
                            onLoading = { isLoading = it },
                            onSuccess = { ip, isp ->
                                ipAddress = ip
                                ispInfo = isp
                                errorMessage = null
                            },
                            onError = { message ->
                                errorMessage = message
                            }
                        )
                    }
                },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Fetching..." else "Refresh")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ISP Information Section
            if (ispInfo != null) {
                Text(
                    text = "Network Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                ISPInfoCard(label = "IP Address", value = ispInfo!!.ip)
                ISPInfoCard(label = "Country", value = ispInfo!!.country)
                ISPInfoCard(label = "Region", value = ispInfo!!.region)
                ISPInfoCard(label = "City", value = ispInfo!!.city)
                ISPInfoCard(label = "ISP", value = ispInfo!!.isp)
                ISPInfoCard(label = "Organization", value = ispInfo!!.org)
                ISPInfoCard(label = "Timezone", value = ispInfo!!.timezone)
                ISPInfoCard(label = "Postal Code", value = ispInfo!!.postal)

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Local Network Info Section
            Text(
                text = "Local Network Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            val localInfo = getLocalNetworkInfo(context)
            ISPInfoCard(label = "Local IP", value = localInfo.localIp)
            ISPInfoCard(label = "Hostname", value = localInfo.hostname)
            ISPInfoCard(label = "Network Type", value = localInfo.networkType)

            Spacer(modifier = Modifier.height(24.dp))

            // IP Version Info
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
                        text = "About IP Addresses",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = """
                            • IPv4 addresses are 32-bit numbers (e.g., 192.168.1.1)
                            • IPv6 addresses are 128-bit numbers (e.g., 2001:0db8:85a3::...)
                            • Your public IP is assigned by your Internet Service Provider (ISP)
                            • This IP can be used to identify your general location
                            • Using a VPN or proxy can mask your real public IP
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
private fun ISPInfoCard(
    label: String,
    value: String?,
    modifier: Modifier = Modifier
) {
    if (!value.isNullOrBlank()) {
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
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private data class ISPInfo(
    val ip: String,
    val country: String?,
    val region: String?,
    val city: String?,
    val isp: String?,
    val org: String?,
    val timezone: String?,
    val postal: String?
)

private data class LocalNetworkInfo(
    val localIp: String,
    val hostname: String,
    val networkType: String
)

private suspend fun fetchIPInfo(
    context: Context,
    onLoading: (Boolean) -> Unit,
    onSuccess: (String, ISPInfo?) -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            onLoading(true)
            
            // Fetch basic IP from ipify
            val ipUrl = URL("https://api.ipify.org?format=json")
            val ipConnection = ipUrl.openConnection() as HttpURLConnection
            ipConnection.connectTimeout = 10000
            ipConnection.readTimeout = 10000
            
            if (ipConnection.responseCode == 200) {
                val response = ipConnection.inputStream.bufferedReader().readText()
                val json = JSONObject(response)
                val ip = json.getString("ip")
                
                // Try to get detailed info from ip-api.com
                try {
                    val infoUrl = URL("http://ip-api.com/json/$ip?fields=status,country,regionName,city,isp,org,timezone,zip,query")
                    val infoConnection = infoUrl.openConnection() as HttpURLConnection
                    infoConnection.connectTimeout = 10000
                    infoConnection.readTimeout = 10000
                    
                    if (infoConnection.responseCode == 200) {
                        val infoResponse = infoConnection.inputStream.bufferedReader().readText()
                        val infoJson = JSONObject(infoResponse)
                        
                        if (infoJson.getString("status") == "success") {
                            val ispInfo = ISPInfo(
                                ip = infoJson.optString("query", ip),
                                country = infoJson.optString("country", null),
                                region = infoJson.optString("regionName", null),
                                city = infoJson.optString("city", null),
                                isp = infoJson.optString("isp", null),
                                org = infoJson.optString("org", null),
                                timezone = infoJson.optString("timezone", null),
                                postal = infoJson.optString("zip", null)
                            )
                            onLoading(false)
                            withContext(Dispatchers.Main) {
                                onSuccess(ip, ispInfo)
                            }
                            return@withContext
                        }
                    }
                    infoConnection.disconnect()
                } catch (_: Exception) {
                    // Continue with just the IP if detailed lookup fails
                }
                
                onLoading(false)
                withContext(Dispatchers.Main) {
                    onSuccess(ip, null)
                }
            } else {
                onLoading(false)
                withContext(Dispatchers.Main) {
                    onError("Failed to fetch IP address. Server returned ${ipConnection.responseCode}")
                }
            }
            ipConnection.disconnect()
        } catch (e: Exception) {
            onLoading(false)
            withContext(Dispatchers.Main) {
                onError("Network error: ${e.message}\nPlease check your internet connection.")
            }
        }
    }
}

private fun getLocalNetworkInfo(context: Context): LocalNetworkInfo {
    var localIp = "Unknown"
    
    try {
        val wifiManager = context.applicationContext.getSystemService(android.net.wifi.WifiManager::class.java)
        val ipInt = wifiManager.connectionInfo.ipAddress
        localIp = String.format(
            "%d.%d.%d.%d",
            ipInt and 0xff,
            (ipInt shr 8) and 0xff,
            (ipInt shr 16) and 0xff,
            (ipInt shr 24) and 0xff
        )
        
        // Handle case where IP is 0.0.0.0 (not connected to WiFi)
        if (localIp == "0.0.0.0") {
            // Try to get from network interfaces
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (!networkInterface.isLoopback && networkInterface.isUp) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                            localIp = addr.hostAddress ?: "Unknown"
                            break
                        }
                    }
                }
            }
        }
    } catch (_: Exception) {
        // Keep default "Unknown"
    }

    val hostname = try {
        java.net.InetAddress.getLocalHost().hostName
    } catch (_: Exception) {
        "Unknown"
    }

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    val networkType = when {
        activeNetwork == null -> "Not Connected"
        activeNetwork.type == android.net.ConnectivityManager.TYPE_WIFI -> "WiFi"
        activeNetwork.type == android.net.ConnectivityManager.TYPE_MOBILE -> "Mobile Data"
        activeNetwork.type == android.net.ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
        else -> "Other (${activeNetwork.typeName})"
    }

    return LocalNetworkInfo(localIp, hostname, networkType)
}

private fun copyToClipboard(context: Context, text: String, showCopiedMessage: Boolean = false) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("IP Address", text)
    clipboardManager.setPrimaryClip(clip)
}
