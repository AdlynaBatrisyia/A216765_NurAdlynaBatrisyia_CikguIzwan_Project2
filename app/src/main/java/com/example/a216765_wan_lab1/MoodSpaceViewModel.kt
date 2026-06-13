package com.example.a216765_wan_lab1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SimpleActivity(
    val title: String,
    val time: String
)

data class MentalHealthResult(
    val status: String,
    val emoji: String,
    val message: String,
    val advice: String,
    val color: String,
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
    val showMentalHealthPopup: Boolean = false,
    val activities: List<SimpleActivity> = emptyList(),
    val mentalHealthHistory: List<MentalHealthResult> = emptyList(),
    val latestMentalHealthResult: MentalHealthResult? = null,
    val savedActivities: List<ActivityEntity> = emptyList()
)

class MoodSpaceViewModel(private val repository: ActivityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodSpaceUiState())
    val uiState: StateFlow<MoodSpaceUiState> = _uiState.asStateFlow()

    init {
        // Load all saved activities from Room when ViewModel starts
        viewModelScope.launch {
            repository.allActivities.collect { entities ->
                _uiState.update { it.copy(savedActivities = entities) }
            }
        }
    }

    fun setDisplayedName(name: String) {
        _uiState.update { it.copy(displayedName = name) }
    }

    fun setMoodDone(mood: String) {
        _uiState.update { it.copy(moodDone = true, selectedMood = mood) }
        // Save mood to Room
        viewModelScope.launch {
            repository.insert(
                ActivityEntity(
                    title = "Mood: $mood",
                    time = getCurrentTime(),
                    type = "mood"
                )
            )
        }
        checkAllGoalsDone()
    }

    fun setGratitudeDone() {
        _uiState.update { it.copy(gratitudeDone = true) }
        // Save gratitude to Room
        viewModelScope.launch {
            repository.insert(
                ActivityEntity(
                    title = "Gratitude",
                    time = getCurrentTime(),
                    type = "gratitude"
                )
            )
        }
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
        // Save sleep to Room
        viewModelScope.launch {
            repository.insert(
                ActivityEntity(
                    title = "Sleep: $duration ($inBed - $outOfBed)",
                    time = getCurrentTime(),
                    type = "sleep"
                )
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
        val result = calculateMentalHealth()
        _uiState.update {
            it.copy(
                showAllGoalsPopup = false,
                showMentalHealthPopup = true,
                latestMentalHealthResult = result,
                mentalHealthHistory = it.mentalHealthHistory + result
            )
        }
        // Save mental health result to Room
        viewModelScope.launch {
            repository.insert(
                ActivityEntity(
                    title = "${result.emoji} ${result.status}",
                    time = result.time,
                    type = "mental_health"
                )
            )
        }
    }

    fun dismissMentalHealthPopup() {
        _uiState.update { it.copy(showMentalHealthPopup = false) }
    }

    fun addActivity(title: String, time: String) {
        _uiState.update {
            it.copy(activities = it.activities + SimpleActivity(title, time))
        }
        // Save simple activity to Room
        viewModelScope.launch {
            repository.insert(
                ActivityEntity(
                    title = title,
                    time = time,
                    type = "simple"
                )
            )
        }
    }

    private fun checkAllGoalsDone() {
        val s = _uiState.value
        if (s.moodDone && s.gratitudeDone && s.sleepDone) {
            _uiState.update { it.copy(showAllGoalsPopup = true) }
        }
    }

    private fun calculateMentalHealth(): MentalHealthResult {
        val s = _uiState.value
        val mood = s.selectedMood
        val sleepHours = parseSleepHours(s.sleepDuration)
        val time = getCurrentTime()

        val moodScore = when (mood) {
            "Great" -> 3
            "Good"  -> 2
            "OK"    -> 1
            else    -> 0
        }

        val sleepScore = when {
            sleepHours >= 8 -> 3
            sleepHours >= 7 -> 2
            sleepHours >= 6 -> 1
            else            -> 0
        }

        val gratitudeBonus = 1
        val totalScore = moodScore + sleepScore + gratitudeBonus

        return when {
            totalScore >= 6 -> MentalHealthResult(
                status  = "You're Doing Great! 🌟",
                emoji   = "😊",
                message = "Based on your mood, sleep, and gratitude today, you appear to be in a positive mental state. Keep up the great work!",
                advice  = "Continue your healthy habits. Regular check-ins like this help maintain good mental well-being.",
                color   = "green",
                time    = time
            )
            totalScore >= 4 -> MentalHealthResult(
                status  = "You're Doing Okay 🌤",
                emoji   = "🙂",
                message = "Your mental well-being seems generally stable today, but there are small signs of stress or low energy. That's completely normal.",
                advice  = "Try to get a little more sleep tonight and take a moment to appreciate the small things. You're doing better than you think.",
                color   = "yellow",
                time    = time
            )
            totalScore >= 2 -> MentalHealthResult(
                status  = "Low Mood Detected 🌧",
                emoji   = "😔",
                message = "Based on today's check-in, your mood and sleep suggest you may be experiencing some emotional difficulty. This is common and you are not alone.",
                advice  = "Consider talking to someone you trust. Small steps like a short walk, drinking water, or calling a friend can make a difference. If this persists, consider speaking to a counsellor.",
                color   = "orange",
                time    = time
            )
            else -> MentalHealthResult(
                status  = "Signs of Distress 💙",
                emoji   = "😢",
                message = "Your responses today suggest you may be going through a difficult time. Please know that what you're feeling is valid, and help is available.",
                advice  = "You don't have to face this alone. Please reach out to someone you trust, or contact a mental health helpline. Speaking to a counsellor or therapist can make a real difference — you deserve support.",
                color   = "red",
                time    = time
            )
        }
    }

    private fun parseSleepHours(sleepDuration: String): Int {
        return try {
            sleepDuration.trim().split(" ")[0].toInt()
        } catch (e: Exception) { 7 }
    }

    private fun getCurrentTime(): String {
        val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    // Factory to create ViewModel with Repository
    companion object {
        fun factory(repository: ActivityRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return MoodSpaceViewModel(repository) as T
                }
            }
        }
    }
}