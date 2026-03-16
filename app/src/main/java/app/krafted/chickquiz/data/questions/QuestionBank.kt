package app.krafted.chickquiz.data.questions

data class Question(
    val id: Int,
    val category: String,
    val difficulty: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val funFact: String,
    val timeLimit: Int
)

data class QuestionBank(
    val questions: List<Question>
)
