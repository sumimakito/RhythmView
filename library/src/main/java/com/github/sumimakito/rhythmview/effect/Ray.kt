package com.github.sumimakito.rhythmview.effect

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.util.MathUtils
import com.github.sumimakito.rhythmview.wave.WavePoint
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * A preset visual effect.
 *
 * Resolution of the data source should not be less than `resolution * 3` here.
 *
 * When using with `PlaybackSource`, parameter `resolution` should not be larger than 256, or the
 * capture size may exceeded the maximum capture size of the system.
 */
class Ray @JvmOverloads constructor(rhythmView: RhythmView, private val resolution: Int = 256, private val waveSpeed: Float = 0.04f) : BaseEffect<Int>(rhythmView) {
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
    private var delta: Float

    init {
        if (resolution < 4) throw RuntimeException("Division should be an integer larger than 4.")
        delta = 360f / resolution
        for (i in 0 until resolution * 3) {
            wavePoints.add(WavePoint(0f, waveSpeed, 0f, 1f))
        }

        paintRay.isAntiAlias = true
        paintRay.style = Paint.Style.STROKE
        paintRay.strokeWidth = 3f

        computePoints()
    }

    private fun refillWave(wave: Array<Int>) {
        for (i in 0 until min(wavePoints.size, wave.size)) {
            wavePoints[i].changeTo(wave[i] / 256f)
        }
    }

    override fun onFrameRendered() {
        if (frameId % 2 == 0) {
            if (dataSource != null && dataSource!!.data != null) refillWave(dataSource!!.data!!)
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
        for (i in 0 until resolution) {
            val start = innerPoints[ptIndex]
            val stop = outerPoints[ptIndex]
            canvas.drawLine(start.x, start.y, stop.x, stop.y, paintRay)
            ptIndex++
        }

        paintRay.color = colorLF
        paintRay.alpha = floor(255f * alphaLF).toInt()
        for (i in 0 until resolution) {
            val start = innerPoints[ptIndex]
            val stop = outerPoints[ptIndex]
            canvas.drawLine(start.x, start.y, stop.x, stop.y, paintRay)
            ptIndex++
        }

        paintRay.color = colorMF
        paintRay.alpha = floor(255f * alphaMF).toInt()
        for (i in 0 until resolution) {
            val start = innerPoints[ptIndex]
            val stop = outerPoints[ptIndex]
            canvas.drawLine(start.x, start.y, stop.x, stop.y, paintRay)
            ptIndex++
        }
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