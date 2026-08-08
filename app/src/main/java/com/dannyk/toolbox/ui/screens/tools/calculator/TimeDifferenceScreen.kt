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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState
import kotlin.math.*
import androidx.compose.foundation.horizontalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeDifferenceScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    // Start time state
    var startHour by remember { mutableStateOf("12") }
    var startMinute by remember { mutableStateOf("00") }
    var startSecond by remember { mutableStateOf("00") }
    var isStartPM by remember { mutableStateOf(false) }
    
    // End time state
    var endHour by remember { mutableStateOf("12") }
    var endMinute by remember { mutableStateOf("00") }
    var endSecond by remember { mutableStateOf("00") }
    var isEndPM by remember { mutableStateOf(false) }
    
    // Options
    var includeDate by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Date pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.timeInMillis ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        startDate = cal
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.timeInMillis ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        endDate = cal
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Calculate difference
    val result = remember(startHour, startMinute, startSecond, isStartPM,
                          endHour, endMinute, endSecond, isEndPM,
                          includeDate, startDate, endDate) {
        calculateTimeDifference(
            startHour, startMinute, startSecond, isStartPM,
            endHour, endMinute, endSecond, isEndPM,
            includeDate, startDate, endDate
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Time Difference",
            subtitle = "Calculate the duration between two times",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Include date option
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
                    Icon(Icons.Default.Event, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Include Dates",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Calculate across different days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = includeDate,
                    onCheckedChange = { includeDate = it }
                )
            }
        }
        
        if (includeDate) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (startDate != null) formatDateShort(startDate!!) else "Start Date", maxLines = 1)
                }
                
                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (endDate != null) formatDateShort(endDate!!) else "End Date", maxLines = 1)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time Input Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Start Time Card
            TimeInputCard(
                title = "Start Time",
                hour = startHour,
                minute = startMinute,
                second = startSecond,
                isPM = isStartPM,
                onHourChange = { 
                    if (it.isEmpty() || (it.toIntOrNull()?.let { h -> h in 0..23 } == true)) {
                        startHour = it.ifEmpty { "0" }.padStart(2, '0').takeLast(2)
                    }
                },
                onMinuteChange = { 
                    if (it.isEmpty() || (it.toIntOrNull()?.let { m -> m in 0..59 } == true)) {
                        startMinute = it.ifEmpty { "00" }.padStart(2, '0').takeLast(2)
                    }
                },
                onSecondChange = { 
                    if (it.isEmpty() || (it.toIntOrNull()?.let { s -> s in 0..59 } == true)) {
                        startSecond = it.ifEmpty { "00" }.padStart(2, '0').takeLast(2)
                    }
                },
                onPeriodToggle = { isStartPM = !isStartPM },
                icon = Icons.Default.PlayArrow,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            
            // End Time Card
            TimeInputCard(
                title = "End Time",
                hour = endHour,
                minute = endMinute,
                second = endSecond,
                isPM = isEndPM,
                onHourChange = { 
                    if (it.isEmpty() || (it.toIntOrNull()?.let { h -> h in 0..23 } == true)) {
                        endHour = it.ifEmpty { "0" }.padStart(2, '0').takeLast(2)
                    }
                },
                onMinuteChange = { 
                    if (it.isEmpty() || (it.toIntOrNull()?.let { m -> m in 0..59 } == true)) {
                        endMinute = it.ifEmpty { "00" }.padStart(2, '0').takeLast(2)
                    }
                },
                onSecondChange = { 
                    if (it.isEmpty() || (it.toIntOrNull()?.let { s -> s in 0..59 } == true)) {
                        endSecond = it.ifEmpty { "00" }.padStart(2, '0').takeLast(2)
                    }
                },
                onPeriodToggle = { isEndPM = !isEndPM },
                icon = Icons.Default.Finish,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Swap button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = {
                // Swap all values
                val tempH = startHour; startHour = endHour; endHour = tempH
                val tempM = startMinute; startMinute = endMinute; endMinute = tempM
                val tempS = startSecond; startSecond = endSecond; endSecond = tempS
                val tempP = isStartPM; isStartPM = isEndPM; isEndPM = tempP
                if (includeDate) {
                    val tempD = startDate; startDate = endDate; endDate = tempD
                }
            }) {
                Icon(Icons.Default.SwapVert, contentDescription = "Swap times")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        result?.let { diff ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Main Result - Hours:Minutes:Seconds
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Large time display
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            TimeUnitLarge(value = diff.hours.toString(), unit = "Hours")
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            TimeUnitLarge(value = diff.minutes.toString().padStart(2, '0'), unit = "Min")
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            TimeUnitLarge(value = diff.seconds.toString().padStart(2, '0'), unit = "Sec")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Total time in words
                        Text(
                            text = formatTimeInWords(diff.totalSeconds),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextButton(onClick = { 
                            copyToClipboard(context, "${diff.hours}h ${diff.minutes}m ${diff.seconds}s")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Result")
                        }
                    }
                }
                
                // Alternative formats
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Alternative Formats",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        FormatRow(label = "Decimal Hours", value = String.format("%.4f", diff.decimalHours))
                        FormatRow(label = "Total Minutes", value = formatNumber(diff.totalMinutes.toLong()))
                        FormatRow(label = "Total Seconds", value = formatNumber(diff.totalSeconds))
                        FormatRow(label = "Total Milliseconds", value = formatNumber(diff.totalMilliseconds))
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
                    QuickTimeChip(label = "1 Hour") {
                        setToNowMinusHours(1, { startHour = it }, { startMinute = it }, { startSecond = it }, { isStartPM = it })
                        setToNow(0, { endHour = it }, { endMinute = it }, { endSecond = it }, { isEndPM = it })
                    }
                    
                    QuickTimeChip(label = "8 Hours (Work)") {
                        setToNowMinusHours(8, { startHour = it }, { startMinute = it }, { startSecond = it }, { isStartPM = it })
                        setToNow(0, { endHour = it }, { endMinute = it }, { endSecond = it }, { isEndPM = it })
                    }
                    
                    QuickTimeChip(label = "24 Hours") {
                        setToNowMinusHours(24, { startHour = it }, { startMinute = it }, { startSecond = it }, { isStartPM = it })
                        setToNow(0, { endHour = it }, { endMinute = it }, { endSecond = it }, { isEndPM = it })
                    }
                    
                    QuickTimeChip(label = "Now to Midnight") {
                        setToNow(0, { startHour = it }, { startMinute = it }, { startSecond = it }, { isStartPM = it })
                        endHour = "11"
                        endMinute = "59"
                        endSecond = "59"
                        isEndPM = true
                    }
                    
                    QuickTimeChip(label = "Noon to Now") {
                        startHour = "12"
                        startMinute = "00"
                        startSecond = "00"
                        isStartPM = true
                        setToNow(0, { endHour = it }, { endMinute = it }, { endSecond = it }, { isEndPM = it })
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
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter two times to calculate the difference",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeInputCard(
    title: String,
    hour: String,
    minute: String,
    second: String,
    isPM: Boolean,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onSecondChange: (String) -> Unit,
    onPeriodToggle: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Time input row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TimeField(value = hour, onChange = onHourChange, label = "HH")
                Text(text = ":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TimeField(value = minute, onChange = onMinuteChange, label = "MM")
                Text(text = ":", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TimeField(value = second, onChange = onSecondChange, label = "SS")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // AM/PM toggle
            Column(modifier = Modifier.horizontalScroll(ScrollState(0))) {
                FilterChip(
                    selected = !isPM,
                    onClick = { if (isPM) onPeriodToggle() },
                    label = { Text("AM") },
                    modifier = Modifier.padding(end = 4.dp)
                )
                FilterChip(
                    selected = isPM,
                    onClick = { if (!isPM) onPeriodToggle() },
                    label = { Text("PM") }
                )
            }
        }
    }
}

@Composable
private fun TimeField(value: String, onChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.width(56.dp)
    )
}

@Composable
private fun TimeUnitLarge(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FormatRow(label: String, value: String) {
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
private fun QuickTimeChip(label: String, onClick: () -> Unit) {
    FilterChip(selected = false, onClick = onClick, label = { Text(label) })
}

// Data class for results
data class TimeDifferenceResult(
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val totalMinutes: Long,
    val totalMilliseconds: Long,
    val decimalHours: Double,
    val isNegative: Boolean
)

private fun calculateTimeDifference(
    startHourStr: String,
    startMinuteStr: String,
    startSecondStr: String,
    isStartPM: Boolean,
    endHourStr: String,
    endMinuteStr: String,
    endSecondStr: String,
    isEndPM: Boolean,
    includeDate: Boolean,
    startDate: Calendar?,
    endDate: Calendar?
): TimeDifferenceResult? {
    return try {
        val startHr = startHourStr.toIntOrNull() ?: return null
        val startMin = startMinuteStr.toIntOrNull() ?: return null
        val startSec = startSecondStr.toIntOrNull() ?: return null
        val endHr = endHourStr.toIntOrNull() ?: return null
        val endMin = endMinuteStr.toIntOrNull() ?: return null
        val endSec = endSecondStr.toIntOrNull() ?: return null
        
        // Create calendars for both times
        val startCal = if (includeDate && startDate != null) {
            startDate.clone() as Calendar
        } else {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
        
        val endCal = if (includeDate && endDate != null) {
            endDate.clone() as Calendar
        } else {
            Calendar.getInstance().apply {
                timeInMillis = startCal.timeInMillis
            }
        }
        
        // Set times (convert from 12-hour to 24-hour format)
        startCal.set(Calendar.HOUR_OF_DAY, convertTo24Hour(startHr, isStartPM))
        startCal.set(Calendar.MINUTE, startMin)
        startCal.set(Calendar.SECOND, startSec)
        startCal.set(Calendar.MILLISECOND, 0)
        
        endCal.set(Calendar.HOUR_OF_DAY, convertTo24Hour(endHr, isEndPM))
        endCal.set(Calendar.MINUTE, endMin)
        endCal.set(Calendar.SECOND, endSec)
        endCal.set(Calendar.MILLISECOND, 0)
        
        // Calculate difference
        var diffMillis = endCal.timeInMillis - startCal.timeInMillis
        val isNegative = diffMillis < 0
        diffMillis = kotlin.math.abs(diffMillis)
        
        val totalSeconds = diffMillis / 1000
        val totalMinutes = totalSeconds / 60
        val totalHours = totalMinutes / 60
        
        val hours = totalHours
        val minutes = (totalMinutes % 60)
        val seconds = (totalSeconds % 60)
        
        val decimalHours = totalSeconds / 3600.0
        
        TimeDifferenceResult(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            totalSeconds = totalSeconds,
            totalMinutes = totalMinutes,
            totalMilliseconds = diffMillis,
            decimalHours = decimalHours,
            isNegative = isNegative
        )
    } catch (e: Exception) {
        null
    }
}

private fun convertTo24Hour(hour: Int, isPM: Boolean): Int {
    return when {
        hour == 12 -> if (isPM) 12 else 0
        isPM -> hour + 12
        else -> hour
    }
}

private fun formatTimeInWords(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("$hours hour${if (hours != 1) "s" else ""}")
    if (minutes > 0) parts.add("$minutes minute${if (minutes != 1) "s" else ""}")
    if (seconds > 0 || parts.isEmpty()) parts.add("$seconds second${if (seconds != 1) "s" else ""}")
    
    return parts.joinToString(", ")
}

private fun formatDateShort(calendar: Calendar): String {
    val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return format.format(calendar.time)
}

private fun formatNumber(number: Long): String {
    return "%,d".format(number)
}

private fun setToNow(
    offsetMinutes: Int,
    setHour: (String) -> Unit,
    setMinute: (String) -> Unit,
    setSecond: (String) -> Unit,
    setIsPM: (Boolean) -> Unit
) {
    val now = Calendar.getInstance()
    now.add(Calendar.MINUTE, offsetMinutes)
    
    val hour24 = now.get(Calendar.HOUR_OF_DAY)
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    
    setHour(hour12.toString().padStart(2, '0'))
    setMinute(now.get(Calendar.MINUTE).toString().padStart(2, '0'))
    setSecond(now.get(Calendar.SECOND).toString().padStart(2, '0'))
    setIsPM(hour24 >= 12)
}

private fun setToNowMinusHours(
    hours: Int,
    setHour: (String) -> Unit,
    setMinute: (String) -> Unit,
    setSecond: (String) -> Unit,
    setIsPM: (Boolean) -> Unit
) {
    val then = Calendar.getInstance()
    then.add(Calendar.HOUR_OF_DAY, -hours)
    
    val hour24 = then.get(Calendar.HOUR_OF_DAY)
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    
    setHour(hour12.toString().padStart(2, '0'))
    setMinute(then.get(Calendar.MINUTE).toString().padStart(2, '0'))
    setSecond(then.get(Calendar.SECOND).toString().padStart(2, '0'))
    setIsPM(hour24 >= 12)
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Time Difference Result", text)
    clipboard.setPrimaryClip(clip)
}
