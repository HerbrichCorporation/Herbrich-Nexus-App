package org.herbrich.nexus

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import kotlinx.coroutines.runBlocking

class HerbrichAccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    // 1. ADD ACCOUNT: Öffnet deine LoginActivity
    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra("ACCOUNT_TYPE", accountType)
            putExtra("AUTH_TYPE", authTokenType)
            putExtra("IS_ADDING_NEW_ACCOUNT", true)
        }
        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }
    fun finishAddAccount(username: String, password: String, authToken: String, userId: String) {
        val am = AccountManager.get(context)
        val account = Account(username, "org.herbrich.accounts")
        // ✅ Nur hier ist addAccountExplicitly() erlaubt
        val added = am.addAccountExplicitly(account, password, null)
        if (added) {
            am.setAuthToken(account, "FullAccess", authToken)
            am.setUserData(account, "jh_user_id", userId)
        }
    }

    // 2. GET AUTH TOKEN: Das Herzstück für die API-Verbindung
    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val am = AccountManager.get(context)

        // Versuche erst, das Token aus dem Cache des Systems zu lesen
        var authToken = am.peekAuthToken(account, authTokenType)

        // WENN KEIN TOKEN DA ODER ABGELAUFEN -> RE-LOGIN GEGEN API
        if (TextUtils.isEmpty(authToken)) {
            val password = am.getPassword(account)
            if (password != null) {
                // Wir nutzen runBlocking, da der Authenticator synchron laufen muss
                authToken = runBlocking {
                    try {
                        val apiResponse = RetrofitClient.instance.login(
                            LoginRequest(account!!.name, password)
                        )
                        if (apiResponse.isSuccessful) {
                            apiResponse.body()?.access_token
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

        // Falls wir ein Token haben (entweder Cache oder frisch von der API)
        if (!TextUtils.isEmpty(authToken)) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        // FALLS ALLES FEHLSCHLÄGT: User muss sich neu einloggen (Activity öffnen)
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra("ACCOUNT_NAME", account?.name)
            putExtra("AUTH_TYPE", authTokenType)
        }
        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun editProperties(r: AccountAuthenticatorResponse?, t: String?) = null
    override fun confirmCredentials(r: AccountAuthenticatorResponse?, a: Account?, o: Bundle?) = null
    override fun updateCredentials(r: AccountAuthenticatorResponse?, a: Account?, t: String?, o: Bundle?) = null
    override fun hasFeatures(r: AccountAuthenticatorResponse?, a: Account?, f: Array<out String>?) = null
    override fun getAuthTokenLabel(authTokenType: String?) = "Herbrich Nexus Full Access"
}