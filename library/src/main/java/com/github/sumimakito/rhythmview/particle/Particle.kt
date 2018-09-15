package com.github.sumimakito.rhythmview.particle

import android.graphics.PointF
import com.github.sumimakito.rhythmview.MathUtils
import kotlin.math.cos
import kotlin.math.sin

class Particle(private val startX: Float, private val startY: Float, private val deg: Float, private val velocity: Float, private val lifespan: Float) {
    constructor(startPoint: PointF, direction: Float, velocity: Float, lifespan: Float) : this(startPoint.x, startPoint.y, direction, velocity, lifespan)

    companion object {
        private const val IGNORANCE_THRESHOLD = 0.0001f
    }

    private val deltaX = (lifespan * velocity * cos(MathUtils.deg2rad(deg.toDouble()))).toFloat()
    private val deltaY = -(lifespan * velocity * sin(MathUtils.deg2rad(deg.toDouble()))).toFloat()

    var recyclable: Boolean = false
    var x: Float = startX
    var y: Float = startY
    var life = 1f

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