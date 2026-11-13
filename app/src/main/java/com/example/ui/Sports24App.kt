package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.local.FavouriteChannel
import com.example.data.model.DatabaseEntry
import com.example.data.model.Channel
import com.example.ui.components.DrawerContent
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.ChannelListScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SportsScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.BgMain
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.EventsViewModel
import com.example.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun Sports24App(
    activity: MainActivity,
    eventsViewModel: EventsViewModel,
    categoriesViewModel: CategoriesViewModel,
    playerViewModel: PlayerViewModel
) {
    // Top-level Navigation screens: "splash", "home", "channel_list", "player"
    var currentScreen by remember { mutableStateOf("splash") }
    
    // Tracks active bottom navigation tab: 0: Events, 1: Categories, 2: Sports
    var selectedTab by remember { mutableIntStateOf(0) }

    // Navigation argument states
    var activeCategory by remember { mutableStateOf<DatabaseEntry?>(null) }
    var activeChannel by remember { mutableStateOf<Channel?>(null) }
    var activePlaylistList by remember { mutableStateOf<List<Channel>>(emptyList()) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    if (currentScreen == "splash") {
        SplashScreen(
            onSplashFinished = {
                currentScreen = "home"
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Box(modifier = Modifier.fillMaxHeight().background(Color.Black)) {
                    DrawerContent(
                        onMenuItemClick = { title ->
                            // menu actions (eg toast or bookmark)
                        },
                        onCloseDrawer = {
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (currentScreen == "home") {
                        Column {
                            HorizontalDivider(color = Color(0xFF1F1F26), thickness = 1.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BgMain)
                                    .navigationBarsPadding()
                                    .height(72.dp)
                                    .testTag("bottom_nav_bar"),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomNavItem(
                                    title = "Events",
                                    activeIcon = Icons.Filled.Radio,
                                    inactiveIcon = Icons.Outlined.Radio,
                                    isSelected = selectedTab == 0,
                                    onClick = { selectedTab = 0 }
                                )
                                BottomNavItem(
                                    title = "Categories",
                                    activeIcon = Icons.Filled.GridOn,
                                    inactiveIcon = Icons.Outlined.GridOn,
                                    isSelected = selectedTab == 1,
                                    onClick = { selectedTab = 1 }
                                )
                                BottomNavItem(
                                    title = "Sports",
                                    activeIcon = Icons.Filled.SportsSoccer,
                                    inactiveIcon = Icons.Outlined.SportsSoccer,
                                    isSelected = selectedTab == 2,
                                    onClick = { selectedTab = 2 }
                                )
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (currentScreen) {
                        "home" -> {
                            when (selectedTab) {
                                0 -> EventsScreen(
                                    eventsViewModel = eventsViewModel,
                                    onMenuClick = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onFavouriteClick = {
                                        eventsViewModel.selectSport("Favourites")
                                    },
                                    onChannelClick = { fav ->
                                        // Wrap Favourite into a Channel object to play instantly
                                        val chan = Channel(
                                            name = fav.name,
                                            logoUrl = fav.logoUrl,
                                            streamUrl = fav.streamUrl,
                                            category = fav.category
                                        )
                                        activeChannel = chan
                                        activePlaylistList = listOf(chan)
                                        currentScreen = "player"
                                    }
                                )
                                1 -> CategoriesScreen(
                                    categoriesViewModel = categoriesViewModel,
                                    onMenuClick = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onCategoryClick = { entry ->
                                        activeCategory = entry
                                        currentScreen = "channel_list"
                                    }
                                )
                                2 -> SportsScreen(
                                    categoriesViewModel = categoriesViewModel,
                                    onMenuClick = {
                                        coroutineScope.launch { drawerState.open() }
                                    },
                                    onPlaylistClick = { entry ->
                                        activeCategory = entry
                                        currentScreen = "channel_list"
                                    }
                                )
                            }
                        }
                        "channel_list" -> {
                            if (activeCategory != null) {
                                ChannelListScreen(
                                    playlistName = activeCategory!!.name,
                                    playlistUrl = activeCategory!!.playlist,
                                    categoriesViewModel = categoriesViewModel,
                                    onBack = { currentScreen = "home" },
                                    onChannelSelect = { selected, list ->
                                        activeChannel = selected
                                        activePlaylistList = list
                                        currentScreen = "player"
                                    }
                                )
                            }
                        }
                        "player" -> {
                            if (activeChannel != null) {
                                androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
                                VideoPlayerScreen(
                                    channel = activeChannel!!,
                                    playlist = activePlaylistList,
                                    playerViewModel = playerViewModel,
                                    onBack = { currentScreen = "channel_list" },
                                    onChannelChange = { newChan ->
                                        activeChannel = newChan
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("nav_item_${title.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) activeIcon else inactiveIcon,
            contentDescription = title,
            tint = if (isSelected) BorderAccent else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            color = if (isSelected) BorderAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
