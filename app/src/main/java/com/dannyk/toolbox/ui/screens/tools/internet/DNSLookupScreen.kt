package com.dannyk.toolbox.ui.screens.tools.internet

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DNSLookupScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var domainInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var lookupResults by remember { mutableStateOf<List<DNSRecord>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "DNS Lookup",
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
                        text = "Enter Domain Name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = domainInput,
                        onValueChange = { 
                            domainInput = it.trim()
                            if (it.isEmpty()) {
                                lookupResults = emptyList()
                                errorMessage = null
                            }
                        },
                        label = { Text("e.g., google.com") },
                        placeholder = { Text("example.com") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Dns, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (domainInput.isNotBlank()) {
                                scope.launch {
                                    performDNSLookup(
                                        context = context,
                                        domain = domainInput,
                                        onLoading = { isLoading = it },
                                        onSuccess = { results ->
                                            lookupResults = results
                                            errorMessage = null
                                        },
                                        onError = { message ->
                                            errorMessage = message
                                            lookupResults = emptyList()
                                        }
                                    )
                                }
                            }
                        },
                        enabled = domainInput.isNotBlank() && !isLoading,
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
                        Text(if (isLoading) "Looking up..." else "Lookup DNS")
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
            if (lookupResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DNS Records",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${lookupResults.size} record(s) found",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Group results by type
                val groupedResults = lookupResults.groupBy { it.type }
                
                groupedResults.forEach { (type, records) ->
                    // Type Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "${records.size} record(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Records for this type
                    records.forEachIndexed { index, record ->
                        DNSRecordCard(
                            record = record,
                            index = index + 1,
                            onCopy = { value ->
                                copyToClipboard(context, value)
                                showCopiedMessage = true
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Empty State
            if (!isLoading && lookupResults.isEmpty() && errorMessage == null && domainInput.isEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "DNS Lookup Tool",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enter a domain name to look up its DNS records including A, AAAA, CNAME, MX, NS, and TXT records.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
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
                        text = "About DNS Record Types",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    listOf(
                        "A" to "Maps a domain to an IPv4 address",
                        "AAAA" to "Maps a domain to an IPv6 address",
                        "CNAME" to "Canonical name - alias to another domain",
                        "MX" to "Mail exchange - email server info",
                        "NS" to "Name server - authoritative DNS servers",
                        "TXT" to "Text - various data like SPF, DKIM"
                    ).forEach { (type, desc) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "$type:",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(60.dp)
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

            if (showCopiedMessage) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Copied to clipboard!")
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    showCopiedMessage = false
                }
            }
        }
    }
}

@Composable
private fun DNSRecordCard(
    record: DNSRecord,
    index: Int,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#$index",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(40.dp)
                )
                
                Text(
                    text = record.value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = { onCopy(record.value) }) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (record.ttl != null || record.preference != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    record.ttl?.let { ttl ->
                        Text(
                            text = "TTL: ${ttl}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    record.preference?.let { pref ->
                        Text(
                            text = "Priority: $pref",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

data class DNSRecord(
    val type: String,
    val value: String,
    val ttl: Int? = null,
    val preference: Int? = null
)

private suspend fun performDNSLookup(
    context: Context,
    domain: String,
    onLoading: (Boolean) -> Unit,
    onSuccess: (List<DNSRecord>) -> Unit,
    onError: (String) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            onLoading(true)
            val results = mutableListOf<DNSRecord>()

            // Clean up domain input
            var cleanDomain = domain.trim().lowercase()
            if (cleanDomain.startsWith("http://") || cleanDomain.startsWith("https://")) {
                cleanDomain = URL(cleanDomain).host
            }
            if (cleanDomain.endsWith("/")) {
                cleanDomain = cleanDomain.dropLast(1)
            }

            // A Record (IPv4) - Using InetAddress
            try {
                val addresses = InetAddress.getAllByName(cleanDomain)
                addresses.forEach { addr ->
                    if (addr is java.net.Inet4Address) {
                        results.add(DNSRecord(type = "A", value = addr.hostAddress ?: ""))
                    }
                }
            } catch (_: Exception) {}

            // AAAA Record (IPv6)
            try {
                val addresses = InetAddress.getAllByName(cleanDomain)
                addresses.forEach { addr ->
                    if (addr is java.net.Inet6Address) {
                        results.add(DNSRecord(type = "AAAA", value = addr.hostAddress ?: ""))
                    }
                }
            } catch (_: Exception) {}

            // Try to get more detailed DNS info using Google DNS-over-HTTPS API
            try {
                val dnsUrl = java.net.URL("https://dns.google/resolve?name=$cleanDomain&type=ANY")
                val connection = dnsUrl.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = org.json.JSONObject(response)
                    
                    if (json.optBoolean("Status", false) == true || json.has("Answer")) {
                        val answers = json.optJSONArray("Answer")
                        if (answers != null) {
                            for (i in 0 until answers.length()) {
                                val answer = answers.getJSONObject(i)
                                val type = answer.getInt("type")
                                val data = answer.getString("data")
                                val ttl = answer.optInt("TTL", null)
                                
                                when (type) {
                                    1 -> { // A
                                        if (results.none { it.type == "A" && it.value == data }) {
                                            results.add(DNSRecord("A", data, ttl))
                                        }
                                    }
                                    28 -> { // AAAA
                                        if (results.none { it.type == "AAAA" && it.value == data }) {
                                            results.add(DNSRecord("AAAA", data, ttl))
                                        }
                                    }
                                    5 -> { // CNAME
                                        results.add(DNSRecord("CNAME", data, ttl))
                                    }
                                    15 -> { // MX
                                        val parts = data.split(" ", limit = 2)
                                        val pref = parts.getOrNull(0)?.toIntOrNull()
                                        val exchange = parts.getOrElse(1) { data }
                                        results.add(DNSRecord("MX", exchange, ttl, pref))
                                    }
                                    2 -> { // NS
                                        results.add(DNSRecord("NS", data, ttl))
                                    }
                                    16 -> { // TXT
                                        results.add(DNSRecord("TXT", data, ttl))
                                    }
                                }
                            }
                        }
                    }
                }
                connection.disconnect()
            } catch (_: Exception) {
                // Continue with basic InetAddress results
            }

            if (results.isEmpty()) {
                withContext(Dispatchers.Main) {
                    onError("No DNS records found for '$cleanDomain'. Please check the domain name.")
                }
            } else {
                // Sort results: A, AAAA first, then CNAME, MX, NS, TXT
                val sortedResults = results.sortedWith(compareBy<String> { 
                    when (it) {
                        "A" -> 0
                        "AAAA" -> 1
                        "CNAME" -> 2
                        "MX" -> 3
                        "NS" -> 4
                        "TXT" -> 5
                        else -> 6
                    }
                }.thenBy { it })
                
                onLoading(false)
                withContext(Dispatchers.Main) {
                    onSuccess(sortedResults)
                }
            }
        } catch (e: Exception) {
            onLoading(false)
            withContext(Dispatchers.Main) {
                onError("DNS lookup failed: ${e.message}")
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("DNS Record", text)
    clipboardManager.setPrimaryClip(clip)
}
