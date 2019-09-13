package com.kroegerama.kaiteki.bcode

import com.google.zxing.Result

interface BarcodeResultListener {

    /**
     * @param result zxing result
     *
     * @return return true to dismiss the dialog/fragment
     */
    fun onBarcodeResult(result: Result): Boolean

    fun onBarcodeScanCancelled()

}