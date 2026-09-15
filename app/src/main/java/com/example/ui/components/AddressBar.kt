package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BrowserTabState

@Composable
fun AddressBar(
    tabState: BrowserTabState?,
    tabCount: Int,
    isBookmarked: Boolean,
    isBottomBar: Boolean,
    onLoadUrl: (String) -> Unit,
    onReload: () -> Unit,
    onStopLoading: () -> Unit,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenDownloads: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onToggleBarPosition: () -> Unit,
    onOpenNewTab: () -> Unit,
    onOpenNewIncognitoTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val isIncognito = tabState?.isIncognito == true

    // Sync input text when active tab changes, unless currently typing
    LaunchedEffect(tabState?.displayUrl) {
        if (!isEditing) {
            textInput = tabState?.displayUrl.orEmpty()
        }
    }

    Surface(
        color = if (isIncognito) Color(0xFF131926) else MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Loading progress indicator
            if (tabState?.isLoading == true) {
                LinearProgressIndicator(
                    progress = { tabState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary,
                    trackColor = if (isIncognito) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceContainerHighest
                )
            } else {
                Spacer(modifier = Modifier.height(3.dp))
            }

            // Main Address input & top action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Address input pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(if (isIncognito) Color(0xFF1E2638) else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isEditing) (if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary)
                            else (if (isIncognito) Color(0xFF3B4861) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(23.dp)
                        )
                        .clickable {
                            isEditing = true
                            focusRequester.requestFocus()
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Incognito Badge
                        if (isIncognito) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF312E81))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = "Incognito Tab",
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Incognito",
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        // SSL or Search Icon
                        if (isEditing) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            val isSecure = tabState?.isSecure == true
                            val hasUrl = !tabState?.url.isNullOrBlank()
                            Icon(
                                imageVector = if (isSecure) Icons.Default.Lock
                                else if (hasUrl) Icons.Default.Warning
                                else Icons.Default.Search,
                                contentDescription = if (isSecure) "Secure connection" else "Security info",
                                tint = if (isSecure) (if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary)
                                else if (hasUrl) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Field
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    isEditing = false
                                    onLoadUrl(textInput)
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { state ->
                                    isEditing = state.isFocused
                                }
                                .testTag("address_bar_input"),
                            decorationBox = { innerTextField ->
                                if (textInput.isEmpty()) {
                                    Text(
                                        text = "Search or type URL",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 15.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Clear input or Refresh/Stop button
                        if (isEditing) {
                            if (textInput.isNotEmpty()) {
                                IconButton(
                                    onClick = { textInput = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            if (tabState?.isLoading == true) {
                                IconButton(
                                    onClick = onStopLoading,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Stop",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = onReload,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reload",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Tab Switcher Button with Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isIncognito) Color(0xFF1E2638) else Color.Transparent)
                        .border(
                            width = 1.5.dp,
                            color = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(onClick = onOpenTabSwitcher)
                        .testTag("tab_switcher_button")
                ) {
                    Text(
                        text = tabCount.toString(),
                        color = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("overflow_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Tab") },
                            onClick = {
                                showMenu = false
                                onOpenNewTab()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "New Incognito Tab",
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFA5B4FC)
                                    )
                                }
                            },
                            onClick = {
                                showMenu = false
                                onOpenNewIncognitoTab()
                            },
                            modifier = Modifier.testTag("menu_new_incognito_tab")
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (tabState?.isDesktopMode == true) "Mobile Site" else "Desktop Site")
                            },
                            onClick = {
                                showMenu = false
                                onToggleDesktopMode()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Bookmarks") },
                            onClick = {
                                showMenu = false
                                onOpenBookmarks()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                showMenu = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Downloads") },
                            onClick = {
                                showMenu = false
                                onOpenDownloads()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Find in Page") },
                            onClick = {
                                showMenu = false
                                onOpenFindInPage()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (isBottomBar) "Move Bar to Top" else "Move Bar to Bottom")
                            },
                            onClick = {
                                showMenu = false
                                onToggleBarPosition()
                            }
                        )
                    }
                }
            }

            // Bottom Navigation Toolbar (Back, Forward, Home, Bookmark, Tabs)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                IconButton(
                    onClick = onGoBack,
                    enabled = tabState?.canGoBack == true,
                    modifier = Modifier.weight(1f).testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = if (tabState?.canGoBack == true) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onGoForward,
                    enabled = tabState?.canGoForward == true,
                    modifier = Modifier.weight(1f).testTag("nav_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Forward",
                        tint = if (tabState?.canGoForward == true) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

                IconButton(
                    onClick = onGoHome,
                    modifier = Modifier.weight(1f).testTag("nav_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.weight(1f).testTag("nav_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark page",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
