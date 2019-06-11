package com.cyrilw.cyriltools.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.base.BaseFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var currentFragment: Fragment
    private val mTagStack: Stack<String> = Stack()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        currentFragment = MainFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment_container, currentFragment, Constant.MAIN_FRAGMENT_TAG)
            .commitNow()
    }

    override fun onBackPressed() {
        if (currentFragment is MainFragment) {
            super.onBackPressed()
        } else {
            switchBack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = false

    fun switchTo(targetTag: String, mode: Int) {
        mTagStack.push(currentFragment.tag)
        switchFragment(targetTag, mode)
    }

    fun switchBack() {
        val upperTag = mTagStack.pop()
        switchFragment(upperTag, 0)
        if (upperTag == Constant.MAIN_FRAGMENT_TAG) {
            supportActionBar?.title = title
        }
    }

    fun showSnackBar(message: String) {
        Snackbar.make(coordinate_layout, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun switchFragment(targetTag: String, mode: Int) {
        val transaction =
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        var fragment = supportFragmentManager.findFragmentByTag(targetTag)
        if (fragment == null) {
            fragment = when (targetTag) {
                Constant.MAIN_FRAGMENT_TAG -> MainFragment()
                Constant.APP_LIST_FRAGMENT_TAG -> AppListFragment()
                Constant.MISCELLANEOUS_FRAGMENT_TAG -> MiscellaneousFragment()
                Constant.QR_CODE_FRAGMENT_TAG -> QRCodeFragment()
                Constant.RULER_FRAGMENT_TAG -> RulerFragment()
                Constant.TEXT_CONVERT_TAG -> TextConvertFragment()
                else -> null
            }
            fragment?.let {
                if (mode != 0) {
                    (it as? BaseFragment)?.setMode(mode)
                }
                transaction.hide(currentFragment).add(R.id.fragment_container, it, targetTag)
                    .commit()
                currentFragment = it
            }
        } else {
            if (mode != 0) {
                (fragment as? BaseFragment)?.setMode(mode)
            }
            transaction.hide(currentFragment).show(fragment).commit()
            currentFragment = fragment
        }

    }

}
