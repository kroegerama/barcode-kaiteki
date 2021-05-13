package com.kroegerama.kaiteki.bcode.views

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.google.zxing.Result
import com.kroegerama.kaiteki.bcode.BuildConfig
import com.kroegerama.kaiteki.bcode.Style
import kotlin.math.max

class ResultPointView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pPoints = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.GREEN
        strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, Resources.getSystem().displayMetrics)
        strokeCap = Paint.Cap.ROUND
    }

    private var resultPoints = floatArrayOf()
    private var rect = RectF()

    var showResultPoints = true
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attrs, Style.ResultPointView, defStyleAttr) {
            showResultPoints = getBoolean(Style.ResultPointView_showResultPoints, showResultPoints)

            pPoints.color = getColor(Style.ResultPointView_resultPointColor, pPoints.color)
            pPoints.strokeWidth = getDimension(Style.ResultPointView_resultPointSize, pPoints.strokeWidth)
        }
    }

    fun setResultPointColor(@ColorInt color: Int) {
        pPoints.color = color
        invalidate()
    }

    fun setPointSize(size: Float) {
        pPoints.strokeWidth = size
    }

    fun clear() {
        resultPoints = floatArrayOf()

        postInvalidate()
    }

    fun setResult(result: Result, imageWidth: Int, imageHeight: Int, imageRotation: Int) {
        if (!showResultPoints) return

        val localMatrix = createMatrix(imageWidth.toFloat(), imageHeight.toFloat(), imageRotation)

        resultPoints = result.resultPoints.flatMap { listOf(it.x, it.y) }.toFloatArray()
        localMatrix.mapPoints(resultPoints)

        if (BuildConfig.DEBUG) {
            rect = RectF(0f, 0f, imageWidth.toFloat(), imageHeight.toFloat())
            localMatrix.mapRect(rect)
        }

        postInvalidate()
    }

    private fun createMatrix(imageWidth: Float, imageHeight: Float, imageRotation: Int) = Matrix().apply {
        preTranslate((width - imageWidth) / 2f, (height - imageHeight) / 2f)
        preRotate(imageRotation.toFloat(), imageWidth / 2f, imageHeight / 2f)

        val wScale: Float
        val hScale: Float

        if (imageRotation % 180 == 0) {
            wScale = width.toFloat() / imageWidth
            hScale = height.toFloat() / imageHeight
        } else {
            wScale = height.toFloat() / imageWidth
            hScale = width.toFloat() / imageHeight

        }

        val scale = max(wScale, hScale)
        preScale(scale, scale, imageWidth / 2f, imageHeight / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        if (showResultPoints) canvas.drawPoints(resultPoints, pPoints)

        if (BuildConfig.DEBUG && !rect.isEmpty) canvas.drawRect(rect, pPoints)
    }
}