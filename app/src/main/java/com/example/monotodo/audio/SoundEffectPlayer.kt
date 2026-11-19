package com.example.monotodo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.monotodo.R

class SoundEffectPlayer(context: Context) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(audioAttributes)
        .build()

    private val completeId = soundPool.load(context, R.raw.sfx_complete, 1)
    private val uncheckId = soundPool.load(context, R.raw.sfx_uncheck, 1)
    private val addId = soundPool.load(context, R.raw.sfx_add, 1)
    private val deleteId = soundPool.load(context, R.raw.sfx_delete, 1)
    private val tapId = soundPool.load(context, R.raw.sfx_tap, 1)
    private val allDoneId = soundPool.load(context, R.raw.sfx_all_done, 1)

    var enabled: Boolean = true

    fun playComplete() = play(completeId, 0.85f)
    fun playUncheck() = play(uncheckId, 0.70f)
    fun playAdd() = play(addId, 0.80f)
    fun playDelete() = play(deleteId, 0.75f)
    fun playTap() = play(tapId, 0.50f)
    fun playAllDone() = play(allDoneId, 0.90f)

    private fun play(soundId: Int, volume: Float) {
        if (!enabled || soundId == 0) return
        soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
    }

    fun release() {
        soundPool.release()
    }
}
