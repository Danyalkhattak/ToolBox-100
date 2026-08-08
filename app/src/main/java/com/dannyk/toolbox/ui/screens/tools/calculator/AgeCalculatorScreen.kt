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
import androidx.compose.foundation.clickable

@Composable
fun AgeCalculatorScreen(navHostController: NavHostController) {
    val context = LocalContext.current
    
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.timeInMillis ?: System.currentTimeMillis() - 25 * 365L * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        selectedDate = cal
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Calculate age based on selected date
    val ageResult = remember(selectedDate) {
        calculateAge(selectedDate)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ToolHeader(
            title = "Age Calculator",
            subtitle = "Calculate your exact age from date of birth",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Date of Birth Selection Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Date of Birth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedDate != null) {
                        formatDate(selectedDate!!)
                    } else {
                        "Select Your Birth Date"
                    })
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Results Section
        ageResult?.let { result ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Main Age Display Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            text = "Your Age Is",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${result.years}",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Column {
                                Text(
                                    text = "Years",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${result.months} months, ${result.days} days",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Copy button
                        TextButton(onClick = { 
                            copyToClipboard(context, "${result.years} years, ${result.months} months, ${result.days} days")
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Age")
                        }
                    }
                }
                
                // Additional Details Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Months
                    DetailCard(
                        title = "Total Months",
                        value = result.totalMonths.toString(),
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    
                    // Total Weeks
                    DetailCard(
                        title = "Total Weeks",
                        value = formatNumber(result.totalWeeks.toInt()),
                        icon = Icons.Default.ViewWeek,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Total Days
                    DetailCard(
                        title = "Total Days Lived",
                        value = formatNumber(result.totalDays),
                        icon = Icons.Default.Today,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    
                    // Total Hours (approximate)
                    DetailCard(
                        title = "Total Hours",
                        value = formatNumber(result.totalHours),
                        icon = Icons.Default.Schedule,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                }
                
                // Next Birthday Countdown
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Celebration,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Next Birthday",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "You'll turn ${result.nextBirthdayAge} on ${formatDateShort(result.nextBirthday)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "In ${result.daysUntilBirthday} days (${result.monthsUntilBirthday} months)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Day of Birth Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Birth Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        ResultRowItem(label = "Born On", value = result.dayOfWeek)
                        ResultRowItem(label = "Zodiac Sign", value = result.zodiacSign)
                        ResultRowItem(label = "Chinese Zodiac", value = result.chineseZodiac)
                        ResultRowItem(label = "Birth Season", value = result.season)
                        ResultRowItem(label = "Generation", value = result.generation)
                    }
                }
                
                // Fun Facts
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Fun Facts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        funFactItem("Heartbeats", "~${formatNumber(result.totalHeartbeats)}", context)
                        funFactItem("Breaths Taken", "~${formatNumber(result.totalBreaths)}", context)
                        funFactItem("Sleep (avg)", "~${formatNumber(result.hoursSlept)} hours", context)
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
                        Icons.Default.Cake,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select your date of birth to see your age",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
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
private fun ResultRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
private fun funFactItem(label: String, value: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { copyToClipboard(context, "$label: $value") },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

// Data class for results
data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalMonths: Int,
    val totalWeeks: Long,
    val totalDays: Long,
    val totalHours: Long,
    val nextBirthday: Calendar,
    val nextBirthdayAge: Int,
    val daysUntilBirthday: Int,
    val monthsUntilBirthday: Int,
    val dayOfWeek: String,
    val zodiacSign: String,
    val chineseZodiac: String,
    val season: String,
    val generation: String,
    val totalHeartbeats: Long,
    val totalBreaths: Long,
    val hoursSlept: Long
)

private fun calculateAge(birthDate: Calendar?): AgeResult? {
    return try {
        if (birthDate == null) return null
        
        val now = Calendar.getInstance()
        
        // Validate date is not in the future
        if (birthDate.after(now)) return null
        
        var years = now.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        var months = now.get(Calendar.MONTH) - birthDate.get(Calendar.MONTH)
        var days = now.get(Calendar.DAY_OF_MONTH) - birthDate.get(Calendar.DAY_OF_MONTH)
        
        // Adjust for negative values
        if (days < 0) {
            months--
            val prevMonth = Calendar.getInstance().apply {
                time = now.time
                add(Calendar.MONTH, -1)
            }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        
        if (months < 0) {
            years--
            months += 12
        }
        
        // Calculate totals
        val totalMonths = years * 12 + months
        val diffMillis = now.timeInMillis - birthDate.timeInMillis
        val totalDays = diffMillis / (1000 * 60 * 60 * 24)
        val totalWeeks = totalDays / 7
        val totalHours = totalDays * 24
        
        // Next birthday calculation
        val nextBirthday = Calendar.getInstance().apply {
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, birthDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, birthDate.get(Calendar.DAY_OF_MONTH))
            
            // If birthday has passed this year, get next year's
            if (before(now) || (get(Calendar.MONTH) == now.get(Calendar.MONTH) && 
                get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH))) {
                add(Calendar.YEAR, 1)
            }
        }
        
        val nextBirthdayAge = nextBirthday.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        val daysUntilBirthday = ((nextBirthday.timeInMillis - now.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val monthsUntilBirthday = daysUntilBirthday / 30
        
        // Day of week
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        dayFormat.timeZone = TimeZone.getDefault()
        val dayOfWeek = dayFormat.format(birthDate.time)
        
        // Zodiac sign
        val zodiacSign = getZodiacSign(birthDate.get(Calendar.MONTH), birthDate.get(Calendar.DAY_OF_MONTH))
        
        // Chinese zodiac
        val chineseZodiac = getChineseZodiac(birthDate.get(Calendar.YEAR))
        
        // Season
        val season = getSeason(birthDate.get(Calendar.MONTH))
        
        // Generation
        val generation = getGeneration(birthDate.get(Calendar.YEAR))
        
        // Fun facts (approximations)
        val totalHeartbeats = totalDays * 100_000L // ~100,000 beats per day
        val totalBreaths = totalDays * 23_000L // ~23,000 breaths per day
        val hoursSlept = totalDays * 8L // ~8 hours per day
        
        AgeResult(
            years = years,
            months = months,
            days = days,
            totalMonths = totalMonths,
            totalWeeks = totalWeeks,
            totalDays = totalDays,
            totalHours = totalHours,
            nextBirthday = nextBirthday,
            nextBirthdayAge = nextBirthdayAge,
            daysUntilBirthday = daysUntilBirthday,
            monthsUntilBirthday = monthsUntilBirthday,
            dayOfWeek = dayOfWeek,
            zodiacSign = zodiacSign,
            chineseZodiac = chineseZodiac,
            season = season,
            generation = generation,
            totalHeartbeats = totalHeartbeats,
            totalBreaths = totalBreaths,
            hoursSlept = hoursSlept
        )
    } catch (e: Exception) {
        null
    }
}

private fun getZodiacSign(month: Int, day: Int): String {
    return when (month) {
        Calendar.JANUARY -> if (day <= 19) "Capricorn" else "Aquarius"
        Calendar.FEBRUARY -> if (day <= 18) "Aquarius" else "Pisces"
        Calendar.MARCH -> if (day <= 20) "Pisces" else "Aries"
        Calendar.APRIL -> if (day <= 19) "Aries" else "Taurus"
        Calendar.MAY -> if (day <= 20) "Taurus" else "Gemini"
        Calendar.JUNE -> if (day <= 20) "Gemini" else "Cancer"
        Calendar.JULY -> if (day <= 22) "Cancer" else "Leo"
        Calendar.AUGUST -> if (day <= 22) "Leo" else "Virgo"
        Calendar.SEPTEMBER -> if (day <= 22) "Virgo" else "Libra"
        Calendar.OCTOBER -> if (day <= 22) "Libra" else "Scorpio"
        Calendar.NOVEMBER -> if (day <= 21) "Scorpio" else "Sagittarius"
        Calendar.DECEMBER -> if (day <= 21) "Sagittarius" else "Capricorn"
        else -> "Unknown"
    }
}

private fun getChineseZodiac(year: Int): String {
    val animals = listOf(
        "Rat", "Ox", "Tiger", "Rabbit", "Dragon", 
        "Snake", "Horse", "Goat", "Monkey", "Rooster", "Dog", "Pig"
    )
    return animals[(year - 1900) % 12]
}

private fun getSeason(month: Int): String {
    return when (month) {
        in 2..4 -> "Spring"
        in 5..7 -> "Summer"
        in 8..10 -> "Fall"
        else -> "Winter"
    }
}

private fun getGeneration(year: Int): String {
    return when {
        year >= 2013 -> "Generation Alpha"
        year >= 1997 -> "Gen Z / Zoomers"
        year >= 1981 -> "Millennials"
        year >= 1965 -> "Generation X"
        year >= 1946 -> "Baby Boomers"
        year >= 1928 -> "Silent Generation"
        else -> "Greatest Generation"
    }
}

private fun formatDate(calendar: Calendar): String {
    val format = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    return format.format(calendar.time)
}

private fun formatDateShort(calendar: Calendar): String {
    val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return format.format(calendar.time)
}

private fun formatNumber(number: Int): String {
    return "%,d".format(number)
}

private fun formatNumber(number: Long): String {
    return "%,d".format(number)
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Age Result", text)
    clipboard.setPrimaryClip(clip)
}
