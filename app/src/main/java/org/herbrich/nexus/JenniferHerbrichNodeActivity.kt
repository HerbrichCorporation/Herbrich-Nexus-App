package org.herbrich.nexus

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class JenniferHerbrichNodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ZWINGEND: Firebase hier händisch starten, bevor Compose oder Coroutinen loslegen
        com.google.firebase.FirebaseApp.initializeApp(this)
        // WICHTIG für OSM: Identifiziere deine App beim Karten-Server
        org.osmdroid.config.Configuration.getInstance().userAgentValue = "HerbrichNexus-App (https://www.herbrich.org)"
        enableEdgeToEdge()

        val nodeGuid = intent.getStringExtra("NODE_GUID")
            ?: intent.data?.lastPathSegment
            ?: ""

        setContent {
            HerbrichNexusTheme {
                var nodeData by remember { mutableStateOf<HerbrichNode?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var hasError by remember { mutableStateOf(false) }

                // --- HIER: Status-Variable deklarieren ---
                var ahState by remember { mutableStateOf(AleksandarHerbrichNodeState.Normal) }

                LaunchedEffect(nodeGuid) {
                    if (nodeGuid.isNotEmpty()) {
                        // 1. Firebase Live-Listener starten
                        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
                        val stateRef = database.getReference("node/$nodeGuid/ah-state")

                        stateRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
                            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                                val remoteValue = snapshot.getValue(String::class.java)?.lowercase() ?: "normal"
                                // Mapping: String aus Firebase -> Dein Enum
                                ahState = when (remoteValue) {
                                    "root" -> AleksandarHerbrichNodeState.RootActivity
                                    "jennijenni" -> AleksandarHerbrichNodeState.JenniJenni
                                    "matrix" -> AleksandarHerbrichNodeState.Matrix
                                    else -> AleksandarHerbrichNodeState.Normal
                                }
                            }
                            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                        })

                        // 2. API Call (Dein bestehender Code)
                        try {
                            isLoading = true
                            val response = RetrofitClient.instance.getNode(nodeGuid)
                            nodeData = response
                            hasError = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                            hasError = true
                        } finally {
                            isLoading = false
                        }
                    } else {
                        isLoading = false
                        hasError = true
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when {
                            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            hasError -> Text(
                                "Knoten nicht in der Matrix gefunden.",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Red
                            )
                            // --- HIER: ahState an die Content-Funktion übergeben ---
                            nodeData != null -> NodeDetailContent(nodeData!!, ahState)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NodeDetailContent(node: HerbrichNode, ahState: AleksandarHerbrichNodeState) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp), // Etwas weniger Padding oben für das Logo
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 0. Das Herbrich Logo (Wiederverwendbare Komponente) ---
        // Ersetze den fehlerhaften Aufruf (Zeile 131-139) durch diesen:
        val accountManager = AccountManager.get(context)

        var accounts by remember {
            mutableStateOf(accountManager.getAccountsByType("org.herbrich.accounts"))
        }

// ✅ Listener der auf Account-Änderungen im System reagiert
        DisposableEffect(Unit) {
            val listener = OnAccountsUpdateListener { allAccounts ->
                accounts = allAccounts.filter { it.type == "org.herbrich.accounts" }.toTypedArray()
            }
            accountManager.addOnAccountsUpdatedListener(listener, null, true)
            onDispose {
                accountManager.removeOnAccountsUpdatedListener(listener)
            }
        }

        val isLoggedIn = accounts.isNotEmpty()
        val currentUserName = if (isLoggedIn) accounts[0].name else null
        HerbrichLogoHeader(
            isLoggedIn = isLoggedIn,
            username = currentUserName,
            onLoginClick = {
                // Korrekte Kotlin-Syntax für den Intent:
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        )


        // 1. Rundes Profilbild (Zentrum der Detailseite)
        Box(
            modifier = Modifier
                .size(260.dp)
                // 1. Vergleich gegen das Enum-Mitglied, nicht gegen einen String
                .then(
                    if (ahState != AleksandarHerbrichNodeState.Normal)
                        Modifier.glowAnimation(ahState)
                    else Modifier
                )
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    // 2. Auch hier den Enum-Check nutzen
                    color = if (ahState == AleksandarHerbrichNodeState.Normal)
                        Color(0xFFE0E0E0)
                    else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = node.imageUrl,
                contentDescription = node.nodeName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        // 2. Titel & Herbrich Name
        var titleFontSize by remember { mutableStateOf(32.sp) } // Startgröße
        Text(
            text = node.nodeName, // z.B. "Wohngruppe Graumannsweg"
            fontSize = titleFontSize,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center, // Center-Ausrichtung sieht schöner aus (Jenni Jenni!)
            fontWeight = FontWeight.Bold,
            // Der "Trick" für den Abstand: lineHeight sollte ca. 1.1x bis 1.2x der fontSize sein
            lineHeight = (titleFontSize.value * 1.15f).sp,
            letterSpacing = (-0.5).sp, // Macht große Titel moderner
            maxLines = 2,
            softWrap = true,
            onTextLayout = { textLayoutResult ->
                // Wenn der Text trotz 2 Zeilen überläuft, Schriftgröße schrumpfen
                if (textLayoutResult.hasVisualOverflow && titleFontSize > 20.sp) {
                    titleFontSize *= 0.9f
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = node.herbrichName,
            fontSize = 18.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 3. Beschreibung
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Beschreibung", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            Text(
                text = node.nodeDescription,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                color = Color(0xFF333333)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 4. Standort (Interaktiv mit Google Maps Verknüpfung)
        // 4. Standort (Interaktiv mit OSM + Google Maps Link)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Standort in der Matrix", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            // 1. Die interaktive OSM Karte
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            ) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    latitude = node.latitude, // Deine API Koordinaten
                    longitude = node.longitude
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Der Google Maps Link direkt darunter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val bundle = Bundle().apply {
                            putString(FirebaseAnalytics.Param.ITEM_ID, node.hallAddress)
                            putString(FirebaseAnalytics.Param.ITEM_NAME, node.herbrichName)
                            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "google_maps_link")
                        }
                        // Das Event absenden
                        Firebase.analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                        val uri = Uri.parse("geo:${node.latitude},${node.longitude}?q=${node.latitude},${node.longitude}(${node.nodeName})")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🗺️ In Google Maps öffnen", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Finaler Abstand unten
        Spacer(modifier = Modifier.height(40.dp))
    }
}
@Composable
fun Modifier.glowAnimation(state: AleksandarHerbrichNodeState): Modifier {
    if (state == AleksandarHerbrichNodeState.Normal) return this

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 40f, // Etwas mehr für den "Glühen"-Effekt
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radius"
    )

    val color = when (state) {
        AleksandarHerbrichNodeState.RootActivity -> Color(0xFFFF0000)
        AleksandarHerbrichNodeState.JenniJenni -> Color(0xFF00BFFF) // DeepSkyBlue für JenniJenni
        AleksandarHerbrichNodeState.Matrix -> Color(0xFF00FF00)
        else -> Color.Transparent
    }

    return this.drawBehind { // drawBehind zeichnet UNTER dem Bild
        if (color != Color.Transparent) {
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                this.color = color.toArgb()
                // Der Schatten-Layer erzeugt das eigentliche Leuchten
                setShadowLayer(
                    glowRadius.dp.toPx(),
                    0f, 0f,
                    color.toArgb()
                )
            }

            drawIntoCanvas { canvas ->
                // Wir zeichnen einen Kreis, der genau so groß ist wie das Bild
                // Durch setShadowLayer glüht es über den Rand hinaus
                canvas.nativeCanvas.drawCircle(
                    center.x,
                    center.y,
                    (size.minDimension / 2) - 2.dp.toPx(), // Knapp unterm Rand
                    paint
                )
            }
        }
    }
}