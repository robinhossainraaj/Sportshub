package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgMain
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.CircleBg
import com.example.ui.theme.RibbonBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    playlistName: String,
    playlistUrl: String,
    categoriesViewModel: CategoriesViewModel,
    onBack: () -> Unit,
    onChannelSelect: (Channel, List<Channel>) -> Unit
) {
    val context = LocalContext.current
    val channelsState by categoriesViewModel.channelsState.collectAsState()

    var showFavouritesSheet by remember { mutableStateOf(false) }
    var selectedChannelForSheet by remember { mutableStateOf<Channel?>(null) }
    var isAlreadyFavourite by remember { mutableStateOf(false) }

    LaunchedEffect(playlistUrl) {
        categoriesViewModel.loadChannelsFromPlaylist(playlistUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .testTag("channel_list_screen")
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 18.dp, end = 18.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = playlistName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }

        when (val state = channelsState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BorderAccent)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                val channels = state.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(channels) { channel ->
                        ChannelListItemRow(
                            channel = channel,
                            onClick = { onChannelSelect(channel, channels) },
                            onLongClick = {
                                categoriesViewModel.isChannelFavourite(channel.streamUrl) { exists ->
                                    isAlreadyFavourite = exists
                                    selectedChannelForSheet = channel
                                    showFavouritesSheet = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet Dialog for Favourites (long-press action)
    if (showFavouritesSheet && selectedChannelForSheet != null) {
        val channel = selectedChannelForSheet!!
        ModalBottomSheet(
            onDismissRequest = { showFavouritesSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = BgCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = {
                        categoriesViewModel.toggleFavourite(channel, isAlreadyFavourite) { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        showFavouritesSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fav_toggle_option")
                ) {
                    Text(
                        text = if (isAlreadyFavourite) "🗑 Remove from Favourites" else "⭐ Add to Favourites",
                        color = if (isAlreadyFavourite) RibbonBg else BorderAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                TextButton(
                    onClick = {
                        onChannelSelect(channel, (channelsState as? UiState.Success)?.data ?: emptyList())
                        showFavouritesSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "▶ Watch Now",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = { showFavouritesSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChannelListItemRow(
    channel: Channel,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isHd = channel.name.contains("HD", ignoreCase = true) || channel.name.contains("FHD", ignoreCase = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderAccent, RoundedCornerShape(12.dp))
            .background(BgCard, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp)
            .testTag("channel_item_${channel.name.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = channel.logoUrl.ifEmpty { "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/assets/ic_channel.png" },
            contentDescription = channel.name,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(CircleBg),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = channel.name,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (isHd) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(RibbonBg, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HD",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Tap to stream • Long-press to save",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .background(BorderAccent, CircleShape)
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
