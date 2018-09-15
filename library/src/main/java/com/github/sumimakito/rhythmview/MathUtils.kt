package com.github.sumimakito.rhythmview

import android.graphics.PointF

class MathUtils {
    companion object {
        fun getPointOnCircle(center: PointF, radius: Float, deg: Float): PointF {
            val rad = deg2rad(deg.toDouble())
            val xDelta = (radius * Math.cos(rad)).toFloat()
            val yDelta = -(radius * Math.sin(rad)).toFloat()
            return PointF(center.x + xDelta, center.y + yDelta)
        }

        fun getPointOnCircle(x: Float, y: Float, radius: Float, deg: Float): PointF {
            return getPointOnCircle(PointF(x, y), radius, deg)
        }

        fun deg2rad(deg: Double): Double {
            return deg / 360.0 * 2 * Math.PI
        }
    }
}