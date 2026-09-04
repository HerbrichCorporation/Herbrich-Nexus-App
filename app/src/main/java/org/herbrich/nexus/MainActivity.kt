package org.herbrich.nexus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerbrichNexusTheme(darkTheme = true, dynamicColor = false) {
                val infinite = rememberInfiniteTransition(label = "rq")
                val scale by infinite.animateFloat(1f, 1.08f, infiniteRepeatable(tween(2200), RepeatMode.Reverse))
                val glitch by infinite.animateFloat(-2f, 2f, infiniteRepeatable(tween(120), RepeatMode.Reverse))

                Surface(Modifier.fillMaxSize(), color = Color(0xFF050507)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                                Image(painterResource(R.drawable.jenniherbrich_redqueen), null, Modifier.size(220.dp).graphicsLayer{translationX=glitch}.alpha(0.35f), colorFilter = ColorFilter.tint(Color(0xFF00B7FF)))
                                Image(painterResource(R.drawable.jenniherbrich_redqueen), null, Modifier.size(220.dp).graphicsLayer{translationX=-glitch}.alpha(0.35f), colorFilter = ColorFilter.tint(Color(0xFF00FF00)))
                                Image(painterResource(R.drawable.jenniherbrich_redqueen), "RedQueen", Modifier.size(220.dp).graphicsLayer{scaleX=scale; scaleY=scale}, contentScale = ContentScale.Fit)
                            }
                            Spacer(Modifier.height(18.dp))
                            Text("RED QUEEN // SYSTEM ACTIVE", color = Color(0xFFFF2A2D), fontFamily = FontFamily.Monospace, fontSize = 11.sp, letterSpacing = 2.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("MATRIX.HERBRICH.ORG // NODES READY", color = Color.Gray, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                            Spacer(Modifier.height(32.dp))
                            Button(onClick = { startActivity(Intent(this@MainActivity, NodesActivity::class.java)) }, colors = ButtonDefaults.buttonColors(Color(0xFFFF2A2D), Color.Black), shape = RoundedCornerShape(4.dp)) {
                                Text("ENTER NODES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}