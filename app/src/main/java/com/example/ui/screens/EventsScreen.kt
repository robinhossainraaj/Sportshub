package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FavouriteChannel
import com.example.data.model.SofaScoreEvent
import com.example.data.model.StandingRow
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BadgeBg
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgMain
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.CircleBg
import com.example.ui.theme.RibbonBg
import com.example.ui.theme.TextDate
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.EventsViewModel
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventsScreen(
    eventsViewModel: EventsViewModel,
    onMenuClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onChannelClick: (FavouriteChannel) -> Unit
) {
    val selectedSport by eventsViewModel.selectedSport.collectAsState()
    val selectedTimeFilter by eventsViewModel.selectedTimeFilter.collectAsState()
    val searchQuery by eventsViewModel.searchQuery.collectAsState()

    val matchesState by eventsViewModel.matchesState.collectAsState()
    val standingsState by eventsViewModel.standingsState.collectAsState()
    val favourites by eventsViewModel.favouriteChannels.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .testTag("events_screen")
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 18.dp, end = 18.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("hamburger_menu")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { eventsViewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search...", color = TextSecondary, fontSize = 14.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BorderAccent,
                        unfocusedBorderColor = Color(0xFF33343D)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .heightIn(max = 50.dp)
                        .testTag("search_input")
                )
            } else {
                Text(
                    text = "Sports24",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("app_title")
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavouriteClick) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Saved Favourites",
                        tint = if (selectedSport == "Favourites") BorderAccent else TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { eventsViewModel.loadData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Matches",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Matches",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // 2. Announcement Banner (Marquee)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .border(1.dp, BorderAccent, RoundedCornerShape(10.dp))
                .padding(vertical = 10.dp, horizontal = 14.dp)
                .testTag("marquee_banner")
        ) {
            Text(
                text = "🏆 Welcome to Sports24 — Live Football • Cricket • Boxing • 24/7 Sports Streaming        🏆 Welcome to Sports24 — Live Football • Cricket • Boxing • 24/7 Sports Streaming",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(iterations = Int.MAX_VALUE)
                    .testTag("marquee_text")
            )
        }

        // 3. Category Circle Row (All, Football, Cricket, Boxing, Favourites)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CategoryItem(
                title = "All",
                icon = Icons.Default.List,
                isSelected = selectedSport == "All",
                badgeCount = 5,
                onClick = { eventsViewModel.selectSport("All") }
            )
            CategoryItem(
                title = "Football",
                icon = Icons.Default.Star,
                isSelected = selectedSport == "Football",
                badgeCount = 3,
                onClick = { eventsViewModel.selectSport("Football") }
            )
            CategoryItem(
                title = "Cricket",
                icon = Icons.Default.Star,
                isSelected = selectedSport == "Cricket",
                badgeCount = 2,
                onClick = { eventsViewModel.selectSport("Cricket") }
            )
            CategoryItem(
                title = "Boxing",
                icon = Icons.Default.Star,
                isSelected = selectedSport == "Boxing",
                badgeCount = 1,
                onClick = { eventsViewModel.selectSport("Boxing") }
            )
            CategoryItem(
                title = "Favourites",
                icon = Icons.Default.Star,
                isSelected = selectedSport == "Favourites",
                badgeCount = favourites.size,
                onClick = { eventsViewModel.selectSport("Favourites") }
            )
        }

        // 4. Time range Filter Pills Row (Only visible if Sport is NOT Favourites)
        if (selectedSport != "Favourites") {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                val filters = listOf("All", "Live", "Upcoming", "Today", "Tomorrow", "Finished")
                items(filters) { filter ->
                    FilterPill(
                        title = filter,
                        isSelected = selectedTimeFilter == filter,
                        onClick = { eventsViewModel.selectTimeFilter(filter) }
                    )
                }
            }
        }

        // 5. Match List & Standings Section (Scrollable Layout)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            if (selectedSport == "Favourites") {
                // Show local fav channels
                if (favourites.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No custom favourites bookmarked",
                            subtext = "Long-press any channel in listing cards to add ⭐"
                        )
                    }
                } else {
                    items(favourites) { fav ->
                        FavouriteChannelRow(fav = fav, onClick = { onChannelClick(fav) })
                    }
                }
            } else {
                // SofaScore Schedules List
                when (val state = matchesState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = BorderAccent)
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            EmptyStateView(
                                title = "Failed loading schedules",
                                subtext = state.message
                            )
                        }
                    }
                    is UiState.Success -> {
                        val list = state.data.filter {
                            it.homeTeam.name.contains(searchQuery, ignoreCase = true) ||
                                    it.awayTeam.name.contains(searchQuery, ignoreCase = true) ||
                                    it.tournament.name.contains(searchQuery, ignoreCase = true)
                        }

                        if (list.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = "No active scheduled matches",
                                    subtext = "Check alternative sport category or active time scope"
                                )
                            }
                        } else {
                            items(list) { event ->
                                MatchCardItem(event = event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("category_circle_${title.lowercase()}")
    ) {
        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) BorderAccent else Color(0xFF33343D),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(RibbonBg, CircleShape)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = if (isSelected) BorderAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun FilterPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .border(
                width = 1.5.dp,
                color = BorderAccent,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                color = if (isSelected) BorderAccent else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .testTag("filter_pill_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LivePulseSignalIcon(pulseAlpha: Float) {
    Canvas(
        modifier = Modifier
            .size(16.dp)
            .graphicsLayer(
                scaleX = pulseAlpha * 0.15f + 0.85f,
                scaleY = pulseAlpha * 0.15f + 0.85f,
                alpha = pulseAlpha
            )
    ) {
        val strokeWidth = 2.dp.toPx()
        // Draw center dot
        drawCircle(
            color = AccentPink,
            radius = 1.5.dp.toPx(),
            center = center
        )
        
        // Left waves
        drawArc(
            color = AccentPink,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - 5.dp.toPx(), center.y - 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        // Right waves
        drawArc(
            color = AccentPink,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - 5.dp.toPx(), center.y - 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        // Outer broad waves
        drawArc(
            color = AccentPink,
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - 9.dp.toPx(), center.y - 9.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 18.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = AccentPink,
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - 9.dp.toPx(), center.y - 9.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 18.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun MatchCardItem(event: SofaScoreEvent) {
    val isLive = event.status.type == "inprogress"
    
    // Live Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .border(2.dp, BorderAccent, RoundedCornerShape(16.dp))
            .testTag("match_card_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            
            // Ribbon on Top-Right Corner for live / hot matches
            val ribbonLabel = when {
                isLive -> "LIVE"
                event.status.type == "notstarted" && event.id % 2L == 0L -> "HOT"
                else -> null
            }

            if (ribbonLabel != null) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(size.width - 44.dp.toPx(), 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width, 44.dp.toPx())
                            close()
                        }
                        drawPath(path = path, color = RibbonBg)
                    }
                    Text(
                        text = ribbonLabel,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 4.dp)
                            .rotate(45f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Competition Badge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Sport type",
                        tint = TextPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(BadgeBg, CircleShape)
                            .padding(vertical = 5.dp, horizontal = 13.dp)
                    ) {
                        val categoryName = event.tournament.category?.name ?: ""
                        val badgeText = if (categoryName.isNotEmpty()) "$categoryName · ${event.tournament.name}" else event.tournament.name
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Teams Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Home Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://api.sofascore.app/api/v1/team/${event.homeTeam.id}/image",
                            contentDescription = event.homeTeam.name,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CircleBg),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = event.homeTeam.name,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Center Details (State details, no score)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(horizontal = 4.dp)
                    ) {
                        if (isLive) {
                            LivePulseSignalIcon(pulseAlpha = pulseAlpha)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Live",
                                color = AccentPink,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (event.status.type == "notstarted") {
                            val timeStr = SimpleDateFormat("hh:mm a", Locale.US)
                                .format(Date(event.startTimestamp * 1000))
                            val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                                .format(Date(event.startTimestamp * 1000))
                            
                            val diffMs = (event.startTimestamp * 1000) - System.currentTimeMillis()
                            val countdownText = if (diffMs > 0) {
                                val hours = diffMs / (1000 * 3600)
                                val minutes = (diffMs % (1000 * 3600)) / (1000 * 60)
                                if (hours > 0) "Starts in $hours hours" else "Starts in $minutes mins"
                            } else {
                                "Starting soon"
                            }

                            Text(
                                text = timeStr,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateStr,
                                color = TextDate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = countdownText,
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            // Finished Plain / Status (No Score)
                            Text(
                                text = (event.status.description ?: "FINISHED").uppercase(),
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Finished",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Away Team
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = "https://api.sofascore.app/api/v1/team/${event.awayTeam.id}/image",
                            contentDescription = event.awayTeam.name,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CircleBg),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = event.awayTeam.name,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // If LIVE match, display the custom timer or status text at the bottom
                if (isLive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.status.description ?: "00:00:00",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.testTag("match_card_timer_${event.id}")
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                    val fullDate = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(event.startTimestamp * 1000))
                    Text(
                        text = fullDate,
                        color = TextDate,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun StandingsTable(rows: List<StandingRow>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .border(1.dp, Color(0xFF1F1F26), RoundedCornerShape(12.dp))
            .background(BgCard, RoundedCornerShape(12.dp))
            .testTag("standings_table")
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BorderAccent, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pos", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
            Text("Team", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AlignTextCol("P", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            AlignTextCol("W", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            AlignTextCol("D", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            AlignTextCol("L", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            AlignTextCol("Pts", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // Table Rows
        rows.forEachIndexed { index, row ->
            val rowBg = if (index % 2 == 0) BgCard else Color(0xFF141418)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(rowBg)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = row.position.toString(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp)
                )
                Text(
                    text = row.team.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = (row.matches ?: 0).toString(), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = (row.wins ?: 0).toString(), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = (row.draws ?: 0).toString(), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(text = (row.losses ?: 0).toString(), color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                Text(
                    text = (row.points ?: 0).toString(),
                    color = AccentPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Custom padding helper for alignment
@Composable
fun AlignTextCol(text: String, color: Color, fontSize: androidx.compose.ui.unit.TextUnit, fontWeight: FontWeight) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = Modifier.width(24.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun StandingsRowTextHelper(text: String, color: Color, modifier: Modifier) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
        textAlign = TextAlign.Center
    )
}

@Composable
fun EmptyStateView(
    title: String,
    subtext: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Empty State",
            tint = TextSecondary,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtext,
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FavouriteChannelRow(
    fav: FavouriteChannel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .border(1.5.dp, BorderAccent, RoundedCornerShape(12.dp))
            .background(BgCard, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = fav.logoUrl,
            contentDescription = fav.name,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CircleBg)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fav.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = fav.category,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Box(
            modifier = Modifier
                .background(BorderAccent, RoundedCornerShape(16.dp))
                .padding(vertical = 6.dp, horizontal = 14.dp)
        ) {
            Text(
                text = "▶ Watch",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
