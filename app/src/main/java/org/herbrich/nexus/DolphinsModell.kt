package org.herbrich.nexus

import com.google.gson.annotations.SerializedName

// --- LOGIN ---
data class LoginRequest(
    @SerializedName("Username") val username: String,
    @SerializedName("Password") val password: String
)

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val jh_user_id: String
)

// --- DOLPHIN CREATE (POST) ---
data class AddDolphinRequest(
    val DolphinEntity: String? = null,
    val FirstName: String,
    val LastName: String,
    val Story: String, // Entspricht deiner VB.NET Property
    val ImageBase64: String?,
    val DelphinariatId: Int,
    val StaticContext: Int
)

// --- DOLPHIN LIST (GET) ---
data class DolphinItem(
    val DolphinId: Int,
    val DolphinEntity: String,
    val FirstName: String,
    val LastName: String,
    val Description: String, // Im GET-JSON heißt es Description
    val Image: String,
    val FullName: String,
    val StaticContext: StaticContextInfo,
    val Delphinariat: String,
    val CreatedAtUtc: String
)

data class StaticContextInfo(
    val Id: String,
    val Name: String,
    val Description: String
)
