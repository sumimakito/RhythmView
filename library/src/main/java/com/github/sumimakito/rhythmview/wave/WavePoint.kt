package com.github.sumimakito.rhythmview.wave

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class WavePoint(private val initialValue: Float, private val velocity: Float, private val minValue: Float, private val maxValue: Float) {
    var displayValue: Float = initialValue
    private var targetValue: Float = initialValue
    private val delta: Float = abs((maxValue - minValue) * velocity)

    init {
        displayValue = initialValue
    }

    fun changeTo(newValue: Float) {
        targetValue = min(maxValue, max(minValue, newValue))
    }

    fun nextTick() {
        if (targetValue == displayValue) return;
        if (targetValue > displayValue) {
            displayValue = min(targetValue, displayValue + delta)
        } else {
            displayValue = max(targetValue, displayValue - delta)
        }
    }
}