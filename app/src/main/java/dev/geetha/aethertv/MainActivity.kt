package dev.geetha.aethertv

import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.google.common.util.concurrent.ListenableFuture
import dev.geetha.aethertv.playback.PlaybackController
import dev.geetha.aethertv.playback.PlaybackFault
import dev.geetha.aethertv.playback.PlaybackService
import dev.geetha.aethertv.playback.SampleStream
import dev.geetha.aethertv.ui.BrowseScreen

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val playback = PlaybackController()
    private var status by mutableStateOf("Select a sample")
    private var playing by mutableStateOf<SampleStream?>(null)

    @OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_BUFFERING) {
                    playback.onBuffering()
                    status = bufferingLabel()
                }
                if (playbackState == Player.STATE_READY) {
                    playback.onReady()
                    status = playing?.title ?: "Ready"
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val fault = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND,
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                    -> PlaybackFault.Source
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                    -> PlaybackFault.Network
                    else -> PlaybackFault.BufferUnderrun
                }
                val decision = playback.onFault(fault)
                if (decision.retry) {
                    status = "Retry ${decision.attempt} after ${decision.delayMs} ms"
                    exoPlayer.prepare()
                    exoPlayer.play()
                } else {
                    status = "Playback stopped"
                }
            }
        })
        player = exoPlayer

        val intent = android.content.Intent(this, PlaybackService::class.java)
        ContextCompat.startForegroundService(this, intent)
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                val current = playing
                if (current == null) {
                    BrowseScreen(onPlay = ::play)
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                PlayerView(context).apply { this.player = exoPlayer }
                            },
                        )
                        Column(modifier = Modifier.padding(32.dp)) {
                            Text(text = current.title, style = MaterialTheme.typography.headlineMedium)
                            Text(text = status, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }

    private fun play(stream: SampleStream) {
        val exoPlayer = player ?: return
        playing = stream
        playback.onBuffering()
        status = bufferingLabel()
        exoPlayer.setMediaItem(MediaItem.fromUri(stream.url))
        exoPlayer.prepare()
        exoPlayer.play()
    }

    private fun bufferingLabel(): String {
        val title = playing?.title ?: "Stream"
        return if (playback.attempt == 0) "Buffering $title" else "Buffering $title · retry ${playback.attempt}"
    }

    override fun onDestroy() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        player?.release()
        player = null
        super.onDestroy()
    }
}
