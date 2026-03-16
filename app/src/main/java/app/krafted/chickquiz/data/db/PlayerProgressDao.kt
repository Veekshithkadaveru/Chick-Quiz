package app.krafted.chickquiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: PlayerProgress)

    @Query("SELECT * FROM player_progress WHERE category = :category")
    suspend fun getProgress(category: String): PlayerProgress?

    @Query("SELECT * FROM player_progress")
    fun getAllProgress(): Flow<List<PlayerProgress>>
}
