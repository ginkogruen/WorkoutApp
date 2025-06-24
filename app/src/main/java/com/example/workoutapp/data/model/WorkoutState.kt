package com.example.workoutapp.data.model

/**
 * Enum für die verschiedenen Zustände des Workouts
 */
enum class WorkoutState {
    IDLE,       // Workout nicht gestartet/beendet
    EXERCISE,   // Übung läuft gerade
    REST,       // Pause zwischen Übungen
    COMPLETED   // Workout abgeschlossen
}