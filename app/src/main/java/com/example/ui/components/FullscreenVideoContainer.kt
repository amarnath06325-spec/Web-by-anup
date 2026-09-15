package com.example.ui.components

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun FullscreenVideoContainer(
    customView: View,
    onExitFullscreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("fullscreen_video_container")
    ) {
        AndroidView(
            factory = { context ->
                FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Detach from parent if previously attached
                    (customView.parent as? ViewGroup)?.removeView(customView)
                    addView(
                        customView,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            update = { frameLayout ->
                if (customView.parent != frameLayout) {
                    (customView.parent as? ViewGroup)?.removeView(customView)
                    frameLayout.addView(
                        customView,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Exit Fullscreen Button
        FloatingActionButton(
            onClick = onExitFullscreen,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .size(44.dp)
                .testTag("exit_fullscreen_button")
        ) {
            Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = "Exit Fullscreen",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
