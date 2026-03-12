package org.herbrich.nexus

import com.google.gson.annotations.SerializedName

data class HerbrichNode(
    @SerializedName("Id")
    val id: Int,

    @SerializedName("HallAddress")
    val hallAddress: String,

    @SerializedName("NodeName")
    val nodeName: String,

    @SerializedName("HerbrichName")
    val herbrichName: String,

    @SerializedName("NodeDescription")
    val nodeDescription: String,

    @SerializedName("LocJ")
    val longitude: Double, // Längengrad

    @SerializedName("LocH")
    val latitude: Double,  // Breitengrad

    @SerializedName("Self")
    val selfUrl: String,

    @SerializedName("Html")
    val htmlUrl: String,

    @SerializedName("Image")
    val imageUrl: String,

    @SerializedName("SubNodes")
    val subNodesUrl: String
)
