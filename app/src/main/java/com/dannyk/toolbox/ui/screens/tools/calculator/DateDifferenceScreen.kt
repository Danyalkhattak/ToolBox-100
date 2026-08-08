package com.dannyk.toolbox.ui.screens.tools.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider

@Composable
fun DateDifferenceScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var includeEndTime by remember { mutableStateOf(false) }
    
    // Start date picker dialog
    if (showStartPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.timeInMillis ?: System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        startDate = cal
                    }
                    showStartPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // End date picker dialog
    if (showEndPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.timeInMillis ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        endDate = cal
                    }
                    showEndPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Calculate difference
    val result = remember(startDate, endDate, includeEndTime) {
        calculateDateDifference(startDate, endDate, includeEndTime)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Date Difference",
            subtitle = "Calculate the time between two dates",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Start Date Card
        DateInputCard(
            title = "Start Date",
            date = startDate,
            icon = Icons.Default.PlayArrow,
            onClick = { showStartPicker = true },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Swap button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    val temp = startDate
                    startDate = endDate
                    endDate = temp
                },
                enabled = startDate != null && endDate != null
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap dates")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // End Date Card
        DateInputCard(
            title = "End Date",
            date = endDate,
            icon = Icons.Default.Finish,
            onClick = { showEndPicker = true },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Include time option
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Include Time Details",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Show hours, minutes, seconds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = includeEndTime,
                    onCheckedChange = { includeEndTime = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        result?.let { diff ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Main Result Card - Years, Months, Days
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Difference",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            TimeUnitDisplay(value = diff.years.toString(), unit = "Years", color = MaterialTheme.colorScheme.primary)
                            TimeUnitDisplay(value = diff.months.toString(), unit = "Months", color = MaterialTheme.colorScheme.secondary)
                            TimeUnitDisplay(value = diff.days.toString(), unit = "Days", color = MaterialTheme.colorScheme.tertiary)
                        }
                        
                        if (includeEndTime) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${diff.hours}h ${diff.minutes}m ${diff.seconds}s",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(onClick = { 
                            copyToClipboard(context, "${diff.years}y ${diff.months}m ${diff.days}d")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Result")
                        }
                    }
                }
                
                // Total Units Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TotalUnitCard(title = "Total Days", value = formatNumber(diff.totalDays), Icons.Default.Today, MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
                    TotalUnitCard(title = "Total Weeks", value = formatNumber(diff.totalWeeks), Icons.Default.ViewWeek, MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TotalUnitCard(title = "Total Hours", value = formatNumber(diff.totalHours), Icons.Default.Schedule, MaterialTheme.colorScheme.tertiaryContainer, Modifier.weight(1f))
                    TotalUnitCard(title = "Total Minutes", value = formatNumber(diff.totalMinutes), Icons.Default.Timer, MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
                }
                
                if (includeEndTime) {
                    TotalUnitCard(title = "Total Seconds", value = formatNumber(diff.totalSeconds), Icons.Default.AvTimer, MaterialTheme.colorScheme.surfaceVariant, Modifier.fillMaxWidth())
                }
                
                // Additional Info Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Additional Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        InfoRow("Weekdays between", diff.weekdaysCount.toString())
                        InfoRow("Weekend days", diff.weekendDaysCount.toString())
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow("Percentage of year", String.format("%.2f%%", diff.percentageOfYear))
                        InfoRow("Is leap year span?", if (diff.containsLeapYear) "Yes" else "No")
                    }
                }
                
                // Quick presets
                Text(
                    text = "Quick Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickPresetChip(label = "Today") {
                        startDate = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        endDate = Calendar.getInstance()
                    }
                    
                    QuickPresetChip(label = "This Week") {
                        startDate = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        endDate = Calendar.getInstance()
                    }
                    
                    QuickPresetChip(label = "This Month") {
                        startDate = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        endDate = Calendar.getInstance()
                    }
                    
                    QuickPresetChip(label = "This Year") {
                        startDate = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_YEAR, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        endDate = Calendar.getInstance()
                    }
                    
                    QuickPresetChip(label = "Last 7 Days") {
                        endDate = Calendar.getInstance()
                        startDate = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -7)
                        }
                    }
                    
                    QuickPresetChip(label = "Last 30 Days") {
                        endDate = Calendar.getInstance()
                        startDate = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -30)
                        }
                    }
                    
                    QuickPresetChip(label = "Last 365 Days") {
                        endDate = Calendar.getInstance()
                        startDate = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -365)
                        }
                    }
                }
            }
        } ?: run {
            // Empty state
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select two dates to calculate the difference",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DateInputCard(
    title: String,
    date: Calendar?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (date != null) formatDateFull(date) else "Tap to select date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (date != null) FontWeight.Bold else FontWeight.Normal,
                    color = if (date != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TimeUnitDisplay(
    value: String,
    unit: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TotalUnitCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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

@Composable
private fun QuickPresetChip(label: String, onClick: () -> Unit) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = { Text(label) }
    )
}

// Data class for results
data class DateDifferenceResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val totalSeconds: Long,
    val weekdaysCount: Int,
    val weekendDaysCount: Int,
    val percentageOfYear: Double,
    val containsLeapYear: Boolean
)

private fun calculateDateDifference(start: Calendar?, end: Calendar?, includeTime: Boolean): DateDifferenceResult? {
    return try {
        if (start == null || end == null) return null
        
        val startCal = start.clone() as Calendar
        val endCal = end.clone() as Calendar
        
        // Ensure start is before end
        if (startCal.after(endCal)) {
            val temp = startCal
            startCal.timeInMillis = endCal.timeInMillis
            endCal.timeInMillis = temp.timeInMillis
        }
        
        // Calculate year, month, day difference
        var years = endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
        var months = endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
        var days = endCal.get(Calendar.DAY_OF_MONTH) - startCal.get(Calendar.DAY_OF_MONTH)
        
        // Adjust for negative values
        if (days < 0) {
            months--
            val prevMonth = endCal.clone() as Calendar
            prevMonth.add(Calendar.MONTH, -1)
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        
        if (months < 0) {
            years--
            months += 12
        }
        
        // Calculate totals in milliseconds
        val diffMillis = endCal.timeInMillis - startCal.timeInMillis
        
        // Time components
        val hours = if (includeTime) (diffMillis / (1000 * 60 * 60) % 24).toInt() else 0
        val minutes = if (includeTime) (diffMillis / (1000 * 60) % 60).toInt() else 0
        val seconds = if (includeTime) (diffMillis / 1000 % 60).toInt() else 0
        
        val totalDays = diffMillis / (1000 * 60 * 60 * 24)
        val totalWeeks = totalDays / 7
        val totalHours = diffMillis / (1000 * 60 * 60)
        val totalMinutes = diffMillis / (1000 * 60)
        val totalSeconds = diffMillis / 1000
        
        // Count weekdays and weekends
        var weekdaysCount = 0
        var weekendDaysCount = 0
        val calIter = startCal.clone() as Calendar
        while (!calIter.after(endCal)) {
            val dayOfWeek = calIter.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendDaysCount++
            } else {
                weekdaysCount++
            }
            calIter.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Percentage of year
        val daysInYear = if (isLeapYear(endCal.get(Calendar.YEAR))) 366 else 365
        val percentageOfYear = (totalDays.toDouble() / daysInYear) * 100
        
        // Check for leap years in range
        var containsLeapYear = false
        for (year in startCal.get(Calendar.YEAR)..endCal.get(Calendar.YEAR)) {
            if (isLeapYear(year)) {
                containsLeapYear = true
                break
            }
        }
        
        DateDifferenceResult(
            years = years,
            months = months,
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            totalSeconds = totalSeconds,
            weekdaysCount = weekdaysCount,
            weekendDaysCount = weekendDaysCount,
            percentageOfYear = percentageOfYear.coerceAtMost(100.0),
            containsLeapYear = containsLeapYear
        )
    } catch (e: Exception) {
        null
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

private fun formatDateFull(calendar: Calendar): String {
    val format = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return format.format(calendar.time)
}

private fun formatNumber(number: Long): String {
    return "%,d".format(number)
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Date Difference Result", text)
    clipboard.setPrimaryClip(clip)
}
