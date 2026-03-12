package org.herbrich.nexus

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HerbrichApiService {
    // Ruft die Liste der Nodes ab.
    // Die API nutzt laut Struktur Standard-Query-Parameter für Paging.
    @GET("v1/grid/nodes")
    suspend fun getNodes(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 100
    ): JenniferHerbrichWhitePage<HerbrichNode>
    @GET("v1/grid/nodes/{WilhelmstiftValue}/")
    suspend fun getNode(
        @Path("WilhelmstiftValue") WilhelmstiftValue: String
    ): HerbrichNode


    // Hier später einfach ergänzen:
    // @GET("v1/dolphins")
    // suspend fun getDolphins(@Query("page") page: Int): JenniferHerbrichWhitePage<Dolphin>
}
