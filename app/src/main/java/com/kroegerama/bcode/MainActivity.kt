package com.kroegerama.bcode

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.kroegerama.kaiteki.baseui.BaseFragmentActivity
import com.kroegerama.kaiteki.bcode.ui.BarcodeFragment
import kotlinx.android.synthetic.main.ac_main.*

class MainActivity : BaseFragmentActivity<Navigation>(
    layout = R.layout.ac_main,
    fragmentContainer = R.id.container,
    startIndex = Navigation.DialogExamples
) {

    override fun setupGUI() {
        btnSwitch.setOnClickListener { switchFragments() }
    }

    private fun switchFragments() {
        if (navigator.selection == Navigation.DialogExamples) {
            navigator.show(Navigation.BarcodeFragment)
        } else {
            navigator.show(Navigation.DialogExamples)
        }
    }

    override fun createFragment(index: Navigation, payload: Any?): Fragment = when (index) {
        Navigation.DialogExamples -> FragDialogExamples()
        Navigation.BarcodeFragment -> BarcodeFragment.makeInstance(
            formats = listOf(BarcodeFormat.QR_CODE),
            barcodeInverted = false
        )
    }

    override fun saveIndexState(index: Navigation, key: String, bundle: Bundle) {
        bundle.putInt(key, index.ordinal)
    }

    override fun loadIndexState(key: String, bundle: Bundle): Navigation? =
        bundle.getInt(key, -1).let { Navigation.values().getOrNull(it) }

    override fun onFragmentSelected(index: Navigation, fragment: Fragment) {
        btnSwitch.text = when (index) {
            Navigation.DialogExamples -> "Go to Simple Fragment"
            Navigation.BarcodeFragment -> "Go to Dialog Examples"
        }
    }

    override fun onBackPressed() {
        if (navigator.selection != Navigation.DialogExamples) {
            navigator.show(Navigation.DialogExamples)
            return
        }

        super.onBackPressed()
    }
}
