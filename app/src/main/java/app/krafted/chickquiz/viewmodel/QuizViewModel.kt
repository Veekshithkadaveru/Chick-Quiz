package app.krafted.chickquiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.chickquiz.data.db.PlayerProgress
import app.krafted.chickquiz.data.db.PlayerProgressDao
import app.krafted.chickquiz.data.db.ScoreRecord
import app.krafted.chickquiz.data.db.ScoreRecordDao
import app.krafted.chickquiz.data.questions.Question
import app.krafted.chickquiz.data.questions.QuestionRepository
import app.krafted.chickquiz.data.questions.QuestionShuffler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MamaHenState { IDLE, HAPPY, SAD, TIMEOUT }

enum class Category(val displayName: String) {
    BREEDS("Breeds"),
    EGGS("Eggs"),
    FEED_CARE("Feed & Care"),
    HEALTH("Health"),
    FUN_FACTS("Fun Facts")
}

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val score: Int = 0,
    val correctCount: Int = 0,
    val sessionComplete: Boolean = false,
    val mamaHenState: MamaHenState = MamaHenState.IDLE,
    val isPersonalBest: Boolean = false,
    val starsEarned: Int = 0,
    val timerKey: Int = 0,
    val lastScoreRecordId: Int = 0
)

class QuizViewModel(
    private val repository: QuestionRepository,
    private val progressDao: PlayerProgressDao,
    private val scoreDao: ScoreRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var playerName: String = ""

    fun startSession(category: String, name: String = "") {
        playerName = name
        viewModelScope.launch {
            val allQuestions = repository.loadQuestions()
            val questions = QuestionShuffler.getSessionQuestions(allQuestions, category)
            _uiState.value = QuizUiState(questions = questions, timerKey = 1)
        }
    }

    fun currentTimeLimit(): Int {
        val state = _uiState.value
        return state.questions.getOrNull(state.currentIndex)?.timeLimit ?: 15
    }

    fun onAnswerSelected(index: Int) {
        val state = _uiState.value
        if (state.selectedAnswer != null || state.sessionComplete) return

        val currentQuestion = state.questions[state.currentIndex]
        val isCorrect = index == currentQuestion.correctIndex
        val points = if (isCorrect) {
            when (currentQuestion.difficulty) {
                "EASY" -> 10
                "MEDIUM" -> 20
                "HARD" -> 30
                else -> 10
            }
        } else 0

        _uiState.update {
            it.copy(
                selectedAnswer = index,
                score = it.score + points,
                correctCount = if (isCorrect) it.correctCount + 1 else it.correctCount,
                mamaHenState = if (isCorrect) MamaHenState.HAPPY else MamaHenState.SAD
            )
        }
    }

    fun onRevealAnswer() {
        _uiState.update { it.copy(isAnswerRevealed = true) }
    }

    fun onNextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex >= state.questions.size) {
            onSessionComplete()
            return
        }

        _uiState.update {
            it.copy(
                currentIndex = nextIndex,
                selectedAnswer = null,
                isAnswerRevealed = false,
                mamaHenState = MamaHenState.IDLE,
                timerKey = it.timerKey + 1
            )
        }
    }

    fun onTimerExpired() {
        val state = _uiState.value
        if (state.selectedAnswer != null) return
        _uiState.update { it.copy(selectedAnswer = -1, mamaHenState = MamaHenState.TIMEOUT) }
    }

    private fun onSessionComplete() {
        viewModelScope.launch {
            val state = _uiState.value
            val stars = calculateStars(state.correctCount)
            val category = state.questions.firstOrNull()?.category ?: "BREEDS"

            val currentProgress = progressDao.getProgress(category)
            val isPersonalBest = state.score > (currentProgress?.bestScore ?: 0)
            val persistedStars = maxOf(currentProgress?.stars ?: 0, stars)

            progressDao.upsert(
                PlayerProgress(
                    category = category,
                    stars = persistedStars,
                    bestScore = maxOf(currentProgress?.bestScore ?: 0, state.score),
                    attempts = (currentProgress?.attempts ?: 0) + 1
                )
            )

            val recordId = scoreDao.insert(
                ScoreRecord(
                    category = category,
                    score = state.score,
                    correctCount = state.correctCount,
                    playerName = playerName
                )
            )

            _uiState.update {
                it.copy(
                    sessionComplete = true,
                    isPersonalBest = isPersonalBest,
                    starsEarned = stars,
                    lastScoreRecordId = recordId.toInt()
                )
            }
        }
    }

    fun resetSession() {
        _uiState.value = QuizUiState()
    }

    companion object {
        fun calculateStars(correctCount: Int): Int = when {
            correctCount >= 9 -> 3
            correctCount >= 7 -> 2
            correctCount >= 5 -> 1
            else -> 0
        }
    }
}
