package com.spop.poverlay.overlay.StatCards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spop.poverlay.overlay.OverlaySensorViewModel



@Composable
fun OverlayControls(

    sensorViewModel: OverlaySensorViewModel,

    onLockControls: () -> Unit = {}
) {
    Box {
        FloatingActionButton(
            onClick = {
                onLockControls()
                sensorViewModel.mutableSliderLockControls.value = false
            },
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier.size(56.dp)
        ) {

            val controlsLocked by sensorViewModel.lockControls.collectAsStateWithLifecycle(
                initialValue = false
            )

            val icon = if (controlsLocked == false) Icons.Default.Lock else Icons.Default.Edit
            val description = if (controlsLocked == false) "Controls Locked" else "Controls Unlocked"
            Icon(icon, contentDescription = description)
        }
    }
}
