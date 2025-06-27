package com.example.workoutapp

import android.content.Context
import android.media.MediaPlayer

class SoundPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(resId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.setOnCompletionListener {
            it.release()
        }
        mediaPlayer?.start()
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

