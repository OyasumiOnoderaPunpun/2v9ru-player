package dev.twov9ru.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SmartPlaylistRepo(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val dao = db.trackDao()

    fun getRecentlyAdded(): Flow<List<MediaItem>> = dao.recentlyAdded().map { it.toMediaItems() }
    fun getMostPlayed(): Flow<List<MediaItem>> = dao.mostPlayed().map { it.toMediaItems() }
    fun getFavorites(): Flow<List<MediaItem>> = dao.favorites().map { it.toMediaItems() }
    fun getAllTracks(): Flow<List<MediaItem>> = dao.allTracks().map { it.toMediaItems() }
    
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.setFavorite(id, isFavorite)
    }

    suspend fun recordPlay(id: Long) {
        dao.incrementPlayCount(id)
    }

    private fun List<TrackEntity>.toMediaItems(): List<MediaItem> = map { track ->
        MediaItem.Builder()
            .setMediaId(track.id.toString())
            .setUri(track.dataUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(track.albumArtUri?.let { android.net.Uri.parse(it) })
                    .setIsPlayable(true)
                    .build()
            )
            .build()
    }
}
