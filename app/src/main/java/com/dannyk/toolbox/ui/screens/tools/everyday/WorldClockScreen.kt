package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.SearchBar
import com.dannyk.toolbox.ui.components.ToolHeader
import java.util.Calendar
import java.util.TimeZone
import androidx.compose.ui.geometry.Offset
import kotlin.math.*
import androidx.compose.runtime.LaunchedEffect

data class TimeZoneCity(
    val id: String,
    val city: String,
    val country: String,
    val timeZoneId: String
)

data class ClockEntry(
    val city: TimeZoneCity,
    var isAdded: Boolean = false
)

// Popular world cities with their time zones
val availableTimeZones = listOf(
    TimeZoneCity("nyc", "New York", "USA", "America/New_York"),
    TimeZoneCity("la", "Los Angeles", "USA", "America/Los_Angeles"),
    TimeZoneCity("chicago", "Chicago", "USA", "America/Chicago"),
    TimeZoneCity("denver", "Denver", "USA", "America/Denver"),
    TimeZoneCity("london", "London", "UK", "Europe/London"),
    TimeZoneCity("paris", "Paris", "France", "Europe/Paris"),
    TimeZoneCity("berlin", "Berlin", "Germany", "Europe/Berlin"),
    TimeZoneCity("moscow", "Moscow", "Russia", "Europe/Moscow"),
    TimeZoneCity("dubai", "Dubai", "UAE", "Asia/Dubai"),
    TimeZoneCity("mumbai", "Mumbai", "India", "Asia/Kolkata"),
    TimeZoneCity("bangkok", "Bangkok", "Thailand", "Asia/Bangkok"),
    TimeZoneCity("singapore", "Singapore", "Singapore", "Asia/Singapore"),
    TimeZoneCity("hong_kong", "Hong Kong", "China", "Asia/Hong_Kong"),
    TimeZoneCity("shanghai", "Shanghai", "China", "Asia/Shanghai"),
    TimeZoneCity("tokyo", "Tokyo", "Japan", "Asia/Tokyo"),
    TimeZoneCity("seoul", "Seoul", "South Korea", "Asia/Seoul"),
    TimeZoneCity("sydney", "Sydney", "Australia", "Australia/Sydney"),
    TimeZoneCity("melbourne", "Melbourne", "Australia", "Australia/Melbourne"),
    TimeZoneCity("auckland", "Auckland", "New Zealand", "Pacific/Auckland"),
    TimeZoneCity("honolulu", "Honolulu", "USA", "Pacific/Honolulu"),
    TimeZoneCity("anchorage", "Anchorage", "USA", "America/Anchorage"),
    TimeZoneCity("mexico_city", "Mexico City", "Mexico", "America/Mexico_City"),
    TimeZoneCity("saopaulo", "São Paulo", "Brazil", "America/Sao_Paulo"),
    TimeZoneCity("buenos_aires", "Buenos Aires", "Argentina", "America/Argentina/Buenos_Aires"),
    TimeZoneCity("cairo", "Cairo", "Egypt", "Africa/Cairo"),
    TimeZoneCity("johannesburg", "Johannesburg", "South Africa", "Africa/Johannesburg"),
    TimeZoneCity("lagos", "Lagos", "Nigeria", "Africa/Lagos")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(navHostController: NavHostController) {
    var addedClocks by remember { 
        mutableStateOf(listOf(availableTimeZones[0], availableTimeZones[4], availableTimeZones[14])) // Default: NYC, London, Tokyo
    }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var is24HourFormat by remember { mutableStateOf(false) }
    
    // Update time every second
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "World Clock",
            subtitle = "Track time across different time zones",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Format toggle and search button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 12/24 hour format toggle
            Card(
                onClick = { is24HourFormat = !is24HourFormat },
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (is24HourFormat) "24h" else "12h",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Text(
                text = "${addedClocks.size} clocks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Search/Add button
            FilledTonalButton(
                onClick = { isSearching = !isSearching },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isSearching) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isSearching) "Cancel" else "Add")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isSearching) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search cities..."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filtered list of available time zones
            val filteredZones = availableTimeZones.filter { zone ->
                searchQuery.isEmpty() ||
                zone.city.contains(searchQuery, ignoreCase = true) ||
                zone.country.contains(searchQuery, ignoreCase = true)
            }.filter { it !in addedClocks }
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredZones) { zone ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    addedClocks = addedClocks + zone
                                    searchQuery = ""
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = zone.city,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${zone.country} • ${getTimeZoneOffset(zone.timeZoneId)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Add clock",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                if (filteredZones.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No cities found" else "All cities added",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // Display added clocks
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addedClocks, key = { it.id }) { zone ->
                    ClockCard(
                        zone = zone,
                        currentTime = currentTime,
                        is24HourFormat = is24HourFormat,
                        onRemove = { 
                            addedClocks = addedClocks.filter { it.id != zone.id }
                        }
                    )
                }
                
                if (addedClocks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No clocks added",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap + to add a city",
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
}

@Composable
private fun ClockCard(
    zone: TimeZoneCity,
    currentTime: Long,
    is24HourFormat: Boolean,
    onRemove: () -> Unit
) {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(zone.timeZoneId))
    calendar.timeInMillis = currentTime
    
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)
    
    val isNight = hour < 6 || hour >= 20
    val isDaytime = hour in 6..20
    
    // Get date string
    val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SUNDAY -> "Sun"
        Calendar.MONDAY -> "Mon"
        Calendar.TUESDAY -> "Tue"
        Calendar.WEDNESDAY -> "Wed"
        Calendar.THURSDAY -> "Thu"
        Calendar.FRIDAY -> "Fri"
        Calendar.SATURDAY -> "Sat"
        else -> ""
    }
    val month = calendar.get(Calendar.MONTH) + 1
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val dateString = "$dayOfWeek, $month/$dayOfMonth"
    
    // Format time
    val displayTime = if (is24HourFormat) {
        String.format("%02d:%02d:%02d", hour, minute, second)
    } else {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        String.format("%02d:%02d:%02d %s", displayHour, minute, second, amPm)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isNight -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                isDaytime -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day/Night indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isNight) Color(0xFF1A237E) else Color(0xFFFFEB3B).copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isNight) Icons.Default.NightsStay else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = if (isNight) Color(0xFFFFC107) else Color(0xFFF57F17),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Time info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = zone.city,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = getTimeZoneOffset(zone.timeZoneId),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = zone.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // Time display
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayTime,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            
            // Remove button
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove clock",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun getTimeZoneOffset(timeZoneId: String): String {
    return try {
        val tz = TimeZone.getTimeZone(timeZoneId)
        val offset = tz.getOffset(System.currentTimeMillis())
        val hours = offset / (1000 * 60 * 60)
        val minutes = Math.abs(offset / (1000 * 60) % 60)
        
        if (minutes == 0) {
            String.format("UTC%+d", hours)
        } else {
            String.format("UTC%+d:%02d", hours, minutes)
        }
    } catch (e: Exception) {
        "UTC"
    }
}
