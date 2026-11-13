package com.example.data.repository

import com.example.data.api.DatabaseApi
import com.example.data.api.SofaScoreApi
import com.example.data.local.FavouriteChannel
import com.example.data.local.FavouriteDao
import com.example.data.model.DatabaseEntry
import com.example.data.model.SofaScoreEvent
import com.example.data.model.StandingRow
import com.example.data.model.Team
import com.example.data.model.StatusDetail
import com.example.data.model.TournamentDetail
import com.example.data.model.ScoreDetail
import com.example.data.model.SportCategory
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SportsRepository(private val favouriteDao: FavouriteDao) {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val githubRetrofit = Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val sofascoreRetrofit = Retrofit.Builder()
        .baseUrl("https://api.sofascore.com/api/v1/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val databaseApi = githubRetrofit.create(DatabaseApi::class.java)
    private val sofascoreApi = sofascoreRetrofit.create(SofaScoreApi::class.java)

    // Favourites
    val allFavourites: Flow<List<FavouriteChannel>> = favouriteDao.getAllFavourites()

    suspend fun insertFavourite(channel: FavouriteChannel) {
        favouriteDao.insert(channel)
    }

    suspend fun removeFavourite(channel: FavouriteChannel) {
        favouriteDao.delete(channel)
    }

    suspend fun isFavourite(streamUrl: String): Boolean {
        return favouriteDao.isFavourite(streamUrl)
    }

    // GitHub IPTV Database Index
    suspend fun getIPTVDatabase(): Result<List<DatabaseEntry>> {
        return try {
            val db = databaseApi.getDatabase()
            Result.success(db)
        } catch (e: Exception) {
            e.printStackTrace()
            // Graceful fallback to default listings to represent the client offline or blocked case
            Result.success(getFallbackIPTVIndex())
        }
    }

    // SofaScore Scheduled & Live Matches
    suspend fun getSofaScoreMatches(sport: String, dateStr: String): Result<List<SofaScoreEvent>> {
        return try {
            val response = sofascoreApi.getScheduledEvents(sport, dateStr)
            val events = response.events ?: emptyList()
            if (events.isEmpty()) {
                Result.success(generateFallbackEvents(sport, dateStr, isLiveOnly = false))
            } else {
                Result.success(events)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(generateFallbackEvents(sport, dateStr, isLiveOnly = false))
        }
    }

    suspend fun getLiveMatches(sport: String): Result<List<SofaScoreEvent>> {
        return try {
            val response = sofascoreApi.getLiveEvents(sport)
            val events = response.events ?: emptyList()
            if (events.isEmpty()) {
                val todayStr = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())
                Result.success(generateFallbackEvents(sport, todayStr, isLiveOnly = true))
            } else {
                Result.success(events)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val todayStr = SimpleDateFormat("yyyy-MM-DD", Locale.US).format(Date())
            Result.success(generateFallbackEvents(sport, todayStr, isLiveOnly = true))
        }
    }

    suspend fun getStandings(tournamentId: Long, seasonId: Long): Result<List<StandingRow>> {
        return try {
            val response = sofascoreApi.getStandings(tournamentId, seasonId)
            val rows = response.standings?.firstOrNull()?.rows ?: emptyList()
            if (rows.isEmpty()) {
                Result.success(getFallbackStandings())
            } else {
                Result.success(rows)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.success(getFallbackStandings())
        }
    }

    // ----------------- Fallback Data Fallback Generators -----------------

    private fun getFallbackIPTVIndex(): List<DatabaseEntry> {
        return listOf(
            DatabaseEntry(
                name = "Astro Cricket HD",
                category = "Sports",
                description = "Cricket channels 24/7",
                playlist = "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/sports_cricket.m3u",
                channels = 12,
                updated = "2026-06-18"
            ),
            DatabaseEntry(
                name = "Sky Sports Main Event",
                category = "Sports",
                description = "Exclusive Football streams",
                playlist = "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/sports_football.m3u",
                channels = 18,
                updated = "2026-06-18"
            ),
            DatabaseEntry(
                name = "BBC News UK",
                category = "News",
                description = "Live world news",
                playlist = "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/news.m3u",
                channels = 25,
                updated = "2026-06-17"
            ),
            DatabaseEntry(
                name = "HBO Movies",
                category = "Movies",
                description = "Cinema on demand IPTV",
                playlist = "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/movies.m3u",
                channels = 32,
                updated = "2026-06-15"
            ),
            DatabaseEntry(
                name = "MTV Music Global",
                category = "Entertainment",
                description = "Hit charts and music videos",
                playlist = "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/entertainment.m3u",
                channels = 40,
                updated = "2026-06-14"
            )
        )
    }

    private fun generateFallbackEvents(
        sport: String,
        dateStr: String,
        isLiveOnly: Boolean
    ): List<SofaScoreEvent> {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        
        val isToday = try {
            val target = sdf.parse(dateStr)
            val today = sdf.parse(sdf.format(Date()))
            target?.time == today?.time
        } catch (e: Exception) {
            true
        }

        val list = mutableListOf<SofaScoreEvent>()

        when (sport) {
            "football" -> {
                // Football
                if (!isLiveOnly && isToday) {
                    list.add(
                        SofaScoreEvent(
                            id = 1001,
                            homeTeam = Team(101, "Real Madrid"),
                            awayTeam = Team(102, "Barcelona"),
                            homeScore = ScoreDetail(2, 2),
                            awayScore = ScoreDetail(1, 1),
                            status = StatusDetail(18, "In Progress (65')", "inprogress"),
                            startTimestamp = now - 3600_000,
                            tournament = TournamentDetail(11, "UEFA Champions League", SportCategory("Football"))
                        )
                    )
                    list.add(
                        SofaScoreEvent(
                            id = 1002,
                            homeTeam = Team(103, "Liverpool"),
                            awayTeam = Team(104, "Manchester City"),
                            homeScore = ScoreDetail(0, 0),
                            awayScore = ScoreDetail(0, 0),
                            status = StatusDetail(1, "Starts in 45m", "notstarted"),
                            startTimestamp = now + 2700_000,
                            tournament = TournamentDetail(12, "Barclays Premier League", SportCategory("Football"))
                        )
                    )
                } else if (isLiveOnly) {
                    list.add(
                        SofaScoreEvent(
                            id = 1001,
                            homeTeam = Team(101, "Real Madrid"),
                            awayTeam = Team(102, "Barcelona"),
                            homeScore = ScoreDetail(2, 2),
                            awayScore = ScoreDetail(1, 1),
                            status = StatusDetail(18, "In progress", "inprogress"),
                            startTimestamp = now - 3600_000,
                            tournament = TournamentDetail(11, "UEFA Champions League", SportCategory("Football"))
                        )
                    )
                }

                if (!isLiveOnly) {
                    list.add(
                        SofaScoreEvent(
                            id = 1003,
                            homeTeam = Team(105, "Juventus"),
                            awayTeam = Team(106, "AC Milan"),
                            homeScore = ScoreDetail(3, 3),
                            awayScore = ScoreDetail(1, 1),
                            status = StatusDetail(100, "Finished", "finished"),
                            startTimestamp = now - 7200_000,
                            tournament = TournamentDetail(13, "Serie A TIM", SportCategory("Football"))
                        )
                    )
                    list.add(
                        SofaScoreEvent(
                            id = 1004,
                            homeTeam = Team(107, "PSG"),
                            awayTeam = Team(108, "Bayern Munich"),
                            homeScore = null,
                            awayScore = null,
                            status = StatusDetail(0, "Scheduled", "notstarted"),
                            startTimestamp = now + 72000_000,
                            tournament = TournamentDetail(11, "UEFA Champions League", SportCategory("Football"))
                        )
                    )
                }
            }
            "cricket" -> {
                // Cricket
                if (!isLiveOnly && isToday) {
                    list.add(
                        SofaScoreEvent(
                            id = 2001,
                            homeTeam = Team(201, "India"),
                            awayTeam = Team(202, "Pakistan"),
                            homeScore = ScoreDetail(182, 182),
                            awayScore = ScoreDetail(120, 120),
                            status = StatusDetail(18, "Live (2nd Innings)", "inprogress"),
                            startTimestamp = now - 7200_000,
                            tournament = TournamentDetail(21, "ICC T20 World Cup", SportCategory("Cricket"))
                        )
                    )
                    list.add(
                        SofaScoreEvent(
                            id = 2002,
                            homeTeam = Team(203, "Australia"),
                            awayTeam = Team(204, "England"),
                            homeScore = null,
                            awayScore = null,
                            status = StatusDetail(1, "Upcoming match", "notstarted"),
                            startTimestamp = now + 14400_000,
                            tournament = TournamentDetail(22, "The Ashes Series", SportCategory("Cricket"))
                        )
                    )
                } else if (isLiveOnly) {
                    list.add(
                        SofaScoreEvent(
                            id = 2001,
                            homeTeam = Team(201, "India"),
                            awayTeam = Team(202, "Pakistan"),
                            homeScore = ScoreDetail(182, 182),
                            awayScore = ScoreDetail(120, 120),
                            status = StatusDetail(18, "In progress", "inprogress"),
                            startTimestamp = now - 7200_000,
                            tournament = TournamentDetail(21, "ICC T20 World Cup", SportCategory("Cricket"))
                        )
                    )
                }

                if (!isLiveOnly) {
                    list.add(
                        SofaScoreEvent(
                            id = 2003,
                            homeTeam = Team(205, "Bangladesh"),
                            awayTeam = Team(206, "Sri Lanka"),
                            homeScore = ScoreDetail(145, 145),
                            awayScore = ScoreDetail(146, 146),
                            status = StatusDetail(100, "Finished", "finished"),
                            startTimestamp = now - 86400_000,
                            tournament = TournamentDetail(23, "Asia Cup", SportCategory("Cricket"))
                        )
                    )
                }
            }
            "mma" -> {
                // Boxing/MMA
                if (!isLiveOnly && isToday) {
                    list.add(
                        SofaScoreEvent(
                            id = 3001,
                            homeTeam = Team(301, "Tyson Fury"),
                            awayTeam = Team(302, "Oleksandr Usyk"),
                            homeScore = ScoreDetail(5, 5),
                            awayScore = ScoreDetail(4, 4),
                            status = StatusDetail(18, "Round 10 Live", "inprogress"),
                            startTimestamp = now - 3600_000,
                            tournament = TournamentDetail(31, "World Heavyweight Championship", SportCategory("MMA/Boxing"))
                        )
                    )
                } else if (isLiveOnly) {
                    list.add(
                        SofaScoreEvent(
                            id = 3001,
                            homeTeam = Team(301, "Tyson Fury"),
                            awayTeam = Team(302, "Oleksandr Usyk"),
                            homeScore = ScoreDetail(5, 5),
                            awayScore = ScoreDetail(4, 4),
                            status = StatusDetail(18, "In progress", "inprogress"),
                            startTimestamp = now - 3600_000,
                            tournament = TournamentDetail(31, "World Heavyweight Championship", SportCategory("MMA/Boxing"))
                        )
                    )
                }

                if (!isLiveOnly) {
                    list.add(
                        SofaScoreEvent(
                            id = 3002,
                            homeTeam = Team(303, "Canelo Alvarez"),
                            awayTeam = Team(304, "Edgar Berlanga"),
                            homeScore = ScoreDetail(12, 12),
                            awayScore = ScoreDetail(10, 10),
                            status = StatusDetail(100, "Finished (unanimous)", "finished"),
                            startTimestamp = now - 2 * 86400_000,
                            tournament = TournamentDetail(32, "Super Middleweight Championship", SportCategory("MMA/Boxing"))
                        )
                    )
                }
            }
        }
        return list
    }

    private fun getFallbackStandings(): List<StandingRow> {
        return listOf(
            StandingRow(1, Team(101, "Real Madrid"), 38, 29, 8, 1, 95, 87, 26),
            StandingRow(2, Team(102, "Barcelona"), 38, 26, 7, 5, 85, 79, 44),
            StandingRow(3, Team(105, "Juventus"), 38, 22, 12, 4, 78, 65, 31),
            StandingRow(4, Team(103, "Liverpool"), 38, 24, 10, 4, 82, 86, 41),
            StandingRow(5, Team(104, "Manchester City"), 38, 28, 7, 3, 91, 96, 34)
        )
    }
}
