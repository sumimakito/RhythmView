package com.github.sumimakito.rhythmview.datasource

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.util.Log
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class PlaybackSource(private val mediaPlayer: MediaPlayer, size: Int) : BaseDataSource<Int>(size) {
    private var visualizer: Visualizer = Visualizer(mediaPlayer.audioSessionId)

    init {
        visualizer.enabled = false
        var captureSize = 2
        while (captureSize < size) {
            captureSize *= 2
        }
        visualizer.captureSize = captureSize
        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onFftDataCapture(visualizer: Visualizer?, bytes: ByteArray?, samplingRate: Int) {

            }

            override fun onWaveFormDataCapture(p0: Visualizer?, bytes: ByteArray?, p2: Int) {
                val wave = Array(size) { 0 }
                val samplingInterval = max(1, floor((bytes!!.size - 1).toFloat() / wave.size).toInt())
                for (i in 0 until wave.size) {
                    wave[i] = max(0, min(256, bytes[i * samplingInterval] + 128))
                }
                data = wave
            }
        }, Visualizer.getMaxCaptureRate(), true, false)
        visualizer.enabled = true
    }
}