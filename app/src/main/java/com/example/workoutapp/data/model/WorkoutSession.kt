package com.example.workoutapp.data.model

/**
 * Datenklasse für eine abgeschlossene Workout-Session
 * @param id Eindeutige ID der Session (Auto-generated)
 * @param completedExercises Anzahl abgeschlossener Übungen
 * @param totalExercises Gesamtanzahl der Übungen im Workout
 * @param duration Gesamtdauer des Workouts in Sekunden
 * @param completedAt Zeitstempel des Workout-Abschlusses
 */
data class WorkoutSession(
    val id: Long = 0,
    val completedExercises: Int,
    val totalExercises: Int,
    val duration: Long, // in Sekunden
    val completedAt: Long = System.currentTimeMillis()
)