package com.kroegerama.bcode

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.kroegerama.kaiteki.bcode.BarcodeResultListener
import com.kroegerama.kaiteki.bcode.ui.BarcodeBottomSheet
import com.kroegerama.kaiteki.bcode.ui.BarcodeDialog
import com.kroegerama.kaiteki.bcode.ui.showBarcodeAlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BarcodeResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnDialogFragment.setOnClickListener {
            BarcodeDialog.show(
                supportFragmentManager,
                formats = listOf(BarcodeFormat.QR_CODE),
                barcodeInverted = cbInverted.isChecked
            )
        }
        btnBottomSheet.setOnClickListener {
            BarcodeBottomSheet.show(
                supportFragmentManager,
                formats = listOf(BarcodeFormat.QR_CODE),
                barcodeInverted = cbInverted.isChecked
            )
        }
        btnAlertDialog.setOnClickListener {
            showBarcodeAlertDialog(
                owner = this,
                listener = this,
                formats = listOf(BarcodeFormat.QR_CODE),
                barcodeInverted = cbInverted.isChecked
            )
        }
    }

    override fun onBarcodeResult(result: Result): Boolean {
        Log.d(TAG, "Result: $result")

        //return false to not automatically close the dialog
        return false
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
