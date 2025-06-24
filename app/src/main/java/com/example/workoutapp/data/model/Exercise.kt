package com.example.workoutapp.data.model

/**
 * Datenklasse für eine einzelne Übung
 * @param id Eindeutige ID der Übung
 * @param name Name der Übung (z.B. "Push-ups")
 * @param description Beschreibung der Übungsausführung
 * @param imageResourceId Resource ID des Übungsbildes
 * @param duration Dauer der Übung in Sekunden (Standard: 30s)
 */
data class Exercise(
    val id: Int,
    val name: String,
    val description: String,
    val imageResourceId: Int,
    val duration: Int = 30 // Standarddauer: 30 Sekunden
)