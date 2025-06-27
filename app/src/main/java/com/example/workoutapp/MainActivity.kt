package com.example.workoutapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.workoutapp.ui.theme.WorkoutAppTheme
import android.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.workoutapp.data.model.Exercise
import com.example.workoutapp.data.model.WorkoutState
import com.example.workoutapp.data.model.WorkoutSession
import com.example.workoutapp.data.repository.WorkoutRepository
import com.example.workoutapp.ui.viewmodel.WorkoutViewModel


// MainActivity.kt
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: WorkoutViewModel
    private var isWorkoutRunning = false
    private var isPaused = false
    private lateinit var soundPlayer: SoundPlayer
    private var previousState: WorkoutState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[WorkoutViewModel::class.java]
        soundPlayer = SoundPlayer(this)

        setupUI()
        observeViewModel()
        updateCompletedWorkoutsDisplay()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btn_start_pause).setOnClickListener {
            handleStartPauseClick()
        }

        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            viewModel.resetWorkout()
            resetUI()
        }
    }

    private fun observeViewModel() {
        viewModel.currentExercise.observe(this) { exercise ->
            updateExerciseDisplay(exercise)
        }

        viewModel.timeRemaining.observe(this) { time ->
            findViewById<TextView>(R.id.tv_timer).apply {
                text = time.toString()
                visibility = if (time > 0) View.VISIBLE else View.GONE
            }
        }

        viewModel.workoutState.observe(this) { state ->
            updateStateDisplay(state)
        }

        viewModel.progress.observe(this) { (current, total) ->
            updateProgressDisplay(current, total)
        }

        viewModel.isWorkoutComplete.observe(this) { isComplete ->
            if (isComplete) {
                showWorkoutComplete()
                updateCompletedWorkoutsDisplay()
            }
        }
    }

    private fun updateExerciseDisplay(exercise: Exercise?) {
        findViewById<ImageView>(R.id.iv_exercise).apply {
            if (exercise != null) {
                setImageResource(exercise.imageResourceId)
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        findViewById<TextView>(R.id.tv_exercise_name).apply {
            if (exercise != null) {
                text = exercise.name
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }

        findViewById<TextView>(R.id.tv_exercise_description).apply {
            if (exercise != null) {
                text = exercise.description
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
    }

    private fun updateStateDisplay(state: WorkoutState) {
        val stateText = findViewById<TextView>(R.id.tv_state)
        // Play sounds based on state transitions
        when (state) {
            WorkoutState.IDLE -> {
                stateText.text = "Bereit zum Training?"
                isWorkoutRunning = false
                isPaused = false
                findViewById<Button>(R.id.btn_start_pause).text = "Start"
            }
            WorkoutState.EXERCISE -> {
                stateText.text = "Übung läuft"
                isWorkoutRunning = true
                findViewById<Button>(R.id.btn_start_pause).text = "Pause"
                if (previousState == WorkoutState.REST) {
                    // End Pause
                    soundPlayer.playSound(R.raw.ep_542042__rob_marion__gasp_ui_alert_2)
                } else {
                    // Start Exercise
                    soundPlayer.playSound(R.raw.se_542035__rob_marion__gasp_ui_notification_4)
                }
            }
            WorkoutState.REST -> {
                stateText.text = "Pause"
                isWorkoutRunning = true
                findViewById<Button>(R.id.btn_start_pause).text = "Pause"
                if (previousState == WorkoutState.EXERCISE) {
                    // End Exercise
                    soundPlayer.playSound(R.raw.ee_542009__rob_marion__gasp_marimba_correct_2)
                } else {
                    // Start Pause
                    soundPlayer.playSound(R.raw.sp_541987__rob_marion__gasp_ui_clicks_5)
                }
            }
            WorkoutState.COMPLETED -> {
                stateText.text = "Workout abgeschlossen!"
                isWorkoutRunning = false
                findViewById<Button>(R.id.btn_start_pause).text = "Start"
                soundPlayer.playSound(R.raw.ee_542009__rob_marion__gasp_marimba_correct_2)
            }
        }
        previousState = state
    }

    private fun updateProgressDisplay(current: Int, total: Int) {
        findViewById<ProgressBar>(R.id.progress_bar).apply {
            max = total
            progress = current
        }

        findViewById<TextView>(R.id.tv_progress_text).text = "$current / $total Übungen"
    }

    private fun updateCompletedWorkoutsDisplay() {
        val completedWorkouts = viewModel.getCompletedWorkouts()
        findViewById<TextView>(R.id.tv_completed_workouts).text =
            "Abgeschlossene Workouts: $completedWorkouts"
    }

    private fun handleStartPauseClick() {
        when {
            !isWorkoutRunning && !isPaused -> {
                viewModel.startWorkout()
            }
            isWorkoutRunning && !isPaused -> {
                viewModel.pauseWorkout()
                isPaused = true
                findViewById<Button>(R.id.btn_start_pause).text = "Fortsetzen"
            }
            isPaused -> {
                viewModel.resumeWorkout()
                isPaused = false
                findViewById<Button>(R.id.btn_start_pause).text = "Pause"
            }
        }
    }

    private fun resetUI() {
        isWorkoutRunning = false
        isPaused = false
        findViewById<Button>(R.id.btn_start_pause).text = "Start"
        findViewById<TextView>(R.id.tv_timer).visibility = View.GONE
    }

    private fun showWorkoutComplete() {
        AlertDialog.Builder(this)
            .setTitle("Glückwunsch!")
            .setMessage("Du hast das Workout erfolgreich abgeschlossen!")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
    }
}