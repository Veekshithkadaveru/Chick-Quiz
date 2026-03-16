package app.krafted.chickquiz.data.questions

import java.util.Calendar
import kotlin.random.Random

object QuestionShuffler {

    fun getSessionQuestions(allQuestions: List<Question>, category: String): List<Question> {
        return allQuestions
            .filter { it.category == category }
            .shuffled()
            .take(10)
            .map { shuffleOptions(it) }
    }

    fun getDailyQuestions(allQuestions: List<Question>): List<Question> {
        val cal = Calendar.getInstance()
        val seed = cal.get(Calendar.DAY_OF_YEAR) * 1000L + cal.get(Calendar.YEAR)
        return allQuestions
            .shuffled(Random(seed))
            .take(10)
            .map { q ->
                val shuffled = q.options.shuffled(Random(seed + q.id))
                q.copy(
                    options = shuffled,
                    correctIndex = shuffled.indexOf(q.options[q.correctIndex])
                )
            }
    }

    private fun shuffleOptions(q: Question): Question {
        val shuffled = q.options.shuffled()
        return q.copy(
            options = shuffled,
            correctIndex = shuffled.indexOf(q.options[q.correctIndex])
        )
    }
}
