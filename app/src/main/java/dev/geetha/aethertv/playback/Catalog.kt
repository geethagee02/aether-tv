package dev.geetha.aethertv.playback

enum class StreamKind {
    HLS,
    DASH,
    MP4,
}

data class SampleStream(
    val title: String,
    val url: String,
    val kind: StreamKind,
)

object SampleCatalog {
    val streams: List<SampleStream> = listOf(
        SampleStream(
            title = "Bipbop",
            url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
            kind = StreamKind.HLS,
        ),
        SampleStream(
            title = "Big Buck Bunny",
            url = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd",
            kind = StreamKind.DASH,
        ),
        SampleStream(
            title = "Big Buck Bunny file",
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            kind = StreamKind.MP4,
        ),
    )

    fun kinds(): Set<StreamKind> = streams.map { it.kind }.toSet()
}
