package org.herbrich.nexus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable // WICHTIG: Dieser Import hat gefehlt!
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme

// 1. Das ViewModel: Bleibt so, wie es ist
class NexusViewModel : ViewModel() {
    var nodes by mutableStateOf<List<HerbrichNode>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun loadNodes() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getNodes(page = 1)
                nodes = response.items
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerbrichNexusTheme {
                val vm: NexusViewModel = viewModel()

                LaunchedEffect(Unit) {
                    vm.loadNodes()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NodeList(
                        nodes = vm.nodes,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// 2. Die Liste
// 2. Die Liste mit dem Header ganz oben
@Composable
fun NodeList(nodes: List<HerbrichNode>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Das Logo als allererstes Element der Liste festlegen
        item {
            HerbrichLogoHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp) // Etwas Abstand nach oben/unten
            )
        }

        // Danach folgen die Karten der Wohnhäuser
        items(nodes) { node ->
            NodeCard(node = node)
        }
    }
}

// 3. Die korrigierte NodeCard
@Composable
fun NodeCard(node: HerbrichNode) {
    val context = LocalContext.current // Holt den Context für den Intent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { // Hier ist der Klick-Handler jetzt richtig platziert!
                val intent = Intent(context, JenniferHerbrichNodeActivity::class.java).apply {
                    putExtra("NODE_GUID", node.hallAddress)
                }
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Hero-Image
            AsyncImage(
                model = node.imageUrl,
                contentDescription = "Bild von ${node.herbrichName}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )

            // Text-Bereich
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = node.herbrichName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = node.nodeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = node.nodeDescription,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}