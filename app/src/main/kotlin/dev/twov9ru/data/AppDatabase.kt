package dev.twov9ru.data

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ── Entities ──────────────────────────────────────────────────────────────

@Entity(
    tableName = "tracks",
    indices = [
        Index("dateAdded"),
        Index("playCount"),
        Index("isFavorite")
    ]
)
data class TrackEntity(
    @PrimaryKey val id:           Long,
    val title:        String,
    val artist:       String,
    val album:        String,
    val albumId:      Long,
    val durationMs:   Long,
    val dataUri:      String,
    val albumArtUri:  String?,
    val dateAdded:    Long,
    val playCount:    Int     = 0,
    val isFavorite:   Boolean = false,
    val lastPlayedAt: Long    = 0L
)

// ── DAOs ──────────────────────────────────────────────────────────────────

@androidx.room.Dao
interface TrackDao {
    @androidx.room.Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT 50")
    fun recentlyAdded(): Flow<List<TrackEntity>>

    @androidx.room.Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT 50")
    fun mostPlayed(): Flow<List<TrackEntity>>

    @androidx.room.Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title ASC")
    fun favorites(): Flow<List<TrackEntity>>

    @androidx.room.Query("SELECT * FROM tracks ORDER BY artist ASC, album ASC, title ASC")
    fun allTracks(): Flow<List<TrackEntity>>

    @androidx.room.Query("SELECT * FROM tracks WHERE title LIKE '%' || :q || '%' OR artist LIKE '%' || :q || '%' LIMIT 100")
    fun search(q: String): Flow<List<TrackEntity>>

    @androidx.room.Upsert
    suspend fun upsertAll(tracks: List<TrackEntity>): LongArray

    @androidx.room.Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedAt = :ts WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, ts: Long = System.currentTimeMillis()): Int

    @androidx.room.Query("UPDATE tracks SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: Long, fav: Boolean): Int
}

// ── Database ──────────────────────────────────────────────────────────────

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "2v9ru.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
