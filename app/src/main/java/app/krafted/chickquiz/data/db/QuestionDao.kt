package app.krafted.chickquiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestions(): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE category = :category")
    suspend fun getByCategory(category: String): List<QuestionEntity>
}
