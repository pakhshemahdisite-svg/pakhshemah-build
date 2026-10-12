package com.pakhshmahdi.app.feature.checkout

import android.content.Context
import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pakhshmahdi.app.ui.theme.PMTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

data class MapAddressSelection(
    val latitude: Double,
    val longitude: Double,
    val addressLine: String = "",
    val provinceName: String = "",
    val cityName: String = ""
)

@Composable
fun IranMapAddressPicker(
    onDismiss: () -> Unit,
    onConfirm: (MapAddressSelection) -> Unit
) {
    val c = PMTheme.colors
    val context = LocalContext.current
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var resolvedSelection by remember { mutableStateOf<MapAddressSelection?>(null) }
    var resolving by remember { mutableStateOf(false) }
    var mapMessage by remember {
        mutableStateOf("روی محل دقیق آدرس در نقشه لمس کنید.")
    }

    val mapView = remember(context) {
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            controller.setZoom(5.3)
            controller.setCenter(GeoPoint(32.4279, 53.6880))
        }
    }

    val marker = remember(mapView) {
        Marker(mapView).apply {
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "آدرس انتخاب‌شده"
        }
    }

    val tapOverlay = remember(mapView) {
        MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(point: GeoPoint?): Boolean {
                if (point == null) return false

                if (!isInsideIran(point.latitude, point.longitude)) {
                    mapMessage = "لطفاً یک نقطه داخل ایران انتخاب کنید."
                    return true
                }

                selectedPoint = point
                marker.position = point
                if (!mapView.overlays.contains(marker)) {
                    mapView.overlays.add(marker)
                }
                mapView.invalidate()
                return true
            }

            override fun longPressHelper(point: GeoPoint?): Boolean =
                singleTapConfirmedHelper(point)
        })
    }

    DisposableEffect(mapView, tapOverlay) {
        if (!mapView.overlays.contains(tapOverlay)) {
            mapView.overlays.add(0, tapOverlay)
        }
        mapView.onResume()

        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(selectedPoint) {
        val point = selectedPoint ?: return@LaunchedEffect
        resolving = true
        mapMessage = "در حال دریافت آدرس این نقطه..."

        val result = reverseGeocode(
            context = context,
            latitude = point.latitude,
            longitude = point.longitude
        )

        resolvedSelection = result
        mapMessage = if (result.addressLine.isNotBlank()) {
            result.addressLine
        } else {
            "موقعیت انتخاب شد. در صورت نیاز متن آدرس را در فرم تکمیل کنید."
        }
        resolving = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.92f)
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = c.background,
            border = BorderStroke(1.dp, c.border),
            shadowElevation = 14.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "بستن",
                            tint = c.textPrimary
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "انتخاب آدرس از روی نقشه",
                            style = MaterialTheme.typography.titleLarge,
                            color = c.textPrimary
                        )
                        Text(
                            "نقشه ایران • لمس برای تعیین موقعیت",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textSecondary
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = c.accentGold.copy(alpha = .15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = c.primary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, c.border),
                    color = Color(0xFFEDEDED)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { mapView }
                    )
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp),
                    color = c.surface,
                    border = BorderStroke(1.dp, c.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (resolving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = c.primary
                            )
                        } else {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = c.primary,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Spacer(Modifier.width(9.dp))

                        Text(
                            mapMessage,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textSecondary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        resolvedSelection?.let(onConfirm)
                    },
                    enabled = resolvedSelection != null && !resolving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c.primary,
                        contentColor = c.onPrimary
                    )
                ) {
                    Text(
                        "تأیید این موقعیت",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private suspend fun reverseGeocode(
    context: Context,
    latitude: Double,
    longitude: Double
): MapAddressSelection = withContext(Dispatchers.IO) {
    val address = runCatching {
        Geocoder(context, Locale("fa", "IR"))
            .getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()
    }.getOrNull()

    val addressLine = address?.getAddressLine(0)
        ?.replace("ایران", "")
        ?.trim(' ', '،', ',')
        .orEmpty()

    MapAddressSelection(
        latitude = latitude,
        longitude = longitude,
        addressLine = addressLine,
        provinceName = address?.adminArea.orEmpty(),
        cityName = listOf(
            address?.locality,
            address?.subAdminArea,
            address?.subLocality
        ).firstOrNull { !it.isNullOrBlank() }.orEmpty()
    )
}

private fun isInsideIran(latitude: Double, longitude: Double): Boolean =
    latitude in 24.0..40.2 && longitude in 43.5..64.2
