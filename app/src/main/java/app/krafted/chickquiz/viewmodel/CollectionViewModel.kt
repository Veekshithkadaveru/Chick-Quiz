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

data class ChickInfo(
    val id: Int,
    val name: String,
    val drawableName: String,
    val unlockDescription: String,
    val isUnlocked: Boolean = false
)

data class CollectionUiState(
    val chicks: List<ChickInfo> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 7
)

class CollectionViewModel(
    private val progressDao: PlayerProgressDao,
    private val dailyDao: DailyRecordDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    init {
        loadCollection()
    }

    fun loadCollection() {
        viewModelScope.launch {
            progressDao.getAllProgress().collect { progressList ->
                val dailyRecord = dailyDao.getDailyRecord()
                val chicks = buildChickList(progressList, dailyRecord?.streakCount ?: 0)
                _uiState.update {
                    it.copy(
                        chicks = chicks,
                        unlockedCount = chicks.count { c -> c.isUnlocked }
                    )
                }
            }
        }
    }

    private fun buildChickList(progressList: List<PlayerProgress>, streakCount: Int): List<ChickInfo> {
        val stars = progressList.associate { it.category to it.stars }

        return listOf(
            ChickInfo(
                id = 1,
                name = "Breeds Chick",
                drawableName = "chick_breeds",
                unlockDescription = "Earn any star in Breeds",
                isUnlocked = (stars["BREEDS"] ?: 0) >= 1
            ),
            ChickInfo(
                id = 2,
                name = "Egg Chick",
                drawableName = "chick_eggs",
                unlockDescription = "Earn any star in Eggs",
                isUnlocked = (stars["EGGS"] ?: 0) >= 1
            ),
            ChickInfo(
                id = 3,
                name = "Farm Chick",
                drawableName = "chick_feed_care",
                unlockDescription = "Earn any star in Feed & Care",
                isUnlocked = (stars["FEED_CARE"] ?: 0) >= 1
            ),
            ChickInfo(
                id = 4,
                name = "Health Chick",
                drawableName = "chick_health",
                unlockDescription = "Earn any star in Health",
                isUnlocked = (stars["HEALTH"] ?: 0) >= 1
            ),
            ChickInfo(
                id = 5,
                name = "Fun Chick",
                drawableName = "chick_fun_facts",
                unlockDescription = "Earn any star in Fun Facts",
                isUnlocked = (stars["FUN_FACTS"] ?: 0) >= 1
            ),
            ChickInfo(
                id = 6,
                name = "Master Chick",
                drawableName = "chick_master",
                unlockDescription = "Earn 3 stars in all 5 categories",
                isUnlocked = Category.values().all { cat -> (stars[cat.name] ?: 0) >= 3 }
            ),
            ChickInfo(
                id = 7,
                name = "Daily Chick",
                drawableName = "chick_daily",
                unlockDescription = "Complete a 7-day daily streak",
                isUnlocked = streakCount >= 7
            )
        )
    }
}
