package app.krafted.chickquiz.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_records")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val score: Int,
    val correctCount: Int,
    val playerName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
