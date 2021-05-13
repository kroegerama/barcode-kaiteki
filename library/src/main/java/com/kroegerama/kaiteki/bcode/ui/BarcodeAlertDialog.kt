package com.kroegerama.kaiteki.bcode.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.postDelayed
import androidx.lifecycle.LifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.kroegerama.kaiteki.bcode.BarcodeResultListener
import com.kroegerama.kaiteki.bcode.R
import com.kroegerama.kaiteki.bcode.hasCameraPermission
import com.kroegerama.kaiteki.bcode.views.BarcodeView


fun Context.showBarcodeAlertDialog(
    owner: LifecycleOwner,
    listener: BarcodeResultListener,
    formats: List<BarcodeFormat> = listOf(BarcodeFormat.QR_CODE),
    barcodeInverted: Boolean = false
) {
    if (!hasCameraPermission) {
        Log.w("BarcodeAlertDialog", "Camera permission required")
        Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
        return
    }

    val view = LayoutInflater.from(this).inflate(R.layout.dlg_barcode, null, false)
    val bcode = view.findViewById<BarcodeView>(R.id.bcode)
    val handler = Handler(Looper.getMainLooper())

    val dlg = AlertDialog.Builder(this)
        .setOnDismissListener { bcode.unbind() }
        .setView(view)
        .setOnCancelListener {
            listener.onBarcodeScanCancelled()
        }
        .setNegativeButton(android.R.string.cancel) { _, _ ->
            listener.onBarcodeScanCancelled()
        }
        .show()

    bcode.setFormats(formats)
    bcode.setBarcodeInverted(barcodeInverted)
    bcode.setBarcodeResultListener(object : BarcodeResultListener {
        override fun onBarcodeResult(result: Result): Boolean {
            val doDismiss = listener.onBarcodeResult(result)
            if (doDismiss) {
                handler.postDelayed(500) {
                    dlg.dismiss()
                }
            }
            return doDismiss
        }

        override fun onBarcodeScanCancelled() {
            //Ignore: BarcodeView will never emit this event
        }
    })

    bcode.bindToLifecycle(owner)
}