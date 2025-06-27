package com.example.workoutapp.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.workoutapp.data.model.Exercise
import com.example.workoutapp.data.model.WorkoutState

/**
 * ViewModel für die Workout-Geschäftslogik
 * Verwaltet den Workout-Ablauf, Timer-Funktionalität und Zustandsänderungen
 * Implementiert MVVM-Pattern und trennt UI-Logik von Geschäftslogik
 */
class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        // Konstanten für Timer-Dauern
        private const val EXERCISE_TIME = 30 * 1000L // 30 Sekunden pro Übung
        private const val REST_TIME = 15 * 1000L // 15 Sekunden Pause zwischen Übungen

        // SharedPreferences Konstanten
        private const val PREFS_NAME = "workout_prefs"
        private const val COMPLETED_WORKOUTS_KEY = "completed_workouts"
    }

    // LiveData für die aktuelle Übung (beobachtbar von der UI)
    private val _currentExercise = MutableLiveData<Exercise?>(null)
    val currentExercise: LiveData<Exercise?> = _currentExercise

    // LiveData für die verbleibende Zeit des aktuellen Timers
    private val _timeRemaining = MutableLiveData(0L)
    val timeRemaining: LiveData<Long> = _timeRemaining

    // LiveData für den aktuellen Workout-Zustand
    private val _workoutState = MutableLiveData(WorkoutState.IDLE)
    val workoutState: LiveData<WorkoutState> = _workoutState

    // LiveData für den Fortschritt (aktuelle Übung / Gesamtübungen)
    private val _progress = MutableLiveData(Pair(0, 0))
    val progress: LiveData<Pair<Int, Int>> = _progress

    // LiveData für Workout-Abschluss-Status
    private val _isWorkoutComplete = MutableLiveData(false)
    val isWorkoutComplete: LiveData<Boolean> = _isWorkoutComplete

    // Timer-Instanz für Countdown-Funktionalität
    private var timer: CountDownTimer? = null

    // Liste der Workout-Übungen
    private var exercises = mutableListOf<Exercise>()

    // Index der aktuell aktiven Übung
    private var currentExerciseIndex = -1

    // Gespeicherte Zeit bei Pause-Funktionalität
    private var pausedTimeRemaining = 0L

    init {
        loadExercises()
        _progress.value = Pair(0, exercises.size)
    }

    /**
     * Lädt die Übungen für das Workout
     * TODO: In Zukunft aus Repository/Datenbank laden
     */
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

    /**
     * Startet ein neues Workout oder setzt ein abgeschlossenes zurück
     */
    fun startWorkout() {
        if (_workoutState.value == WorkoutState.IDLE || _workoutState.value == WorkoutState.COMPLETED) {
            currentExerciseIndex = -1
            _isWorkoutComplete.value = false
            nextExercise()
        }
    }

    /**
     * Pausiert das laufende Workout und speichert die verbleibende Zeit
     */
    fun pauseWorkout() {
        timer?.cancel()
        pausedTimeRemaining = _timeRemaining.value ?: 0L
    }

    /**
     * Setzt das Workout mit der gespeicherten Zeit fort
     */
    fun resumeWorkout() {
        startTimer(pausedTimeRemaining)
    }

    /**
     * Setzt das komplette Workout auf den Ausgangszustand zurück
     */
    fun resetWorkout() {
        timer?.cancel()
        _currentExercise.value = null
        _timeRemaining.value = 0L
        _workoutState.value = WorkoutState.IDLE
        _progress.value = Pair(0, exercises.size)
        _isWorkoutComplete.value = false
        currentExerciseIndex = -1
    }

    /**
     * Wechselt zur nächsten Übung oder beendet das Workout
     * Verwaltet die Workout-Progression
     */
    private fun nextExercise() {
        timer?.cancel()
        currentExerciseIndex++

        // Prüfen ob alle Übungen abgeschlossen sind
        if (currentExerciseIndex >= exercises.size) {
            completeWorkout()
            return
        }

        // Nächste Übung setzen und Timer starten
        _currentExercise.value = exercises[currentExerciseIndex]
        _progress.value = Pair(currentExerciseIndex + 1, exercises.size)
        _workoutState.value = WorkoutState.EXERCISE
        startTimer(EXERCISE_TIME)
    }

    /**
     * Startet eine Pause zwischen den Übungen
     */
    private fun startRest() {
        _workoutState.value = WorkoutState.REST
        startTimer(REST_TIME)
    }

    /**
     * Startet einen Countdown-Timer mit der angegebenen Dauer
     * @param duration Timer-Dauer in Millisekunden
     */
    private fun startTimer(duration: Long) {
        timer = object : CountDownTimer(duration, 1000) {
            // Wird jede Sekunde aufgerufen
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished / 1000
            }

            // Wird aufgerufen wenn Timer abläuft
            override fun onFinish() {
                when (_workoutState.value) {
                    WorkoutState.EXERCISE -> startRest() // Nach Übung kommt Pause
                    WorkoutState.REST -> nextExercise() // Nach Pause kommt nächste Übung
                    else -> {} // Andere Zustände ignorieren
                }
            }
        }.start()
    }

    /**
     * Beendet das Workout und markiert es als abgeschlossen
     * Speichert die Statistiken
     */
    private fun completeWorkout() {
        timer?.cancel()
        _workoutState.value = WorkoutState.COMPLETED
        _isWorkoutComplete.value = true
        _currentExercise.value = null
        incrementCompletedWorkouts()
    }

    /**
     * Erhöht den Zähler für abgeschlossene Workouts in SharedPreferences
     */
    private fun incrementCompletedWorkouts() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentCount = sharedPrefs.getInt(COMPLETED_WORKOUTS_KEY, 0)
        sharedPrefs.edit().putInt(COMPLETED_WORKOUTS_KEY, currentCount + 1).apply()
    }

    /**
     * Gibt die Anzahl der abgeschlossenen Workouts zurück
     * @return Anzahl abgeschlossener Workouts
     */
    fun getCompletedWorkouts(): Int {
        val sharedPrefs = getApplication<Application>().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getInt(COMPLETED_WORKOUTS_KEY, 0)
    }

    /**
     * Lifecycle-Methode: Cleanup beim Zerstören des ViewModels
     * Stellt sicher dass Timer-Ressourcen freigegeben werden
     */
    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}