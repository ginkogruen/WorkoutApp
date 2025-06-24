package com.example.workoutapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.workoutapp.R
import com.example.workoutapp.data.model.Exercise
import com.example.workoutapp.data.model.WorkoutSession

/**
 * Repository-Klasse für Workout-Daten
 * Verwaltet Übungen und speichert Workout-Sessions
 */
class WorkoutRepository(private val context: Context) {

    // SharedPreferences für lokale Datenspeicherung
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "workout_prefs"
        private const val KEY_COMPLETED_WORKOUTS = "completed_workouts"
        private const val KEY_TOTAL_WORKOUT_TIME = "total_workout_time"
        private const val KEY_LAST_WORKOUT = "last_workout"
    }

    /**
     * Gibt eine Liste aller verfügbaren Übungen zurück
     * @return List<Exercise> - Vordefinierte Übungen
     */
    fun getExercises(): List<Exercise> {
        return listOf(
            Exercise(
                id = 1,
                name = "Push-ups",
                description = "Klassische Liegestütze für Brust- und Armmuskulatur. " +
                        "Hände schulterbreit aufstellen, Körper gerade halten.",
                imageResourceId = R.drawable.ic_pushups,
                duration = 30
            ),
            Exercise(
                id = 2,
                name = "Squats",
                description = "Kniebeugen für Bein- und Gesäßmuskulatur. " +
                        "Füße hüftbreit, Po nach hinten schieben.",
                imageResourceId = R.drawable.ic_squats,
                duration = 30
            ),
            Exercise(
                id = 3,
                name = "Plank",
                description = "Unterarmstütz für Core-Stabilität. " +
                        "Körper bildet eine gerade Linie vom Kopf bis zu den Füßen.",
                imageResourceId = R.drawable.ic_plank,
                duration = 30
            ),
            Exercise(
                id = 4,
                name = "Jumping Jacks",
                description = "Hampelmänner für Cardio-Training. " +
                        "Rhythmische Sprungbewegung mit Arm- und Beinbewegung.",
                imageResourceId = R.drawable.ic_sit_ups,
                duration = 30
            ),
            Exercise(
                id = 5,
                name = "Lunges",
                description = "Ausfallschritte für Beinmuskulatur. " +
                        "Großer Schritt nach vorn, Knie im 90°-Winkel beugen.",
                imageResourceId = R.drawable.ic_lunges,
                duration = 30
            )
        )
    }

    /**
     * Speichert eine abgeschlossene Workout-Session
     * @param session Die zu speichernde WorkoutSession
     */
    fun saveWorkoutSession(session: WorkoutSession) {
        val currentCompletedWorkouts = getCompletedWorkouts()
        val currentTotalTime = getTotalWorkoutTime()

        sharedPrefs.edit()
            .putInt(KEY_COMPLETED_WORKOUTS, currentCompletedWorkouts + 1)
            .putLong(KEY_TOTAL_WORKOUT_TIME, currentTotalTime + session.duration)
            .putLong(KEY_LAST_WORKOUT, session.completedAt)
            .apply()
    }

    /**
     * Gibt die Anzahl abgeschlossener Workouts zurück
     * @return Int - Anzahl der abgeschlossenen Workouts
     */
    fun getCompletedWorkouts(): Int {
        return sharedPrefs.getInt(KEY_COMPLETED_WORKOUTS, 0)
    }

    /**
     * Gibt die gesamte Workout-Zeit zurück
     * @return Long - Gesamte Workout-Zeit in Sekunden
     */
    fun getTotalWorkoutTime(): Long {
        return sharedPrefs.getLong(KEY_TOTAL_WORKOUT_TIME, 0L)
    }

    /**
     * Gibt den Zeitstempel des letzten Workouts zurück
     * @return Long - Zeitstempel des letzten Workouts
     */
    fun getLastWorkoutDate(): Long {
        return sharedPrefs.getLong(KEY_LAST_WORKOUT, 0L)
    }

    /**
     * Löscht alle gespeicherten Workout-Daten (für Reset-Funktion)
     */
    fun clearAllData() {
        sharedPrefs.edit().clear().apply()
    }

    /**
     * Gibt Workout-Statistiken zurück
     * @return Map<String, Any> - Verschiedene Statistiken
     */
    fun getWorkoutStatistics(): Map<String, Any> {
        val completedWorkouts = getCompletedWorkouts()
        val totalTime = getTotalWorkoutTime()
        val averageTime = if (completedWorkouts > 0) totalTime / completedWorkouts else 0L

        return mapOf(
            "completedWorkouts" to completedWorkouts,
            "totalTimeSeconds" to totalTime,
            "averageTimeSeconds" to averageTime,
            "lastWorkout" to getLastWorkoutDate()
        )
    }
}