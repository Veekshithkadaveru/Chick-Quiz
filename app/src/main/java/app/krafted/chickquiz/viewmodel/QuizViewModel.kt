package app.krafted.chickquiz.viewmodel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.chickquiz.data.db.DailyRecord
import app.krafted.chickquiz.data.db.DailyRecordDao
import app.krafted.chickquiz.data.db.PlayerProgress
import app.krafted.chickquiz.data.db.PlayerProgressDao
import app.krafted.chickquiz.data.db.ScoreRecord
import app.krafted.chickquiz.data.db.ScoreRecordDao
import app.krafted.chickquiz.data.questions.Question
import app.krafted.chickquiz.data.questions.QuestionRepository
import app.krafted.chickquiz.data.questions.QuestionShuffler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

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
    val timeRemaining: Float = 1f,
    val score: Int = 0,
    val correctCount: Int = 0,
    val isDaily: Boolean = false,
    val sessionComplete: Boolean = false,
    val mamaHenState: MamaHenState = MamaHenState.IDLE,
    val isPersonalBest: Boolean = false,
    val starsEarned: Int = 0,
    val newUnlocks: List<String> = emptyList()
)

class QuizViewModel(
    private val repository: QuestionRepository,
    private val progressDao: PlayerProgressDao,
    private val dailyDao: DailyRecordDao,
    private val scoreDao: ScoreRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    val timerAnimatable = Animatable(1f)
    private var timerJob: Job? = null

    fun startSession(category: String, isDaily: Boolean) {
        viewModelScope.launch {
            val allQuestions = repository.loadQuestions()
            val questions = if (isDaily) {
                QuestionShuffler.getDailyQuestions(allQuestions)
            } else {
                QuestionShuffler.getSessionQuestions(allQuestions, category)
            }

            _uiState.value = QuizUiState(
                questions = questions,
                isDaily = isDaily
            )

            startTimer()
        }
    }

    fun onAnswerSelected(index: Int) {
        val state = _uiState.value
        if (state.selectedAnswer != null || state.sessionComplete) return

        timerJob?.cancel()

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
                mamaHenState = MamaHenState.IDLE
            )
        }

        startTimer()
    }

    fun onTimerExpired() {
        val state = _uiState.value
        if (state.selectedAnswer != null) return

        _uiState.update {
            it.copy(
                selectedAnswer = -1,
                mamaHenState = MamaHenState.TIMEOUT
            )
        }
    }

    private fun onSessionComplete() {
        viewModelScope.launch {
            val state = _uiState.value
            val stars = calculateStars(state.correctCount)
            val category = if (state.isDaily) "DAILY" else state.questions.firstOrNull()?.category ?: "BREEDS"

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

            scoreDao.insert(
                ScoreRecord(
                    category = category,
                    score = state.score,
                    correctCount = state.correctCount,
                    isDaily = state.isDaily
                )
            )

            if (state.isDaily) {
                updateDailyStreak()
            }

            val newUnlocks = checkNewUnlocks(category, persistedStars)

            _uiState.update {
                it.copy(
                    sessionComplete = true,
                    isPersonalBest = isPersonalBest,
                    starsEarned = stars,
                    newUnlocks = newUnlocks
                )
            }
        }
    }

    private suspend fun updateDailyStreak() {
        val record = dailyDao.getDailyRecord() ?: DailyRecord()
        val today = getStartOfToday()
        val yesterday = today - 86_400_000L

        val newStreak = when (record.lastDailyDate) {
            yesterday -> record.streakCount + 1
            today -> record.streakCount
            else -> 1
        }

        dailyDao.upsert(DailyRecord(lastDailyDate = today, streakCount = newStreak))
    }

    private suspend fun checkNewUnlocks(category: String, newStars: Int): List<String> {
        val unlocks = mutableListOf<String>()
        val allProgress = mutableMapOf<String, Int>()

        Category.values().forEach { cat ->
            val p = progressDao.getProgress(cat.name)
            allProgress[cat.name] = p?.stars ?: 0
        }
        allProgress[category] = maxOf(allProgress[category] ?: 0, newStars)

        val breedsStars = allProgress["BREEDS"] ?: 0
        val eggsStars = allProgress["EGGS"] ?: 0
        val maxStars = allProgress.values.maxOrNull() ?: 0

        if (breedsStars >= 1 && category == "BREEDS") unlocks += "FEED_CARE"
        if (eggsStars >= 1 && category == "EGGS") unlocks += "HEALTH"
        if (maxStars >= 2) unlocks += "FUN_FACTS"

        return unlocks
    }

    private fun startTimer() {
        timerJob?.cancel()
        val state = _uiState.value
        val currentQuestion = state.questions.getOrNull(state.currentIndex) ?: return

        timerJob = viewModelScope.launch {
            timerAnimatable.snapTo(1f)
            timerAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = currentQuestion.timeLimit * 1000,
                    easing = LinearEasing
                )
            )
            onTimerExpired()
        }
    }

    fun resetSession() {
        timerJob?.cancel()
        _uiState.value = QuizUiState()
    }

    companion object {
        fun calculateStars(correctCount: Int): Int = when {
            correctCount >= 9 -> 3
            correctCount >= 7 -> 2
            correctCount >= 5 -> 1
            else -> 0
        }

        fun getStartOfToday(): Long {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }
}
