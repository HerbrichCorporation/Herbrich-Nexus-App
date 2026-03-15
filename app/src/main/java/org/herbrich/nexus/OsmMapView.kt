package org.herbrich.nexus

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OsmMapView(modifier: Modifier = Modifier, latitude: Double, longitude: Double) {
    val context = LocalContext.current

    // Wir konfigurieren die Map nur einmal
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
        }
    }

    // Lifecycle-Handling: Die Map muss wissen, wann sie pausieren soll
    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            val nodeLocation = GeoPoint(latitude, longitude)
            view.controller.setCenter(nodeLocation)
        }
    )
}