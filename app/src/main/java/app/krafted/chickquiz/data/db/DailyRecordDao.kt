package app.krafted.chickquiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DailyRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: DailyRecord)

    @Query("SELECT * FROM daily_records WHERE id = 1")
    suspend fun getDailyRecord(): DailyRecord?
}
