package com.github.sumimakito.rhythmview.effect

import android.graphics.Canvas
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.datasource.BaseDataSource

abstract class BaseEffect<T>(val centerX: Float, val centerY: Float, val radius: Float, var minDrawingRadius: Float, var maxDrawingWidth: Float, var dataSource: BaseDataSource<T>? = null) {
    constructor(rhythmView: RhythmView, dataSource: BaseDataSource<T>? = null) : this(rhythmView.centerX, rhythmView.centerY, rhythmView.radius, rhythmView.minDrawingRadius, rhythmView.maxDrawingWidth, dataSource)

    abstract fun render(canvas: Canvas)
    abstract fun onFrameRendered()
}