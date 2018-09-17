package com.github.sumimakito.rhythmview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.github.sumimakito.rhythmview.effect.BaseEffect
import java.text.DecimalFormat
import kotlin.math.min
import kotlin.math.round

/**
 * A view for visualizing rhythms.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class RhythmView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : View(context, attrs, defStyleAttr, defStyleRes) {

    private var paintText = Paint()
    private var paintBitmap = Paint()
    private var paintParticle = Paint()
    private var coverRotation: Float = 0f
    private var fps = 0f
    private var frames = 0
    private var frameTime: Long = 0
    private var coverSourceRect: Rect? = null
    private var coverTargetRect: Rect? = null
    private var renderLoopStarted: Boolean = false
    private var renderInterval = 15L
    internal var centerX: Float = 0f
    internal var centerY: Float = 0f
    internal var radius: Float = 0f
    internal var minDrawingRadius: Float = 0f
    internal var maxDrawingWidth: Float = 70f

    /**
     * Speed of the spinning cover.
     * Unit: degree
     */
    var coverSpinningSpeed: Float = 0.5f

    /**
     * If true, the cover will stop spinning.
     */
    var isPaused: Boolean = true

    /**
     * If true, an FPS counter will show up at the top left corner of the view.
     */
    var showFpsCounter: Boolean = false
    var innerDrawingPaddingScale: Float = 0.01f
        set(value) {
            field = value
            updateLayoutMetrics()
        }

    var maxDrawingWidthScale = 0.24f
        set(value) {
            field = value
            updateLayoutMetrics()
        }

    var onRhythmViewLayoutChangedListener: (RhythmView)->Unit = {_ -> }

    private var scaledAlbumCover: Bitmap? = null
    var albumCover: Bitmap? = null
        set(value) {
            field = value
            if (value == null) return
            val clippingPaint = Paint()
            clippingPaint.color = 0xFFFFFFFF.toInt()
            clippingPaint.isAntiAlias = true
            val size = min(value.width, value.height)
            val squareRaw = Bitmap.createScaledBitmap(value, size, size, true)
            field = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(albumCover)
            canvas.drawCircle((albumCover!!.width / 2).toFloat(), (albumCover!!.height / 2).toFloat(), (albumCover!!.width / 2).toFloat(), clippingPaint)
            clippingPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(squareRaw, 0f, 0f, clippingPaint)
            scaledAlbumCover = null
        }

    var visualEffect: BaseEffect<*>? = null

    init {
        paintText.color = Color.WHITE
        paintText.textSize = 36f
        paintText.alpha = 180
        paintText.isAntiAlias = true

        paintBitmap.color = Color.WHITE
        paintBitmap.isAntiAlias = true
        paintBitmap.isFilterBitmap = true

        paintParticle.color = Color.WHITE
        paintParticle.style = Paint.Style.FILL
        paintParticle.isAntiAlias = true
    }

    private fun updateLayoutMetrics() {
        val innerWidth = width - paddingLeft - paddingRight
        val innerHeight = height - paddingTop - paddingBottom
        val innerSize = Math.min(innerWidth, innerHeight)
        centerX = paddingLeft + innerWidth * .5f
        centerY = paddingTop + innerHeight * .5f
        maxDrawingWidth = innerSize * maxDrawingWidthScale / 2f
        radius = (innerSize - 2 * maxDrawingWidth - 2 * innerDrawingPaddingScale * innerSize) / 2f
        minDrawingRadius = radius + innerDrawingPaddingScale * innerSize
        coverTargetRect = null

        onRhythmViewLayoutChangedListener.invoke(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        updateLayoutMetrics()

        if (!renderLoopStarted) {
            renderLoopStarted = true
            post(object : Runnable {
                override fun run() {
                    if (visualEffect != null && visualEffect!!.dataSource != null) {
                        invalidate()
                    }
                    postDelayed(this, renderInterval)
                }
            })
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (showFpsCounter) {
            val df = DecimalFormat("0.00")
            canvas?.drawText("${df.format(fps)} f/s", 18f, 18f + paintText.textSize, paintText)
        }

        visualEffect?.render(canvas!!)

        if (albumCover != null) {
            canvas?.save()
            canvas?.rotate(coverRotation, centerX, centerY)
            if (coverTargetRect == null) {
                coverTargetRect = Rect(round(centerX - radius).toInt(), round(centerY - radius).toInt(), round(centerX + radius).toInt(), round(centerY + radius).toInt())
            }
            /**
             * Cache the scaled album cover for a better performance.
             */
            if (scaledAlbumCover == null) {
                canvas?.drawBitmap(albumCover, Rect(0, 0, albumCover!!.width, albumCover!!.height), coverTargetRect, paintBitmap)
                Thread {
                    scaledAlbumCover = Bitmap.createScaledBitmap(albumCover, coverTargetRect!!.width(), coverTargetRect!!.height(), true)
                    coverSourceRect = Rect(0, 0, scaledAlbumCover!!.width, scaledAlbumCover!!.height)
                }.start()
            } else {
                canvas?.drawBitmap(scaledAlbumCover, coverSourceRect, coverTargetRect, paintBitmap)
            }
            canvas?.restore()
        }

        visualEffect?.onFrameRendered()

        if (!isPaused) {
            coverRotation += coverSpinningSpeed
        }
        if (coverRotation >= 360f) {
            coverRotation = 0f
        }

        if (showFpsCounter) {
            val time = System.currentTimeMillis()
            val delta = time - frameTime
            if (delta > 1000) {
                frameTime = time
                fps = (frames.toFloat() / delta * 1000)
                frames = 0
            } else {
                frames++
            }
        }
    }
}