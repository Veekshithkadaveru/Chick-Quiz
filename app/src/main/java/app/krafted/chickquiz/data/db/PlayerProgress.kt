package app.krafted.chickquiz.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val category: String,
    val stars: Int = 0,
    val bestScore: Int = 0,
    val attempts: Int = 0
)
