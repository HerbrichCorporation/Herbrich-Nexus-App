package org.herbrich.nexus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image

/**
 * Der offizielle Herbrich Nexus Header.
 *
 * @param isLoggedIn Steuert, ob der User-Status oder der Login-Button angezeigt wird.
 * @param username Der Name aus dem JWT (unique_name).
 * @param onLoginClick Aktion für den Login-Button.
 */
@Composable
fun HerbrichLogoHeader(
    isLoggedIn: Boolean,
    username: String? = null,
    onLoginClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LOGO (Links) ---
        Image(
            painter = painterResource(id = R.drawable.jenniferherbrich_herbrichcorporation),
            contentDescription = "Herbrich Corporation",
            modifier = Modifier
                .width(190.dp)
                .height(42.dp),
            contentScale = ContentScale.FillBounds
        )

        // --- STATUS / AUTH BEREICH (Rechts) ---
        Box(contentAlignment = Alignment.CenterEnd) {
            if (isLoggedIn && !username.isNullOrBlank()) {
                // Eingeloggter Zustand: Punkt + Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.05f), shape = CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Online Indikator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(color = Color(0xFF4CAF50), shape = CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = username.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                // Nicht eingeloggt: Kleiner schlichter Login-Button
                Button(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(34.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "LOGIN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}