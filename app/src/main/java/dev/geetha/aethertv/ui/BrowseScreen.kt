package dev.geetha.aethertv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.geetha.aethertv.playback.SampleCatalog
import dev.geetha.aethertv.playback.SampleStream

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BrowseScreen(onPlay: (SampleStream) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "Aether TV",
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = "Public sample streams. D-pad to a title, then select.",
            style = MaterialTheme.typography.bodyLarge,
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(SampleCatalog.streams, key = { it.url }) { stream ->
                Surface(
                    onClick = { onPlay(stream) },
                    modifier = Modifier
                        .width(280.dp)
                        .height(160.dp),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = stream.title, style = MaterialTheme.typography.titleLarge)
                        Text(text = stream.kind.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
