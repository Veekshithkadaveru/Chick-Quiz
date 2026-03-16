package app.krafted.chickquiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.chickquiz.data.db.DailyRecordDao
import app.krafted.chickquiz.data.db.PlayerProgress
import app.krafted.chickquiz.data.db.PlayerProgressDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val unlockState: Map<String, Boolean> = mapOf(
        "BREEDS" to true,
        "EGGS" to true,
        "FEED_CARE" to false,
        "HEALTH" to false,
        "FUN_FACTS" to false
    ),
    val starRatings: Map<String, Int> = emptyMap(),
    val dailyAvailable: Boolean = true,
    val streakCount: Int = 0
)

class HomeViewModel(
    private val progressDao: PlayerProgressDao,
    private val dailyDao: DailyRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    fun loadState() {
        viewModelScope.launch {
            progressDao.getAllProgress().collect { progressList ->
                val stars = progressList.associate { it.category to it.stars }
                val unlocks = computeUnlocks(progressList)
                _uiState.update {
                    it.copy(
                        starRatings = stars,
                        unlockState = unlocks
                    )
                }
            }
        }

        viewModelScope.launch {
            val record = dailyDao.getDailyRecord()
            val today = QuizViewModel.getStartOfToday()
            _uiState.update {
                it.copy(
                    dailyAvailable = record?.lastDailyDate != today,
                    streakCount = record?.streakCount ?: 0
                )
            }
        }
    }

    private fun computeUnlocks(progressList: List<PlayerProgress>): Map<String, Boolean> {
        val stars = progressList.associate { it.category to it.stars }
        val breedsStars = stars["BREEDS"] ?: 0
        val eggsStars = stars["EGGS"] ?: 0
        val maxStars = stars.values.maxOrNull() ?: 0

        return mapOf(
            "BREEDS" to true,
            "EGGS" to true,
            "FEED_CARE" to (breedsStars >= 1),
            "HEALTH" to (eggsStars >= 1),
            "FUN_FACTS" to (maxStars >= 2)
        )
    }
}
