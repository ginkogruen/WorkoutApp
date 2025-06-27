package com.example.workoutapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.workoutapp.data.model.Exercise
import com.example.workoutapp.data.model.WorkoutState
import com.example.workoutapp.ui.viewmodel.WorkoutViewModel

/**
 * Haupt-Activity der Workout-App
 * Verwaltet die Benutzeroberfläche und koordiniert die Interaktion zwischen UI und ViewModel
 * Zeigt Übungen an, verwaltet Timer und spielt Sounds ab
 */
class MainActivity : AppCompatActivity() {

    // ViewModel für die Geschäftslogik des Workouts
    private lateinit var viewModel: WorkoutViewModel

    // Zustandsvariablen für die UI-Kontrolle
    private var isWorkoutRunning = false
    private var isPaused = false

    // Sound-Player für Feedback-Töne
    private lateinit var soundPlayer: SoundPlayer

    // Vorheriger Zustand für Sound-Transitions
    private var previousState: WorkoutState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisierung der Komponenten
        viewModel = ViewModelProvider(this)[WorkoutViewModel::class.java]
        soundPlayer = SoundPlayer(this)

        setupUI()
        observeViewModel()
        updateCompletedWorkoutsDisplay()
    }

    /**
     * Initialisiert die UI-Komponenten und setzt Event-Listener
     */
    private fun setupUI() {
        // Start/Pause Button Event-Handler
        findViewById<Button>(R.id.btn_start_pause).setOnClickListener {
            handleStartPauseClick()
        }

        // Reset Button Event-Handler
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            viewModel.resetWorkout()
            resetUI()
        }
    }

    /**
     * Registriert Observer für ViewModel LiveData-Objekte
     * Reagiert auf Änderungen im Workout-Zustand und aktualisiert die UI entsprechend
     */
    private fun observeViewModel() {
        // Beobachtet Änderungen der aktuellen Übung
        viewModel.currentExercise.observe(this) { exercise ->
            updateExerciseDisplay(exercise)
        }

        // Beobachtet den Timer-Countdown
        viewModel.timeRemaining.observe(this) { time ->
            findViewById<TextView>(R.id.tv_timer).apply {
                text = time.toString()
                visibility = if (time > 0) View.VISIBLE else View.GONE
            }
        }

        // Beobachtet Änderungen des Workout-Zustands
        viewModel.workoutState.observe(this) { state ->
            updateStateDisplay(state)
        }

        // Beobachtet den Fortschritt des Workouts
        viewModel.progress.observe(this) { (current, total) ->
            updateProgressDisplay(current, total)
        }

        // Beobachtet ob das Workout abgeschlossen ist
        viewModel.isWorkoutComplete.observe(this) { isComplete ->
            if (isComplete) {
                showWorkoutComplete()
                updateCompletedWorkoutsDisplay()
            }
        }
    }

    /**
     * Aktualisiert die Anzeige der aktuellen Übung
     * @param exercise Die anzuzeigende Übung oder null wenn keine aktiv ist
     */
    private fun updateExerciseDisplay(exercise: Exercise?) {
        // Übungsbild laden und anzeigen
        findViewById<ImageView>(R.id.iv_exercise).apply {
            if (exercise != null) {
                Glide.with(this@MainActivity)
                    .load(exercise.imageResourceId)
                    .into(this)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Übungsname anzeigen
        findViewById<TextView>(R.id.tv_exercise_name).apply {
            if (exercise != null) {
                text = exercise.name
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        // Übungsbeschreibung anzeigen
        findViewById<TextView>(R.id.tv_exercise_description).apply {
            if (exercise != null) {
                text = exercise.description
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    /**
     * Aktualisiert die Zustandsanzeige und spielt entsprechende Sounds ab
     * @param state Der neue Workout-Zustand
     */
    private fun updateStateDisplay(state: WorkoutState) {
        val stateText = findViewById<TextView>(R.id.tv_state)
        val startPauseButton = findViewById<Button>(R.id.btn_start_pause)

        // UI-Aktualisierung basierend auf dem Zustand und Sound-Feedback
        when (state) {
            WorkoutState.IDLE -> {
                stateText.text = "🚀 Bereit zum Training?"
                isWorkoutRunning = false
                isPaused = false
                startPauseButton.text = "▶️ Start"
            }
            WorkoutState.EXERCISE -> {
                stateText.text = "💪 Übung läuft"
                isWorkoutRunning = true
                startPauseButton.text = "⏸️ Pause"
                // Sound-Feedback für verschiedene Übergänge
                if (previousState == WorkoutState.REST) {
                    // Ende der Pause - zurück zur Übung
                    soundPlayer.playSound(R.raw.ep_542042__rob_marion__gasp_ui_alert_2)
                } else {
                    // Start einer neuen Übung
                    soundPlayer.playSound(R.raw.se_542035__rob_marion__gasp_ui_notification_4)
                }
            }
            WorkoutState.REST -> {
                stateText.text = "⏱️ Pause"
                isWorkoutRunning = true
                startPauseButton.text = "⏸️ Pause"
                if (previousState == WorkoutState.EXERCISE) {
                    // Ende einer Übung - Pause beginnt
                    soundPlayer.playSound(R.raw.ee_542009__rob_marion__gasp_marimba_correct_2)
                } else {
                    // Pause wird fortgesetzt
                    soundPlayer.playSound(R.raw.sp_541987__rob_marion__gasp_ui_clicks_5)
                }
            }
            WorkoutState.COMPLETED -> {
                stateText.text = "🎉 Workout abgeschlossen!"
                isWorkoutRunning = false
                startPauseButton.text = "▶️ Start"
                // Erfolgs-Sound für abgeschlossenes Workout
                soundPlayer.playSound(R.raw.ee_542009__rob_marion__gasp_marimba_correct_2)
            }
        }
        previousState = state
    }

    /**
     * Aktualisiert die Fortschrittsanzeige
     * @param current Anzahl der abgeschlossenen Übungen
     * @param total Gesamtanzahl der Übungen
     */
    private fun updateProgressDisplay(current: Int, total: Int) {
        findViewById<ProgressBar>(R.id.progress_bar).apply {
            max = total
            progress = current
        }

        findViewById<TextView>(R.id.tv_progress_text).text = "$current / $total Übungen"
    }

    /**
     * Aktualisiert die Anzeige der abgeschlossenen Workouts
     */
    private fun updateCompletedWorkoutsDisplay() {
        val completedWorkouts = viewModel.getCompletedWorkouts()
        findViewById<TextView>(R.id.tv_completed_workouts).text =
            "Abgeschlossene Workouts: $completedWorkouts"
    }

    /**
     * Behandelt Klicks auf den Start/Pause/Fortsetzen Button
     * Verwaltet die verschiedenen Zustände des Workout-Ablaufs
     */
    private fun handleStartPauseClick() {
        val startPauseButton = findViewById<Button>(R.id.btn_start_pause)

        when {
            // Workout ist nicht gestartet - starten
            !isWorkoutRunning && !isPaused -> {
                viewModel.startWorkout()
            }
            // Workout läuft - pausieren
            isWorkoutRunning && !isPaused -> {
                viewModel.pauseWorkout()
                isPaused = true
                startPauseButton.text = "▶️ Fortsetzen"
            }
            // Workout ist pausiert - fortsetzen
            isPaused -> {
                viewModel.resumeWorkout()
                isPaused = false
                startPauseButton.text = "⏸️ Pause"
            }
        }
    }

    /**
     * Setzt die UI in den Ausgangszustand zurück
     */
    private fun resetUI() {
        isWorkoutRunning = false
        isPaused = false
        findViewById<Button>(R.id.btn_start_pause).text = "▶️ Start"
        findViewById<TextView>(R.id.tv_timer).visibility = View.GONE
    }

    /**
     * Zeigt einen Glückwunsch-Dialog bei erfolgreichem Workout-Abschluss
     */
    private fun showWorkoutComplete() {
        AlertDialog.Builder(this)
            .setTitle("Glückwunsch!")
            .setMessage("Du hast das Workout erfolgreich abgeschlossen!")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }
}

