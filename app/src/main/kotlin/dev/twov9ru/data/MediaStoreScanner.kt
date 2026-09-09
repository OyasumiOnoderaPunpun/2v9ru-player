package dev.twov9ru.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans the device's MediaStore for audio files in a background coroutine.
 *
 * Performance characteristics:
 * - Runs on [Dispatchers.IO]: never touches the main thread
 * - Single ContentResolver query — no recursion, no filesystem traversal
 * - Returns only .mp3, .flac, .wav files via MIME type filter
 * - Album art URIs are computed lazily via ContentUris (no bitmap loading)
 *
 * Phase 2: Results are inserted into Room DB via [AppDatabase] and trigger
 * the Smart Playlist auto-generation.
 */
object MediaStoreScanner {

    data class Track(
        val id:           Long,
        val title:        String,
        val artist:       String,
        val album:        String,
        val albumId:      Long,
        val durationMs:   Long,
        val dataUri:      Uri,
        val albumArtUri:  Uri?,
        val dateAdded:    Long,
        val playCount:    Int = 0,
        val isFavorite:   Boolean = false
    )

    private val SUPPORTED_MIME_TYPES = listOf(
        "audio/mpeg",        // .mp3
        "audio/flac",        // .flac
        "audio/x-flac",
        "audio/wav",         // .wav
        "audio/x-wav",
        "audio/aac",         // .aac
        "audio/ogg",         // .ogg
        "audio/opus",        // .opus
        "audio/mp4"          // .m4a
    )

    private val PROJECTION = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.MIME_TYPE
    )

    suspend fun scan(context: Context): List<Track> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()

        val mimeSelection = SUPPORTED_MIME_TYPES.joinToString(" OR ") {
            "${MediaStore.Audio.Media.MIME_TYPE} = ?"
        }
        val selectionArgs = SUPPORTED_MIME_TYPES.toTypedArray()

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            PROJECTION,
            "$mimeSelection AND ${MediaStore.Audio.Media.DURATION} > 10000", // skip < 10s
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        ) ?: return@withContext emptyList()

        cursor.use {
            val idCol        = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol     = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol    = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol     = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol   = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol  = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateCol      = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id      = it.getLong(idCol)
                val albumId = it.getLong(albumIdCol)

                tracks += Track(
                    id          = id,
                    title       = it.getString(titleCol) ?: "Unknown",
                    artist      = it.getString(artistCol) ?: "Unknown Artist",
                    album       = it.getString(albumCol) ?: "Unknown Album",
                    albumId     = albumId,
                    durationMs  = it.getLong(durationCol),
                    dataUri     = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                    albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"), albumId
                    ),
                    dateAdded   = it.getLong(dateCol)
                )
            }
        }
        tracks
    }
}
