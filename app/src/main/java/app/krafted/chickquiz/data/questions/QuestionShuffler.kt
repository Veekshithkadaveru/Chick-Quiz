package app.krafted.chickquiz.data.questions

object QuestionShuffler {

    fun getSessionQuestions(allQuestions: List<Question>, category: String): List<Question> {
        return allQuestions
            .filter { it.category == category }
            .shuffled()
            .take(10)
            .map { shuffleOptions(it) }
    }

    private fun shuffleOptions(q: Question): Question {
        val shuffled = q.options.shuffled()
        return q.copy(
            options = shuffled,
            correctIndex = shuffled.indexOf(q.options[q.correctIndex])
        )
    }
}
