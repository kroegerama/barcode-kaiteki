package com.kroegerama.kaiteki.bcode.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Matrix
import android.os.Handler
import android.os.HandlerThread
import android.util.*
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.kroegerama.kaiteki.bcode.*
import com.kroegerama.kaiteki.bcode.R
import kotlin.math.max

class BarcodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ResultListener {

    private val textureView: TextureView
    private val resultView: ResultPointView

    private var bufferSize = SizeF(0f, 0f)

    private var listener: BarcodeResultListener? = null

    private val barcodeReader by lazy { MultiFormatReader() }

    private val analyzer by lazy { BarcodeAnalyzer(this, barcodeReader) }

    private val resultDebouncer = Debouncer(500)

    init {
        LayoutInflater.from(context).inflate(R.layout.barcode_view, this)

        keepScreenOn = true

        textureView = findViewById(R.id.textureView)
        resultView = findViewById(R.id.resultView)

        attrs.handleArguments(context, Style.BarcodeView, defStyleAttr, 0) {
            resultView.showResultPoints = getBoolean(Style.BarcodeView_showResultPoints, true)
            resultView.setResultPointColor(getColor(Style.BarcodeView_resultPointColor, Color.GREEN))
            val defaultSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, Resources.getSystem().displayMetrics)
            resultView.setPointSize(getDimension(Style.BarcodeView_resultPointSize, defaultSize))
            analyzer.inverted = getBoolean(Style.BarcodeView_barcodeInverted, false)
        }

        textureView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    override fun onResult(result: Result, imageWidth: Int, imageHeight: Int, imageRotation: Int) {
        resultView.setResult(result, imageWidth, imageHeight, imageRotation)

        val d = resultDebouncer {
            listener?.onBarcodeResult(result)
        }
        if (d == true) {
            // dialog/fragment will be dismissed -> do not send any more events
            listener = null
        }
    }

    override fun onNoResult() {
        resultView.clear()
    }

    fun setBarcodeResultListener(listener: BarcodeResultListener) {
        this.listener = listener
    }

    /**
     * enable scanning of inverted barcodes (e.g. white QR Code on black background)
     */
    fun setBarcodeInverted(inverted: Boolean) {
        analyzer.inverted = inverted
    }

    fun bindToLifecycle(owner: LifecycleOwner) {
        textureView.post { startPreview(owner) }
    }

    fun unbind() {
        resultView.clear()
        listener = null
        CameraX.unbindAll()
    }

    fun setFormats(formats: List<BarcodeFormat>) = barcodeReader.setHints(
        mapOf(
            DecodeHintType.POSSIBLE_FORMATS to formats
        )
    )

    private fun startPreview(owner: LifecycleOwner) {
        val metrics = DisplayMetrics().also { textureView.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        val screenRotation = textureView.display.rotation

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetResolution(screenSize / 2)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(screenRotation)
        }.build()

        val preview = Preview(previewConfig).apply {
            setOnPreviewOutputUpdateListener(::previewOutputUpdated)
        }

        val analysisConfig = ImageAnalysisConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.BACK)
            setTargetResolution(screenSize / 2)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(textureView.display.rotation)

            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            val analyzerThread = HandlerThread("BarcodeAnalyzer").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
        }.build()

        val analysis = ImageAnalysis(analysisConfig).apply { analyzer = this@BarcodeView.analyzer }

        CameraX.bindToLifecycle(owner, preview, analysis)
    }

    private fun previewOutputUpdated(output: Preview.PreviewOutput) {
        // https://github.com/android/camera/blob/848cf1e2c8404599050d79086dee1d0c8951b66e/CameraXBasic/app/src/main/java/com/android/example/cameraxbasic/utils/AutoFitPreviewBuilder.kt#L100
        (textureView.parent as? ViewGroup)?.apply {
            val idx = indexOfChild(textureView)
            removeView(textureView)
            addView(textureView, idx)
            textureView.surfaceTexture = output.surfaceTexture
        }

        bufferSize = SizeF(output.textureSize.height.toFloat(), output.textureSize.width.toFloat())
        updateTransform()
    }

    private fun updateTransform() {
        val viewFinderWidth = textureView.width.toFloat()
        val viewFinderHeight = textureView.height.toFloat()
        val viewFinderRotation = when (textureView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        val matrix = Matrix()
        val centerX = viewFinderWidth / 2f
        val centerY = viewFinderHeight / 2f

        matrix.postRotate(-viewFinderRotation.toFloat(), centerX, centerY)

        val bufferRatio = bufferSize.width / bufferSize.height
        val viewRatio = viewFinderWidth / viewFinderHeight

        if (bufferRatio > viewRatio) {
            val factor = bufferRatio / viewRatio
            matrix.preScale(factor, 1f, centerX, centerY)
        } else {
            val factor = viewRatio / bufferRatio
            matrix.preScale(1f, factor, centerX, centerY)
        }

        if (viewFinderRotation % 180 != 0) {
            if (bufferRatio > viewRatio) {
                val s = 1f / bufferRatio
                matrix.preScale(s, s, centerX, centerY)
            } else {
                val s = max(bufferRatio, 1f / viewRatio)
                matrix.preScale(s, s, centerX, centerY)
            }
        }

//        val dbgScale = .95f
//        matrix.preScale(dbgScale, dbgScale, centerX, centerY)

        textureView.setTransform(matrix)
    }
}

private operator fun Size.div(other: Int): Size = Size(width / other, height / other)
