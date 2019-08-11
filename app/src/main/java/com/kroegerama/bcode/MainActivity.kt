package com.kroegerama.bcode

import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.kroegerama.kaiteki.FragmentNavigator
import com.kroegerama.kaiteki.baseui.BaseActivity
import com.kroegerama.kaiteki.bcode.ui.BarcodeFragment
import kotlinx.android.synthetic.main.ac_main.*

class MainActivity : BaseActivity(), FragmentNavigator.FragmentProvider<Navigation> {

    override val layoutResource = R.layout.ac_main
    override val fragmentContainer = R.id.container

    private val navigator by lazy { FragmentNavigator(supportFragmentManager, this) }

    override fun setupGUI() {
        btnSwitch.setOnClickListener { switchFragments() }
    }

    override fun run(runState: RunState) {
        if (!navigator.hasSelection) {
            navigator.show(Navigation.DialogExamples)
        }
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
