package com.example.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auth.GoogleAuthManager
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        GoogleAuthManager.init(this)

        setContent {
            MyApplicationTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentUser by GoogleAuthManager.currentUser.collectAsState()
    var selectedSearchEngine by remember {
        mutableStateOf(BrowserSettingsManager.getSearchEngine(context))
    }
    var autoLoginEnabled by remember {
        mutableStateOf(BrowserSettingsManager.isAutoLoginGoogleEnabled(context))
    }
    var selectedTheme by remember {
        mutableStateOf(BrowserSettingsManager.getThemeMode(context))
    }

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showSearchEngineMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Browser"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // -------------------------------------------------------------
            // Section 1: Account & Google Sync
            // -------------------------------------------------------------
            SettingsSectionHeader(title = "Google Account & Sync")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_google_account_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Picture or Fallback
                    if (!currentUser?.profilePictureUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser?.profilePictureUrl,
                            contentDescription = "Google Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (currentUser != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (currentUser?.displayName?.take(1) ?: "G").uppercase(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "Google Account",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser?.email ?: "Sync bookmarks, logins & passwords",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (currentUser == null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val res = GoogleAuthManager.signIn(context)
                                    if (res.isSuccess) {
                                        Toast.makeText(
                                            context,
                                            "Synced: ${res.getOrNull()?.displayName}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Sign in: ${res.exceptionOrNull()?.message ?: "Cancelled"}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("btn_settings_sign_in")
                        ) {
                            Text("Sign In")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    GoogleAuthManager.signOut(context)
                                    Toast.makeText(context, "Signed out from Google", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("btn_settings_sign_out")
                        ) {
                            Text("Sign Out")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 2: Search Engine Selection
            // -------------------------------------------------------------
            SettingsSectionHeader(title = "Search Engine")

            val searchEngineNames = listOf(
                BrowserSettingsManager.SEARCH_ENGINE_GOOGLE,
                BrowserSettingsManager.SEARCH_ENGINE_DUCKDUCKGO,
                BrowserSettingsManager.SEARCH_ENGINE_BING,
                BrowserSettingsManager.SEARCH_ENGINE_YAHOO
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                SettingsClickableRow(
                    icon = Icons.Default.Search,
                    title = "Default Search Engine",
                    subtitle = selectedSearchEngine,
                    onClick = { showSearchEngineMenu = true }
                )

                DropdownMenu(
                    expanded = showSearchEngineMenu,
                    onDismissRequest = { showSearchEngineMenu = false }
                ) {
                    searchEngineNames.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            trailingIcon = {
                                if (selectedSearchEngine == name) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected")
                                }
                            },
                            onClick = {
                                selectedSearchEngine = name
                                BrowserSettingsManager.setSearchEngine(context, name)
                                showSearchEngineMenu = false
                                Toast.makeText(context, "Search engine set to $name", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 3: Privacy & Seamless Auto-Login
            // -------------------------------------------------------------
            SettingsSectionHeader(title = "Privacy & Passwords")

            // Auto-login toggle
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Seamless Google Auto-Login",
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Auto-detect and pre-fill synced Google Account for 'Continue with Google'",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoLoginEnabled,
                        onCheckedChange = { isChecked ->
                            autoLoginEnabled = isChecked
                            BrowserSettingsManager.setAutoLoginGoogleEnabled(context, isChecked)
                        },
                        modifier = Modifier.testTag("switch_auto_login")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clear Data
            SettingsClickableRow(
                icon = Icons.Default.Delete,
                title = "Clear Browsing Data & Cookies",
                subtitle = "Clear history, cookies, cache, and site data",
                onClick = { showClearDataDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 4: Permission Manager
            // -------------------------------------------------------------
            SettingsSectionHeader(title = "Permissions & System")

            SettingsClickableRow(
                icon = Icons.Default.Security,
                title = "Site & System Permissions",
                subtitle = "Camera, Mic (WebRTC), Location & Notifications",
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Unable to open app settings", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingsClickableRow(
                icon = Icons.Default.OpenInBrowser,
                title = "Default Browser App",
                subtitle = "Set Anup Web as your default browser",
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Open settings to set default browser", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // -------------------------------------------------------------
            // Section 5: Theme & Appearance
            // -------------------------------------------------------------
            SettingsSectionHeader(title = "Appearance & Theme")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val themeOptions = listOf(
                        "System Default" to "system",
                        "Light Theme" to "light",
                        "Dark Theme" to "dark"
                    )
                    themeOptions.forEach { (label, mode) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = mode
                                    BrowserSettingsManager.setThemeMode(context, mode)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedTheme == mode),
                                onClick = {
                                    selectedTheme = mode
                                    BrowserSettingsManager.setThemeMode(context, mode)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // About Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Anup Web v1.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Modern Chromium Engine & Seamless Google Identity",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Browsing Data?") },
            text = {
                Text("This will clear cached pages, history, cookies, and local database storage. Active website sessions will be logged out.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            CookieManager.getInstance().removeAllCookies(null)
                            CookieManager.getInstance().flush()
                            WebStorage.getInstance().deleteAllData()
                            Toast.makeText(context, "All cookies and browsing data cleared", Toast.LENGTH_SHORT).show()
                        } catch (_: Exception) {
                            Toast.makeText(context, "Failed to clear all data", Toast.LENGTH_SHORT).show()
                        }
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
