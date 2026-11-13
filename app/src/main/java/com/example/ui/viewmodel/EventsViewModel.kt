package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SofaScoreEvent
import com.example.data.model.StandingRow
import com.example.data.repository.SportsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class EventsViewModel(private val repository: SportsRepository) : ViewModel() {

    private val _selectedSport = MutableStateFlow("All") // "All", "Football", "Cricket", "Boxing", "Favourites"
    val selectedSport: StateFlow<String> = _selectedSport.asStateFlow()

    private val _selectedTimeFilter = MutableStateFlow("Live") // "All", "Live", "Upcoming", "Today", "Tomorrow", "Finished"
    val selectedTimeFilter: StateFlow<String> = _selectedTimeFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _matchesState = MutableStateFlow<UiState<List<SofaScoreEvent>>>(UiState.Loading)
    val matchesState: StateFlow<UiState<List<SofaScoreEvent>>> = _matchesState.asStateFlow()

    private val _standingsState = MutableStateFlow<UiState<List<StandingRow>>>(UiState.Loading)
    val standingsState: StateFlow<UiState<List<StandingRow>>> = _standingsState.asStateFlow()

    // Favourites list collected raw from SQL Flow
    val favouriteChannels = repository.allFavourites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private var refreshJob: Job? = null

    init {
        loadData()
        startAutoRefresh()
    }

    fun selectSport(sport: String) {
        _selectedSport.value = sport
        loadData()
    }

    fun selectTimeFilter(filter: String) {
        _selectedTimeFilter.value = filter
        loadData()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadData() {
        viewModelScope.launch {
            _matchesState.value = UiState.Loading
            _standingsState.value = UiState.Loading

            val sport = _selectedSport.value
            val timeFilter = _selectedTimeFilter.value

            val todayStr = getFormattedDate(0)
            val tomorrowStr = getFormattedDate(1)

            val apiSportName = when (sport) {
                "Football" -> "football"
                "Cricket" -> "cricket"
                "Boxing" -> "mma"
                else -> "football" // Default matching
            }

            // If selected sport is Favourites, we don't need SofaScore API fetches
            if (sport == "Favourites") {
                _matchesState.value = UiState.Success(emptyList())
                _standingsState.value = UiState.Success(emptyList())
                return@launch
            }

            // Load matches
            val dateToLoad = when (timeFilter) {
                "Tomorrow" -> tomorrowStr
                else -> todayStr
            }

            val sportList = if (sport == "All") {
                listOf("football", "cricket", "mma")
            } else {
                listOf(apiSportName)
            }

            val allFetchedEvents = mutableListOf<SofaScoreEvent>()
            
            for (s in sportList) {
                if (timeFilter == "Live") {
                    repository.getLiveMatches(s).onSuccess {
                        allFetchedEvents.addAll(it)
                    }
                } else {
                    repository.getSofaScoreMatches(s, dateToLoad).onSuccess {
                        allFetchedEvents.addAll(it)
                    }
                }
            }

            // Filter by time code
            val filtered = filterEvents(allFetchedEvents, timeFilter)
            _matchesState.value = UiState.Success(filtered)

            // Load Standings (Example: UCL tournament id = 7, season id = 61625 / fallback id)
            repository.getStandings(7, 61625).onSuccess {
                _standingsState.value = UiState.Success(it)
            }.onFailure {
                _standingsState.value = UiState.Error(it.message ?: "Failed loading standings")
            }
        }
    }

    private fun filterEvents(events: List<SofaScoreEvent>, timeFilter: String): List<SofaScoreEvent> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        val startOfTomorrow = startOfToday + 86400_000
        val endOfTomorrow = startOfTomorrow + 86400_000

        var list = events

        list = when (timeFilter) {
            "Live" -> list.filter { it.status.type == "inprogress" }
            "Upcoming" -> list.filter { it.status.type == "notstarted" }
            "Today" -> list.filter { it.startTimestamp in startOfToday until startOfTomorrow }
            "Tomorrow" -> list.filter { it.startTimestamp in startOfTomorrow until endOfTomorrow }
            "Finished" -> list.filter { it.status.type == "finished" }
            else -> list // All
        }

        // Apply order: Live -> Upcoming -> Finished
        return list.sortedWith { m1, m2 ->
            val p1 = getSortingPriority(m1.status.type)
            val p2 = getSortingPriority(m2.status.type)
            if (p1 != p2) {
                p1.compareTo(p2)
            } else {
                m1.startTimestamp.compareTo(m2.startTimestamp)
            }
        }
    }

    private fun getSortingPriority(type: String): Int {
        return when (type) {
            "inprogress" -> 1
            "notstarted" -> 2
            "finished" -> 3
            else -> 4
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(30_000) // 30 seconds timer
                // Auto-refresh match info silently or gracefully
                val timeFilter = _selectedTimeFilter.value
                val sport = _selectedSport.value
                if (sport != "Favourites") {
                    val todayStr = getFormattedDate(0)
                    val tomorrowStr = getFormattedDate(1)
                    val apiSportName = when (sport) {
                        "Football" -> "football"
                        "Cricket" -> "cricket"
                        "Boxing" -> "mma"
                        else -> "football"
                    }
                    val dateToLoad = if (timeFilter == "Tomorrow") tomorrowStr else todayStr
                    val sportList = if (sport == "All") listOf("football", "cricket", "mma") else listOf(apiSportName)
                    
                    val allFetchedEvents = mutableListOf<SofaScoreEvent>()
                    for (s in sportList) {
                        if (timeFilter == "Live") {
                            repository.getLiveMatches(s).onSuccess { allFetchedEvents.addAll(it) }
                        } else {
                            repository.getSofaScoreMatches(s, dateToLoad).onSuccess { allFetchedEvents.addAll(it) }
                        }
                    }
                    val filtered = filterEvents(allFetchedEvents, timeFilter)
                    _matchesState.value = UiState.Success(filtered)
                }
            }
        }
    }

    private fun getFormattedDate(offsetDays: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, offsetDays)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(calendar.time)
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
