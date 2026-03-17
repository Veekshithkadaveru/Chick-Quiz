package app.krafted.chickquiz.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val totalCount: Int = 6,
    val newlyUnlockedIds: Set<Int> = emptySet()
)

class CollectionViewModel(
    private val progressDao: PlayerProgressDao,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val acknowledgedIds: MutableSet<Int> =
        prefs.getStringSet(KEY_ACKNOWLEDGED, emptySet())!!
            .mapTo(mutableSetOf()) { it.toInt() }

    init {
        loadCollection()
    }

    private fun loadCollection() {
        viewModelScope.launch {
            progressDao.getAllProgress().collect { progressList ->
                val chicks = buildChickList(progressList)
                val newlyUnlocked = chicks
                    .filter { it.isUnlocked && it.id !in acknowledgedIds }
                    .map { it.id }
                    .toSet()
                _uiState.update {
                    it.copy(
                        chicks = chicks,
                        unlockedCount = chicks.count { c -> c.isUnlocked },
                        newlyUnlockedIds = newlyUnlocked
                    )
                }
            }
        }
    }

    fun acknowledgeUnlock(id: Int) {
        acknowledgedIds.add(id)
        prefs.edit()
            .putStringSet(KEY_ACKNOWLEDGED, acknowledgedIds.map { it.toString() }.toSet())
            .apply()
        _uiState.update { it.copy(newlyUnlockedIds = it.newlyUnlockedIds - id) }
    }

    private fun buildChickList(progressList: List<PlayerProgress>): List<ChickInfo> {
        val stars = progressList.associate { it.category to it.stars }
        return listOf(
            ChickInfo(1, "Breeds Chick", "chick_breeds", "Earn any star in Breeds",
                (stars["BREEDS"] ?: 0) >= 1),
            ChickInfo(2, "Egg Chick", "chick_eggs", "Earn any star in Eggs",
                (stars["EGGS"] ?: 0) >= 1),
            ChickInfo(3, "Farm Chick", "chick_feed_care", "Earn any star in Feed & Care",
                (stars["FEED_CARE"] ?: 0) >= 1),
            ChickInfo(4, "Health Chick", "chick_health", "Earn any star in Health",
                (stars["HEALTH"] ?: 0) >= 1),
            ChickInfo(5, "Fun Chick", "chick_fun_facts", "Earn any star in Fun Facts",
                (stars["FUN_FACTS"] ?: 0) >= 1),
            ChickInfo(6, "Master Chick", "chick_master", "Earn 3\u2605 in all categories",
                Category.entries.all { cat -> (stars[cat.name] ?: 0) >= 3 })
        )
    }

    companion object {
        private const val KEY_ACKNOWLEDGED = "acknowledged_unlocks"
    }
}
