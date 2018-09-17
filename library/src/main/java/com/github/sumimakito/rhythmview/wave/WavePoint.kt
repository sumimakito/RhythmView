package com.github.sumimakito.rhythmview.wave

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Defines a point on the wave, which possesses its own resistance (velocity) towards value changes.
 *
 * Parameter `velocity` is calculated by dividing 1 by the frames for the motion tween.
 *     e.g. If `velocity == 0.1f`, it takes 10 frames for the animation to complete.
 */
class WavePoint(private val initialValue: Float, private val velocity: Float, private val minValue: Float, private val maxValue: Float) {
    /**
     * The value used for displaying or computing the wave path.
     *
     * Read-only for external accesses.
     */
    var displayValue: Float = initialValue
        private set

    /**
     * Final value for the wave point
     */
    private var targetValue: Float = initialValue
    private val delta: Float = abs((maxValue - minValue) * velocity)

    init {
        displayValue = initialValue
    }

    /**
     * Call `changeTo(newValue)` to set a final value for the wave point.
     */
    fun changeTo(newValue: Float) {
        targetValue = min(maxValue, max(minValue, newValue))
    }

    /**
     * Remember to call `nextTick()` after rendering each frame.
     */
    fun nextTick() {
        if (targetValue == displayValue) return;
        if (targetValue > displayValue) {
            displayValue = min(targetValue, displayValue + delta)
        } else {
            displayValue = max(targetValue, displayValue - delta)
        }
    }
}