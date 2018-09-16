package com.github.sumimakito.rhythmview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.media.audiofx.Visualizer
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.github.sumimakito.rhythmview.datasource.BaseDataSource
import com.github.sumimakito.rhythmview.effect.BaseEffect
import java.text.DecimalFormat
import kotlin.math.round


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class RhythmView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : View(context, attrs, defStyleAttr, defStyleRes) {

    private var paintRipple = Paint()
    private var paintText = Paint()
    private var paintFill = Paint()
    private var paintParticle = Paint()
    private var coverRotation: Float = 0f
    private var fps = 0f
    private var frames = 0
    private var frameTime: Long = 0
    private var visualizer: Visualizer? = null
    private var coverSourceRect: Rect? = null
    private var coverTargetRect: Rect? = null
    private var renderLoopStarted: Boolean = false
    private var renderCanvas: Boolean = false
    private var renderInterval = 15L
    var minDrawingRadius: Float = 0f
    var innerDrawingPaddingScale: Float = 0.01f
        set(value) {
            field = value
            updateLayoutMetrics()
        }

    var centerX: Float = 0f
    var centerY: Float = 0f
    var radius: Float = 0f
    var maxDrawingWidth: Float = 70f
    var maxDrawingWidthScale = 0.24f
        set(value) {
            field = value
            updateLayoutMetrics()
        }

    var onRhythmViewLayoutChangedListener: OnRhythmViewLayoutChangedListener? = null

    var dataSource: BaseDataSource<*>? = null

    private var scaledAlbumCover: Bitmap? = null
    var albumCover: Bitmap? = null
        set(value) {
            val clippingPaint = Paint()
            clippingPaint.color = 0xFFFFFFFF.toInt()
            clippingPaint.isAntiAlias = true
            field = Bitmap.createBitmap(value!!.width, value.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(albumCover)
            canvas.drawCircle((albumCover!!.width / 2).toFloat(), (albumCover!!.height / 2).toFloat(), (albumCover!!.width / 2).toFloat(), clippingPaint)
            clippingPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(value, 0f, 0f, clippingPaint)
            scaledAlbumCover = null
        }

    var visualEffect: BaseEffect<*>? = null

    init {
        paintRipple.color = Color.WHITE
        paintRipple.style = Paint.Style.FILL
        paintRipple.alpha = 60
        paintRipple.isAntiAlias = true

        paintText.color = Color.WHITE
        paintText.textSize = 98f
        paintText.alpha = 40
        paintText.isAntiAlias = true

        paintFill.color = Color.WHITE
        paintFill.style = Paint.Style.FILL
        paintFill.isAntiAlias = true
        paintFill.isFilterBitmap = true

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

        onRhythmViewLayoutChangedListener?.onLayoutChanged(this)
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

        val df = DecimalFormat("0.00")
        canvas?.drawText("${df.format(fps)} FPS", 18f, 108f, paintText)

        visualEffect?.render(canvas!!)

        if (albumCover != null) {
            canvas?.save()
            canvas?.rotate(coverRotation, centerX, centerY)
            if (coverTargetRect == null) {
                coverTargetRect = Rect(round(centerX - radius).toInt(), round(centerY - radius).toInt(), round(centerX + radius).toInt(), round(centerY + radius).toInt())
            }
            if (scaledAlbumCover == null) {
                canvas?.drawBitmap(albumCover, Rect(0, 0, albumCover!!.width, albumCover!!.height), coverTargetRect, paintFill)
                Thread {
                    scaledAlbumCover = Bitmap.createScaledBitmap(albumCover, coverTargetRect!!.width(), coverTargetRect!!.height(), true)
                    coverSourceRect = Rect(0, 0, scaledAlbumCover!!.width, scaledAlbumCover!!.height)
                }.start()
            } else {
                canvas?.drawBitmap(scaledAlbumCover, coverSourceRect, coverTargetRect, paintFill)
            }
            canvas?.restore()
        }

        visualEffect?.onFrameRendered()

        coverRotation += 0.5f
        if (coverRotation >= 360f) {
            coverRotation = 0f
        }
        val time = System.currentTimeMillis()
        val delta = time - frameTime
        if (delta > 1000) {
            frameTime = time
            fps = (frames.toFloat() / delta * 1000)
            frames = 0
        } else {
            frames++
        }
        renderCanvas = true
    }

    interface OnRhythmViewLayoutChangedListener {
        fun onLayoutChanged(rhythmView: RhythmView)
    }
}