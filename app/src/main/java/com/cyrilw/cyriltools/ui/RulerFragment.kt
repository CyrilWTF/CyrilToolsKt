package com.cyrilw.cyriltools.ui

import android.view.View
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.base.BaseFragment
import com.cyrilw.cyriltools.component.RulerView

class RulerFragment : BaseFragment() {

    override val layoutRes: Int
        get() = R.layout.fragment_ruler
    override val menuRes: Int?
        get() = R.menu.menu_ruler

    private val mRuler: RulerView by bindView(R.id.ruler)

    override fun initView(view: View) {
        setToolbarTitle(Constant.FEATURE_RULER)
        setShowBack()
    }

    override fun setMenu(itemId: Int) {
        when (itemId) {
            R.id.menu_reset -> mRuler.reset()
        }
    }

}