package com.example.workoutapp

import android.os.Bundle
import android.app.AlertDialog
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
import android.media.MediaPlayer

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: WorkoutViewModel
    private var isWorkoutRunning = false
    private var isPaused = false
    private var startSound: MediaPlayer? = null
    private var completeSound: MediaPlayer? = null
    private var tickSound: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[WorkoutViewModel::class.java]

        initializeSounds()
        setupUI()
        observeViewModel()
        updateCompletedWorkoutsDisplay()
    }

    private fun initializeSounds() {
        try {

            startSound = MediaPlayer.create(this, R.raw.start_317318)
            completeSound = MediaPlayer.create(this, R.raw.stop_sound_274739)
            tickSound = MediaPlayer.create(this, R.raw.timer_55420)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playSound(mediaPlayer: MediaPlayer?) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()
                }
                it.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                Glide.with(this@MainActivity)
                    .load(exercise.imageResourceId)
                    .into(this)
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
            }
            WorkoutState.REST -> {
                stateText.text = "Pause"
                isWorkoutRunning = true
                findViewById<Button>(R.id.btn_start_pause).text = "Pause"
            }
            WorkoutState.COMPLETED -> {
                stateText.text = "Workout abgeschlossen!"
                isWorkoutRunning = false
                findViewById<Button>(R.id.btn_start_pause).text = "Start"
            }
        }
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
                playSound(startSound)
                viewModel.startWorkout()
            }
            isWorkoutRunning && !isPaused -> {
                viewModel.pauseWorkout()
                isPaused = true
                findViewById<Button>(R.id.btn_start_pause).text = "Fortsetzen"
            }
            isPaused -> {
                playSound(startSound)
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
        playSound(completeSound)
        AlertDialog.Builder(this)
            .setTitle("Glückwunsch!")
            .setMessage("Du hast das Workout erfolgreich abgeschlossen!")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        startSound?.release()
        completeSound?.release()
        tickSound?.release()
    }
}