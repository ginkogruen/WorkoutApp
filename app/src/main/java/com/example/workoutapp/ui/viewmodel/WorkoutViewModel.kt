package com.example.workoutapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.workoutapp.data.model.Exercise
import com.example.workoutapp.data.model.WorkoutState

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val EXERCISE_TIME = 30 * 1000L // 30 Sekunden
        private const val REST_TIME = 15 * 1000L // 15 Sekunden
        private const val PREFS_NAME = "workout_prefs"
        private const val COMPLETED_WORKOUTS_KEY = "completed_workouts"
    }

    private val _currentExercise = MutableLiveData<Exercise?>(null)
    val currentExercise: LiveData<Exercise?> = _currentExercise

    private val _timeRemaining = MutableLiveData(0L)
    val timeRemaining: LiveData<Long> = _timeRemaining

    private val _workoutState = MutableLiveData(WorkoutState.IDLE)
    val workoutState: LiveData<WorkoutState> = _workoutState

    private val _progress = MutableLiveData(Pair(0, 0))
    val progress: LiveData<Pair<Int, Int>> = _progress

    private val _isWorkoutComplete = MutableLiveData(false)
    val isWorkoutComplete: LiveData<Boolean> = _isWorkoutComplete

    private var timer: CountDownTimer? = null
    private var exercises = mutableListOf<Exercise>()
    private var currentExerciseIndex = -1
    private var pausedTimeRemaining = 0L

    init {
        loadExercises()
        _progress.value = Pair(0, exercises.size)
    }

    private fun loadExercises() {
        // Hier könnten die Übungen aus einer Datenbank oder einer anderen Quelle geladen werden
        // Für jetzt verwenden wir Beispielübungen
        exercises = mutableListOf(
            Exercise(1, "Kniebeugen", "Stehe mit den Füßen schulterbreit auseinander und gehe in die Hocke",
                com.example.workoutapp.R.drawable.bodyweight_squat_example_2650866943),
            Exercise(2, "Liegestütze", "Stütze dich mit den Händen ab und senke deinen Körper",
                com.example.workoutapp.R.drawable.resistance_band_push_ups_3631796511),
            Exercise(3, "Sit-ups", "Lege dich auf den Rücken und hebe deinen Oberkörper an",
                com.example.workoutapp.R.drawable.sit_ups_2_1000x1000_2124161126),
            Exercise(4, "Ausfallschritte", "Mache einen großen Schritt nach vorne und beuge das Knie",
                com.example.workoutapp.R.drawable.klassische_lunges_uebung_2434463905),
            Exercise(5, "Planke", "Halte deinen Körper gerade und stütze dich auf Unterarmen und Zehenspitzen ab",
                com.example.workoutapp.R.drawable.body_saw_plank_3580357734)
        )
    }

    fun startWorkout() {
        if (_workoutState.value == WorkoutState.IDLE || _workoutState.value == WorkoutState.COMPLETED) {
            currentExerciseIndex = -1
            _isWorkoutComplete.value = false
            nextExercise()
        }
    }

    fun pauseWorkout() {
        timer?.cancel()
        pausedTimeRemaining = _timeRemaining.value ?: 0L
    }

    fun resumeWorkout() {
        startTimer(pausedTimeRemaining)
    }

    fun resetWorkout() {
        timer?.cancel()
        _currentExercise.value = null
        _timeRemaining.value = 0L
        _workoutState.value = WorkoutState.IDLE
        _progress.value = Pair(0, exercises.size)
        _isWorkoutComplete.value = false
        currentExerciseIndex = -1
    }

    private fun nextExercise() {
        timer?.cancel()
        currentExerciseIndex++

        if (currentExerciseIndex >= exercises.size) {
            completeWorkout()
            return
        }

        _currentExercise.value = exercises[currentExerciseIndex]
        _progress.value = Pair(currentExerciseIndex + 1, exercises.size)
        _workoutState.value = WorkoutState.EXERCISE
        startTimer(EXERCISE_TIME)
    }

    private fun startRest() {
        _workoutState.value = WorkoutState.REST
        startTimer(REST_TIME)
    }

    private fun startTimer(duration: Long) {
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                if (_workoutState.value == WorkoutState.EXERCISE) {
                    startRest()
                } else if (_workoutState.value == WorkoutState.REST) {
                    nextExercise()
                }
            }
        }.start()
    }

    private fun completeWorkout() {
        timer?.cancel()
        _workoutState.value = WorkoutState.COMPLETED
        _isWorkoutComplete.value = true
        _currentExercise.value = null
        incrementCompletedWorkouts()
    }

    private fun incrementCompletedWorkouts() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = sharedPrefs.getInt(COMPLETED_WORKOUTS_KEY, 0)
        sharedPrefs.edit().putInt(COMPLETED_WORKOUTS_KEY, currentCount + 1).apply()
    }

    fun getCompletedWorkouts(): Int {
        val sharedPrefs = getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(COMPLETED_WORKOUTS_KEY, 0)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}