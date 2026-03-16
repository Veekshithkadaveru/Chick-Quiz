package app.krafted.chickquiz.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey val id: Int = 1,
    val lastDailyDate: Long = 0L,
    val streakCount: Int = 0
)
