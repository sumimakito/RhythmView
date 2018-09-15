package com.github.sumimakito.rhythmview.effect

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.media.audiofx.Visualizer
import com.github.sumimakito.rhythmview.MathUtils
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.wave.WavePoint
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Ray(rhythmView: RhythmView, private val division: Int = 256, private val waveSpeed: Float = 0.04f) : BaseEffect(rhythmView) {

    var colorHF: Int = 0xffef9a9a.toInt()
    var colorMF: Int = 0xff90caf9.toInt()
    var colorLF: Int = 0xffa5d6a7.toInt()
    var alphaHF: Float = 0.65f
    var alphaMF: Float = 0.65f
    var alphaLF: Float = 0.65f
    private var frameId = 0
    private var wavePoints = ArrayList<WavePoint>()
    private var outerPoints = ArrayList<PointF>()
    private var innerPoints = ArrayList<PointF>()
    private var paintRay = Paint()
    private var cachedWave: IntArray? = null
    private var delta: Float

    init {
        if (division < 4) throw RuntimeException("Division should be an integer larger than 4.")
        delta = 360f / division
        for (i in 0 until division * 3) {
            wavePoints.add(WavePoint(0f, waveSpeed, 0f, 1f))
        }

        paintRay.isAntiAlias = true
        paintRay.style = Paint.Style.STROKE
        paintRay.strokeWidth = 3f

        computePoints()
    }

    private fun refillWave(wave: IntArray) {
        for (i in 0 until min(wavePoints.size, wave.size)) {
            wavePoints[i].changeTo(wave[i] / 256f)
        }
    }

    override fun onFrameRendered() {
        if (frameId % 2 == 0) {
            if (cachedWave != null) refillWave(cachedWave!!)
        }

        for (wavePoint in wavePoints) {
            wavePoint.nextTick()
        }
        computePoints()

        frameId++
        if (frameId > 2) {
            frameId = 0
        }
    }

    override fun render(canvas: Canvas) {
        paintRay.color = colorHF
        paintRay.alpha = floor(255f * alphaHF).toInt()

        var ptIndex = 0
        for (i in 0 until division) {
            val start = innerPoints[ptIndex]
            val stop = outerPoints[ptIndex]
            canvas.drawLine(start.x, start.y, stop.x, stop.y, paintRay)
            ptIndex++
        }

        paintRay.color = colorLF
        paintRay.alpha = floor(255f * alphaLF).toInt()
        for (i in 0 until division) {
            val start = innerPoints[ptIndex]
            val stop = outerPoints[ptIndex]
            canvas.drawLine(start.x, start.y, stop.x, stop.y, paintRay)
            ptIndex++
        }

        paintRay.color = colorMF
        paintRay.alpha = floor(255f * alphaMF).toInt()
        for (i in 0 until division) {
            val start = innerPoints[ptIndex]
            val stop = outerPoints[ptIndex]
            canvas.drawLine(start.x, start.y, stop.x, stop.y, paintRay)
            ptIndex++
        }
    }

    override fun setupVisualizer(visualizer: Visualizer) {
        var captureSize = 2
        while (captureSize < division * 3) {
            captureSize *= 2
        }
        visualizer.captureSize = captureSize
        visualizer.setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
            override fun onFftDataCapture(visualizer: Visualizer?, bytes: ByteArray?, samplingRate: Int) {

            }

            override fun onWaveFormDataCapture(p0: Visualizer?, bytes: ByteArray?, p2: Int) {
                val wave = IntArray(division * 3)
                val samplingInterval = floor((bytes!!.size - 1).toFloat() / wave.size).toInt()
                for (i in 0 until wave.size) {
                    wave[i] = max(0, min(256, bytes[i * samplingInterval] + 128))
                }
                cachedWave = wave
            }
        }, Visualizer.getMaxCaptureRate(), true, false)
    }

    private fun computePoints() {
        outerPoints.clear()
        for (i in 0 until wavePoints.size) {
            val deg = (i % 360) * delta
            innerPoints.add(MathUtils.getPointOnCircle(centerX, centerY, minDrawingRadius, deg))
            outerPoints.add(MathUtils.getPointOnCircle(centerX, centerY, minDrawingRadius + getWaveHeight(i), deg))
        }
    }

    private fun getWaveHeight(index: Int): Float {
        if (index < wavePoints.size) {
            return min(1f, max(0f, wavePoints[index].displayValue)) * maxDrawingWidth
        }
        return 0f
    }

}