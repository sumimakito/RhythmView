package com.github.sumimakito.rhythmviewdemo

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.sumimakito.rhythmview.RhythmView
import com.github.sumimakito.rhythmview.datasource.PlaybackSource
import com.github.sumimakito.rhythmview.effect.Ray
import com.github.sumimakito.rhythmview.effect.Ripple
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var dataSource: PlaybackSource? = null
    private var mediaPlayer: MediaPlayer? = null
    private var divisionValue: Int = 8
    private var waveSpeedValue: Float = 0.06f
    private var particleSpeedValue: Float = 0.01f
    private var colorH: Int = 0xFFFFFFFF.toInt()
    private var colorM: Int = 0xFFFFFFFF.toInt()
    private var colorL: Int = 0xFFFFFFFF.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0xCEE)

        rhythmView.onRhythmViewLayoutChangedListener = object : RhythmView.OnRhythmViewLayoutChangedListener {
            override fun onLayoutChanged(rhythmView: RhythmView) {
                if (rhythmView.albumCover == null) {
                    val cover = BitmapFactory.decodeResource(resources, R.raw.cover)
                    colorM = getDominantColor(cover)
                    val hsvL = FloatArray(3)
                    val hsvM = FloatArray(3)
                    val hsvD = FloatArray(3)
                    Color.colorToHSV(colorM, hsvL)
                    Color.colorToHSV(colorM, hsvM)
                    Color.colorToHSV(colorM, hsvD)
                    hsvL[1] = 0.40f
                    hsvM[1] = 0.40f
                    hsvD[1] = 0.40f
                    hsvL[2] = 0.98f
                    hsvM[2] = 0.92f
                    hsvD[2] = 0.86f
                    colorH = Color.HSVToColor(hsvL)
                    colorM = Color.HSVToColor(hsvM)
                    colorL = Color.HSVToColor(hsvD)
                    rhythmView.albumCover = cover
                }
                reloadVisualEffect()
            }
        }

        settingsToggle.setOnClickListener {
            optionsInnerContainer.visibility = if (optionsInnerContainer.visibility == View.GONE) VISIBLE else GONE
        }

        innerPadding.progress = (rhythmView.innerDrawingPaddingScale * 100).toInt()
        innerPaddingDisplay.text = "${rhythmView.innerDrawingPaddingScale}"
        innerPadding.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                rhythmView.innerDrawingPaddingScale = progress / 100f
                innerPaddingDisplay.text = "${rhythmView.innerDrawingPaddingScale}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        outerDrawingSize.progress = (rhythmView.maxDrawingWidthScale * 100).toInt()
        outerDrawingSizeDisplay.text = "${rhythmView.maxDrawingWidthScale}"
        outerDrawingSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                rhythmView.maxDrawingWidthScale = progress / 100f
                outerDrawingSizeDisplay.text = "${rhythmView.maxDrawingWidthScale}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        division.progress = divisionValue - 4
        divisionDisplay.text = "$divisionValue"
        division.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                divisionValue = progress + 4
                divisionDisplay.text = "$divisionValue"
                reloadVisualEffect()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        waveSpeed.progress = (waveSpeedValue * 1000).toInt()
        waveSpeedDisplay.text = "$waveSpeedValue"
        waveSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                waveSpeedValue = progress / 1000f
                waveSpeedDisplay.text = "$waveSpeedValue"
                reloadVisualEffect()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        particleSpeed.progress = (particleSpeedValue * 1000).toInt() - 5
        particleSpeedDisplay.text = "$particleSpeedValue"
        particleSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                particleSpeedValue = (progress + 5) / 1000f
                particleSpeedDisplay.text = "$particleSpeedValue"
                reloadVisualEffect()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        visualEffect.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonRipple -> {
                    divisionValue = 8
                    dataSource = PlaybackSource(mediaPlayer!!, 3 * divisionValue)
                    rhythmView.dataSource = dataSource!!
                    waveSpeedValue = 0.04f
                    particleSpeedValue = 0.01f
                    waveSpeed.progress = (waveSpeedValue * 1000).toInt()
                    division.max = 12
                    division.progress = divisionValue - 4
                    waveSpeedDisplay.text = "$waveSpeedValue"
                    divisionDisplay.text = "$divisionValue"
                    particleSpeed.progress = (particleSpeedValue * 1000).toInt() - 5
                    particleSpeedDisplay.text = "$particleSpeedValue"
                    particleSpeed.isEnabled = true
                }
                R.id.radioButtonRay -> {
                    divisionValue = 256
                    dataSource = PlaybackSource(mediaPlayer!!, 3 * divisionValue)
                    rhythmView.dataSource = dataSource!!
                    waveSpeedValue = 0.04f
                    waveSpeed.progress = (waveSpeedValue * 1000).toInt()
                    division.max = 252
                    division.progress = divisionValue - 4
                    waveSpeedDisplay.text = "$waveSpeedValue"
                    divisionDisplay.text = "$divisionValue"
                    particleSpeed.isEnabled = false
                }
            }
            reloadVisualEffect()
        }
    }

    private fun getDominantColor(bitmap: Bitmap): Int {
        val newBitmap = Bitmap.createScaledBitmap(bitmap, 8, 8, true)
        var red = 0
        var green = 0
        var blue = 0
        var c = 0
        var r: Int
        var g: Int
        var b: Int
        for (y in 0 until newBitmap.getHeight()) {
            for (x in 0 until newBitmap.getHeight()) {
                val color = newBitmap.getPixel(x, y)
                r = color shr 16 and 0xFF
                g = color shr 8 and 0xFF
                b = color and 0xFF
                if (r > 200 || g > 200 || b > 200) continue
                red += r
                green += g
                blue += b
                c++
            }
        }
        newBitmap.recycle()
        if (c == 0) {
            return 0xFFFFFFFF.toInt()
        } else {
            red = Math.max(0, Math.min(0xFF, red / c))
            green = Math.max(0, Math.min(0xFF, green / c))
            blue = Math.max(0, Math.min(0xFF, blue / c))

            val hsv = FloatArray(3)
            Color.RGBToHSV(red, green, blue, hsv)
            hsv[2] = Math.max(hsv[2], 0.7f)

            return 0xFF shl 24 or Color.HSVToColor(hsv)
        }
    }

    private fun reloadVisualEffect() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.creativeminds)
            mediaPlayer!!.start()
        }
        if (dataSource == null) {
            dataSource = PlaybackSource(mediaPlayer!!, 3 * divisionValue)
            rhythmView.dataSource = dataSource!!
        }

        when (visualEffect.checkedRadioButtonId) {
            R.id.radioButtonRipple -> {
                val ripple = Ripple(rhythmView, divisionValue, waveSpeedValue, particleSpeedValue)
                ripple.colorLF = colorL
                ripple.colorMF = colorM
                ripple.colorHF = colorH
                ripple.colorParticle = colorM
                rhythmView.visualEffect = ripple
            }
            R.id.radioButtonRay -> {
                val ray = Ray(rhythmView, divisionValue, waveSpeedValue)
                ray.colorLF = colorL
                ray.colorMF = colorM
                ray.colorHF = colorH
                rhythmView.visualEffect = ray
            }
        }
    }
}
