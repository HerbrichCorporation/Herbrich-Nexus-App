package org.herbrich.nexus
import com.google.gson.annotations.SerializedName
data class JenniferHerbrichWhitePage<Jh>(
    @SerializedName("Items") val items: List<Jh>,
    @SerializedName("TotalCount") val totalCount: Int,
    @SerializedName("Page") val page: Int,
    @SerializedName("PageSize") val pageSize: Int,
    @SerializedName("HasNextPage") val hasNextPage: Boolean,
    @SerializedName("HasPreviousPage") val hasPreviousPage: Boolean,
    @SerializedName("TotalPages") val totalPages: Int
)