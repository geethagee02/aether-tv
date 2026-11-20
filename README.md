# Aether TV

Android TV player for public sample streams. The 10-foot screen is Compose for TV with a Leanback launcher. D-pad to a title and select it. Media3 ExoPlayer plays HLS, DASH, and MP4. Buffering and network faults retry up to three times. A bad source does not retry. `PlaybackService` is a Media3 `MediaSessionService`.

## Open

Android Studio, a TV emulator (API 24 or newer), and JDK 17.

```bash
./gradlew testDebugUnitTest assembleDebug
```

The unit tests cover the sample catalog and the retry rules. They do not measure startup time.
