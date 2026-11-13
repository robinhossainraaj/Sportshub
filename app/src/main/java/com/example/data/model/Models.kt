package com.example.data.model

// GitHub index database entry
data class DatabaseEntry(
    val name: String,
    val category: String,
    val description: String,
    val playlist: String,
    val channels: Int = 0,
    val updated: String = ""
)

// Channel parsed from M3U playlist files
data class Channel(
    val name: String,
    val logoUrl: String,
    val streamUrl: String,
    val category: String = ""
)

// SofaScore Match events mappings
data class SofaScoreResponse(
    val events: List<SofaScoreEvent>? = null
)

data class SofaScoreEvent(
    val id: Long,
    val homeTeam: Team,
    val awayTeam: Team,
    val homeScore: ScoreDetail? = null,
    val awayScore: ScoreDetail? = null,
    val status: StatusDetail,
    val startTimestamp: Long,
    val tournament: TournamentDetail,
    val roundInfo: RoundInfoDetail? = null
)

data class Team(
    val id: Long,
    val name: String
)

data class ScoreDetail(
    val current: Int? = null,
    val display: Int? = null
)

data class StatusDetail(
    val code: Int? = null,
    val description: String? = null,
    val type: String // "inprogress" for Live, "notstarted" for upcoming, "finished" for ended
)

data class TournamentDetail(
    val id: Long,
    val name: String,
    val category: SportCategory? = null
)

data class SportCategory(
    val name: String
)

data class RoundInfoDetail(
    val round: Int? = null
)

// SofaScore Standings mapping
data class StandingsResponse(
    val standings: List<StandingsContainer>? = null
)

data class StandingsContainer(
    val rows: List<StandingRow>? = null,
    val name: String? = null,
    val type: String? = null
)

data class StandingRow(
    val position: Int,
    val team: Team,
    val matches: Int? = null,
    val wins: Int? = null,
    val draws: Int? = null,
    val losses: Int? = null,
    val points: Int? = null,
    val scoresFor: Int? = null,
    val scoresAgainst: Int? = null
)
