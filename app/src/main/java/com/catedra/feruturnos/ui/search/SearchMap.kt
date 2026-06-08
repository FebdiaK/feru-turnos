package com.catedra.feruturnos.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catedra.feruturnos.R
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import org.osmdroid.util.GeoPoint

@Composable
fun SearchMap(
    userLocation: GeoPoint,
    enclosures: List<EnclosureItem>,
    selectedEnclosure: EnclosureItem?,
    focusedEnclosure: EnclosureItem?,
    onSelectedEnclosureChange: (EnclosureItem) -> Unit,
    userIconDrawable: android.graphics.drawable.Drawable?
) {
    val cameraState = key(userLocation) {
        rememberCameraState {
            geoPoint = userLocation
            zoom = 14.0
        }
    }

    LaunchedEffect(focusedEnclosure) {
        focusedEnclosure?.let {
            cameraState.geoPoint = it.location
            cameraState.zoom = 15.0
        }
    }

    val currentUserMarkerState = rememberMarkerState(
        geoPoint = userLocation
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        OpenStreetMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
            properties = DefaultMapProperties.copy(
                isTilesScaledToDpi = true,
                isMultiTouchControls = true,
                zoomButtonVisibility = ZoomButtonVisibility.SHOW_AND_FADEOUT
            )
        ) {
            enclosures.forEach { enclosure ->

                val markerState = rememberMarkerState(
                    key = enclosure.id,
                    geoPoint = enclosure.location
                )

                Marker(
                    state = markerState,
                    title = enclosure.name,
                    snippet = stringResource(R.string.predio_cercano),
                    onClick = {
                        onSelectedEnclosureChange(enclosure)
                        true
                    }
                )
            }
            Marker(
                state = currentUserMarkerState,
                title = stringResource(R.string.tu_ubicacion),
                snippet = stringResource(R.string.estas_aca),
                icon = userIconDrawable,
                onClick = { true }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1565C0))
                )

                Text(
                    text = stringResource(R.string.tu_ubicacion),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}