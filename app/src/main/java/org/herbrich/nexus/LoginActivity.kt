package org.herbrich.nexus

import android.accounts.Account
import android.accounts.AccountManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerbrichNexusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginRequest = { user, pass, onFinished ->
                            performSystemLogin(user, pass, onFinished)
                        }
                    )
                }
            }
        }
    }
    private fun performSystemLogin(username: String, password: String, onFinished: (Boolean, String?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(username, password))

                if (response.isSuccessful && response.body() != null) {
                    val loginData = response.body()!!

                    // ✅ NUR Ergebnis zurückgeben – kein AccountManager hier!
                    val accountAuthenticatorResponse = intent.getParcelableExtra<android.accounts.AccountAuthenticatorResponse>(
                        AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE
                    )
                    accountAuthenticatorResponse?.onResult(Bundle().apply {
                        putString(AccountManager.KEY_ACCOUNT_NAME, username)
                        putString(AccountManager.KEY_ACCOUNT_TYPE, "org.herbrich.accounts")
                        putString(AccountManager.KEY_AUTHTOKEN, loginData.access_token)
                        putString("jh_user_id", loginData.jh_user_id.toString())
                        putString(AccountManager.KEY_PASSWORD, password)
                    })

                    withContext(Dispatchers.Main) {
                        onFinished(true, username)
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFinished(false, null)
                        Toast.makeText(this@LoginActivity, "Login abgelehnt: Prüfe deine Daten.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFinished(false, null)
                    Toast.makeText(this@LoginActivity, "Netzwerkfehler: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginRequest: (String, String, (Boolean, String?) -> Unit) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var loggedInUser by remember { mutableStateOf<String?>(null) }

    val isLoggedIn = loggedInUser != null

    Column(modifier = modifier.fillMaxSize()) {
        HerbrichLogoHeader(
            isLoggedIn = isLoggedIn,
            username = loggedInUser
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isLoggedIn) {
                Text(
                    text = "Nexus Authentifizierung",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Benutzername") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isLoading = true
                        onLoginRequest(username, password) { success, name ->
                            isLoading = false
                            if (success) loggedInUser = name
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Text("Willkommen zurück, $loggedInUser!", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Du bist jetzt mit der Herbrich Matrix verbunden.")
            }
        }
    }
}

@Composable
fun HerbrichLogoHeader(
    isLoggedIn: Boolean,
    username: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.jenniferherbrich_herbrichcorporation),
            contentDescription = "Herbrich Corporation",
            modifier = Modifier
                .width(180.dp)
                .height(40.dp),
            contentScale = ContentScale.FillBounds
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isLoggedIn) Color.Green else Color.Red,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoggedIn) username?.uppercase() ?: "ONLINE" else "OFFLINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}