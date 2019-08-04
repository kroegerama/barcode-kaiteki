package com.kroegerama.kaiteki.bcode

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.os.Build
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

internal fun AttributeSet?.handleArguments(
    context: Context, attrs: IntArray, defStyleAttr: Int, defStyleRes: Int,
    block: TypedArray.() -> Unit
) = this?.let {
    val arr = context.obtainStyledAttributes(it, attrs, defStyleAttr, defStyleRes)
    block(arr)
    arr.recycle()
}

internal typealias Style = R.styleable

internal val Context.hasCameraPermission
    get() = isPermissionGranted(Manifest.permission.CAMERA)

internal fun Fragment.requestCameraPermission(requestCode: Int) =
    requestPermission(Manifest.permission.CAMERA, requestCode)

internal val IntArray.isPermissionGranted
    get() = size > 0 && get(0) == PackageManager.PERMISSION_GRANTED

private fun Context.isPermissionGranted(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun Fragment.requestPermission(permission: String, requestCode: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    if (context?.isPermissionGranted(permission) == true) return
    requestPermissions(arrayOf(permission), requestCode)
}

internal class Debouncer(
    val debounceTime: Int
) {
    private var lastShot = 0L

    operator fun <T> invoke(block: () -> T) = if (System.currentTimeMillis() - lastShot > debounceTime) {
        block.invoke().also {
            lastShot = System.currentTimeMillis()
        }
    } else {
        null
    }
}