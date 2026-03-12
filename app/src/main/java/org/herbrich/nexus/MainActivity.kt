package org.herbrich.nexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme

// 1. Das ViewModel: Holt die Daten im Hintergrund
class NexusViewModel : ViewModel() {
    var nodes by mutableStateOf<List<HerbrichNode>>(emptyList())
    var isLoading by mutableStateOf(false)

    fun loadNodes() {
        viewModelScope.launch {
            isLoading = true
            try {
                // Wir nutzen hier dein Retrofit-Singleton
                val response = RetrofitClient.instance.getNodes(page = 1)
                nodes = response.items
            } catch (e: Exception) {
                e.printStackTrace() // Falls das Internet mal hakt
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
                // ViewModel initialisieren
                val vm: NexusViewModel = viewModel()

                // Beim Start der App einmal die Daten laden
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

// 2. Die UI: Eine scrollbare Liste für deine Nodes
@Composable
fun NodeList(nodes: List<HerbrichNode>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        items(nodes) { node ->
            NodeCard(node = node)
        }
    }
}

@Composable
fun NodeCard(node: HerbrichNode) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp), // Schön abgerundete Ecken
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // --- Das Hero-Image ---
            AsyncImage(
                model = node.imageUrl,
                contentDescription = "Bild von ${node.herbrichName}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Feste Höhe für das Hero-Banner
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop // Bild füllt den Bereich sauber aus
            )

            // --- Der Text-Bereich ---
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
