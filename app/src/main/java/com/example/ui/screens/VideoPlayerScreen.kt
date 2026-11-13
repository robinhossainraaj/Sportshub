package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.model.Channel
import com.example.ui.theme.BgCard
import com.example.ui.theme.BgMain
import com.example.ui.theme.BorderAccent
import com.example.ui.theme.CircleBg
import com.example.ui.theme.RibbonBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

import java.util.Locale

@UnstableApi
@Composable
fun VideoPlayerScreen(
    channel: Channel,
    playlist: List<Channel>,
    playerViewModel: PlayerViewModel,
    onBack: () -> Unit,
    onChannelChange: (Channel) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isMuted by playerViewModel.isMuted.collectAsState()
    val isLocked by playerViewModel.isScreenLocked.collectAsState()
    val resizeMode by playerViewModel.resizeMode.collectAsState()
    val activeSourceIndex by playerViewModel.activeSourceIndex.collectAsState()

    var isPlayerLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }
    var streamUrl by remember { mutableStateOf(channel.streamUrl) }

    // Position tracker for VOD streams
    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    // Setup ExoPlayer stability builder:
    val exoplayer = remember {
        val loadControl = DefaultLoadControl.Builder().build()

        ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
    }

    LaunchedEffect(streamUrl) {
        isPlayerLoading = true
        exoplayer.stop()
        exoplayer.clearMediaItems()
        exoplayer.setMediaItem(MediaItem.fromUri(streamUrl))
        exoplayer.prepare()
        exoplayer.playWhenReady = true
    }

    // Progress updates loop
    LaunchedEffect(exoplayer) {
        while (true) {
            currentPosition = exoplayer.currentPosition
            totalDuration = exoplayer.duration
            delay(500)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isPlayerLoading = playbackState == Player.STATE_BUFFERING
                isPlaying = exoplayer.isPlaying
            }

            override fun onIsPlayingChanged(isPlayingState: Boolean) {
                isPlaying = isPlayingState
            }
        }
        exoplayer.addListener(listener)
        onDispose {
            exoplayer.removeListener(listener)
            exoplayer.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    // Mute listener
    LaunchedEffect(isMuted) {
        exoplayer.volume = if (isMuted) 0f else 1f
    }

    // Back button override
    BackHandler {
        onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgMain)
            .testTag("video_player_screen")
    ) {
        // 1. Player Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = channel.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { playerViewModel.toggleMute() }) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { playerViewModel.toggleScreenLock() }) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Lock Controls",
                        tint = if (isLocked) BorderAccent else Color.White
                    )
                }
            }
        }

        // 2. Main video Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
                .testTag("video_surface")
        ) {
            val playerScaleMode = when (resizeMode) {
                1 -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                2 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoplayer
                        useController = false
                        this.resizeMode = playerScaleMode
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.resizeMode = playerScaleMode
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isPlayerLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BorderAccent
                )
            }
        }

        // 3. Player Controls row (only responsive when NOT screen locked)
        if (!isLocked) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { playerViewModel.cycleResizeMode() }) {
                    Icon(
                        imageVector = Icons.Default.AspectRatio,
                        contentDescription = "Resize mode",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            activity?.enterPictureInPictureMode()
                        } catch (e: Exception) {
                            Toast.makeText(context, "PiP not supported on this device", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Requires Android 8.0+", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.PictureInPictureAlt,
                        contentDescription = "Picture-in-Picture Mode",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { exoplayer.seekTo(exoplayer.currentPosition - 10000) }) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Rewind",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Play Pause
                IconButton(onClick = {
                    if (isPlaying) exoplayer.pause() else exoplayer.play()
                }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause Button",
                        tint = BorderAccent,
                        modifier = Modifier.size(38.dp)
                    )
                }
                IconButton(onClick = { exoplayer.seekTo(exoplayer.currentPosition + 10000) }) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Fast Forward",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = {
                    Toast.makeText(context, "Auto ABR stream quality balanced", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = {
                    // Toggle Fullscreen layout
                    val currentOrientation = activity?.requestedOrientation
                    if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Toggle Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 4. Seekbar / LIVE Status Dot Indicator
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // If live stream, exoplayer duration will be null/negative
                val isStreamDurationLive = totalDuration <= 0
                if (isStreamDurationLive) {
                    Box(
                        modifier = Modifier
                            .background(RibbonBg, RoundedCornerShape(4.dp))
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = "● LIVE STREAMING HD",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = formatTime(currentPosition),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = currentPosition.toFloat(),
                        onValueChange = { exoplayer.seekTo(it.toLong()) },
                        valueRange = 0f..(if (totalDuration > 0) totalDuration.toFloat() else 100f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = BorderAccent,
                            thumbColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )
                    Text(
                        text = formatTime(totalDuration),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Source Selector pills
        Text(
            text = "📡 Alternate Servers",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val streamSources = listOf("▶ Main HLS Stream", "Backup Server 1", "Backup Server 2 (VOD)")
            items(streamSources.size) { index ->
                val active = activeSourceIndex == index
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.5.dp,
                            color = BorderAccent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .background(
                            color = if (active) BorderAccent else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            playerViewModel.selectSource(index)
                            // switch stream fallback URLs
                            streamUrl = if (index == 0) channel.streamUrl else "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/test_hls.m3u8"
                            Toast.makeText(context, "Swapping source stream...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = streamSources[index],
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Related Channels Grid
        Text(
            text = "📺 Related Live Stations",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )
        
        val relatedList = playlist.filter { it.streamUrl != channel.streamUrl }.take(12)
        if (relatedList.isEmpty()) {
            Text(
                text = "No related channels in this playlist.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(relatedList) { related ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onChannelChange(related) }
                            .testTag("related_channel_${related.name.lowercase().replace(" ", "_")}")
                    ) {
                        AsyncImage(
                            model = related.logoUrl.ifEmpty { "https://raw.githubusercontent.com/robinhossainraaj/rorax-iptv-database/main/assets/ic_channel.png" },
                            contentDescription = related.name,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(CircleBg)
                                .border(1.dp, Color(0xFF1F1F26), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = related.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSecs = ms / 1000
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
