package app.krafted.chickquiz.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Int,
    val category: String,
    val difficulty: String,
    val question: String,
    val option0: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val correctIndex: Int,
    val funFact: String,
    val timeLimit: Int
)
