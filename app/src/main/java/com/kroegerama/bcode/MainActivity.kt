package com.kroegerama.bcode

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.kroegerama.bcode.databinding.AcMainBinding
import com.kroegerama.kaiteki.FragmentNavigator
import com.kroegerama.kaiteki.baseui.ViewBindingActivity
import com.kroegerama.kaiteki.bcode.ui.BarcodeFragment
import com.kroegerama.kaiteki.onClick

class MainActivity : ViewBindingActivity<AcMainBinding>(
    AcMainBinding::inflate
), FragmentNavigator.FragmentProvider<Navigation> {

    override val fragmentContainer = R.id.container

    private val navigator by lazy {
        FragmentNavigator(
            supportFragmentManager,
            this
        )
    }

    override fun AcMainBinding.setupGUI() {
        btnSwitch.onClick { switchFragments() }
    }

    override fun run() {
        if (!navigator.hasSelection) {
            navigator.show(Navigation.BarcodeFragment)
        }
    }

    override fun saveState(outState: Bundle) {
        navigator.saveState(outState, ::saveIndexState)
        super.saveState(outState)
    }

    override fun loadState(state: Bundle) {
        navigator.loadState(state, ::loadIndexState)
        super.loadState(state)
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

    private fun saveIndexState(index: Navigation, key: String, bundle: Bundle) {
        bundle.putInt(key, index.ordinal)
    }

    private fun loadIndexState(key: String, bundle: Bundle): Navigation? =
        bundle.getInt(key, -1).let { Navigation.values().getOrNull(it) }

    override fun onFragmentSelected(index: Navigation, fragment: Fragment) {
        binding.btnSwitch.text = when (index) {
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
