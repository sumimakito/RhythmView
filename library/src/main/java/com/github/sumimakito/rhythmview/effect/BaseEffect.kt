package com.github.sumimakito.rhythmview.effect

import android.graphics.Canvas
import android.media.audiofx.Visualizer
import com.github.sumimakito.rhythmview.RhythmView

abstract class BaseEffect(val centerX: Float, val centerY: Float, val radius: Float, var minDrawingRadius: Float, var maxDrawingWidth: Float) {
    constructor(rhythmView: RhythmView) : this(rhythmView.centerX, rhythmView.centerY, rhythmView.radius, rhythmView.minDrawingRadius, rhythmView.maxDrawingWidth)

    abstract fun render(canvas: Canvas)
    abstract fun onFrameRendered()
    abstract fun setupVisualizer(visualizer: Visualizer)
}