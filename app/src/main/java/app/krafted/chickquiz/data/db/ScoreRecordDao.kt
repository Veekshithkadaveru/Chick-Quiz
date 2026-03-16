package app.krafted.chickquiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreRecordDao {
    @Insert
    suspend fun insert(record: ScoreRecord)

    @Query("SELECT * FROM score_records WHERE category = :category ORDER BY score DESC LIMIT 10")
    fun getTopScores(category: String): Flow<List<ScoreRecord>>
}
