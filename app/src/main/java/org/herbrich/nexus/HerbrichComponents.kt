package org.herbrich.nexus
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
// Einfach eine Top-Level Funktion
@Composable
fun HerbrichLogoHeader(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.jenniferherbrich_herbrichcorporation),
        contentDescription = "Herbrich Corporation",
        modifier = modifier
            // Das Verhältnis der Webseite (350x73) auf DP übertragen:
            .width(240.dp)
            .height(50.dp)
            .padding(vertical = 8.dp),
        // FillBounds ignoriert das interne Seitenverhältnis der SVG
        // und zwingt sie in deine Maße (wie im Browser-CSS)
        contentScale = ContentScale.FillBounds
    )
}