package com.example.data.api

import com.example.data.model.DatabaseEntry
import com.example.data.model.SofaScoreResponse
import com.example.data.model.StandingsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface DatabaseApi {
    @GET("robinhossainraaj/rorax-iptv-database/main/database.json")
    suspend fun getDatabase(): List<DatabaseEntry>
}

interface SofaScoreApi {
    @GET("sport/{sport}/scheduled-events/{date}")
    suspend fun getScheduledEvents(
        @Path("sport") sport: String, // e.g. "football", "cricket", "mma"
        @Path("date") date: String,    // "YYYY-MM-DD"
        @Header("User-Agent") userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    ): SofaScoreResponse

    @GET("sport/{sport}/events/live")
    suspend fun getLiveEvents(
        @Path("sport") sport: String,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    ): SofaScoreResponse

    @GET("tournament/{tournamentId}/season/{seasonId}/standings/total")
    suspend fun getStandings(
        @Path("tournamentId") tournamentId: Long,
        @Path("seasonId") seasonId: Long,
        @Header("User-Agent") userAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    ): StandingsResponse
}
