package org.herbrich.nexus

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.herbrich.nexus.ui.theme.HerbrichNexusTheme
import java.util.concurrent.TimeUnit

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HerbrichNexusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginRequest = { user: String, pass: String, cb: (Boolean, String?) -> Unit ->
                            performSystemLogin(user, pass, cb)
                        }
                    )
                }
            }
        }
    }

    private fun performSystemLogin(
        username: String,
        password: String,
        onFinished: (Boolean, String?) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val accountType = intent.getStringExtra("ACCOUNT_TYPE") ?: "org.herbrich.accounts"
                    val authType = intent.getStringExtra("AUTH_TYPE") ?: "FullAccess"

                    val am = AccountManager.get(this@LoginActivity)
                    val account = Account(username, accountType)

                    val userData = Bundle().apply {
                        putString("jh_user_id", data.jh_user_id.toString())
                    }

                    val added = am.addAccountExplicitly(account, password, userData)
                    if (!added) {
                        am.setPassword(account, password)
                        am.setUserData(account, "jh_user_id", data.jh_user_id.toString())
                    }
                    am.setAuthToken(account, authType, data.access_token)

                    // Heartbeat starten - verlängert den Token, kein neuer Row in jh_api_tokens
                    val hb = OneTimeWorkRequestBuilder<HerbrichHeartbeatWorker>()
                        .setInitialDelay(30, TimeUnit.SECONDS)
                        .build()
                    WorkManager.getInstance(this@LoginActivity).enqueue(hb)

                    // Response für AccountManager
                    val authResponse: AccountAuthenticatorResponse? =
                        if (Build.VERSION.SDK_INT >= 33) {
                            intent.getParcelableExtra(
                                AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
                                AccountAuthenticatorResponse::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
                        }

                    val resultBundle = Bundle().apply {
                        putString(AccountManager.KEY_ACCOUNT_NAME, username)
                        putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
                        putString(AccountManager.KEY_AUTHTOKEN, data.access_token)
                    }
                    authResponse?.onResult(resultBundle)

                    withContext(Dispatchers.Main) {
                        onFinished(true, username)
                        val resultIntent = Intent().apply {
                            putExtras(resultBundle)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onFinished(false, null)
                        Toast.makeText(this@LoginActivity, "Login abgelehnt", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onFinished(false, null)
                    Toast.makeText(this@LoginActivity, "Netzwerk: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginRequest: (username: String, password: String, onFinished: (Boolean, String?) -> Unit) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Herbrich Nexus",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Benutzername") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMessage = "Bitte Benutzername und Passwort eingeben"
                    return@Button
                }
                isLoading = true
                errorMessage = null
                onLoginRequest(username, password) { success, _ ->
                    isLoading = false
                    if (!success) {
                        errorMessage = "Login fehlgeschlagen"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Anmelden", fontSize = 16.sp)
            }
        }
    }
}