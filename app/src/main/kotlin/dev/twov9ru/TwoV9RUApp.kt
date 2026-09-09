package dev.twov9ru

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

/**
 * Application class — configures Coil's global ImageLoader with:
 * - Memory cache capped at 20MB (well within the 30MB budget)
 * - Aggressive bitmap downsampling via `size(128)` on each request
 * - Disk cache for album art (avoids redundant MediaStore reads)
 */
class TwoV9RUApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizeBytes(20 * 1024 * 1024) // 20MB hard cap
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("album_art"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB disk cache
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false) // content:// URIs have no cache headers
            .build()
}
