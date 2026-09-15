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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HistoryEntity

data class QuickShortcut(
    val title: String,
    val url: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun StartPage(
    recentHistory: List<HistoryEntity>,
    onSelectUrl: (String) -> Unit,
    isIncognito: Boolean = false,
    modifier: Modifier = Modifier
) {
    val shortcuts = if (isIncognito) {
        listOf(
            QuickShortcut("DuckDuckGo", "https://duckduckgo.com", Icons.Default.Security, Color(0xFFDE5833)),
            QuickShortcut("Google", "https://www.google.com", Icons.Default.Search, Color(0xFF4285F4)),
            QuickShortcut("Wikipedia", "https://www.wikipedia.org", Icons.Default.Public, Color(0xFF6B7280)),
            QuickShortcut("GitHub", "https://github.com", Icons.Default.Code, Color(0xFF24292F)),
            QuickShortcut("Reddit", "https://www.reddit.com", Icons.Default.Explore, Color(0xFFFF4500)),
            QuickShortcut("SpeedTest", "https://fast.com", Icons.Default.Speed, Color(0xFF10B981)),
            QuickShortcut("HackerNews", "https://news.ycombinator.com", Icons.Default.Star, Color(0xFFFF6600)),
            QuickShortcut("YouTube", "https://www.youtube.com", Icons.Default.VideoLibrary, Color(0xFFFF0000))
        )
    } else {
        listOf(
            QuickShortcut("Google", "https://www.google.com", Icons.Default.Search, Color(0xFF4285F4)),
            QuickShortcut("YouTube", "https://www.youtube.com", Icons.Default.VideoLibrary, Color(0xFFFF0000)),
            QuickShortcut("Wikipedia", "https://www.wikipedia.org", Icons.Default.Public, Color(0xFF6B7280)),
            QuickShortcut("GitHub", "https://github.com", Icons.Default.Code, Color(0xFF24292F)),
            QuickShortcut("Reddit", "https://www.reddit.com", Icons.Default.Explore, Color(0xFFFF4500)),
            QuickShortcut("DuckDuckGo", "https://duckduckgo.com", Icons.Default.Security, Color(0xFFDE5833)),
            QuickShortcut("SpeedTest", "https://fast.com", Icons.Default.Speed, Color(0xFF10B981)),
            QuickShortcut("HackerNews", "https://news.ycombinator.com", Icons.Default.Star, Color(0xFFFF6600))
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(if (isIncognito) Color(0xFF0F172A) else MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Logo & Title
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncognito) {
                            Brush.linearGradient(
                                listOf(Color(0xFF312E81), Color(0xFF6366F1))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Explore,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isIncognito) "You've gone Incognito" else "Flow Browser",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = if (isIncognito) Color.White else MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isIncognito)
                    "Now you can browse privately without saving history or site data."
                else
                    "Fast, private & modern web experience",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isIncognito) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Incognito explanation card
        if (isIncognito) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Flow Browser won't save:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val points = listOf(
                            "Your browsing history or search queries",
                            "Cookies and temporary site data (deleted on tab close)",
                            "Information entered in web forms"
                        )
                        points.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ",
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = point,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Quick Search Bar preview
        item {
            Surface(
                onClick = { onSelectUrl(if (isIncognito) "https://duckduckgo.com" else "https://www.google.com") },
                shape = RoundedCornerShape(24.dp),
                color = if (isIncognito) Color(0xFF1E2638) else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(
                        1.dp,
                        if (isIncognito) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(
                        imageVector = if (isIncognito) Icons.Default.Security else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isIncognito) Color(0xFFA5B4FC) else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isIncognito) "Search privately or enter web address..." else "Search or enter web address...",
                        color = if (isIncognito) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Quick Shortcuts Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isIncognito) "Private Shortcuts" else "Quick Shortcuts",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isIncognito) Color.White else MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Shortcuts Grid (2 rows x 4 cols)
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(shortcuts) { shortcut ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectUrl(shortcut.url) }
                            .padding(vertical = 4.dp)
                            .testTag("shortcut_${shortcut.title.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isIncognito) Color(0xFF1E2638) else shortcut.accentColor.copy(alpha = 0.14f))
                                .border(
                                    1.dp,
                                    if (isIncognito) Color(0xFF334155) else shortcut.accentColor.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = shortcut.icon,
                                contentDescription = shortcut.title,
                                tint = if (isIncognito) Color(0xFFA5B4FC) else shortcut.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = shortcut.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isIncognito) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recently Visited Sites Section
        if (!isIncognito && recentHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recently Visited",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            val displayHistory = recentHistory.take(5)
            items(displayHistory.size) { index ->
                val item = displayHistory[index]
                Card(
                    onClick = { onSelectUrl(item.url) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.url,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
