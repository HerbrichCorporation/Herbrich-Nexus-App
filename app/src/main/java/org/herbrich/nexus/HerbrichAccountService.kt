package org.herbrich.nexus

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HerbrichAccountService : Service() {
    private lateinit var authenticator: HerbrichAccountAuthenticator

    override fun onCreate() {
        authenticator = HerbrichAccountAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return authenticator.iBinder
    }
}
