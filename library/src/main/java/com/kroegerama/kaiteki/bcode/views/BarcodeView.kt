package com.kroegerama.kaiteki.bcode.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.AttributeSet
import android.util.Size
import android.util.SizeF
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.kroegerama.kaiteki.bcode.*
import com.kroegerama.kaiteki.bcode.databinding.BarcodeViewBinding
import java.util.concurrent.Executors

class BarcodeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ResultListener {

    private val executor = Executors.newFixedThreadPool(1)

    private val binding = BarcodeViewBinding.inflate(LayoutInflater.from(context), this)

    private val cameraProvider by lazy { ProcessCameraProvider.getInstance(context).get() }

    private var bufferSize = SizeF(0f, 0f)

    private var listener: BarcodeResultListener? = null

    private val barcodeReader by lazy { MultiFormatReader() }

    private val analyzer by lazy { BarcodeAnalyzer(this, barcodeReader) }

    private val resultDebouncer = Debouncer(500)

    init {
        keepScreenOn = true

        context.withStyledAttributes(attrs, Style.BarcodeView, defStyleAttr) {
            binding.resultView.showResultPoints = getBoolean(Style.BarcodeView_showResultPoints, true)
            binding.resultView.setResultPointColor(getColor(Style.BarcodeView_resultPointColor, Color.GREEN))
            val defaultSize =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, Resources.getSystem().displayMetrics)
            binding.resultView.setPointSize(getDimension(Style.BarcodeView_resultPointSize, defaultSize))
            analyzer.inverted = getBoolean(Style.BarcodeView_barcodeInverted, false)
        }
    }

    override fun onResult(result: Result, imageWidth: Int, imageHeight: Int, imageRotation: Int) {
        binding.resultView.setResult(result, imageWidth, imageHeight, imageRotation)

        val d = resultDebouncer {
            listener?.onBarcodeResult(result)
        }
        if (d == true) {
            // dialog/fragment will be dismissed -> do not send any more events
            listener = null
        }
    }

    override fun onNoResult() {
        binding.resultView.clear()
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
        val preview = Preview.Builder()
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val analysis = ImageAnalysis.Builder().apply {
            setTargetResolution(Size(640, 480))
            setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        }.build().apply {
            setAnalyzer(executor, analyzer)
        }
        cameraProvider.unbindAll()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider.bindToLifecycle(owner, cameraSelector, preview, analysis)
    }

    fun unbind() {
        binding.resultView.clear()
        listener = null
        cameraProvider.unbindAll()
    }

    fun setFormats(formats: List<BarcodeFormat>) = barcodeReader.setHints(
        mapOf(
            DecodeHintType.POSSIBLE_FORMATS to formats
        )
    )
}

private operator fun Size.div(other: Int): Size = Size(width / other, height / other)
