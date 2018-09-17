package com.github.sumimakito.rhythmview.effect

import android.graphics.*
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.particle.Particle
import com.github.sumimakito.rhythmview.particle.ParticleManager
import com.github.sumimakito.rhythmview.util.MathUtils
import com.github.sumimakito.rhythmview.wave.WavePoint
import java.util.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * A preset visual effect.
 *
 * Resolution of the data source should not be less than `resolution * 3` here.
 *
 * Parameter `resolution` should not be larger than 16 or the edges may looks not smooth enough.
 *
 * When using with `PlaybackSource`, parameter `resolution` should not be larger than 256, or the
 * capture size may exceeded the maximum capture size of the system.
 */
class Ripple @JvmOverloads constructor(rhythmView: RhythmView, private val resolution: Int = 8, private val waveSpeed: Float = 0.06f, private val particleSpeed: Float = 0.005f) : BaseEffect<Int>(rhythmView) {
    init {
        /**
         * Since the radius for the paths seems to be not that precise, in order to reveal the ripple
         * even if the playback has stopped, I applied a dirty-fix.
         */
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
    private var delta: Float
    private var particleManager = ParticleManager()
    private var allZero: Boolean = true

    init {
        if (resolution < 4) throw RuntimeException("Division should be an integer larger than 4.")
        delta = 360f / resolution
        for (i in 0 until resolution * 3) {
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

    private fun refillWave(wave: Array<Int>) {
        var allZero = true
        for (i in 0 until min(wavePoints.size, wave.size)) {
            if (wave[i] != 0) allZero = false
            wavePoints[i].changeTo(wave[i] / 256f)
        }
        this.allZero = allZero
    }

    override fun onFrameRendered() {
        /**
         * Refill the waveform data every two frames to reduce the changing rate for the wave.
         */
        if (frameId % 2 == 0) {
            if (dataSource != null && dataSource!!.data != null) {
                refillWave(dataSource!!.data!!)
            }
        }

        /**
         * Create a new particle every 30 frames.
         *
         * If the playback stops (waveform has no non-zero items), stop creating new particles.
         */
        if (!allZero && frameId % 30 == 0) {
            val direction = (Math.random() * 360).toFloat()
            val startPoint = MathUtils.getPointOnCircle(centerX, centerY, minDrawingRadius * 0.5f, direction)
            particleManager.add(Particle(startPoint, direction, particleSpeed, minDrawingRadius * 0.5f + maxDrawingWidth * 0.4f))
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

    private fun computePoints() {
        curvePoints.clear()
        for (i in 0 until wavePoints.size) {
            curvePoints.add(MathUtils.getPointOnCircle(centerX, centerY, minDrawingRadius + getWaveHeight(i), (i % 360) * delta))
        }
    }

    private fun buildPath() {
        curvePathLF.reset()
        curvePathLF.moveTo(curvePoints[0].x, curvePoints[0].y)
        for (i in 1 until resolution) {
            curvePathLF.lineTo(curvePoints[i].x, curvePoints[i].y)
        }
        curvePathLF.close()

        curvePathMF.reset()
        curvePathMF.moveTo(curvePoints[resolution].x, curvePoints[resolution].y)
        for (i in 1 until resolution) {
            curvePathMF.lineTo(curvePoints[resolution + i].x, curvePoints[resolution + i].y)
        }
        curvePathMF.close()

        curvePathHF.reset()
        curvePathHF.moveTo(curvePoints[resolution * 2].x, curvePoints[resolution * 2].y)
        for (i in 1 until resolution) {
            curvePathHF.lineTo(curvePoints[resolution * 2 + i].x, curvePoints[resolution * 2 + i].y)
        }
        curvePathHF.close()
    }

    private fun getWaveHeight(index: Int): Float {
        if (index < wavePoints.size) {
            return min(1f, max(0f, wavePoints[index].displayValue)) * maxDrawingWidth * 0.4f
        }
        return 0f
    }
}