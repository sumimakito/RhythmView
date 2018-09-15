package com.github.sumimakito.rhythmview.effect

import android.graphics.*
import android.media.audiofx.Visualizer
import com.github.sumimakito.rhythmview.MathUtils
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.particle.Particle
import com.github.sumimakito.rhythmview.particle.ParticleManager
import com.github.sumimakito.rhythmview.wave.WavePoint
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

class Ripple(rhythmView: RhythmView, private val division: Int = 8, private val waveSpeed: Float = 0.06f, private val particleSpeed: Float = 0.01f) : BaseEffect(rhythmView) {

    init {
        minDrawingRadius += maxDrawingWidth * 0.3f
    }

    var colorHF: Int = 0xFFFFFFFF.toInt()
    var colorMF: Int = 0xFFFFFFFF.toInt()
    var colorLF: Int = 0xFFFFFFFF.toInt()
    var colorParticle: Int = 0xFFFFFFFF.toInt()
    var alphaHF: Float = 0.2f
    var alphaMF: Float = 0.2f
    var alphaLF: Float = 0.2f
    private var frameId = 0
    private var curvePathHF = Path()
    private var curvePathMF = Path()
    private var curvePathLF = Path()
    private var wavePoints = ArrayList<WavePoint>()
    private var curvePoints = ArrayList<PointF>()
    private var paintRipple = Paint()
    private var paintParticle = Paint()
    private var cachedWave: IntArray? = null
    private var delta: Float
    private var particleManager = ParticleManager()

    init {
        if (division < 4) throw RuntimeException("Division should be an integer larger than 4.")
        delta = 360f / division
        for (i in 0 until division * 3) {
            wavePoints.add(WavePoint(0f, waveSpeed, 0f, 1f))
        }

        particleManager.capacity = 200

        paintRipple.isAntiAlias = true
        paintRipple.pathEffect = CornerPathEffect(radius)

        paintParticle.color = Color.WHITE
        paintParticle.style = Paint.Style.FILL
        paintParticle.isAntiAlias = true

        computePoints()
        buildPath()
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
        if (frameId % 30 == 0) {
            val direction = (Math.random() * 360).toFloat()
            val startPoint = MathUtils.getPointOnCircle(centerX, centerY, minDrawingRadius * 0.8f, direction)
            particleManager.add(Particle(startPoint, direction, particleSpeed, maxDrawingWidth))
        }

        particleManager.nextTick()
        for (wavePoint in wavePoints) {
            wavePoint.nextTick()
        }
        computePoints()
        buildPath()

        frameId++
        if (frameId > 60) {
            frameId = 0
        }
    }

    override fun render(canvas: Canvas) {
        paintRipple.color = colorHF
        paintRipple.alpha = floor(255f * alphaHF).toInt()
        canvas.drawPath(curvePathHF, paintRipple)

        paintRipple.color = colorLF
        paintRipple.alpha = floor(255f * alphaLF).toInt()
        canvas.drawPath(curvePathLF, paintRipple)

        paintRipple.color = colorMF
        paintRipple.alpha = floor(255f * alphaMF).toInt()
        canvas.drawPath(curvePathMF, paintRipple)

        paintParticle.color = colorParticle
        for (particle in particleManager.particles) {
            var particleRadius = 10f
            if (particle.life < 0.2) {
                particleRadius *= ((particle.life + 0.1f) / 0.3f)
                paintParticle.alpha = floor(180 * ((particle.life) / 0.2f)).toInt()
            } else {
                paintParticle.alpha = 180
            }
            canvas.drawCircle(particle.x, particle.y, particleRadius, paintParticle)
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
        curvePoints.clear()
        for (i in 0 until wavePoints.size) {
            curvePoints.add(MathUtils.getPointOnCircle(centerX, centerY, minDrawingRadius + getWaveHeight(i), (i % 360) * delta))
        }
    }

    private fun buildPath() {
        curvePathLF.reset()
        curvePathLF.moveTo(curvePoints[0].x, curvePoints[0].y)
        for (i in 1 until division) {
            curvePathLF.lineTo(curvePoints[i].x, curvePoints[i].y)
        }
        curvePathLF.close()

        curvePathMF.reset()
        curvePathMF.moveTo(curvePoints[division].x, curvePoints[division].y)
        for (i in 1 until division) {
            curvePathMF.lineTo(curvePoints[division + i].x, curvePoints[division + i].y)
        }
        curvePathMF.close()

        curvePathHF.reset()
        curvePathHF.moveTo(curvePoints[division * 2].x, curvePoints[division * 2].y)
        for (i in 1 until division) {
            curvePathHF.lineTo(curvePoints[division * 2 + i].x, curvePoints[division * 2 + i].y)
        }
        curvePathHF.close()
    }

    private fun getWaveHeight(index: Int): Float {
        if (index < wavePoints.size) {
            return min(1f, max(0f, wavePoints[index].displayValue)) * maxDrawingWidth * 0.3f
        }
        return 0f
    }

}