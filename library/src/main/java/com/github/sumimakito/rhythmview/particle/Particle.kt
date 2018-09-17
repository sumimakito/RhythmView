package com.github.sumimakito.rhythmview.particle

import android.graphics.PointF
import com.github.sumimakito.rhythmview.util.MathUtils
import kotlin.math.cos
import kotlin.math.sin

/**
 * An abstract particle for the particle system.
 */
class Particle(private val startX: Float, private val startY: Float, private val deg: Float, private val velocity: Float, private val lifespan: Float) {
    constructor(startPoint: PointF, direction: Float, velocity: Float, lifespan: Float) : this(startPoint.x, startPoint.y, direction, velocity, lifespan)

    companion object {
        /**
         * To avoid the loss of precision, a rather small threshold is set to recycle more particles.
         */
        private const val IGNORANCE_THRESHOLD = 0.0001f
    }

    private val deltaX = (lifespan * velocity * cos(MathUtils.deg2rad(deg.toDouble()))).toFloat()
    private val deltaY = -(lifespan * velocity * sin(MathUtils.deg2rad(deg.toDouble()))).toFloat()

    var recyclable: Boolean = false
        private set

    var x: Float = startX
        private set

    var y: Float = startY
        private set

    var life = 1f
        private set

    /**
     * Remember to call `nextTick()` after rendering each frame.
     *
     * If the particle is managed by `ParticleManager`, then call `nextTick()` in the manager instead.
     */
    fun nextTick() {
        life -= velocity
        if (life > IGNORANCE_THRESHOLD) {
            x += deltaX
            y += deltaY
        } else {
            recyclable = true
        }
    }
}