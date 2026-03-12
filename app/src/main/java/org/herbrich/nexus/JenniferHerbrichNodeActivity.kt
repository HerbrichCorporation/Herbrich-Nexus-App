package org.herbrich.nexus

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme

class JenniferHerbrichNodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val nodeGuid = intent.getStringExtra("NODE_GUID")
            ?: intent.data?.lastPathSegment
            ?: ""

        setContent {
            HerbrichNexusTheme {
                var nodeData by remember { mutableStateOf<HerbrichNode?>(null) }
                var isLoading by remember { mutableStateOf(true) }
                var hasError by remember { mutableStateOf(false) }

                LaunchedEffect(nodeGuid) {
                    if (nodeGuid.isNotEmpty()) {
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
                            nodeData != null -> NodeDetailContent(nodeData!!)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NodeDetailContent(node: HerbrichNode) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Rundes Profilbild
        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
        ) {
            AsyncImage(
                model = node.imageUrl,
                contentDescription = node.nodeName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Titel
        Text(
            text = node.nodeName,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
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

        // 4. Standort (Interaktiv)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Standort", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clickable {
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
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("🗺️ In Google Maps öffnen", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}