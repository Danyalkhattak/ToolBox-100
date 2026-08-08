package com.dannyk.toolbox.ui.screens.tools.internet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import androidx.compose.ui.text.font.FontWeight
import android.content.Context
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState
import kotlin.math.*
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PingTesterScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    var hostInput by remember { mutableStateOf("google.com") }
    var pingCount by remember { mutableStateOf(4) }
    var isPinging by remember { mutableStateOf(false) }
    var pingResults by remember { mutableStateOf<List<PingResult>>(emptyList()) }
    var pingSummary by remember { mutableStateOf<PingSummary?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPing by remember { mutableStateOf<Int?>(null) }

    // Cancel pinging when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            isPinging = false
        }
    }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "Ping Tester",
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
                        text = "Target Host",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("Hostname or IP Address") },
                        placeholder = { Text("e.g., google.com or 8.8.8.8") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Uri
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isPinging
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ping Count",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = pingCount.toFloat(),
                            onValueChange = { pingCount = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            enabled = !isPinging,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "$pingCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (hostInput.isNotBlank()) {
                                startPing(
                                    host = hostInput.trim(),
                                    count = pingCount,
                                    onPingingChanged = { 
                                        isPinging = it
                                        if (!it) currentPing = null
                                    },
                                    onCurrentPing = { currentPing = it },
                                    onResults = { results, summary ->
                                        pingResults = results
                                        pingSummary = summary
                                        errorMessage = null
                                    },
                                    onError = { message ->
                                        errorMessage = message
                                        pingResults = emptyList()
                                        pingSummary = null
                                    }
                                )
                            }
                        },
                        enabled = hostInput.isNotBlank() && !isPinging,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isPinging) "Pinging..." else "Start Ping")
                    }
                }
            }

            // Current Ping Indicator
            if (isPinging && currentPing != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Ping",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(100.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { 0.5f },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 6.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            
                            Text(
                                text = "${currentPing}ms",
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
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

            // Summary Section
            if (pingSummary != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Ping Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            pingSummary!!.packetLoss == 0f -> MaterialTheme.colorScheme.primaryContainer
                            pingSummary!!.packetLoss < 50f -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Packet Loss - Visual indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Packet Loss", style = MaterialTheme.typography.bodyMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { pingSummary!!.packetLoss / 100f },
                                    modifier = Modifier.width(80.dp),
                                    color = when {
                                        pingSummary!!.packetLoss == 0f -> Color.Green
                                        pingSummary!!.packetLoss < 50f -> Color(0xFFFFA500)
                                        else -> Color.Red
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${"%.1f".format(pingSummary!!.packetLoss)}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))

                        // Stats Grid
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pingSummary!!.minTime}ms", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pingSummary!!.avgTime}ms", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Max", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pingSummary!!.maxTime}ms", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Sent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pingSummary!!.sent}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pingSummary!!.received}", style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }

            // Individual Results
            if (pingResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Individual Pings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                pingResults.forEachIndexed { index, result ->
                    PingResultCard(result = result, index = index + 1)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Info Section
            if (!isPinging && pingResults.isEmpty() && errorMessage == null) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.NetworkCheck,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Network Ping Tool",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Test network connectivity and measure response time to any host.\n\nNote: This uses TCP connectivity tests as Android restricts raw ICMP access.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }

            // Latency Guide
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
                        text = "Latency Reference",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    listOf(
                        "< 20ms" to "Excellent - Local/LAN" to Color.Green,
                        "20-50ms" to "Very Good - Same region" to Color(0xFF4CAF50),
                        "50-100ms" to "Good - Different region" to Color(0xFFFFC107),
                        "100-200ms" to "Acceptable - International" to Color(0xFFFF9800),
                        "> 200ms" to "Poor - High latency" to Color.Red
                    ).forEach { (range, desc, color) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = range,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(90.dp)
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
private fun PingResultCard(
    result: PingResult,
    index: Int,
    modifier: Modifier = Modifier
) {
    val isSuccess = result.time != null
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSuccess) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSuccess) Color.Green else Color.Red
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSuccess) "✓" else "✗",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Sequence number
            Text(
                text = "#$index",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(40.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Time or error
            if (isSuccess) {
                Text(
                    text = "${result.time}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                // Visual bar for time
                val barWidth = (result.time!! / 500f).coerceIn(0.05f, 1f)
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxFraction(barWidth)
                            .background(
                                when {
                                    result.time <= 20 -> Color.Green
                                    result.time <= 50 -> Color(0xFF4CAF50)
                                    result.time <= 100 -> Color(0xFFFFC107)
                                    result.time <= 200 -> Color(0xFFFF9800)
                                    else -> Color.Red
                                }
                            )
                    )
                }
            } else {
                Text(
                    text = result.errorMessage ?: "Timeout",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

data class PingResult(
    val sequence: Int,
    val time: Long?,
    val errorMessage: String? = null
)

data class PingSummary(
    val sent: Int,
    val received: Int,
    val packetLoss: Float,
    val minTime: Long,
    val maxTime: Long,
    val avgTime: Long
)

private fun startPing(
    host: String,
    count: Int,
    onPingingChanged: (Boolean) -> Unit,
    onCurrentPing: (Int?) -> Unit,
    onResults: (List<PingResult>, PingSummary) -> Unit,
    onError: (String) -> Unit
) {
    onPingingChanged(true)
    
    kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
        try {
            val results = mutableListOf<PingResult>()
            val times = mutableListOf<Long>()
            
            // Resolve hostname first
            val address = try {
                InetAddress.getByName(host)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onPingingChanged(false)
                    onError("Failed to resolve host '$host': ${e.message}")
                }
                return@launch
            }
            
            val hostAddress = address.hostAddress ?: host
            
            for (i in 1..count) {
                if (!kotlinx.coroutines.coroutineContext.isActive) break
                
                val startTime = System.currentTimeMillis()
                
                try {
                    // Use TCP connect as alternative to ICMP ping
                    // Try common ports: 80 (HTTP), 443 (HTTPS), then fall back to DNS port 53
                    var connected = false
                    
                    for (port in listOf(80, 443, 53)) {
                        try {
                            val socket = Socket()
                            socket.soTimeout = 3000
                            socket.connect(InetSocketAddress(hostAddress, port), 3000)
                            socket.close()
                            connected = true
                            break
                        } catch (_: Exception) {
                            // Try next port
                        }
                    }
                    
                    val endTime = System.currentTimeMillis()
                    val timeTaken = endTime - startTime
                    
                    if (connected) {
                        times.add(timeTaken)
                        results.add(PingResult(sequence = i, time = timeTaken))
                        withContext(Dispatchers.Main) {
                            onCurrentPing(timeTaken.toInt())
                        }
                    } else {
                        results.add(PingResult(sequence = i, time = null, errorMessage = "Connection refused"))
                    }
                } catch (e: Exception) {
                    val endTime = System.currentTimeMillis()
                    results.add(PingResult(sequence = i, time = null, errorMessage = e.message ?: "Unknown error"))
                }
                
                // Delay between pings (except after last one)
                if (i < count) {
                    delay(500)
                }
            }
            
            // Calculate summary
            val received = times.size
            val sent = count
            val packetLoss = if (sent > 0) ((sent - received).toFloat() / sent * 100) else 0f
            
            val summary = PingSummary(
                sent = sent,
                received = received,
                packetLoss = packetLoss,
                minTime = times.minOrNull() ?: 0,
                maxTime = times.maxOrNull() ?: 0,
                avgTime = if (times.isNotEmpty()) (times.sum() / times.size) else 0
            )
            
            withContext(Dispatchers.Main) {
                onPingingChanged(false)
                onCurrentPing(null)
                onResults(results, summary)
            }
            
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onPingingChanged(false)
                onCurrentPing(null)
                onError("Ping failed: ${e.message}")
            }
        }
    }
}
