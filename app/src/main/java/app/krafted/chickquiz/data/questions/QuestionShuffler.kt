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
        if (q.options.isEmpty() || q.correctIndex !in q.options.indices) return q
        val shuffled = q.options.shuffled()
        val correctAnswer = q.options[q.correctIndex]
        val newIndex = shuffled.indexOf(correctAnswer).takeIf { it >= 0 } ?: 0
        return q.copy(options = shuffled, correctIndex = newIndex)
    }
}
