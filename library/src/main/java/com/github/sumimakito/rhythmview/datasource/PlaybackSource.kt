package com.github.sumimakito.rhythmview.datasource

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.Log
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * A preset data source.
 *
 * Extract waveform data from the given media file using `MediaPlayer`.
 *
 * NOTICE: this data source requires the RECORD_AUDIO permission to work.
 */
class PlaybackSource(private val mediaPlayer: MediaPlayer, resolution: Int) : BaseDataSource<Int>(resolution) {
    companion object {
        private const val TAG = "PlaybackSource"
    }

    private var visualizer: Visualizer = Visualizer(mediaPlayer.audioSessionId)

    init {
        visualizer.enabled = false
        var captureSize = 2
        while (captureSize < resolution) {
            captureSize *= 2
        }
        if (captureSize > Visualizer.getCaptureSizeRange()[1]) {
            /**
             * This can cause an app crash.
             */
            Log.e(TAG, "Capture size $captureSize exceeded the maximum capture size: ${Visualizer.getCaptureSizeRange()[1]}. Will use the maximum capture size instead.")
            visualizer.captureSize = Visualizer.getCaptureSizeRange()[1]
        } else {
            visualizer.captureSize = captureSize
        }
        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onFftDataCapture(visualizer: Visualizer?, bytes: ByteArray?, samplingRate: Int) {

            }

            override fun onWaveFormDataCapture(p0: Visualizer?, bytes: ByteArray?, p2: Int) {
                val wave = Array(resolution) { 0 }
                val samplingInterval = max(1, floor((bytes!!.size - 1).toFloat() / wave.size).toInt())
                for (i in 0 until wave.size) {
                    /**
                     * Shift and clamp
                     */
                    wave[i] = max(0, min(256, bytes[i * samplingInterval] + 128))
                }
                data = wave
            }
        }, Visualizer.getMaxCaptureRate(), true, false)
        visualizer.enabled = true
    }
}