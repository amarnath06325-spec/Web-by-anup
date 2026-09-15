package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BrowserTabState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherSheet(
    tabs: List<BrowserTabState>,
    currentTabIndex: Int,
    onSelectTab: (Int) -> Unit,
    onCloseTab: (Int) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onCloseAll: (incognitoOnly: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentTabIsIncognito = tabs.getOrNull(currentTabIndex)?.isIncognito == true
    var selectedSection by remember { mutableIntStateOf(if (currentTabIsIncognito) 1 else 0) }

    val regularTabs = remember(tabs) {
        tabs.mapIndexed { index, tab -> index to tab }.filter { !it.second.isIncognito }
    }
    val incognitoTabs = remember(tabs) {
        tabs.mapIndexed { index, tab -> index to tab }.filter { it.second.isIncognito }
    }

    val activeDisplayList = if (selectedSection == 1) incognitoTabs else regularTabs

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (selectedSection == 1) Color(0xFF131926) else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Switcher: Standard vs Incognito
            PrimaryTabRow(
                selectedTabIndex = selectedSection,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tabs (${regularTabs.size})")
                        }
                    },
                    modifier = Modifier.testTag("tab_filter_regular")
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = if (selectedSection == 1) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Incognito (${incognitoTabs.size})",
                                color = if (selectedSection == 1) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_filter_incognito")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Toolbar (Close All, Add Tab)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (activeDisplayList.isNotEmpty()) {
                    TextButton(
                        onClick = { onCloseAll(selectedSection == 1) },
                        modifier = Modifier.testTag("close_all_tabs_button")
                    ) {
                        Text(
                            text = if (selectedSection == 1) "Close All Incognito" else "Close All",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                FilledTonalButton(
                    onClick = {
                        if (selectedSection == 1) {
                            onNewIncognitoTab()
                        } else {
                            onNewTab()
                        }
                    },
                    colors = if (selectedSection == 1) {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF312E81),
                            contentColor = Color(0xFFA5B4FC)
                        )
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("new_tab_button")
                ) {
                    Icon(
                        imageVector = if (selectedSection == 1) Icons.Default.VisibilityOff else Icons.Default.Add,
                        contentDescription = "New tab",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (selectedSection == 1) "New Incognito Tab" else "New Tab")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Cards Grid or Empty State
            if (activeDisplayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selectedSection == 1) Color(0xFF1E2638) else MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selectedSection == 1) Icons.Default.VisibilityOff else Icons.Default.Public,
                            contentDescription = null,
                            tint = if (selectedSection == 1) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedSection == 1) "No Incognito Tabs Open" else "No Regular Tabs Open",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedSection == 1) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedSection == 1)
                                "Browse without saving search history, cookies, or autofill data."
                            else
                                "Open a new tab to explore websites.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedSection == 1) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FilledTonalButton(
                            onClick = {
                                if (selectedSection == 1) onNewIncognitoTab() else onNewTab()
                            },
                            colors = if (selectedSection == 1) {
                                ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF312E81),
                                    contentColor = Color(0xFFA5B4FC)
                                )
                            } else {
                                ButtonDefaults.filledTonalButtonColors()
                            }
                        ) {
                            Text(if (selectedSection == 1) "Open Incognito Tab" else "Open New Tab")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeDisplayList, key = { it.second.id }) { (originalIndex, tab) ->
                        val isActive = originalIndex == currentTabIndex
                        val isIncognito = tab.isIncognito

                        Card(
                            onClick = { onSelectTab(originalIndex) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isIncognito) {
                                    if (isActive) Color(0xFF1E293B) else Color(0xFF0F172A)
                                } else {
                                    if (isActive) MaterialTheme.colorScheme.surfaceContainerHighest
                                    else MaterialTheme.colorScheme.surface
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .then(
                                    if (isActive) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = if (isIncognito) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    }
                                )
                                .testTag("tab_item_$originalIndex")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            ) {
                                // Tab Header (Favicon + Title + Close Button)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Public,
                                        contentDescription = null,
                                        tint = if (isIncognito) Color(0xFFA5B4FC)
                                        else if (isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tab.title.ifBlank { if (isIncognito) "Incognito Tab" else "New Tab" },
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isIncognito) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { onCloseTab(originalIndex) },
                                        modifier = Modifier.size(24.dp).testTag("close_tab_$originalIndex")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close tab",
                                            tint = if (isIncognito) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Tab Body preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isIncognito) Color(0xFF1E2638)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (isIncognito) {
                                            Text(
                                                text = "🔒 Incognito",
                                                fontSize = 10.sp,
                                                color = Color(0xFFA5B4FC),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                        Text(
                                            text = tab.url.ifBlank { if (isIncognito) "Incognito Home" else "Start Page" },
                                            fontSize = 11.sp,
                                            color = if (isIncognito) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
