package com.example.workoutapp

import android.content.Context
import android.media.MediaPlayer

/**
 * Utility-Klasse für die Wiedergabe von Sound-Effekten
 * Verwaltet MediaPlayer-Instanzen und spielt Workout-Feedback-Sounds ab
 */
class SoundPlayer(private val context: Context) {

    // MediaPlayer-Instanz für Sound-Wiedergabe
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Spielt einen Sound ab basierend auf der Resource-ID
     * Stoppt vorherige Sounds und startet den neuen
     * @param resId Resource-ID der abzuspielenden Sound-Datei
     */
    fun playSound(resId: Int) {
        // Vorherigen Sound stoppen und Ressourcen freigeben
        mediaPlayer?.release()

        // Neuen MediaPlayer erstellen und Sound laden
        mediaPlayer = MediaPlayer.create(context, resId)

        // Automatisches Cleanup nach Wiedergabe-Ende
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }

        // Sound abspielen
        mediaPlayer?.start()
    }

    /**
     * Gibt alle MediaPlayer-Ressourcen frei
     * Sollte aufgerufen werden wenn SoundPlayer nicht mehr benötigt wird
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
