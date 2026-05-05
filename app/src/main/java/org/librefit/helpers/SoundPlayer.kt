/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2024-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.helpers

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.SoundPool
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.librefit.R
import org.librefit.di.qualifiers.MainDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages short, low-latency audio playback using [SoundPool].
 */
@Singleton
class SoundPlayer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : AutoCloseable {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(audioAttributes)
        .build()

    private val focusRequest =
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { /* No-op for transient sounds */ }
        .build()

    private val soundId: Int = soundPool.load(context, R.raw.alert_notification, 1)

    // Coroutine scope for focus management
    private val scope = CoroutineScope(mainDispatcher + SupervisorJob())


    /**
     * Plays the alert sound and ducks other audio.
     */
    fun playAlert() {
        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            scope.launch {
                // A short delay allows the "ducking" transition to start, preventing other media from masking the sound
                delay(300L)
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)

                // Hold focus for 1.5 seconds, which is sufficient for the sound (lasts 1 second) to finish.
                delay(1500L)
                audioManager.abandonAudioFocusRequest(focusRequest)
            }
        }
    }

    override fun close() {
        scope.cancel()
        audioManager.abandonAudioFocusRequest(focusRequest)
        soundPool.release()
    }
}
