package com.kroegerama.kaiteki.bcode

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

internal interface ResultListener {
    fun onResult(result: Result, imageWidth: Int, imageHeight: Int, imageRotation: Int)
    fun onNoResult()
}

internal class BarcodeAnalyzer(
    private val listener: ResultListener,
    private val reader: MultiFormatReader
) : ImageAnalysis.Analyzer {

    var enabled = true

    override fun analyze(image: ImageProxy, rotationDegrees: Int) {
        if (!enabled) return

        //YUV_420 is normally the input type here, but other YUV types are also supported in theory
        if (ImageFormat.YUV_420_888 != image.format && ImageFormat.YUV_422_888 != image.format && ImageFormat.YUV_444_888 != image.format) {
            Log.e(TAG, "Unexpected format: ${image.format}")
            listener.onNoResult()
            return
        }
        val byteBuffer = image.image?.planes?.firstOrNull()?.buffer
        if (byteBuffer == null) {
            listener.onNoResult()
            return
        }

        val data = ByteArray(byteBuffer.remaining()).also { byteBuffer.get(it) }

        val width = image.width
        val height = image.height

        val source = PlanarYUVLuminanceSource(data, width, height, 0, 0, width, height, false)
//        val thumb = Bitmap.createBitmap(source.thumbnailWidth, source.thumbnailHeight, Bitmap.Config.ARGB_8888).apply {
//            val ints = source.renderThumbnail()
//            copyPixelsFromBuffer(IntBuffer.wrap(ints))
//        }
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decodeWithState(bitmap)
            listener.onResult(result, width, height, rotationDegrees)
        } catch (e: Exception) {
            listener.onNoResult()
        }
    }

    companion object {
        private const val TAG = "BarcodeAnalyzer"
    }
}