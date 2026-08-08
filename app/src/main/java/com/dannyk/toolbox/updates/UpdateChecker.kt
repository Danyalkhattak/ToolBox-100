package com.dannyk.toolbox.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dannyk.toolbox.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val currentVersion: String,
    val latestVersion: String?,
    val releaseNotes: String?,
    val releaseUrl: String?,
    val error: String? = null
)

object GitHubUpdateChecker {
    
    private const val GITHUB_OWNER = "Danyalkhattak"
    private const val GITHUB_REPO = "ToolBox-100"
    private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    private const val GITHUB_RELEASES_URL = "https://github.com/$GITHUB_OWNER/$GITHUB_REPO/releases"
    
    /**
     * Check for updates from GitHub Releases API
     */
    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                when (connection.responseCode) {
                    200 -> {
                        val response = connection.inputStream.bufferedReader().readText()
                        val json = JSONObject(response)
                        
                        val latestTag = json.optString("tag_name", "").removePrefix("v").removePrefix("V")
                        val releaseName = json.optString("name", "")
                        val body = json.optString("body", "")
                        val htmlUrl = json.optString("html_url", GITHUB_RELEASES_URL)
                        
                        val currentVersion = BuildConfig.VERSION_NAME
                        val hasUpdate = compareVersions(currentVersion, latestTag) < 0
                        
                        UpdateInfo(
                            hasUpdate = hasUpdate,
                            currentVersion = currentVersion,
                            latestVersion = latestTag,
                            releaseNotes = buildString {
                                if (releaseName.isNotEmpty()) appendLine(releaseName)
                                if (body.isNotEmpty()) append(body)
                            }.trim().ifEmpty { null },
                            releaseUrl = htmlUrl
                        )
                    }
                    404 -> {
                        UpdateInfo(
                            hasUpdate = false,
                            currentVersion = BuildConfig.VERSION_NAME,
                            latestVersion = null,
                            releaseNotes = null,
                            releaseUrl = GITHUB_RELEASES_URL
                        )
                    }
                    403 -> {
                        UpdateInfo(
                            hasUpdate = false,
                            currentVersion = BuildConfig.VERSION_NAME,
                            latestVersion = null,
                            releaseNotes = null,
                            releaseUrl = null,
                            error = "API rate limit exceeded. Please try again later."
                        )
                    }
                    else -> {
                        UpdateInfo(
                            hasUpdate = false,
                            currentVersion = BuildConfig.VERSION_NAME,
                            latestVersion = null,
                            releaseNotes = null,
                            releaseUrl = null,
                            error = "Failed to check for updates (HTTP ${connection.responseCode})"
                        )
                    }
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            UpdateInfo(
                hasUpdate = false,
                currentVersion = BuildConfig.VERSION_NAME,
                latestVersion = null,
                releaseNotes = null,
                releaseUrl = null,
                error = getErrorMessage(e)
            )
        }
    }
    
    /**
     * Compare two semantic version strings.
     */
    fun compareVersions(v1: String, v2: String): Int {
        val cleanV1 = v1.replace(Regex("[^0-9.]"), "")
        val cleanV2 = v2.replace(Regex("[^0-9.]"), "")
        
        val parts1 = cleanV1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = cleanV2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(parts1.size, parts2.size)
        
        for (i in 0 until maxLength) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        
        return 0
    }
    
    private fun getErrorMessage(e: Exception): String {
        return when (e) {
            is java.net.SocketTimeoutException -> "Connection timed out. Please check your internet connection."
            is java.net.UnknownHostException -> "Unable to connect to server. Please check your internet connection."
            is java.net.ConnectException -> "No internet connection. Please check your network settings."
            is javax.net.ssl.SSLException -> "Secure connection failed. Please try again."
            else -> "Failed to check for updates: ${e.message}"
        }
    }
    
    /**
     * Open the GitHub releases page in a browser
     */
    fun openReleasesPage(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_RELEASES_URL))
            context.startActivity(intent)
        } catch (e: Exception) {
            // No browser available
        }
    }
}

/**
 * Composable state holder for update checking
 */
class UpdateCheckerState(
    initialLoading: Boolean = false,
    initialInfo: UpdateInfo? = null,
    initialShowDialog: Boolean = false
) {
    var isLoading by mutableStateOf(initialLoading)
    var updateInfo by mutableStateOf(initialInfo)
    var showDialog by mutableStateOf(initialShowDialog)
    
    suspend fun performCheck() {
        isLoading = true
        updateInfo = GitHubUpdateChecker.checkForUpdates()
        isLoading = false
        showDialog = true
    }
    
    fun dismiss() {
        showDialog = false
    }
}

@Composable
fun rememberUpdateCheckerState(): UpdateCheckerState {
    return remember { UpdateCheckerState() }
}

/**
 * Update dialog composable
 */
@Composable
fun UpdateDialog(
    info: UpdateInfo?,
    show: Boolean,
    onDismiss: () -> Unit,
    onOpenRelease: (Context) -> Unit
) {
    if (!show || info == null) return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (info.hasUpdate) "Update Available" else "Up to Date",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (info.error != null) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = info.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (info.hasUpdate) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info.latestVersion ?: "New Version",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current: ${info.currentVersion}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    info.releaseNotes?.let { notes ->
                        Text(
                            text = "Release Notes:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "You're running the latest version!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Version ${info.currentVersion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            if (info.hasUpdate && info.releaseUrl != null) {
                Button(onClick = { onOpenRelease }) {
                    Text("Download Update")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (info.hasUpdate) {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        }
    )
}
