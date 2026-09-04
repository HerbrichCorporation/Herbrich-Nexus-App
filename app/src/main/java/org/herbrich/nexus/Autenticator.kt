package org.herbrich.nexus

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import kotlinx.coroutines.runBlocking

class HerbrichAccountAuthenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    companion object {
        const val ACCOUNT_TYPE = "org.herbrich.accounts"
        const val AUTH_TOKEN_TYPE = "FullAccess"
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra("ACCOUNT_TYPE", accountType ?: ACCOUNT_TYPE)
            putExtra("AUTH_TYPE", authTokenType ?: AUTH_TOKEN_TYPE)
            // Wichtig: signalisiert, dass wir einen neuen Account hinzufügen
            putExtra("IS_ADDING_NEW_ACCOUNT", true)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        if (account == null) {
            return Bundle().apply {
                putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_ARGUMENTS)
                putString(AccountManager.KEY_ERROR_MESSAGE, "Account is null")
            }
        }

        val am = AccountManager.get(context)
        val tokenType = authTokenType ?: AUTH_TOKEN_TYPE

        // Zuerst vorhandenen Token prüfen
        var authToken = am.peekAuthToken(account, tokenType)

        // Falls kein Token → mit gespeichertem Passwort neu einloggen
        if (TextUtils.isEmpty(authToken)) {
            val password = am.getPassword(account)
            if (!password.isNullOrEmpty()) {
                authToken = runBlocking {
                    try {
                        val apiResponse = RetrofitClient.instance.login(
                            LoginRequest(account.name, password)
                        )
                        if (apiResponse.isSuccessful) {
                            val token = apiResponse.body()?.access_token
                            if (!token.isNullOrEmpty()) {
                                am.setAuthToken(account, tokenType, token)
                            }
                            token
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        }

        // Token vorhanden → zurückgeben
        if (!TextUtils.isEmpty(authToken)) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        }

        // Kein Token → LoginActivity erneut starten
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra("ACCOUNT_NAME", account.name)
            putExtra("ACCOUNT_TYPE", account.type)
            putExtra("AUTH_TYPE", tokenType)
            putExtra("IS_ADDING_NEW_ACCOUNT", false)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        return "Herbrich Nexus Full Access"
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        // Muss immer ein Bundle zurückgeben, niemals null
        return Bundle().apply {
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
        }
    }

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle? = null

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? = null

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle? = null
}