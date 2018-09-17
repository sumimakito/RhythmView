package com.github.sumimakito.rhythmview.effect

import android.graphics.Canvas
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.datasource.BaseDataSource

/**
 * Base class for visual effects.
 *
 * You may create a customized visual effect by extending this class.
 *
 * `T` should be the type provided by the corresponding data source.
 */
abstract class BaseEffect<T>(val centerX: Float, val centerY: Float, val radius: Float, var minDrawingRadius: Float, var maxDrawingWidth: Float, var dataSource: BaseDataSource<T>? = null) {
    constructor(rhythmView: RhythmView, dataSource: BaseDataSource<T>? = null) : this(rhythmView.centerX, rhythmView.centerY, rhythmView.radius, rhythmView.minDrawingRadius, rhythmView.maxDrawingWidth, dataSource)

    /**
     * Called when a frame is being rendered.
     *
     * You should do rendering works on `canvas` in this function.
     */
    abstract fun render(canvas: Canvas)

    /**
     * Called when a frame has been rendered.
     *
     * You should call `nextTick()` functions in this function.
     */
    abstract fun onFrameRendered()
}