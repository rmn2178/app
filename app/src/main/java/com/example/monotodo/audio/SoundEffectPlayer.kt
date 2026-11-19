package com.example.monotodo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.monotodo.R
import com.example.monotodo.model.SoundPack
import com.example.monotodo.model.SoundSettings

class SoundEffectPlayer(private val appContext: Context) {
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()

    @Volatile private var softComplete = 0
    @Volatile private var softAdd = 0
    @Volatile private var softDelete = 0
    @Volatile private var softTap = 0

    @Volatile private var crispComplete = 0
    @Volatile private var crispAdd = 0
    @Volatile private var crispDelete = 0
    @Volatile private var crispTap = 0

    @Volatile private var zenComplete = 0
    @Volatile private var zenAdd = 0
    @Volatile private var zenDelete = 0
    @Volatile private var zenTap = 0

    @Volatile private var released = false
    @Volatile var settings: SoundSettings = SoundSettings()

    /**
     * Call from Dispatchers.IO so audio loading never stalls app startup frames.
     */
    fun preload() {
        if (released) return
        softComplete = soundPool.load(appContext, R.raw.sfx_soft_complete, 1)
        softAdd = soundPool.load(appContext, R.raw.sfx_soft_add, 1)
        softDelete = soundPool.load(appContext, R.raw.sfx_soft_delete, 1)
        softTap = soundPool.load(appContext, R.raw.sfx_soft_tap, 1)

        crispComplete = soundPool.load(appContext, R.raw.sfx_crisp_complete, 1)
        crispAdd = soundPool.load(appContext, R.raw.sfx_crisp_add, 1)
        crispDelete = soundPool.load(appContext, R.raw.sfx_crisp_delete, 1)
        crispTap = soundPool.load(appContext, R.raw.sfx_crisp_tap, 1)

        zenComplete = soundPool.load(appContext, R.raw.sfx_zen_complete, 1)
        zenAdd = soundPool.load(appContext, R.raw.sfx_zen_add, 1)
        zenDelete = soundPool.load(appContext, R.raw.sfx_zen_delete, 1)
        zenTap = soundPool.load(appContext, R.raw.sfx_zen_tap, 1)
    }

    fun playComplete() {
        val s = settings
        if (!s.enabled || !s.onComplete) return
        val id = when (s.pack) {
            SoundPack.SOFT -> softComplete
            SoundPack.CRISP -> crispComplete
            SoundPack.ZEN -> zenComplete
        }
        play(id, 1.0f)
    }

    fun playAdd() {
        val s = settings
        if (!s.enabled || !s.onAdd) return
        val id = when (s.pack) {
            SoundPack.SOFT -> softAdd
            SoundPack.CRISP -> crispAdd
            SoundPack.ZEN -> zenAdd
        }
        play(id, 0.9f)
    }

    fun playDelete() {
        val s = settings
        if (!s.enabled || !s.onDelete) return
        val id = when (s.pack) {
            SoundPack.SOFT -> softDelete
            SoundPack.CRISP -> crispDelete
            SoundPack.ZEN -> zenDelete
        }
        play(id, 0.85f)
    }

    fun playTap() {
        val s = settings
        if (!s.enabled || !s.onTap) return
        val id = when (s.pack) {
            SoundPack.SOFT -> softTap
            SoundPack.CRISP -> crispTap
            SoundPack.ZEN -> zenTap
        }
        play(id, 0.65f)
    }

    fun previewPack(pack: SoundPack, volume: Float) {
        if (released) return
        val id = when (pack) {
            SoundPack.SOFT -> softComplete
            SoundPack.CRISP -> crispComplete
            SoundPack.ZEN -> zenComplete
        }
        val v = volume.coerceIn(0f, 1f)
        val gain = (v * v).coerceAtLeast(0.08f)
        if (id != 0) {
            soundPool.play(id, gain, gain, 1, 0, 1.0f)
        }
    }

    private fun play(soundId: Int, weight: Float) {
        if (released || soundId == 0) return
        val v = settings.volume.coerceIn(0f, 1f)
        val gain = (v * v * weight).coerceIn(0f, 1f)
        if (gain > 0.005f) {
            soundPool.play(soundId, gain, gain, 1, 0, 1.0f)
        }
    }

    fun release() {
        released = true
        soundPool.release()
    }
}
