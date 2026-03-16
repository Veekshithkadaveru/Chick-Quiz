package app.krafted.chickquiz.data.questions

import android.content.Context
import app.krafted.chickquiz.data.db.QuestionDao
import app.krafted.chickquiz.data.db.QuestionEntity
import com.google.gson.Gson

class QuestionRepository(
    private val context: Context,
    private val questionDao: QuestionDao
) {
    suspend fun loadQuestions(): List<Question> {
        val cached = questionDao.getAllQuestions()
        if (cached.isNotEmpty()) return cached.map { it.toQuestion() }

        val json = context.assets.open("questions.json")
            .bufferedReader().use { it.readText() }
        val parsed = Gson().fromJson(json, QuestionBank::class.java)

        questionDao.insertAll(parsed.questions.map { it.toEntity() })
        return parsed.questions
    }

    suspend fun loadByCategory(category: String): List<Question> {
        val cached = questionDao.getByCategory(category)
        if (cached.isNotEmpty()) return cached.map { it.toQuestion() }
        return loadQuestions().filter { it.category == category }
    }
}

private fun QuestionEntity.toQuestion() = Question(
    id = id,
    category = category,
    difficulty = difficulty,
    question = question,
    options = listOf(option0, option1, option2, option3),
    correctIndex = correctIndex,
    funFact = funFact,
    timeLimit = timeLimit
)

private fun Question.toEntity() = QuestionEntity(
    id = id,
    category = category,
    difficulty = difficulty,
    question = question,
    option0 = options[0],
    option1 = options[1],
    option2 = options[2],
    option3 = options[3],
    correctIndex = correctIndex,
    funFact = funFact,
    timeLimit = timeLimit
)
