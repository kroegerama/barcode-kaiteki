package com.kroegerama.kaiteki.bcode.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.kroegerama.kaiteki.bcode.BarcodeResultListener
import com.kroegerama.kaiteki.bcode.databinding.DlgBarcodeBinding
import com.kroegerama.kaiteki.bcode.hasCameraPermission
import com.kroegerama.kaiteki.bcode.isPermissionGranted
import com.kroegerama.kaiteki.bcode.requestCameraPermission

class BarcodeFragment : Fragment(), BarcodeResultListener {

    private var binding: DlgBarcodeBinding? = null

    @Suppress("UNCHECKED_CAST")
    private val formats: List<BarcodeFormat>? by lazy {
        arguments?.getSerializable(KEY_FORMATS) as? List<BarcodeFormat>
    }

    private val barcodeInverted by lazy {
        arguments?.getBoolean(KEY_INVERTED, false) ?: false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        DlgBarcodeBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding!!) {
            formats?.let(bcode::setFormats)
            bcode.setBarcodeInverted(barcodeInverted)
            bcode.setBarcodeResultListener(this@BarcodeFragment)

            if (requireContext().hasCameraPermission) {
                bcode.bindToLifecycle(this@BarcodeFragment)
            } else {
                requestCameraPermission(REQUEST_CAMERA)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA ->
                if (grantResults.isPermissionGranted)
                    binding?.bcode?.bindToLifecycle(this)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStop() {
        binding?.bcode?.unbind()
        super.onStop()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onBarcodeResult(result: Result): Boolean {
        if ((parentFragment as? BarcodeResultListener)?.onBarcodeResult(result) == true) {
            return true
        } else if ((activity as? BarcodeResultListener)?.onBarcodeResult(result) == true) {
            return true
        }
        return false
    }

    override fun onBarcodeScanCancelled() {
        //Ignore: BarcodeView will never emit this event
    }

    companion object {
        private const val KEY_FORMATS = "formats"
        private const val KEY_INVERTED = "inverted"

        private const val REQUEST_CAMERA = 0xbf_ca

        fun makeInstance(
            formats: List<BarcodeFormat> = listOf(BarcodeFormat.QR_CODE),
            barcodeInverted: Boolean = false
        ) = BarcodeFragment().apply {
            arguments = bundleOf(
                KEY_FORMATS to formats,
                KEY_INVERTED to barcodeInverted
            )
        }
    }
}