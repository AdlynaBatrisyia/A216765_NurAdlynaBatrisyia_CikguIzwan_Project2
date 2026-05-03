package com.example.a216765_wan_lab1

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SimpleActivity(
    val title: String,
    val time: String
)

data class MoodSpaceUiState(
    val displayedName: String = "",
    val moodDone: Boolean = false,
    val gratitudeDone: Boolean = false,
    val sleepDone: Boolean = false,
    val selectedMood: String = "",
    val inBedTime: String = "11:00 PM",
    val outOfBedTime: String = "7:00 AM",
    val sleepDuration: String = "8 hr",
    val readMessages: Set<Int> = emptySet(),
    val showAllGoalsPopup: Boolean = false,
    val activities: List<SimpleActivity> = emptyList()
)

class MoodSpaceViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MoodSpaceUiState())
    val uiState: StateFlow<MoodSpaceUiState> = _uiState.asStateFlow()

    fun setDisplayedName(name: String) {
        _uiState.update { it.copy(displayedName = name) }
    }

    fun setMoodDone(mood: String) {
        _uiState.update { it.copy(moodDone = true, selectedMood = mood) }
        checkAllGoalsDone()
    }

    fun setGratitudeDone() {
        _uiState.update { it.copy(gratitudeDone = true) }
        checkAllGoalsDone()
    }

    fun setSleepDone(inBed: String, outOfBed: String, duration: String) {
        _uiState.update {
            it.copy(
                sleepDone = true,
                inBedTime = inBed,
                outOfBedTime = outOfBed,
                sleepDuration = duration
            )
        }
        checkAllGoalsDone()
    }

    fun markMessageRead(index: Int) {
        _uiState.update { it.copy(readMessages = it.readMessages + index) }
    }

    fun markAllRead() {
        _uiState.update { it.copy(readMessages = setOf(0, 1, 2)) }
    }

    fun dismissAllGoalsPopup() {
        _uiState.update { it.copy(showAllGoalsPopup = false) }
    }

    fun addActivity(title: String, time: String) {
        _uiState.update {
            it.copy(activities = it.activities + SimpleActivity(title, time))
        }
    }

    private fun checkAllGoalsDone() {
        val s = _uiState.value
        if (s.moodDone && s.gratitudeDone && s.sleepDone) {
            _uiState.update { it.copy(showAllGoalsPopup = true) }
        }
    }
}