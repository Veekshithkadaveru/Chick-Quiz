package app.krafted.chickquiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        "FEED_CARE" to true,
        "HEALTH" to true,
        "FUN_FACTS" to true
    ),
    val starRatings: Map<String, Int> = emptyMap()
)

class HomeViewModel(
    private val progressDao: PlayerProgressDao
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
                val unlocks = computeUnlocks()
                _uiState.update {
                    it.copy(
                        starRatings = stars,
                        unlockState = unlocks
                    )
                }
            }
        }
    }

    private fun computeUnlocks(): Map<String, Boolean> {
        return mapOf(
            "BREEDS" to true,
            "EGGS" to true,
            "FEED_CARE" to true,
            "HEALTH" to true,
            "FUN_FACTS" to true
        )
    }
}
