package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BrowserTabState

/**
 * Bottom Navigation Bar located at the very bottom ("sabse niche") of the screen.
 * Houses Back, Forward, Home, Bookmark, and Tabs switcher with active count.
 */
@Composable
fun BottomNavBar(
    tabState: BrowserTabState?,
    tabCount: Int,
    isBookmarked: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncognito = tabState?.isIncognito == true
    val canGoBack = tabState?.canGoBack == true
    val canGoForward = tabState?.canGoForward == true

    Surface(
        color = if (isIncognito) Color(0xFF131926) else MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Sits comfortably above gesture pill or navigation bar
        ) {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = if (isIncognito) Color(0xFF2E384D) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                // 1. Back Button
                IconButton(
                    onClick = onGoBack,
                    enabled = canGoBack,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back",
                        tint = if (canGoBack) {
                            if (isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                        } else {
                            if (isIncognito) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Forward Button
                IconButton(
                    onClick = onGoForward,
                    enabled = canGoForward,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_forward_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go forward",
                        tint = if (canGoForward) {
                            if (isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                        } else {
                            if (isIncognito) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 3. Home Button
                IconButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (isIncognito) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 4. Bookmark Button
                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_bookmark_button")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark page",
                        tint = if (isBookmarked) {
                            if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary
                        } else {
                            if (isIncognito) Color.White else MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 5. Tabs Switcher Button with Count Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nav_tabs_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isIncognito) Color(0xFF1E2638) else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(onClick = onOpenTabSwitcher)
                    ) {
                        Text(
                            text = tabCount.coerceAtLeast(1).toString(),
                            color = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
