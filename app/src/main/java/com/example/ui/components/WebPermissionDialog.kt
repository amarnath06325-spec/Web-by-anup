package com.example.ui.components

import android.webkit.PermissionRequest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WebPermissionDialog(
    request: PermissionRequest,
    onGrant: (Array<String>) -> Unit,
    onDeny: () -> Unit
) {
    val origin = request.origin.host.orEmpty().ifBlank { request.origin.toString() }
    val resources = request.resources
    val isCamera = resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
    val isAudio = resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)

    val permissionDesc = when {
        isCamera && isAudio -> "Camera and Microphone"
        isCamera -> "Camera"
        isAudio -> "Microphone"
        else -> "Media hardware"
    }

    AlertDialog(
        onDismissRequest = onDeny,
        icon = {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "WebRTC Permission Request",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "$origin is requesting access to your $permissionDesc for video/audio streaming.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Do you want to allow this website to access your device?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onGrant(resources) },
                modifier = Modifier.testTag("grant_webrtc_permission")
            ) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDeny,
                modifier = Modifier.testTag("deny_webrtc_permission")
            ) {
                Text("Block")
            }
        }
    )
}

@Composable
fun GeolocationDialog(
    origin: String,
    onAllow: () -> Unit,
    onBlock: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onBlock,
        icon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Location Access Request",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "$origin is requesting your physical location to provide location-based services.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant HTML5 Geolocation API permission?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAllow,
                modifier = Modifier.testTag("grant_geolocation_permission")
            ) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onBlock,
                modifier = Modifier.testTag("deny_geolocation_permission")
            ) {
                Text("Block")
            }
        }
    )
}
